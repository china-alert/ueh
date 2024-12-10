package com.mcinfotech.event.transmit.timer;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.handler.domain.*;
import com.mcinfotech.event.transmit.config.MediaTypeConfig;
import com.mcinfotech.event.transmit.domain.MediaInfo;
import com.mcinfotech.event.transmit.push.HttpPusher;
import com.mcinfotech.event.utils.DateTimeUtils;
import com.mcinfotech.event.utils.FastJsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * date 2022/10/13 16:42
 *
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.timer
 */
@Component
public class RepeatNotificationTask {
    private static Logger log = LogManager.getLogger(RepeatNotificationTask.class);

    @Autowired
    HttpPusher httpPusher;

    @Autowired
    DataSource dataSource;

    @Resource
    ProjectInfo projectInfo;

    @Resource
    MediaTypeConfig notifySettingsConfig;

    /**
     * 未恢复告警重复通知
     */
    @Scheduled(cron = "${repeat.notification.cron}")
    public void repeatNotification() {
        //1.未恢复 剩余通知次数大于0 确认状态为未确认和进行中的告警
        ResultPattern alarmResult = getRepeatAlarm();
        if (!alarmResult.isSuccess() || alarmResult.isEmpty()) {
            return;
        }
        //2.获取告警的通知规则
        ResultPattern ruleResult = getRepeatRule(alarmResult.getDatas());
        if (!ruleResult.isSuccess() || ruleResult.isEmpty()) {
            return;
        }
        //3.判断规则有效性
        List<EventHandlerRule> rules = JSON.parseArray(JSON.toJSONString(ruleResult.getDatas()), EventHandlerRule.class);
        rules = rules.stream()
                .filter(rule -> DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        //4.判断是否达到重复通知时间
        List<EventHandlerRule> finalRules = rules;
        List<Map<String, Object>> alarms = alarmResult.getDatas().stream()
                .filter(alarm -> {
                    for (EventHandlerRule rule : finalRules) {
                        if ((""+alarm.get("RuleID")).equals(rule.getId()+"")) {
                            NoticeParam noticeParam = JSON.parseObject(rule.getParams(), NoticeParam.class);
                            int interval = noticeParam.getInterval() * 60 * 1000;
                            alarm.putAll(JSON.parseObject(JSON.toJSONString(rule), Map.class));
                            return Long.parseLong(alarm.get("NotificationTimestamp") + "") + interval <= LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(alarms)) {
            return;
        }
        //5.通知
        for (Map<String, Object> alarm : alarms) {
            String alarmString = JSON.toJSONString(alarm);
            EventHandlerRule rule = JSON.parseObject(alarmString, EventHandlerRule.class);
            NoticeParam noticeParam = JSON.parseObject(rule.getParams(), NoticeParam.class);
            RepeatNotification repeatNotification = JSON.parseObject(alarmString, RepeatNotification.class);
            List<String> noticeTypes = noticeParam.getNotificationName();
            String isDefaultNotification = noticeParam.getDefaultNotification();
            String effectStr = rule.getEffect();
            EventHandlerRuleEffect effectObj = JSON.parseArray(effectStr, EventHandlerRuleEffect.class).get(0);
            String effectValue = effectObj.getEffectValue();
            long projectId = projectInfo.getId();
            Map<String, List<EventHandleUser>> noticeList = new HashMap<>();
            Map<EventHandleUser, List> candidateVo = new HashMap<>();
            //5.1通知方式
            if ("Y".equalsIgnoreCase(isDefaultNotification)) {
                noticeTypes = getNoticeNamesById(alarm.get("RuleID"));
            }
            if (CollectionUtils.isEmpty(noticeTypes)) {
                continue;
            }
            //5.2通知人
            List<EventHandleUser> candidates;
            if (StringUtils.isNotBlank(effectValue)) {
                //按填写组织人员列表
                candidates = httpPusher.getCandidatesByConsole(effectValue, projectId, noticeTypes, alarm);
            } else {
                Object filterFlagObject = alarm.get(PlatformEventColumn.FIXED_COLUMN_FILTER_FLAG);
                Set<String> excludeMediaNames = httpPusher.getExcludeMediaNames(filterFlagObject);
                List<MediaInfo> mediaInfoList = notifySettingsConfig.getMediaTemplates(projectId, rule.getId(), excludeMediaNames);
                //按分组组织人员列表/按节点人员列表
                candidates = httpPusher.getCandidatesFromDb(projectId, alarm, noticeTypes, mediaInfoList);
            }
            if (CollectionUtils.isEmpty(candidates)) {
                continue;
            }
            //5.3通知归集
            for (String noticeType : noticeTypes) {
                List<EventHandleUser> intersectionList = new ArrayList<>();
                for (EventHandleUser candidate : candidates) {
                    List<String> candidateNotificationNames = candidate.getGroupSeverityNotification().getNotificationName();
                    if (candidateNotificationNames.contains(noticeType)) {
                        intersectionList.add(candidate);
                    }
                }
                List<EventHandleUser> candidateList = noticeList.get(noticeType);
                if (CollectionUtils.isNotEmpty(candidateList) && CollectionUtils.isNotEmpty(intersectionList)) {
                    List<EventHandleUser> union = new ArrayList<>(CollectionUtils.union(candidateList, intersectionList));
                    noticeList.put(noticeType, union);
                } else if (CollectionUtils.isEmpty(candidateList) && CollectionUtils.isNotEmpty(intersectionList)) {
                    noticeList.put(noticeType, intersectionList);
                }
            }
            //5.4通知日志归集
            for (EventHandleUser candidate : candidates) {
                List<String> effectTypes = candidateVo.get(candidate);
                List<String> intersectionList = candidate.getGroupSeverityNotification().getNotificationName();
                if (CollectionUtils.isNotEmpty(effectTypes) && CollectionUtils.isNotEmpty(intersectionList)) {
                    List<String> union = new ArrayList<>(CollectionUtils.union(effectTypes, intersectionList));
                    candidateVo.put(candidate, union);
                } else if (CollectionUtils.isEmpty(effectTypes) && CollectionUtils.isNotEmpty(intersectionList)) {
                    candidateVo.put(candidate, intersectionList);
                }
            }
            //5.5通知及通知日志
            long lastNoticeTime = 0;
            for (Map.Entry<String, List<EventHandleUser>> entry : noticeList.entrySet()) {
                String key = entry.getKey();
                List<EventHandleUser> value = entry.getValue();
                String flowStatus = "2";
                //记录日志
                lastNoticeTime = httpPusher.saveNotificationLog(projectId, alarm, candidateVo, key, flowStatus);
                //通知
                httpPusher.notify(key, value, alarm);
            }
            //5.6更新重复通知关系
            Integer repeatCount = repeatNotification.getNotificationCount();
            repeatNotification.setNotificationTimestamp(lastNoticeTime);
            repeatNotification.setNotificationCount(repeatCount - 1);
            if (repeatCount > 1) {
                updateRepeatNotification(repeatNotification);
            } else {
                deleteRepeatNotification(repeatNotification);
            }
        }
    }

    private void deleteRepeatNotification(RepeatNotification repeatNotification) {
        Map<String, Object> filter = JSON.parseObject(JSON.toJSONString(repeatNotification), Map.class);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectInfo.getId());
        params.setDcName("deleteRepeatNotification");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        DataServiceUtils.dataLoad(dataSource, params);
    }

    private void updateRepeatNotification(RepeatNotification repeatNotification) {
        Map<String, Object> filter = JSON.parseObject(JSON.toJSONString(repeatNotification), Map.class);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectInfo.getId());
        params.setDcName("updateRepeatNotification");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        DataServiceUtils.dataLoad(dataSource, params);
    }

    private List<String> getNoticeNamesById(Object ruleId) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("ruleId", ruleId);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectInfo.getId());
        params.setDcName("getNoticeNamesById");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        return DataServiceUtils.dataLoad(dataSource, params).getListData().stream().map(name->name+"").collect(Collectors.toList());
    }

    private ResultPattern getRepeatRule(List<Map<String, Object>> datas) {
        List<Object> ruleIds = datas.stream().map(data -> data.get("RuleID")).collect(Collectors.toList());
        Map<String, Object> filter = new HashMap<>();
        filter.put("ruleIds", ruleIds);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectInfo.getId());
        params.setDcName("getRepeatRule");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        return DataServiceUtils.dataLoad(dataSource, params);
    }

    private ResultPattern getRepeatAlarm() {
        Map<String, Object> filter = new HashMap<>();
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectInfo.getId());
        params.setDcName("getRepeatAlarm");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        return DataServiceUtils.dataLoad(dataSource, params);
    }

}

package com.mcinfotech.event.transmit.push;

import cn.mcinfotech.data.service.db.ColumnDefine;
import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mcinfotech.event.domain.EventSourceType;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.handler.config.EventHandlerRuleConfig;
import com.mcinfotech.event.handler.config.PlatformEventColumnConfig;
import com.mcinfotech.event.handler.domain.*;
import com.mcinfotech.event.push.IPusher;
import com.mcinfotech.event.transmit.algorithm.EventNotify;
import com.mcinfotech.event.transmit.config.CandidateConfig;
import com.mcinfotech.event.transmit.config.EventConfig;
import com.mcinfotech.event.transmit.config.MediaTypeConfig;
import com.mcinfotech.event.transmit.domain.MediaInfo;
import com.mcinfotech.event.transmit.domain.MediaType;
import com.mcinfotech.event.transmit.domain.ProtocolType;
import com.mcinfotech.event.transmit.domain.SocketParam;
import com.mcinfotech.event.transmit.domain.itsm.ItsmResult;
import com.mcinfotech.event.transmit.domain.vo.CandidateVo;
import com.mcinfotech.event.transmit.http.OpenFeignService;
import com.mcinfotech.event.transmit.utils.HttpUtils;
import com.mcinfotech.event.transmit.utils.SendmailUtil;
import com.mcinfotech.event.utils.FastJsonUtils;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mcinfotech.event.domain.EventConstant.eventColumnEventSeverityType;

/**
 * 通知转发
 */
@Component
public class HttpPusher implements IPusher<ProbeEventMessage> {
    private static Logger logger = LogManager.getLogger(HttpPusher.class);
    @Resource
    DataSource dataSource;
    @Resource
    ProjectInfo projectInfo;
    @Resource
    EventHandlerRuleConfig eventHandlerRulesConfig;
    @Resource
    PlatformEventColumnConfig ColumnDefineConfig;
    @Resource
    CandidateConfig candidateConfig;
    @Resource
    EventConfig eventConfig;
    @Resource
    MediaTypeConfig notifySettingsConfig;
    @Autowired
    OpenFeignService openFeignService;
    @Value("${component.status}")
    String componentStatus;
    @Value("${email.timeout}")
    int timeout;

    private AtomicLong messageCount = new AtomicLong(0);

    @Override
    public void push(ProbeEventMessage message) {
    }

    @Override
    public void push(String handlerType, List<ProbeEventMessage> messages) {
    }

    @Override
    public void push(String handlerType, ProbeEventMessage message) {
    }

    @Override
    public void push(List<ProbeEventMessage> messages) {
        long projectId = projectInfo.getId();
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("isEnable", "Y");
        List<ColumnDefine> columnDefines = ColumnDefineConfig.getPlatformEventColumn(this.projectInfo.getId(), conditions);
        //通知规则
        List<EventHandlerRule> rules = eventHandlerRulesConfig.getEventHandlerRules(projectInfo.getId(), null, EventSourceType.INTERNAL, "N");
        //每条消息单独处理
        for (ProbeEventMessage message : messages) {
            Map<String, Object> mapMessage = FastJsonUtils.stringToCollect(message.getMessageBody());
            //1.绕过过滤规则发送
            if (mapMessage.containsKey("events")) {
                //通知方式集合
                List<String> notifyTypes = (List<String>) mapMessage.get("notifyType");
                //通知接收人:控制台输入
                List<EventHandleUser> candidates = getCandidatesByConsole(mapMessage.get("person") + "", projectId);
                for (Map<String, Object> event : (List<Map<String, Object>>) mapMessage.get("events")) {
                    //确认状态
                    eventConfig.updateEventAcknowledgedStatus(event, mapMessage.get("acknoledger") + "", componentStatus);
                    //通知日志
                    saveNotificationLog(mapMessage, candidates, event);
                    if (CollectionUtils.isEmpty(notifyTypes)) {
                        continue;
                    }
                    if (CollectionUtils.isEmpty(candidates)) {
                        continue;
                    }
                    //通知
                    for (String mediaName : notifyTypes) {
                        notify(mediaName, candidates, event);
                    }
                }
            } else {
                //2.匹配过滤规则发送
                //根据屏蔽字段FilterFlag 判断该条消息是否不通知、不分享或者不开单
                Object filterFlagObject = mapMessage.get(PlatformEventColumn.FIXED_COLUMN_FILTER_FLAG);
                Set<String> excludeMediaNames = getExcludeMediaNames(filterFlagObject);
                //判断消息是否符合通知规则
                if (CollectionUtils.isNotEmpty(rules)) {
                    Map<String, List<EventHandleUser>> noticeList = new HashMap<>();
                    Map<EventHandleUser, List> candidateVo = new HashMap<>();
                    Map<String, Integer> delayNotice = new HashMap<>();
                    StringBuffer filterNames = new StringBuffer();
                    //是否有符合的发送规则
                    boolean flag = false;
                    for (EventHandlerRule rule : rules) {
                        boolean excute = EventNotify.excute(mapMessage, ColumnDefine.toMap(columnDefines), rule);
                        if (excute) {
                            //关联策略
                            if (filterNames.length() > 0) {
                                filterNames.append("#");
                            }
                            filterNames.append(rule.getName());

                            flag = flag || excute;
                            String effectStr = rule.getEffect();
                            EventHandlerRuleEffect effectObj = JSON.parseArray(effectStr, EventHandlerRuleEffect.class).get(0);
                            //通知方式
                            List<MediaInfo> mediaInfoList = notifySettingsConfig.getMediaTemplates(projectId, rule.getId(), excludeMediaNames);
                            if (CollectionUtils.isEmpty(mediaInfoList)) {
                                continue;
                            }
                            List<String> effectTypeList = mediaInfoList.stream().map(mediaInfo -> mediaInfo.getName()).collect(Collectors.toList());
                            //通知接收人
                            String effectValue = effectObj.getEffectValue();
                            List<EventHandleUser> candidates;
                            if (StringUtils.isNotBlank(effectValue)) {
                                //按填写组织人员列表
                                candidates = getCandidatesByConsole(effectValue, projectId, effectTypeList, mapMessage);
                            } else {
                                //按分组组织人员列表/按节点人员列表
                                candidates = getCandidatesFromDb(projectId, mapMessage, effectTypeList, mediaInfoList);
                            }

                            //按人员组织通知,用于记录日志
                            if (CollectionUtils.isNotEmpty(candidates)) {
                                for (EventHandleUser candidate : candidates) {
                                    List<String> effectTypes = candidateVo.get(candidate);
                                    List<String> intersectionList = candidate.getGroupSeverityNotification().getNotificationName();
//                                    if (CollectionUtils.isNotEmpty(effectTypeList)) {
//                                        intersectionList = getIntersectionMediaType(mapMessage, mediaInfoList, candidate);
//                                    }
                                    if (CollectionUtils.isNotEmpty(effectTypes) && CollectionUtils.isNotEmpty(intersectionList)) {
                                        List<String> union = new ArrayList<>(CollectionUtils.union(effectTypes, intersectionList));
                                        candidateVo.put(candidate, union);
                                    } else if (CollectionUtils.isEmpty(effectTypes) && CollectionUtils.isNotEmpty(intersectionList)) {
                                        candidateVo.put(candidate, intersectionList);
                                    }
                                }
                            }

                            //按通知模板组织通知
                            RepeatNotification repeatNotification = new RepeatNotification();
                            repeatNotification.setEventId(mapMessage.get("EventID") + "");
                            repeatNotification.setRuleId(rule.getId());
                            repeatNotification.setNotificationTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                            if (CollectionUtils.isNotEmpty(effectTypeList)) {
                                Integer delayTime = 0;
                                if (StringUtils.isNotBlank(rule.getParams())) {
                                    NoticeParam noticeParam = JSON.parseObject(rule.getParams(), NoticeParam.class);
                                    delayTime = noticeParam.getDelayTime();
                                    repeatNotification.setNotificationCount(noticeParam.getCount());
                                    repeatNotification.setNotificationTimestamp(repeatNotification.getNotificationTimestamp() + delayTime * 60 * 1000);
                                }
                                for (String effectType : effectTypeList) {
                                    //延迟通知列表，如果存在多个延迟时间，按最长的计算
                                    Integer delayTimeOld = delayNotice.get(effectType);
                                    if (delayTimeOld == null || delayTime > delayTimeOld) {
                                        delayNotice.put(effectType, delayTime);
                                    }

                                    //通知方式:通知人
//                                    List<EventHandleUser> intersectionList = null;
//                                    if (CollectionUtils.isNotEmpty(effectTypeList)) {
//                                        intersectionList = getIntersectionCandidate(mapMessage, effectType, candidates, mediaInfoList);
//                                    }
                                    List<EventHandleUser> intersectionList = new ArrayList<>();
                                    for (EventHandleUser candidate : candidates) {
                                        List<String> candidateNotificationNames = candidate.getGroupSeverityNotification().getNotificationName();
                                        if (candidateNotificationNames.contains(effectType)) {
                                            intersectionList.add(candidate);
                                        }
                                    }
                                    List<EventHandleUser> candidateList = noticeList.get(effectType);
                                    if (CollectionUtils.isNotEmpty(candidateList) && CollectionUtils.isNotEmpty(intersectionList)) {
                                        List<EventHandleUser> union = new ArrayList<>(CollectionUtils.union(candidateList, intersectionList));
                                        noticeList.put(effectType, union);
                                    } else if (CollectionUtils.isEmpty(candidateList) && CollectionUtils.isNotEmpty(intersectionList)) {
                                        noticeList.put(effectType, intersectionList);
                                    }
                                }
                            }
                            if (repeatNotification.getNotificationCount() > 0 && "1".equals(mapMessage.get(eventColumnEventSeverityType) + "")) {
                                saveRepeatNotification(repeatNotification);
                            }
                        }
                    }

                    //如果有符合的发送规则
                    if (flag && noticeList.size() > 0) {
                        //更新历史事件表关联的通知策略
                        mapMessage.put("RefNotifyRules", filterNames.toString());
                        DataLoadParams notifyParams = new DataLoadParams();
                        Map<Object, Object> notifyfilter = new HashMap<>();
                        notifyfilter.put("mapMessage", mapMessage);
                        notifyParams.setFilter(JSON.toJSONString(notifyfilter));
                        notifyParams.setDcName("updateRefNotifyRules");
                        notifyParams.setProjectId(projectId);
                        notifyParams.setStart(1);
                        notifyParams.setLimit(-10);
                        DataServiceUtils.dataLoad(dataSource, notifyParams);
                        if ("slave".equalsIgnoreCase(componentStatus)) {
                            notifyParams.setDcName("updateRefNotifyRulesMaster");
                            DataServiceUtils.dataLoad(dataSource, notifyParams);
                        }

                        //发送通知
                        for (Map.Entry<String, List<EventHandleUser>> entry : noticeList.entrySet()) {
                            String key = entry.getKey();
                            List<EventHandleUser> value = entry.getValue();
                            long delayTime = delayNotice.get(key) * 60 * 1000;
                            String eventID = (String) mapMessage.get("EventID");
                            Object recoveredStatus = mapMessage.get("recoveredStatus");
                            String eventSeverityType = mapMessage.get(eventColumnEventSeverityType) + "";
                            long lastOccurrence = (long) mapMessage.get("LastOccurrence");
                            String flowStatus = "2";
                            if ("1".equals(recoveredStatus + "")) {
                                //告警事件未恢复状态更新为进行中
                                flowStatus = "2";
                            } else if ("2".equals(recoveredStatus + "")) {
                                //告警事件已恢复事件状态更新为已完成
                                flowStatus = "1";
                            }

                            //1.告警事件
                            //延迟通知是针对告警事件，如果在delay时间间隔内未恢复则通知，恢复事件没必要延时通知
                            if ("1".equals(eventSeverityType)) {
                                if (delayTime == 0) {//未设置延迟时间
                                    //记录日志
                                    saveNotificationLog(projectId, mapMessage, candidateVo, key, flowStatus);
                                    //确认状态
                                    eventConfig.updateEventAcknowledgedStatus(mapMessage, flowStatus, projectId, componentStatus);
                                    //通知
                                    notify(key, value, mapMessage);
                                } else {//设置了延迟时间的告警事件
                                    long delayMilliSecond = lastOccurrence + delayTime - LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                                    Timer timer = new Timer();
                                    TimerTask timerTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                            DataLoadParams dataLoadParams = new DataLoadParams();
                                            HashMap<Object, Object> filter = new HashMap<>();
                                            filter.put("projectId", projectId);
                                            filter.put("eventId", eventID);
                                            dataLoadParams.setFilter(JSON.toJSONString(filter));
                                            dataLoadParams.setDcName("getEventCount");
                                            dataLoadParams.setProjectId(projectId);
                                            dataLoadParams.setStart(1);
                                            dataLoadParams.setLimit(-10);
                                            ResultPattern resultPattern = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
                                            if (resultPattern.isSuccess()) {
                                                if (Integer.valueOf(resultPattern.getStrData()) == 1) {
                                                    //记录日志
                                                    saveNotificationLog(projectId, mapMessage, candidateVo, key, "2");
                                                    //确认状态
                                                    eventConfig.updateEventAcknowledgedStatus(mapMessage, "2", projectId, componentStatus);
                                                    //发送消息
                                                    HttpPusher.this.notify(key, value, mapMessage);
                                                } else {
                                                    eventConfig.updateEventAcknowledgedStatus(mapMessage, "1", projectId, componentStatus);
                                                }
                                            }
                                        }
                                    };
                                    //1.任务  2.时间（毫秒）
                                    timer.schedule(timerTask, delayMilliSecond > 0 ? delayMilliSecond : 1);
                                }
                            }
                            if ("2".equals(eventSeverityType)) {
                                //2.恢复事件
                                //2.1告警事件已通知，恢复事件如果符合通知条件也通知
                                //2.2告警事件未通知，恢复事件即使符合通知条件也不通知
                                DataLoadParams dataLoadParams = new DataLoadParams();
                                HashMap<Object, Object> filter = new HashMap<>();
                                filter.put("projectId", projectId);
                                filter.put("eventId", eventID);
                                dataLoadParams.setFilter(JSON.toJSONString(filter));
                                dataLoadParams.setDcName("getProblemIdByOkId");
                                dataLoadParams.setProjectId(projectId);
                                dataLoadParams.setStart(1);
                                dataLoadParams.setLimit(-10);
                                ResultPattern res = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
                                if (!res.isSuccess() || res.isEmpty()) {
                                    continue;
                                }
                                String problemId = res.getListData().get(0) + "";
                                filter.put("eventId", problemId);
                                dataLoadParams.setFilter(JSON.toJSONString(filter));
                                dataLoadParams.setDcName("getCandidateByProblemId");
                                res = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
                                if (!res.isSuccess() || CollectionUtils.isEmpty(res.getListData())) {
                                    continue;
                                }
                                List<Object> noticeLogList = res.getListData();
                                List<CandidateVo> candidateVos = new ArrayList<>();
                                for (Object noticeLog : noticeLogList) {
                                    String noticeLogStr = String.valueOf(noticeLog);
                                    List<CandidateVo> candidateVosTemp = JSON.parseArray(noticeLogStr, CandidateVo.class);
                                    candidateVos.addAll(candidateVosTemp);
                                }
                                List<String> notifiedList = new ArrayList<>();
                                for (CandidateVo vo : candidateVos) {
                                    List<String> notificationTypes = vo.getNotification_types();
                                    int index = notificationTypes.indexOf(key) == -1 ? -1 : notificationTypes.indexOf(key);
                                    if (index != -1) {
                                        notifiedList.add(vo.getCandidate());
                                    }
                                }
                                Iterator<EventHandleUser> notifies = value.iterator();
                                Map<EventHandleUser, List> candidateVoTemp = new HashMap<>();
                                while (notifies.hasNext()) {
                                    EventHandleUser currentUser = notifies.next();
                                    if (notifiedList.contains(currentUser.getDomainName())) {
                                        candidateVoTemp.put(currentUser,Arrays.asList(key));
                                        continue;
                                    }
                                    notifies.remove();
                                }
                                if (value.size() > 0) {
                                    //记录日志
                                    saveNotificationLog(projectId, mapMessage, candidateVoTemp, key, flowStatus);
                                    //确认状态
                                    eventConfig.updateEventAcknowledgedStatus(mapMessage, flowStatus, projectId, componentStatus);
                                    //通知
                                    notify(key, value, mapMessage);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private void saveRepeatNotification(RepeatNotification repeatNotification) {
        DataLoadParams dataLoadParams = new DataLoadParams();
        Map<Object, Object> filter = JSON.parseObject(JSON.toJSONString(repeatNotification), Map.class);
        dataLoadParams.setFilter(JSON.toJSONString(filter));
        dataLoadParams.setDcName("saveRepeatNotification");
        dataLoadParams.setProjectId(projectInfo.getId());
        ResultPattern res = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
    }

    /**
     * @param filterFlagObject 来源：上级处理流程中的屏蔽功能 数据格式：["通知介质名称1","通知介质名称2","通知介质名称3","通知介质名称4"]
     * @return 去重后的通知介质名称
     */
    public Set<String> getExcludeMediaNames(Object filterFlagObject) {
        String filterFlagValue = filterFlagObject == null ? "" : filterFlagObject.toString();
        Set<String> excludeMediaNames = new HashSet<>();
        if (StringUtils.isNotEmpty(filterFlagValue) && !filterFlagValue.equalsIgnoreCase("NA") && !filterFlagValue.equalsIgnoreCase("[]")) {
            List<String> filterMediaNames = FastJsonUtils.toList(filterFlagValue, String.class);
            excludeMediaNames.addAll(filterMediaNames);
        }
        return excludeMediaNames;
    }

    /**
     * @param mapMessage    事件，取级别
     * @param effectType    通知类型
     * @param candidates    通知人
     * @param mediaInfoList
     * @return
     */
    private List<EventHandleUser> getIntersectionCandidate(Map<String, Object> mapMessage, String effectType, List<EventHandleUser> candidates, List<MediaInfo> mediaInfoList) {
        List<EventHandleUser> users = new ArrayList<>();
        if (CollectionUtils.isEmpty(mediaInfoList)) {
            return users;
        }
        String effectTypeId = mediaInfoList.stream().filter(media -> media.getName().equalsIgnoreCase(effectType)).map(media -> media.getId()).collect(Collectors.joining());
        String eventSeverity = mapMessage.get("Severity") + "";
        for (EventHandleUser candidate : candidates) {
            String severity = candidate.getSeverity();
            String notification = candidate.getNotification();
            if (StringUtils.isBlank(severity)) {
                continue;
            }
            if (StringUtils.isBlank(notification)) {
                continue;
            }
            List<String> severityList = JSON.parseArray(severity, String.class);
            if (!severityList.contains(eventSeverity)) {
                continue;
            }
            List<String> notificationList = JSON.parseArray(notification, String.class);
            if (!notificationList.contains(effectTypeId)) {
                continue;
            }
            users.add(candidate);
        }
        return users;
    }

    /**
     * @param mapMessage    事件，取级别
     * @param mediaInfoList 策略关联的通知类型
     * @param candidate     通知人
     * @return
     */
    private List<String> getIntersectionMediaType(Map<String, Object> mapMessage, List<MediaInfo> mediaInfoList, EventHandleUser candidate) {
        List<String> mediaTypes = new ArrayList<>();
        String severity = candidate.getSeverity();
        String notification = candidate.getNotification();
        String eventSeverity = mapMessage.get("Severity") + "";
        if (StringUtils.isBlank(severity)) {
            return mediaTypes;
        }
        if (StringUtils.isBlank(notification)) {
            return mediaTypes;
        }
        if (CollectionUtils.isEmpty(mediaInfoList)) {
            return mediaTypes;
        }
        List<String> severityList = JSON.parseArray(severity, String.class);
        if (!severityList.contains(eventSeverity)) {
            return mediaTypes;
        }
        List<String> notificationList = JSON.parseArray(notification, String.class);
        return mediaInfoList.stream()
                .filter(media -> notificationList.contains(media.getId()))
                .map(media -> media.getName()).collect(Collectors.toList());
    }


    private void saveNotificationLog(Map<String, Object> mapMessage, List<EventHandleUser> candidates, Map<String, Object> event) {
        DataLoadParams dataLoadParams = new DataLoadParams();
        Map<Object, Object> filter = new HashMap<>();
        filter.put("event_id", event.get("EventID"));
        filter.put("acknoledger", mapMessage.get("acknoledger"));
        filter.put("notification_type", mapMessage.get("notificationType") == null ? "" : mapMessage.get("notificationType").toString());
        filter.put("transmit_date", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        filter.put("remark", mapMessage.get("remark") == null ? "" : mapMessage.get("remark").toString());
        filter.put("description", mapMessage.get("description"));
        filter.put("flow_status", event.get("Acknowledged"));
        filter.put("project_id", projectInfo.getId());
        List<CandidateVo> candidateList = new ArrayList<>();
        if (candidates != null) {
            for (EventHandleUser candidate : candidates) {
                CandidateVo candidateVo = new CandidateVo();
                candidateVo.setNotification_types((List<String>) mapMessage.get("notifyType"));
                candidateVo.setCandidate(candidate.getDomainName());
                candidateList.add(candidateVo);
            }
        }
        filter.put("candidate", JSON.toJSONString(candidateList));
        dataLoadParams.setFilter(JSON.toJSONString(filter));
        dataLoadParams.setDcName("addNotificationLog");
        dataLoadParams.setProjectId(10L);
        dataLoadParams.setStart(1);
        dataLoadParams.setLimit(-10);
        ResultPattern res = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
        if (!res.isSuccess()) {
            logger.error(res.getErrorMsg());
        }
        if ("slave".equalsIgnoreCase(componentStatus)) {
            dataLoadParams.setDcName("addNotificationLogMaster");
            ResultPattern resMaster = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
            if (!resMaster.isSuccess()) {
                logger.error(resMaster.getErrorMsg());
            }
        }
    }

    /**
     * 按分组组织人员列表/按节点人员列表
     *
     * @param projectId
     * @param event          事件
     * @param effectTypeList 通知方式的名称：来自当前通知策略配置
     * @param mediaInfoList
     * @return
     */
    public List<EventHandleUser> getCandidatesFromDb(long projectId, Map<String, Object> event, List<String> effectTypeList, List<MediaInfo> mediaInfoList) {
        String eventSeverity = event.get("Severity") + "";
        Map<String, Object> filter = new HashMap<>();
        filter.put("categoryName", event.get("EventCategory"));
        filter.put("deviceKey", event.get("Node"));
        filter.put("status", '0');
        filter.put("isEnable", 'Y');
        List<EventHandleUser> candidatesFromNode = candidateConfig.getCandidatesByNode(projectId, filter);
        List<EventHandleUser> candidatesFromGroup = candidateConfig.getCandidatesByGroup(projectId, filter);
        List<EventHandleUser> candidatesFromLabel = candidateConfig.getCandidatesByLabel(projectId, filter);
        for (EventHandleUser user : candidatesFromNode) {
            SeverityNotification severityNotification = new SeverityNotification();
            severityNotification.setNotificationName(effectTypeList);
            severityNotification.setSeverity(eventSeverity);
            user.setGroupSeverityNotification(severityNotification);
        }
        List<EventHandleUser> candidatesTemp = Stream.of(candidatesFromGroup, candidatesFromLabel)
                .flatMap(Collection::stream)
                .map(user -> {
                    List<SeverityNotification> severityNotifications = JSON.parseArray(user.getSeverityNotification(), SeverityNotification.class);
                    List<String> notificationNameList = new ArrayList();
                    for (SeverityNotification severityNotification : severityNotifications) {
                        String notificationSeverity = severityNotification.getSeverity();
                        if (eventSeverity.equalsIgnoreCase(notificationSeverity)) {
                            List<String> notificationName = severityNotification.getNotificationName();
                            Iterator<String> iterator = notificationName.iterator();
                            while (iterator.hasNext()) {
                                String next = iterator.next();
                                if (effectTypeList.contains(next)) {
                                    notificationNameList.add(next);
                                }
                            }
                            break;
                        }
                    }
                    SeverityNotification groupSeverityNotification = new SeverityNotification();
                    groupSeverityNotification.setNotificationName(notificationNameList);
                    groupSeverityNotification.setSeverity(eventSeverity);
                    user.setGroupSeverityNotification(groupSeverityNotification);
                    return user;
                })
                .collect(Collectors.toList());
        List<EventHandleUser> candidates = Stream.of(candidatesFromNode, candidatesTemp)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<EventHandleUser> candidateMerge = new ArrayList<>();
        for (EventHandleUser candidate : candidates) {
            if (CollectionUtils.isEmpty(candidateMerge)) {
                candidateMerge.add(candidate);
                continue;
            }
            for (EventHandleUser user : candidateMerge) {
                if (user.equals(candidate)) {
                    List<String> notificationName1 = user.getGroupSeverityNotification().getNotificationName();
                    List<String> notificationName2 = candidate.getGroupSeverityNotification().getNotificationName();
                    Set<String> notificationName3 = new HashSet<>(notificationName1);
                    notificationName3.addAll(notificationName2);
                    user.getGroupSeverityNotification().setNotificationName(new ArrayList<>(notificationName3));
                    break;
                }
            }
            if (!candidateMerge.contains(candidate)) {
                candidateMerge.add(candidate);
            }
        }
        Map<String, String> mediaNameToId = new HashMap<>();
        for (MediaInfo mediaInfo : mediaInfoList) {
            mediaNameToId.put(mediaInfo.getName(), mediaInfo.getId());
        }
        List<EventHandleUser> candidateFinal = candidateMerge.stream().filter(user -> {
            String userSeverity = user.getSeverity();
            String userNotification = user.getNotification();
            SeverityNotification groupSeverityNotification = user.getGroupSeverityNotification();
            if (StringUtils.isBlank(userSeverity)) {
                return false;
            }
            if (StringUtils.isBlank(userNotification)) {
                return false;
            }
            List<String> severityList = JSON.parseArray(userSeverity, String.class);
            if (!severityList.contains(groupSeverityNotification.getSeverity())) {
                return false;
            }
            return true;
        }).map(user -> {
            List<String> notificationList = JSON.parseArray(user.getNotification(), String.class);
            Iterator<String> iterator = user.getGroupSeverityNotification().getNotificationName().iterator();
            while (iterator.hasNext()) {
                if (!notificationList.contains(mediaNameToId.get(iterator.next()))) {
                    iterator.remove();
                }
            }
            return user;
        }).collect(Collectors.toList());
        return candidateFinal;
    }

    /**
     * @param person
     * @return
     */
    private List<EventHandleUser> getCandidatesByConsole(String person, Long projectId) {
        List<EventHandleUser> candidates = new ArrayList<>();
        if (StringUtils.isNotBlank(person)) {
            String[] split = person.split("#");
            List<String> domainNames = Arrays.asList(split);
            DataLoadParams dataLoadParams = new DataLoadParams();
            Map<Object, Object> filter = new HashMap<>();
            filter.put("domainNames", domainNames);
            dataLoadParams.setFilter(JSON.toJSONString(filter));
            dataLoadParams.setDcName("getUserFromDomainNames");
            dataLoadParams.setProjectId(projectId);
            ResultPattern res = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
            if (res.isSuccess() && res.getTotalCount() > 0) {
                candidates = JSON.parseArray(JSON.toJSONString(res.getDatas()), EventHandleUser.class);
            }
        }
        return candidates;
    }

    /**
     * @param person         通知人唯一名称1#通知人唯一名称2#通知人唯一名称3
     * @param projectId      项目id
     * @param effectTypeList 通知方式名称
     * @param mapMessage     告警
     * @return
     */
    public List<EventHandleUser> getCandidatesByConsole(String person, Long projectId, List<String> effectTypeList, Map<String, Object> mapMessage) {
        List<EventHandleUser> candidates = new ArrayList<>();
        if (StringUtils.isNotBlank(person)) {
            String[] split = person.split("#");
            List<String> domainNames = Arrays.asList(split);
            DataLoadParams dataLoadParams = new DataLoadParams();
            Map<Object, Object> filter = new HashMap<>();
            filter.put("domainNames", domainNames);
            dataLoadParams.setFilter(JSON.toJSONString(filter));
            dataLoadParams.setDcName("getUserFromDomainNames");
            dataLoadParams.setProjectId(projectId);
            ResultPattern res = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
            if (res.isSuccess() && res.getTotalCount() > 0) {
                candidates = JSON.parseArray(JSON.toJSONString(res.getDatas()), EventHandleUser.class);
                candidates = candidates.stream().map(user -> {
                    SeverityNotification groupSeverityNotification = new SeverityNotification();
                    groupSeverityNotification.setNotificationName(effectTypeList);
                    groupSeverityNotification.setSeverity(mapMessage.get("Severity") + "");
                    user.setGroupSeverityNotification(groupSeverityNotification);
                    return user;
                }).collect(Collectors.toList());
            }
        }
        return candidates;
    }

    /**
     * @param projectId        项目ID
     * @param mapMessage       事件
     * @param candidateVo      被通知人
     * @param notificationType 通知类型
     * @param flowStatus
     */
    public long saveNotificationLog(long projectId, Map<String, Object> mapMessage, Map<EventHandleUser, List> candidateVo, String notificationType, String flowStatus) {
        List<CandidateVo> CandidateVoList = new ArrayList<>();
        Set<Map.Entry<EventHandleUser, List>> entries = candidateVo.entrySet();
        for (Map.Entry<EventHandleUser, List> entry : entries) {
            String key = entry.getKey().getDomainName();
            List<String> value = entry.getValue();
            if (value.contains(notificationType)) {
                CandidateVo candidateVo1 = new CandidateVo();
                candidateVo1.setCandidate(key);
                candidateVo1.setNotification_types(Arrays.asList(notificationType));
                CandidateVoList.add(candidateVo1);
            }
        }
        DataLoadParams dataLoadParams = new DataLoadParams();
        HashMap<Object, Object> filter = new HashMap<>();
        filter.put("event_id", mapMessage.get("EventID"));
        filter.put("acknoledger", "SYSTEM");
        long nowMilli = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        filter.put("transmit_date", nowMilli);
        filter.put("candidate", JSON.toJSONString(CandidateVoList));
        filter.put("project_id", projectId);
        filter.put("flow_status", flowStatus);
        dataLoadParams.setFilter(JSON.toJSONString(filter));
        dataLoadParams.setDcName("addNotificationLog");
        dataLoadParams.setProjectId(projectId);
        dataLoadParams.setStart(1);
        dataLoadParams.setLimit(-10);
        ResultPattern res = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
        if ("slave".equalsIgnoreCase(componentStatus)) {
            dataLoadParams.setDcName("addNotificationLogMaster");
            ResultPattern resMaster = DataServiceUtils.dataLoad(dataSource, dataLoadParams);
        }
        return nowMilli;
    }

    /**
     * @param mediaName     发送介质名称
     * @param candidateList 接收人
     * @param message       告警消息
     */
    public void notify(String mediaName, List<EventHandleUser> candidateList, Map<String, Object> message) {
        if (StringUtils.isBlank(mediaName)) {
            return;
        }
        if (MapUtils.isEmpty(message)) {
            return;
        }
        List<EventHandleUser> candidates = candidateList.stream().distinct().collect(Collectors.toList());
        Map<String, Object> newMessage = new HashMap<>();
        newMessage.putAll(message);
        MediaInfo mediaInfo = this.notifySettingsConfig.getSendServiceInfos(projectInfo.getId(), mediaName);
        if (mediaInfo == null) {
            return;
        }
        MediaType mediaType = mediaInfo.getMediaType();
        //工单系统
        if (mediaType == MediaType.FLOW) {
            MediaInfo flowInfo = this.notifySettingsConfig.getSendServiceInfo(projectInfo.getId(), mediaType.name().toLowerCase());
            String msStr = flowInfo.getMediaSettings();
            Map<String, String> msMap = JSON.parseObject(msStr, HashMap.class);
            newMessage.put("userName", msMap.get("userName"));
            newMessage.put("password", msMap.get("password"));
            String mtStr = flowInfo.getMediaTemplet();
            try {
                String msgStr = this.buildBody(mtStr, newMessage);
                List<NameValuePair> nvps = new ArrayList();
                Set<Map.Entry> entrySet = JSON.parseObject(msgStr, Map.class).entrySet();
                for (Map.Entry entry : entrySet) {
                    nvps.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
                }
                String result = HttpUtils.postForm(msMap.get("url"), nvps, null);
                //更新工单URL至
                if (StringUtils.isNotBlank(result)) {
                    ItsmResult itsmResult = JSON.parseObject(result, ItsmResult.class);
                    if (itsmResult.getResult() == 0) {//0成功  1失败
                        Map<String, Object> filter = new HashMap<String, Object>();
                        filter.put("data", itsmResult.getData());
                        filter.put("eventId", newMessage.get("EventID"));
                        DataLoadParams queryParams = new DataLoadParams();
                        queryParams.setProjectId(projectInfo.getId());
                        queryParams.setDcName("updateItsmUrl");
                        queryParams.setFilter(FastJsonUtils.convertObjectToJSON(filter));
                        DataServiceUtils.dataLoad(dataSource, queryParams);
                    }
                }
                logger.info("发起工单:{};返回状态:{}", msgStr, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mediaType == MediaType.EMAIL) {
            //查询SMTP邮件服务
            //解析邮箱服务信息
            JSONObject mediaSettings = JSONObject.parseObject(mediaInfo.getMediaSettings());
            //设置发送SMTP服务
            boolean isSSL = StringUtils.isEmpty(mediaSettings.getString("is_ssl")) ? false : mediaSettings.getString("is_ssl").equalsIgnoreCase("Y") ? true : false;
            String mailServer = mediaSettings.getString("mail_server");
            int mailServerPort = new Integer(mediaSettings.getString("mail_server_port"));
            String fromEmail = mediaSettings.getString("from_mail");
            String fromEmailPassword = mediaSettings.getString("from_mail_password");
            String mailSubject = mediaSettings.getString("mailSubject");
            List<String> toMails = new ArrayList<>();
            for (EventHandleUser candidate : candidates) {
                String userEmail = candidate.getUserEmail();
                if (StringUtils.isNotBlank(userEmail)) {
                    toMails.add(userEmail);
                    continue;
                }
                logger.warn("{} email is not set", candidate.getDomainName());
            }
            //发送
            try {
                String s = this.buildBody(mailSubject, newMessage);
                String s1 = this.buildBody(mediaInfo.getMediaTemplet(), newMessage);
                SendmailUtil.sendEmail(toMails, null, s, s1, mailServer, mailServerPort, isSSL, fromEmail, "事件通知", fromEmailPassword, timeout);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
        } else if (mediaType == MediaType.SOCKET) {
            SocketParam socketParam = JSON.parseObject(mediaInfo.getMediaSettings(), SocketParam.class);
            String socketAddress = socketParam.getIp();
            int socketPort = Integer.parseInt(socketParam.getPort());
            for (EventHandleUser user : candidates) {
                try {
                    newMessage.putAll(JSON.parseObject(JSON.toJSONString(user), Map.class));
                    String templateMessage = this.buildBody(mediaInfo.getMediaTemplet(), newMessage);
                    if (ProtocolType.TCP.toString().equalsIgnoreCase(socketParam.getType())) {
                        NettyTcpClient client = new NettyTcpClient(templateMessage);
                        client.connect(socketAddress, socketPort);
                    } else if (ProtocolType.UDP.toString().equalsIgnoreCase(socketParam.getType())) {
                        NettyUdpClient client = new NettyUdpClient(templateMessage);
                        client.connect(socketAddress, socketPort);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    continue;
                }
            }
        } else {//事件总线
            try {
                Map<String, String> mediaSettings = JSON.parseObject(mediaInfo.getMediaSettings(), Map.class);
                String url = mediaSettings.get("url");
                String contentType = mediaSettings.get("content_type");
                for (EventHandleUser user : candidates) {
                    newMessage.putAll(JSON.parseObject(JSON.toJSONString(user), Map.class));
                    String formatMsg = this.buildBody(mediaInfo.getMediaTemplet(), newMessage);
                    String request = "";
                    if ("JSON".equals(contentType)) {
                        request = openFeignService.getRequest(new URI(url), JSON.parseObject(formatMsg, Map.class));
                    }
                    logger.debug(request);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    private String buildBody(String templet, Map<String, Object> event) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        Map<String, Object> params = new HashMap<>(event);
        MethodToBytes methodToBytes = new MethodToBytes();
        params.put("methodToBytes", methodToBytes);
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("emailContents", templet);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setNumberFormat("#");
        cfg.setTemplateLoader(stringLoader);
        Writer out = new StringWriter(4096);
        Template template = cfg.getTemplate("emailContents");
        template.process(params, out);
        return out.toString();
    }

    //    public static void main(String[] args) throws IOException, TemplateException {
//        HashMap<String, Object> msg = new HashMap<>();
//        msg.put("summary", "中");
//        String template="${methodToBytes(summary)}";
//        HttpPusher httpPusher = new HttpPusher();
//        httpPusher.buildBody(template, msg);
//    }
}
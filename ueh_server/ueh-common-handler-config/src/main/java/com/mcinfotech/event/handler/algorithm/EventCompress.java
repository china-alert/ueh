package com.mcinfotech.event.handler.algorithm;

import java.math.BigDecimal;
import java.util.*;

import javax.sql.DataSource;

import com.mcinfotech.event.utils.DateTimeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.handler.config.EventCompressConfig;
import com.mcinfotech.event.handler.domain.CompressParam;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleEffect;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import cn.mcinfotech.data.service.util.DataSourceUtils;

/**
 * 事件压缩
 * 针对单台设备中一个或几个字段重复出现时，对最后发生事件、发生次数进行更新(目前是可以自由定义)
 * 条件字段：[{"conditionColumn":"Node","conditionDataType":"string","operator":"","conditionValue":""},{"conditionColumn":"NodeAlias","conditionDataType":"string","operator":"","operator":"","conditionValue":""},{"conditionColumn":"AlertGroup","conditionDataType":"string","operator":"","conditionValue":""},{"conditionColumn":"AlertKey","conditionDataType":"string","operator":"","conditionValue":""},{"conditionColumn":"Summary","conditionDataType":"string","operator":"","conditionValue":""}]
 * 生效字段：[{"effectColumn":"Tally","effectDataType":"int","effectType":"counter","effectValue":""},{"effectColumn":"LastOccurrence","effectDataType":"longtimestamp","effectType":"newest","effectValue":""}]
 *

 */
public class EventCompress {
    private static Logger logger = LogManager.getLogger(EventCompress.class);

    /**
     * 按照指定的字段进行压缩，必须是内容一致的才能做压缩。
     *
     * @param rawMessages 原始消息
     * @param rule        压缩规则配置，参考EventHandlerRule
     * @return 压缩过的事件
     */
    public static Collection<Map<String, Object>> excute(List<Map<String, Object>> rawMessages, EventHandlerRule rule, DataSource dataSource, long projectID) {
        if (CollectionUtils.isEmpty(rawMessages)) return null;
        if (rule == null) return rawMessages;
		/*boolean expired=DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
		if(expired){
			return rawMessages;
		}*/
        //装配条件字段和结果字段
        String expressionStr = rule.getExpression();
        List<EventHandlerRuleExpression> expressions = FastJsonUtils.toList(expressionStr, EventHandlerRuleExpression.class);
        List<String> conditionColumns = new ArrayList<String>();
        for (EventHandlerRuleExpression expression : expressions) {
            conditionColumns.add(expression.getConditionColumn());
        }
        if (CollectionUtils.isEmpty(conditionColumns)) return rawMessages;
        List<EventHandlerRuleEffect> effects = FastJsonUtils.toList(rule.getEffect(), EventHandlerRuleEffect.class);

        Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
        //1不查库压缩
        for (Map<String, Object> message : rawMessages) {
            /**
             * 按照指定的条件字段进行取值然后构建事件消息的KEY
             */
            StringBuffer identifier = new StringBuffer();
            for (String key : conditionColumns) {
                identifier.append(message.get(key));
            }
            String identifierKey = identifier.toString();
            if (logger.isDebugEnabled()) {
                logger.debug("current identifier : " + identifierKey);
            }
            /**
             * 每个条消息的Identifier由压缩字段组成，不再单独指定。
             */
            message.put("Identifier", identifierKey);
            if (result.containsKey(identifierKey)) {
                Map<String, Object> newMessage = result.get(identifierKey);
                for (EventHandlerRuleEffect effect : effects) {
                    if (effect.getEffectType().equalsIgnoreCase("counter")) {
                        //更新次数
                        String last = newMessage.get(effect.getEffectColumn()) == null ? "0" : newMessage.get(effect.getEffectColumn()).toString();
                        if (newMessage.containsKey(effect.getEffectColumn())) {
                            newMessage.remove(effect.getEffectColumn());
                        }
                        newMessage.put(effect.getEffectColumn(), new BigDecimal(last).add(new BigDecimal(message.get(effect.getEffectColumn()).toString())).intValue());
                        continue;
                    }
                    if (effect.getEffectType().equalsIgnoreCase("newest")) {
                        //更新最后发生日期
                        if (newMessage.containsKey(effect.getEffectColumn())) {
                            newMessage.remove(effect.getEffectColumn());
                        }
                        newMessage.put(effect.getEffectColumn(), message.get(effect.getEffectColumn()));
                        continue;
                    }
                }
                result.put(identifierKey, newMessage);
            } else {
                result.put(identifierKey, message);
            }
        }
        //2查库压缩（长压缩）
        String params = rule.getParams();
        List<Map<String, Object>> messages = new ArrayList<>();
        if (StringUtils.isNotBlank(params)) {
            CompressParam compressParam = JSON.parseObject(params, CompressParam.class);
            String isLongCycleCompress = compressParam.getIsLongCycleCompress();
            String cycleTime = compressParam.getCycleTime();
            //如果开启了长压缩
            if ("Y".equals(isLongCycleCompress)) {
                for (Map<String, Object> message : result.values()) {
                    //1告警事件，2恢复事件，EventType=S时有效
                    String eventSeverityType = String.valueOf(message.get("EventSeverityType"));
                    //P不可恢复字段，S可恢复事件
                    String eventType = String.valueOf(message.get("EventType"));
                    //可恢复的恢复事件
                    if ("S".equals(eventType) && "2".equals(eventSeverityType)) {
                        messages.add(message);
                        //可恢复的告警事件和不可恢复事件
                    } else {
                        //查询库中未恢复事件
                        List<Map<String, Object>> alarmEvents = EventCompressConfig.getAlarmEventByCondition(message, conditionColumns, cycleTime, dataSource, projectID);
                        //根据结果字段更新：最后发生时间、发生次数、描述的组合
                        if (CollectionUtils.isEmpty(alarmEvents)) {
                            messages.add(message);
                        } else {
                            for (Map<String, Object> alarmEvent : alarmEvents) {

                                Map<Object, Object> filter = new HashMap<>();
                                filter.put("EventID", alarmEvent.get("EventID"));
                                filter.put("projectID", projectID);
                                for (EventHandlerRuleEffect effect : effects) {
                                    String effectType = effect.getEffectType();
                                    String effectColumn = effect.getEffectColumn();
                                    //累计
                                    if (effectType.equalsIgnoreCase("counter")) {
                                        String last = alarmEvent.get(effectColumn) == null ? "0" : alarmEvent.get(effectColumn).toString();
                                        filter.put(effectColumn, new BigDecimal(last).add(new BigDecimal(message.get(effectColumn).toString())).intValue());
                                        continue;
                                    }
                                    //最新
                                    if (effectType.equalsIgnoreCase("newest")) {
                                        filter.put(effectColumn, message.get(effectColumn));
                                        continue;
                                    }
                                }
                                EventCompressConfig.updateAlarmEventByCondition(filter, dataSource, projectID);
                            }
                        }
                    }
                }
            } else {
                messages.addAll(result.values());
            }
        } else {
            messages.addAll(result.values());
        }
        return messages;
    }
    /**
     * 按照内置压缩规则进行压缩
     *
     * @param rawMessages  原始事件集合
     * @param rules        压缩规则
     * @param dataSource   数据源
     * @param projectID    项目ID
     * @param recoveryRule 恢复规则
     * @return 处理后事件集合
     */
    public static Collection<Map<String, Object>> defaultExcute(Collection<Map<String, Object>> rawMessages, List<EventHandlerRule> rules, DataSource dataSource, long projectID, List<EventHandlerRule> recoveryRule) {
        if (CollectionUtils.isEmpty(rawMessages)) return null;
        /**
         * 默认条件字段
         */
        List<String> conditionColumns = Arrays.asList("Node", "AlertKey", "Summary", "EventSeverityType");
        /**
         * 默认结果字段
         */
        List<EventHandlerRuleEffect> effects = new ArrayList<>();
        EventHandlerRuleEffect effect1 = new EventHandlerRuleEffect();
        EventHandlerRuleEffect effect2 = new EventHandlerRuleEffect();
        effect1.setEffectColumn("Tally");
        effect1.setEffectType("counter");
        effect2.setEffectColumn("LastOccurrence");
        effect2.setEffectType("newest");
        effects.add(effect1);
        effects.add(effect2);

        Map<String, Map<String, Object>> result = new HashMap<>();
        //1不查库压缩  内置压缩策略压缩
        for (Map<String, Object> message : rawMessages) {
            //按照指定的条件字段进行取值然后构建事件消息的KEY
            StringBuffer identifier = new StringBuffer();
            for (String key : conditionColumns) {
                identifier.append(message.get(key));
            }
            String identifierKey = identifier.toString();
            if (logger.isDebugEnabled()) logger.debug("current identifier : " + identifierKey);
            //每个条消息的Identifier由压缩字段组成，不再单独指定。
            message.put("Identifier", identifierKey);
            //结果中不包含唯一key,次数加1,最后发生时间更新为最新
            if (result.containsKey(identifierKey)) {
                Map<String, Object> newMessage = result.get(identifierKey);
                for (EventHandlerRuleEffect effect : effects) {
                    if (effect.getEffectType().equalsIgnoreCase("counter")) {
                        //更新次数
                        String last = newMessage.get(effect.getEffectColumn()) == null ? "0" : newMessage.get(effect.getEffectColumn()).toString();
                        if (newMessage.containsKey(effect.getEffectColumn())) {
                            newMessage.remove(effect.getEffectColumn());
                        }
                        newMessage.put(effect.getEffectColumn(), new BigDecimal(last).add(new BigDecimal(message.get(effect.getEffectColumn()).toString())).intValue());
                        continue;
                    }
                    if (effect.getEffectType().equalsIgnoreCase("newest")) {
                        //更新最后发生日期
                        if (newMessage.containsKey(effect.getEffectColumn())) {
                            newMessage.remove(effect.getEffectColumn());
                        }
                        newMessage.put(effect.getEffectColumn(), message.get(effect.getEffectColumn()));
                        continue;
                    }
                }
                result.put(identifierKey, newMessage);
            } else {
                result.put(identifierKey, message);
            }
        }
        rawMessages.clear();
        rawMessages.addAll(result.values());
        return excute(rawMessages, rules, dataSource, projectID, recoveryRule);
    }

    /**
     * 按照指定的字段进行压缩，必须是内容一致的才能做压缩。
     *
     * @param rawMessages 原始消息
     * @param rules       压缩规则配置，参考EventHandlerRule
     * @return 压缩过的事件
     */
    public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages, List<EventHandlerRule> rules, DataSource dataSource, long projectID, List<EventHandlerRule> recoveryRules) {
        if (CollectionUtils.isEmpty(rawMessages)) return null;
        if (rules == null) return rawMessages;
        for (EventHandlerRule rule : rules) {
            boolean notExpired= DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
            if (!notExpired) {
                continue;
            }
            //装配条件字段和结果字段
            String expressionStr = rule.getExpression();
            List<EventHandlerRuleExpression> expressions = FastJsonUtils.toList(expressionStr, EventHandlerRuleExpression.class);
            List<String> conditionColumns = new ArrayList<>();
            for (EventHandlerRuleExpression expression : expressions) {
                conditionColumns.add(expression.getConditionColumn());
            }
            if (CollectionUtils.isEmpty(conditionColumns)) continue;
            List<EventHandlerRuleEffect> effects = FastJsonUtils.toList(rule.getEffect(), EventHandlerRuleEffect.class);

            Map<String, Map<String, Object>> result = new HashMap<>();
            //1根据压缩策略压缩内存中的事件
            for (Map<String, Object> message : rawMessages) {
                if (message.get("isCompress") != null) {
                    result.put(String.valueOf(message.get("Identifier")), message);
                    continue;//被压缩过
                }
                /**
                 * 按照指定的条件字段进行取值然后构建事件消息的KEY
                 */
                StringBuffer identifier = new StringBuffer();
                for (String key : conditionColumns) {
                    identifier.append(message.get(key));
                }
                if ("S".equals(message.get("EventType"))) {//如果是可恢复事件，强制区分告警事件和恢复事件进行压缩
                    identifier.append(message.get("EventSeverityType"));
                }
                String identifierKey = identifier.toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("current identifier : " + identifierKey);
                }
                /**
                 * 每个条消息的Identifier由压缩字段组成，不再单独指定。
                 */
                message.put("Identifier", identifierKey);
                if (result.containsKey(identifierKey)) {
                    Map<String, Object> newMessage = result.get(identifierKey);
                    for (EventHandlerRuleEffect effect : effects) {
                        if (effect.getEffectType().equalsIgnoreCase("counter")) {
                            //更新次数
                            String last = newMessage.get(effect.getEffectColumn()) == null ? "0" : newMessage.get(effect.getEffectColumn()).toString();
                            if (newMessage.containsKey(effect.getEffectColumn())) {
                                newMessage.remove(effect.getEffectColumn());
                            }
                            newMessage.put(effect.getEffectColumn(), new BigDecimal(last).add(new BigDecimal(message.get(effect.getEffectColumn()).toString())).intValue());
                            continue;
                        }
                        if (effect.getEffectType().equalsIgnoreCase("newest")) {
                            //更新最后发生日期
                            if (newMessage.containsKey(effect.getEffectColumn())) {
                                newMessage.remove(effect.getEffectColumn());
                            }
                            newMessage.put(effect.getEffectColumn(), message.get(effect.getEffectColumn()));
                            continue;
                        }
                    }
                    //标识是否压缩成功过 true  false
                    newMessage.put("isCompress", true);
                    newMessage.put("RefCompressRules", rule.getName());
                    result.put(identifierKey, newMessage);
                } else {
                    result.put(identifierKey, message);
                }
            }
            rawMessages.clear();
            rawMessages.addAll(result.values());
        }
        //2查库压缩（长压缩）
        for (EventHandlerRule rule : rules) {
            boolean notExpired= DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
            if (!notExpired) {
                continue;
            }
            //装配条件字段和结果字段
            String expressionStr = rule.getExpression();
            List<EventHandlerRuleExpression> expressions = FastJsonUtils.toList(expressionStr, EventHandlerRuleExpression.class);
            List<String> conditionColumns = new ArrayList<>();
            for (EventHandlerRuleExpression expression : expressions) {
                conditionColumns.add(expression.getConditionColumn());
            }
            if (CollectionUtils.isEmpty(conditionColumns)) continue;
            List<EventHandlerRuleEffect> effects = FastJsonUtils.toList(rule.getEffect(), EventHandlerRuleEffect.class);
            String params = rule.getParams();
            if (StringUtils.isNotBlank(params)) {
                CompressParam compressParam = JSON.parseObject(params, CompressParam.class);
                String isLongCycleCompress = compressParam.getIsLongCycleCompress();
                String cycleTime = compressParam.getCycleTime();
                //如果开启了长压缩
                if ("Y".equals(isLongCycleCompress)) {
                    Iterator<Map<String, Object>> iterator = rawMessages.iterator();
                    while (iterator.hasNext()) {
                        Map<String, Object> message = iterator.next();
                        //1告警事件，2恢复事件，EventType=S时有效
                        String eventSeverityType = String.valueOf(message.get("EventSeverityType"));
                        //P不可恢复字段，S可恢复事件
                        String eventType = String.valueOf(message.get("EventType"));
                        //可恢复的恢复事件
                        int total = 0;
                        if (("S".equals(eventType) && "2".equals(eventSeverityType))) {
                            if (!CollectionUtils.isEmpty(recoveryRules)) {
                                for (EventHandlerRule recoveryRule : recoveryRules) {
                                    boolean isNotExpired= DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
                                    if (!isNotExpired) {
                                        continue;
                                    }
                                    //根据恢复策略以及当前告警查询是否存在未恢复的告警
                                    ResultPattern noRecoveryResult = getNoRecoveryEvent(dataSource, projectID, recoveryRule, message);
                                    if (noRecoveryResult.isSuccess()) {
                                        total = Integer.parseInt(noRecoveryResult.getStrData());
                                    }
                                    break;
                                }
                            }

                        }
                        if (total == 0) {//不存在未恢复的告警
                            //查询库中符合条件的事件
                            List<Map<String, Object>> alarmEvents = EventCompressConfig.getAlarmEventByCondition(message, conditionColumns, cycleTime, dataSource, projectID);
                            //根据结果字段更新：最后发生时间、发生次数、描述的组合
                            if (!CollectionUtils.isEmpty(alarmEvents)) {
                                //boolean remove = rawMessages.remove(message);
                                iterator.remove();
                                for (Map<String, Object> alarmEvent : alarmEvents) {
                                    Map<Object, Object> filter = new HashMap<>();
                                    filter.put("EventID", alarmEvent.get("EventID"));
                                    filter.put("projectID", projectID);
                                    for (EventHandlerRuleEffect effect : effects) {
                                        String effectType = effect.getEffectType();
                                        String effectColumn = effect.getEffectColumn();
                                        //累计
                                        if (effectType.equalsIgnoreCase("counter")) {
                                            String last = alarmEvent.get(effectColumn) == null ? "0" : alarmEvent.get(effectColumn).toString();
                                            filter.put(effectColumn, new BigDecimal(last).add(new BigDecimal(message.get(effectColumn).toString())).intValue());
                                            continue;
                                        }
                                        //最新
                                        if (effectType.equalsIgnoreCase("newest")) {
                                            filter.put(effectColumn, message.get(effectColumn));
                                            continue;
                                        }
                                    }
                                    //数据库中告警命中的压缩策略与内存中告警命中的压缩策略进行拼接
                                    StringJoiner ruleJoiner = new StringJoiner("#");
                                    Object ruleFromDb = alarmEvent.get("RefCompressRules");
                                    Object ruleFromMemory = message.get("RefCompressRules");
                                    if (ObjectUtils.isNotEmpty(ruleFromDb)) {
                                        ruleJoiner.add(ruleFromDb+"");
                                    }
                                    if (ObjectUtils.isNotEmpty(ruleFromMemory)) {
                                        ruleJoiner.add(ruleFromMemory+"");
                                    }
                                    ruleJoiner.add(rule.getName());
                                    filter.put("RefCompressRules",ruleJoiner.toString());
                                    EventCompressConfig.updateAlarmEventByCondition(filter, dataSource, projectID);
                                }
                            }
                        }
                    }
                }
            }
        }
        return rawMessages;
    }

    private static ResultPattern getNoRecoveryEvent(DataSource dataSource, long projectID, EventHandlerRule recoveryRule, Map<String, Object> message) {
        String expressionRecoveryStr = recoveryRule.getExpression();
        List<EventHandlerRuleExpression> expressionsRecovery = FastJsonUtils.toList(expressionRecoveryStr, EventHandlerRuleExpression.class);
        Map<String, Object> filter = new HashMap<>();
        ArrayList<Map<String, Object>> reDtoMessages = new ArrayList<>();
        for (EventHandlerRuleExpression expression : expressionsRecovery) {
            String conditionColumn = expression.getConditionColumn();
            HashMap<String, Object> reDtoMessage = new HashMap<>();
            reDtoMessage.put("reColumn", conditionColumn);
            reDtoMessage.put("reValue", message.get(conditionColumn));
            reDtoMessages.add(reDtoMessage);
        }
        filter.put("conditions", reDtoMessages);
        DataLoadParams paramsRecovery = new DataLoadParams();
        paramsRecovery.setDcName("getNoRecoveryEvent");
        paramsRecovery.setProjectId(projectID);
        paramsRecovery.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        paramsRecovery.setStart(1);
        paramsRecovery.setLimit(-10);
        paramsRecovery.setEngine(SQLEngine.Freemarker);
        return DataServiceUtils.dataLoad(dataSource, paramsRecovery);
    }
}
package com.mcinfotech.event.handler.algorithm;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.utils.DateTimeUtils;
import com.mcinfotech.event.utils.FastJsonUtils;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 事件恢复
 */
public class EventRecovery {
    /**
     * @param rawMessages 原始消息
     * @param rules        恢复规则
     * @param dataSource  数据源
     * @param projectInfo 项目信息
     * @return 过滤过的事件
     */
    public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages, List<EventHandlerRule> rules, DataSource dataSource, ProjectInfo projectInfo) {
        //todo 恢复动作一个线程   后续处理一个线程
        if (CollectionUtils.isEmpty(rawMessages)) return null;
        if (rules == null||CollectionUtils.isEmpty(rules)) return rawMessages;

        //1.筛选恢复事件
        List<Map<String, Object>> reMessages = new ArrayList<>();
        Iterator<Map<String, Object>> iterator = rawMessages.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rawMessage = iterator.next();
            String eventType = String.valueOf(rawMessage.get("EventType"));
            String eventSeverityType = String.valueOf(rawMessage.get("EventSeverityType"));
            //可恢复事件:EventType(P不可恢复字段，S可恢复事件) EventSeverityType(1告警事件，2恢复事件，EventType=S时有效)
            if ("S".equalsIgnoreCase(eventType) && "2".equals(eventSeverityType)) {
                //状态标记为恢复
                rawMessage.put("recoveredStatus", 2);
                //状态标记为已完成
                rawMessage.put("Acknowledged", 1);
                rawMessage.put("Operator", "SYSTEM");
                rawMessage.put("OperateTimestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                rawMessage.put("OperateState", 1);
                reMessages.add(rawMessage);
                //iterator.remove();
            }
        }

        /*
         * 3.逐条更新事件信息
         * 可恢复事件：实时表删除已恢复事件、历史表更新恢复事件状态
         */
        if (reMessages.size() > 0) {
            for (EventHandlerRule rule : rules) {
                boolean isNotExpired= DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
                if (!isNotExpired) {
                    continue;
                }
                //2.恢复条件重新封装到list
                String expressionStr = rule.getExpression();
                List<EventHandlerRuleExpression> expressions = FastJsonUtils.toList(expressionStr, EventHandlerRuleExpression.class);
                recoveryInMemory(rawMessages, expressions, reMessages, rule.getName());
                recoveryInDB(dataSource, projectInfo, expressions, reMessages, rule.getName());
                break;
            }
        }

        return rawMessages;
    }

    private static void recoveryInDB(DataSource dataSource, ProjectInfo projectInfo, List<EventHandlerRuleExpression> expressions, List<Map<String, Object>> reMessages, String name) {
        for (Map<String, Object> reVoMessage : reMessages) {
            //3.1 前端传过来的动态条件 where
            Map<String, Object> filter = new HashMap<>();
            ArrayList<Map<String, Object>> reDtoMessages = new ArrayList<>();
            for (EventHandlerRuleExpression expression : expressions) {
                String conditionColumn = expression.getConditionColumn();
                HashMap<String, Object> reDtoMessage = new HashMap<>();
                reDtoMessage.put("reColumn", conditionColumn);
                reDtoMessage.put("reValue", reVoMessage.get(conditionColumn));
                reDtoMessages.add(reDtoMessage);
            }
            filter.put("conditions", reDtoMessages);
            //3.2 固定更新字段  update
            filter.put("lastOccurrence", String.valueOf(reVoMessage.get("LastOccurrence")));
            filter.put("deleteTime", String.valueOf(reVoMessage.get("LastOccurrence")));
            filter.put("recoveredEeventID", String.valueOf(reVoMessage.get("EventID")));
            filter.put("recoveredStatus", 2);
            filter.put("Acknowledged", 1);
            filter.put("Operator", "SYSTEM");
            filter.put("OperateTimestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            filter.put("OperateState", 1);
            filter.put("RefRecoveryRules", name);
            DataLoadParams params = new DataLoadParams();
            params.setDcName("operateEvent");
            params.setProjectId(projectInfo.getId());
            params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
            params.setStart(1);
            params.setLimit(-10);
            params.setEngine(SQLEngine.Freemarker);
            DataServiceUtils.dataLoad(dataSource, params);
        }
    }

    private static void recoveryInMemory(Collection<Map<String, Object>> rawMessages, List<EventHandlerRuleExpression> expressions, List<Map<String, Object>> reMessages, String name) {
        for (Map<String, Object> rawMessage : rawMessages) {
            String eventType = String.valueOf(rawMessage.get("EventType"));
            String eventSeverityType = String.valueOf(rawMessage.get("EventSeverityType"));
            String firstOccurrence = String.valueOf(rawMessage.get("FirstOccurrence"));
            if ("S".equalsIgnoreCase(eventType) && "1".equals(eventSeverityType)) {
                for (Map<String, Object> reMessage : reMessages) {
                    boolean flag = false;
                    for (EventHandlerRuleExpression expression : expressions) {
                        String conditionColumn = expression.getConditionColumn();
                        if (!String.valueOf(reMessage.get(conditionColumn)).equals(String.valueOf(rawMessage.get(conditionColumn)))) {
                            flag = false;
                            break;
                        } else {
                            flag = true;
                        }
                    }
                    if (flag) {
                        if (Long.parseLong(String.valueOf(reMessage.get("FirstOccurrence"))) > Long.parseLong(firstOccurrence)) {
                            rawMessage.put("LastOccurrence", Long.parseLong(String.valueOf(reMessage.get("LastOccurrence"))));
                            rawMessage.put("DeleteTime", Long.parseLong(String.valueOf(reMessage.get("LastOccurrence"))));
                            rawMessage.put("RecoveredEeventID", String.valueOf(reMessage.get("EventID")));
                            rawMessage.put("recoveredStatus", 2);
                            //状态标记为已完成
                            rawMessage.put("Acknowledged", 1);
                            rawMessage.put("Operator", "SYSTEM");
                            rawMessage.put("OperateTimestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                            rawMessage.put("OperateState", 1);
                            rawMessage.put("RefRecoveryRules", name);
                        }
                    }
                }
            }
        }
    }
    /**
     * @param rawMessages 原始消息
     * @param rules        恢复规则
     * @param dataSource  数据源
     * @param projectInfo 项目信息
     * @return 过滤过的事件
     */
    public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages, List<EventHandlerRule> rules, DataSource dataSource, ProjectInfo projectInfo,String componentStatus) {
        //todo 恢复动作一个线程   后续处理一个线程
        if (CollectionUtils.isEmpty(rawMessages)) return null;
        if (rules == null||CollectionUtils.isEmpty(rules)) return rawMessages;

        //1.筛选恢复事件
        List<Map<String, Object>> reMessages = new ArrayList<>();
        Iterator<Map<String, Object>> iterator = rawMessages.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> rawMessage = iterator.next();
            String eventType = String.valueOf(rawMessage.get("EventType"));
            String eventSeverityType = String.valueOf(rawMessage.get("EventSeverityType"));
            //可恢复事件:EventType(P不可恢复字段，S可恢复事件) EventSeverityType(1告警事件，2恢复事件，EventType=S时有效)
            if ("S".equalsIgnoreCase(eventType) && "2".equals(eventSeverityType)) {
                //状态标记为恢复
                rawMessage.put("recoveredStatus", 2);
                //状态标记为已完成
                rawMessage.put("Acknowledged", 1);
                rawMessage.put("Operator", "SYSTEM");
                rawMessage.put("OperateTimestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                rawMessage.put("OperateState", 1);
                reMessages.add(rawMessage);
                //iterator.remove();
            }
        }

        /*
         * 3.逐条更新事件信息
         * 可恢复事件：实时表删除已恢复事件、历史表更新恢复事件状态
         */
        if (reMessages.size() > 0) {
            for (EventHandlerRule rule : rules) {
                boolean isNotExpired= DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
                if (!isNotExpired) {
                    continue;
                }
                //2.恢复条件重新封装到list
                String expressionStr = rule.getExpression();
                List<EventHandlerRuleExpression> expressions = FastJsonUtils.toList(expressionStr, EventHandlerRuleExpression.class);
                recoveryInMemory(rawMessages, expressions, reMessages, rule.getName());
                recoveryInDB(dataSource, projectInfo, expressions, reMessages, rule.getName(),componentStatus);
                break;
            }
        }

        return rawMessages;
    }
    private static void recoveryInDB(DataSource dataSource, ProjectInfo projectInfo, List<EventHandlerRuleExpression> expressions, List<Map<String, Object>> reMessages, String name,String componentStatus) {
        for (Map<String, Object> reVoMessage : reMessages) {
            //3.1 前端传过来的动态条件 where
            Map<String, Object> filter = new HashMap<>();
            ArrayList<Map<String, Object>> reDtoMessages = new ArrayList<>();
            for (EventHandlerRuleExpression expression : expressions) {
                String conditionColumn = expression.getConditionColumn();
                HashMap<String, Object> reDtoMessage = new HashMap<>();
                reDtoMessage.put("reColumn", conditionColumn);
                reDtoMessage.put("reValue", reVoMessage.get(conditionColumn));
                reDtoMessages.add(reDtoMessage);
            }
            filter.put("conditions", reDtoMessages);
            //3.2 固定更新字段  update
            filter.put("lastOccurrence", String.valueOf(reVoMessage.get("LastOccurrence")));
            filter.put("deleteTime", String.valueOf(reVoMessage.get("LastOccurrence")));
            filter.put("recoveredEeventID", String.valueOf(reVoMessage.get("EventID")));
            filter.put("recoveredStatus", 2);
            filter.put("Acknowledged", 1);
            filter.put("Operator", "SYSTEM");
            filter.put("OperateTimestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            filter.put("OperateState", 1);
            filter.put("RefRecoveryRules", name);
            DataLoadParams params = new DataLoadParams();
            params.setDcName("operateEvent");
            params.setProjectId(projectInfo.getId());
            params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
            params.setStart(1);
            params.setLimit(-10);
            params.setEngine(SQLEngine.Freemarker);
            DataServiceUtils.dataLoad(dataSource, params);
            if ("slave".equalsIgnoreCase(componentStatus)) {
                params.setDcName("operateEventMaster");
                DataServiceUtils.dataLoad(dataSource, params);
            }
        }
    }
}

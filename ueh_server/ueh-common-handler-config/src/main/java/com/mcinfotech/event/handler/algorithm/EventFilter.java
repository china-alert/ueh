package com.mcinfotech.event.handler.algorithm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleEffect;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.utils.DateTimeUtils;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.db.ColumnDefine;
import cn.mcinfotech.data.service.db.DataType;
import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import cn.mcinfotech.data.service.util.DataSourceUtils;

/**
 * 事件屏蔽过滤
 * 针对单台设备中一个或几个字段符合条件时，对事件进行屏蔽过滤(修改FilterFlag字段)
 * 条件设置：[{"conditionColumn":"Node","conditionDataType":"string","operator":"=","conditionValue":"173.21.10.18"}]
 * 生效设置：[{"effectColumn":"FilterFlag","effectDataType":"string","effectType":"equal","effectValue":""}]
 *

 */
public class EventFilter {
    private static Logger logger = LogManager.getLogger(EventFilter.class);

    public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages, Map<String, ColumnDefine> columnMappings, List<EventHandlerRule> rules) {
        if (CollectionUtils.isEmpty(rawMessages)) return rawMessages;
        if (CollectionUtils.isEmpty(rules)) return rawMessages;

        List<Map<String, Object>> newMessages = new ArrayList<>();
        for (Map<String, Object> rawMessage : rawMessages) {
            Map<String, Object> newMessage = paddingFilterTypesByRules(rawMessage, rules, columnMappings);
            newMessages.add(newMessage);
        }
        return newMessages;
    }

    //每条消息 与每个策略里得每个条件进行对比
    private static Map<String, Object> paddingFilterTypesByRules(Map<String, Object> rawMessage, List<EventHandlerRule> rules, Map<String, ColumnDefine> columnDefineMap) {
        Set<Object> filterTypes = new HashSet<>();
        StringBuffer filterNames = new StringBuffer();
        for (EventHandlerRule rule : rules) {//每条策略
            //策略是否在有效期
            boolean notExpired = DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
            if (!notExpired) continue;
            String expressionStr = rule.getExpression();
            List<EventHandlerRuleExpression> expressions = JSON.parseArray(expressionStr, EventHandlerRuleExpression.class);
            String effectStr = rule.getEffect();
            List<EventHandlerRuleEffect> effects = JSON.parseArray(effectStr, EventHandlerRuleEffect.class);
            String effectValue = effects.get(0).getEffectValue();
            //"NS","NF","NN" 组成的集合
            List<String> effectValueList = JSON.parseArray(effectValue, String.class);
            boolean isValid1 = false;//是否符合多条规则  规则之间得关系为与 或
            String operatorLogic = null;//上一条规则的与下一条规则的关系，从前一条规则中取出
            for (EventHandlerRuleExpression expression : expressions) {//每条规则
                boolean isValid2 = false;//是否符合某条规则
                String conditionColumn = expression.getConditionColumn();
                String operator = expression.getOperator();
                String conditionValue = expression.getConditionValue();
                List<String> conditionValueList = JSON.parseArray(conditionValue, String.class);
                Object rawValue = rawMessage.get(conditionColumn);
                ColumnDefine columnDefine = columnDefineMap.get(conditionColumn);//列定义
                DataType dataType = columnDefine.getDataType();
                String conditionValueVo = "";
                String conditionValueVo2 = "";
                if (conditionValueList.size() == 1) {
                    conditionValueVo = conditionValueList.get(0);
                } else if (conditionValueList.size() == 2) {
                    conditionValueVo = conditionValueList.get(0);
                    conditionValueVo2 = conditionValueList.get(1);
                }
                if (dataType.equals(DataType.INT)) {
                    int rawValueInt = Integer.parseInt(String.valueOf(rawValue));
                    int conditionValueInt = -1;
                    if (!org.springframework.util.StringUtils.isEmpty(conditionValueVo)) {
                        conditionValueInt = Integer.parseInt(conditionValueVo);
                    }
                    if ("=".equals(operator)) {
                        isValid2 = rawValueInt == conditionValueInt;
                    } else if ("!=".equals(operator)) {
                        isValid2 = rawValueInt != conditionValueInt;
                    } else if ("<".equals(operator)) {
                        isValid2 = rawValueInt < conditionValueInt;
                    } else if ("<=".equals(operator)) {
                        isValid2 = rawValueInt <= conditionValueInt;
                    } else if (">".equals(operator)) {
                        isValid2 = rawValueInt > conditionValueInt;
                    } else if (">=".equals(operator)) {
                        isValid2 = rawValueInt >= conditionValueInt;
                    } else if ("in".equals(operator)) {
                        isValid2 = conditionValueList.contains(String.valueOf(rawValue));
                    } else if ("between".equals(operator)) {
                        int conditionValueInt2 = Integer.parseInt(conditionValueVo2);
                        if (conditionValueInt > conditionValueInt2) {
                            isValid2 = rawValueInt >= conditionValueInt2 && rawValueInt <= conditionValueInt;
                        } else {
                            isValid2 = rawValueInt >= conditionValueInt && rawValueInt <= conditionValueInt2;
                        }
                    } else if ("regex".equalsIgnoreCase(operator)) {//匹配正则
                        try {
                            String rawValueStr = String.valueOf(rawValue).trim();
                            Pattern pattern = Pattern.compile(conditionValueVo);
                            Matcher matcher = pattern.matcher(rawValueStr);
                            isValid2 = matcher.find();
                        } catch (java.lang.Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (dataType.equals(DataType.STRING)) {
                    String rawValueStr = String.valueOf(rawValue).trim();
                    if ("=".equals(operator)) {
                        isValid2 = rawValueStr.equalsIgnoreCase(conditionValueVo);
                    } else if ("!=".equals(operator)) {
                        isValid2 = !rawValueStr.equalsIgnoreCase(conditionValueVo);
                    } else if ("like".equals(operator)) {
                        isValid2 = rawValueStr.contains(conditionValueVo);
                    } else if ("regex".equalsIgnoreCase(operator)) {//匹配正则
                        try {
                            Pattern pattern = Pattern.compile(conditionValueVo);
                            Matcher matcher = pattern.matcher(rawValueStr);
                            isValid2 = matcher.find();
                        } catch (java.lang.Exception e) {
                            e.printStackTrace();
                        }
                    } else if (StringUtils.equalsAnyIgnoreCase(operator, "ip", "eip")) {//匹配ip地址段 192.168.1.1;192.168.1.3;192.168.1.1-254
                        if (StringUtils.isNotBlank(conditionValueVo)) {
                            List<String> ipList = new ArrayList<>();
                            StringTokenizer tokenizer = new StringTokenizer(conditionValueVo, ";；");
                            while (tokenizer.hasMoreTokens()) {
                                String token = tokenizer.nextToken();
                                String[] tokenArr = token.split("-");
                                if (tokenArr.length == 1) {
                                    ipList.add(token);
                                } else if (tokenArr.length == 2) {
                                    try {
                                        String tokenStart = tokenArr[0];
                                        int tokenEnd = Integer.valueOf(tokenArr[1]);
                                        ipList.add(tokenStart);
                                        String tokenCommon = tokenStart.substring(0, tokenStart.lastIndexOf(".") + 1);
                                        int tokenDiff = Integer.valueOf(tokenStart.substring(tokenStart.lastIndexOf(".") + 1));
                                        for (int i = tokenDiff; i < tokenEnd; i++) {
                                            ipList.add(tokenCommon + Integer.sum(1, i));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            isValid2 = ipList.contains(rawValueStr);
                            if ("eip".equalsIgnoreCase(operator)) {
                                isValid2 = !isValid2;
                            }
                        }
                    }
                } else if (dataType.equals(DataType.LONGTIMESTAMP)) {
                    if ("between".equals(operator)) {
                        conditionValueList = JSON.parseArray(conditionValueVo, String.class);
                        long conditionValueVol = Long.parseLong(conditionValueList.get(0));
                        long conditionValueVo2l = Long.parseLong(conditionValueList.get(1));
                        long rawValuel = Long.parseLong(String.valueOf(rawValue));
                        isValid2 = rawValuel >= conditionValueVol && rawValuel <= conditionValueVo2l;
                    }
                }
                if (StringUtils.isEmpty(operatorLogic)) {
                    isValid1 = isValid2;
                } else if ("&&".equals(operatorLogic)) {
                    isValid1 = isValid1 && isValid2;
                } else if ("||".equals(operatorLogic)) {
                    isValid1 = isValid1 || isValid2;
                }
                operatorLogic = expression.getOperatorLogic();
            }
            if (isValid1) {
                filterTypes.addAll(effectValueList);
                if (filterNames.length() > 0) {
                    filterNames.append("#");
                }
                filterNames.append(rule.getName());
                //维护期屏蔽结束时间处理
                if ("MP".equals(rule.getRuleType())) {
                    Long endTime = DateTimeUtils.getEndTime(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
                    Object oldEndTime = rawMessage.get("FilterEndTime");
                    if (oldEndTime == null) {
                        rawMessage.put("FilterEndTime", endTime);
                    } else {
                        if (endTime > (Long) oldEndTime) {
                            rawMessage.put("FilterEndTime", endTime);
                        }
                    }
                }
            }
        }
        if (filterTypes.size() > 0) {
            rawMessage.put("FilterFlag", JSON.toJSONString(filterTypes.toArray()));
            rawMessage.put("RefFilterRules", filterNames.toString());
        } else {
            rawMessage.put("FilterFlag", new ArrayList<>());
        }
        return rawMessage;
    }

    /**
     * 按照指定的条件字段进行匹配，然后将屏蔽结果写入指定字段
     *
     * @param rawMessages    原始消息
     * @param columnMappings 事件源与平台映射关系
     * @param rule           过滤规则
     * @return 过滤过的事件
     */
    public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages, Map<String, ColumnDefine> columnMappings, EventHandlerRule rule) {
        if (CollectionUtils.isEmpty(rawMessages)) return null;
        if (rule == null) return rawMessages;
        boolean notExpired = DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
        if (!notExpired) {
            return rawMessages;
        }
        String expressionStr = rule.getExpression();
        if (StringUtils.isEmpty(expressionStr)) return rawMessages;

        List<EventHandlerRuleExpression> expressions = FastJsonUtils.toList(expressionStr, EventHandlerRuleExpression.class);
        Map<String, EventHandlerRuleExpression> conditionColumns = new HashMap<String, EventHandlerRuleExpression>();
        for (EventHandlerRuleExpression expression : expressions) {
            conditionColumns.put(expression.getConditionColumn(), expression);
        }
        List<EventHandlerRuleEffect> effects = FastJsonUtils.toList(rule.getEffect(), EventHandlerRuleEffect.class);

        if (MapUtils.isEmpty(conditionColumns)) return rawMessages;
        if (CollectionUtils.isEmpty(effects)) return rawMessages;

        List<Map<String, Object>> newMessages = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> message : rawMessages) {
            /**
             * 规则匹配
             */

            boolean isValid = true;
            boolean currentValid = true;
            for (String key : conditionColumns.keySet()) {
                currentValid = true;
                Object messageValue = message.get(key);
                EventHandlerRuleExpression condition = conditionColumns.get(key);
                ColumnDefine columnDefine = columnMappings.get(key);
                if (columnDefine.getDataType() == DataType.STRING) {
                    String messageStrValue = (String) messageValue;
                    if (condition.getOperator().equalsIgnoreCase("=") || condition.getOperator().equalsIgnoreCase("in")) {
                        if (!messageStrValue.contains(condition.getConditionValue())) {
                            isValid &= false;
                            //break;
                        }
                    } else if (condition.getOperator().equalsIgnoreCase("regex")) {//匹配正则
                        try {
                            Pattern pattern = Pattern.compile(condition.getConditionValue());
                            Matcher matcher = pattern.matcher(messageStrValue);
                            while (!matcher.find()) {
                                isValid &= false;
                                //							break;
                            }
                        } catch (java.lang.Exception e) {
                            isValid &= false;
                            e.printStackTrace();
                        }
                    }
                } else if (columnDefine.getDataType() == DataType.INT) {
                    int messageIntValue = Integer.parseInt(messageValue.toString());
                    int conditionIntValue = Integer.parseInt(condition.getConditionValue());
                    if (conditionColumns.get(key).getOperator().equalsIgnoreCase(">")) {
                        if (!(messageIntValue > conditionIntValue)) {
                            isValid &= false;
                            //break;
                        }
                    } else if (condition.getOperator().equalsIgnoreCase("<")) {
                        if (!(messageIntValue < conditionIntValue)) {
                            isValid &= false;
                            //break;
                        }
                    } else if (condition.getOperator().equalsIgnoreCase("=")) {
                        if (!(messageIntValue == conditionIntValue)) {
                            isValid &= false;
                            //break;
                        }
                    } else if (condition.getOperator().equalsIgnoreCase("regex")) {//匹配正则
                        try {
                            String messageStrValue = String.valueOf(messageValue);
                            Pattern pattern = Pattern.compile(conditionColumns.get(key).getConditionValue());
                            Matcher matcher = pattern.matcher(messageStrValue);
                            while (!matcher.find()) {
                                isValid &= false;
    //							break;
                            }
                        } catch (java.lang.Exception e) {
                            isValid &= false;
                            e.printStackTrace();
                        }
                    }
                } else if (columnDefine.getDataType() == DataType.LONGTIMESTAMP) {
                    long messageLongValue = Long.parseLong(messageValue.toString());
                    long conditionLongValue = Long.parseLong(condition.getConditionValue());
                    if (condition.getOperator().equalsIgnoreCase(">")) {
                        if (!(messageLongValue > conditionLongValue)) {
                            isValid &= false;
                            //break;
                        }
                    } else if (condition.getOperator().equalsIgnoreCase("<")) {
                        if (!(messageLongValue < conditionLongValue)) {
                            isValid &= false;
                            break;
                        }
                    } else if (condition.getOperator().equalsIgnoreCase("=")) {
                        if (!(messageLongValue == conditionLongValue)) {
                            isValid &= false;
                            //break;
                        }
                    }
                } else if (columnDefine.getDataType() == DataType.FLOAT) {
                    float messageIntValue = Float.parseFloat(messageValue.toString());
                    float conditionIntValue = Float.parseFloat(condition.getConditionValue());
                    if (condition.getOperator().equalsIgnoreCase(">")) {
                        if (!(messageIntValue > conditionIntValue)) {
                            isValid &= false;
                            //break;
                        }
                    } else if (condition.getOperator().equalsIgnoreCase("<")) {
                        if (!(messageIntValue < conditionIntValue)) {
                            isValid &= false;
                            //break;
                        }
                    }
                }
                if (StringUtils.isNotEmpty(condition.getOperatorLogic())) {
                    if (condition.getOperatorLogic().equalsIgnoreCase("&&")) {
                        isValid = isValid && currentValid;
                    } else if (condition.getOperatorLogic().equalsIgnoreCase("||")) {
                        isValid = isValid || currentValid;
                    }
                }
            }
            /**
             * 进行生效
             */
            if (isValid) {
                Map<String, Object> newMessage = message;
                if (newMessage.containsKey("FilterFlag")) {
                    newMessage.remove("FilterFlag");
                }
                newMessage.put("FilterFlag", effects.get(0).getEffectValue());
                newMessages.add(newMessage);
            } else {
                newMessages.add(message);
            }
        }
        return newMessages;
    }
}

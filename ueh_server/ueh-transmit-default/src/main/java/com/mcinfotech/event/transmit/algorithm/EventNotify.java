package com.mcinfotech.event.transmit.algorithm;

import cn.mcinfotech.data.service.db.ColumnDefine;
import cn.mcinfotech.data.service.db.DataType;
import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 判断事件是否符合发送条件
 * 针对单台设备中一个或几个字段符合条件时，对事件进行通知条件判断
 * 条件设置：[{"conditionColumn":"Node","conditionDataType":"string","operator":"=","conditionValue":"173.21.10.18"}]
 *

 */
public class EventNotify {
    private static Logger logger = LogManager.getLogger(EventNotify.class);

    /**
     * @param message        原始消息
     * @param columnMappings 事件源与平台映射关系
     * @param rule           规则
     * @return
     */
    public static boolean excute(Map<String, Object> message, Map<String, ColumnDefine> columnMappings, EventHandlerRule rule) {
        if (message == null) return false;
        if (rule == null) return false;
        boolean isValid1 = false;//是否符合多条规则  规则之间得关系为与 或
        try {
            //策略是否过期
            boolean notExpired = DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
            if (!notExpired) {
                return false;
            }
            String expressionStr = rule.getExpression();
            if (StringUtils.isEmpty(expressionStr)) return false;

            List<EventHandlerRuleExpression> expressions = JSON.parseArray(expressionStr, EventHandlerRuleExpression.class);
            isValid1 = false;
            String operatorLogic = null;//上一条规则的与下一条规则的关系，从前一条规则中取出
            for (EventHandlerRuleExpression expression : expressions) {//每条规则
                boolean isValid2 = false;//是否符合某条规则
                String conditionColumn = expression.getConditionColumn();
                String operator = expression.getOperator();
                String conditionValue = expression.getConditionValue();
                List<String> conditionValueList = JSON.parseArray(conditionValue, String.class);
                Object rawValue = message.get(conditionColumn);
                ColumnDefine columnDefine = columnMappings.get(conditionColumn);//列定义
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
        } catch (Exception e) {
            isValid1=false;
            logger.error(e.getMessage());
        } finally {
            return isValid1;
        }
    }
}

package com.mcinfotech.event.handler.algorithm;

import cn.mcinfotech.data.service.db.ColumnDefine;
import cn.mcinfotech.data.service.db.DataType;
import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleEffect;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.utils.DateTimeUtils;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 事件级别升降级
 * 针对单台设备中一个或几个字段重复出现时，事件级别进行升降级
 * 条件设置：[{"conditionColumn":"Node","conditionDataType":"string","operator":"=","conditionValue":"173.21.10.18"}]
 * 生效设置：[{"effectColumn":"Severity","effectDataType":"int","effectType":"plus","effectValue":"1"}]
 *

 */
public class EventSeverityUpOrDown {
    private static Logger logger = LogManager.getLogger(EventSeverityUpOrDown.class);

    /**
     * 按照指定的条件进行升级或者降级。
     *
     * @param rawMessages    原始消息
     * @param columnMappings 事件源与平台映射关系
     * @param rules          生效规则配置
     * @return 升降级过的事件
     */
    public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages, Map<String, ColumnDefine> columnMappings, List<EventHandlerRule> rules) {
        if (CollectionUtils.isEmpty(rawMessages)) {
            return null;
        }
        if (rules == null) {
            return rawMessages;
        }
        try {
            //每条消息 与每个策略里得每个条件进行对比
            for (Map<String, Object> rawMessage : rawMessages) {
                //每条策略
                boolean isSeverityComplete = Boolean.FALSE;
                boolean isBusinessSeverityComplete = Boolean.FALSE;
                StringJoiner upDownRuleJoiner = new StringJoiner("#");
                for (EventHandlerRule rule : rules) {
                    boolean notExpired = DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
                    if (!notExpired) {
                        continue;
                    }
                    String expressionStr = rule.getExpression();
                    //{"hitType":"expression/condition"}，expression代表命中类型为表达式，condition代表命中类型为条件
                    String paramStr = rule.getParams();
                    if (StringUtils.isBlank(paramStr)) {
                        continue;
                    }
                    Map paramMap = JSON.parseObject(paramStr, Map.class);
                    String hitType = paramMap.get("hitType") + "";
                    String effectStr = rule.getEffect();
                    List<EventHandlerRuleExpression> expressions = JSON.parseArray(expressionStr, EventHandlerRuleExpression.class);
                    //[{"effectColumn":"Severity","effectDataType":"","effectType":"","effectValue":2}]，其中effectColumn值包括Severity、BusinessSeverity,Severity为事件级别，BusinessSeverity为业务级别
                    List<EventHandlerRuleEffect> effects = JSON.parseArray(effectStr, EventHandlerRuleEffect.class);
                    if ("condition".equals(hitType) && CollectionUtils.isEmpty(expressions)) {
                        continue;
                    }
                    if (CollectionUtils.isEmpty(effects)) {
                        continue;
                    }
                    String effectValue = effects.get(0).getEffectValue();
                    String effectColumn = effects.get(0).getEffectColumn();
                    String effectType = effects.get(0).getEffectType();
                    if ("expression".equals(hitType) && StringUtils.isBlank(effectType)) {
                        continue;
                    }
                    if ("Severity".equals(effectColumn) && isSeverityComplete) {
                        continue;
                    }
                    if ("BusinessSeverity".equals(effectColumn) && isBusinessSeverityComplete) {
                        continue;
                    }
                    //是否符合多条规则  规则之间得关系为与 或
                    boolean isValid1 = false;
                    //上一条规则的与下一条规则的关系，从前一条规则中取出
                    String operatorLogic = null;
                    //条件处理
                    if ("condition".equals(hitType)) {
                        //每条规则
                        for (EventHandlerRuleExpression expression : expressions) {
                            //是否符合某条规则
                            boolean isValid2 = isSingleExpressionValid(columnMappings, rawMessage, expression);
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
                            if (StringUtils.isBlank(effectValue)) {
                                continue;
                            }
                            if ("Severity".equals(effectColumn)) {
                                rawMessage.put(effectColumn, Integer.valueOf(effectValue));
                                isSeverityComplete = true;
                            }
                            if ("BusinessSeverity".equals(effectColumn)) {
                                rawMessage.put(effectColumn, effectValue);
                                isBusinessSeverityComplete = true;
                            }
                            upDownRuleJoiner.add(rule.getName());
                        }
                    }
                    //表达式处理
                    if ("expression".equals(hitType)) {
                        String newSeverity = buildBody(effectType, rawMessage);
                        if (StringUtils.isBlank(newSeverity)) {
                            continue;
                        }
                        if ("Severity".equals(effectColumn)&& NumberUtils.isDigits(newSeverity)) {
                            rawMessage.put(effectColumn, Integer.valueOf(newSeverity));
                            isSeverityComplete = true;
                        }
                        if ("BusinessSeverity".equals(effectColumn)) {
                            rawMessage.put(effectColumn, newSeverity);
                            isBusinessSeverityComplete = true;
                        }
                        upDownRuleJoiner.add(rule.getName());
                    }
                }
                rawMessage.put("RefUpdownRules", upDownRuleJoiner.toString());
            }
        } catch (Exception e) {
            logger.error("升降级策略执行异常", e);
            e.printStackTrace();
        }
        return rawMessages;
    }

    private static boolean isSingleExpressionValid(Map<String, ColumnDefine> columnMappings, Map<String, Object> rawMessage, EventHandlerRuleExpression expression) {
        boolean isValid2 = false;
        String conditionColumn = expression.getConditionColumn();
        String operator = expression.getOperator();
        String conditionValue = expression.getConditionValue();
        List<String> conditionValueList = JSON.parseArray(conditionValue, String.class);
        Object rawValue = rawMessage.get(conditionColumn);
        //列定义
        ColumnDefine columnDefine = columnMappings.get(conditionColumn);
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
            if (!StringUtils.isEmpty(conditionValueVo)) {
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
                isValid2 = conditionValueList.indexOf(String.valueOf(rawValue)) != -1;
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
                //匹配正则
            } else if ("regex".equalsIgnoreCase(operator)) {
                Pattern pattern = Pattern.compile(conditionValueVo);
                Matcher matcher = pattern.matcher(rawValueStr);
                isValid2 = matcher.find();
                //匹配ip地址段 192.168.1.1;192.168.1.3;192.168.1.1-254
            } else if (StringUtils.equalsAnyIgnoreCase(operator, "ip", "eip")) {
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
        return isValid2;
    }

    private static String buildBody(String templet, Map<String, Object> params) throws IOException, TemplateException {
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("upOrDownStrategy", templet);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setNumberFormat("#");
        cfg.setTemplateLoader(stringLoader);
        Writer out = new StringWriter(4096);
        Template template = cfg.getTemplate("upOrDownStrategy");
        template.process(params, out);
        return out.toString();
    }
}

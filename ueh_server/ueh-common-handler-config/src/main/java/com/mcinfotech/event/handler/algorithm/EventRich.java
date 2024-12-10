package com.mcinfotech.event.handler.algorithm;

import java.util.*;

import javax.sql.DataSource;

import com.mcinfotech.event.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleEffect;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.handler.domain.Expression;
import com.mcinfotech.event.handler.domain.RichResultColumn;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.db.ColumnDefine;
import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;

/**
 * 事件丰富
 * 针对符合条件的告警进行丰富：映射或者关联
 *

 */
public class EventRich {
    private static Logger logger = LogManager.getLogger(EventRich.class);

    /**
     * 丰富来源字段为单个的版本执行的方法
     *
     * @param rawMessages
     * @param columnDefineMap
     * @param richRules
     * @param dataSource
     * @param projectID
     * @return
     */
    public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages, Map<String, ColumnDefine> columnDefineMap, List<EventHandlerRule> richRules, DataSource dataSource, long projectID) {
        if (CollectionUtils.isEmpty(rawMessages)) return null;
        if (richRules == null) return rawMessages;
        try {
            for (Map<String, Object> rawMessage : rawMessages) {
                for (EventHandlerRule richRule : richRules) {
                    verticalRichV0(dataSource, projectID, rawMessage, richRule);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return rawMessages;
        }
    }

    /**
     * 丰富来源字段为多个的版本执行的方法
     *
     * @param rawMessages
     * @param richRules
     * @param dataSource
     * @param projectID
     * @return
     */
    public static Collection<Map<String, Object>> excuteMultiple(Collection<Map<String, Object>> rawMessages, List<EventHandlerRule> richRules, DataSource dataSource, long projectID) {
        if (CollectionUtils.isEmpty(rawMessages)) {
            return null;
        }
        if (richRules == null) {
            return rawMessages;
        }
        try {
            for (Map<String, Object> rawMessage : rawMessages) {
                StringJoiner ruleJoiner = new StringJoiner("#");
                for (EventHandlerRule richRule : richRules) {
                    boolean isNotExpired = DateTimeUtils.isValid(richRule.getExecType(), richRule.getIntervalType(), richRule.getDayOfWeekAt(), richRule.getDayOfWeekUtil(), richRule.getExecuteAt(), richRule.getExecuteUtil());
                    if (!isNotExpired) {
                        continue;
                    }
                    boolean success;
                    String expressionStr = richRule.getExpression();
                    if (StringUtils.isBlank(expressionStr)) {
                        logger.warn("条件为空{}", expressionStr);
                        continue;
                    }
                    List<Expression> expressions = JSON.parseArray(expressionStr, Expression.class);
                    String tableName = expressions.get(0).getTableName();
                    if (StringUtils.equals("t_res_attribute_set", tableName)) {
                        success = horizontalRich(dataSource, projectID, rawMessage, richRule);
                    } else {
                        success = verticalRich(dataSource, projectID, rawMessage, richRule);
                    }
                    if (success) {
                        ruleJoiner.add(richRule.getName());
                    }
                }
                rawMessage.put("RefRichRules", ruleJoiner.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return rawMessages;
        }
    }

    private static boolean verticalRich(DataSource dataSource, long projectID, Map<String, Object> rawMessage, EventHandlerRule richRule) {
        String expressionStr = richRule.getExpression();
        if (StringUtils.isBlank(expressionStr)) {
            logger.info("条件为空{}", expressionStr);
            //continue;
            return Boolean.FALSE;
        }
        List<Expression> expressions = JSON.parseArray(expressionStr, Expression.class);
        String tableName = expressions.get(0).getTableName();
        //丰富来源
        String result = expressions.get(0).getResult();
        //获取丰富来源字段来源id
        ResultPattern res = getVerticalDataId(dataSource, projectID, rawMessage, expressions, tableName);
        List<Map<String, Object>> tableIdList = res.getDatas();
        if (res.isSuccess() && tableIdList.size() == 1) {
            Object dataId = tableIdList.get(0).get("dataId");
            //拼接丰富来源字段
            if (StringUtils.isBlank(result)) {
                logger.warn("丰富来源字段为空{}", result);
                //continue;
                return Boolean.FALSE;
            }
            List<RichResultColumn> richResultColumns = JSON.parseArray(result, RichResultColumn.class);
            if (res.isSuccess() && res.getDatas().size() > 0) {
                String sourceRichResult = getVerticalSourceRichResult(dataSource, projectID, dataId, tableName, richResultColumns);
                //生效
                String effectStr = richRule.getEffect();
                List<EventHandlerRuleEffect> effect = JSON.parseArray(effectStr, EventHandlerRuleEffect.class);
                String effectValue = effect.get(0).getEffectValue();
                String operate = richResultColumns.get(0).getOperate();
                if ("append".equals(operate)) {
                    Object o = rawMessage.get(effectValue);
                    if (o != null) {
                        sourceRichResult = sourceRichResult.concat(String.valueOf(o));
                    }
                    rawMessage.put(effectValue, sourceRichResult);
                } else if ("cover".equals(operate)) {
                    rawMessage.put(effectValue, sourceRichResult);
                }
            }
            //break;
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private static boolean verticalRichV0(DataSource dataSource, long projectID, Map<String, Object> rawMessage, EventHandlerRule richRule) {
        String expressionStr = richRule.getExpression();
        if (StringUtils.isBlank(expressionStr)) {
            logger.info("条件为空{}", expressionStr);
            //continue;
            return Boolean.FALSE;
        }
        List<Expression> expressions = JSON.parseArray(expressionStr, Expression.class);
        String tableName = expressions.get(0).getTableName();
        String resultColumnValue = expressions.get(0).getResultColumnValue();
        List<Map<String, String>> columnNames = new ArrayList<>();
        for (Expression expression : expressions) {
            String columnName = expression.getColumnName();
            String conditionColumn = expression.getConditionColumn();
            Map<String, String> map = new HashMap<>();
            map.put("columnName", columnName);
            map.put("columnValue", (String) rawMessage.get(conditionColumn));
            columnNames.add(map);
        }

        Map<String, Object> filter = new HashMap<>();
        filter.put("tableName", tableName);
        filter.put("resultColumnValue", resultColumnValue);
        filter.put("columnNames", columnNames);
        DataLoadParams params = new DataLoadParams();
        params.setDcName("selectRichColumnValue");
        params.setProjectId(projectID);
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(-10);
        params.setEngine(SQLEngine.Freemarker);
        ResultPattern resultPattern = DataServiceUtils.dataLoad(dataSource, params);

        if (resultPattern.isSuccess() && resultPattern.getDatas().size() > 0) {
            String effectStr = richRule.getEffect();
            List<EventHandlerRuleEffect> effect = JSON.parseArray(effectStr, EventHandlerRuleEffect.class);
            String effectValue = effect.get(0).getEffectValue();
            rawMessage.put(effectValue, resultPattern.getDatas().get(0).get("resultValue"));
            //break;
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private static boolean horizontalRich(DataSource dataSource, long projectID, Map<String, Object> rawMessage, EventHandlerRule richRule) {

        String expressionStr = richRule.getExpression();
        if (StringUtils.isBlank(expressionStr)) {
            logger.warn("条件为空{}", expressionStr);
            //continue;
            return Boolean.FALSE;
        }
        List<Expression> expressions = JSON.parseArray(expressionStr, Expression.class);
        String tableName = expressions.get(0).getTableName();
        String result = expressions.get(0).getResult();
        //获取丰富来源字段来源id
        ResultPattern res = getDataId(dataSource, projectID, rawMessage, expressions, tableName);
        List<Map<String, Object>> tableIdList = res.getDatas();
        if (res.isSuccess() && tableIdList.size() == 1) {
            Object dataId = tableIdList.get(0).get("dataId");
            //拼接丰富来源字段
            if (StringUtils.isBlank(result)) {
                logger.warn("丰富来源字段为空{}", result);
                //continue;
                return Boolean.FALSE;
            }
            List<RichResultColumn> richResultColumns = JSON.parseArray(result, RichResultColumn.class);
            if (res.isSuccess() && res.getDatas().size() > 0) {
                String sourceRichResult = getSourceRichResult(dataSource, projectID, dataId, richResultColumns);
                //生效
                String effectStr = richRule.getEffect();
                List<EventHandlerRuleEffect> effect = JSON.parseArray(effectStr, EventHandlerRuleEffect.class);
                String effectValue = effect.get(0).getEffectValue();
                String operate = richResultColumns.get(0).getOperate();
                if ("append".equals(operate)) {
                    Object o = rawMessage.get(effectValue);
                    if (o != null) {
                        sourceRichResult = sourceRichResult.concat(String.valueOf(o));
                    }
                    rawMessage.put(effectValue, sourceRichResult);
                } else if ("cover".equals(operate)) {
                    rawMessage.put(effectValue, sourceRichResult);
                }
            }
            //break;
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private static String getSourceRichResult(DataSource dataSource, long projectID, Object dataId, List<RichResultColumn> richResultColumns) {
        StringBuilder richResult = new StringBuilder();
        for (RichResultColumn richResultColumn : richResultColumns) {
            String columnName = richResultColumn.getColumnName();
            String columnValue = richResultColumn.getColumnValue();
            Map<String, Object> filter = new HashMap<>();
            filter.put("dataId", dataId);
            filter.put("columnValue", columnValue);
            DataLoadParams params = new DataLoadParams();
            params.setDcName("getColumnValue");
            params.setProjectId(projectID);
            params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
            params.setStart(1);
            params.setLimit(-10);
            params.setEngine(SQLEngine.Freemarker);
            ResultPattern res = DataServiceUtils.dataLoad(dataSource, params);
            richResult.append(columnName).append(":").append(res.getStrData()).append(',');
        }
        return StringUtils.substringBeforeLast(richResult.toString(), ",");
    }

    private static String getVerticalSourceRichResult(DataSource dataSource, long projectID, Object dataId, String tableName, List<RichResultColumn> richResultColumns) {
        StringBuilder richResult = new StringBuilder();
        for (RichResultColumn richResultColumn : richResultColumns) {
            String columnName = richResultColumn.getColumnName();
            String columnValue = richResultColumn.getColumnValue();
            Map<String, Object> filter = new HashMap<>();
            filter.put("dataId", dataId);
            filter.put("tableName", tableName);
            filter.put("columnValue", columnValue);
            DataLoadParams params = new DataLoadParams();
            params.setDcName("getVerticalColumnValue");
            params.setProjectId(projectID);
            params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
            params.setStart(1);
            params.setLimit(-10);
            params.setEngine(SQLEngine.Freemarker);
            ResultPattern res = DataServiceUtils.dataLoad(dataSource, params);
            richResult.append(columnName).append(":").append(res.getStrData()).append(',');
        }
        return StringUtils.substringBeforeLast(richResult.toString(), ",");
    }

    private static ResultPattern getDataId(DataSource dataSource, long projectID, Map<String, Object> rawMessage, List<Expression> expressions, String tableName) {
        List<Map<String, String>> columnNames = new ArrayList<>();
        for (Expression expression : expressions) {
            String columnName = expression.getColumnName();
            String conditionColumn = expression.getConditionColumn();
            String columnValue = (String) rawMessage.get(conditionColumn);
            if ("fill".equals(expression.getConditionDataType())) {
                columnValue = conditionColumn;
            }
            Map<String, String> map = new HashMap<>();
            map.put("columnName", columnName);
            map.put("columnValue", columnValue);
            columnNames.add(map);
        }
        Map<String, Object> filter = new HashMap<>();
        filter.put("tableName", tableName);
        filter.put("columnNames", columnNames);
        filter.put("count", columnNames.size());
        DataLoadParams params = new DataLoadParams();
        params.setDcName("getDataId");
        params.setProjectId(projectID);
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(-10);
        params.setEngine(SQLEngine.Freemarker);
        ResultPattern res = DataServiceUtils.dataLoad(dataSource, params);
        if (res.getDatas().size() > 1) {
            logger.warn("匹配到两个表{}", res.getDatas());
        }
        return res;
    }

    private static ResultPattern getVerticalDataId(DataSource dataSource, long projectID, Map<String, Object> rawMessage, List<Expression> expressions, String tableName) {
        List<Map<String, String>> columnNames = new ArrayList<>();
        for (Expression expression : expressions) {
            String columnName = expression.getColumnName();
            String conditionColumn = expression.getConditionColumn();
            String columnValue = (String) rawMessage.get(conditionColumn);
            if ("fill".equals(expression.getConditionDataType())) {
                columnValue = conditionColumn;
            }
            Map<String, String> map = new HashMap<>();
            map.put("columnName", columnName);
            map.put("columnValue", columnValue);
            columnNames.add(map);
        }
        Map<String, Object> filter = new HashMap<>();
        filter.put("tableName", tableName);
        filter.put("columnNames", columnNames);
        DataLoadParams params = new DataLoadParams();
        params.setDcName("getVerticalDataId");
        params.setProjectId(projectID);
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(-10);
        params.setEngine(SQLEngine.Freemarker);
        ResultPattern res = DataServiceUtils.dataLoad(dataSource, params);
        if (res.getDatas().size() > 1) {
            logger.warn("匹配到两个表{}", res.getDatas());
        }
        return res;
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
    /**
     *
     * @param rawMessages
     * @param columnMappings
     * @param formular
     * @param expressions
     * @param effects
     * @return
     *//*
	public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages,Map<String,PlatformColumnMapping> columnMappings,String formular,Map<String,EventHandlerRuleExpression> expressions,List<EventHandlerRuleRichEffect> effects){
		if(CollectionUtils.isEmpty(rawMessages))return null;
		if(StringUtils.isEmpty(formular))return rawMessages;
		if(MapUtils.isEmpty(expressions))return rawMessages;
		if(CollectionUtils.isEmpty(effects))return rawMessages;
		*//*boolean expired=DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
		if(expired){
			return rawMessages;
		}*//*
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
		for(Map<String,Object> message:rawMessages){
			*//**
     * 规则匹配
     *//*
			
			boolean isValid=true;
			for(String key:expressions.keySet()){
				Object messageValue=message.get(key);
				if(columnMappings.get(key).getPlatformDataType().equalsIgnoreCase("string")){
					String messageStrValue=(String) messageValue;
					if(expressions.get(key).getOperator().equalsIgnoreCase("=")||expressions.get(key).getOperator().equalsIgnoreCase("in")){
						if(!messageStrValue.contains(expressions.get(key).getConditionValue())){
							isValid&=false;
							break;
						}
					}
				}else if(columnMappings.get(key).getPlatformDataType().equalsIgnoreCase("int")){
					int messageIntValue=Integer.parseInt(messageValue.toString());
					int conditionIntValue=Integer.parseInt(expressions.get(key).getConditionValue());
					if(expressions.get(key).getOperator().equalsIgnoreCase(">")){
						if(!(messageIntValue>conditionIntValue)){
							isValid&=false;
							break;
						}
					}else if(expressions.get(key).getOperator().equalsIgnoreCase("<")){
						if(!(messageIntValue<conditionIntValue)){
							isValid&=false;
							break;
						}
					}else if(expressions.get(key).getOperator().equalsIgnoreCase("=")){
						if(!(messageIntValue==conditionIntValue)){
							isValid&=false;
							break;
						}
					}
				}else if(columnMappings.get(key).getPlatformDataType().equalsIgnoreCase("longtimestamp")){
					long messageLongValue=Long.parseLong(messageValue.toString());
					long conditionLongValue=Long.parseLong(expressions.get(key).getConditionValue());
					if(expressions.get(key).getOperator().equalsIgnoreCase(">")){
						if(!(messageLongValue>conditionLongValue)){
							isValid&=false;
							break;
						}
					}else if(expressions.get(key).getOperator().equalsIgnoreCase("<")){
						if(!(messageLongValue<conditionLongValue)){
							isValid&=false;
							break;
						}
					}else if(expressions.get(key).getOperator().equalsIgnoreCase("=")){
						if(!(messageLongValue==conditionLongValue)){
							isValid&=false;
							break;
						}
					}
				}else if(columnMappings.get(key).getPlatformDataType().equalsIgnoreCase("float")){
					float messageLongValue=Float.parseFloat(messageValue.toString());
					float conditionLongValue=Float.parseFloat(expressions.get(key).getConditionValue());
					if(expressions.get(key).getOperator().equalsIgnoreCase(">")){
						if(!(messageLongValue>conditionLongValue)){
							isValid&=false;
							break;
						}
					}else if(expressions.get(key).getOperator().equalsIgnoreCase("<")){
						if(!(messageLongValue<conditionLongValue)){
							isValid&=false;
							break;
						}
					}
				}
			}
			*//**
     * 进行生效
     *//*
			if(isValid){
				Map<String,Object> newMessage=message;
				for(EventHandlerRuleRichEffect effect:effects){
					Object currentValue=newMessage.get(effect.getRichColumn());
					PlatformColumnMapping columnMapping=columnMappings.get(effect.getRichColumn());
					if(effect.getRichType().equalsIgnoreCase("mapping")){
						if(columnMapping.getPlatformDataType().equalsIgnoreCase("int")){
							int currentIntValue=Integer.parseInt(currentValue.toString());
							for(EventHandlerRuleRichMapping mapping:effect.getValueMapping()){
								int conditionValue=Integer.parseInt(mapping.getSourceValue());
								if(currentIntValue==conditionValue){
									if(mapping.getEffectDataType().equalsIgnoreCase("int")){
										newMessage.put(mapping.getEffectColumn(), Integer.parseInt(mapping.getEffectValue()));
									}else if(mapping.getEffectDataType().equalsIgnoreCase("string")){
										newMessage.put(mapping.getEffectColumn(), mapping.getEffectValue());
									}
									break;
								}
							}
						}else if(columnMapping.getPlatformDataType().equalsIgnoreCase("string")){
							String currentStrValue=currentValue.toString();
							for(EventHandlerRuleRichMapping mapping:effect.getValueMapping()){
								String conditionValue=mapping.getSourceValue();
								if(currentStrValue.equalsIgnoreCase(conditionValue)){
									if(mapping.getEffectDataType().equalsIgnoreCase("int")){
										newMessage.put(mapping.getEffectColumn(), Integer.parseInt(mapping.getEffectValue()));
									}else if(mapping.getEffectDataType().equalsIgnoreCase("string")){
										newMessage.put(mapping.getEffectColumn(), mapping.getEffectValue());
									}
									break;
								}
							}
						}
					}else if(effect.getRichType().equalsIgnoreCase("relation")){
						//需要根据关联字段及值进行数据库查询
						*//*if(columnMapping.getPlatformDataType().equalsIgnoreCase("int")){
							int currentIntValue=Integer.parseInt(currentValue.toString());
							//根据事件中当前的值，去关联表中查询关联字段
							//然后按字段进行数据迁移
							for(EventHandlerRuleRichMapping mapping:effect.getValueMapping()){
								int conditionValue=Integer.parseInt(mapping.getSourceValue());
								if(currentIntValue==conditionValue){
									if(mapping.getEffectDataType().equalsIgnoreCase("int")){
										newMessage.put(mapping.getEffectColumn(), Integer.parseInt(mapping.getEffectValue()));
									}else if(mapping.getEffectDataType().equalsIgnoreCase("string")){
										newMessage.put(mapping.getEffectColumn(), mapping.getEffectValue());
									}
									break;
								}
							}
						}else if(columnMapping.getPlatformDataType().equalsIgnoreCase("string")){
							String currentStrValue=currentValue.toString();
							for(EventHandlerRuleRichMapping mapping:effect.getValueMapping()){
								String conditionValue=mapping.getSourceValue();
								if(currentStrValue.equalsIgnoreCase(conditionValue)){
									if(mapping.getEffectDataType().equalsIgnoreCase("int")){
										newMessage.put(mapping.getEffectColumn(), Integer.parseInt(mapping.getEffectValue()));
									}else if(mapping.getEffectDataType().equalsIgnoreCase("string")){
										newMessage.put(mapping.getEffectColumn(), mapping.getEffectValue());
									}
									break;
								}
							}
						}*//*
					}
				}
				result.add(newMessage);
			}else{
				result.add(message);
			}
		}
		return result;
	}*/
}

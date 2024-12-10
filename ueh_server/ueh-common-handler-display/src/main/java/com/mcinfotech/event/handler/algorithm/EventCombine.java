package com.mcinfotech.event.handler.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.handler.domain.EventHandlerRuleRichEffect;
import com.mcinfotech.event.handler.domain.EventHandlerRuleRichMapping;
import com.mcinfotech.event.handler.domain.PlatformColumnMapping;
import com.mcinfotech.event.handler.domain.PlatformProbeColumnMapping;

/**
 * 事件丰富
 * 针对符合条件的告警进行丰富：映射或者关联
 * 关联暂未实现（20210408）

 *
 */
public class EventCombine {
	private static Logger logger=LogManager.getLogger(EventCombine.class);
	/**
	 * 
	 * @param rawMessages
	 * @param columnMappings
	 * @param formular
	 * @param expressions
	 * @param effects
	 * @return
	 */
	public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages,Map<String,PlatformColumnMapping> columnMappings,String formular,Map<String,EventHandlerRuleExpression> expressions,List<EventHandlerRuleRichEffect> effects){
		if(CollectionUtils.isEmpty(rawMessages))return null;
		if(StringUtils.isEmpty(formular))return rawMessages;
		if(MapUtils.isEmpty(expressions))return rawMessages;
		if(CollectionUtils.isEmpty(effects))return rawMessages;
		/*boolean expired=DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
		if(expired){
			return rawMessages;
		}*/
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
		for(Map<String,Object> message:rawMessages){
			/**
			 * 规则匹配
			 */
			
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
				}
			}
			/**
			 * 进行生效
			 */
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
						/*if(columnMapping.getPlatformDataType().equalsIgnoreCase("int")){
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
						}*/
					}
				}
				result.add(newMessage);
			}else{
				result.add(message);
			}
		}
		return result;
	}
}

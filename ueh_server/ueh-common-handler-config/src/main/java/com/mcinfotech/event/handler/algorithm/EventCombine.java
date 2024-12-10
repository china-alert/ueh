package com.mcinfotech.event.handler.algorithm;

import cn.mcinfotech.data.service.db.ColumnDefine;
import cn.mcinfotech.data.service.db.DataType;
import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleEffect;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 事件合并,暂未实现
 * 针对单台设备中一个或几个字段重复出现时，对最后发生事件、发生次数进行更新(目前是可以自由定义)
 * 条件字段：[{"conditionColumn":"Node","conditionDataType":"string","operator":"","conditionValue":""},{"conditionColumn":"NodeAlias","conditionDataType":"string","operator":"","operator":"","conditionValue":""},{"conditionColumn":"AlertGroup","conditionDataType":"string","operator":"","conditionValue":""},{"conditionColumn":"AlertKey","conditionDataType":"string","operator":"","conditionValue":""},{"conditionColumn":"Summary","conditionDataType":"string","operator":"","conditionValue":""}]
 * 生效字段：[{"effectColumn":"Tally","effectDataType":"int","effectType":"counter","effectValue":""},{"effectColumn":"LastOccurrence","effectDataType":"longtimestamp","effectType":"newest","effectValue":""}]

 *
 */
public class EventCombine {
	private static Logger logger=LogManager.getLogger(EventCombine.class);
	/**
	 * 按照指定的字段进行合并，必须是内容一致的才能做合并。
	 * @param rawMessages 原始消息
	 * @param
	 * @param rules 合并规则配置，参考EventHandlerRule
	 * @return 合并过的事件
	 */
	public static Collection<Map<String, Object>> excute(Collection<Map<String, Object>> rawMessages, Map<String, ColumnDefine> columnDefineMap, List<EventHandlerRule> rules){
		if(CollectionUtils.isEmpty(rawMessages))return null;
		if(rules==null)return rawMessages;

		try {
			for (Map<String, Object> rawMessage : rawMessages) {//每条消息 与每个策略里得每个条件进行对比
				ArrayList<Object> combineFlag = new ArrayList<>();
				for (EventHandlerRule rule : rules) {//每条策略
					String expressionStr = rule.getExpression();
					List<EventHandlerRuleExpression> expressions = JSON.parseArray(expressionStr, EventHandlerRuleExpression.class);
					String effectStr = rule.getEffect();
					List<EventHandlerRuleEffect> effects = JSON.parseArray(effectStr, EventHandlerRuleEffect.class);
					String effectValue = effects.get(0).getEffectValue();
					boolean isValid1 = false;//是否符合多条规则  规则之间得关系为与 或
					String operatorLogic=null;//上一条规则的与下一条规则的关系，从前一条规则中取出
					for (EventHandlerRuleExpression expression : expressions) {//每条规则
						boolean isValid2=false;//是否符合某条规则
						String conditionColumn = expression.getConditionColumn();
						String operator = expression.getOperator();
						String conditionValue = expression.getConditionValue();
						List<String> conditionValueList = JSON.parseArray(conditionValue,String.class);
						Object rawValue = rawMessage.get(conditionColumn);
						ColumnDefine columnDefine = columnDefineMap.get(conditionColumn);//列定义
						DataType dataType = columnDefine.getDataType();
						String conditionValueVo="";
						String conditionValueVo2="";
						if(conditionValueList.size()==1){
							conditionValueVo=conditionValueList.get(0);
						}else if(conditionValueList.size()==2){
							conditionValueVo=conditionValueList.get(0);
							conditionValueVo2=conditionValueList.get(1);
						}
						if (dataType.equals(DataType.INT)){
							int rawValueInt = Integer.parseInt(String.valueOf(rawValue));
							int conditionValueInt=-1;
							if(!StringUtils.isEmpty(conditionValueVo)) {
								conditionValueInt = Integer.parseInt(conditionValueVo);
							}
							if("=".equals(operator)){
								isValid2= rawValueInt == conditionValueInt;
							}else if("!=".equals(operator)){
								isValid2= rawValueInt != conditionValueInt;
							}else if("<".equals(operator)){
								isValid2= rawValueInt < conditionValueInt;
							}else if("<=".equals(operator)){
								isValid2= rawValueInt <= conditionValueInt;
							}else if(">".equals(operator)){
								isValid2= rawValueInt > conditionValueInt;
							}else if(">=".equals(operator)){
								isValid2= rawValueInt >= conditionValueInt;
							}else if("in".equals(operator)){
								isValid2=conditionValueList.indexOf(String.valueOf(rawValue))!=-1;
							}else if("between".equals(operator)){
								int conditionValueInt2 = Integer.parseInt(conditionValueVo2);
								if(conditionValueInt>conditionValueInt2){
									isValid2=rawValueInt>=conditionValueInt2&&rawValueInt<=conditionValueInt;
								}else {
									isValid2=rawValueInt>=conditionValueInt&&rawValueInt<=conditionValueInt2;
								}
							}else if ("regex".equalsIgnoreCase(operator)){//匹配正则
								try {
									String rawValueStr = String.valueOf(rawValue).trim();
									Pattern pattern = Pattern.compile(conditionValueVo);
									Matcher matcher = pattern.matcher(rawValueStr);
									isValid2=matcher.find();
								} catch (java.lang.Exception e) {
									e.printStackTrace();
								}
							}
						}else if (dataType.equals(DataType.STRING)){
							String rawValueStr = String.valueOf(rawValue).trim();
							if("=".equals(operator)){
								isValid2=rawValueStr.equalsIgnoreCase(conditionValueVo);
							}else if("!=".equals(operator)){
								isValid2=!rawValueStr.equalsIgnoreCase(conditionValueVo);
							}else if("like".equals(operator)){
								isValid2= rawValueStr.contains(conditionValueVo);
							}else if ("regex".equalsIgnoreCase(operator)){//匹配正则
								try {
									Pattern pattern = Pattern.compile(conditionValueVo);
									Matcher matcher = pattern.matcher(rawValueStr);
									isValid2=matcher.find();
								} catch (java.lang.Exception e) {
									e.printStackTrace();
								}
							}
						}else if (dataType.equals(DataType.LONGTIMESTAMP)){
							if("between".equals(operator)){
								conditionValueList=JSON.parseArray(conditionValueVo,String.class);
								long conditionValueVol = Long.parseLong(conditionValueList.get(0));
								long conditionValueVo2l = Long.parseLong(conditionValueList.get(1));
								long rawValuel = Long.parseLong(String.valueOf(rawValue) );
								isValid2=rawValuel>=conditionValueVol&&rawValuel<=conditionValueVo2l;
							}
						}
						if(StringUtils.isEmpty(operatorLogic)){
							isValid1=isValid2;
						}else if("&&".equals(operatorLogic)){
							isValid1=isValid1&&isValid2;
						}else if("||".equals(operatorLogic)){
							isValid1=isValid1||isValid2;
						}
						operatorLogic = expression.getOperatorLogic();
					}
					if (isValid1) combineFlag.add(effectValue);
				}
				rawMessage.put("CombineFlag", JSON.toJSONString(combineFlag));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rawMessages;
	}
}
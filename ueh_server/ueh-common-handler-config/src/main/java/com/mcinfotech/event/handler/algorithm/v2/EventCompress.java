package com.mcinfotech.event.handler.algorithm.v2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.sql.DataSource;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.domain.Constant;
import com.mcinfotech.event.domain.EventConstant;
import com.mcinfotech.event.domain.EventSourceType;
import com.mcinfotech.event.domain.ExecuteScope;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.handler.config.EventCompressConfig;
import com.mcinfotech.event.handler.domain.CompressParam;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleEffect;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.utils.DateTimeUtils;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.ResultPattern;

/**
 * 事件压缩 针对单台设备中一个或几个字段重复出现时，对最后发生事件、发生次数进行更新(目前是可以自由定义)
 * 条件字段：[{"conditionColumn":"Node","conditionDataType":"string","operator":"","conditionValue":""},{"conditionColumn":"NodeAlias","conditionDataType":"string","operator":"","operator":"","conditionValue":""},{"conditionColumn":"AlertGroup","conditionDataType":"string","operator":"","conditionValue":""},{"conditionColumn":"AlertKey","conditionDataType":"string","operator":"","conditionValue":""},{"conditionColumn":"Summary","conditionDataType":"string","operator":"","conditionValue":""}]
 * 生效字段：[{"effectColumn":"Tally","effectDataType":"int","effectType":"counter","effectValue":""},{"effectColumn":"LastOccurrence","effectDataType":"longtimestamp","effectType":"newest","effectValue":""}]
 *

 */
public class EventCompress {
	private static Logger logger = LogManager.getLogger();
	/**
	 * 默认条件字段
	 */
	private static List<String> defaultconditionColumns = Arrays.asList(EventConstant.eventColumnNode, EventConstant.eventColumnAlertKey, EventConstant.eventColumnSummary, EventConstant.eventColumnEventSeverityType);
	/**
	 * 默认结果字段
	 */
	private static List<EventHandlerRuleEffect> effectRules = new ArrayList<>();

	static {
		effectRules.add(new EventHandlerRuleEffect(EventConstant.effectTally,"int",EventConstant.effctTypeCounter,null));
		effectRules.add(new EventHandlerRuleEffect(EventConstant.effectLastOccurrence,"longtimestamp",EventConstant.effectTypeNewest,null));
	}

	/**
	 * 1.按照内置压缩规则进行压缩，
	 * 2.按照指定的字段进行压缩，必须是内容一致的才能做压缩。
	 *
	 * @param rawMessages  原始事件集合
	 * @param rules        压缩规则
	 * @param dataSource   数据源
	 * @param projectID    项目ID
	 * @param recoveryRule 恢复规则
	 * @return 处理后事件集合
	 */
	public static Collection<Map<String, Object>> excute(ProbeInfo probeInfo,Collection<Map<String, Object>> rawMessages,List<EventHandlerRule> rules, DataSource dataSource, long projectID, EventHandlerRule recoveryRule) {
		if (CollectionUtils.isEmpty(rawMessages))
			return null;

		Map<String, Map<String, Object>> result = new HashMap<>();
		// 1不查库压缩 内置压缩策略压缩，,如果是级联来的告警事件内存中压缩，将不再处理
		if(probeInfo.getEventSourceType()!=EventSourceType.CASCADE) {
			for (Map<String, Object> message : rawMessages) {
				// 按照指定的条件字段进行取值然后构建事件消息的KEY
				StringBuffer identifier = new StringBuffer();
				for (String key : defaultconditionColumns) {
					identifier.append(message.get(key));
				}
				String identifierKey = identifier.toString();
				if (logger.isDebugEnabled())
					logger.debug("current identifier : " + identifierKey);
				// 每个条消息的Identifier由压缩字段组成，不再单独指定。
				message.put(EventConstant.eventColumnIdentifier, identifierKey);
				// 结果中不包含唯一key,次数加1,最后发生时间更新为最新
				if (result.containsKey(identifierKey)) {
					Map<String, Object> newMessage = result.get(identifierKey);
					result.put(identifierKey, doCompress(Constant.FLAG_INTERNAL,effectRules,message, newMessage));
				} else {
					result.put(identifierKey, message);
				}
			}
			rawMessages.clear();
			rawMessages.addAll(result.values());
		}
		//按照规则执行压缩
		return excuteCustomRule(probeInfo,rawMessages, rules, dataSource, projectID, recoveryRule);
	}

	/**
	 * 按照配置策略进行压缩：字段匹配、长周期。
	 *
	 * @param rawMessages 原始消息
	 * @param rules       压缩规则配置，参考EventHandlerRule
	 * @return 压缩过的事件
	 */
	public static Collection<Map<String, Object>> excuteCustomRule(ProbeInfo probeInfo,Collection<Map<String, Object>> rawMessages,List<EventHandlerRule> rules, DataSource dataSource, long projectID, EventHandlerRule recoveryRule) {
		if (CollectionUtils.isEmpty(rawMessages))
			return null;
		/**
		 * 如果没有压缩规则退出返回
		 * 
		 */
		if (rules == null)
			return rawMessages;
		for (EventHandlerRule rule : rules) {
			boolean notExpired = DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(),rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
			if (!notExpired) {
				continue;
			}
			//如果是级联来的信息，收到事件之后不再进行规则处理。
			if(probeInfo.getEventSourceType()==EventSourceType.CASCADE&&(rule.getExecuteScope()==ExecuteScope.S||rule.getExecuteScope()==ExecuteScope.MS)) {
				continue;
			}
			// 装配条件字段和结果字段
			String expressionStr = rule.getExpression();
			List<EventHandlerRuleExpression> expressions = FastJsonUtils.toList(expressionStr,EventHandlerRuleExpression.class);
			List<String> conditionColumns = new ArrayList<>();
			for (EventHandlerRuleExpression expression : expressions) {
				conditionColumns.add(expression.getConditionColumn());
			}
			if (CollectionUtils.isEmpty(conditionColumns))
				continue;
			List<EventHandlerRuleEffect> effects = FastJsonUtils.toList(rule.getEffect(), EventHandlerRuleEffect.class);

			Map<String, Map<String, Object>> result = new HashMap<>();
			// 1根据压缩策略压缩内存中的事件
			for (Map<String, Object> message : rawMessages) {
				if (message.get("isCompress") != null) {
					result.put(String.valueOf(message.get(EventConstant.eventColumnIdentifier)), message);
					continue;// 被压缩过
				}
					/**
					 * 按照指定的条件字段进行取值然后构建事件消息的KEY
					 */
				StringBuffer identifier = new StringBuffer();
				for (String key : conditionColumns) {
					identifier.append(message.get(key));
				}
				if ("S".equals(message.get(EventConstant.eventColumnEventType))) {// 如果是可恢复事件，强制区分告警事件和恢复事件进行压缩
					identifier.append(message.get(EventConstant.eventColumnEventSeverityType));
				}
				String identifierKey = identifier.toString();
				if (logger.isDebugEnabled()) {
					logger.debug("current identifier : " + identifierKey);
				}
					/**
					 * 每个条消息的Identifier由压缩字段组成，不再单独指定。
					 */
				message.put(EventConstant.eventColumnIdentifier, identifierKey);
				if (result.containsKey(identifierKey)) {
					Map<String, Object> newMessage = result.get(identifierKey);
					result.put(identifierKey, doCompress(rule.getName(),effects,message, newMessage));
				} else {
					result.put(identifierKey, message);
				}
			}
			rawMessages.clear();
			rawMessages.addAll(result.values());
		}
		// 2查库压缩（长压缩）,这里后期需要修改为只做压缩标记，不做压缩操作，所有的操作在DefaultAfterRuleHandler做
		//执行压缩时如果是级联过来的事件，不再执行恢复策略
		for (EventHandlerRule rule : rules) {
			//如果是级联来的信息，收到事件之后不再进行恢复规则处理。
			if(probeInfo.getEventSourceType()==EventSourceType.CASCADE&&(rule.getExecuteScope()==ExecuteScope.S||rule.getExecuteScope()==ExecuteScope.MS)) {
				continue;
			}
			boolean notExpired= DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
			if (!notExpired) {
				continue;
			}
			// 装配条件字段和结果字段
			String expressionStr = rule.getExpression();
			List<EventHandlerRuleExpression> expressions = FastJsonUtils.toList(expressionStr,EventHandlerRuleExpression.class);
			List<String> conditionColumns = new ArrayList<>();
			for (EventHandlerRuleExpression expression : expressions) {
				conditionColumns.add(expression.getConditionColumn());
			}
			if (CollectionUtils.isEmpty(conditionColumns))
				continue;
			List<EventHandlerRuleEffect> effects = FastJsonUtils.toList(rule.getEffect(), EventHandlerRuleEffect.class);
			String params = rule.getParams();
			if (StringUtils.isNotBlank(params)) {
				CompressParam compressParam = JSON.parseObject(params, CompressParam.class);
				String isLongCycleCompress = compressParam.getIsLongCycleCompress();
				String cycleTime = compressParam.getCycleTime();
				// 如果开启了长压缩
				if ("Y".equals(isLongCycleCompress)) {
					Iterator<Map<String, Object>> iterator = rawMessages.iterator();
					while (iterator.hasNext()) {
						Map<String, Object> message = iterator.next();
						if(ObjectUtils.isNotEmpty(message.get(EventConstant.eventColumnRefCompressRules))){
							continue;
						}
						// 1告警事件，2恢复事件，EventType=S时有效
						String eventSeverityType = String.valueOf(message.get(EventConstant.eventColumnEventSeverityType));
						// P不可恢复字段，S可恢复事件
						String eventType = String.valueOf(message.get(EventConstant.eventColumnEventType));
						// 可恢复的恢复事件
						int total = 0;
						if (("S".equals(eventType) && "2".equals(eventSeverityType))) {
							// 根据恢复策略以及当前告警查询是否存在未恢复的告警
							ResultPattern noRecoveryResult = EventCompressConfig.getNoRecoveryEvent(dataSource, projectID, recoveryRule,message);
							if (noRecoveryResult.isSuccess()) {
								total = Integer.parseInt(noRecoveryResult.getStrData());
							}
						}
						//查库压缩（长压缩）,这里后期需要修改为只做压缩标记，不做压缩操作，所有的操作在DefaultAfterRuleHandler做
						if (total == 0) {// 不存在未恢复的告警
							// 查询库中符合条件的事件
							List<Map<String, Object>> alarmEvents = EventCompressConfig.getAlarmEventByCondition(message, conditionColumns, cycleTime, dataSource, projectID);
							// 根据结果字段更新：最后发生时间、发生次数、描述的组合
							if (!CollectionUtils.isEmpty(alarmEvents)) {
								// boolean remove = rawMessages.remove(message);
								iterator.remove();
								for (Map<String, Object> alarmEvent : alarmEvents) {
									Map<Object, Object> filter = new HashMap<>();
									filter.put("EventID", alarmEvent.get("EventID"));
									filter.put("projectID", projectID);
									for (EventHandlerRuleEffect effect : effects) {
										String effectType = effect.getEffectType();
										String effectColumn = effect.getEffectColumn();
										// 累计
										if (effectType.equalsIgnoreCase(EventConstant.effctTypeCounter)) {
											String last = alarmEvent.get(effectColumn) == null ? "0": alarmEvent.get(effectColumn).toString();
											filter.put(effectColumn,new BigDecimal(last).add(new BigDecimal(message.get(effectColumn).toString())).intValue());
											continue;
										}
										// 最新
										if (effectType.equalsIgnoreCase(EventConstant.effectTypeNewest)) {
											filter.put(effectColumn, message.get(effectColumn));
											continue;
										}
									}
									//如果，没有压缩则跳过不更新，此处可以修改批量更新
									if(filter.containsKey(EventConstant.effectTally)||filter.containsKey(EventConstant.effectLastOccurrence)) {
										//数据库中告警命中的压缩策略与内存中告警命中的压缩策略进行拼接
										StringJoiner ruleJoiner = new StringJoiner("#");
										Object ruleFromDb = alarmEvent.get(EventConstant.eventColumnRefCompressRules);
										Object ruleFromMemory = message.get(EventConstant.eventColumnRefCompressRules);
										if (ObjectUtils.isNotEmpty(ruleFromDb)) {
											ruleJoiner.add(ruleFromDb + "");
										}
										if (ObjectUtils.isNotEmpty(ruleFromMemory)) {
											ruleJoiner.add(ruleFromMemory + "");
										}
										ruleJoiner.add(rule.getName());
										filter.put(EventConstant.eventColumnRefCompressRules, ruleJoiner.toString());
										EventCompressConfig.updateAlarmEventByCondition(filter, dataSource, projectID);
									}
								}
							}
						}
					}
				}
			}
		}
		return rawMessages;
	}
	/**
	 * 根据已经存在的压缩事件对新的事件进行压缩
	 * @param rawmessage
	 * @param compressedMessage
	 * @return 返回压缩过的事件
	 */
	private static Map<String, Object> doCompress(String ruleName,List<EventHandlerRuleEffect> effectRules,Map<String, Object> rawmessage,Map<String, Object> compressedMessage) {
		boolean flagHit=false;
		for (EventHandlerRuleEffect effect : effectRules) {
			if (effect.getEffectType().equalsIgnoreCase(EventConstant.effctTypeCounter)) {
				// 更新次数
				String last = compressedMessage.get(effect.getEffectColumn()) == null ? "0": compressedMessage.get(effect.getEffectColumn()).toString();
				if (compressedMessage.containsKey(effect.getEffectColumn())) {
					compressedMessage.remove(effect.getEffectColumn());
				}
				compressedMessage.put(effect.getEffectColumn(), new BigDecimal(last).add(new BigDecimal(rawmessage.get(effect.getEffectColumn()).toString())).intValue());
				flagHit=true;
				continue;
			}
			if (effect.getEffectType().equalsIgnoreCase(EventConstant.effectTypeNewest)) {
				// 更新最后发生日期
				if (compressedMessage.containsKey(effect.getEffectColumn())) {
					compressedMessage.remove(effect.getEffectColumn());
				}
				compressedMessage.put(effect.getEffectColumn(), rawmessage.get(effect.getEffectColumn()));
				flagHit=true;
				continue;
			}
		}
		if(flagHit) {
			compressedMessage.put("isCompress", true);
			/**
			 * 如果已经存在压缩规则，则追加
			 */
			Object oldRule=compressedMessage.get(EventConstant.eventColumnRefCompressRules);
			StringJoiner ruleJoiner = new StringJoiner("#");
			if (ObjectUtils.isNotEmpty(oldRule)) {
				ruleJoiner.add(oldRule.toString());
			}
			ruleJoiner.add(ruleName);
			compressedMessage.put(EventConstant.eventColumnRefCompressRules, ruleJoiner.toString());
		}
		return compressedMessage;
	}
}
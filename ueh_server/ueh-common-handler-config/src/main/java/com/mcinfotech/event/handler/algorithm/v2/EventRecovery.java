package com.mcinfotech.event.handler.algorithm.v2;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.util.CollectionUtils;

import com.mcinfotech.event.domain.EventConstant;
import com.mcinfotech.event.domain.EventSourceType;
import com.mcinfotech.event.domain.ExecuteScope;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.utils.DateTimeUtils;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;

/**
 * 事件恢复，如果是恢复事件，后续不再参与其他的策略，入库结束
 */
public class EventRecovery {
	
	/**
	 * @param rawMessages 原始消息
	 * @param rule        恢复规则
	 * @param dataSource  数据源
	 * @param projectInfo 项目信息
	 * @return 过滤过的事件
	 */
	public static Collection<Map<String, Object>> excute(ProbeInfo probeInfo,Collection<Map<String, Object>> rawMessages,EventHandlerRule rule, DataSource dataSource, ProjectInfo projectInfo) {
		// todo 恢复动作一个线程 后续处理一个线程
		if (CollectionUtils.isEmpty(rawMessages))
			return null;
		if (rule == null)
			return rawMessages;
		//如果是级联来的事件，不需要做恢复处理
		if(probeInfo.getEventSourceType()==EventSourceType.CASCADE) {
			return rawMessages;
		}
		// 2.恢复条件重新封装到list
		boolean notExpired = DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(),rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
		if (!notExpired) {
			return rawMessages;
		}
		String expressionStr = rule.getExpression();
		List<EventHandlerRuleExpression> expressions = FastJsonUtils.toList(expressionStr,EventHandlerRuleExpression.class);

		// 1.筛选恢复事件
		List<Map<String, Object>> reMessages = new ArrayList<>();
		Iterator<Map<String, Object>> iterator = rawMessages.iterator();
		while (iterator.hasNext()) {
			Map<String, Object> rawMessage = iterator.next();
			String eventType = String.valueOf(rawMessage.get(EventConstant.eventColumnEventType));
			String eventSeverityType = String.valueOf(rawMessage.get(EventConstant.eventColumnEventSeverityType));
			// 可恢复事件:EventType(P不可恢复字段，S可恢复事件) EventSeverityType(1告警事件，2恢复事件，EventType=S时有效)
			if ("S".equalsIgnoreCase(eventType) && "2".equals(eventSeverityType)) {
				// 状态标记为恢复
				rawMessage.put(EventConstant.eventColumnRecoveredStatus, 2);
				// 状态标记为已完成
				rawMessage.put(EventConstant.eventColumnAcknowledged, 1);
				rawMessage.put(EventConstant.eventColumnOperator, EventConstant.defaultOperator);
				rawMessage.put(EventConstant.eventColumnOperateTimestamp,LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
				rawMessage.put(EventConstant.eventColumnOperateState, 1);
				reMessages.add(rawMessage);
				// iterator.remove();
			}
		}

		/*
		 * 3.逐条更新事件信息 可恢复事件：实时表删除已恢复事件、历史表更新恢复事件状态
		 */
		if (reMessages.size() > 0) {

			recoveryInMemory(rawMessages, expressions, reMessages, rule.getName());

			recoveryInDB(dataSource, projectInfo, expressions, reMessages, rule.getName());
		}

		return rawMessages;
	}

	private static void recoveryInDB(DataSource dataSource, ProjectInfo projectInfo,
			List<EventHandlerRuleExpression> expressions, List<Map<String, Object>> reMessages, String name) {
		for (Map<String, Object> reVoMessage : reMessages) {
			// 3.1 前端传过来的动态条件 where
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
			// 3.2 固定更新字段 update
			filter.put("lastOccurrence", String.valueOf(reVoMessage.get(EventConstant.eventColumnLastOccurrence)));
			filter.put("deleteTime", String.valueOf(reVoMessage.get(EventConstant.eventColumnLastOccurrence)));
			filter.put("recoveredEeventID", String.valueOf(reVoMessage.get(EventConstant.eventColumnEventID)));
			filter.put("recoveredStatus", 2);
			filter.put(EventConstant.eventColumnAcknowledged, 1);
			filter.put(EventConstant.eventColumnOperator, EventConstant.defaultOperator);
			filter.put(EventConstant.eventColumnOperateTimestamp,LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
			filter.put(EventConstant.eventColumnOperateState, 1);
			filter.put(EventConstant.eventColumnRefRecoveryRules, name);
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

	private static void recoveryInMemory(Collection<Map<String, Object>> rawMessages,List<EventHandlerRuleExpression> expressions, List<Map<String, Object>> reMessages, String name) {
		for (Map<String, Object> rawMessage : rawMessages) {
			String eventType = String.valueOf(rawMessage.get(EventConstant.eventColumnEventType));
			String eventSeverityType = String.valueOf(rawMessage.get(EventConstant.eventColumnEventSeverityType));
			String firstOccurrence = String.valueOf(rawMessage.get(EventConstant.eventColumnFirstOccurrence));
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
						if (Long.parseLong(String.valueOf(reMessage.get(EventConstant.eventColumnFirstOccurrence))) > Long.parseLong(firstOccurrence)) {
							rawMessage.put(EventConstant.eventColumnLastOccurrence,Long.parseLong(String.valueOf(reMessage.get(EventConstant.eventColumnLastOccurrence))));
							rawMessage.put(EventConstant.eventColumnDeleteTime,Long.parseLong(String.valueOf(reMessage.get(EventConstant.eventColumnLastOccurrence))));
							rawMessage.put(EventConstant.eventColumnRecoveredEeventID, String.valueOf(reMessage.get(EventConstant.eventColumnEventID)));
							rawMessage.put(EventConstant.eventColumnRecoveredStatus, 2);
							// 状态标记为已完成
							rawMessage.put(EventConstant.eventColumnAcknowledged, 1);
							rawMessage.put(EventConstant.eventColumnOperator, EventConstant.defaultOperator);
							rawMessage.put(EventConstant.eventColumnOperateTimestamp,LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
							rawMessage.put(EventConstant.eventColumnOperateState, 1);
							rawMessage.put(EventConstant.eventColumnRefRecoveryRules, name);
						}
					}
				}
			}
		}
	}
}

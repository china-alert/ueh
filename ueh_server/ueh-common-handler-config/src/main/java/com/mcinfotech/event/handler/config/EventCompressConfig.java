package com.mcinfotech.event.handler.config;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;

import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleExpression;
import com.mcinfotech.event.utils.FastJsonUtils;
import com.sun.media.jfxmedia.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventCompressConfig {

	public static List<Map<String, Object>> getAlarmEventByCondition(Map<String, Object> message,
			List<String> conditionColumns, String cycleTime, DataSource dataSource, long projectID) {
		LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(Long.valueOf(cycleTime));
		long l = localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
		Map<Object, Object> filter = new HashMap<>();
		for (String columnName : conditionColumns) {
			Object columnValue = message.get(columnName);
			filter.put(columnName, columnValue);
		}
		if ("S".equalsIgnoreCase(String.valueOf(message.get("EventType")))) {
			filter.put("EventSeverityType", message.get("EventSeverityType"));
		}
		filter.put("cycleTime", l);
		DataLoadParams params = new DataLoadParams();
//		params.setProjectId(Long.valueOf((String) message.get("projectId")));
		params.setProjectId(projectID);
		params.setDcName("getAlarmEventByCondition");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		ResultPattern resultPattern = DataServiceUtils.dataLoad(dataSource, params);

		return resultPattern.getDatas();
	}

	public static ResultPattern getNoRecoveryEvent(DataSource dataSource, long projectID,EventHandlerRule recoveryRule, Map<String, Object> message) {
		if(recoveryRule==null)return new ResultPattern();
		
		String expressionRecoveryStr = recoveryRule.getExpression();
		List<EventHandlerRuleExpression> expressionsRecovery = FastJsonUtils.toList(expressionRecoveryStr,EventHandlerRuleExpression.class);
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

	public static void updateAlarmEventByCondition(Map<Object, Object> filter, DataSource dataSource, long projectID) {
		DataLoadParams params = new DataLoadParams();
		params.setProjectId(projectID);
		params.setDcName("updateAlarmEventByCondition");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		DataServiceUtils.dataLoad(dataSource, params);
	}
}

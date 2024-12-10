package com.mcinfotech.event.handler.algorithm.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.domain.EventSourceType;
import com.mcinfotech.event.domain.ExecuteScope;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleEffect;
import com.mcinfotech.event.handler.domain.Expression;
import com.mcinfotech.event.handler.domain.RichResultColumn;
import com.mcinfotech.event.utils.DateTimeUtils;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;

/**
 * 事件丰富 针对符合条件的告警进行丰富：映射或者关联
 *

 */
public class EventRich {
	private static Logger logger = LogManager.getLogger();
	
	/**
	 * 丰富来源字段为单个的版本执行的方法 丰富规则可以有多条，多条时按优先级进行，优先级的策略高的策略被最后执行
	 * 丰富策略在
	 *
	 * @param rawMessages
	 * @param columnDefineMap
	 * @param richRules
	 * @param dataSource
	 * @param projectID
	 * @return
	 */
	public static Collection<Map<String, Object>> excute(ProbeInfo probeInfo,Collection<Map<String, Object>> rawMessages,List<EventHandlerRule> richRules, DataSource dataSource, long projectID) {
		if (CollectionUtils.isEmpty(rawMessages))
			return null;
		if (CollectionUtils.isEmpty(richRules))
			return rawMessages;
		try {
			for (Map<String, Object> rawMessage : rawMessages) {
				StringJoiner ruleJoiner = new StringJoiner("#");
				for (EventHandlerRule rule : richRules) {
					boolean notExpired = DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(),rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
					if (!notExpired) {
						continue;
					}
					//如果是级联来的信息，收到事件之后不再进行规则处理。
					if(probeInfo.getEventSourceType()==EventSourceType.CASCADE&&(rule.getExecuteScope()==ExecuteScope.S||rule.getExecuteScope()==ExecuteScope.MS)) {
						continue;
					}
					boolean success;
					String expressionStr = rule.getExpression();
					if (StringUtils.isBlank(expressionStr)) {
						logger.warn("条件为空{}", expressionStr);
						continue;
					}
					List<Expression> expressions = JSON.parseArray(expressionStr, Expression.class);
					String tableName = expressions.get(0).getTableName();
					if (StringUtils.equals("t_res_attribute_set", tableName)) {
						success = horizontalRich(dataSource, projectID, rawMessage, rule);
					} else {
						success = verticalRich(dataSource, projectID, rawMessage, rule);
					}
					if (success) {
						ruleJoiner.add(rule.getName());
					}
				}
				rawMessage.put("RefRichRules", ruleJoiner.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rawMessages;
	}

	private static boolean verticalRich(DataSource dataSource, long projectID, Map<String, Object> rawMessage,
			EventHandlerRule richRule) {
		String expressionStr = richRule.getExpression();
		if (StringUtils.isBlank(expressionStr)) {
			logger.info("条件为空{}", expressionStr);
			// continue;
			return Boolean.FALSE;
		}
		List<Expression> expressions = JSON.parseArray(expressionStr, Expression.class);
		String tableName = expressions.get(0).getTableName();
		// 丰富来源
		String result = expressions.get(0).getResult();
		// 获取丰富来源字段来源id
		ResultPattern res = getVerticalDataId(dataSource, projectID, rawMessage, expressions, tableName);
		List<Map<String, Object>> tableIdList = res.getDatas();
		if (res.isSuccess() && tableIdList.size() == 1) {
			Object dataId = tableIdList.get(0).get("dataId");
			// 拼接丰富来源字段
			if (StringUtils.isBlank(result)) {
				logger.warn("丰富来源字段为空{}", result);
				// continue;
				return Boolean.FALSE;
			}
			List<RichResultColumn> richResultColumns = JSON.parseArray(result, RichResultColumn.class);
			if (res.isSuccess() && res.getDatas().size() > 0) {
				String sourceRichResult = getVerticalSourceRichResult(dataSource, projectID, dataId, tableName,
						richResultColumns);
				// 生效
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
			// break;
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	private static boolean horizontalRich(DataSource dataSource, long projectID, Map<String, Object> rawMessage,EventHandlerRule richRule) {

		String expressionStr = richRule.getExpression();
		if (StringUtils.isBlank(expressionStr)) {
			logger.warn("条件为空{}", expressionStr);
			// continue;
			return Boolean.FALSE;
		}
		List<Expression> expressions = JSON.parseArray(expressionStr, Expression.class);
		String tableName = expressions.get(0).getTableName();
		String result = expressions.get(0).getResult();
		// 获取丰富来源字段来源id
		ResultPattern res = getDataId(dataSource, projectID, rawMessage, expressions, tableName);
		List<Map<String, Object>> tableIdList = res.getDatas();
		if (res.isSuccess() && tableIdList.size() == 1) {
			Object dataId = tableIdList.get(0).get("dataId");
			// 拼接丰富来源字段
			if (StringUtils.isBlank(result)) {
				logger.warn("丰富来源字段为空{}", result);
				// continue;
				return Boolean.FALSE;
			}
			List<RichResultColumn> richResultColumns = JSON.parseArray(result, RichResultColumn.class);
			if (res.isSuccess() && res.getDatas().size() > 0) {
				String sourceRichResult = getSourceRichResult(dataSource, projectID, dataId, richResultColumns);
				// 生效
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
			// break;
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private static String getSourceRichResult(DataSource dataSource, long projectID, Object dataId,
			List<RichResultColumn> richResultColumns) {
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

	private static String getVerticalSourceRichResult(DataSource dataSource, long projectID, Object dataId,
			String tableName, List<RichResultColumn> richResultColumns) {
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

	private static ResultPattern getDataId(DataSource dataSource, long projectID, Map<String, Object> rawMessage,
			List<Expression> expressions, String tableName) {
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
			map.put("columnValue",columnValue);
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

	private static ResultPattern getVerticalDataId(DataSource dataSource, long projectID,
			Map<String, Object> rawMessage, List<Expression> expressions, String tableName) {
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
}

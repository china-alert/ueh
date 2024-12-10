package com.mcinfotech.event.handler.domain;

import com.mcinfotech.event.domain.BaseBean;
import com.mcinfotech.event.domain.EventSourceType;
import com.mcinfotech.event.domain.ExecuteScope;

/**
 * 事件处理策略

 *
 */
public class EventHandlerRule extends BaseBean{
	/**
	 * 策略名称
	 */
	private String name;
	/**
	 * 执行类型：生效类型，O单次，R重复
	 */
	private String execType;
	/**
	 * 重复类型，day每天，week每周，month月，year年
	 */
	private String intervalType;
	/**
	 * 从周几开始,适用于重复执行
	 */
	private String dayOfWeekAt;
	/**
	 * 到周几结束，适用于重复执行
	 */
	private String dayOfWeekUtil;
	/**
	 * 从什么时间开始，适用于单次
	 */
	private String executeAt;
	/**
	 * 从什么时间结束，适用于单次
	 */
	private String executeUtil;
	/**
	 * 过期保留，Y保留，N不保留
	 */
	private String onCompletion;
	/**
	 * 规则类型：N通用，C合并，R丰富，U升级，D降级，Z:压缩,F过滤，G分组
	 */
	private String ruleType;
	/**
	 * 事件源，ProbeKey
	 */
	private String eventSource;
	/**
	 * 策略：(A or B) and (C or D),A、B、C、D为字段的运算结果,用字段名称作为公式的参数
	 */
	private String formular;
	/**
	 * 用来存储每个字段构成的表达式，比如severity>3
	 * 对压缩来讲只有字段，没有操作符和值
	 */
	private String expression;
	/**
	 * 生效字段
	 */
	private String effect;
	private EventSourceType eventSourceType;
	/**
	 * 其他参数定义，存储每个策略独特的参数
	 */
	private String params;

	/**
	 * 策略执行状态
	 *0 未执行 1 执行中  2 执行完
	 * @return
	 */
	private Integer status;
	/**
	 * 策略执行范围，主、从、主从
	 */
	private ExecuteScope executeScope;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExecType() {
		return execType;
	}
	public void setExecType(String execType) {
		this.execType = execType;
	}
	public String getIntervalType() {
		return intervalType;
	}
	public void setIntervalType(String intervalType) {
		this.intervalType = intervalType;
	}
	public String getDayOfWeekAt() {
		return dayOfWeekAt;
	}
	public void setDayOfWeekAt(String dayOfWeekAt) {
		this.dayOfWeekAt = dayOfWeekAt;
	}
	public String getDayOfWeekUtil() {
		return dayOfWeekUtil;
	}
	public void setDayOfWeekUtil(String dayOfWeekUtil) {
		this.dayOfWeekUtil = dayOfWeekUtil;
	}
	public String getExecuteAt() {
		return executeAt;
	}
	public void setExecuteAt(String executeAt) {
		this.executeAt = executeAt;
	}
	public String getExecuteUtil() {
		return executeUtil;
	}
	public void setExecuteUtil(String executeUtil) {
		this.executeUtil = executeUtil;
	}
	public String getOnCompletion() {
		return onCompletion;
	}
	public void setOnCompletion(String onCompletion) {
		this.onCompletion = onCompletion;
	}
	public String getRuleType() {
		return ruleType;
	}
	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}
	public String getEventSource() {
		return eventSource;
	}
	public void setEventSource(String eventSource) {
		this.eventSource = eventSource;
	}
	public String getFormular() {
		return formular;
	}
	public void setFormular(String formular) {
		this.formular = formular;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public String getEffect() {
		return effect;
	}
	public void setEffect(String effect) {
		this.effect = effect;
	}
	public EventSourceType getEventSourceType() {
		return eventSourceType;
	}
	public void setEventSourceType(EventSourceType eventSourceType) {
		this.eventSourceType = eventSourceType;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public ExecuteScope getExecuteScope() {
		return executeScope;
	}
	
	public void setExecuteScope(ExecuteScope executeScope) {
		this.executeScope = executeScope;
	}
	
	@Override
	public String toString() {
		return "EventHandlerRule [name=" + name + ", execType=" + execType + ", intervalType=" + intervalType
				+ ", dayOfWeekAt=" + dayOfWeekAt + ", dayOfWeekUtil=" + dayOfWeekUtil + ", executeAt=" + executeAt
				+ ", executeUtil=" + executeUtil + ", onCompletion=" + onCompletion + ", ruleType=" + ruleType
				+ ", eventSource=" + eventSource + ", formular=" + formular + ", expression=" + expression + ", effect="
				+ effect + ", eventSourceType=" + eventSourceType + ", params=" + params + ", status=" + status
				+ ", executeScope=" + executeScope + "]";
	}
}

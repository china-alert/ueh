package com.mcinfotech.event.handler.domain;

import com.mcinfotech.event.domain.BaseBean;

public class EventHandlerTemplet extends BaseBean{
	/**
	 * 模板名称
	 */
	private String name;
	/**
	 * 表达式
	 */
	private String expression;
	/**
	 * 表达式类型：R正则，K关键字
	 */
	private String expressionType;
	/**
	 * 用途：与EventHandlerRules.java中ruleType策略类型一致，包括：G通用，C合并，R丰富，U升降级
	 */
	private String usageType;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public String getExpressionType() {
		return expressionType;
	}
	public void setExpressionType(String expressionType) {
		this.expressionType = expressionType;
	}
	public String getUsageType() {
		return usageType;
	}
	public void setUsageType(String usageType) {
		this.usageType = usageType;
	}
}

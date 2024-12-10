package com.mcinfotech.event.handler.domain;

/**
 * 事件处理策略表达式

 *
 */
public class EventHandlerRuleExpression{
	/**
	 * 事件平台字段名称
	 */
	private String conditionColumn;
	/**
	 * 事件平台字段类型
	 */
	private String conditionDataType;
	/**
	 * 操作符，根据字段类型不一样有不同的操作符选项
	 */
	private String operator;
	/**
	 * 规则，对字段值使用的公式、算法等。
	 * 如果是规则模板则是规则模板的类型
	 */
	private String conditionValue;
	/**
	 * 与其他条件的逻辑运算符号：&&、||、XOR
	 */
	private String operatorLogic;
	
	public String getConditionColumn() {
		return conditionColumn;
	}
	public void setConditionColumn(String conditionColumn) {
		this.conditionColumn = conditionColumn;
	}
	public String getConditionDataType() {
		return conditionDataType;
	}
	public void setConditionDataType(String conditionDataType) {
		this.conditionDataType = conditionDataType;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String opertor) {
		this.operator = opertor;
	}
	public String getConditionValue() {
		return conditionValue;
	}
	public void setConditionValue(String conditionValue) {
		this.conditionValue = conditionValue;
	}
	public String getOperatorLogic() {
		return operatorLogic;
	}
	public void setOperatorLogic(String operatorLogic) {
		this.operatorLogic = operatorLogic;
	}
}

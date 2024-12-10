package com.mcinfotech.event.handler.domain;

/**
 * 事件处理策略结果表达式，适用于丰富

 *
 */
public class EventHandlerRuleRichMapping{
	/**
	 * 原始字段名称
	 */
	private String sourceColumn;
	/**
	 * 原始字段类型
	 */
	private String sourceDataType;
	/**
	 * 运算符
	 */
	private String operator;
	/**
	 * 原始值
	 */
	private String sourceValue;
	/**
	 * 生效字段名称
	 */
	private String effectColumn;
	/**
	 * 生效字段数据类型
	 */
	private String effectDataType;
	/**
	 * 生效方式：counter计数，newest最新,fill填充，minus降级，plus升级
	 */
	private String effectType;
	/**
	 * 生效值，effectType为填充时则填充值
	 */
	private String effectValue;
	
	public String getEffectColumn() {
		return effectColumn;
	}
	public void setEffectColumn(String effectColumn) {
		this.effectColumn = effectColumn;
	}
	public String getEffectDataType() {
		return effectDataType;
	}
	public void setEffectDataType(String effectDataType) {
		this.effectDataType = effectDataType;
	}
	public String getEffectType() {
		return effectType;
	}
	public void setEffectType(String effectType) {
		this.effectType = effectType;
	}
	public String getEffectValue() {
		return effectValue;
	}
	public void setEffectValue(String effectValue) {
		this.effectValue = effectValue;
	}
	
	public String getSourceColumn() {
		return sourceColumn;
	}
	public void setSourceColumn(String sourceColumn) {
		this.sourceColumn = sourceColumn;
	}
	public String getSourceDataType() {
		return sourceDataType;
	}
	public void setSourceDataType(String sourceDataType) {
		this.sourceDataType = sourceDataType;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getSourceValue() {
		return sourceValue;
	}
	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
	}
	
	@Override
	public String toString() {
		return "EventHandlerRuleRichMapping [sourceColumn=" + sourceColumn + ", sourceDataType=" + sourceDataType
				+ ", operator=" + operator + ", sourceValue=" + sourceValue + ", effectColumn=" + effectColumn
				+ ", effectDataType=" + effectDataType + ", effectType=" + effectType + ", effectValue=" + effectValue
				+ "]";
	}
}

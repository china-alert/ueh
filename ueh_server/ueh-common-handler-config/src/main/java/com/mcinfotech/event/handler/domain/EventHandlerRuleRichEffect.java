package com.mcinfotech.event.handler.domain;

import java.util.List;

/**
 * 事件处理策略结果表达式，适用于丰富

 *
 */
public class EventHandlerRuleRichEffect{
	/**
	 * 丰富类型，mapping，relation
	 */
	private String richType;
	/**
	 * 待丰富字段
	 */
	private String richColumn;
	/**
	 * 关联数据库表，目前与事件表在同一个库或者同一个Schema中
	 */
	private String relationTable;
	/**
	 * 关联数据库表的对应的字段
	 */
	private String relationColumn;
	/**
	 * 字段映射关系或关联关系
	 */
	List<EventHandlerRuleRichMapping> valueMapping;

	public String getRichType() {
		return richType;
	}

	public void setRichType(String richType) {
		this.richType = richType;
	}

	public String getRichColumn() {
		return richColumn;
	}

	public void setRichColumn(String richColumn) {
		this.richColumn = richColumn;
	}

	public String getRelationTable() {
		return relationTable;
	}

	public void setRelationTable(String relationTable) {
		this.relationTable = relationTable;
	}

	public String getRelationColumn() {
		return relationColumn;
	}

	public void setRelationColumn(String relationColumn) {
		this.relationColumn = relationColumn;
	}

	public List<EventHandlerRuleRichMapping> getValueMapping() {
		return valueMapping;
	}

	public void setValueMapping(List<EventHandlerRuleRichMapping> valueMapping) {
		this.valueMapping = valueMapping;
	}

	@Override
	public String toString() {
		return "EventHandlerRuleRichEffect [richType=" + richType + ", richColumn=" + richColumn + ", relationTable="
				+ relationTable + ", relationColumn=" + relationColumn + ", valueMapping=" + valueMapping + "]";
	}
}

package com.mcinfotech.event.handler.domain;

import java.util.List;
/**
 * Probe接入时事件平台与事件源级别映射

 *
 */
public class PlatformProbeSeverityMapping {
	/**
	 * 事件源级别字段名称
	 */
	private String eventSourceSeverityColumn;
	/**
	 * 事件平台与事件源级别值映射关系
	 */
	private List<PlatformEventSourceSeverityValueMapping> valueMapping;
	public String getEventSourceSeverityColumn() {
		return eventSourceSeverityColumn;
	}
	public void setEventSourceSeverityColumn(String eventSourceSeverityColumn) {
		this.eventSourceSeverityColumn = eventSourceSeverityColumn;
	}
	public List<PlatformEventSourceSeverityValueMapping> getValueMapping() {
		return valueMapping;
	}
	public void setValueMapping(List<PlatformEventSourceSeverityValueMapping> valueMapping) {
		this.valueMapping = valueMapping;
	}
	@Override
	public String toString() {
		return "PlatformProbeSeverityMapping [eventSourceSeverityColumn=" + eventSourceSeverityColumn
				+ ", valueMapping=" + valueMapping + "]";
	}
}

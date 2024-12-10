package com.mcinfotech.event.handler.domain;

/**
 * 事件平台与事件源级别映射关系

 *
 */
public class PlatformEventSourceSeverityValueMapping {
	/**
	 * 事件平台级别值
	 */
	private String platformSeverity;
	/**
	 * 事件源级别值，多个值以逗号隔开
	 */
	private String sourceSeverity;
	/**
	 * 事件平台级别值与事件源匹配关系
	 * = 等于
	 * IN 在里面
	 */
	private String operator;
	
	public String getPlatformSeverity() {
		return platformSeverity;
	}
	public void setPlatformSeverity(String platformSeverity) {
		this.platformSeverity = platformSeverity;
	}
	public String getSourceSeverity() {
		return sourceSeverity;
	}
	public void setSourceSeverity(String sourceSeverity) {
		this.sourceSeverity = sourceSeverity;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	@Override
	public String toString() {
		return "PlatformEventSourceSeverityValueMapping [platformSeverity=" + platformSeverity + ", sourceSeverity="
				+ sourceSeverity + ", operator=" + operator + "]";
	}
	
}

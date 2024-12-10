package com.mcinfotech.event.handler.domain;

/**
 * Probe接入时事件平台与事件源字段映射

 *
 */
public class PlatformColumnMapping {
	/**
	 * 事件平台字段名称
	 */
	private String platformColumn;
	/**
	 * LONGTIMESTAMP、STRING、INT、FLOAT,分别为时间戳、字符串、整型、非整型
	 */
	private String platformDataType;
	/**
	 * 事件平台与事件源映射关系,
	 * 单一：一对一
	 * 组合：多个事件源字段组成一个，也可以添加常量，
	 * 组合格式：${字段1}${字段2}或者${字段1}常量${字段2}
	 */
	private String mappingType;
	/**
	 * 事件源字段名称
	 */
	private String sourceColumn;
	/**
	 * 事件源字段类型
	 */
	private String sourceDataType;
	/**
	 * 如果事件源不存在对应的字段，事件平台中又是个常量值，可以将常量值通过sourceValue设置
	 * 该字段与sourceColumn同时设置，SourceColumn优先级高
	 */
	private String sourceValue;
	
	public String getPlatformColumn() {
		return platformColumn;
	}
	public void setPlatformColumn(String platformColumn) {
		this.platformColumn = platformColumn;
	}
	public String getPlatformDataType() {
		return platformDataType;
	}
	public void setPlatformDataType(String platformDataType) {
		this.platformDataType = platformDataType;
	}
	public String getMappingType() {
		return mappingType;
	}
	public void setMappingType(String mappingType) {
		this.mappingType = mappingType;
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
	public String getSourceValue() {
		return sourceValue;
	}
	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
	}
	@Override
	public String toString() {
		return "PlatformColumnMapping [platformColumn=" + platformColumn + ", platformDataType=" + platformDataType
				+ ", mappingType=" + mappingType + ", sourceColumn=" + sourceColumn + ", sourceDataType="
				+ sourceDataType + ", sourceValue=" + sourceValue + "]";
	}
}

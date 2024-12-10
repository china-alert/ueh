package com.mcinfotech.event.handler.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.mcinfotech.event.domain.BaseBean;

public class PlatformEventColumn extends BaseBean{
	public final static String FIXED_COLUMN_FILTER_FLAG="FilterFlag";
	public final static String FIXED_COLUMN_EVENT_CATEOGORY="EventCategory";
	public final static String FIXED_COLUMN_DELIVERY_FLAG="DeliveryFlag";
	public final static String FIXED_COLUMN_ACKNOWLEDGE="Acknowledge";
	public final static String FIXED_COLUMN_SEVERITY="Severity";
	
	/**
	 * 平台字段名称
	 */
	private String columnName;
	/**
	 * 平台字段类型，包括int,string,date
	 */
	private String dataType;
	/**
	 * 在数据库中字段名称
	 */
	private String columnInDB;
	/**
	 * 字段长度
	 */
	private String dataLength;
	/**
	 * 是否为空
	 */
	private String isNotNull;
	
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getDataLength() {
		return dataLength;
	}
	public void setDataLength(String dataLength) {
		this.dataLength = dataLength;
	}
	public String getIsNotNull() {
		return isNotNull;
	}
	public void setIsNotNull(String isNotNull) {
		this.isNotNull = isNotNull;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnInDB() {
		return columnInDB;
	}
	public void setColumnInDB(String columnInDB) {
		this.columnInDB = columnInDB;
	}
	@Override
	public String toString() {
		return "PlatformEventColumn [columnName=" + columnName + ", dataType=" + dataType + ", columnInDB=" + columnInDB
				+ ", dataLength=" + dataLength + ", isNotNull=" + isNotNull + "]";
	}
	public static  Map<String,PlatformEventColumn> toMap(List<PlatformEventColumn> rawColumns){
		if(CollectionUtils.isEmpty(rawColumns))return null;
		Map<String,PlatformEventColumn> result=new HashMap<String,PlatformEventColumn>();
		for(PlatformEventColumn raw:rawColumns){
			result.put(raw.getColumnInDB(), raw);
		}
		return result;
	}
}

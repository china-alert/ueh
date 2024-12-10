package com.mcinfotech.event.handler.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Probe接入时事件平台与事件源字段映射

 *
 */
public class PlatformProbeColumnMapping {
	
	private String syslogType;
	private String syslogSeparatorChar;
	
	List<PlatformColumnMapping> columnMapping;
	
	public String getSyslogType() {
		return syslogType;
	}

	public void setSyslogType(String syslogType) {
		this.syslogType = syslogType;
	}

	public String getSyslogSeparatorChar() {
		return syslogSeparatorChar;
	}

	public void setSyslogSeparatorChar(String syslogSeparatorChar) {
		this.syslogSeparatorChar = syslogSeparatorChar;
	}

	public List<PlatformColumnMapping> getColumnMapping() {
		return columnMapping;
	}

	public void setColumnMapping(List<PlatformColumnMapping> columnMapping) {
		this.columnMapping = columnMapping;
	}

	@Override
	public String toString() {
		return "PlatformProbeColumnMapping [syslogType=" + syslogType + ", syslogSeparatorChar=" + syslogSeparatorChar
				+ ", columnMapping=" + columnMapping + "]";
	}

	public static  Map<String,PlatformColumnMapping> toMap(List<PlatformColumnMapping> rawColumnMappings){
		if(CollectionUtils.isEmpty(rawColumnMappings))return null;
		Map<String,PlatformColumnMapping> result=new HashMap<String,PlatformColumnMapping>();
		for(PlatformColumnMapping raw:rawColumnMappings){
			result.put(raw.getPlatformColumn(), raw);
		}
		return result;
	}
}

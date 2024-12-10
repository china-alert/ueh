package com.mcinfotech.event.handler.domain;

import com.mcinfotech.event.domain.BaseBean;

public class PlatformEventSeverity extends BaseBean{
	/**
	 * 级别名称
	 */
	private String name;
	/**
	 * 级别取值
	 */
	private int value;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "PlatformEventSeverity [name=" + name + ", value=" + value + ", getId()=" + getId() + ", getIsEnable()="
				+ getIsEnable() + ", getRemark()=" + getRemark() + ", getProjectId()=" + getProjectId()
				+ ", getLmTimestamp()=" + getLmTimestamp() + ", getDisplayOrder()=" + getDisplayOrder() + "]";
	}
	
}

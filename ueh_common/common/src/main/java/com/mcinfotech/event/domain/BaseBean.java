package com.mcinfotech.event.domain;

import java.sql.Timestamp;

public class BaseBean {
	private int id;
	private String isEnable;
	private String remark;
	private int projectId;
	private Timestamp lmTimestamp;
	private int displayOrder;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIsEnable() {
		return isEnable;
	}
	public void setIsEnable(String isEnable) {
		this.isEnable = isEnable;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getProjectId() {
		return projectId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public Timestamp getLmTimestamp() {
		return lmTimestamp;
	}
	public void setLmTimestamp(Timestamp lmTimestamp) {
		this.lmTimestamp = lmTimestamp;
	}
	public int getDisplayOrder() {
		return displayOrder;
	}
	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
	
}

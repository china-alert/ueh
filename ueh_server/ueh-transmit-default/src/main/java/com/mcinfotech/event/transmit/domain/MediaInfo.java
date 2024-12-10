package com.mcinfotech.event.transmit.domain;

/**
 * 通知方式定义：邮件、电话、短信、微信、钉钉等
 *

 */
public class MediaInfo {
	// 通知类型：email、phone_number、Wechat、Nail
	private MediaType mediaType;
	// 通知配置
	private String mediaSettings;
	// 通知内容模板
	private String mediaTemplet;
	// 模板参数
	private String paramNames;
	private long projectId;
	private String remark;
	private String isEnable;
	private String id;
	private String name;


	public String getParamNames() {
		return paramNames;
	}

	public void setParamNames(String paramNames) {
		this.paramNames = paramNames;
	}

	public String getMediaSettings() {
		return mediaSettings;
	}

	public void setMediaSettings(String mediaSettings) {
		this.mediaSettings = mediaSettings;
	}

	public String getMediaTemplet() {
		return mediaTemplet;
	}

	public void setMediaTemplet(String mediaTemplet) {
		this.mediaTemplet = mediaTemplet;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getIsEnable() {
		return isEnable;
	}

	public void setIsEnable(String isEnable) {
		this.isEnable = isEnable;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

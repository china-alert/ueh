package com.mcinfotech.event.domain;

/**
 * 心跳消息定义

 *
 */
public class HeartBeatMessage {
	private String occurencedTime;
	private MessageType messageType;
	private String description;
	
	public HeartBeatMessage(String occurencedTime,MessageType messageType,String description){
		this.occurencedTime=occurencedTime;
		this.messageType=messageType;
		this.description=description;
	}

	public String getOccurencedTime() {
		return occurencedTime;
	}


	public void setOccurencedTime(String occurencedTime) {
		this.occurencedTime = occurencedTime;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
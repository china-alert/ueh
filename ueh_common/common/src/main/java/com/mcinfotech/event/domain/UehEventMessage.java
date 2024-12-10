package com.mcinfotech.event.domain;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 用来在网关与Handler之间传递消息

 *
 */
public class UehEventMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9161510341892106402L;
	
	private String header="UEH";
	private String status;
	private UUID transactionId;
	private ProbeInfo probe;
	private String reserved;
	/**
	 * 项目信息
	 */
	private ProjectInfo project;
	private int messageLenth;
	private ProtocolVersion protocolVersion=ProtocolVersion.V2;
	
	private List<String> messages;
	
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public UUID getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(UUID transactionId) {
		this.transactionId = transactionId;
	}

	public ProbeInfo getProbe() {
		return probe;
	}

	public void setProbe(ProbeInfo probe) {
		this.probe = probe;
	}

	public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	public ProjectInfo getProject() {
		return project;
	}

	public void setProject(ProjectInfo project) {
		this.project = project;
	}

	public int getMessageLenth() {
		return messageLenth;
	}

	public void setMessageLenth(int messageLenth) {
		this.messageLenth = messageLenth;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public String toSimpleString() {
		return "UehEventMessage [header=" + header + ", status=" + status + ", transactionId=" + transactionId
				+ ", probe=" + probe + ", reserved=" + reserved + ", project=" + project + ", messageLenth="
				+ messageLenth + ", protocolVersion=" + protocolVersion+"]";
	}

	@Override
	public String toString() {
		return "UehEventMessage [header=" + header + ", status=" + status + ", transactionId=" + transactionId
				+ ", probe=" + probe + ", reserved=" + reserved + ", project=" + project + ", messageLenth="
				+ messageLenth + ", protocolVersion=" + protocolVersion + ", messages=" + messages + "]";
	}
}

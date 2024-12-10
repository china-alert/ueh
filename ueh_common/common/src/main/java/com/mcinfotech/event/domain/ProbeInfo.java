package com.mcinfotech.event.domain;

import java.io.Serializable;

/**
 * Probe信息描述

 *
 */
public class ProbeInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4837101551168035078L;
	private String name;
	private String version;
	private ProbeType type;
	private EventSourceType eventSourceType;
	private String key;
	private int port;
	private int heartbeat;
	private int timeout;
	private int threadCount;
	private ExecuteScope scope=ExecuteScope.M;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public ProbeType getType() {
		return type;
	}
	public void setType(ProbeType type) {
		this.type = type;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getHeartbeat() {
		return heartbeat;
	}
	public void setHeartbeat(int heartbeat) {
		this.heartbeat = heartbeat;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public int getThreadCount() {
		return threadCount;
	}
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
	public EventSourceType getEventSourceType() {
		return eventSourceType;
	}
	public void setEventSourceType(EventSourceType eventSourceType) {
		this.eventSourceType = eventSourceType;
	}
	public ExecuteScope getScope() {
		return scope;
	}
	public void setScope(ExecuteScope scope) {
		this.scope = scope;
	}
	@Override
	public String toString() {
		return "ProbeInfo [name=" + name + ", version=" + version + ", type=" + type + ", eventSourceType="
				+ eventSourceType + ", key=" + key + ", port=" + port + ", heartbeat=" + heartbeat + ", timeout="
				+ timeout + ", threadCount=" + threadCount + "]";
	}
}

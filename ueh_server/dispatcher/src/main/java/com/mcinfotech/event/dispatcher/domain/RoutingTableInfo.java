package com.mcinfotech.event.dispatcher.domain;

import com.mcinfotech.event.domain.ProtocolVersion;

/**
 * 转发路由表信息

 *
 */
public class RoutingTableInfo {
	private String type;
	private String host;
	private int port;
	private boolean enable;
	private ProtocolVersion protocolVersion;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}
	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
	@Override
	public String toString() {
		return "RoutingTableInfo [type=" + type + ", host=" + host + ", port=" + port + ", enable=" + enable
				+ ", protocolVersion=" + protocolVersion + "]";
	}
}

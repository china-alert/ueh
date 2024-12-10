package com.mcinfotech.event.dispatcher.domain;

import java.util.Map;

/**
 * 转发路由表，目前已类型为索引，后续修改为ProbeKey作为索引

 *
 */
public class RoutingTable {
	private Map<String,RoutingTableInfo> routingTable;
	
	public RoutingTable(Map<String,RoutingTableInfo> routingTable){
		this.routingTable=routingTable;

	}
	public Map<String, RoutingTableInfo> getRoutingTable() {
		return routingTable;
	}

	public void setRoutingTable(Map<String, RoutingTableInfo> routingTable) {
		this.routingTable = routingTable;
	}
	@Override
	public String toString() {
		return "RoutingTable [routingTable=" + routingTable + "]";
	}
}

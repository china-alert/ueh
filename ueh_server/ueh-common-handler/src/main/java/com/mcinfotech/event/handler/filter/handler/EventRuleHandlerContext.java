package com.mcinfotech.event.handler.filter.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.handler.config.EventHandlerRuleConfig;
import com.mcinfotech.event.handler.domain.EventIntegratedProbe;

import cn.mcinfotech.data.service.db.ColumnDefine;

public class EventRuleHandlerContext {
	private ProjectInfo projectInfo;
	private ProbeInfo probeInfo;
	private LoadingCache<String,ColumnDefine> columnDefineCache;
	private EventHandlerRuleConfig eventHandlerRuleConfig;
	private LoadingCache<String,EventIntegratedProbe> eventIntegratedProbeCache;
	private ConcurrentMap<String,Object> parameters=new ConcurrentHashMap<>();
	
	public EventRuleHandlerContext(ProjectInfo projectInfo,ProbeInfo probeInfo,LoadingCache<String,EventIntegratedProbe> eventIntegratedProbeCache,LoadingCache<String,ColumnDefine> columnDefineCache,EventHandlerRuleConfig eventHandlerRuleConfig) {
		this.projectInfo=projectInfo;
		this.probeInfo=probeInfo;
		this.eventIntegratedProbeCache=eventIntegratedProbeCache;
		this.columnDefineCache = columnDefineCache;
		this.eventHandlerRuleConfig = eventHandlerRuleConfig;
	}

	public LoadingCache<String, ColumnDefine> getColumnDefineCache() {
		return columnDefineCache;
	}

	public void setColumnDefineCache(LoadingCache<String, ColumnDefine> columnDefineCache) {
		this.columnDefineCache = columnDefineCache;
	}

	public LoadingCache<String, EventIntegratedProbe> getEventIntegratedProbeCache() {
		return eventIntegratedProbeCache;
	}

	public void setEventIntegratedProbeCache(LoadingCache<String, EventIntegratedProbe> eventIntegratedProbeCache) {
		this.eventIntegratedProbeCache = eventIntegratedProbeCache;
	}

	public ProjectInfo getProjectInfo() {
		return projectInfo;
	}

	public void setProjectInfo(ProjectInfo projectInfo) {
		this.projectInfo = projectInfo;
	}

	public ProbeInfo getProbeInfo() {
		return probeInfo;
	}

	public void setProbeInfo(ProbeInfo probeInfo) {
		this.probeInfo = probeInfo;
	}

	public EventHandlerRuleConfig getEventHandlerRuleConfig() {
		return eventHandlerRuleConfig;
	}

	public void setEventHandlerRuleConfig(EventHandlerRuleConfig eventHandlerRuleConfig) {
		this.eventHandlerRuleConfig = eventHandlerRuleConfig;
	}

	public ConcurrentMap<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(ConcurrentMap<String, Object> parameters) {
		this.parameters = parameters;
	}
}

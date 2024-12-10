package com.mcinfotech.event.handler.filter.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.UehEventMessage;
import com.mcinfotech.event.handler.algorithm.EventMapping;
import com.mcinfotech.event.handler.domain.EventIntegratedProbe;
import com.mcinfotech.event.handler.domain.PlatformProbeColumnMapping;
import com.mcinfotech.event.handler.domain.PlatformProbeSeverityMapping;
import com.mcinfotech.event.utils.FastJsonUtils;

/**
 * 已接入Probe：
 * Zabbix Probe、API Server Probe要求事件源传递Probe Key
 * 邮件（从邮箱主动读取，不包括Probe Key）
 * 动环（从接口主动读取，不包括Probe Key）
 * 天旦、科来（非标Syslog,UDP消息，不包括Probe Key）
 * 在各自的Probe中需要配置Probe Key，Probe Key无效，到来的事件消息将不会被处理。

 *
 */
public class DefaultMappingRuleHandler implements EventRuleHandler<UehEventMessage,Collection<Map<String, Object>>,EventRuleHandlerContext> {
	Logger logger = LogManager.getLogger();
	@Override
	public  Collection<Map<String, Object>> process(UehEventMessage uehEventMessage,EventRuleHandlerContext ctx) throws InterruptedException, ExecutionException {
		if(logger.isDebugEnabled()) {
			logger.debug("the infomation of input event message is "+uehEventMessage.toSimpleString());
		}
		List<Map<String, Object>> handleredDatas = new ArrayList<Map<String, Object>>();
		if(uehEventMessage.getProbe()==null) {
			logger.warn("invalid message , integrated probe is null, error message id is " + uehEventMessage.getTransactionId());
			return handleredDatas;
		}
		EventIntegratedProbe integratedProbe=ctx.getEventIntegratedProbeCache().get(uehEventMessage.getProbe().getKey());
		/**
		 * 对到来的告警事件进行验证，如果找不到接入Probe配置则返回空的消息（或者Probe已经被被禁用）；
		 * 如果没有做字段映射、级别映射配置则返回空的事件信息，同时打印事件ID
		 */
		if (integratedProbe== null) {
			logger.warn("invalid message , not found integrated probe , error message id is " + uehEventMessage.getTransactionId());
			logger.warn("the probe infomation of invalid message is "+uehEventMessage.getProbe());
			return handleredDatas;
		}
		if (StringUtils.isBlank(integratedProbe.getColumnMapping())) {
			logger.warn("invalid message , integrated probe column's mapping is not setup , error message id is " + uehEventMessage.getTransactionId());
			return handleredDatas;
		}
		if (StringUtils.isBlank(integratedProbe.getSeverityMapping())) {
			logger.warn("invalid message , integrated probe severity's mapping is not setup , error message id is " + uehEventMessage.getTransactionId());
			return handleredDatas;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("mapping columns " + integratedProbe.getColumnMapping());
			logger.debug("mapping severity :" + integratedProbe.getSeverityMapping());
		}
		/*
		 * if(integratedProbe.getSourceType()==EventSourceType.CASCADE) {
		 * 
		 * }
		 */
		PlatformProbeColumnMapping columnsSettings = FastJsonUtils.toBean(integratedProbe.getColumnMapping(),PlatformProbeColumnMapping.class);
		PlatformProbeSeverityMapping severitySettings = FastJsonUtils.toBean(integratedProbe.getSeverityMapping(),PlatformProbeSeverityMapping.class);
		for (String messageContent : uehEventMessage.getMessages()) {
			// 1.接收校验，通过Probe(Probe Key)、Project(ID)进行校验,校验不通过的，废弃
			// 每个List应该是同一Probe、Project,尚未验证
			Map<String, Object> eventMessage = (Map) FastJsonUtils.stringToCollect(messageContent);
			eventMessage.put("RefIntegratedRules",integratedProbe.getName());
			handleredDatas.add(EventMapping.excute(eventMessage, columnsSettings, severitySettings));
		}
		if(logger.isDebugEnabled()) {
			logger.debug("the size of output event message is"+handleredDatas.size());
		}
		return handleredDatas;
	}
}

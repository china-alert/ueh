package com.mcinfotech.event.handler.filter.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.OrderBy;
import com.mcinfotech.event.handler.algorithm.v2.EventRecovery;
import com.mcinfotech.event.handler.domain.EventHandlerRule;

public class DefaultRecoveryRuleHandler implements EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>, EventRuleHandlerContext> {
	Logger logger = LogManager.getLogger();
	@Override
	public Collection<Map<String, Object>> process(Collection<Map<String, Object>> in, EventRuleHandlerContext ctx) {
		if(CollectionUtils.isEmpty(in))return in;
		if(logger.isDebugEnabled()) {
			logger.debug("the size of input event message is "+in==null?0:in.size());
		}
		List<EventHandlerRule> recoveryRules =ctx.getEventHandlerRuleConfig().getRules(ctx.getProjectInfo().getId(),ctx.getProbeInfo().getKey(), ctx.getProbeInfo().getEventSourceType(),null,OrderBy.ASC, "RE");
		/**
		 * 多条恢复策略只选一条级别最高的，恢复策略与事件源是强关联
		 */
		EventHandlerRule eventHandlerRule = null;
		if (CollectionUtils.isNotEmpty(recoveryRules))
			eventHandlerRule = recoveryRules.get(0);
		return EventRecovery.excute(ctx.getProbeInfo(),in, eventHandlerRule, ctx.getEventHandlerRuleConfig().getDataSource(), ctx.getProjectInfo());
	}
}

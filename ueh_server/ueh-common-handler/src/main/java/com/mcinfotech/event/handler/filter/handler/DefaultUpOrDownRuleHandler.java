package com.mcinfotech.event.handler.filter.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.OrderBy;
import com.mcinfotech.event.handler.algorithm.v2.EventSeverityUpOrDown;
import com.mcinfotech.event.handler.domain.EventHandlerRule;

public class DefaultUpOrDownRuleHandler implements EventRuleHandler<Collection<Map<String,Object>>,Collection<Map<String,Object>>,EventRuleHandlerContext>{
	Logger logger = LogManager.getLogger();
	@Override
	public Collection<Map<String, Object>> process(Collection<Map<String, Object>> in,EventRuleHandlerContext ctx) {
		if(CollectionUtils.isEmpty(in))return in;
		if(logger.isDebugEnabled()) {
			logger.debug("the size of input event message is "+in==null?0:in.size());
		}
		List<EventHandlerRule> upRules = ctx.getEventHandlerRuleConfig().getRules(ctx.getProjectInfo().getId(), ctx.getProbeInfo().getKey(), ctx.getProbeInfo().getEventSourceType(),null,OrderBy.DESC, "U");
		if(MapUtils.isEmpty(ctx.getColumnDefineCache().asMap()))ctx.getColumnDefineCache().getAll(Arrays.asList("ALL"));
		return EventSeverityUpOrDown.excute(ctx.getProbeInfo(),in, ctx.getColumnDefineCache().asMap(), upRules);
	}
}

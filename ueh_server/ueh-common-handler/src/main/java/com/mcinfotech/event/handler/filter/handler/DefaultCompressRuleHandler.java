package com.mcinfotech.event.handler.filter.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.OrderBy;
import com.mcinfotech.event.handler.algorithm.v2.EventCompress;
import com.mcinfotech.event.handler.domain.EventHandlerRule;

public class DefaultCompressRuleHandler implements EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>, EventRuleHandlerContext> {
	Logger logger = LogManager.getLogger();
	@Override
	public Collection<Map<String, Object>> process(Collection<Map<String, Object>> in, EventRuleHandlerContext ctx) {
		if(CollectionUtils.isEmpty(in))return in;
		if(logger.isDebugEnabled()) {
			logger.debug("the size of input event message is "+in==null?0:in.size());
		}
		// 拿优先级最高的一条压缩
		List<EventHandlerRule> compressRules = ctx.getEventHandlerRuleConfig().getRules(ctx.getProjectInfo().getId(),ctx.getProbeInfo().getKey(), ctx.getProbeInfo().getEventSourceType(),null,OrderBy.ASC, "Z");
		/**
		 * 同一类型的事件源只有一个恢复策略
		 */
		List<EventHandlerRule> recoveryRules = ctx.getEventHandlerRuleConfig().getRules(ctx.getProjectInfo().getId(),ctx.getProbeInfo().getKey(), ctx.getProbeInfo().getEventSourceType(),null,OrderBy.ASC, "RE");
		EventHandlerRule eventHandlerRule = null;
		if (CollectionUtils.isNotEmpty(recoveryRules))
			eventHandlerRule = recoveryRules.get(0);
		return EventCompress.excute(ctx.getProbeInfo(), in,compressRules,ctx.getEventHandlerRuleConfig().getDataSource(), ctx.getProjectInfo().getId(),eventHandlerRule);
	}
}

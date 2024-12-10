package com.mcinfotech.event.handler.filter.handler;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultDoItHandler
		implements EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> {
	Logger logger = LogManager.getLogger();
	@Override
	public Collection<Map<String, Object>> process(Collection<Map<String, Object>> in,EventRuleHandlerContext ctx) {
		if(logger.isDebugEnabled()) {
			logger.debug("the size of input event message is "+in==null?0:in.size());
		}
		return in;
	}

}

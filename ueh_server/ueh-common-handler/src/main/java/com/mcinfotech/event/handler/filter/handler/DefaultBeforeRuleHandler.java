package com.mcinfotech.event.handler.filter.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.UehEventMessage;

public class DefaultBeforeRuleHandler implements EventRuleHandler<UehEventMessage, UehEventMessage,EventRuleHandlerContext>{
	Logger logger = LogManager.getLogger();
	@Override
	public UehEventMessage process(UehEventMessage in,EventRuleHandlerContext ctx){
		
		if(logger.isDebugEnabled()) {
			logger.debug("before event rule handler has executed . ");
			logger.debug("the size of input event message is "+in.getMessageLenth());
		}
		return in;
	}
}

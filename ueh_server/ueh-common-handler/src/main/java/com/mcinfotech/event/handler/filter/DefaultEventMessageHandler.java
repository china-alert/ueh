package com.mcinfotech.event.handler.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.UehEventMessage;
import com.mcinfotech.event.handler.filter.executor.EventMessageHandlerExecutor;
import com.mcinfotech.event.handler.filter.handler.DefaultAfterRuleHandler;
import com.mcinfotech.event.handler.filter.handler.DefaultBeforeRuleHandler;
import com.mcinfotech.event.handler.filter.handler.DefaultCompressRuleHandler;
import com.mcinfotech.event.handler.filter.handler.DefaultDivideRuleHandler;
import com.mcinfotech.event.handler.filter.handler.DefaultDoItHandler;
import com.mcinfotech.event.handler.filter.handler.DefaultFilterRuleHandler;
import com.mcinfotech.event.handler.filter.handler.DefaultMappingRuleHandler;
import com.mcinfotech.event.handler.filter.handler.DefaultRecoveryRuleHandler;
import com.mcinfotech.event.handler.filter.handler.DefaultRichRuleHandler;
import com.mcinfotech.event.handler.filter.handler.DefaultUpOrDownRuleHandler;
import com.mcinfotech.event.handler.filter.handler.EventRuleHandler;
import com.mcinfotech.event.handler.filter.handler.EventRuleHandlerContext;

/**
 * 事件接收处理默认处理器
 * 其他的事件接收处理可以继承这个类，如果需要对某个处理节点做修改，可以重载这个节点对应的方法
 * 比如要对BeforeRuleHandler这个节点做修改，可以重载getBeforeRuleHandler()这个方法
 * 

 *
 */
public class DefaultEventMessageHandler extends EventMessageHandlerExecutor{

	@Override
	public EventRuleHandler<UehEventMessage, UehEventMessage,EventRuleHandlerContext> getBeforeRuleHandler() {
		return new DefaultBeforeRuleHandler();
	}

	@Override
	public EventRuleHandler<UehEventMessage, Collection<Map<String, Object>>,EventRuleHandlerContext> getMappingRuleHandler() {
		return new DefaultMappingRuleHandler();
	}

	@Override
	public EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getCompressRuleHandler() {
		return new DefaultCompressRuleHandler();
	}

	@Override
	public EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getRecoveryRuleHandler() {
		return new DefaultRecoveryRuleHandler();
	}

	@Override
	public EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getDivideRuleHandler() {
		return new DefaultDivideRuleHandler();
	}

	@Override
	public EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getUpOrDownRuleHandler() {
		return new DefaultUpOrDownRuleHandler();
	}

	@Override
	public EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getFilterRuleHandler() {
		return new DefaultFilterRuleHandler();
	}

	@Override
	public EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getRichRuleHandler() {
		return new DefaultRichRuleHandler();
	}

	@Override
	public EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getDoItHandler() {
		return new DefaultDoItHandler();
	}

	@Override
	public EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getAfterRuleHandler() {
		return new DefaultAfterRuleHandler();
	}
}

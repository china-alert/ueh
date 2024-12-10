package com.mcinfotech.event.handler.filter.handler;

public interface EventRuleHandler<In,Out,Context> {
	Out process(In in,Context ctx) throws Exception;

	default <NewOut> EventRuleHandler<In, NewOut,Context> add(EventRuleHandler<Out, NewOut,Context> pipe) {
		return (input,ctx) -> pipe.process(process(input,ctx),ctx);
	}
}

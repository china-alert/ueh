package com.mcinfotech.event.dispatcher.filter;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.utils.FastJsonUtils;

import io.netty.channel.ChannelHandlerContext;

/**
 * 处理器主动请求事件消息
 * 处理器传递处理器类型，然后Dispatcher将消息写回处理器
 *

 */
@Component
@Order(3)
public class HandlerRequestMessageilter implements IFilter<String> {
	private Logger logger = LogManager.getLogger(HandlerRequestMessageilter.class);
	/*@Resource
	private EventMessageConsumer producer;*/

	@Override
	public boolean chain(String message, ChannelHandlerContext ctx) {
		Map<String,Object> handler=(Map)FastJsonUtils.extractValue(message, "handler");
		
		if(handler!=null){
			if(logger.isDebugEnabled()){
				logger.debug(ctx.channel().remoteAddress() +"'s message has pushed queue .");
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean chain(List<String> message, ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		return false;
	}
}

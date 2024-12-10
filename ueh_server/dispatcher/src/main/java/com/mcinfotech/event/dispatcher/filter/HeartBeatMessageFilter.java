package com.mcinfotech.event.dispatcher.filter;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.utils.FastJsonUtils;

import io.netty.channel.ChannelHandlerContext;

/**
 * 将收到Probe送来的PING消息进行处理，心跳检测
 * 1.直接回消息给Probe

 */
@Component
@Order(1)
public class HeartBeatMessageFilter implements IFilter<String> {
	private Logger logger = LogManager.getLogger(HeartBeatMessageFilter.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public boolean chain(String message, ChannelHandlerContext ctx) {
		String messageType=(String)FastJsonUtils.extractValue(message, "messageType");
		
		if(StringUtils.isNotEmpty(messageType)){
			if(logger.isDebugEnabled()){
				logger.debug(ctx.channel().remoteAddress() +"'s message has pushed queue .");
			}
			//ctx.writeAndFlush(MessageBuilder.buildByteBuf(new HeartBeatMessage(sdf.format(new Date()),MessageType.PONG, Constant.PONG)));
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

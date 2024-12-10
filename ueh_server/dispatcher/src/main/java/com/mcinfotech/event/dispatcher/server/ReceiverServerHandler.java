package com.mcinfotech.event.dispatcher.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.filter.IFilter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 处理从Probe送来的消息
 *

 */
public class ReceiverServerHandler extends ChannelInboundHandlerAdapter {
	private Logger logger = LogManager.getLogger(getClass());
	/**
	 * 请自行维护Filter的添加顺序
	 */
	private List<IFilter<ProbeEventMessage>> messageFilters = new ArrayList<>();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object message) {
		if (StringUtils.isEmpty(message)) {
			logger.warn("no message comming .");
			return;
		}
		if(logger.isDebugEnabled()){
			logger.debug("message incoming : "+message);
		}
		if(message instanceof List) {
			List<ProbeEventMessage> probeMessage=(List<ProbeEventMessage>)message;
			for (IFilter<ProbeEventMessage> messageFilter : messageFilters) {
				boolean doNext = false;
				try {
					doNext = messageFilter.chain(probeMessage, ctx);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				if (!doNext) {
					return;
				}
			}
		}else {
			for (IFilter<ProbeEventMessage> messageFilter : messageFilters) {
				boolean doNext = false;
				try {
					doNext = messageFilter.chain((ProbeEventMessage)message, ctx);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				if (!doNext) {
					return;
				}
			}
		}
	}

	/*@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.handlerAdded(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+" has came ");
		}
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.handlerRemoved(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+" has left ");
		}
	}*/

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("some thing is error , " + cause.getMessage());
	}

	/*@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+"'s channel has actived");
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
		super.channelInactive(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+"'s channel has inactived");
		}
	}*/

	public void addMessageFilter(IFilter<ProbeEventMessage> iNettyMsgFilter) {
		if (iNettyMsgFilter != null) {
			messageFilters.add(iNettyMsgFilter);
		}
	}

	public void addMessageFilters(List<IFilter<ProbeEventMessage>> iNettyMsgFilters) {
		if (!CollectionUtils.isEmpty(iNettyMsgFilters)) {
			messageFilters.addAll(iNettyMsgFilters);
		}
	}

	/*@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+"'s channel has registered .");
		}
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+"'s channel has unregistered .");
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+"'s channel has read completely .");
		}
	}*/

	/*@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
		if(logger.isDebugEnabled()){
			logger.debug("event triggered : "+ctx.channel().remoteAddress()+"'s event has triggered");
		}
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
			//if (idleStateEvent.state() == IdleState.ALL_IDLE) {
				*//**
				 * 想转发服务端发送心跳检测，若是服务器在约定的时间内没有则判定为超时
				 *//*
				try {
					if(logger.isDebugEnabled()){
						logger.debug("reply pong to dispatcher when state is "+idleStateEvent.state());
					}
					ctx.writeAndFlush(MessageBuilder.buildByteBuf(new HeartBeatMessage(sdf.format(new Date()),MessageType.PONG, Constant.PONG)));
				} catch (Exception e) {
					e.printStackTrace();
					
				}
			//}
		}
	}*/

	/*@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		super.channelWritabilityChanged(ctx);
		if(logger.isDebugEnabled()){
			logger.debug("writablity changed : "+ctx.channel().remoteAddress()+"'s writablity of channel has changed");
		}
	}*/
}

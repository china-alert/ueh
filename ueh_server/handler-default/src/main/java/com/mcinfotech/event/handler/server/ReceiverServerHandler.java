package com.mcinfotech.event.handler.server;

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
 * 事件消息接收之后进行处理：
 * 1.接收校验，通过Probe(Probe Key)、Project(ID)进行校验,校验不通过的，废弃
 * 2.接收：校验通过的，存入内置队列，完成接收
 * 3.按照事件接入设置的字段映射定义、级别映射定义进行事件解析
 * 4.执行事件处理规则，包括合并、压缩、丰富、屏蔽等，也可能没有
 * 5.将事件消息与角色关联，对数据打标、入库
 * 1和2通过Filter处理
 * 3、4、5通过Pusher处理
 *

 */
public class ReceiverServerHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LogManager.getLogger(getClass());
    /**
     * 请自行维护Filter的添加顺序
     */
    private List<IFilter<ProbeEventMessage>> messageFilters = new ArrayList<>();
    //private ProbeInfo probe;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        if (StringUtils.isEmpty(message)) {
            logger.warn("no message comming .");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("message incoming : " + message);
        }
        List<ProbeEventMessage> probeMessage = (List<ProbeEventMessage>) message;
        /**
         * 对送来的消息进行过滤
         */
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
    }

	/*@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.handlerAdded(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+" has came ");
		}
	}*/

	/*@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.handlerRemoved(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+" has left ");
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		logger.error("some thing is error , " + cause.getMessage());
	}

	@Override
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
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		super.channelRead(ctx, msg);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+"'s channel has read .");
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+"'s channel has registered .");
		}
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelUnregistered(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+"'s channel has unregistered .");
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelReadComplete(ctx);
		if(logger.isDebugEnabled()){
			logger.debug(ctx.channel().remoteAddress()+"'s channel has read completely .");
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
		if(logger.isDebugEnabled()){
			logger.debug("event triggered : "+ctx.channel().remoteAddress()+"'s event has triggered");
		}
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelWritabilityChanged(ctx);
		if(logger.isDebugEnabled()){
			logger.debug("writablity changed : "+ctx.channel().remoteAddress()+"'s writablity of channel has changed");
		}
	}*/
}

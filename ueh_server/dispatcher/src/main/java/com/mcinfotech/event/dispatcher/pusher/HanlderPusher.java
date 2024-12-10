package com.mcinfotech.event.dispatcher.pusher;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.dispatcher.DispatcherHolder;
import com.mcinfotech.event.dispatcher.domain.RoutingTable;
import com.mcinfotech.event.dispatcher.domain.RoutingTableInfo;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.push.IPusher;

/**
 * 推送到消息处理器，enable开关决定了是否启用推送

 */
@Component
public class HanlderPusher implements IPusher<ProbeEventMessage> {
	private static Logger logger=LogManager.getLogger(HanlderPusher.class);
	@Resource
	RoutingTable routingTable;
	
	private AtomicLong messageCount=new AtomicLong(0);
	
	@Override
	public void push(ProbeEventMessage message) {
		RoutingTableInfo routingInfo=this.routingTable.getRoutingTable().get(message.getProbe().getType().name());
		if(routingInfo!=null&&routingInfo.isEnable()){
			if(logger.isDebugEnabled()){
				logger.debug("pusher start to work and push message to "+routingInfo.getHost()+" via port "+routingInfo.getPort());
			}
			messageCount.set(0l);
			DispatcherHolder.flushToDispatcher(routingInfo.getType(),message,routingInfo.getHost(),routingInfo.getPort());
		}else{
			if(logger.isDebugEnabled()){
				logger.debug("notice not start , message transmit stop ."+messageCount.incrementAndGet());
			}
		}
	}

	@Override
	public void push(String handlerType,List<ProbeEventMessage> messages) {
		RoutingTableInfo routingInfo=this.routingTable.getRoutingTable().get(handlerType);
		if(routingInfo!=null&&routingInfo.isEnable()){
			if(logger.isDebugEnabled()){
				logger.debug("pusher start to work and push message to "+routingInfo.getHost()+" via port "+routingInfo.getPort());
			}
			messageCount.set(0l);
			DispatcherHolder.flushToDispatcher(routingInfo.getType(),messages,routingInfo.getHost(),routingInfo.getPort());
		}else{
			if(logger.isDebugEnabled()){
				logger.debug("notice not start , message transmit stop ."+messageCount.incrementAndGet());
			}
		}
	}

	@Override
	public void push(String handlerType, ProbeEventMessage message) {
		RoutingTableInfo routingInfo=this.routingTable.getRoutingTable().get(handlerType);
		if(routingInfo!=null&&routingInfo.isEnable()){
			if(logger.isDebugEnabled()){
				logger.debug("pusher start to work and push message to "+routingInfo.getHost()+" via port "+routingInfo.getPort());
			}
			messageCount.set(0l);
			DispatcherHolder.flushToDispatcher(routingInfo.getType(),message,routingInfo.getHost(),routingInfo.getPort());
		}else{
			if(logger.isDebugEnabled()){
				logger.debug("notice not start , message transmit stop ."+messageCount.incrementAndGet());
			}
		}
	}

	@Override
	public void push(List<ProbeEventMessage> message) {
		RoutingTableInfo routingInfo=this.routingTable.getRoutingTable().get(message.get(0).getProbe().getType().name());
		if(routingInfo!=null&&routingInfo.isEnable()){
			if(logger.isDebugEnabled()){
				logger.debug("pusher start to work and push message to "+routingInfo.getHost()+" via port "+routingInfo.getPort());
			}
			messageCount.set(0l);
			DispatcherHolder.flushToDispatcher(routingInfo.getType(),message,routingInfo.getHost(),routingInfo.getPort());
		}else{
			if(logger.isDebugEnabled()){
				logger.debug("notice not start , message transmit stop ."+messageCount.incrementAndGet());
			}
		}
	}
}

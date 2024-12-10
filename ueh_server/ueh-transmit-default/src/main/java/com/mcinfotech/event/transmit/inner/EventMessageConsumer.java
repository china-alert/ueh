package com.mcinfotech.event.transmit.inner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Queues;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.listener.IListener;
import com.mcinfotech.event.transmit.domain.TransmitInfo;

/**
 * 事件消费
 * 按照事件源类型进行消费，消费策略：按数量或者时间周期进行消费，任何一个先到都会触发消费

 */

public class EventMessageConsumer {
	private Logger logger=LogManager.getLogger(EventMessageConsumer.class);
	private IListener<ProbeEventMessage> probeEventMessageListener;
	private TransmitInfo transmitInfo;
	
	public void beginConsume() {
		while (true) {
			try {
				/*for(String handlerType:routingTable.getRoutingTable().keySet()){*/
					List<ProbeEventMessage> messages=new ArrayList<ProbeEventMessage>();
					Queues.drain(InnerConfig.QUEUE, messages, transmitInfo.getBatch(), transmitInfo.getInterval(), TimeUnit.SECONDS);
					/*if(logger.isDebugEnabled()){
						logger.debug("fetch "+handlerType+"'s message , size is "+messages.size());
					}*/
					if(messages.size()>0){
						probeEventMessageListener.dispatcher(messages);
					}
				//}
			} catch (Exception e) {
				try {
					Thread.sleep(500);
				} catch (Exception ie) {
					logger.error(ie.getMessage(),ie);
				}
				logger.error(e.getMessage(),e);
			}
		}
	}
	public void setProbeEventMessageListener(IListener<ProbeEventMessage> eventProbeMessageListener) {
		this.probeEventMessageListener = eventProbeMessageListener;
	}
	public TransmitInfo getTransmitInfo() {
		return transmitInfo;
	}
	public void setTransmitInfo(TransmitInfo transmitInfo) {
		this.transmitInfo = transmitInfo;
	}
}

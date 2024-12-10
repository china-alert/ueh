package com.mcinfotech.event.probe.inner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.listener.IListener;

/**
 * 事件消费

 */

public class EventMessageConsumer {
	private Logger logger= LogManager.getLogger(EventMessageConsumer.class);
	private IListener<ProbeEventMessage> eventProbeMessageListener;

	public void setEventProbeMessageListener(IListener<ProbeEventMessage> eventProbeMessageListener) {
		this.eventProbeMessageListener = eventProbeMessageListener;
	}

	public void beginConsume() {
		while (true) {
			try {
				if(!InnerConfig.MESSAGEQUEUE.isEmpty()){
					eventProbeMessageListener.dispatcher(InnerConfig.MESSAGEQUEUE.take());
				}else {
					Thread.sleep(500);
				}
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
}

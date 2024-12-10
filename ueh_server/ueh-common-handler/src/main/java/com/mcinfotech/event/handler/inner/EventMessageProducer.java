package com.mcinfotech.event.handler.inner;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeEventMessage;

/**
 * 将监控工具来的事件，存入队列

 */
@Component
public class EventMessageProducer {
	private Logger logger=LogManager.getLogger(EventMessageProducer.class);
	
	public void push(List<ProbeEventMessage> messages){
		try {
			InnerConfig.QUEUE.addAll(messages);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}

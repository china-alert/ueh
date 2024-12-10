package com.mcinfotech.event.probe.inner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mcinfotech.event.domain.ProbeEventMessage;

/**
 * 将监控工具来的事件，存入队列

 */
@Component
public class EventMessageProducer {
	private Logger logger=LogManager.getLogger(EventMessageProducer.class);
	
	public void push(ProbeEventMessage message) {
		if (StringUtils.isEmpty(message.getMessageBody())) {
			logger.warn("no message comming");
			return;
		}
		try {
			InnerConfig.MESSAGEQUEUE.put(message);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}

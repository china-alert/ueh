package com.mcinfotech.event.probe.inner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeState;

/**
 * 将Probe状态存储到队列

 */
@Component
public class ProbeStateProducer {
	private Logger logger=LogManager.getLogger(ProbeStateProducer.class);
	
	public void push(ProbeState state) {
		if (state==null) {
			logger.warn("no state comming");
			return;
		}
		try {
			InnerConfig.STATEQUEUE.put(state);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

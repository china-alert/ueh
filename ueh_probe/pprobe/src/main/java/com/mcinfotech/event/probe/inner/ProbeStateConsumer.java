package com.mcinfotech.event.probe.inner;

import com.mcinfotech.event.domain.ProbeState;
import com.mcinfotech.event.listener.IListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 从状态队列消费Probe状态

 */

public class ProbeStateConsumer {
	private Logger logger= LogManager.getLogger(ProbeStateConsumer.class);
	private IListener<ProbeState> probeStateListener;

	public void setProbeStateListener(IListener<ProbeState> probeStateListener) {
		this.probeStateListener = probeStateListener;
	}

	public void beginConsume() {
		while (true) {
			try {
				if(InnerConfig.STATEQUEUE.size()>0){
					ProbeState model = InnerConfig.STATEQUEUE.take();
					probeStateListener.dispatcher(model);
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

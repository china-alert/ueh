package com.mcinfotech.event.handler.starter;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.handler.server.ReceiverServer;
import com.mcinfotech.event.utils.AsyncPool;

/**
 * Dispatcher接收器启动

 */
@Component
public class EventReceiverServerStarter {
	private Logger logger = LogManager.getLogger(EventReceiverServerStarter.class);
	
	@Resource
	private List<IFilter<ProbeEventMessage>> messageFilters;
	
	@Resource
	private ProbeInfo probe;
	
	@PostConstruct
	public void start() {
		AsyncPool.asyncDo(() -> {
			if(logger.isInfoEnabled()){
				logger.info("event receiver is starting");
			}
			
			ReceiverServer receiverServer = new ReceiverServer();
			receiverServer.setMessageFilters(messageFilters);
			//receiverServer.setProbe(probe);
			try {
				receiverServer.startNettyServer(this.probe.getPort());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		});
	}
}

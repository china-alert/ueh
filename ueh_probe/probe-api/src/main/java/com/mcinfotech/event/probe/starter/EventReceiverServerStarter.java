package com.mcinfotech.event.probe.starter;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.probe.server.ReceiverServer;
import com.mcinfotech.event.utils.AsyncPool;

/**
 * probe接收器启动

 */
@Component
public class EventReceiverServerStarter {
	private Logger logger = LogManager.getLogger(EventReceiverServerStarter.class);
	@Autowired
	ProbeInfo probeInfo;
	@Resource
	private List<IFilter<String>> messageFilters;
	
	@PostConstruct
	public void start() {
		AsyncPool.asyncDo(() -> {
			if(logger.isInfoEnabled()){
				logger.info("event receiver is starting");
			}
			
			ReceiverServer receiverServer = new ReceiverServer();
			receiverServer.setMessageFilters(messageFilters);
			try {
				receiverServer.startNettyServer(probeInfo.getPort());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		});
	}
}

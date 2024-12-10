package com.mcinfotech.event.probe.pusher;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeState;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.probe.state.StateReportUtils;
import com.mcinfotech.event.push.IPusher;

/**
 * 上报Probe状态：将Probe状态入库
 */
@Component
public class ProbeStateDispatcherPusher implements IPusher<ProbeState> {
	private static Logger logger=LogManager.getLogger(ProbeStateDispatcherPusher.class);
	@Value("${dispatcher.host}")
	private String host;
	@Value("${dispatcher.port}")
	private int port;
	@Value("${dispatcher.enable}")
	private boolean enable;
	private AtomicLong messageCount=new AtomicLong(0);
	
	@Autowired
	DataSource dataSource;
	@Autowired
	ProjectInfo project;
	//@StateReport(action="启动",stateType=ProbeStateType.RUNNING)
	@Override
	public void push(ProbeState probeState) {
		if(this.enable){
			if(logger.isDebugEnabled()){
				logger.debug("pusher start to work and push message to "+host+" via port "+port);
			}
			StateReportUtils.report(dataSource, probeState, project);
			messageCount.set(0l);
		}else{
			if(logger.isDebugEnabled()){
				logger.debug("notice not start , message transmit stop . "+messageCount.incrementAndGet());
			}
		}
	}
	@Override
	public void push(String handlerType, List<ProbeState> messages) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void push(String handlerType, ProbeState messages) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void push(List<ProbeState> message) {
		// TODO Auto-generated method stub
		
	}
}

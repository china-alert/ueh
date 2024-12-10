package com.mcinfotech.event.probe.inner.listener;

import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeState;
import com.mcinfotech.event.listener.IListener;
import com.mcinfotech.event.push.IPusher;

/**
 * 将Probe状态推送入库
 *

 */
@Component
public class ProbeStateListener implements IListener<ProbeState> {
	private Logger logger = LogManager.getLogger(ProbeStateListener.class);
	@Resource
	private List<IPusher<ProbeState>> iPushers;

	@Override
	public void dispatcher(ProbeState state) {
		// 开启推送
		if (logger.isInfoEnabled()) {
			logger.info("push message start ...");
		}
		// 分别推送到各client和etcd
		for (IPusher<ProbeState> pusher : iPushers) {
			pusher.push(state);
		}
	}

	@Override
	public void dispatcher(String handlerType, List<ProbeState> message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispatcher(String handlerType, ProbeState message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispatcher(List<ProbeState> message) {
		// TODO Auto-generated method stub
		
	}
}

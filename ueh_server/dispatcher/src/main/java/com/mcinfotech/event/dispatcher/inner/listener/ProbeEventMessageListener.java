package com.mcinfotech.event.dispatcher.inner.listener;

import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.listener.IListener;
import com.mcinfotech.event.push.IPusher;

/**
 * 将事件推送到事件转发程序
 *

 */
@Component
public class ProbeEventMessageListener implements IListener<ProbeEventMessage> {
	private Logger logger = LogManager.getLogger(ProbeEventMessageListener.class);
	@Resource
	private List<IPusher<ProbeEventMessage>> iPushers;

	@Override
	public void dispatcher(ProbeEventMessage message) {
		// 开启推送
		if (logger.isInfoEnabled()) {
			logger.info("push message start ...");
		}
		// 分别推送到各client和etcd
		for (IPusher<ProbeEventMessage> pusher : iPushers) {
			pusher.push(message);
		}
	}
	@Override
	public void dispatcher(String handlerType,List<ProbeEventMessage> messages) {
		// 开启推送
		if (logger.isInfoEnabled()) {
			logger.info("push message start ...");
		}
		// 分别推送到各client和etcd
		for (IPusher<ProbeEventMessage> pusher : iPushers) {
			pusher.push(handlerType,messages);
		}
	}
	@Override
	public void dispatcher(String handlerType, ProbeEventMessage message) {
		if (logger.isInfoEnabled()) {
			logger.info("push message start ...");
		}
		// 分别推送到各client和etcd
		for (IPusher<ProbeEventMessage> pusher : iPushers) {
			pusher.push(handlerType,message);
		}
	}
	@Override
	public void dispatcher(List<ProbeEventMessage> messages) {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("push message start ...");
		}
		// 分别推送到各client和etcd
		for (IPusher<ProbeEventMessage> pusher : iPushers) {
			pusher.push(messages);
		}
	}
}

package com.mcinfotech.event.dispatcher.inner.listener;

import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.UehEventMessage;
import com.mcinfotech.event.listener.IListener;
import com.mcinfotech.event.push.IPusher;

/**
 * 将事件推送到事件转发程序
 *

 */
@Component
public class InnerEventMessageListener implements IListener<UehEventMessage> {
	private Logger logger = LogManager.getLogger(InnerEventMessageListener.class);
	@Resource
	private List<IPusher<UehEventMessage>> iPushers;

	@Override
	public void dispatcher(UehEventMessage message) {
		// 开启推送
		if (logger.isInfoEnabled()) {
			logger.info("push message start ...");
		}
		// 分别推送到各client和etcd
		for (IPusher<UehEventMessage> pusher : iPushers) {
			pusher.push(message);
		}
	}
	@Override
	public void dispatcher(String handlerType,List<UehEventMessage> messages) {
		// 开启推送
		if (logger.isInfoEnabled()) {
			logger.info("push message start ...");
		}
		// 分别推送到各client和etcd
		for (IPusher<UehEventMessage> pusher : iPushers) {
			pusher.push(handlerType,messages);
		}
	}
	@Override
	public void dispatcher(String handlerType, UehEventMessage message) {
		if (logger.isInfoEnabled()) {
			logger.info("push message start ...");
		}
		// 分别推送到各client和etcd
		for (IPusher<UehEventMessage> pusher : iPushers) {
			pusher.push(handlerType,message);
		}
	}
	@Override
	public void dispatcher(List<UehEventMessage> messages) {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("push message start ...");
		}
		// 分别推送到各client和etcd
		for (IPusher<UehEventMessage> pusher : iPushers) {
			pusher.push(messages);
		}
	}
}

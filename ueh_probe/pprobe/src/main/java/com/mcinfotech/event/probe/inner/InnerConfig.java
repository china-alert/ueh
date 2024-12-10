package com.mcinfotech.event.probe.inner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProbeState;
import com.mcinfotech.event.inner.Consumer;
import com.mcinfotech.event.listener.IListener;
import com.mcinfotech.event.utils.CpuNum;

/**
 * probe内部存储事件消息
 * 
 */
@Configuration
public class InnerConfig {
	@Resource
	private IListener<ProbeEventMessage> eventProbeMessageListener;
	
	@Resource
	private IListener<ProbeState> eventProbeStateListener;

	private ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

	@Autowired
	ProbeInfo probeInfo;
	/**
	 * 接收消息队列
	 */
	public static LinkedBlockingQueue<ProbeEventMessage> MESSAGEQUEUE = new LinkedBlockingQueue<>(2000000);
	/**
	 * 
	 */
	public static LinkedBlockingQueue<ProbeState> STATEQUEUE=new LinkedBlockingQueue<>(1000);
	@Bean
	/**
	 * 启动消费事件队列
	 * @return
	 */
	public Consumer<EventMessageConsumer> messageConsumer() {
		int nowCount = CpuNum.workerCount();
		// 将实际值赋给static变量
		if (this.probeInfo.getThreadCount() != 0) {
			nowCount = this.probeInfo.getThreadCount();
		} else {
			if (nowCount >= 8) {
				nowCount = nowCount / 2;
			}
		}

		List<EventMessageConsumer> consumerList = new ArrayList<>();
		for (int i = 0; i < nowCount; i++) {
			EventMessageConsumer keyConsumer = new EventMessageConsumer();
			keyConsumer.setEventProbeMessageListener(eventProbeMessageListener);
			consumerList.add(keyConsumer);
			threadPoolExecutor.submit(keyConsumer::beginConsume);
		}
		return new Consumer<EventMessageConsumer>(consumerList);
	}
	@Bean
	/**
	 * 启动Probe状态消费
	 * @return
	 */
	public Consumer<ProbeStateConsumer> stateConsumer() {
		int nowCount = CpuNum.workerCount();
		// 将实际值赋给static变量
		if (this.probeInfo.getThreadCount() != 0) {
			nowCount = this.probeInfo.getThreadCount();
		} else {
			if (nowCount >= 8) {
				nowCount = nowCount / 2;
			}
		}

		List<ProbeStateConsumer> consumerList = new ArrayList<>();
		for (int i = 0; i < nowCount; i++) {
			ProbeStateConsumer keyConsumer = new ProbeStateConsumer();
			keyConsumer.setProbeStateListener(eventProbeStateListener);
			consumerList.add(keyConsumer);
			threadPoolExecutor.submit(keyConsumer::beginConsume);
		}
		return new Consumer<ProbeStateConsumer>(consumerList);
	}
}

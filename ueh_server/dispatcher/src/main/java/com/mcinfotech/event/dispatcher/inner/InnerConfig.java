package com.mcinfotech.event.dispatcher.inner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mcinfotech.event.dispatcher.domain.DispatcherInfo;
import com.mcinfotech.event.dispatcher.domain.RoutingTable;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.UehEventMessage;
import com.mcinfotech.event.inner.Consumer;
import com.mcinfotech.event.listener.IListener;
import com.mcinfotech.event.utils.CpuNum;

/**
 * probe（Dispatcher）内部存储事件消息
 * 

 */
@Configuration
public class InnerConfig {
	@Resource
	private IListener<ProbeEventMessage>eventMessageV1Listener;
	@Resource
	private IListener<UehEventMessage> eventMessageV2Listener;
	@Resource
	private DispatcherInfo dispatcherInfo;
	@Autowired
	ProbeInfo probeInfo;
	@Autowired
	RoutingTable routingTableInfo;
	private ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();
	/**
	 * 队列
	 */
	public static ConcurrentMap<String,LinkedBlockingQueue<ProbeEventMessage>> TABLE=new ConcurrentHashMap<>();
	/**
	 * 按照配置的dispatcher路由进行dispatcher存储初始化
	 * @return
	 */
	@Bean
	public boolean initilizeTable(){
		for(String handlerType:this.routingTableInfo.getRoutingTable().keySet()){
			InnerConfig.TABLE.put(handlerType, new LinkedBlockingQueue<>(2000000));
		}
		return true;
	}
	
	/**
	 * 启动消费事件队列
	 * @return
	 */
	//@Bean
	@Deprecated
	public Consumer<EventMessageConsumer> zabbixConsumer() {
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
			EventMessageConsumer consumer = new EventMessageConsumer();
			consumer.setProbeEventMessageListener(eventMessageV1Listener);
			consumer.setRoutingTable(routingTableInfo);
			consumer.setDispatcherInfo(dispatcherInfo);
			consumerList.add(consumer);

			threadPoolExecutor.submit(consumer::beginConsume);
		}
		return new Consumer<EventMessageConsumer>(consumerList);
	}
	
	@Bean
	Consumer<EventMessageDefaultConsumer> buildDefaulConsumer() {
		if(this.routingTableInfo==null)return null;
		if(MapUtils.isEmpty(this.routingTableInfo.getRoutingTable())) return null;
		
		List<EventMessageDefaultConsumer> consumerList = new ArrayList<>();
		int nowCount = CpuNum.workerCount();
		if (this.probeInfo.getThreadCount() != 0) {
			nowCount = this.probeInfo.getThreadCount();
		} else {
			if (nowCount >= 8) {
				nowCount = nowCount / 2;
			}
		}
		
		for(String handlerType:routingTableInfo.getRoutingTable().keySet()){
			for (int i = 0; i < nowCount; i++) {
				EventMessageDefaultConsumer consumer = new EventMessageDefaultConsumer();
				consumer.setEventMessageV2Listener(eventMessageV2Listener);
				consumer.setEventMessageV1Listener(eventMessageV1Listener);
				//consumer.setCurrentEventRoutingName(handlerType);
				consumer.setCurrentRouteTableInfo(routingTableInfo.getRoutingTable().get(handlerType));
				consumer.setDispatcherInfo(dispatcherInfo);
				consumerList.add(consumer);
				threadPoolExecutor.submit(consumer::beginConsume);
			}
		}
		return new Consumer<EventMessageDefaultConsumer>(consumerList);
	}
}

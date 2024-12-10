package com.mcinfotech.event.transmit.filter;

import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.transmit.inner.EventMessageProducer;

import io.netty.channel.ChannelHandlerContext;

/**
 * 从事件处理器接收发送的事情
 * 1.执行规则匹配，是否通过
 * 2.通过进行告警通知
 * 3.不通过结束

 */
@Component
@Order(1)
public class DefaultTransmitFilter implements IFilter<ProbeEventMessage> {
	private Logger logger = LogManager.getLogger(DefaultTransmitFilter.class);
	@Resource
	private EventMessageProducer producer;
	
	@Override
	public boolean chain(ProbeEventMessage message, ChannelHandlerContext ctx) {
		if(message!=null){
			producer.push(message);
			return false;
		}
		return true;
	}

	@Override
	public boolean chain(List<ProbeEventMessage> message, ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		producer.push(message);
		return false;
	}
}

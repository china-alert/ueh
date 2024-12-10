package com.mcinfotech.event.filter;

import java.util.List;

import com.mcinfotech.event.domain.ProbeInfo;

import io.netty.channel.ChannelHandlerContext;

/**
 * 对Dispatcher来的事件进行过滤，可以有多个过滤器
 * 1.接收校验，通过Probe(Probe Key)、Project(ID)进行校验,校验不通过的，废弃
 * 2.接收：校验通过的，存入内置队列，完成接收

 */
public interface IFilter<T> {
	/**
	 * 对消息进行过滤
	 * @param message 从监控工具来的消息
	 * @param ctx
	 * @return 返回值为true是还需要执行下个过滤，为false时不执行
	 */
	boolean chain(T message, ChannelHandlerContext ctx);
	/**
	 * 对消息进行过滤
	 * @param message 从监控工具来的消息
	 * @param ctx
	 * @return 返回值为true是还需要执行下个过滤，为false时不执行
	 */
	boolean chain(List<T> message, ChannelHandlerContext ctx);
}

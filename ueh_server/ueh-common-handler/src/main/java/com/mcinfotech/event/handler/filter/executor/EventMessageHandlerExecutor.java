package com.mcinfotech.event.handler.filter.executor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.UehEventMessage;
import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.handler.config.EventHandlerRuleConfig;
import com.mcinfotech.event.handler.domain.EventIntegratedProbe;
import com.mcinfotech.event.handler.filter.ProbeEventMessageHandler;
import com.mcinfotech.event.handler.filter.handler.EventRuleHandler;
import com.mcinfotech.event.handler.filter.handler.EventRuleHandlerContext;
import com.mcinfotech.event.handler.inner.EventMessageProducer;

import cn.mcinfotech.data.service.db.ColumnDefine;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * 从事件网关来的告警事件，已经做了按照Probe Key做了合并处理，确保到来的每一批事件都是同一个Probe Key

 *
 */
public abstract class EventMessageHandlerExecutor implements IFilter<UehEventMessage>{
	private Logger logger = LogManager.getLogger(EventMessageHandlerExecutor.class);
	@Resource
	EventHandlerRuleConfig eventHandlerRulesConfig;
	@Resource
	LoadingCache<String, ColumnDefine> columnDefineCache;
	@Resource
	LoadingCache<String, EventIntegratedProbe> eventIntegratedProbeDefineCache;
	@Resource
	private EventMessageProducer nextHandlerEventProducer;
	
	@Override
	public boolean chain(UehEventMessage message, ChannelHandlerContext ctx) {
		try{
			if(logger.isDebugEnabled()){
				logger.debug("message start to handler...");
			}
			doIt(message,eventIntegratedProbeDefineCache,columnDefineCache,eventHandlerRulesConfig,nextHandlerEventProducer);
			if(logger.isDebugEnabled()){
				logger.debug("message end to handler...");
			}
			return false;
		}catch(Exception e){
			logger.error("when message do handle , error has occurred , error detail is ",e);
			return false;
		}
	}
	@Override
	public boolean chain(List<UehEventMessage> messages, ChannelHandlerContext ctx) {
		try{
			if(logger.isDebugEnabled()){
				logger.debug("batch message start to handler...");
			}
			for(UehEventMessage message:messages) {
				doIt(message,eventIntegratedProbeDefineCache,columnDefineCache,eventHandlerRulesConfig,nextHandlerEventProducer);
			}
			if(logger.isDebugEnabled()){
				logger.debug("batch message end to handler...");
			}
			return false;
		}catch(Exception e){
			logger.error("when batch message do handle , error has occurred , error detail is ",e);
			return false;
		}
	}
	/**
	 * 1.事件处理前置函数,返回true继续其他的规则处理，返回false停止其他的规则处理
	 * 2.解析,字段映射、级别映射
	 * 2.按照事件接入设置的字段映射定义、级别映射定义进行事件解析
	 * 2.1读取接入配置的字段映射、级别映射
	 * 3.执行事件处理规则，包括合并、压缩、丰富、屏蔽等，也可能没有
	 * 3.1压缩规则处理
	 * 针对单台设备中一个或几个字段重复出现时，对最后发生事件、发生次数进行更新
	 * 3.2恢复规则处理
	 * 3.3.分组
	 * 3.4.升降级：一个事件源或者多个事件源，一个或者几个字段重复出现，对事件的告警级别进行升级或降级
	 * 3.5 屏蔽过滤：一个或多个事件源中，一个或者几个字段重复出现时，这类事件不产生告警通知，放到
	 * 3.5 丰富
	 * 3.5 做其他的定制操作
	 * 4.事件处理后函数，可以完成：入库、通知转发等操作
	 * 4.1通知
	 * @param uehEventMessage
	 * @param eventIntegratedProbeDefineCache
	 * @param columnDefineCache
	 * @param eventHandlerRulesConfig
	 * @param nextHandlerEventProducer
	 * @throws Exception
	 */
	private void doIt(UehEventMessage uehEventMessage,LoadingCache<String, EventIntegratedProbe> eventIntegratedProbeDefineCache,LoadingCache<String, ColumnDefine> columnDefineCache,EventHandlerRuleConfig eventHandlerRulesConfig,EventMessageProducer nextHandlerEventProducer) throws Exception {
		if(uehEventMessage.getProject()==null) {
			logger.warn("no project infomation setted , please check it .");
			return;
		}
		if(uehEventMessage.getProbe()==null) {
			logger.warn("no intergrated probe infomation setted , please check it .");
			return;
		}
		if(uehEventMessage.getMessageLenth()<1) {
			logger.warn("no event message transmmit , please check it .");
			return;
		}
		EventRuleHandlerContext context=new EventRuleHandlerContext(uehEventMessage.getProject(),uehEventMessage.getProbe(),eventIntegratedProbeDefineCache,columnDefineCache,eventHandlerRulesConfig);
		EventRuleHandler<UehEventMessage, Collection<Map<String, Object>>,EventRuleHandlerContext> pipeline=this.getBeforeRuleHandler()
				.add(this.getMappingRuleHandler())
				.add(this.getRichRuleHandler())
				.add(this.getCompressRuleHandler())
				.add(this.getRecoveryRuleHandler())
				.add(this.getDivideRuleHandler())
				.add(this.getUpOrDownRuleHandler())
				.add(this.getFilterRuleHandler())
				.add(this.getDoItHandler())
				.add(this.getAfterRuleHandler());
		Collection<Map<String,Object>> handleredMessage=pipeline.process(uehEventMessage,context);
		// 8.2通知
		Map<String, String> excludes = new HashMap<String, String>();
		excludes.put("FilterFlag", "NF#NN#NS");
		nextHandlerEventProducer.push(ProbeEventMessage.collectionToProbeEventMessage(uehEventMessage.getProject(), uehEventMessage.getProbe(), handleredMessage,excludes));
	}
	/**
	 * 待实现方法
	 */
	public abstract EventRuleHandler<UehEventMessage, UehEventMessage,EventRuleHandlerContext> getBeforeRuleHandler();
	public abstract EventRuleHandler<UehEventMessage, Collection<Map<String, Object>>,EventRuleHandlerContext> getMappingRuleHandler();
	public abstract EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getCompressRuleHandler();
	public abstract EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getRecoveryRuleHandler();
	public abstract EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getDivideRuleHandler();
	public abstract EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getUpOrDownRuleHandler();
	public abstract EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getFilterRuleHandler();
	public abstract EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getRichRuleHandler();
	public abstract EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getDoItHandler();
	public abstract EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>,EventRuleHandlerContext> getAfterRuleHandler();
}

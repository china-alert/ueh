package com.mcinfotech.event.handler.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.handler.config.EventHandlerRuleConfig;
import com.mcinfotech.event.handler.config.EventIntegratedProbeConfig;
import com.mcinfotech.event.handler.domain.EventIntegratedProbe;

import cn.mcinfotech.data.service.db.ColumnDefine;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.util.CollectionUtils;

/**
 * 在1.0中使用，后续废弃
 * 将从Dispatcher来的事件根据规则做事件处理,合并与过滤在其他地方实现
 * 1.接收校验，通过Probe(Probe Key)、Project(ID)进行校验,校验不通过的，废弃
 * 2.接收：校验通过的，进行事件规则处理：字段映射、级别映射
 * 3.压缩
 * 4.角色打标
 * 5.升降级

 */
@Deprecated
public abstract class ProbeEventMessageHandler implements IFilter<ProbeEventMessage> {
	private Logger logger = LogManager.getLogger(ProbeEventMessageHandler.class);
	@Resource
	ProbeInfo probeInfo;
	@Resource
	ProjectInfo projectInfo;
	@Resource
	EventIntegratedProbeConfig eventIntegratedProbeConfig;
	@Resource
	EventHandlerRuleConfig eventHandlerRulesConfig;
	
	@Override
	public boolean chain(ProbeEventMessage message, ChannelHandlerContext ctx) {
		/*//1.接收校验，通过Probe(Probe Key)、Project(ID)进行校验,校验不通过的，废弃
		if(message.getProbe().getKey().equalsIgnoreCase(probeInfo.getKey())&&message.getProject().getId()==projectInfo.getId()){
			if(logger.isDebugEnabled()){
				logger.debug(ctx.channel().remoteAddress() +"'s message has pushed queue .");
			}
			//2.接收：校验通过的，存入内置队列，完成接收
			producer.push(message);
		}*/
		return false;
	}
	@Override
	public boolean chain(List<ProbeEventMessage> messages, ChannelHandlerContext ctx) {
		try{
			if(logger.isDebugEnabled()){
				logger.debug("message start to handler...");
			}
			/*ProbeInfo probeInfo=null;			
			ProjectInfo projectInfo=null;			
			EventIntegratedProbeConfig eventIntegratedProbeConfig=null;
			EventHandlerRuleConfig eventHandlerRulesConfig=null;*/
			
			//1.事件处理前置函数,返回true继续其他的规则处理，返回false停止其他的规则处理
			if(!this.beforeRuleHandler(projectInfo,messages,ctx)){
				return false;
			}
			//this.initialize();
			List<ColumnDefine> ColumnDefines=this.getPlatformColumnDefine(projectInfo.getId());
			ProbeInfo eventIntegratedProbe=messages.get(0).getProbe();
			//2.解析,字段映射、级别映射
			//2.按照事件接入设置的字段映射定义、级别映射定义进行事件解析
			//2.1读取接入配置的字段映射、级别映射
			List<Map<String,Object>> handleredDatas=this.mapping(messages,eventIntegratedProbe,projectInfo,eventIntegratedProbeConfig);
			//ColumnDefineConfig.getColumnDefine(projectInfo.getId(), conditions);
			//3.执行事件处理规则，包括合并、压缩、丰富、屏蔽等，也可能没有
			//压缩和分组是不能缺少的规则
			//3.1丰富规则处理
			Collection<Map<String, Object>> richMessage=this.rich(handleredDatas, ColumnDefines, eventHandlerRulesConfig, eventIntegratedProbe, projectInfo);
			//3.1压缩规则处理
			//压缩：
			//针对单台设备中一个或几个字段重复出现时，对最后发生事件、发生次数进行更新
			Collection<Map<String, Object>> compressedMessage=this.compress(richMessage, eventHandlerRulesConfig, eventIntegratedProbe, projectInfo);
			//3.2恢复规则处理
			Collection<Map<String, Object>> recoveryMessage=this.recovery(compressedMessage, eventHandlerRulesConfig, eventIntegratedProbe, projectInfo);
			if(CollectionUtils.isEmpty(recoveryMessage)) return false;
			//3.2将事件消息与角色关联，对数据打标、入库，按照资源类别、资源进行分组
			Collection<Map<String,Object>> groupedMessage=this.divide(recoveryMessage, ColumnDefines, eventHandlerRulesConfig, eventIntegratedProbe, projectInfo);
			//3.3.升降级：一个事件源或者多个事件源，一个或者几个字段重复出现，对事件的告警级别进行升级或降级
			Collection<Map<String,Object>> downMessage=this.upOrDown(groupedMessage, ColumnDefines, eventHandlerRulesConfig, eventIntegratedProbe, projectInfo);
			//Collection<Map<String,Object>> downMessage=this.upOrDown(upMessage, ColumnDefines, eventHandlerRulesConfig, probeInfo, projectInfo);
			//3.4.屏蔽过滤
			Collection<Map<String, Object>> filterMessage=this.filter(downMessage, ColumnDefines, eventHandlerRulesConfig, eventIntegratedProbe, projectInfo);
//			Collection<Map<String, Object>> filterMessage=this.filter(downMessage, ColumnDefines, eventHandlerRulesConfig, probeInfo, projectInfo);
			//3.5 合并
			Collection<Map<String, Object>> combineMessage = this.combine(filterMessage, ColumnDefines, eventHandlerRulesConfig, eventIntegratedProbe, projectInfo);
			//
			Collection<Map<String, Object>> finalMessage=this.doIt(combineMessage, ColumnDefines, eventHandlerRulesConfig, eventIntegratedProbe, projectInfo);
			//4.事件处理后函数，可以完成：入库、通知转发等操作
			this.afterRuleHandler(projectInfo,eventIntegratedProbe,finalMessage);
//			this.afterRuleHandler(projectInfo,probeInfo,compressedMessage);
			//合并：一个或多个事件源中，一个或几个字段重复出现时，根据一个或者几个关键字对事件进行统计，可以查看每个事件
			//屏蔽过滤：一个或多个事件源中，一个或者几个字段重复出现时，这类事件不产生告警通知，放到
			if(logger.isDebugEnabled()){
				logger.debug("message end to handler...");
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	//public abstract void initialize(ProjectInfo project,ProbeInfo probeInfo,EventIntegratedProbeConfig eventIntegratedProbeConfig,EventHandlerRuleConfig eventHandlerRulesConfig);
	/**
	 * 事件处理前置处理函数
	 * @param projectInfo 项目信息
	 * @param messages 未经过规则处理的事件
	 * @param ctx 通道上下文
	 * @return false停止其他的规则处理，true继续其他的规则处理
	 */
	public abstract boolean beforeRuleHandler(ProjectInfo projectInfo,List<ProbeEventMessage> messages,ChannelHandlerContext ctx);
	/**
	 * 事件mapping
	 * 1.按照事件接入设置的字段映射定义、级别映射定义进行事件解析
	 * 2.取接入配置的字段映射、级别映射
	 * @param messages 原始事件列表
	 * @param probeInfo 处理器信息
	 * @param projectInfo 项目信息
	 * @param eventIntegratedProbeConfig 接入Probe操作类
	 * @return 与平台定义的事件字段映射之后的事件
	 */
	public abstract List<Map<String,Object>> mapping(List<ProbeEventMessage> messages,ProbeInfo probeInfo,ProjectInfo projectInfo,EventIntegratedProbeConfig eventIntegratedProbeConfig);
	/**
	 * 查询平台定义的事件字段
	 * @param projectId 项目ID
	 * @return 平台定义的事件字段
	 */
	public abstract List<ColumnDefine> getPlatformColumnDefine(long projectId);
	/**
	 * 查询平台定义的事件接入Probe
	 * @param projectId 项目ID
	 * @return 平台定义的事件字段
	 */
	//public abstract EventIntegratedProbe getEventIntegratedProbeDefine(long projectId);

	/**
	 * 对mapping之后的事件进行压缩
	 * @param rawData
	 * @param eventHandlerRulesConfig
	 * @param probeInfo
	 * @param projectInfo
	 * @return
	 */
	public abstract Collection<Map<String, Object>> rich(Collection<Map<String, Object>> rawData,
														 List<ColumnDefine> ColumnDefines, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo);

	/**
	 * 对mapping之后的事件进行压缩
	 * @param mappingData mapping之后的事件
	 * @param eventHandlerRulesConfig 压缩规则
	 * @param probeInfo 处理器信息
	 * @param projectInfo 项目信息
	 * @return 压缩过的事件
	 */
	public abstract Collection<Map<String, Object>> compress(Collection<Map<String,Object>> mappingData,EventHandlerRuleConfig eventHandlerRulesConfig,ProbeInfo probeInfo,ProjectInfo projectInfo);

	/**
	 * 对压缩之后符合条件得事件进行恢复
	 * @param compressedMessage
	 * @param eventHandlerRulesConfig
	 * @param probeInfo
	 * @param projectInfo
	 * @return
	 */
	protected abstract Collection<Map<String, Object>> recovery(Collection<Map<String, Object>> compressedMessage, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo);
	/**
	 * 对事件进行分组打标
	 * @param rawData 压缩过的事件
	 * @param ColumnDefines 平台事件字段
	 * @param eventHandlerRulesConfig 分组规则
	 * @param probeInfo 处理器信息
	 * @param projectInfo 项目信息
	 * @return 分组打标过的事件
	 */
	public abstract Collection<Map<String, Object>> divide(Collection<Map<String, Object>> rawData,List<ColumnDefine> ColumnDefines,EventHandlerRuleConfig eventHandlerRulesConfig,ProbeInfo probeInfo,ProjectInfo projectInfo);
	/**
	 * 对事件升降级处理
	 * @param rawData 分组打标过的事件
	 * @param ColumnDefines 平台事件字段
	 * @param eventHandlerRulesConfig 升降级规则
	 * @param probeInfo 处理器信息
	 * @param projectInfo 项目信息
	 * @return 升降级过的事件
	 * @return
	 */
	public abstract Collection<Map<String, Object>> upOrDown(Collection<Map<String, Object>> rawData,List<ColumnDefine> ColumnDefines,EventHandlerRuleConfig eventHandlerRulesConfig,ProbeInfo probeInfo,ProjectInfo projectInfo);
	/**
	 * 对事件屏蔽处理
	 * @param rawData 升降级过的事件
	 * @param ColumnDefines 平台事件字段
	 * @param eventHandlerRulesConfig 屏蔽规则
	 * @param probeInfo 处理器信息
	 * @param projectInfo 项目信息
	 * @return 屏蔽过的事件
	 * @return
	 */
	public abstract Collection<Map<String, Object>> filter(Collection<Map<String, Object>> rawData,List<ColumnDefine> ColumnDefines,EventHandlerRuleConfig eventHandlerRulesConfig,ProbeInfo probeInfo,ProjectInfo projectInfo);

	/**
	 * 对事件进行合并处理
	 * @param rawData 过滤后的事件
	 * @param ColumnDefines 平台事件字段
	 * @param eventHandlerRulesConfig 合并规则
	 * @param probeInfo 处理器信息
	 * @param projectInfo 项目信息
	 * @return 合并过的事件
	 */
	public abstract Collection<Map<String, Object>> combine(Collection<Map<String, Object>> rawData,List<ColumnDefine> ColumnDefines,EventHandlerRuleConfig eventHandlerRulesConfig,ProbeInfo probeInfo,ProjectInfo projectInfo);
	/**
	 * 其他的事件处理
	 * @param rawData 升降级过的事件
	 * @param ColumnDefines 平台事件字段
	 * @param eventHandlerRulesConfig 屏蔽规则
	 * @param probeInfo 处理器信息
	 * @param projectInfo 项目信息
	 * @return 屏蔽过的事件
	 * @return
	 */
	public abstract Collection<Map<String, Object>> doIt(Collection<Map<String, Object>> rawData,List<ColumnDefine> ColumnDefines,EventHandlerRuleConfig eventHandlerRulesConfig,ProbeInfo probeInfo,ProjectInfo projectInfo);
	/**
	 * 事件后处理函数
	 * @param projectInfo 项目信息
	 * @param rawData 经过规则处理的事件
	 */
	public abstract void afterRuleHandler(ProjectInfo projectInfo,ProbeInfo probeInfo,Collection<Map<String,Object>> rawData);
	
	public void setProbeInfo(ProbeInfo probeInfo) {
		this.probeInfo = probeInfo;
	}
	public void setProjectInfo(ProjectInfo projectInfo) {
		this.projectInfo = projectInfo;
	}
	public void setEventIntegratedProbeConfig(EventIntegratedProbeConfig eventIntegratedProbeConfig) {
		this.eventIntegratedProbeConfig = eventIntegratedProbeConfig;
	}
	public void setEventHandlerRulesConfig(EventHandlerRuleConfig eventHandlerRulesConfig) {
		this.eventHandlerRulesConfig = eventHandlerRulesConfig;
	}
	public EventHandlerRuleConfig getEventHandlerRulesConfig() {
		return eventHandlerRulesConfig;
	}
	public ProbeInfo getProbeInfo() {
		return probeInfo;
	}
	public ProjectInfo getProjectInfo() {
		return projectInfo;
	}
	public EventIntegratedProbeConfig getEventIntegratedProbeConfig() {
		return eventIntegratedProbeConfig;
	}
}

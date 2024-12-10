package com.mcinfotech.event.transmit.push;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.mcinfotech.event.domain.EventSourceType;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.handler.config.EventHandlerRuleConfig;
import com.mcinfotech.event.handler.config.EventIntegratedProbeConfig;
import com.mcinfotech.event.handler.config.PlatformEventColumnConfig;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventHandlerRuleEffect;
import com.mcinfotech.event.handler.domain.PlatformEventColumn;
import com.mcinfotech.event.push.IPusher;
import com.mcinfotech.event.transmit.algorithm.EventNotify;
import com.mcinfotech.event.transmit.config.CandidateConfig;
import com.mcinfotech.event.transmit.config.MediaTypeConfig;
import com.mcinfotech.event.transmit.domain.Candidate;
import com.mcinfotech.event.transmit.domain.MediaInfo;
import com.mcinfotech.event.transmit.domain.MediaType;
import com.mcinfotech.event.transmit.utils.SendmailUtil;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.db.ColumnDefine;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

/**
 * 通知转发

 */

public class EmailPusher implements IPusher<ProbeEventMessage> {
	private static Logger logger=LogManager.getLogger(EmailPusher.class);
	@Resource
	DataSource dataSource;
	@Resource
	ProbeInfo probeInfo;
	@Resource
	ProjectInfo projectInfo;
	@Resource
	EventIntegratedProbeConfig eventIntegratedProbeConfig;
	@Resource
	EventHandlerRuleConfig eventHandlerRulesConfig;
	@Resource
	PlatformEventColumnConfig ColumnDefineConfig;
	@Resource
	CandidateConfig candidateConfig;
	@Resource
	MediaTypeConfig notifySettingsConfig;
	
	private AtomicLong messageCount=new AtomicLong(0);
	@Override
	public void push(ProbeEventMessage message) {
	}
	@Override
	public void push(String handlerType,List<ProbeEventMessage> messages) {
		/*DeliveryInfo routingInfo=this.deliveryInfo.getDelivery().get(handlerType);
		if(routingInfo!=null&&routingInfo.isEnable()){
			if(logger.isDebugEnabled()){
				logger.debug("pusher start to work and push message to "+routingInfo.getHost()+" via port "+routingInfo.getPort());
			}
			messageCount.set(0l);
			DispatcherHolder.flushToDelivery(routingInfo.getType(),messages,routingInfo.getHost(),routingInfo.getPort());
		}else{
			if(logger.isDebugEnabled()){
				logger.debug("notice not start , message transmit stop ."+messageCount.incrementAndGet());
			}
		}*/
	}

	@Override
	public void push(String handlerType, ProbeEventMessage message) {
		/*DeliveryInfo routingInfo=this.deliveryInfo.getDelivery().get(handlerType);
		if(routingInfo!=null&&routingInfo.isEnable()){
			if(logger.isDebugEnabled()){
				logger.debug("pusher start to work and push message to "+routingInfo.getHost()+" via port "+routingInfo.getPort());
			}
			messageCount.set(0l);
			DispatcherHolder.flushToDelivery(routingInfo.getType(),message,routingInfo.getHost(),routingInfo.getPort());
		}else{
			if(logger.isDebugEnabled()){
				logger.debug("notice not start , message transmit stop ."+messageCount.incrementAndGet());
			}
		}*/
	}
	@Override
	public void push(List<ProbeEventMessage> messages) {
		Map<String,Object> conditions=new HashMap<String,Object>();
		conditions.put("isEnable", "Y");
		List<ColumnDefine> columnDefines=ColumnDefineConfig.getPlatformEventColumn(this.projectInfo.getId(), conditions);
		EventHandlerRule rule=this.eventHandlerRulesConfig.getEventHandlerRule(projectInfo.getId(),null, EventSourceType.INTERNAL,"N");
		for(ProbeEventMessage message:messages){
			Map<String,Object> mapMessage=FastJsonUtils.stringToCollect(message.getMessageBody());
			if(mapMessage.containsKey("events")){
				//if(EventNotice.excute(mapMessage.get("events"), columnMappings, rule))
				List<Map<String,Object>> events=(List<Map<String, Object>>) mapMessage.get("events");
				List<String> notifyTypes=(List<String>) mapMessage.get("notifyType");
				String remark=mapMessage.get("remark")==null?"":mapMessage.get("remark").toString();
				ProjectInfo projectInfo=message.getProject();
				for(Map<String,Object> event:events){
					if(StringUtils.isNotEmpty(remark)){
						event.put("remark", remark);
					}
					this.notify(projectInfo,notifyTypes,null, event);
				}
			}else{
				Object filterFlagObject=mapMessage.get(PlatformEventColumn.FIXED_COLUMN_FILTER_FLAG);
				String filterFlagValue=filterFlagObject==null?"":filterFlagObject.toString();
				Set<String> excludeMediaTypes=new HashSet<String>();
				if(StringUtils.isNotEmpty(filterFlagValue)&&!filterFlagValue.equalsIgnoreCase("NA")&&!filterFlagValue.equalsIgnoreCase("[]")){
					List<String> filterTypes=FastJsonUtils.toList(filterFlagValue, String.class);
					for(String filterType:filterTypes){
						if(filterType.equalsIgnoreCase("NS")){
							excludeMediaTypes.add("share");
						}else if(filterType.equalsIgnoreCase("NN")){
							excludeMediaTypes.add("email");
							excludeMediaTypes.add("wechat");
							excludeMediaTypes.add("nail");
						}else if(filterType.equalsIgnoreCase("NF")){
							excludeMediaTypes.add("flow");
						}
					}
					
				}
				if(EventNotify.excute(mapMessage,ColumnDefine.toMap(columnDefines),rule)){
					this.notify(projectInfo,rule,excludeMediaTypes,mapMessage);
				}
			}
		}
	}
	/*public void executeNotify(ProjectInfo projectInfo,List<String> notifyTypes,Map<String,Object> message){
		this.notify(projectInfo, notifyTypes, message);
	}*/
	
	/**
	 * 接收告警数据，解析处理发送邮件通知
	 * @param message
	 * @return
	 */
	public void notify(ProjectInfo projectInfo,EventHandlerRule rule,Set<String> excludeMediaTypes,Map<String, Object> message) {
		if(message==null) return;
		//1.assemble data
		//通知方式,根据通知规则查找发送介质：
		List<EventHandlerRuleEffect> effects=FastJsonUtils.toList(rule.getEffect(), EventHandlerRuleEffect.class);
		List<String> mediaTypes=new ArrayList<String>();
		for(EventHandlerRuleEffect effect:effects){
			mediaTypes.addAll(FastJsonUtils.toList(effect.getEffectValue(), String.class));
		}
		notify(projectInfo,mediaTypes,excludeMediaTypes,message);
	}
	public void notify(ProjectInfo projectInfo,List<String> mediaTypes,Set<String> excludeMediaTypes,Map<String, Object> message) {
		if(message==null) return;
		if(CollectionUtils.isEmpty(mediaTypes)) {
			logger.warn("no candidated media type exist.");
			return;
		}
		Set<String> mergedMediaTypes=new HashSet<String>();
		mergedMediaTypes.addAll(mediaTypes);
		if(CollectionUtils.isNotEmpty(excludeMediaTypes)){
			for(String mediaType:mediaTypes){
				if(excludeMediaTypes.contains(mediaType)){
					mergedMediaTypes.remove(mediaType);
				}
			}
		}
		if(CollectionUtils.isEmpty(mergedMediaTypes)){
			logger.warn("waiting to send message :");
			logger.warn(message);
			logger.warn("no media matched ...");
			return;
		}
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("mediaTypes", mergedMediaTypes);
		List<MediaInfo> medias=this.notifySettingsConfig.getSendServiceInfo(projectInfo.getId(), filter);
		if(medias!=null){
			for(MediaInfo media:medias){
				try {
					//准备发送内容
					//String emailBody=media.getMediaTemplet();
					//查询发送对象，根据事件的EventCategory、节点查找设备的负责人
					//根据事件的EventCategory查找改事情对应的角色，然后根据角色查找负责人
					String deviceCategory=(String) message.get("EventCategory");
					String node=(String) message.get("Node");
					Map<String,Object> params=new HashMap<String,Object>();
					params.put("categoryName", deviceCategory);
					params.put("deviceKey", node);
					params.put("status", '0');
					params.put("isEnable", 'Y');
					//2.查找候选人，发送
					List<Candidate> candidates=this.candidateConfig.getCandidates(this.projectInfo.getId(), params);
					//群发
					if(CollectionUtils.isNotEmpty(candidates)){
						if(media.getMediaType()==MediaType.WECHAT){
							System.out.println("wechat not implements...........................................");
						}else if(media.getMediaType()==MediaType.FLOW){
							System.out.println("flow not implements.............................................");
						}else if(media.getMediaType()==MediaType.NAIL){
							System.out.println("nail not implements.............................................");
						}else{
							//查询SMTP邮件服务
							MediaInfo sendServiceInfo=this.notifySettingsConfig.getSendServiceInfo(projectInfo.getId(),"email");
							//解析邮箱服务信息
							JSONObject mediaSettings=JSONObject.parseObject(sendServiceInfo.getMediaSettings());
							//设置发送SMTP服务
							boolean isSSL=StringUtils.isEmpty(mediaSettings.getString("is_ssl"))?false:mediaSettings.getString("is_ssl").equalsIgnoreCase("Y")?true:false;
							String mailserver = mediaSettings.getString("mail_server");//邮箱的 SMTP 服务器地址为: smtp.qq.com
							int mailserverPort=new Integer(mediaSettings.getString("mail_server_port"));
							String fromEmail = mediaSettings.getString("from_mail");//发件人邮箱
							String fromEmailPassword = mediaSettings.getString("from_mail_password");//发件人邮箱密码（授权码）
							List<String> toMails=new ArrayList<String>();
							for(Candidate candidate:candidates){
								toMails.add(candidate.getEmail());
							}
							//设置发送信息
							//装配邮件正文
							//发送
							//SendmailUtil.sendEmail(toMails,null,"告警事件通知",this.buildBody(sendServiceInfo.getMediaTemplet(),message),mailserver,mailserverPort,isSSL,fromEmail,"事件通知",fromEmailPassword);
						}
					}else{
						logger.warn("no candidate !");
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	private String buildBody(String templet,Map<String,Object> params) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException{
		String firstOccurrenceStr = String.valueOf(params.get("FirstOccurrence"));
		String LastOccurrenceStr = String.valueOf(params.get("LastOccurrence"));
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(StringUtils.isNotEmpty(firstOccurrenceStr)) {
			long firstOccurrence = Long.parseLong(firstOccurrenceStr);
			params.put("FirstOccurrence",simpleDateFormat.format(new Date(firstOccurrence)));
		}
		if(StringUtils.isNotEmpty(LastOccurrenceStr)) {
			long lastOccurrence = Long.parseLong(LastOccurrenceStr);
			params.put("LastOccurrence",simpleDateFormat.format(new Date(lastOccurrence)));
		}
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		stringLoader.putTemplate("emailContents",templet);
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
		cfg.setNumberFormat("#");
		cfg.setTemplateLoader(stringLoader);
		Writer out=new StringWriter(4096);
		Template template = cfg.getTemplate("emailContents");
		template.process(params, out);
		return out.toString();
	}
}

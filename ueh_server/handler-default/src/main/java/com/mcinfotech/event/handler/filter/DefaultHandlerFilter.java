package com.mcinfotech.event.handler.filter;

import cn.mcinfotech.data.service.db.ColumnDefine;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.handler.algorithm.*;
import com.mcinfotech.event.handler.config.EventHandlerRuleConfig;
import com.mcinfotech.event.handler.config.EventIntegratedProbeConfig;
import com.mcinfotech.event.handler.config.PlatformEventColumnConfig;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventIntegratedProbe;
import com.mcinfotech.event.handler.domain.PlatformProbeColumnMapping;
import com.mcinfotech.event.handler.domain.PlatformProbeSeverityMapping;
import com.mcinfotech.event.handler.inner.EventMessageProducer;
import com.mcinfotech.event.manage.EventManage;
import com.mcinfotech.event.utils.FastJsonUtils;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;

/**
 * 将从Dispatcher来的事件根据规则做事件处理,合并与过滤在其他地方实现
 * 1.接收校验，通过Probe(Probe Key)、Project(ID)进行校验,校验不通过的，废弃
 * 2.接收：校验通过的，进行事件规则处理：字段映射、级别映射
 * 3.压缩
 * 4.角色打标
 * 5.升降级
 */
@Component
@Order(1)
public class DefaultHandlerFilter extends ProbeEventMessageHandler {
    private Logger logger = LogManager.getLogger(ProbeEventMessageHandler.class);
    @Resource
    private EventMessageProducer producer;
    @Resource
    DataSource dataSource;
    @Resource
    ProbeInfo probe;
    @Resource
    PlatformEventColumnConfig ColumnDefineConfig;

    @Override
    public boolean beforeRuleHandler(ProjectInfo projectInfo, List<ProbeEventMessage> messages, ChannelHandlerContext ctx) {
        return true;
    }

    @Override
    public List<Map<String, Object>> mapping(List<ProbeEventMessage> messages, ProbeInfo probeInfo, ProjectInfo projectInfo, EventIntegratedProbeConfig eventIntegratedProbeConfig) {
        List<Map<String, Object>> handleredDatas = new ArrayList<Map<String, Object>>();
        for (ProbeEventMessage message : messages) {
            //1.接收校验，通过Probe(Probe Key)、Project(ID)进行校验,校验不通过的，废弃
            //每个List应该是同一Probe、Project,尚未验证
            EventIntegratedProbe integratedProbe = this.eventIntegratedProbeConfig.getEventIntegratedProbe(projectInfo.getId(), message.getProbe().getKey());
            if (integratedProbe == null) {
                logger.warn("invalid message , not found integrated probe , error message is " + message);
                continue;
            }
            if (integratedProbe.getColumnMapping() == null) {
                logger.warn("invalid message , integrated probe column's mapping is not setup , error message is " + message);
                continue;
            }
            if (integratedProbe.getSeverityMapping() == null) {
                logger.warn("invalid message , integrated probe severity's mapping is not setup , error message is " + message);
                continue;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("mapping columns " + integratedProbe.getColumnMapping());
                logger.debug("mapping severity :" + integratedProbe.getSeverityMapping());
            }
            PlatformProbeColumnMapping columnsSettings = FastJsonUtils.toBean(integratedProbe.getColumnMapping(), PlatformProbeColumnMapping.class);
            PlatformProbeSeverityMapping severitySettings = FastJsonUtils.toBean(integratedProbe.getSeverityMapping(), PlatformProbeSeverityMapping.class);
            Map<String, Object> eventMessage = (Map) FastJsonUtils.stringToCollect(message.getMessageBody());
            eventMessage.put("RefIntegratedRules", integratedProbe.getName());
            handleredDatas.add(EventMapping.excute(eventMessage, columnsSettings, severitySettings));
        }
        return handleredDatas;
    }

    @Override
    public List<ColumnDefine> getPlatformColumnDefine(long projectId) {
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("isEnable", "Y");
        return ColumnDefineConfig.getPlatformEventColumn(projectInfo.getId(), conditions);
    }

    @Override
    public Collection<Map<String, Object>> rich(Collection<Map<String, Object>> rawData,
                                                List<ColumnDefine> ColumnDefines, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo) {
        List<EventHandlerRule> richRules = this.eventHandlerRulesConfig.getEventHandlerRules(projectInfo.getId(), probeInfo.getKey(), null, "R");
        return EventRich.excuteMultiple(rawData, richRules, dataSource, projectInfo.getId());
    }

    @Override
    public Collection<Map<String, Object>> compress(Collection<Map<String, Object>> mappingData, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo) {
        //拿优先级最高的一条压缩
        List<EventHandlerRule> compressRules = this.eventHandlerRulesConfig.getRules(projectInfo.getId(), probeInfo.getKey(), probeInfo.getEventSourceType(), "Z");
        List<EventHandlerRule> recoveryRules = this.eventHandlerRulesConfig.getRules(projectInfo.getId(), probeInfo.getKey(), probeInfo.getEventSourceType(), "RE");
//        EventHandlerRule eventHandlerRule = null;
//        if (CollectionUtils.isNotEmpty(recoveryRules)) eventHandlerRule = recoveryRules.get(0);
//		EventHandlerRule eventHandlerRule = null;
//		if (CollectionUtils.isNotEmpty(compressRules)) eventHandlerRule=compressRules.get(0);
//		EventHandlerRule compressRule=this.eventHandlerRulesConfig.getEventHandlerRule(projectInfo.getId(),probeInfo.getKey(), probeInfo.getEventSourceType(),"Z");
        return EventCompress.defaultExcute(mappingData, compressRules, dataSource, projectInfo.getId(), recoveryRules);
    }

    @Value("${componet.status}")
    String componetStatus;

    @Override
    protected Collection<Map<String, Object>> recovery(Collection<Map<String, Object>> compressedMessage, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo) {
        //拿优先级最高的一条恢复
        List<EventHandlerRule> recoveryRules = this.eventHandlerRulesConfig.getRules(projectInfo.getId(), probeInfo.getKey(), probeInfo.getEventSourceType(), "RE");
        return EventRecovery.excute(compressedMessage, recoveryRules, dataSource, projectInfo, componetStatus);
    }

    @Override
    public Collection<Map<String, Object>> divide(Collection<Map<String, Object>> rawData, List<ColumnDefine> ColumnDefines, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo) {
        List<EventHandlerRule> fillRules = this.eventHandlerRulesConfig.getEventHandlerRules(projectInfo.getId(), probeInfo.getKey(), null, "G");//事件分组不用eventSourceType区分，传null
        return EventDivide.excute(rawData, ColumnDefine.toMap(ColumnDefines), fillRules, dataSource);
    }

    @Override
    public Collection<Map<String, Object>> upOrDown(Collection<Map<String, Object>> rawData, List<ColumnDefine> ColumnDefines, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo) {
        //升级
//		EventHandlerRule upRule= this.eventHandlerRulesConfig.getEventHandlerRule(projectInfo.getId(),probeInfo.getKey(), probeInfo.getEventSourceType(),"U");
//		Collection<Map<String, Object>> upMessage=EventSeverityUpOrDown.excute(rawData,ColumnDefine.toMap(ColumnDefines),upRule);
        List<EventHandlerRule> upRules = this.eventHandlerRulesConfig.getRules(projectInfo.getId(), probeInfo.getKey(), probeInfo.getEventSourceType(), "U");
        return EventSeverityUpOrDown.excute(rawData, ColumnDefine.toMap(ColumnDefines), upRules);
        //降级
//		EventHandlerRule downRule=this.eventHandlerRulesConfig.getEventHandlerRule(projectInfo.getId(),probeInfo.getKey(), probeInfo.getEventSourceType(),"D");
//		return EventSeverityUpOrDown.excute(upMessage,ColumnDefine.toMap(ColumnDefines),downRule);
//		List<EventHandlerRule> downRules = this.eventHandlerRulesConfig.getRules(projectInfo.getId(), probeInfo.getKey(), probeInfo.getEventSourceType(), "D");
//		return EventSeverityUpOrDown.excute(upMessage,ColumnDefine.toMap(ColumnDefines),downRules);
    }

    @Override
    public Collection<Map<String, Object>> filter(Collection<Map<String, Object>> rawData, List<ColumnDefine> ColumnDefines, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo) {
//		EventHandlerRule filterRule=this.eventHandlerRulesConfig.getEventHandlerRule(projectInfo.getId(),probeInfo.getKey(), probeInfo.getEventSourceType(),"F");
        List<String> ruleTypes = Arrays.asList("F", "MP");
        List<EventHandlerRule> filterRules = this.eventHandlerRulesConfig.getRules(projectInfo.getId(), probeInfo.getKey(), probeInfo.getEventSourceType(), ruleTypes);
        return EventFilter.excute(rawData, ColumnDefine.toMap(ColumnDefines), filterRules);
    }

    @Override
    public Collection<Map<String, Object>> combine(Collection<Map<String, Object>> rawData, List<ColumnDefine> ColumnDefines, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo) {
        List<EventHandlerRule> combineRules = this.eventHandlerRulesConfig.getEventHandlerRules(projectInfo.getId(), probeInfo.getKey(), null, "C");//事件合并不用eventSourceType区分，传null

        return EventCombine.excute(rawData, ColumnDefine.toMap(ColumnDefines), combineRules);
    }

    @Override
    public Collection<Map<String, Object>> doIt(Collection<Map<String, Object>> rawData,
                                                List<ColumnDefine> ColumnDefines, EventHandlerRuleConfig eventHandlerRulesConfig, ProbeInfo probeInfo, ProjectInfo projectInfo) {
//        List<EventHandlerRule> richRules = this.eventHandlerRulesConfig.getEventHandlerRules(projectInfo.getId(), probeInfo.getKey(), null, "R");
//        return EventRich.excuteMultiple(rawData, ColumnDefine.toMap(ColumnDefines), richRules, dataSource, projectInfo.getId());
//        return EventRich.excute(rawData, ColumnDefine.toMap(ColumnDefines), richRules, dataSource, projectInfo.getId());
        return rawData;
    }

    @Override
    public void afterRuleHandler(ProjectInfo projectInfo, ProbeInfo probeInfo, Collection<Map<String, Object>> rawMessages) {
        //8.入库,并通知转发
        //8.1入库
        if (CollectionUtils.isEmpty(rawMessages)) {
            return;
        }
        //处理完毕的事件同时入实时表和历史表
        EventManage.createEvent(dataSource, projectInfo.getId(), rawMessages);
        //8.2通知
        Map<String, String> excludes = new HashMap<String, String>();
        excludes.put("FilterFlag", "NF#NN#NS");
        producer.push(ProbeEventMessage.collectionToProbeEventMessage(projectInfo, probeInfo, rawMessages, excludes));
    }
}

package com.mcinfotech.event.dispatcher.inner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Queues;
import com.mcinfotech.event.dispatcher.domain.DispatcherInfo;
import com.mcinfotech.event.dispatcher.domain.RoutingTableInfo;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.domain.ProtocolVersion;
import com.mcinfotech.event.domain.UehEventMessage;
import com.mcinfotech.event.listener.IListener;

/**
 * 事件消费
 * 按照事件源类型进行消费，消费策略：按数量或者时间周期进行消费，任何一个先到都会触发消费
 * 发送给Handler的事件需要按照Probe Key进行合并，然后再发出
 *

 */

public class EventMessageDefaultConsumer {
    private Logger logger = LogManager.getLogger(EventMessageDefaultConsumer.class);
    private IListener<UehEventMessage> eventMessageV2Listener;
    private IListener<ProbeEventMessage> eventMessageV1Listener;
    //private String currentEventRoutingName;
    private RoutingTableInfo currentRouteTableInfo;
    private DispatcherInfo dispatcherInfo;

    /**
     * 目前只接收同一个Project的高警事件，不进行跨Project事件
     */
    //private ProjectInfo projectInfo;
    public void beginConsume() {
        while (true) {
            try {
                List<ProbeEventMessage> messages = new ArrayList<ProbeEventMessage>();
                Queues.drain(InnerConfig.TABLE.get(this.currentRouteTableInfo.getType()), messages, dispatcherInfo.getBatch(), dispatcherInfo.getInterval(), TimeUnit.SECONDS);
                if (logger.isDebugEnabled()) {
                    logger.debug("fetch " + this.currentRouteTableInfo.getType() + "'s message , size is " + messages.size());
                }
                if (messages.size() > 0) {
                    //目前只有Cascade处理器采用的UehEventMessage，其他的还没有做切换
                    if (this.currentRouteTableInfo.getProtocolVersion() == ProtocolVersion.V2) {
                        //将消息按照Probe Key:ProbeEventMessage列表方式重新组合，每次发过去的消息同一个ProbeKey
                        Map<String, List<String>> recombinedMessages = new HashMap<>();
                        Map<String, ProbeInfo> probeInfos = new HashMap<>();
                        Map<String, ProjectInfo> projectInfos = new HashMap<>();
                        messages.forEach(message -> {
                            recombinedMessages.computeIfAbsent(message.getProbe().getKey(), ml -> new ArrayList<>()).add(message.getMessageBody());
                            probeInfos.putIfAbsent(message.getProbe().getKey(), message.getProbe());
                            projectInfos.putIfAbsent(message.getProbe().getKey(), message.getProject());
                        });
                        for (String probeKey : recombinedMessages.keySet()) {
                            UehEventMessage innerMessage = new UehEventMessage();
                            innerMessage.setMessageLenth(recombinedMessages.get(probeKey).size());
                            innerMessage.setMessages(recombinedMessages.get(probeKey));
                            innerMessage.setProbe(probeInfos.get(probeKey));
                            innerMessage.setProject(projectInfos.get(probeKey));
                            innerMessage.setTransactionId(UUID.randomUUID());
                            innerMessage.setStatus("toHandler");
                            eventMessageV2Listener.dispatcher(innerMessage);
                        }
                    } else {
                        Map<String, List<ProbeEventMessage>> recombinedMessages = new HashMap<>();
                        for (ProbeEventMessage message : messages) {
                            List<ProbeEventMessage> eventMessages = new ArrayList<ProbeEventMessage>();
                            if (recombinedMessages.containsKey(message.getProbe().getKey())) {
                                eventMessages = recombinedMessages.get(message.getProbe().getKey());
                            }
                            eventMessages.add(message);
                            recombinedMessages.put(message.getProbe().getKey(), eventMessages);
                        }
                        for (String key : recombinedMessages.keySet()) {
                            eventMessageV1Listener.dispatcher(this.currentRouteTableInfo.getType(), recombinedMessages.get(key));
                        }
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (Exception ie) {
                    logger.error(ie.getMessage(), ie);
                }
                logger.error(e.getMessage(), e);
            }
        }
    }


    public void setDispatcherInfo(DispatcherInfo dispatcherInfo) {
        this.dispatcherInfo = dispatcherInfo;
    }

    public RoutingTableInfo getCurrentRouteTableInfo() {
        return currentRouteTableInfo;
    }

    public void setCurrentRouteTableInfo(RoutingTableInfo currentRouteTableInfo) {
        this.currentRouteTableInfo = currentRouteTableInfo;
    }


    public IListener<UehEventMessage> getEventMessageV2Listener() {
        return eventMessageV2Listener;
    }


    public void setEventMessageV2Listener(IListener<UehEventMessage> eventMessageV2Listener) {
        this.eventMessageV2Listener = eventMessageV2Listener;
    }


    public IListener<ProbeEventMessage> getEventMessageV1Listener() {
        return eventMessageV1Listener;
    }


    public void setEventMessageV1Listener(IListener<ProbeEventMessage> eventMessageV1Listener) {
        this.eventMessageV1Listener = eventMessageV1Listener;
    }
}

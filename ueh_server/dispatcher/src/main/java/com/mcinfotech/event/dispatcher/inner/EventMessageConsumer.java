package com.mcinfotech.event.dispatcher.inner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Queues;
import com.mcinfotech.event.dispatcher.domain.DispatcherInfo;
import com.mcinfotech.event.dispatcher.domain.RoutingTable;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.listener.IListener;

/**
 * 事件消费
 * 按照事件源类型进行消费，消费策略：按数量或者时间周期进行消费，任何一个先到都会触发消费
 * 发送给Handler的事件需要按照Probe Key进行合并，然后再发出
 *

 */
@Deprecated
public class EventMessageConsumer {
    private Logger logger = LogManager.getLogger(EventMessageConsumer.class);
    private IListener<ProbeEventMessage> probeEventMessageListener;
    private RoutingTable routingTable;
    private DispatcherInfo dispatcherInfo;

    public void beginConsume() {
        while (true) {
            try {
                for (String handlerType : routingTable.getRoutingTable().keySet()) {
                    List<ProbeEventMessage> messages = new ArrayList<ProbeEventMessage>();
                    Queues.drain(InnerConfig.TABLE.get(handlerType), messages, dispatcherInfo.getBatch(), dispatcherInfo.getInterval(), TimeUnit.SECONDS);
                    if (logger.isDebugEnabled()) {
                        logger.debug("fetch " + handlerType + "'s message , size is " + messages.size());
                    }
                    if (messages.size() > 0) {
                        //将消息按照Probe Key:ProbeEventMessage列表方式重新组合，每次发过去的消息同一个ProbeKey
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
                            probeEventMessageListener.dispatcher(handlerType, recombinedMessages.get(key));
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

    public void setProbeEventMessageListener(IListener<ProbeEventMessage> eventProbeMessageListener) {
        this.probeEventMessageListener = eventProbeMessageListener;
    }

    public void setRoutingTable(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    public void setDispatcherInfo(DispatcherInfo dispatcherInfo) {
        this.dispatcherInfo = dispatcherInfo;
    }
}

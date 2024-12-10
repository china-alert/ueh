package com.mcinfotech.event.probe.filter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.probe.inner.EventMessageProducer;
import com.mcinfotech.event.utils.FastJsonUtils;

import io.netty.channel.ChannelHandlerContext;

/**
 * 将从监控工具来的事件发送到队列,送至队列由EventMessageProducer完成
 * 根据zabbix传来的事件消息，做如下检测
 * 1.检测传来的是否是JSON字符串，
 * 2.检测probeKey是否存在，
 * 3.检测probeKey在配置中是否存在（暂时不实现）
 * 通过检查的消息才能发送EventMessageProducer.
 */
@Component
@Order(1)
public class RawEventMessageFilter implements IFilter<String> {
	private Logger logger = LogManager.getLogger(RawEventMessageFilter.class);
	@Resource
	private EventMessageProducer producer;
	@Autowired
	ProjectInfo project;
	@Autowired
	ProbeInfo probe;

	@Override
	public boolean chain(String message, ChannelHandlerContext ctx) {
		String probeKey=(String)FastJsonUtils.extractValue(message, "ProbeKey");

		if(StringUtils.isEmpty(probeKey)){
			logger.error("probe key not found in coming message .");
			return true;
		}
		if(!probeKey.equalsIgnoreCase(probe.getKey())){
			logger.error("different from probe key of comming message and probe key of configuration file , please check it in zabbix and probe's configuration file .");
			message=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( new Date())+" "+probe.getName()+" "+probe.getType()+"\r\n"+message+"\r\n\r\n";
			try (FileChannel channel = new FileOutputStream("invalidMessage.log",true).getChannel()) {
				ByteBuffer byteBuffer = ByteBuffer.allocate(message.getBytes().length);
				byteBuffer.put(message.getBytes());
				byteBuffer.flip();
				while (byteBuffer.hasRemaining()){
					int i = channel.write(byteBuffer);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		producer.push(new ProbeEventMessage(project,probe,message));
		return false;
	}

	@Override
	public boolean chain(List<String> message, ChannelHandlerContext ctx) {
		logger.warn("not support!");
		return false;
	}
}

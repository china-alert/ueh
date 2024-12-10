package com.mcinfotech.event.transmit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mcinfotech.event.transmit.domain.TransmitInfo;

/**
 * Dispatcher的dispatcher配置
 * 

 */
@Configuration
public class TransmitInfoConfig {
	@Value("${transmit.batch}")
	private int batch;
	@Value("${transmit.interval}")
	private int interval;
	@Bean
	public TransmitInfo buildTransmitInfo() {
		TransmitInfo probe=new TransmitInfo();
		probe.setBatch(batch);
		probe.setInterval(interval);
		return probe;
	}
}

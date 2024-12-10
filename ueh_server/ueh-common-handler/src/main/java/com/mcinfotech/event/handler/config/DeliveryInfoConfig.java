package com.mcinfotech.event.handler.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mcinfotech.event.handler.domain.DeliveryInfo;
import com.mcinfotech.event.handler.domain.DeliveryTableInfo;


@Configuration
@ConfigurationProperties(prefix="delivery")
public class DeliveryInfoConfig {
	private List<DeliveryInfo> data;

	@Bean(name="deliveryTableInfo")
	public DeliveryTableInfo build() {
		Map<String,DeliveryInfo> devliveryTable=new HashMap<String,DeliveryInfo>();
		for(DeliveryInfo info:data){
			devliveryTable.put(info.getType(), info);
		}
		return new DeliveryTableInfo(devliveryTable);
	}

	public List<DeliveryInfo> getData() {
		return data;
	}

	public void setData(List<DeliveryInfo> data) {
		this.data = data;
	}
}
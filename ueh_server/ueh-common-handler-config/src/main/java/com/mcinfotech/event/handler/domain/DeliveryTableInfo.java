package com.mcinfotech.event.handler.domain;

import java.util.Map;

public class DeliveryTableInfo {
private Map<String,DeliveryInfo> delivery;
	
	public DeliveryTableInfo(Map<String,DeliveryInfo> delivery){
		this.delivery=delivery;

	}

	public Map<String, DeliveryInfo> getDelivery() {
		return delivery;
	}

	public void setDelivery(Map<String, DeliveryInfo> delivery) {
		this.delivery = delivery;
	}
}

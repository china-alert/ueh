package com.mcinfotech.event.transmit.domain;

import java.util.HashSet;
import java.util.Set;

public enum MediaType {
	//
	EMAIL,WECHAT,FLOW,NAIL,HRAPP,HREMAIL,EVENTBUS,SOCKET,SHARE;
	public static Set<String> getMediaTypes(String parentType){
		Set<String> mediaTypes = new HashSet<>();
		if ("NS".equalsIgnoreCase(parentType)) {
			//分享
			mediaTypes.add(SHARE.name().toLowerCase());
		} else if ("NN".equalsIgnoreCase(parentType)) {
			//通知
			mediaTypes.add(EMAIL.name().toLowerCase());
			mediaTypes.add(WECHAT.name().toLowerCase());
			mediaTypes.add(NAIL.name().toLowerCase());
			mediaTypes.add(EVENTBUS.name().toLowerCase());
			mediaTypes.add(SOCKET.name().toLowerCase());
		} else if ("NF".equalsIgnoreCase(parentType)) {
			//开单
			mediaTypes.add(FLOW.name().toLowerCase());
		}
		return mediaTypes;
	}

}

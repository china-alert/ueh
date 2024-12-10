package com.mcinfotech.event.transmit.config;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.utils.FastJsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Component
public class EventConfig {
	@Autowired
	DataSource dataSource;
	@Resource
	ProjectInfo projectInfo;

	public boolean updateEventAcknowledgedStatus(Map<String, Object> event){
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectInfo.getId());
		params.setDcName("updateEventAcknowledgedStatus");
		params.setFilter(FastJsonUtils.convertObjectToJSON(event));
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		return result.isSuccess();
	}

	public boolean updateEventAcknowledgedStatus(Map<String, Object> event,String acknoledger){
		event.put("Operator", acknoledger);
		event.put("OperateTimestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectInfo.getId());
		params.setDcName("updateEventAcknowledgedStatus");
		params.setFilter(FastJsonUtils.convertObjectToJSON(event));
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		return result.isSuccess();
	}

	public boolean updateEventAcknowledgedStatus(Map<String, Object> event, String flowStatus, long projectId) {
		event.put("Operator", "SYSTEM");
		event.put("OperateTimestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		event.put("Acknowledged",flowStatus);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setDcName("updateEventAcknowledgedStatus");
		params.setFilter(FastJsonUtils.convertObjectToJSON(event));
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		return result.isSuccess();
	}

	public boolean updateEventAcknowledgedStatus(Map<String, Object> event,String acknoledger,String componentStatus){
		event.put("Operator", acknoledger);
		event.put("OperateTimestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectInfo.getId());
		params.setDcName("updateEventAcknowledgedStatus");
		params.setFilter(FastJsonUtils.convertObjectToJSON(event));
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if ("slave".equalsIgnoreCase(componentStatus)){
			params.setDcName("updateEventAcknowledgedStatusMaster");
			DataServiceUtils.dataLoad(dataSource, params);
		}
		return result.isSuccess();
	}

	public boolean updateEventAcknowledgedStatus(Map<String, Object> event, String flowStatus, long projectId,String componentStatus) {
		event.put("Operator", "SYSTEM");
		event.put("OperateTimestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		event.put("Acknowledged",flowStatus);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setDcName("updateEventAcknowledgedStatus");
		params.setFilter(FastJsonUtils.convertObjectToJSON(event));
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if ("slave".equalsIgnoreCase(componentStatus)){
			params.setDcName("updateEventAcknowledgedStatusMaster");
			DataServiceUtils.dataLoad(dataSource, params);
		}
		return result.isSuccess();
	}
}

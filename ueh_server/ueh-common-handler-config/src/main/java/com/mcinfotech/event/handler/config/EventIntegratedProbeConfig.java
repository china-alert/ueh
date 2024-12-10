package com.mcinfotech.event.handler.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.handler.domain.EventIntegratedProbe;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.util.DataServiceUtils;

@Component
public class EventIntegratedProbeConfig {
	@Autowired
	DataSource dataSource;
	
	public EventIntegratedProbe getEventIntegratedProbe(long projectId,int id){
		EventIntegratedProbe column=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("key", id);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setDcName("eventProbeSelectOne");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(0);
		//System.out.println(new Date());
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			column=(EventIntegratedProbe) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getMapData()),EventIntegratedProbe.class);
		}
		return column;
	}
	/**
	 * 根据PorbeKey查询Probe信息
	 * @param projectId
	 * @param probeKey 指定的ProbeKey
	 * @return
	 */
	public EventIntegratedProbe getEventIntegratedProbe(long projectId,String probeKey){
		EventIntegratedProbe column=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("key", probeKey);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setDcName("eventProbeSelectOne");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(0);
		//System.out.println(new Date());
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			column=(EventIntegratedProbe) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getMapData()),EventIntegratedProbe.class);
		}
		return column;
	}
	
	public List<EventIntegratedProbe> getEventIntegratedProbes(long projectId,Map<String,Object> condition){
		List<EventIntegratedProbe> columns=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.putAll(condition);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setDcName("eventProbeSelect");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(0);
		//System.out.println(new Date());
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			columns=(List<EventIntegratedProbe>) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getDatas()),EventIntegratedProbe.class);
		}
		return columns;
	}
}

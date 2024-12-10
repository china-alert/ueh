package com.mcinfotech.event.config;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mcinfotech.event.domain.EventSourceType;
import com.mcinfotech.event.domain.ExecuteScope;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProbeType;

/**
 * 从配置读取Probe配置信息
 * 

 */
@Configuration
public class ProbeInfoConfig {
	public final static String NEXTCHANNELNAME=UUID.randomUUID().toString();
	
	@Value("${probe.threadCount}")
	private int threadCount;
	@Value("${probe.name}")
	private String name;
	@Value("${probe.type}")
	private ProbeType type;
	@Value("${probe.version}")
	private String version;
	@Value("${probe.key}")
	private String key;
	@Value("${probe.port}")
	private int port;
	@Value("${probe.heartbeat}")
	private int heartbeat;
	@Value("${probe.timeout}")
	private int timeout;
	@Value("${probe.source}")
	private String source;
	@Value("${probe.eventSourceType}")
	private EventSourceType eventSourceType;
	@Value("${probe.executeScope}")
	private ExecuteScope scope;
	
	@Bean
	public ProbeInfo buildProbeInfo() {
		ProbeInfo probe=new ProbeInfo();
		if(StringUtils.isEmpty(source)||source.equalsIgnoreCase("file")){
			probe.setName(this.name);
			probe.setType(this.type);
			probe.setVersion(this.version);
			probe.setKey(this.key);
			probe.setPort(this.port);
			probe.setHeartbeat(this.heartbeat);
			probe.setThreadCount(this.threadCount);
			probe.setTimeout(this.timeout);
			probe.setEventSourceType(eventSourceType);
			probe.setScope(scope);
		}else{
			//probe=getProbeInfo(this.probjectInfo.getId(),"");
		}
		
		return probe;
	}
	/*public EventIntegratedProbe getEventIntegratedProbe(long projectId,int id){
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
	*//**
	 * 根据PorbeKey查询Probe信息
	 * @param projectId
	 * @param probeKey指定的ProbeKey
	 * @return
	 *//*
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
	}*/
}

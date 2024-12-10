package com.mcinfotech.event.handler.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.mcinfotech.event.handler.domain.EventIntegratedProbe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.domain.EventSourceType;
import com.mcinfotech.event.domain.ExecuteScope;
import com.mcinfotech.event.domain.OrderBy;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;

@Component
public class EventHandlerRuleConfig {
	@Autowired
	DataSource dataSource;

	/**
	 * 根据ProbeKey（EventSource)查询事件处理规则
	 * @param projectId
	 * @param eventSource 传入ProbeKey
	 * @param ruleType 压缩规则，C:combine,R:rich,Z:compress,U：升级，D:降级，F:过滤
	 * @return
	 */
	public EventHandlerRule getEventHandlerRule(long projectId,String eventSource,EventSourceType eventSourceType,String ruleType){
		EventHandlerRule column=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		//if(StringUtils.isNotEmpty(eventSource)){
		filter.put("key", eventSource);
		//}
		filter.put("eventSourceType", eventSourceType);
		filter.put("ruleType", ruleType);
		filter.put("isEnable", "Y");
		DataLoadParams params=new DataLoadParams();
		params.setEngine(SQLEngine.Freemarker);
		params.setDcName("eventRulesSelectOne");
		params.setProjectId(projectId);
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(-10);
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			column=(EventHandlerRule) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getMapData()),EventHandlerRule.class);
		}
		return column;
	}
	public List<EventHandlerRule> getEventHandlerRules(long projectId,Map<String,Object> condition){
		List<EventHandlerRule> columns=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("projectId", projectId);
		filter.putAll(condition);
		DataLoadParams params=new DataLoadParams();
		params.setEngine(SQLEngine.Freemarker);
		params.setDcName("eventRulesSelect");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(-10);
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			columns=(List<EventHandlerRule>) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getDatas()),EventHandlerRule.class);
		}
		return columns;
	}

	/**
	 * 根据事件源key和规则类型获取规则列表
	 * @param projectId 项目id
	 * @param eventSource probe key
	 * @param eventSourceType 事件源
	 * @param ruleType 规则类型
	 * @return
	 */
	@Deprecated
	public List<EventHandlerRule> getEventHandlerRules(long projectId,String eventSource,EventSourceType eventSourceType,String ruleType){
		List<EventHandlerRule> columns=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("eventSourceType", eventSourceType);
		filter.put("ruleType", ruleType);
		filter.put("eventSource", eventSource);
		filter.put("isEnable", "Y");
		DataLoadParams params=new DataLoadParams();
		params.setDcName("findEventRoles");
		params.setProjectId(projectId);
		params.setEngine(SQLEngine.Freemarker);
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(-10);
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			columns= JSON.parseArray(JSON.toJSONString(result.getDatas()),EventHandlerRule.class);
		}
		return columns;
	}
	/**
	 * 根据事件源分类key或事件源获取处理规则
	 * @param projectId 项目id
	 * @param eventSource probe key
	 * @param eventSourceType 事件源
	 * @param ruleType 规则类型
	 * @return
	 */
	public List<EventHandlerRule> getRules(long projectId,String eventSource,EventSourceType eventSourceType,ExecuteScope scope,OrderBy priorityOrderBy,String ...ruleType ){
		List<EventHandlerRule> columns=null;
		Map<String,Object> filter=new HashMap<>();
		filter.put("eventSourceType", eventSourceType);
		filter.put("ruleTypes", ruleType);
		filter.put("eventSource", eventSource);
		filter.put("isEnable", "Y");
		filter.put("executeScope", scope);
		if(priorityOrderBy==null) {
			priorityOrderBy=OrderBy.ASC;
		}
		filter.put("orderBy", priorityOrderBy);
		DataLoadParams params=new DataLoadParams();
		params.setDcName("findEventRules");
		params.setProjectId(projectId);
		params.setEngine(SQLEngine.Freemarker);
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(-10);
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			columns= JSON.parseArray(JSON.toJSONString(result.getDatas()),EventHandlerRule.class);
		}
		return columns;
	}
	/**
	 * 合并到getRules
	 * @param projectId
	 * @param eventSource
	 * @param eventSourceType
	 * @param ruleType
	 * @return
	 */
	@Deprecated
	public List<EventHandlerRule> getRules(long projectId,String eventSource,EventSourceType eventSourceType,String ruleType ){
		List<EventHandlerRule> columns=null;
		Map<String,Object> filter=new HashMap<>();
		filter.put("eventSourceType", eventSourceType);
		filter.put("ruleType", ruleType);
		filter.put("eventSource", eventSource);
		filter.put("isEnable", "Y");
		DataLoadParams params=new DataLoadParams();
		params.setDcName("findEventRules");
		params.setProjectId(projectId);
		params.setEngine(SQLEngine.Freemarker);
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(-10);
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			columns= JSON.parseArray(JSON.toJSONString(result.getDatas()),EventHandlerRule.class);
		}
		return columns;
	}
	/**
	 * 合并到getRules
	 * 根据事件源分类key或事件源获取处理规则
	 * findEventRulesByTypes数据集废弃
	 * @param projectId 项目id
	 * @param eventSource probe key
	 * @param eventSourceType 事件源
	 * @param ruleTypes 规则类型集合
	 * @return
	 */
	@Deprecated
	public List<EventHandlerRule> getRules(long projectId,String eventSource,EventSourceType eventSourceType,List<String> ruleTypes){
		List<EventHandlerRule> columns=null;
		Map<String,Object> filter=new HashMap<>();
		filter.put("eventSourceType", eventSourceType);
		filter.put("ruleTypes", ruleTypes);
		filter.put("eventSource", eventSource);
		filter.put("isEnable", "Y");
		DataLoadParams params=new DataLoadParams();
		params.setDcName("findEventRulesByTypes");
		params.setProjectId(projectId);
		params.setEngine(SQLEngine.Freemarker);
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(-10);
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			columns= JSON.parseArray(JSON.toJSONString(result.getDatas()),EventHandlerRule.class);
		}
		return columns;
	}

	/**
	 * 根据PorbeKey查询Probe信息
	 * @param projectId
	 * @param probeKey 指定的ProbeKey
	 * @return
	 */
	public EventIntegratedProbe getEventIntegratedProbe(long projectId, String probeKey){
		EventIntegratedProbe column=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("key", probeKey);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setDcName("eventProbeSelectOne");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(0);
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			column=(EventIntegratedProbe) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getMapData()),EventIntegratedProbe.class);
		}
		return column;
	}

	public DataSource getDataSource() {
		return dataSource;
	}
}

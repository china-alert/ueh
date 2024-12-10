package com.mcinfotech.event.handler.config;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.db.ColumnDefine;
import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;

@Component
public class PlatformEventColumnConfig {
	@Autowired
	DataSource dataSource;
	
	/**
	 * 根据列ID查询列定义信息
	 * @param projectId
	 * @param id 列定义ID
	 * @return
	 */
	public ColumnDefine getPlatformEventColumn(long projectId,int id){
		ColumnDefine column=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("id", id);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setDcName("columnSelectOne");
		params.setEngine(SQLEngine.Freemarker);
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(0);
		System.out.println(new Date());
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			column=(ColumnDefine) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getMapData()),ColumnDefine.class);
		}
		return column;
	}
	public ColumnDefine getPlatformEventColumn(long projectId,String columnInDbName){
		ColumnDefine column=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("columnInDB", columnInDbName);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setEngine(SQLEngine.Freemarker);
		params.setDcName("columnSelectOne");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(0);
		System.out.println(new Date());
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			column=(ColumnDefine) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getMapData()),ColumnDefine.class);
		}
		return column;
	}
	/**
	 * 
	 * @param projectId
	 * @param condition
	 * @return
	 */
	public List<ColumnDefine> getPlatformEventColumn(long projectId,Map<String,Object> condition){
		List<ColumnDefine> columns=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		//filter.put("projectId", projectId);
		filter.putAll(condition);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setEngine(SQLEngine.Freemarker);
		params.setDcName("columnSelect");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(0);
		//System.out.println(new Date());
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			columns=FastJsonUtils.toList(FastJsonUtils.convertObjectToJSON(result.getDatas()), ColumnDefine.class);
		}
		return columns;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}

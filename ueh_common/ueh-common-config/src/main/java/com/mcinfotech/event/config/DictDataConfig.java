package com.mcinfotech.event.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.util.DataServiceUtils;

@Component
public class DictDataConfig {
	@Autowired
	DataSource dataSource;
	
	public Map<String,String> getDictData(long projectId,String dictType,String dictLabel){
		Map<String,String> data=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("dictType", dictType);
		filter.put("dictLabel", dictLabel);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setDcName("getDictData");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(0);
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			data=(Map) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getMapData()),Map.class);
		}
		return data;
	}
	/*public List<ColumnDefine> getPlatformEventColumn(long projectId,Map<String,Object> condition){
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
		System.out.println(new Date());
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			columns=FastJsonUtils.toList(FastJsonUtils.convertObjectToJSON(result.getDatas()), ColumnDefine.class);
		}
		return columns;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}*/
}

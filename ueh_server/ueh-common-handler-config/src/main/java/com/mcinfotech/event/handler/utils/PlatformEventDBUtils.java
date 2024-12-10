package com.mcinfotech.event.handler.utils;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;

/**
 * 事件平台数据库操作

 *
 */
public class PlatformEventDBUtils {
	/**
	 * 将事件插入到数据库中
	 * @param dataSource
	 * @param rawDatas
	 */
	public static void insert(DataSource dataSource,long projectId,Iterable<Map<String,Object>> rawDatas){
		Map<String,Object> filter=new HashMap<String,Object>();
		//StringBuffer sb=new StringBuffer();
		for(Map<String,Object> rawData:rawDatas){
			// ('${Identifier}','${Node}','${NodeAlias}','${Manager}','${Agent}','${AlertGroup}','${AlertKey}',${Severity},${OldSeverity},'${Summary}','${FirstOccurrence}','${LastOccurrence}',${Type},${Tally},${Class},${Grade},'${Location}',${Acknowledged},'${Customer}','${LocalNodeAlias}','${ITMStatus}','${ITMSitType}','${ITMSitGroup}','${ITMSitOrigin}',${RowID},${ProjectID})
			//StringBuffer sb=new StringBuffer();
			//sb.append("(")
			for(String key:rawData.keySet()){
				filter.put(key, rawData.get(key));
			}
			DataLoadParams params=new DataLoadParams();
			params.setDcName("eventCreate");
			params.setProjectId(projectId);
			params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
			params.setStart(1);
			params.setLimit(-10);
			params.setEngine(SQLEngine.Freemarker);
			//System.out.println(new Date());
			ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		}
		/*filter.put("key", eventSource);
		filter.put("ruleType", ruleType);*/
		/*DataLoadParams params=new DataLoadParams();
		params.setDcName("eventCreate");
		params.setProjectId(projectId);
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(-10);
		//System.out.println(new Date());
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);*/
	}
	public static void createTable(){
		
	}
}

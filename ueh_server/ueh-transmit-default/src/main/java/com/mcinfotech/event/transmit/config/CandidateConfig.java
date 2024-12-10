package com.mcinfotech.event.transmit.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.handler.domain.EventHandleUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.transmit.domain.Candidate;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.util.DataServiceUtils;

@Component
public class CandidateConfig {
    @Autowired
    DataSource dataSource;
	
	/*public Candidate getCandidate(long projectId,int id){
		Candidate column=null;
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("key", id);
		DataLoadParams params=new DataLoadParams();
		params.setProjectId(projectId);
		params.setDcName("getCandidate");
		params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
		params.setStart(1);
		params.setLimit(0);
		ResultPattern result=DataServiceUtils.dataLoad(dataSource, params);
		if(result.isSuccess()&&!result.isEmpty()){
			column=(Candidate) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getMapData()),Candidate.class);
		}
		return column;
	}*/

    public List<Candidate> getCandidates(long projectId, Map<String, Object> condition) {
        List<Candidate> columns = null;
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.putAll(condition);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("getCandidates");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            columns = (List<Candidate>) FastJsonUtils.toList(FastJsonUtils.convertObjectToJSON(result.getDatas()), Candidate.class);
        }
        return columns;
    }

    /**
     * 告警node直接关联通知对象
     * @param projectId
     * @param condition
     * @return
     */
    public List<EventHandleUser> getCandidatesByNode(long projectId, Map<String, Object> condition) {
        List<EventHandleUser> columns = new ArrayList<>();
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.putAll(condition);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("getCandidatesByNode");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            columns = JSON.parseArray(JSON.toJSONString(result.getDatas()), EventHandleUser.class);
        }
        return columns;
    }

    /**
     * 告警分组关联通知组，进而获取通知对象
     * @param projectId
     * @param condition
     * @return
     */
    public List<EventHandleUser> getCandidatesByGroup(long projectId, Map<String, Object> condition) {
        List<EventHandleUser> columns = new ArrayList<>();
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.putAll(condition);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("getCandidatesByGroup");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            columns = JSON.parseArray(JSON.toJSONString(result.getDatas()), EventHandleUser.class);
        }
        return columns;
    }

    /**
     * 告警标签关联通知组，进而获取通知对象
     * @param projectId
     * @param condition
     * @return
     */
    public List<EventHandleUser> getCandidatesByLabel(long projectId, Map<String, Object> condition) {
        List<EventHandleUser> columns = new ArrayList<>();
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.putAll(condition);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("getCandidatesByLabel");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            columns = JSON.parseArray(JSON.toJSONString(result.getDatas()), EventHandleUser.class);
        }
        return columns;
    }

    public List<Candidate> getCandidatesByNames(long projectId, Map<String, Object> condition) {
        List<Candidate> columns = null;
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.putAll(condition);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("getCandidatesByNames");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            columns = (List<Candidate>) FastJsonUtils.toList(FastJsonUtils.convertObjectToJSON(result.getDatas()), Candidate.class);
        }
        return columns;
    }
}

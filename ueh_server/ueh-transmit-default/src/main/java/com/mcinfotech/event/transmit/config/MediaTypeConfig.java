package com.mcinfotech.event.transmit.config;

import java.util.*;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcinfotech.event.transmit.domain.MediaInfo;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.util.DataServiceUtils;

@Component
public class MediaTypeConfig {
    @Autowired
    DataSource dataSource;

    public MediaInfo getSendServiceInfo(long projectId, String type) {
        MediaInfo column = null;
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.put("mediaType", type);
        filter.put("isEnable", "Y");
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("notifySettingSelectOne");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        //System.out.println(new Date());
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            column = (MediaInfo) FastJsonUtils.convertJSONToObject(FastJsonUtils.convertObjectToJSON(result.getMapData()), MediaInfo.class);
        }
        return column;
    }

    public MediaInfo getSendServiceInfos(long projectId, String name) {
        List<MediaInfo> mediaInfos;
        MediaInfo mediaInfo = null;
        Map<String, Object> filter = new HashMap<>();
        filter.put("name", name);
        filter.put("isEnable", "Y");
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("notifySettingSelectMultiple");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            mediaInfos = JSON.parseArray(JSON.toJSONString(result.getDatas()), MediaInfo.class);
            mediaInfo=mediaInfos.get(0);
        }
        return mediaInfo;
    }

    public List<MediaInfo> getSendServiceInfo(long projectId, Map<String, Object> conditions) {
        List<MediaInfo> columns = null;
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.put("mediaTypes", conditions.get("mediaTypes"));
        filter.put("isEnable", "Y");
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("notifySettingSelect");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            columns = (List<MediaInfo>) FastJsonUtils.toList(FastJsonUtils.convertObjectToJSON(result.getDatas()), MediaInfo.class);
        }
        return columns;
    }

    public List<String> getNamesByTypes(long projectId, List<String> types) {
        List<String> names = null;
        Map<String, Object> filter = new HashMap();
        filter.put("mediaTypes", types);
        filter.put("isEnable", "Y");
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("getMediaNamesByTypes");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            names = result.getListData().stream().map(data->""+data).collect(Collectors.toList());
        }
        return names;
    }

    /**
     *
     * @param projectId 项目id
     * @param ruleId 规则id
     * @param excludeMediaNames 屏蔽名称
     * @return
     */
    public List<MediaInfo> getMediaTemplates(long projectId, int ruleId, Set<String> excludeMediaNames) {
        List<MediaInfo> mediaInfos = new ArrayList<>();
        Map<String, Object> filter = new HashMap<>();
        filter.put("ruleId", ruleId);
        filter.put("excludeMediaNames", excludeMediaNames);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(projectId);
        params.setDcName("getMediaTemplates");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        params.setStart(1);
        params.setLimit(0);
        ResultPattern result = DataServiceUtils.dataLoad(dataSource, params);
        if (result.isSuccess() && !result.isEmpty()) {
            mediaInfos = JSON.parseArray(JSON.toJSONString(result.getDatas()), MediaInfo.class);
        }
        return mediaInfos;
    }
}

package com.mcinfotech.event.probe.state;

import javax.sql.DataSource;

import com.mcinfotech.event.domain.ProbeState;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.utils.FastJsonUtils;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.util.DataServiceUtils;
/**
 * 将组件状态入库

 *
 */
public class StateReportUtils {
	public static void report(DataSource dataSource,ProbeState probeState,ProjectInfo project){
		/*if(dataSource.getConnection()==null||dataSource.getConnection().isClosed()){
			dataSource=
		}*/
		DataLoadParams loadParams = new DataLoadParams();
		loadParams.setProjectId(project.getId());
		loadParams.setDcName("insertProbeState");
		loadParams.setFilter(FastJsonUtils.convertObjectToJSON(probeState));
		DataServiceUtils.dataLoad(dataSource, loadParams);
	}
}

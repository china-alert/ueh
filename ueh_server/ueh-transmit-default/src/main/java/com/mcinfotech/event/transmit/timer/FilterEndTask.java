package com.mcinfotech.event.transmit.timer;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.transmit.http.OpenFeignService;
import com.mcinfotech.event.transmit.push.HttpPusher;
import com.mcinfotech.event.utils.FastJsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date 2022/10/13 16:42
 *
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.timer
 */
@Component
public class FilterEndTask {
    private static Logger log = LogManager.getLogger(FilterEndTask.class);

    @Autowired
    HttpPusher httpPusher;

    @Autowired
    DataSource dataSource;

    @Autowired
    OpenFeignService openFeignService;

    /**
     * 维护期结束后仍然未恢复的告警进行通知
     */
    @Scheduled(cron = "${filter.task.cron}")
    public void getMail() {
        //维护期结束未恢复的告警
        ResultPattern result = getUnrecoveredAlarm();
        if (result.isSuccess()) {
            log.info(String.join(FilterEndTask.class.getName(), result.toString()));
            List<Map<String, Object>> alarms = result.getDatas();
            if (CollectionUtils.isNotEmpty(alarms)) {
                Long nowMilliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                for (Map<String, Object> alarm : alarms) {
                    Long filterEndTime = (Long) alarm.get("FilterEndTime");
                    if (nowMilliSecond >= filterEndTime) {
                        //置空维护期结束时间
                        alarm.put("FilterEndTime", 0l);
                        ResultPattern updateResult = updateUnrecoveredAlarm(alarm.get("EventID"));
                        if (updateResult.isSuccess()) {
                            log.info(String.join(FilterEndTask.class.getName(), updateResult.toString()));
                            alarm.put("FilterFlag", "NA");
                            //推送告警至通知
                            ProbeEventMessage probeEventMessage = new ProbeEventMessage(null, null, JSON.toJSONString(alarm));
                            httpPusher.push(Arrays.asList(probeEventMessage));
                        } else {
                            log.error(String.join(FilterEndTask.class.getName(), updateResult.getErrorMsg()));
                        }
                    }
                }
            }
        } else {
            log.error(String.join(FilterEndTask.class.getName(), result.getErrorMsg()));
        }
    }

    private ResultPattern updateUnrecoveredAlarm(Object eventID) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("eventID", eventID);
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(10l);
        params.setDcName("updateUnrecoveredAlarm");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        return DataServiceUtils.dataLoad(dataSource, params);
    }

    private ResultPattern getUnrecoveredAlarm() {
        Map<String, Object> filter = new HashMap<String, Object>();
        DataLoadParams params = new DataLoadParams();
        params.setProjectId(10l);
        params.setDcName("getUnrecoveredAlarm");
        params.setFilter(FastJsonUtils.convertObjectToJSON(filter));
        return DataServiceUtils.dataLoad(dataSource, params);
    }
}

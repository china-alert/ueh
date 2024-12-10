package com.mcinfotech.event.dispatcher.timer;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.utils.DateTimeToNumberUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 * date
 * @version V1.0
 * @Package com.mcinfotech.event.dispatcher.timer
 * 定时删除实时表中已恢复告警
 */
@Configuration
@EnableScheduling
public class EventAutoDeleteTimer {

    Logger logger = LogManager.getLogger(EventAutoDeleteTimer.class);

    @Scheduled(cron = "${event.delete.cron}")
    public void updateStatus() {
        ResultPattern res = null;
        try {
            res = deleteExpiredEvent();
        } catch (Exception e) {
            logger.error("实时表删除过期告警异常", e);
        }
        if (!res.isSuccess()) {
            logger.error(res.getErrorMsg());
        }
    }

    @Autowired
    DataSource dataSource;

    @Value("${event.delete.interval}")
    private String interval;

    private ResultPattern deleteExpiredEvent() {
        Map<String, Object> filter = new HashMap<>();
        filter.put("interval", LocalDateTime.now().minusHours(Integer.parseInt(interval)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        DataLoadParams params = new DataLoadParams();
        params.setDcName("deleteExpiredEvent");
        params.setEngine(SQLEngine.Freemarker);
        params.setFilter(JSON.toJSONString(filter));
        params.setProjectId(10L);
        params.setStart(1);
        params.setLimit(-10);
        return DataServiceUtils.dataLoad(dataSource, params);
    }
}

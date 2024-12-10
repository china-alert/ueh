package com.mcinfotech.event.dispatcher.timer;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.utils.DateTimeToNumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 * date 2023/5/16 9:38
 * @version V1.0
 * @Package com.mcinfotech.event.dispatcher.timer
 */
@Configuration
@EnableScheduling
public class StrategyRunningStatusTimer {
    /**
     * 更新维护期策略：0 未执行 1 执行中  2 执行完
     */
    @Scheduled(cron = "${event.strategy.running.status.cron}")
    public void updateStatus() {
        List<EventHandlerRule> rules = null;
        ResultPattern result = selectMaintainEventStrategy();
        if (result.isSuccess() && !result.isEmpty()) {
            rules = JSON.parseArray(JSON.toJSONString(result.getDatas()), EventHandlerRule.class);
            //策略是否在有效期
            for (EventHandlerRule rule : rules) {
                Integer valid = DateTimeToNumberUtil.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
                if (!valid.equals(rule.getStatus())) {
                    rule.setStatus(valid);
                    updateEventStrategyStatus(rule);
                }

            }
        }
    }

    @Autowired
    DataSource dataSource;

    private ResultPattern selectMaintainEventStrategy() {
        Map<String, Object> filter = new HashMap<>();
        DataLoadParams params = new DataLoadParams();
        params.setDcName("selectMaintainEventStrategy");
        params.setEngine(SQLEngine.Freemarker);
        params.setFilter(JSON.toJSONString(filter));
        params.setProjectId(10L);
        params.setStart(1);
        params.setLimit(-10);
        return DataServiceUtils.dataLoad(dataSource, params);
    }

    private ResultPattern updateEventStrategyStatus(EventHandlerRule rule) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("rule", rule);
        DataLoadParams params = new DataLoadParams();
        params.setDcName("updateEventStrategyStatus");
        params.setEngine(SQLEngine.Freemarker);
        params.setFilter(JSON.toJSONString(filter));
        params.setProjectId(10L);
        params.setStart(1);
        params.setLimit(-10);
        return DataServiceUtils.dataLoad(dataSource, params);
    }
}

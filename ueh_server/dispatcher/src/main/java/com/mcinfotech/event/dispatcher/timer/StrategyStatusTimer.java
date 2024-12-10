package com.mcinfotech.event.dispatcher.timer;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import cn.mcinfotech.data.service.domain.SQLEngine;
import cn.mcinfotech.data.service.util.DataServiceUtils;
import com.alibaba.fastjson.JSON;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.utils.DateTimeUtils;
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
public class StrategyStatusTimer {
    /**
     * 过期的并且不保留的事件策略状态改为不启用
     */
    @Scheduled(cron = "${event.strategy.status.cron}")
    public void updateStatus(){
        List<EventHandlerRule> rules=null;
        ResultPattern result = null;
        try {
            result = selectAllEventStrategy();
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Throwable e){
            e.printStackTrace();
        }
        if(result.isSuccess()&&!result.isEmpty()){
            rules= JSON.parseArray(JSON.toJSONString(result.getDatas()),EventHandlerRule.class);
            //策略是否在有效期
            for (EventHandlerRule rule : rules) {
                boolean notExpired=  DateTimeUtils.isValid(rule.getExecType(), rule.getIntervalType(), rule.getDayOfWeekAt(), rule.getDayOfWeekUtil(), rule.getExecuteAt(), rule.getExecuteUtil());
                if(!notExpired) {
                    updateEventStrategy(rule.getId());
                }
            }
        }
    }

    @Autowired
    DataSource dataSource;

    private ResultPattern selectAllEventStrategy() {
        Map<String,Object> filter=new HashMap<>();
        DataLoadParams params=new DataLoadParams();
        params.setDcName("selectAllEventStrategy");
        params.setEngine(SQLEngine.Freemarker);
        params.setFilter(JSON.toJSONString(filter));
        params.setProjectId(10L);
        params.setStart(1);
        params.setLimit(-10);
        return DataServiceUtils.dataLoad(dataSource, params);
    }

    private ResultPattern updateEventStrategy(int id) {
        Map<String,Object> filter=new HashMap<>();
        filter.put("id", id);
        DataLoadParams params=new DataLoadParams();
        params.setDcName("updateEventStrategy");
        params.setEngine(SQLEngine.Freemarker);
        params.setFilter(JSON.toJSONString(filter));
        params.setProjectId(10L);
        params.setStart(1);
        params.setLimit(-10);
        return DataServiceUtils.dataLoad(dataSource, params);
    }
}

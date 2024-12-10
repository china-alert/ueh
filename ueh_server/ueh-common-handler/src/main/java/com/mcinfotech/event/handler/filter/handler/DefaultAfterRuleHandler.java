package com.mcinfotech.event.handler.filter.handler;

import com.mcinfotech.event.manage.EventManage;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Map;

public class DefaultAfterRuleHandler implements
        EventRuleHandler<Collection<Map<String, Object>>, Collection<Map<String, Object>>, EventRuleHandlerContext> {

    @Override
    public Collection<Map<String, Object>> process(Collection<Map<String, Object>> in, EventRuleHandlerContext ctx) {
        // 8.入库,并通知转发
        // 8.1入库
        if (CollectionUtils.isEmpty(in))
            return in;
        EventManage.createEvent(ctx.getEventHandlerRuleConfig().getDataSource(), ctx.getProjectInfo().getId(), in);
        return in;
    }
}

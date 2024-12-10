package com.mcinfotech.event.handler.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date 2022/1/17 19:58
 *
 * @version V1.0
 * @Package com.mcinfotech.event.handler.domain
 * 通知策略独特的参数
 */
public class NoticeParam implements Cloneable {
    /**
     * 未恢复事件delayTime时间后未恢复才发送通知
     * 单位分钟
     */
    private Integer delayTime = 0;
    /**
     * 重复通知功能，是否使用上次的通知介质
     */
    private String defaultNotification;
    /**
     * 重复通知功能，当前配置的通知介质
     */
    private List<String> notificationName;
    /**
     * 重复通知功能，每隔多长时间通知一次
     * 单位分钟
     */
    private Integer interval = 0;
    /**
     * 重复通知功能，最大通知次数
     */
    private Integer count = 0;

    /**
     * 通知人:通知方式名称
     */
    private Map<EventHandleUser, List> candidateVo = new HashMap<>();

    public Integer getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Integer delayTime) {
        this.delayTime = delayTime;
    }

    public String getDefaultNotification() {
        return defaultNotification;
    }

    public void setDefaultNotification(String defaultNotification) {
        this.defaultNotification = defaultNotification;
    }

    public List<String> getNotificationName() {
        return notificationName;
    }

    public void setNotificationName(List<String> notificationName) {
        this.notificationName = notificationName;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Map<EventHandleUser, List> getCandidateVo() {
        return candidateVo;
    }

    public void setCandidateVo(Map<EventHandleUser, List> candidateVo) {
        this.candidateVo = candidateVo;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

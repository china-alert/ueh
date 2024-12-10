package com.mcinfotech.event.handler.domain;

/**
 * date 2024/4/25 10:46
 *
 * @version V1.0
 * @Package com.mcinfotech.event.handler.domain
 * 重复通知实体类
 */
public class RepeatNotification {
    /**
     * 事件ID
     */
    private String eventId;
    /**
     * 通知规则ID
     */
    private Integer ruleId;
    /**
     * 上次通知时间，单位毫秒时间戳
     */
    private Long notificationTimestamp;
    /**
     * 剩余通知次数
     */
    private Integer notificationCount = 0;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public Long getNotificationTimestamp() {
        return notificationTimestamp;
    }

    public void setNotificationTimestamp(Long notificationTimestamp) {
        this.notificationTimestamp = notificationTimestamp;
    }

    public Integer getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(Integer notificationCount) {
        this.notificationCount = notificationCount;
    }
}

package com.mcinfotech.event.handler.domain;

import java.util.List;

/**
 * 当前用户在当前组中每个级别允许的通知方式
 * date 2024/4/8 16:39
 * @version V1.0
 * @Package com.mcinfotech.event.handler.domain
 */
public class SeverityNotification {
    private String severity;
    private List<String> notificationName;

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public List<String> getNotificationName() {
        return notificationName;
    }

    public void setNotificationName(List<String> notificationName) {
        this.notificationName = notificationName;
    }
}

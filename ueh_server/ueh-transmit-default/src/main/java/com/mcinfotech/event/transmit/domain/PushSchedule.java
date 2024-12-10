package com.mcinfotech.event.transmit.domain;

import java.io.Serializable;

/**
 * date 2021/9/26 20:59
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.domain
 */
public class PushSchedule implements Serializable {
    private String cron;
    private String endTime;
    private String interval;
    private String intervalUnit;
    private String startTime;

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(String intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}

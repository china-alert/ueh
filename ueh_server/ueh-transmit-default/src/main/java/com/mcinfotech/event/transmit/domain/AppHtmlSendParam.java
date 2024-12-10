package com.mcinfotech.event.transmit.domain;

import java.io.Serializable;

/**
 * date 2021/9/26 20:52
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.domain
 */
public class AppHtmlSendParam implements Serializable {
    private String bussinessKey;
    private String bussinessType="default";
    private String content;
    private PushSchedule schedule;
    private AppTarget to;

    public String getBussinessKey() {
        return bussinessKey;
    }

    public void setBussinessKey(String bussinessKey) {
        this.bussinessKey = bussinessKey;
    }

    public String getBussinessType() {
        return bussinessType;
    }

    public void setBussinessType(String bussinessType) {
        this.bussinessType = bussinessType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PushSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(PushSchedule schedule) {
        this.schedule = schedule;
    }

    public AppTarget getTo() {
        return to;
    }

    public void setTo(AppTarget to) {
        this.to = to;
    }
}

package com.mcinfotech.event.transmit.domain;

import java.io.Serializable;

/**
 * date 2021/9/26 20:52
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.domain
 */
public class MailSendParam implements Serializable {
    private String bussinessKey;
    private String bussinessType="default";
    private MailTarget cc;
    private String content;
    private PushSchedule schedule;
    private String subject;
    private MailTarget to;

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

    public MailTarget getCc() {
        return cc;
    }

    public void setCc(MailTarget cc) {
        this.cc = cc;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public MailTarget getTo() {
        return to;
    }

    public void setTo(MailTarget to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "MailSendParam{" +
                "bussinessKey='" + bussinessKey + '\'' +
                ", bussinessType='" + bussinessType + '\'' +
                ", cc=" + cc +
                ", content='" + content + '\'' +
                ", schedule=" + schedule +
                ", subject='" + subject + '\'' +
                ", to=" + to +
                '}';
    }
}

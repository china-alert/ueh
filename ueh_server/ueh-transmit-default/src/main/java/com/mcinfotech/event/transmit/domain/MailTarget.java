package com.mcinfotech.event.transmit.domain;

import java.io.Serializable;
import java.util.List;

/**
 * date 2021/9/26 20:54
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.domain
 */
public class MailTarget implements Serializable {
    private List<String> toMails;
    private List<String> toUserIds;

    public List<String> getToMails() {
        return toMails;
    }

    public void setToMails(List<String> toMails) {
        this.toMails = toMails;
    }

    public List<String> getToUserIds() {
        return toUserIds;
    }

    public void setToUserIds(List<String> toUserIds) {
        this.toUserIds = toUserIds;
    }
}

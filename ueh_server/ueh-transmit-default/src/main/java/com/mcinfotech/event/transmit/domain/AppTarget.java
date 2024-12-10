package com.mcinfotech.event.transmit.domain;

import java.io.Serializable;
import java.util.List;

/**
 * date 2021/9/26 20:54
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.domain
 */
public class AppTarget implements Serializable {
    private List<String> toAccounts;
    private List<String> toUserIds;

    public List<String> getToAccounts() {
        return toAccounts;
    }

    public void setToAccounts(List<String> toAccounts) {
        this.toAccounts = toAccounts;
    }

    public List<String> getToUserIds() {
        return toUserIds;
    }

    public void setToUserIds(List<String> toUserIds) {
        this.toUserIds = toUserIds;
    }
}

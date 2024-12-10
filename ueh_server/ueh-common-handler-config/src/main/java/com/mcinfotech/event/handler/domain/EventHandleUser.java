package com.mcinfotech.event.handler.domain;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * (TEventHandleUser)实体类
 *
 * @since 2024-01-12 10:39:03
 */
public class EventHandleUser implements Serializable {
    private static final long serialVersionUID = 225541661241112261L;

    private Long id;
    /**
     * 域账号名称
     */
    private String domainName;
    /**
     * 姓名
     */
    private String userName;

    private String isEnable;

    private Date modifyTime;

    private String remark;
    /**
     * 邮箱
     */
    private String userEmail;
    /**
     * 电话
     */
    private String userPhone;

    private String severity;
    private String notification;
    private String severityNotification;
    private SeverityNotification groupSeverityNotification;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(String isEnable) {
        this.isEnable = isEnable;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getSeverityNotification() {
        return severityNotification;
    }

    public void setSeverityNotification(String severityNotification) {
        this.severityNotification = severityNotification;
    }

    public SeverityNotification getGroupSeverityNotification() {
        return groupSeverityNotification;
    }

    public void setGroupSeverityNotification(SeverityNotification groupSeverityNotification) {
        this.groupSeverityNotification = groupSeverityNotification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventHandleUser that = (EventHandleUser) o;
        return domainName.equals(that.domainName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domainName);
    }
}


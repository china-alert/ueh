package com.mcinfotech.event.domain;

import java.util.HashMap;
import java.util.Map;

/**
    * @Description: 统一消息头
    * @date 2021/5/12 15:07
    */
  
  
public class MessageHeader {
    private int crcCode = 0xabef0101;
    private int length;
    private long sessionId;
    private MessageType type;
    private int priority;
    private Map<String,String> attachment = new HashMap<>();

    public int getCrcCode() {
        return crcCode;
    }

    public void setCrcCode(int crcCode) {
        this.crcCode = crcCode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, String> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, String> attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "MessageHeader{" +
                "crcCode=" + crcCode +
                ", length=" + length +
                ", sessionId=" + sessionId +
                ", type='" + type + '\'' +
                ", priority=" + priority +
                ", attachment=" + attachment +
                '}';
    }
}

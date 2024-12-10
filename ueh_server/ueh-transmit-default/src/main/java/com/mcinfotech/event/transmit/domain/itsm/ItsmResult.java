package com.mcinfotech.event.transmit.domain.itsm;

import java.util.Map;

/**
 * date 2022/4/2 14:39
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.domain.itsm
 */
public class ItsmResult {
    private String cause;
    private Map<String, String> data;
    private String message;
    private Integer result;

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }
}

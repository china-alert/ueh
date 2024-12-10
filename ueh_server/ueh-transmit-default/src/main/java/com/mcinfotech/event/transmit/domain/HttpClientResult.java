package com.mcinfotech.event.transmit.domain;

import java.io.Serializable;

/**
 * date 2021/9/7 23:28
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.domain
 */
public class HttpClientResult implements Serializable {
    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应数据
     */
    private String content;

    public HttpClientResult(int statusCode, String content) {
        this.code=statusCode;
        this.content=content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public HttpClientResult(int scInternalServerError) {
    }

    @Override
    public String toString() {
        return "HttpClientResult{" +
                "code=" + code +
                ", content='" + content + '\'' +
                '}';
    }
}

package com.mcinfotech.event.handler.domain;

/**
 * date 2022/1/17 19:58
 * @version V1.0
 * @Package com.mcinfotech.event.handler.domain
 * 压缩策略独特的参数
 */
public class CompressParam {
    /**
     * 是否开启长压缩
     *  Y:是 N否
     */
    private String isLongCycleCompress;
    /**
     * 长压缩周期
     * 单位分钟
     */
    private String cycleTime;

    public String getIsLongCycleCompress() {
        return isLongCycleCompress;
    }

    public void setIsLongCycleCompress(String isLongCycleCompress) {
        this.isLongCycleCompress = isLongCycleCompress;
    }

    public String getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(String cycleTime) {
        this.cycleTime = cycleTime;
    }

    @Override
    public String toString() {
        return "CompressParam{" +
                "isLongCycleCompress='" + isLongCycleCompress + '\'' +
                ", cycleTime='" + cycleTime + '\'' +
                '}';
    }
}

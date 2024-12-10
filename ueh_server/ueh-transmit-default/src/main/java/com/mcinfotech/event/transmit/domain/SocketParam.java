package com.mcinfotech.event.transmit.domain;

/**

 * date 2024/1/23 15:54
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.domain
 */
public class SocketParam {
    /**
     * socket协议类型：tcp udp
     */
    private String type;
    /**
     * socket地址
     */
    private String ip;
    /**
     * socket端口
     */
    private String port;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "SocketParam{" +
                "type='" + type + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}

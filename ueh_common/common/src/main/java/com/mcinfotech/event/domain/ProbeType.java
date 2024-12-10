package com.mcinfotech.event.domain;

/**
 * 组件类型

 */
public enum ProbeType {
	ZABBIX((byte) 1), SYSLOG((byte) 2), SNMPTRAP((byte) 3),RESTAPI((byte)4),DISPATCHER((byte)5),HANDLER((byte)6),DELIVERY((byte)7),EMAIL((byte) 8),CASCADE((byte) 9),UNKNOW((byte)100),CUSTOM((byte)30);

	private byte type;

	ProbeType(byte type) {
		this.type = type;
	}

	public byte getType() {
		return type;
	}

	public static ProbeType get(byte type) {
		for (ProbeType value : values()) {
			if (value.type == type) {
				return value;
			}
		}
		return null;
	}
}
package com.mcinfotech.event.domain;

/**

 */
public enum MessageType {
	ZABBIX((byte) 1),
	SYSLOG((byte) 2),
	SNMPTRAP((byte) 3),
	PING((byte)4),
	PONG((byte)5),
	EMPTY((byte) 6),
	EMAIL((byte) 7),
	RESTAPI((byte)8),
	DISPATCHER((byte)9),
	UNKNOW((byte)10);

	private byte type;

	MessageType(byte type) {
		this.type = type;
	}

	public byte getType() {
		return type;
	}

	public static MessageType get(byte type) {
		for (MessageType value : values()) {
			if (value.type == type) {
				return value;
			}
		}
		return null;
	}
}
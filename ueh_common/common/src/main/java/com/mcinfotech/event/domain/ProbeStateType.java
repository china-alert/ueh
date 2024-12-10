package com.mcinfotech.event.domain;

/**
 * 组件状态

 */
public enum ProbeStateType {
	OK((byte)0),RUNNING((byte) 1), STOP((byte) 2), FAILURE((byte) 3),ERROR((byte)4),UNKNOW((byte)10);

	private byte type;

	ProbeStateType(byte type) {
		this.type = type;
	}

	public byte getType() {
		return type;
	}

	public static ProbeStateType get(byte type) {
		for (ProbeStateType value : values()) {
			if (value.type == type) {
				return value;
			}
		}
		return null;
	}
}
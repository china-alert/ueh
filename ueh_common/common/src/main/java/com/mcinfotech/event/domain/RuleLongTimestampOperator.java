package com.mcinfotech.event.domain;

public enum RuleLongTimestampOperator {
	Between("between");

	private String code;

	private RuleLongTimestampOperator(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	/**
	 * Get RuleLongTimestampOperator from code
	 * 
	 * @param code The input RuleLongTimestampOperator code
	 * @return The matching enum value. None if there is not matching enum value
	 */
	static public RuleLongTimestampOperator fromCode(String code) {
		for (RuleLongTimestampOperator operator : values()) {
			if (operator.getCode().equals(code)) {
				return operator;
			}
		}
		return null;
	}
}

package com.mcinfotech.event.domain;

public enum RuleStringOperator {
	In("in"), Equal("="), 
	Between("between"), Regex("regex"),Like("like"),IP("ip"),IPRange("eip");

	private String code;

	private RuleStringOperator(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	/**
	 * Get RuleStringOperator from code
	 * 
	 * @param code The input RuleStringOperator code
	 * @return The matching enum value. None if there is not matching enum value
	 */
	static public RuleStringOperator fromCode(String code) {
		for (RuleStringOperator operator : values()) {
			if (operator.getCode().equals(code)) {
				return operator;
			}
		}
		return null;
	}
}

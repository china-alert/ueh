package com.mcinfotech.event.domain;

public enum RuleNumericOperator {
	In("in"), Equal("="), Greater(">"), Less("<"), NotEqual("!="),
	Between("between"), Regex("regex"), GreaterEqual(">="), LessEqual("<=");

	private String code;

	private RuleNumericOperator(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	/**
	 * Get RuleIntOperator from code
	 * 
	 * @param code The input FilterOperator code
	 * @return The matching enum value. None if there is not matching enum value
	 */
	static public RuleNumericOperator fromCode(String code) {
		for (RuleNumericOperator operator : values()) {
			if (operator.getCode().equals(code)) {
				return operator;
			}
		}
		return null;
	}
}

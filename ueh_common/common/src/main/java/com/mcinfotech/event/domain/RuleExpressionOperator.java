package com.mcinfotech.event.domain;

public enum RuleExpressionOperator {
	And("&&"), Or("||");

	private String code;

	private RuleExpressionOperator(String code) {
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
	static public RuleExpressionOperator fromCode(String code) {
		for (RuleExpressionOperator operator : values()) {
			if (operator.getCode().equals(code)) {
				return operator;
			}
		}
		return null;
	}
}

package com.adfonic.reporting.service.parameter;

public enum Required implements Parameter {
	/**
	 * If a required parameter is added here, remember to add corresponding value checks for 
	 * it in RequiredParameterInterceptor.
	 */
	ADVERTISER("advertiser"),
	FROM("from"),
	TO("to");
	
	private String parameter;
	
	Required(String parameter) {
		this.parameter = parameter;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
}

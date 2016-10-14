package com.adfonic.reporting.service.parameter;

@Deprecated
public enum Invalid implements Parameter {
	/**
	 * The followings parameter values are invalid for certain endpoints. These need to checked and managed
	 * If a new parameter is added here, make sure to add corresponding logic in InvalidParameterInterceptor
	 */
	
	TOTAL("total"),
	HOURLY("hourly");
	
	private String value;
	
	Invalid(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public String getParameter() {
		return value;
	}
}

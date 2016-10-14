package com.adfonic.reporting.service.parameter;

public enum Filter {
	
	DETAIL("detail"),
	TOTAL("summary_by_day"),
	DAILY("detail_by_day"),
	HOURLY("detail_by_hour");
	
	private String name;
	
	Filter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}

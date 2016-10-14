package com.adfonic.reporting.service.parameter;

public enum Group {
	
	INV_SOURCE("invsrc"),
	CREATIVE("cre"),
	CHANNEL("ch"),
	IAB("iab"),
	PLATFORM("plt"),
	VENDOR("vnd"),
	REGION("rgn"),
	LOCATION("loc");

	private String name;
	
	Group(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}

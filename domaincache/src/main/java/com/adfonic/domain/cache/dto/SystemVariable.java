package com.adfonic.domain.cache.dto;

public class SystemVariable extends BusinessKeyDto {

	public SystemVariable(String name, Integer intValue, Double doubleValue) {
		this.name = name;
		this.intValue = intValue;
		this.doubleValue = doubleValue;
	}
	public SystemVariable() {
	}

	private static final long serialVersionUID = 2L;

	private String name;
	private Integer intValue;
	private Double doubleValue;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}
}

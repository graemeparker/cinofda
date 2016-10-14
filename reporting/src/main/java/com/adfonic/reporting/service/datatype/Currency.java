package com.adfonic.reporting.service.datatype;

public class Currency {
	
	private Float value;
	
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	
	public String toString() {
		return this.value.toString();
	}
}

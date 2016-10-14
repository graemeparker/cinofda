package com.adfonic.reporting.service.datatype;

public class Seconds {
	
	private Long value;
	
	public Long getValue() {
		return value;
	}
	public void setValue(Long value) {
		this.value = value;
	}
	
	public String toString() {
		return this.value.toString();
	}
}

package com.adfonic.reporting.service.datatype;

public class Percentage {
	private Double value;
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	
	public String toString() {
		return this.value.toString();
	}
}

package com.adfonic.reporting.sql.dto;


public class OperatorDetailDto extends BaseReportDto {

	private static final long serialVersionUID = 1L;
	
	protected String country,operator,countryIsocode;
	
	public String getCountry() {
		return country;
	}
	public String getCountryIsocode() {
		return countryIsocode;
	}
	public void setCountryIsocode(String countryIsocode) {
		this.countryIsocode = countryIsocode;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
}

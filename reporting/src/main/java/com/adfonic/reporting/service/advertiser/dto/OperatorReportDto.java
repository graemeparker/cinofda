package com.adfonic.reporting.service.advertiser.dto;

import com.adfonic.reporting.service.advertiser.dto.mixin.OperatorReportDtoMixin;


public class OperatorReportDto extends BaseReportDto {

	private static final long serialVersionUID = 1L;
	
	private String country, countryIso, operator;
	
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
	public String getCountryIso() {
		return countryIso;
	}
	public void setCountryIso(String countryIso) {
		this.countryIso = countryIso;
	}
	
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public OperatorReportDtoMixin getMixin() {
		return new OperatorReportDtoMixin();
	}
}

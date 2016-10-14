package com.adfonic.reporting.sql.dto;


public class LocationDetailDto extends BaseReportDto {

	private static final long serialVersionUID = 1L;
	
	protected double percentTotalImpressions;
	public String getCountryIsocode() {
		return countryIsocode;
	}
	public void setCountryIsocode(String countryIsocode) {
		this.countryIsocode = countryIsocode;
	}
	protected String country,countryIsocode,location;
	
	public double getPercentTotalImpressions() {
		return percentTotalImpressions;
	}
	public void setPercentTotalImpressions(double percentTotalImpressions) {
		this.percentTotalImpressions = percentTotalImpressions;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}

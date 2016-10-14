package com.adfonic.reporting.sql.dto;

public class DeviceDetailByCountryByBrandDto extends BaseReportDto {

    private static final long serialVersionUID = 1L;
    
    protected String brand;
	protected String country;
	
	public String getBrand(){ return brand; }
	public void setBrand(String brand){ this.brand = brand; }
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
}

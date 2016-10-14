package com.adfonic.reporting.service.advertiser.dto;

import com.adfonic.reporting.service.advertiser.dto.mixin.DeviceReportDtoMixin;


public class DeviceReportDto extends BaseReportDto {

	private static final long serialVersionUID = 1L;
	
	private String model, vendor, platform, region, country;
	
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public DeviceReportDtoMixin getMixin() {
		return new DeviceReportDtoMixin();
	}
}

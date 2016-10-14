package com.adfonic.reporting.service.advertiser.dto;

import com.adfonic.reporting.service.advertiser.dto.mixin.LocationReportDtoMixin;
import com.adfonic.reporting.service.datatype.Percentage;

public class LocationReportDto extends BaseReportDto {

	private static final long serialVersionUID = 1L;
	
	private Percentage percentTotalImpressions;
	
	private String country, countryIso, location, channel, inventorySource, iab;
	
	public Percentage getPercentTotalImpressions() {
		return percentTotalImpressions;
	}
	public void setPercentTotalImpressions(Double percentTotalImpressions) {
		if(percentTotalImpressions != null) {
			this.percentTotalImpressions = new Percentage();
			this.percentTotalImpressions.setValue(percentTotalImpressions);
		}
	}
	
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
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public String getInventorySource() {
		return inventorySource;
	}
	public void setInventorySource(String inventorySource) {
		this.inventorySource = inventorySource;
	}
	
	public String getIab() {
		return iab;
	}
	public void setIab(String iab) {
		this.iab = iab;
	}
	
	public LocationReportDtoMixin getMixin() {
		return new LocationReportDtoMixin();
	}
}

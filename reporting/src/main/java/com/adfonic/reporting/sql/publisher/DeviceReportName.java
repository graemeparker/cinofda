package com.adfonic.reporting.sql.publisher;

public enum DeviceReportName {
	DEV_RGN_VND_DAY("deviceByRegionAndBrandDay"),
	DEV_RGN_VND("deviceByRegionAndBrand"),
	DEV_PLT_LOC_DAY("deviceByPlatformAndCountryDay"),
	DEV_VND_DAY("deviceByBrandAndDay"),
	DEV_VND("deviceByBrand"),
	DEV_VND_LOC_DAY("deviceByCountryAndBrandDay"),
	DEV_VND_LOC("deviceByCountryAndBrand"),
	DEV_PLT_LOC("deviceByCountryAndPlatform"),
	DEV_LOC_DAY("deviceByCountryAndDay"),
	DEV_LOC("deviceByCountry"),
	DEV_PLT_DAY("deviceByPlatformAndDay"),
	DEV_PLT("deviceByPlatform"),
	DEV_RGN_PLT_DAY("deviceByRegionAndPlatformDay"),
	DEV_RGN_PLT("deviceByRegionAndPlatform"),
	DEV_RGN_DAY("deviceByRegionAndDay"),
	DEV_RGN("deviceByRegion"),
	DEV_DAY("deviceReportByDay");
	
	
	private String name;
	
	private DeviceReportName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
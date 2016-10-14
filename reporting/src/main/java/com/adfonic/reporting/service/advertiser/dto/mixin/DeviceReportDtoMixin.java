package com.adfonic.reporting.service.advertiser.dto.mixin;

import com.adfonic.reporting.service.ColumnUtil;
import com.adfonic.reporting.service.datatype.Percentage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceReportDtoMixin extends BaseReportDtoMixin {

	@JsonProperty(ColumnUtil.MODEL)
	private String model;
	
	@JsonProperty(ColumnUtil.VENDOR)
	private String vendor;
	
	@JsonProperty(ColumnUtil.PLATFORM)
	private String platform;
	
	@JsonProperty(ColumnUtil.REGION)
	private String region;
	
	@JsonProperty(ColumnUtil.COUNTRY)
	private String country;
}

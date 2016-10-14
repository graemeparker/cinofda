package com.adfonic.reporting.service.advertiser.dto.mixin;

import com.adfonic.reporting.service.ColumnUtil;
import com.adfonic.reporting.service.datatype.Percentage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationReportDtoMixin extends BaseReportDtoMixin {

	@JsonProperty(ColumnUtil.LOCATION_PERCENT_IMPRESSIONS)
	private Percentage percentTotalImpressions;
	
	@JsonProperty(ColumnUtil.COUNTRY)
	private String country;
	
	@JsonProperty(ColumnUtil.COUNTRY_ISO)
	private String countryIso;
	
	@JsonProperty(ColumnUtil.REGION)
	private String location;
	
	@JsonProperty(ColumnUtil.CHANNEL)
	private String channel;
	
	@JsonProperty(ColumnUtil.IAB)
	private String iab;
	
	@JsonProperty(ColumnUtil.INVENTORY)
	private String inventorySource;
}

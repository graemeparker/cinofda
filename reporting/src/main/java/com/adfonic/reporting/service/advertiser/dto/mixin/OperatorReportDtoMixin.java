package com.adfonic.reporting.service.advertiser.dto.mixin;

import com.adfonic.reporting.service.ColumnUtil;
import com.adfonic.reporting.service.datatype.Percentage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OperatorReportDtoMixin extends BaseReportDtoMixin {

	@JsonProperty(ColumnUtil.COUNTRY)
	private String country;
	
	@JsonProperty(ColumnUtil.COUNTRY_ISO)
	private String countryIso;
	
	@JsonProperty(ColumnUtil.OPERATOR)
	private String operator;
}

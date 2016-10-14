package com.adfonic.reporting.service.advertiser.dto.mixin;

import com.adfonic.reporting.service.ColumnUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreativeReportDtoMixin extends BaseReportDtoMixin {

	@JsonProperty(ColumnUtil.CAMPAIGN)
	private String campaign;
	
	@JsonProperty(ColumnUtil.CAMPAIGN_EXTERNAL_ID)
	private String campaignExternalId;
	
	@JsonProperty(ColumnUtil.CHANNEL)
	private String channel;
	
	@JsonProperty(ColumnUtil.IAB)
	private String iab;
	
	@JsonProperty(ColumnUtil.INVENTORY)
	private String inventorySource;
	
	@JsonProperty(ColumnUtil.CREATIVE)
	private String creative;
	
	@JsonProperty(ColumnUtil.CREATIVE_EXTERNAL_ID)
	private String creativeExternalId;
	
	@JsonProperty(ColumnUtil.FORMAT)
	private String format;
	
}

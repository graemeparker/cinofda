package com.adfonic.reporting.service.advertiser.dto;

import com.adfonic.reporting.service.advertiser.dto.mixin.CreativeReportDtoMixin;


public class CreativeReportDto extends BaseReportDto {

	private static final long serialVersionUID = 1L;
	
	private String campaign, campaignExternalId, channel, inventorySource, creative, creativeExternalId, format, iab;
	
	public String getCampaign() {
		return campaign;
	}
	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}
	
	
	public String getCampaignExternalId() {
		return campaignExternalId;
	}
	public void setCampaignExternalId(String campaignExternalId) {
		this.campaignExternalId = campaignExternalId;
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
	
	public String getCreative() {
		return creative;
	}
	public void setCreative(String creative) {
		this.creative = creative;
	}
	
	public String getCreativeExternalId() {
		return creativeExternalId;
	}
	public void setCreativeExternalId(String creativeExternalId) {
		this.creativeExternalId = creativeExternalId;
	}
	
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getIab() {
		return iab;
	}
	public void setIab(String iab) {
		this.iab = iab;
	}
	
	public CreativeReportDtoMixin getMixin() {
		return new CreativeReportDtoMixin();
	}
}

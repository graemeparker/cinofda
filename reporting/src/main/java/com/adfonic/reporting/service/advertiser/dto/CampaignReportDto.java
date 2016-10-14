package com.adfonic.reporting.service.advertiser.dto;

import com.adfonic.reporting.service.advertiser.dto.mixin.CampaignReportDtoMixin;

public class CampaignReportDto extends BaseReportDto {

	private static final long serialVersionUID = 1L;
	
	private String campaign, campaignExternalId, channel, inventorySource, hour, iab;
	
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
	
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	
	public String getIab() {
		return iab;
	}
	public void setIab(String iab) {
		this.iab = iab;
	}
	
	public CampaignReportDtoMixin getMixin() {
		return new CampaignReportDtoMixin();
	}
}

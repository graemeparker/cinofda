package com.adfonic.reporting.sql.dto;

public class CampaignDetailDto extends BaseReportDto {

	private static final long serialVersionUID = 1L;
	
	protected String campaign,campaignExternalId;

	public String getCampaignExternalId() {
		return campaignExternalId;
	}
	public void setCampaignExternalId(String campaignExternalId) {
		this.campaignExternalId = campaignExternalId;
	}
	public String getCampaign() {
		return campaign;
	}
	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}
}

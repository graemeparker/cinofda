package com.adfonic.reporting.sql.dto;

public class CreativeDetailDto extends BaseReportDto {

	private static final long serialVersionUID = 1L;
	
	protected String campaign,creative,format;
	protected String campaign_external_id,creative_external_id;
	
	public String getCampaign_external_id() {
		return campaign_external_id;
	}
	public void setCampaign_external_id(String campaign_external_id) {
		this.campaign_external_id = campaign_external_id;
	}
	public String getCreative_external_id() {
		return creative_external_id;
	}
	public void setCreative_external_id(String creative_external_id) {
		this.creative_external_id = creative_external_id;
	}
	public String getCampaign() {
		return campaign;
	}
	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}
	public String getCreative() {
		return creative;
	}
	public void setCreative(String creative) {
		this.creative = creative;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
}

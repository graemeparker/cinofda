package com.adfonic.reporting.sql.dto;


public class CampaignDetailByHourByInvSourceDto extends CampaignDetailByHourDto {

	private static final long serialVersionUID = 1L;
	
	protected String inventorySource;

	public String getInventorySource() {
		return inventorySource;
	}

	public void setInventorySource(String inventorySource) {
		this.inventorySource = inventorySource;
	}
}

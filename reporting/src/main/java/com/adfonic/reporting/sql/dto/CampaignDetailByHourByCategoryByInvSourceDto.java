package com.adfonic.reporting.sql.dto;


public class CampaignDetailByHourByCategoryByInvSourceDto extends CampaignDetailByHourDto {

	private static final long serialVersionUID = 1L;
	
	protected String category,inventorySource;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getInventorySource() {
		return inventorySource;
	}
	public void setInventorySource(String inventorySource) {
		this.inventorySource = inventorySource;
	}
}

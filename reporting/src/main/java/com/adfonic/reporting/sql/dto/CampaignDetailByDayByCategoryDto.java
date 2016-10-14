package com.adfonic.reporting.sql.dto;


public class CampaignDetailByDayByCategoryDto extends CampaignDetailByDayDto {

	private static final long serialVersionUID = 1L;
	
	protected String category;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}

package com.adfonic.reporting.sql.dto;


public class CampaignTotalByHourDto extends CampaignTotalDto {

	private static final long serialVersionUID = 1L;
	
	protected String hour;
	
	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}
}

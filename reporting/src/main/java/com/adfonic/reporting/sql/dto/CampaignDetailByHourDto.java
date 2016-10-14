package com.adfonic.reporting.sql.dto;


public class CampaignDetailByHourDto extends CampaignDetailDto {

	private static final long serialVersionUID = 1L;
	
	protected String dayUnixTimestamp,hour;
	
	public String getDayUnixTimestamp() {
		return dayUnixTimestamp;
	}

	public void setDayUnixTimestamp(String dayUnixTimestamp) {
		this.dayUnixTimestamp = dayUnixTimestamp;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}
}

package com.adfonic.reporting.sql.dto;


public class CampaignInvSourceDetailByDayDto extends CampaignInvSourceDetailDto {
	private static final long serialVersionUID = 1L;
	
	protected String dayUnixTimestamp;

	public String getDayUnixTimestamp() {
		return dayUnixTimestamp;
	}

	public void setDayUnixTimestamp(String dayUnixTimestamp) {
		this.dayUnixTimestamp = dayUnixTimestamp;
	}
}

package com.adfonic.reporting.sql.dto;


public class OperatorDetailDayDto extends OperatorDetailDto {

	private static final long serialVersionUID = 1L;
	
	protected String dayUnixTimestamp;

	public String getDayUnixTimestamp() {
		return dayUnixTimestamp;
	}

	public void setDayUnixTimestamp(String dayUnixTimestamp) {
		this.dayUnixTimestamp = dayUnixTimestamp;
	}
}

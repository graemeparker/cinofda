package com.adfonic.reporting.service.scheduled.dto;

public class ScheduledReportTimePeriodDto {
	
	private int id;
	private String name;
	private String startSql;
	private String endSql;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStartSql() {
		return startSql;
	}
	public void setStartSql(String startSql) {
		this.startSql = startSql;
	}
	public String getEndSql() {
		return endSql;
	}
	public void setEndSql(String endSql) {
		this.endSql = endSql;
	}
}

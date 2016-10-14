package com.adfonic.reporting.service.scheduled.dto;

public class ScheduledReportFrequencyDto {
	
	private int id;
	private String name;
	private int hoursOfDay;
	private int daysOfWeek;
	private int daysOfMonth;
	
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
	public int getHoursOfDay() {
		return hoursOfDay;
	}
	public void setHoursOfDay(int hoursOfDay) {
		this.hoursOfDay = hoursOfDay;
	}
	public int getDaysOfWeek() {
		return daysOfWeek;
	}
	public void setDaysOfWeek(int daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}
	public int getDaysOfMonth() {
		return daysOfMonth;
	}
	public void setDaysOfMonth(int daysOfMonth) {
		this.daysOfMonth = daysOfMonth;
	}
}

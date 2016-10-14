package com.adfonic.reporting.service.scheduled.dto;

public class ScheduledReportDto {

	private String externalId;
	private int reportType;
	private int frequency;
	private int timePeriod;
	private long advertiser;
	private String campaigns;
	private String creatives;
	private String formats;
	private String models;
	private String vendors;
	private int startDay;
	private int endDay;
	private int status;
	private String emails;
	private String dateFormat;
	private int runHour;
	
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	public int getReportType() {
		return reportType;
	}

	public void setReportType(int reportType) {
		this.reportType = reportType;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(int timePeriod) {
		this.timePeriod = timePeriod;
	}

	public long getAdvertiser() {
		return advertiser;
	}

	public void setAdvertiser(long advertiser) {
		this.advertiser = advertiser;
	}

	public String getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(String campaigns) {
		this.campaigns = campaigns;
	}

	public String getCreatives() {
		return creatives;
	}

	public void setCreatives(String creatives) {
		this.creatives = creatives;
	}

	public String getFormats() {
		return formats;
	}

	public void setFormats(String formats) {
		this.formats = formats;
	}

	public String getModels() {
		return models;
	}

	public void setModels(String models) {
		this.models = models;
	}

	public String getVendors() {
		return vendors;
	}

	public void setVendors(String vendors) {
		this.vendors = vendors;
	}

	public Integer getStartDay() {
		return (startDay == 0) ? null : startDay;
	}

	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}

	public Integer getEndDay() {
		return (endDay == 0) ? null : endDay;
	}

	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public String getDateFormat() {
		return (dateFormat == null) ? "" : dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public int getRunHour() {
		return runHour;
	}

	public void setRunHour(int runHour) {
		this.runHour = runHour;
	}
}

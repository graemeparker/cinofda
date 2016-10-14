package com.adfonic.reporting.sql.dto;

public class BudgetReportCampaignOverallDto {

	protected String campaign;
	protected double budget;
	protected String startDate;
	protected String endDate;
	protected double depleted;
	protected double remaining;
	

	public String getCampaign() {
		return campaign;
	}
	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}
	
	public double getBudget() {
		return budget;
	}
	public void setBudget(double budget) {
		this.budget = budget;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public double getDepleted() {
		return depleted;
	}
	public void setDepleted(double depleted) {
		this.depleted = depleted;
	}
	public double getRemaining() {
		return remaining;
	}
	public void setRemaining(double remaining) {
		this.remaining = remaining;
	}
	
}

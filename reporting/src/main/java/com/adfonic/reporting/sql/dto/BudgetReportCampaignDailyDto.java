package com.adfonic.reporting.sql.dto;

public class BudgetReportCampaignDailyDto {

	protected String campaign;
	protected String date;
	protected double budget;
	protected double depleted;
	protected double remaining;

	
	
	public String getCampaign() {
		return campaign;
	}
	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public double getBudget() {
		return budget;
	}
	public void setBudget(double budget) {
		this.budget = budget;
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

package com.adfonic.reporting.sql.dto;

import java.io.Serializable;

public abstract class BaseReportDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected long impressions, clicks, conversions, totalViews, completedViews, averageDuration;
	protected double ctr,ecpm,ecpc,clickConversion, costPerConversion, costPerView, q1percent,q2percent,q3percent,q4percent,engagementScore;
	protected float cost;
	
	public long getImpressions() {
		return impressions;
	}
	public void setImpressions(long impressions) {
		this.impressions = impressions;
	}
	public long getClicks() {
		return clicks;
	}
	public void setClicks(long clicks) {
		this.clicks = clicks;
	}
	public double getCtr() {
		return ctr;
	}
	public void setCtr(double ctr) {
		this.ctr = ctr;
	}
	public double getEcpm() {
		return ecpm;
	}
	public void setEcpm(double ecpm) {
		this.ecpm = ecpm;
	}
	public double getEcpc() {
		return ecpc;
	}
	public void setEcpc(double ecpc) {
		this.ecpc = ecpc;
	}
	public long getConversions() {
		return conversions;
	}
	public void setConversions(long conversions) {
		this.conversions = conversions;
	}
	public double getClickConversion() {
		return clickConversion;
	}
	public void setClickConversion(double clickConversion) {
		this.clickConversion = clickConversion;
	}
	public double getCostPerConversion() {
		return costPerConversion;
	}
	public void setCostPerConversion(double costPerConversion) {
		this.costPerConversion = costPerConversion;
	}
	public float getCost() {
		return cost;
	}
	public void setCost(float cost) {
		this.cost = cost;
	}
	public long getTotalViews() {
		return totalViews;
	}
	public void setTotalViews(long totalViews) {
		this.totalViews = totalViews;
	}
	public long getCompletedViews() {
		return completedViews;
	}
	public void setCompletedViews(long completedViews) {
		this.completedViews = completedViews;
	}
	public long getAverageDuration() {
		return averageDuration;
	}
	public void setAverageDuration(long averageDuration) {
		this.averageDuration = averageDuration;
	}
	public double getCostPerView() {
		return costPerView;
	}
	public void setCostPerView(double costPerView) {
		this.costPerView = costPerView;
	}
	public double getQ1percent() {
		return q1percent;
	}
	public void setQ1percent(double q1percent) {
		this.q1percent = q1percent;
	}
	public double getQ2percent() {
		return q2percent;
	}
	public void setQ2percent(double q2percent) {
		this.q2percent = q2percent;
	}
	public double getQ3percent() {
		return q3percent;
	}
	public void setQ3percent(double q3percent) {
		this.q3percent = q3percent;
	}
	public double getQ4percent() {
		return q4percent;
	}
	public void setQ4percent(double q4percent) {
		this.q4percent = q4percent;
	}
	public double getEngagementScore() {
		return engagementScore;
	}
	public void setEngagementScore(double engagementScore) {
		this.engagementScore = engagementScore;
	}
}

package com.adfonic.reporting.service.advertiser.dto;

import java.io.Serializable;
import java.util.Date;

import com.adfonic.reporting.service.advertiser.dto.mixin.BaseReportDtoMixin;
import com.adfonic.reporting.service.datatype.Currency;
import com.adfonic.reporting.service.datatype.Percentage;
import com.adfonic.reporting.service.datatype.Seconds;
import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class BaseReportDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long totalImpressions, totalClicks, totalConversions, totalViews, completedViews;
	
	private Double ctr, ecpm, ecpc, clickConversion, costPerConversion, costPerView, engagementScore;
	
	private Percentage q1percent,q2percent, q3percent, q4percent;
	
	private Currency totalCost;
	
	private Seconds averageDuration;
	
	private Date day;
	
	public Long getTotalImpressions() {
		return totalImpressions;
	}
	public void setTotalImpressions(Long totalImpressions) {
		this.totalImpressions = totalImpressions;
	}
	
	public Long getTotalClicks() {
		return totalClicks;
	}
	public void setTotalClicks(Long totalClicks) {
		this.totalClicks = totalClicks;
	}
	
	public Double getCtr() {
		return ctr;
	}
	public void setCtr(Double ctr) {
		this.ctr = ctr;
	}
	
	public Double getEcpm() {
		return ecpm;
	}
	public void setEcpm(Double ecpm) {
		this.ecpm = ecpm;
	}
	
	public Double getEcpc() {
		return ecpc;
	}
	public void setEcpc(Double ecpc) {
		this.ecpc = ecpc;
	}
	
	public Long getTotalConversions() {
		return totalConversions;
	}
	public void setTotalConversions(Long totalConversions) {
		this.totalConversions = totalConversions;
	}
	
	public Double getClickConversion() {
		return clickConversion;
	}
	public void setClickConversion(Double clickConversion) {
		this.clickConversion = clickConversion;
	}
	
	public Double getCostPerConversion() {
		return costPerConversion;
	}
	public void setCostPerConversion(Double costPerConversion) {
		this.costPerConversion = costPerConversion;
	}
	
	public Currency getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(Float totalCost) {
		if(totalCost != null) {
			this.totalCost = new Currency();
			this.totalCost.setValue(totalCost);
		}
	}
	
	public Long getTotalViews() {
		return totalViews;
	}
	public void setTotalViews(Long totalViews) {
		this.totalViews = totalViews;
	}
	
	public Long getCompletedViews() {
		return completedViews;
	}
	public void setCompletedViews(Long completedViews) {
		this.completedViews = completedViews;
	}
	
	public Seconds getAverageDuration() {
		return averageDuration;
	}
	public void setAverageDuration(Long averageDuration) {
		if(averageDuration !=null) {
			this.averageDuration = new Seconds();
			this.averageDuration.setValue(averageDuration);
		}
	}
	
	public Double getCostPerView() {
		return costPerView;
	}
	public void setCostPerView(Double costPerView) {
		this.costPerView = costPerView;
	}
	
	public Percentage getQ1percent() {
		return q1percent;
	}
	public void setQ1percent(Double q1percent) {
		if(q1percent != null) {
			this.q1percent = new Percentage();
			this.q1percent.setValue(q1percent);
		}
	}
	
	public Percentage getQ2percent() {
		return q2percent;
	}
	public void setQ2percent(Double q2percent) {
		if(q2percent != null) {
			this.q2percent = new Percentage();
			this.q2percent.setValue(q2percent);
		}
	}
	
	public Percentage getQ3percent() {
		return q3percent;
	}
	public void setQ3percent(Double q3percent) {
		if(q3percent != null) {
			this.q3percent = new Percentage();
			this.q3percent.setValue(q3percent);
		}
	}
	
	public Percentage getQ4percent() {
		return q4percent;
	}
	public void setQ4percent(Double q4percent) {
		if(q4percent != null) {
			this.q4percent = new Percentage();
			this.q4percent.setValue(q4percent);
		}
	}
	
	public Double getEngagementScore() {
		return engagementScore;
	}
	public void setEngagementScore(Double engagementScore) {
		this.engagementScore = engagementScore;
	}
	
	public Date getDay() {
		return day;
	}
	public void setDay(Date day) {
		this.day = day;
	}
	
	@JsonIgnore
	public abstract BaseReportDtoMixin getMixin();
}

package com.adfonic.reporting.service.advertiser.dto.mixin;

import java.util.Date;

import com.adfonic.reporting.service.ColumnUtil;
import com.adfonic.reporting.service.datatype.Currency;
import com.adfonic.reporting.service.datatype.Percentage;
import com.adfonic.reporting.service.datatype.Seconds;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class BaseReportDtoMixin {

	@JsonProperty(ColumnUtil.IMPRESSIONS)
	private Long totalImpressions;
	
	@JsonProperty(ColumnUtil.CLICKS)
	private Long totalClicks;
	
	@JsonProperty(ColumnUtil.CONVERSIONS)
	private Long totalConversions;
	
	@JsonProperty(ColumnUtil.TOTAL_VIEWS)
	private Long totalViews;
	
	@JsonProperty(ColumnUtil.COMPLETED_VIEWS)
	private Long completedViews;
	
	@JsonProperty(ColumnUtil.CTR)
	private Double ctr;
	
	@JsonProperty(ColumnUtil.ECPM)
	private Double ecpm;
	
	@JsonProperty(ColumnUtil.ECPC)
	private Double ecpc;
	
	@JsonProperty(ColumnUtil.CONVERSION_PERCENT)
	private Double clickConversion; 
	
	@JsonProperty(ColumnUtil.COST_PER_CONVERSION)
	private Double costPerConversion; 
	
	@JsonProperty(ColumnUtil.COST_PER_VIEW)
	private Double costPerView;
	
	@JsonProperty(ColumnUtil.ENGAGEMENT_SCORE)
	private Double engagementScore;
	
	@JsonProperty(ColumnUtil.Q1_PERCENT)
	private Percentage q1percent;
	
	@JsonProperty(ColumnUtil.Q2_PERCENT)
	private Percentage q2percent;
	
	@JsonProperty(ColumnUtil.Q3_PERCENT)
	private Percentage q3percent;
	
	@JsonProperty(ColumnUtil.Q4_PERCENT)
	private Percentage q4percent;
	
	@JsonProperty(ColumnUtil.COST)
	private Currency totalCost;
	
	@JsonProperty(ColumnUtil.AVERAGE_DURATION)
	private Seconds averageDuration;
	
	@JsonProperty(ColumnUtil.DATE)
	private Date day;
}

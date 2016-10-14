package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AUDIENCE_DATA_FEE")
public class AudienceDataFee extends BusinessKey {

	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue @Column(name="ID")
	private long id;
	@Column(name="START_DATE", nullable=false)
	private Date startTime;
	@Column(name="END_DATE", nullable=true)
	private Date endTime;
	@Embedded
	private AudiencePrices audiencePrices;
    @Column(name="IS_MAXIMUM_FOR_VENDOR",nullable=false)
    private boolean isMaximumForVendor;
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CAMPAIGN_AUDIENCE_ID", nullable = true)
	private CampaignAudience campaignAudience;
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CAMPAIGN_DATA_FEE_ID", nullable = false)
	private CampaignDataFee campaignDataFee;
    
    @Override
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public BigDecimal getDataRetail() {
		return audiencePrices.getDataRetail();
	}
	public void setDataRetail(BigDecimal dataRetail) {
		if (audiencePrices==null){
			audiencePrices = new AudiencePrices();
		}
		this.audiencePrices.setDataRetail(dataRetail);
	}
	public BigDecimal getDataWholesale() {
		return audiencePrices.getDataWholesale();
	}
	public void setDataWholesale(BigDecimal dataWholesale) {
		if (audiencePrices==null){
			audiencePrices = new AudiencePrices();
		}
		this.audiencePrices.setDataWholesale(dataWholesale);
	}
	public CampaignAudience getCampaignAudience() {
		return campaignAudience;
	}
	public void setCampaignAudience(CampaignAudience campaignAudience) {
		this.campaignAudience = campaignAudience;
	}
	public CampaignDataFee getCampaignDataFee() {
		return campaignDataFee;
	}
	public void setCampaignDataFee(CampaignDataFee campaignDataFee) {
		this.campaignDataFee = campaignDataFee;
	}
	public boolean isMaximumForVendor() {
		return isMaximumForVendor;
	}
	public void setMaximumForVendor(boolean isMaximumForVendor) {
		this.isMaximumForVendor = isMaximumForVendor;
	}
	
}

package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="CAMPAIGN_DATA_FEE")
public class CampaignDataFee extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=true)
    private Campaign campaign;
    @Column(name="AMOUNT",nullable=false)
    private BigDecimal dataFee;
    @Column(name="START_DATE",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    @OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "CAMPAIGN_DATA_FEE_ID", nullable = true)
    private Set<AudienceDataFee> audienceDataFee;
    
    public CampaignDataFee() {
    	
    }
    
    public CampaignDataFee(Campaign campaign, Date startDate) {
		this.campaign = campaign;
		this.startDate = startDate;
	}
    
	public CampaignDataFee(Campaign campaign, Date startDate, BigDecimal dataFee) {
		this.campaign = campaign;
		this.startDate = startDate;
		this.dataFee = dataFee;
	}
	@Override
	public long getId() {
		return id;
	}
	public Campaign getCampaign() {
		return campaign;
	}
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}
	public BigDecimal getDataFee() {
		return dataFee;
	}
	public void setDataFee(BigDecimal dataFee) {
		this.dataFee = dataFee;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Set<AudienceDataFee> getAudienceDataFee() {
		return audienceDataFee;
	}

	public void setAudienceDataFee(Set<AudienceDataFee> audienceDataFee) {
		this.audienceDataFee = audienceDataFee;
	}
}

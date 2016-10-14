package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="CAMPAIGN_BID")
public class CampaignBid extends BusinessKey {
    private static final long serialVersionUID = 1L;

    public enum BidModelType { NORMAL, SECOND_PRICE, DSP_LIC }
    
    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=true)
    private Campaign campaign;
    @Column(name="BID_TYPE",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private BidType bidType;
    @Column(name="AMOUNT",nullable=false)
    private BigDecimal amount;
    @Column(name="START_DATE",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    @Column(name="MAXIMUM",nullable=false)
    private boolean maximum;
    @Column(name="BID_MODEL_TYPE",length=16,nullable=false)
    @Enumerated(EnumType.STRING)
    private BidModelType bidModelType;


    {
	this.startDate = new Date();
    }
    
    CampaignBid() {}
    
    CampaignBid(Campaign campaign, BidType bidType, BigDecimal amount, BidModelType modelType) {
	this.campaign = campaign;
	this.bidType = bidType;
	this.amount = amount;
	this.bidModelType = modelType;
    }

    public long getId() { return id; };
    
    public Campaign getCampaign() { return campaign; }
    
    public BidType getBidType() { return bidType; }
    
    public BigDecimal getAmount() { return amount; }
    
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

	public boolean isMaximum() {
		return maximum;
	}

	public void setMaximum(boolean maximum) {
		this.maximum = maximum;
	}

	public BidModelType getBidModelType() {
		return bidModelType;
	}

	public void setBidModelType(BidModelType bidModelType) {
		this.bidModelType = bidModelType;
	}
}

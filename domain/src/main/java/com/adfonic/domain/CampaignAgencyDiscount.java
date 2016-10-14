package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="CAMPAIGN_AGENCY_DISCOUNT")
public class CampaignAgencyDiscount extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=true)
    private Campaign campaign;
    @Column(name="DISCOUNT",nullable=false)
    private BigDecimal discount;
    @Column(name="START_DATE",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    {
	this.startDate = new Date();
    }

    CampaignAgencyDiscount() {}
    
    CampaignAgencyDiscount(Campaign campaign, BigDecimal discount) {
    	this.campaign = campaign;
    	this.discount = discount;
    }

	public BigDecimal getDiscount() {
		return discount;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
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

	public long getId() {
		return id;
	}
    
    
}

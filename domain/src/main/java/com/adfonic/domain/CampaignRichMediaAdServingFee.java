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
@Table(name="CAMPAIGN_RM_AD_SERVING_FEE")
public class CampaignRichMediaAdServingFee extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=true)
    private Campaign campaign;
    @Column(name="RM_AD_SERVING_FEE",nullable=false)
    private BigDecimal richMediaAdServingFee;
    @Column(name="START_DATE",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    
    CampaignRichMediaAdServingFee() {}
    
	public CampaignRichMediaAdServingFee(Campaign campaign, BigDecimal richMediaAdServingFee) {
		this.campaign = campaign;
		this.richMediaAdServingFee = richMediaAdServingFee;
	}
	
	public long getId() { return id; }
	public Campaign getCampaign() {	return campaign; }
	public BigDecimal getRichMediaAdServingFee() { return richMediaAdServingFee; }

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
}

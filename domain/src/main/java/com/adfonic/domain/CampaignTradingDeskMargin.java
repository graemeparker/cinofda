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
@Table(name="CAMPAIGN_TRADING_DESK_MARGIN")
public class CampaignTradingDeskMargin extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=true)
    private Campaign campaign;
    @Column(name="TRADING_DESK_MARGIN",nullable=false)
    private BigDecimal tradingDeskMargin;
    @Column(name="START_DATE",nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Column(name="END_DATE",nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    
    CampaignTradingDeskMargin() {}
	public CampaignTradingDeskMargin(Campaign campaign, BigDecimal tradingDeskMargin) {
		this.campaign = campaign;
		this.tradingDeskMargin = tradingDeskMargin;
	}

	public long getId() { return id; }
	public Campaign getCampaign() {	return campaign; }
	public BigDecimal getTradingDeskMargin() { return tradingDeskMargin; }
	
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

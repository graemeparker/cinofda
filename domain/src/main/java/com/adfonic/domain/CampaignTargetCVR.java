package com.adfonic.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="CAMPAIGN_TARGET_CVR")
public class CampaignTargetCVR extends BusinessKey {

    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    // Careful here. Even tho the relationship is marked as LAZY, OneToOne lazies are not supported by Hibernate.
    // The object will always be hydrated.
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CAMPAIGN_ID",nullable=false)
    private Campaign campaign;
    @Column(name="TARGET_CVR",nullable=false)
    private BigDecimal targetCVR;

    public long getId() { return id; };

    public Campaign getCampaign() { 
    	return campaign; 
    }
    
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

	public BigDecimal getTargetCVR() {
		return targetCVR;
	}

	public void setTargetCVR(BigDecimal targetCVR) {
		this.targetCVR = targetCVR;
	}

}

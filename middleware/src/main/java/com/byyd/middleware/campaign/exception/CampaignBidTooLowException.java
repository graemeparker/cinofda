package com.byyd.middleware.campaign.exception;

import java.math.BigDecimal;

import com.adfonic.domain.Campaign;

public class CampaignBidTooLowException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final BigDecimal bidAmount;
    private final BigDecimal minAmount;
    private final Campaign campaign;

    public CampaignBidTooLowException(Campaign campaign, BigDecimal bidAmount, BigDecimal minAmount) {
        this.bidAmount = bidAmount;
        this.minAmount = minAmount;
        this.campaign = campaign;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public Campaign getCampaign() {
        return campaign;
    }



}

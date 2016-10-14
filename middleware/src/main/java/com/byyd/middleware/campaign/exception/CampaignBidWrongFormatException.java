package com.byyd.middleware.campaign.exception;

import java.math.BigDecimal;

import com.adfonic.domain.Campaign;

public class CampaignBidWrongFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final BigDecimal bidAmount;
    private final Campaign campaign;

    public CampaignBidWrongFormatException(Campaign campaign, BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
        this.campaign = campaign;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public Campaign getCampaign() {
        return campaign;
    }


}

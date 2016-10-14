package com.adfonic.webservices.dto;

import java.math.BigDecimal;

import com.adfonic.domain.BidType;

public class CampaignBidDTO {

    private BidType type;// bidType

    private BigDecimal amount;


    public BidType getType() {
        return type;
    }


    public void setType(BidType type) {
        this.type = type;
    }


    public BigDecimal getAmount() {
        return amount;
    }


    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

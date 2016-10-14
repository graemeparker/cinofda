package com.adfonic.domain.cache.dto.datacollector.campaign;

import java.math.BigDecimal;
import java.util.Date;

import com.adfonic.domain.BidType;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CampaignBidDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private BidType bidType;
    private BigDecimal amount;
    private Date startDate;
    private Date endDate;
    private boolean maximum;
    private BidModelType bidModelType;

    public BidType getBidType() {
        return bidType;
    }

    public void setBidType(BidType bidType) {
        this.bidType = bidType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    @Override
    public String toString() {
        return "CampaignBidDto {" + getId() + ", bidType=" + bidType + ", amount=" + amount + ", startDate=" + startDate + ", endDate=" + endDate + ", maximum=" + maximum
                + ", bidModelType=" + bidModelType + "}";
    }

}

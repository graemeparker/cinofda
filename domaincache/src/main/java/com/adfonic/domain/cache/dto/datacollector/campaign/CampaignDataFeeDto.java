package com.adfonic.domain.cache.dto.datacollector.campaign;

import java.math.BigDecimal;
import java.util.Date;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CampaignDataFeeDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private BigDecimal amount;
    private Date startDate;
    private Date endDate;

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

    @Override
    public String toString() {
        return "CampaignDataFeeDto {" + getId() + ", amount=" + amount + ", startDate=" + startDate + ", endDate=" + endDate + "}";
    }

}

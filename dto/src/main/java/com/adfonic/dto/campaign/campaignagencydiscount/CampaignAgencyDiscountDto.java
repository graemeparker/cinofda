package com.adfonic.dto.campaign.campaignagencydiscount;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class CampaignAgencyDiscountDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "discount")
    private BigDecimal discount;

    @Source(value = "startDate")
    private Date startDate;

    @Source(value = "endDate")
    private Date endDate;

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = (startDate == null ? null : new Date(startDate.getTime()));
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = (endDate == null ? null : new Date(endDate.getTime()));
    }

}

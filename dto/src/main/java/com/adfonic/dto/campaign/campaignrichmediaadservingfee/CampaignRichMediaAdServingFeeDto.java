package com.adfonic.dto.campaign.campaignrichmediaadservingfee;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class CampaignRichMediaAdServingFeeDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @Source(value = "richMediaAdServingFee")
    private BigDecimal richMediaAdServingFee;

    @Source(value = "startDate")
    private Date startDate;

    @Source(value = "endDate")
    private Date endDate;

    public BigDecimal getRichMediaAdServingFee() {
        return richMediaAdServingFee;
    }

    public void setRichMediaAdServingFee(BigDecimal richMediaAdServingFee) {
        this.richMediaAdServingFee = richMediaAdServingFee;
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
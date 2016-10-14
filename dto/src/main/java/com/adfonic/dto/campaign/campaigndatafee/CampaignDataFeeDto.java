package com.adfonic.dto.campaign.campaigndatafee;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class CampaignDataFeeDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "dataFee")
    private BigDecimal dataFee;

    @Source(value = "startDate")
    private Date startDate;

    @Source(value = "endDate")
    private Date endDate;

    public BigDecimal getDataFee() {
        return dataFee;
    }

    public void setDataFee(BigDecimal dataFee) {
        this.dataFee = dataFee;
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
package com.adfonic.domain.cache.dto.datacollector.publication;

import java.math.BigDecimal;
import java.util.Date;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class PublisherRevShareDto extends BusinessKeyDto {
    private static final long serialVersionUID = 1L;

    private BigDecimal revShare;
    private Date startDate;
    private Date endDate;

    public BigDecimal getRevShare() {
        return revShare;
    }

    public void setRevShare(BigDecimal revShare) {
        this.revShare = revShare;
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
        return "PublisherRevShareDto {" + getId() + ", revShare=" + revShare + ", startDate=" + startDate + ", endDate=" + endDate + "}";
    }

}

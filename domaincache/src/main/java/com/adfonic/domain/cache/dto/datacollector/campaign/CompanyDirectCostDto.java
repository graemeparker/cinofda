package com.adfonic.domain.cache.dto.datacollector.campaign;

import java.math.BigDecimal;
import java.util.Date;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CompanyDirectCostDto extends BusinessKeyDto {
    private static final long serialVersionUID = 1L;

    private BigDecimal directCost;
    private Date startDate;
    private Date endDate;

    public BigDecimal getDirectCost() {
		return directCost;
	}

	public void setDirectCost(BigDecimal directCost) {
		this.directCost = directCost;
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
		return "CompanyDirectCostDto [directCost=" + directCost + ", startDate=" + startDate + ", endDate=" + endDate
				+ "]";
	}


}

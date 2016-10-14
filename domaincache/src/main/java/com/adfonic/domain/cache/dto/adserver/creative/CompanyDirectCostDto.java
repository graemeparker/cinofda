package com.adfonic.domain.cache.dto.adserver.creative;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CompanyDirectCostDto extends BusinessKeyDto {
    private static final long serialVersionUID = 1L;

    private double directCost;

	public double getDirectCost() {
		return directCost;
	}

	public void setDirectCost(double directCost) {
		this.directCost = directCost;
	}

	@Override
	public String toString() {
		return "CompanyDirectCostDto [directCost=" + directCost + "]";
	}




}

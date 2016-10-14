package com.adfonic.domain.cache.dto.adserver.creative;

import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CampaignBidDto extends BusinessKeyDto {
    private static final long serialVersionUID = 4L;

    private BidType bidType;
    private double amount;
    private boolean maximum;
    private BidModelType bidModelType;
    private BudgetType budgetType;

    public BidType getBidType() {
        return bidType;
    }

    public void setBidType(BidType bidType) {
        this.bidType = bidType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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
    
    public BudgetType getBudgetType() {
		return budgetType;
	}

	public void setBudgetType(BudgetType budgetType) {
		this.budgetType = budgetType;
	}

	@Override
    public String toString() {
        return "CampaignBidDto {" + getId() + ", bidType=" + bidType + ", amount=" + amount + ", maximum=" + maximum + ", bidModelType=" + bidModelType + "}";
    }

}

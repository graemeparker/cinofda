package com.adfonic.domain.cache.dto.adserver.creative;

import java.util.HashMap;
import java.util.Map;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CompanyDto extends BusinessKeyDto {
    private static final long serialVersionUID = 4L;

    @Deprecated
    private double discount; // needed for pricing info and also RTB price calc
    private boolean backfill;
    private double mediaCostMargin;
    //AD-367
    private double marginShareDSP;
    
    private CompanyDirectCostDto directCost;
    
    // MAD-3168 - Enable/disable rtb seats id
    private boolean enableRtbBidSeat;
    
    // MAD-3168 - Map whose key is the publisher id and the value the seatId 
    private Map<Long, String> rtbBidSeats = new HashMap<Long, String>();

    @Deprecated
    public double getDiscount() {
        return discount;
    }

    @Deprecated
    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public boolean isBackfill() {
        return backfill;
    }

    public void setBackfill(boolean backfill) {
        this.backfill = backfill;
    }

    public double getMediaCostMargin() {
        return mediaCostMargin;
    }

    public void setMediaCostMargin(double mediaCostMargin) {
        this.mediaCostMargin = mediaCostMargin;
    }

    public boolean isSaaS() {
        return mediaCostMargin > 0;
    }

    //AD-367
    public double getMarginShareDSP() {
        return marginShareDSP;
    }

    public void setMarginShareDSP(double marginShareDSP) {
        this.marginShareDSP = marginShareDSP;
    }

	public double getDirectCostOrZero() {
		if (directCost == null)
			return 0.0;
		else
			return directCost.getDirectCost();
	}
    
    public CompanyDirectCostDto getDirectCost() {
        return directCost;
    }

    public void setDirectCost(CompanyDirectCostDto directCost) {
        this.directCost = directCost;
    }
    
    public boolean isEnableRtbBidSeat() {
        return enableRtbBidSeat;
    }

    public void setEnableRtbBidSeat(boolean enableRtbBidSeat) {
        this.enableRtbBidSeat = enableRtbBidSeat;
    }

    public Map<Long, String> getRtbBidSeats() {
        return rtbBidSeats;
    }

    @Override
    public String toString() {
        return "CompanyDto {" + getId() + ", discount=" + discount + ", backfill=" + backfill + ", mediaCostMargin=" + mediaCostMargin + ", marginShareDSP=" + marginShareDSP
                + ", directCost=" + directCost + ", enableRtbBidSeat=" + enableRtbBidSeat + ", #rtbBidSeats=" + rtbBidSeats.size() + "}";
    }

}

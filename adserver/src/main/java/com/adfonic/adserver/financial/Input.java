package com.adfonic.adserver.financial;

import com.adfonic.domain.AdAction;

public class Input {
    
    public AdAction adAction;
    public Double settlementPrice;
    public Double publisherRevShare;
    public Double buyerPremium;
    public Double directCost;
    public Double mediaCostMarkup;
    public Double marginShareDSP;
    public Double dataFee;
    public Double richMediaFee;
    public Double campaignDiscount;
    public Double bidAmout;
    
	@Override
	public String toString() {
		return "Input [adAction=" + adAction + ", settlementPrice=" + settlementPrice + ", publisherRevShare="
				+ publisherRevShare + ", buyerPremium=" + buyerPremium + ", directCost=" + directCost
				+ ", mediaCostMarkup=" + mediaCostMarkup + ", marginShareDSP=" + marginShareDSP + ", dataFee=" + dataFee
				+ ", richMediaFee=" + richMediaFee + ", campaignDiscount=" + campaignDiscount + ", bidAmout=" + bidAmout
				+ "]";
	}
    
}

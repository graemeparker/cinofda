package com.adfonic.adserver.financial;

import java.math.BigDecimal;

public class Output {

    double payout;
    double buyer_premium;
    double direct_cost;
    double tech_fee;
    double data_fee;
    double third_pas_fee;
    double dsp_margin;
    double cust_margin;
    double campaign_discount;
    double accounting_cost;

    public BigDecimal getPayout() {
        return new BigDecimal(payout);
    }

    public BigDecimal getBuyer_premium() {
        return new BigDecimal(buyer_premium);
    }

    public BigDecimal getDirect_cost() {
        return new BigDecimal(direct_cost);
    }

    public BigDecimal getTech_fee() {
        return new BigDecimal(tech_fee);
    }

    public BigDecimal getData_fee() {
        return new BigDecimal(data_fee);
    }

    public BigDecimal getThird_pas_fee() {
        return new BigDecimal(third_pas_fee);
    }

    public BigDecimal getDsp_margin() {
        return new BigDecimal(dsp_margin);
    }

    public BigDecimal getCust_margin() {
        return new BigDecimal(cust_margin);
    }

    public BigDecimal getCampaign_discount() {
        return new BigDecimal(campaign_discount);
    }

    public BigDecimal getAccountingCost() {
    	return new BigDecimal(accounting_cost);
    }
}

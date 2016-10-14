package com.adfonic.adserver.financial;


public class LicenseCPM_RTB implements FinancialCalc {
    
    @Override
    public Output calculate(final Input input) {

        switch (input.adAction) {
        case AD_SERVED:
            return adServed(input);
        case CLICK:
            return click(input);
        default:
            return new Output();
        }
    }

    Output click(Input input) {
        Output out = new Output();
        return out;
    }

    Output adServed(Input i) {
        Output o = new Output();

        o.payout = i.settlementPrice / 1000.0;
        o.buyer_premium = o.payout * i.buyerPremium;
        o.direct_cost = (o.payout+o.buyer_premium)*i.directCost;
        o.tech_fee = (o.payout+o.buyer_premium+o.direct_cost)/(1-i.mediaCostMarkup)-(o.payout+o.buyer_premium+o.direct_cost);
        o.data_fee = i.dataFee / 1000.0;
        o.third_pas_fee = i.richMediaFee / 1000.0;
        
//        o.dsp_margin =;
//        out.cust_margin;
        double sum = o.payout+o.buyer_premium+o.direct_cost+o.tech_fee+o.data_fee+o.third_pas_fee;
        o.campaign_discount =sum /(1-i.campaignDiscount)-sum;
        o.accounting_cost = o.payout + o.buyer_premium + o.direct_cost + o.tech_fee + o.data_fee + o.third_pas_fee + o.campaign_discount;
        return o;
    }
}

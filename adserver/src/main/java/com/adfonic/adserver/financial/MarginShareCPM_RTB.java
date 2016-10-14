package com.adfonic.adserver.financial;


public class MarginShareCPM_RTB implements FinancialCalc {
    
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
        // out.direct_cost;
        // out.tech_fee;
        o.data_fee = i.dataFee / 1000.0;
        o.third_pas_fee = i.richMediaFee / 1000.0;

        double x = (i.bidAmout * i.campaignDiscount) / 1000;
        
        o.dsp_margin = ((i.bidAmout / 1000) - x - o.payout - o.buyer_premium) * i.marginShareDSP;
        o.cust_margin =((i.bidAmout/1000)-x-o.payout-o.buyer_premium)*(1-i.marginShareDSP);
        o.campaign_discount = (i.bidAmout * i.campaignDiscount) / 1000;
        o.accounting_cost = ((i.settlementPrice != null) ? i.settlementPrice : i.bidAmout ) / 1000;
        return o;
    }
}

package com.adfonic.adserver.financial;


public class ManagedCPC_RTB implements FinancialCalc {
    
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

    Output click(Input i) {
        Output o = new Output();

        o.payout = 0.0;
        o.buyer_premium = i.buyerPremium *o.payout;
//        out.direct_cost;
//        out.tech_fee;
//        out.data_fee = 0.0;
//        out.third_pas_fee = 0.0;
        double x = i.bidAmout * i.campaignDiscount;
        o.dsp_margin = i.bidAmout-x;
//        out.cust_margin;
        o.campaign_discount = i.bidAmout * i.campaignDiscount;
        o.accounting_cost = i.bidAmout;
        return o;
    }

    Output adServed(Input i) {
        Output o = new Output();

        // TODO we have occasionally NPE here
        o.payout = i.settlementPrice / 1000.0;
        o.buyer_premium = i.buyerPremium *o.payout;
//        out.direct_cost;
//        out.tech_fee;
        o.data_fee = i.dataFee / 1000.0;
        o.third_pas_fee = i.richMediaFee / 1000.0;
        o.dsp_margin = -o.payout - o.buyer_premium;
//        out.cust_margin;
//        out.campaign_discount;
        o.accounting_cost = 0;
        return o;
    }
}

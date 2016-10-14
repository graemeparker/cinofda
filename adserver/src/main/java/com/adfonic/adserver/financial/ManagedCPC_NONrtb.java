package com.adfonic.adserver.financial;


public class ManagedCPC_NONrtb implements FinancialCalc {
    
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

        o.payout = (i.bidAmout*(1-i.campaignDiscount))*i.publisherRevShare;
//        out.buyer_premium;
//        out.direct_cost;
//        out.tech_fee;
//        out.data_fee = 0.0;
//        out.third_pas_fee = 0.0;
        double x = i.bidAmout * i.campaignDiscount;
        o.dsp_margin = i.bidAmout -x - o.payout;
//        out.cust_margin;
        o.campaign_discount = i.bidAmout * i.campaignDiscount;
        o.accounting_cost = i.bidAmout;
        return o;
    }

    Output adServed(Input input) {
        Output out = new Output();

//        out.payout;
//        out.buyer_premium;
//        out.direct_cost;
//        out.tech_fee;
        out.data_fee = input.dataFee / 1000.0;
        out.third_pas_fee = input.richMediaFee / 1000.0;
//        out.dsp_margin;
//        out.cust_margin;
//        out.campaign_discount;
        out.accounting_cost = 0;
        return out;
    }
}

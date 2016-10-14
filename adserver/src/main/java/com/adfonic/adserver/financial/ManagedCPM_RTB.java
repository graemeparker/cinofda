package com.adfonic.adserver.financial;


public class ManagedCPM_RTB implements FinancialCalc {
    
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
        Output out = new Output();
        return out;
    }

    Output adServed(Input i) {
        Output o = new Output();

        o.payout = i.settlementPrice / 1000.0;
        o.buyer_premium = i.buyerPremium *o.payout;
//        out.direct_cost;
//        out.tech_fee;
        o.data_fee = i.dataFee / 1000.0;
        o.third_pas_fee = i.richMediaFee / 1000.0;
        
        double dspM =(i.bidAmout / 1000.0) ;
        double x = i.bidAmout * i.campaignDiscount ; 
        x /= 1000.0;// same as campaign_discount
        dspM -= x;
        dspM -= o.payout;
        dspM -= o.buyer_premium;
        
        o.dsp_margin = dspM;
//        out.cust_margin;
        o.campaign_discount = (i.bidAmout * i.campaignDiscount) / 1000.0;
        o.accounting_cost = ((i.settlementPrice != null) ? i.settlementPrice : i.bidAmout) / 1000.0;
        return o;
    }
}

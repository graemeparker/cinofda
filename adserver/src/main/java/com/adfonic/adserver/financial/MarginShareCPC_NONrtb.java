package com.adfonic.adserver.financial;


public class MarginShareCPC_NONrtb implements FinancialCalc {
    
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

        o.payout =(i.bidAmout*(1-i.campaignDiscount))*i.publisherRevShare*i.marginShareDSP/(1+i.publisherRevShare*i.marginShareDSP-i.publisherRevShare);
//        out.buyer_premium;
//        out.direct_cost;
//        out.tech_fee;
//        out.data_fee;
//        out.third_pas_fee;
        double x= i.bidAmout*i.campaignDiscount;
        o.dsp_margin= ((i.bidAmout)-x-o.payout-o.buyer_premium)*i.marginShareDSP+o.data_fee+o.third_pas_fee;
        o.cust_margin= ((i.bidAmout)-x-o.payout-o.buyer_premium)*(1-i.marginShareDSP);
        o.campaign_discount= i.bidAmout*i.campaignDiscount;
        o.accounting_cost = i.bidAmout;
        return o;
    }

    Output adServed(Input i) {
        Output o = new Output();

//        out.payout;
//        out.buyer_premium;
        // out.direct_cost;
        // out.tech_fee;
        o.data_fee = i.dataFee / 1000.0;
        o.third_pas_fee = i.richMediaFee / 1000.0;

//        o.dsp_margin;
//        out.cust_margin;
        o.campaign_discount=(i.bidAmout*i.campaignDiscount)/1000;
        o.accounting_cost = 0;
        return o;
    }
}

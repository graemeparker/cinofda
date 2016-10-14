package com.adfonic.adserver.financial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.adfonic.domain.AdAction;

public class TestHelper {
    private double accuracy = 1e-10;

    public void testCSV(FinancialCalc testObj, String fName, AdAction adAction) throws IOException {

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("financials/"+fName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));) {

            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                
                String[] split = line.split(",");
                
                Input i = new Input();
                i.adAction = adAction;
                i.settlementPrice = parseDouble(split[0]);
                i.publisherRevShare = Double.parseDouble(split[1]);
                i.buyerPremium = Double.parseDouble(split[2]);
                i.directCost = Double.parseDouble(split[3]);
                i.mediaCostMarkup = Double.parseDouble(split[4]);
                i.marginShareDSP = Double.parseDouble(split[5]);
                i.dataFee = Double.parseDouble(split[6]);
                i.richMediaFee = Double.parseDouble(split[7]);
                i.campaignDiscount = Double.parseDouble(split[8]);
                i.bidAmout = Double.parseDouble(split[9]);
 
                Output o = new Output();
                o.payout = Double.parseDouble(split[12]);
                o.buyer_premium = Double.parseDouble(split[13]);
                o.direct_cost = Double.parseDouble(split[14]);
                o.tech_fee = Double.parseDouble(split[15]);
                o.data_fee = Double.parseDouble(split[16]);
                o.third_pas_fee = Double.parseDouble(split[17]);
                o.dsp_margin = Double.parseDouble(split[18]);
                o.cust_margin = parseDouble(split[19]);
                o.campaign_discount = Double.parseDouble(split[20]);
                
                Output out = testObj.calculate(i);
                assertEq(o, out);
            }
        }
    }


    private Double parseDouble(String string) {
        if(StringUtils.isBlank(string)) {
            return 0.0;
        }
        if("null".equals(string.trim().toLowerCase())) {
            return null;
        }
        return Double.parseDouble(string.trim());
    }
    
    
    public void assertEq(Output o1 , Output o2) {
        
        Assert.assertEquals(o1. payout            ,  o2.payout              ,accuracy);
        Assert.assertEquals(o1. buyer_premium     ,  o2. buyer_premium      ,accuracy);
        Assert.assertEquals(o1. direct_cost       ,  o2. direct_cost        ,accuracy);
        Assert.assertEquals(o1. tech_fee          ,  o2. tech_fee           ,accuracy);
        Assert.assertEquals(o1. data_fee          ,  o2. data_fee           ,accuracy);
        Assert.assertEquals(o1. third_pas_fee     ,  o2. third_pas_fee      ,accuracy);
        Assert.assertEquals(o1. dsp_margin        ,  o2. dsp_margin         ,accuracy);
        Assert.assertEquals(o1. cust_margin       ,  o2. cust_margin        ,accuracy);
        Assert.assertEquals(o1. campaign_discount ,  o2. campaign_discount  ,accuracy);
    }
    
}

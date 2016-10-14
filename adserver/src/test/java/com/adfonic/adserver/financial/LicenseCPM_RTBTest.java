package com.adfonic.adserver.financial;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.adfonic.domain.AdAction;

public class LicenseCPM_RTBTest {

    LicenseCPM_RTB testObj = new LicenseCPM_RTB();
    private double accuracy = 1e-10;

    @Test 
    public void unknownAction() {
        Input input = new Input();
        input.adAction = AdAction.BID_FAILED;
        
        // act
        Output out = testObj.calculate(input);
        
        Assert.assertEquals(0, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0, out.accounting_cost, accuracy);
    }
    
    @Test
    public void testAdServed3() {

        Input input = new Input();
        input.adAction = AdAction.AD_SERVED;
        input.settlementPrice = 1.0;
        input. publisherRevShare = 0.0;
        input. buyerPremium = 0.0;
        input. directCost = 0.0;
        input. mediaCostMarkup = 0.10;
        input.marginShareDSP = 0.0;
        input.dataFee = 0.0;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 5.0;

        // act
        Output out = testObj.adServed(input);

        Assert.assertEquals(0.001, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0.0001111111, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0.0011111111, out.accounting_cost, accuracy);
    }
    
    @Test
    public void testAdServed3_2() {

        Input input = new Input();
        input.adAction = AdAction.AD_SERVED;
        input.settlementPrice = 2.0;
        input. publisherRevShare = 0.0;
        input. buyerPremium = 0.0;
        input. directCost = 0.0;
        input. mediaCostMarkup = 0.10;
        input.marginShareDSP = 0.0;
        input.dataFee = 0.0;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 5.0;

        // act
        Output out = testObj.adServed(input);

        Assert.assertEquals(0.002, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0.0002222222, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0.0022222222, out.accounting_cost, accuracy);
    }
    
    @Test
    public void testAdServed119() {

        Input input = new Input();
        input.adAction = AdAction.AD_SERVED;
        input.settlementPrice = 1.19;
        input. publisherRevShare = 0.60;
        input. buyerPremium = 0.0;
        input. directCost = 0.10;
        input. mediaCostMarkup = 0.16;
        input.marginShareDSP = 0.0;
        input.dataFee = 0.0;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 3.0;

        // act
        Output out = testObj.adServed(input);

        Assert.assertEquals(0.00119, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0.000119, out.direct_cost, accuracy);
        Assert.assertEquals(0.0002493333, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0.0015583333, out.accounting_cost, accuracy);
    }
    
  //------------------------------------------------------------
    
    @Test
    public void csvAdServed() throws IOException{
        new TestHelper().testCSV(testObj, "License CPM on RTB-AD_SERVED.csv", AdAction.AD_SERVED);
    }
    
    @Test
    public void csvClick() throws IOException{
        new TestHelper().testCSV(testObj,"License CPM on RTB-CLICK.csv", AdAction.CLICK);
    }
    

}

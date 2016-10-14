package com.adfonic.adserver.financial;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.adfonic.domain.AdAction;

public class MarginShareCPC_RTBTest {

    MarginShareCPC_RTB testObj = new MarginShareCPC_RTB();
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
        input.publisherRevShare = 0.0;
        input.buyerPremium = 0.0;
        input.directCost = 0.0;
        input.mediaCostMarkup = 0.0;
        input.marginShareDSP = 0.50;
        input.dataFee = 0.0;
        input.richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 0.05;

        // act
        Output out = testObj.adServed(input);

        Assert.assertEquals(0.001, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(-0.000500, out.dsp_margin, accuracy);
        Assert.assertEquals(-0.000500, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0, out.accounting_cost, accuracy);
    }

    @Test
    public void testAdServed3_v2() {

        Input input = new Input();
        input.adAction = AdAction.AD_SERVED;
        input.settlementPrice = 2.0;
        input.publisherRevShare = 0.0;
        input.buyerPremium = 0.0;
        input.directCost = 0.0;
        input.mediaCostMarkup = 0.0;
        input.marginShareDSP = 0.50;
        input.dataFee = 0.0;
        input.richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 0.05;

        // act
        Output out = testObj.adServed(input);

        Assert.assertEquals(0.002, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(-0.001, out.dsp_margin, accuracy);
        Assert.assertEquals(-0.001, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0, out.accounting_cost, accuracy);
    }
    
    @Test
    public void testClick3_v2() {
    	
    	Input input = new Input();
    	input.adAction = AdAction.AD_SERVED;
    	input.settlementPrice = 0.02;
    	input.publisherRevShare = 0.0;
    	input.buyerPremium = 0.0;
    	input.directCost = 0.0;
    	input.mediaCostMarkup = 0.0;
    	input.marginShareDSP = 0.50;
    	input.dataFee = 0.0;
    	input.richMediaFee = 0.0;
    	input.campaignDiscount = 0.0;
    	input.bidAmout = 0.05;
    	
    	// act
    	Output out = testObj.click(input);
    	
    	Assert.assertEquals(0, out.payout, accuracy);
    	Assert.assertEquals(0, out.buyer_premium, accuracy);
    	Assert.assertEquals(0, out.direct_cost, accuracy);
    	Assert.assertEquals(0, out.tech_fee, accuracy);
    	Assert.assertEquals(0, out.data_fee, accuracy);
    	Assert.assertEquals(0, out.third_pas_fee, accuracy);
    	Assert.assertEquals(0.025, out.dsp_margin, accuracy);
    	Assert.assertEquals(0.025, out.cust_margin, accuracy);
    	Assert.assertEquals(0, out.campaign_discount, accuracy);
    	Assert.assertEquals(0.02, out.accounting_cost, accuracy);
    }

    // ------------------------------------------------------------

    @Test
    public void csvAdServed() throws IOException {
        new TestHelper().testCSV(testObj, "MarginShare CPC on RTB-AD_SERVED.csv", AdAction.AD_SERVED);
    }

    @Test
    public void csvClick() throws IOException {
        new TestHelper().testCSV(testObj, "MarginShare CPC on RTB-CLICK.csv", AdAction.CLICK);
    }

}

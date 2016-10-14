package com.adfonic.adserver.financial;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.adfonic.domain.AdAction;

public class ManagedCPM_RTBTest {

    ManagedCPM_RTB testObj = new ManagedCPM_RTB();
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
        // input. publisherRevShare;
         input. buyerPremium = 0.0;
        // input. directCost;
        // input. mediaCostMarkup;
        input.marginShareDSP = 1.0;
        input.dataFee = 0.0;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 5.0;

        // act
        Output out = testObj.adServed(input);

        Assert.assertEquals(0.001, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0.004, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0.001, out.accounting_cost, accuracy);
    }
    
    @Test
    public void testAdServed3_v2() {
        
        Input input = new Input();
        input.adAction = AdAction.AD_SERVED;
        input.settlementPrice = 2.0;
        // input. publisherRevShare;
        input. buyerPremium = 0.0;
        // input. directCost;
        // input. mediaCostMarkup;
        input.marginShareDSP = 1.0;
        input.dataFee = 0.0;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 5.0;
        
        // act
        Output out = testObj.adServed(input);
        
        Assert.assertEquals(0.002, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0.003, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0.002, out.accounting_cost, accuracy);
    }

    @Test
    public void testAdServed4() {

        Input input = new Input();
        input.adAction = AdAction.AD_SERVED;
        input.settlementPrice = 1.0;
        // input. publisherRevShare;
         input. buyerPremium = 0.0;
        // input. directCost;
        // input. mediaCostMarkup;
        input.marginShareDSP = 1.0;
        input.dataFee = 0.0;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.15;
        input.bidAmout = 5.0;

        // act
        Output out = testObj.adServed(input);

        Assert.assertEquals(0.001, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0.00325, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0.00075, out.campaign_discount, accuracy);
        Assert.assertEquals(0.001, out.accounting_cost, accuracy);
    }
    
    @Test
    public void testAdServed5() {
        
        Input input = new Input();
        input.adAction = AdAction.AD_SERVED;
        input.settlementPrice = 1.0;
        // input. publisherRevShare;
        input. buyerPremium = 0.15;
        // input. directCost;
        // input. mediaCostMarkup;
        input.marginShareDSP = 1.0;
        input.dataFee = 0.0;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 5.0;
        
        // act
        Output out = testObj.adServed(input);
        
        Assert.assertEquals(0.001, out.payout, accuracy);
        Assert.assertEquals(0.00015, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0.00385, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0.001, out.accounting_cost, accuracy);
    }
    
    @Test
    public void testAdServed6() {
        
        Input input = new Input();
        input.adAction = AdAction.AD_SERVED;
        input.settlementPrice = 1.0;
        // input. publisherRevShare;
        input. buyerPremium = 0.15;
        // input. directCost;
        // input. mediaCostMarkup;
        input.marginShareDSP = 1.0;
        input.dataFee = 0.0;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.15;
        input.bidAmout = 5.0;
        
        // act
        Output out = testObj.adServed(input);
        
        Assert.assertEquals(0.001, out.payout, accuracy);
        Assert.assertEquals(0.00015, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0, out.tech_fee, accuracy);
        Assert.assertEquals(0, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0.0031, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0.00075, out.campaign_discount, accuracy);
        Assert.assertEquals(0.001, out.accounting_cost, accuracy);
    }
    
    @Test
    public void testAdServed7() {
        
        Input input = new Input();
        input.adAction = AdAction.AD_SERVED;
        input.settlementPrice = 1.0;
        // input. publisherRevShare;
        input. buyerPremium = 0.0;
        // input. directCost;
        // input. mediaCostMarkup;
        input.marginShareDSP = 1.0;
        input.dataFee = 0.50;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 5.0;
        
        // act
        Output out = testObj.adServed(input);
        
        Assert.assertEquals(0.001, out.payout, accuracy);
        Assert.assertEquals(0, out.buyer_premium, accuracy);
        Assert.assertEquals(0, out.direct_cost, accuracy);
        Assert.assertEquals(0, out.tech_fee, accuracy);
        Assert.assertEquals(0.0005, out.data_fee, accuracy);
        Assert.assertEquals(0, out.third_pas_fee, accuracy);
        Assert.assertEquals(0.004, out.dsp_margin, accuracy);
        Assert.assertEquals(0, out.cust_margin, accuracy);
        Assert.assertEquals(0, out.campaign_discount, accuracy);
        Assert.assertEquals(0.001, out.accounting_cost, accuracy);
    }
    
    //------------------------------------------------------------
    @Test
    public void testClick20() {
        
        Input input = new Input();
        input.adAction = AdAction.CLICK;
        input.settlementPrice = 0.0;
        input. publisherRevShare = 0.0;
        input. buyerPremium = 0.0;
        input. directCost = 0.0;
        input. mediaCostMarkup = 0.0;
        input.marginShareDSP = 1.0;
        input.dataFee = 0.0;
        input. richMediaFee = 0.0;
        input.campaignDiscount = 0.0;
        input.bidAmout = 5.0;
        
        // act
        Output out = testObj.click(input);
        
        Assert.assertEquals(0., out.payout, accuracy);
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
    
  //------------------------------------------------------------
    
    @Test
    public void csvAdServed() throws IOException{
        new TestHelper().testCSV(testObj, "Managed CPM on RTB-AD_SERVED.csv", AdAction.AD_SERVED);
    }
    
    @Test
    public void csvClick() throws IOException{
        new TestHelper().testCSV(testObj,"Managed CPM on RTB-CLICK.csv", AdAction.CLICK);
    }
    

}

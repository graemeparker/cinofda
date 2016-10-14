package com.byyd.middleware.campaign.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignAgencyDiscount;
import com.adfonic.domain.CampaignRichMediaAdServingFee;
import com.adfonic.domain.CampaignTradingDeskMargin;
import com.adfonic.domain.Campaign_;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class FeeManagerIT extends AbstractAdfonicTest{
    
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private FeeManager feeManager;

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testCampaignRichMediaAdServingFee() {
        FetchStrategy fs = new FetchStrategyBuilder()
                           .addLeft(Campaign_.currentRichMediaAdServingFee)
                           .addLeft(Campaign_.historicalRMAdServingFees)
                           .build();
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        double amount = 1000.00;
        CampaignRichMediaAdServingFee fee = null;
        try {
            campaign = feeManager.saveCampaignRichMediaAdServingFee(campaign.getId(), BigDecimal.valueOf(amount));
            fee = campaign.getCurrentRichMediaAdServingFee();
            assertNotNull(fee);
            long id = fee.getId();
            assertTrue(id > 0L);

            fee = feeManager.getCampaignRichMediaAdServingFeeById(id);
            assertNotNull(fee);
            assertEquals(id, fee.getId());

            fee = feeManager.getCampaignRichMediaAdServingFeeById(Long.toString(id));
            assertNotNull(fee);
            assertEquals(id, fee.getId());

            Date now = new Date();
            CampaignRichMediaAdServingFee f = campaign.getRichMediaAdServingFeeForDate(now);
            assertNotNull(f);
            assertEquals(f.getId(), f.getId());
            
            campaign = campaignManager.getCampaignById(1L, fs);
            assertTrue(campaign.getHistoricalRMAdServingFees().contains(fee));
            
            List<CampaignRichMediaAdServingFee> list = feeManager.getAllCampaignRichMediaAdServingFeesForCampaign(campaign);
            assertTrue(list.contains(fee));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            //campaignManager.delete(campaignBid);
            //assertNull(campaignManager.getCampaignBidById(campaignBid.getId()));
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testCampaignTradingDeskMargin() {
        FetchStrategy fs = new FetchStrategyBuilder()
                           .addLeft(Campaign_.currentTradingDeskMargin)
                           .addLeft(Campaign_.historicalTDMarginFees)
                           .build();
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        double amount = 1.00;
        CampaignTradingDeskMargin fee = null;
        try {
            campaign = feeManager.saveCampaignTradingDeskMargin(campaign.getId(), BigDecimal.valueOf(amount));
            fee = campaign.getCurrentTradingDeskMargin();
            assertNotNull(fee);
            long id = fee.getId();
            assertTrue(id > 0L);

            fee = feeManager.getCampaignTradingDeskMarginById(id);
            assertNotNull(fee);
            assertEquals(id, fee.getId());

            fee = feeManager.getCampaignTradingDeskMarginById(Long.toString(id));
            assertNotNull(fee);
            assertEquals(id, fee.getId());

            Date now = new Date();
            CampaignTradingDeskMargin f = campaign.getTradingDeskMarginForDate(now);
            assertNotNull(f);
            assertEquals(f.getId(), f.getId());
            
            campaign = campaignManager.getCampaignById(1L, fs);
            assertTrue(campaign.getHistoricalTDMarginFees().contains(fee));
            
            List<CampaignTradingDeskMargin> list = feeManager.getAllCampaignTradingDeskMarginsForCampaign(campaign);
            assertTrue(list.contains(fee));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        }
    }

    //----------------------------------------------------------------------------------------------------------

    @Test
    public void testCampaignAgencyDiscount() {
        FetchStrategy fs = new FetchStrategyBuilder()
                               .addLeft(Campaign_.currentAgencyDiscount)
                               .addLeft(Campaign_.historicalAgencyDiscounts)
                               .build();
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        if(campaign != null) {
            double discount = 0.33;
            CampaignAgencyDiscount campaignAgencyDiscount = null;
            try {
                campaign = feeManager.newCampaignAgencyDiscount(campaign, BigDecimal.valueOf(discount));
                campaignAgencyDiscount = campaign.getCurrentAgencyDiscount();
                assertNotNull(campaignAgencyDiscount);
                long id = campaignAgencyDiscount.getId();
                assertTrue(id > 0L);
    
                campaignAgencyDiscount = feeManager.getCampaignAgencyDiscountById(id);
                assertNotNull(campaignAgencyDiscount);
                assertEquals(id, campaignAgencyDiscount.getId());
    
                campaignAgencyDiscount = feeManager.getCampaignAgencyDiscountById(Long.toString(id));
                assertNotNull(campaignAgencyDiscount);
                assertEquals(id, campaignAgencyDiscount.getId());
    
                campaign = campaignManager.getCampaignById(1L, fs);
                assertTrue(campaign.getHistoricalAgencyDiscounts().contains(campaignAgencyDiscount));
                
                List<CampaignAgencyDiscount> list = feeManager.getAllCampaignAgencyDiscountsForCampaign(campaign);
                assertTrue(list.contains(campaignAgencyDiscount));
    
            } catch(Exception e) {
                String stackTrace = ExceptionUtils.getStackTrace(e);
                System.out.println(stackTrace);
                fail(stackTrace);
            } finally {
                //campaignManager.delete(campaignAgencyDiscount);
                //assertNull(campaignManager.getCampaignAgencyDiscountById(campaignAgencyDiscount.getId()));
            }
        }
    }

}

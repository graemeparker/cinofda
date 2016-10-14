package com.byyd.middleware.campaign.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignBid;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.CampaignTargetCTR;
import com.adfonic.domain.CampaignTargetCTR_;
import com.adfonic.domain.CampaignTargetCVR;
import com.adfonic.domain.CampaignTargetCVR_;
import com.adfonic.domain.Campaign_;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class BiddingManagerIT extends AbstractAdfonicTest{
    
    @Autowired
    private BiddingManager biddingManager;
    
    @Autowired
    private CampaignManager campaignManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetCampaignBidWithInvalidId() {
        assertNull(biddingManager.getCampaignBidById(0L));
    }

    @Test
    public void testCampaignBid() {
        FetchStrategyImpl fs = new FetchStrategyImpl();
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "historicalBids", JoinType.LEFT);
        fs.addEagerlyLoadedFieldForClass(Campaign.class, "currentBid", JoinType.LEFT);
        Campaign campaign = campaignManager.getCampaignById(1L, fs);
        BidType bidType = BidType.CPC;
        double amount = 1000.00;
        CampaignBid campaignBid = null;
        try {
            campaign = biddingManager.newCampaignBid(campaign, bidType, BigDecimal.valueOf(amount));
            campaignBid = campaign.getCurrentBid();
            assertNotNull(campaignBid);
            long id = campaignBid.getId();
            assertTrue(id > 0L);
            assertEquals(campaignBid.getBidModelType(), BidModelType.NORMAL);
            //campaign = campaignManager.update(campaign);

            campaignBid = biddingManager.getCampaignBidById(id);
            assertNotNull(campaignBid);
            assertEquals(id, campaignBid.getId());

            campaignBid = biddingManager.getCampaignBidById(Long.toString(id));
            assertNotNull(campaignBid);
            assertEquals(id, campaignBid.getId());

            Date now = new Date();
            CampaignBid cb = campaign.getBidForDate(now);
            assertNotNull(cb);
            assertEquals(cb.getId(), campaignBid.getId());
            
            campaign = campaignManager.getCampaignById(1L, fs);
            assertTrue(campaign.getHistoricalBids().contains(campaignBid));
            
            List<CampaignBid> list = biddingManager.getAllCampaignBidsForCampaign(campaign);
            assertTrue(list.contains(campaignBid));



        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            //campaignManager.delete(campaignBid);
            //assertNull(campaignManager.getCampaignBidById(campaignBid.getId()));
        }
    }

    @Test
    @Transactional
    public void testGetReferenceBids() {
        Campaign campaign = campaignManager.getCampaignById(2L);
        try {
            biddingManager.getReferenceBids(campaign, BidType.CPC);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
//----------------------------------------------------------------------------------------------------------------
    
    @Test
    public void testCampaignTargetCTR() {
        CampaignTargetCTR targetCTR = null;
        try {
            FetchStrategy fs = new FetchStrategyBuilder()
            .addLeft(Campaign_.targetCTR)
            .build();
            FetchStrategy fs2 = new FetchStrategyBuilder()
            .addInner(CampaignTargetCTR_.campaign)
            .build();
            
            Campaign campaign = campaignManager.getCampaignById(1L);
            BigDecimal amount = new BigDecimal(10.5);
            targetCTR = biddingManager.newCampaignTargetCTR(campaign, amount, fs2);
            assertNotNull(targetCTR);
            assertEquals(amount.floatValue(), targetCTR.getTargetCTR().floatValue(), 0);
            assertEquals(targetCTR.getCampaign(), campaign);
            campaign = campaignManager.getCampaignById(1L, fs);
            assertEquals(campaign.getTargetCTR(), targetCTR);
            
            amount = amount.add(new BigDecimal(10));
            targetCTR.setTargetCTR(amount);
            targetCTR = biddingManager.update(targetCTR);
            
            campaign = campaignManager.getCampaignById(1L, fs);
            assertEquals(amount.floatValue(), campaign.getTargetCTR().getTargetCTR().floatValue(), 0);
            
            Campaign c2 = campaignManager.copyCampaign(campaign);
            c2 = campaignManager.getCampaignById(c2.getId());
            CampaignTargetCTR newTargetCTR = c2.getTargetCTR();
            assertNotNull(newTargetCTR);
            assertEquals(newTargetCTR.getTargetCTR().floatValue(), campaign.getTargetCTR().getTargetCTR().floatValue(), 0);

            try {
                biddingManager.newCampaignTargetCTR(campaign, amount, fs2);
                fail("Duplicate Target CTR object was allowed");
            } catch(Exception e) {
            }
            
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            biddingManager.delete(targetCTR);
        }
    }
    
    //----------------------------------------------------------------------------------------------------------------
    
    @Test
    public void testCampaignTargetCVR() {
        CampaignTargetCVR targetCVR = null;
        try {
            FetchStrategy fs2 = new FetchStrategyBuilder()
            .addInner(CampaignTargetCVR_.campaign)
            .build();
            
            Campaign campaign = campaignManager.getCampaignById(1L);
            BigDecimal amount = new BigDecimal(10.5);
            targetCVR = biddingManager.newCampaignTargetCVR(campaign, amount, fs2);
            assertNotNull(targetCVR);
            assertEquals(amount.floatValue(), targetCVR.getTargetCVR().floatValue(), 0);
            assertEquals(targetCVR.getCampaign(), campaign);
            campaign = campaignManager.getCampaignById(1L);
            assertEquals(campaign.getTargetCVR(), targetCVR);
            
            amount = amount.add(new BigDecimal(10));
            targetCVR.setTargetCVR(amount);
            targetCVR = biddingManager.update(targetCVR);
            
            campaign = campaignManager.getCampaignById(1L);
            assertEquals(amount.floatValue(), campaign.getTargetCVR().getTargetCVR().floatValue(), 0);
            
            Campaign c2 = campaignManager.copyCampaign(campaign);
            c2 = campaignManager.getCampaignById(c2.getId());
            CampaignTargetCVR newTargetCVR = c2.getTargetCVR();
            assertNotNull(newTargetCVR);
            assertEquals(newTargetCVR.getTargetCVR().floatValue(), campaign.getTargetCVR().getTargetCVR().floatValue(), 0);

            try {
                biddingManager.newCampaignTargetCVR(campaign, amount, fs2);
                fail("Duplicate Target CVR object was allowed");
            } catch(Exception e) {
            }
            
            
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            biddingManager.delete(targetCVR);
        }
    }
}

package com.adfonic.datacollector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.Ignore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.time.DateUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.AdEvent;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.datacollector.campaign.AdvertiserDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignBidDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDataFeeDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherRevShareDto;
import com.adfonic.test.AbstractAdfonicTest;

public class TestAdEventAccounting extends AbstractAdfonicTest {
	
	@Before
	public void setup() {
		Logger.getLogger(AdEventAccounting.class.getName()).setLevel(Level.FINEST);
	}
	
    @Test
    public void testGettersAfterConstructor() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);

        expect(new Expectations() {{
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.CLICK));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(false));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(null));
            allowing (adEvent).getAdSpaceId(); will(returnValue(0L));
            allowing (adEvent).getCreativeId(); will(returnValue(0L));
            allowing (adEvent).getImpressionExternalID(); will(returnValue(null));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(null));
            allowing (publisher).getId(); will(returnValue(0L));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
        assertEquals(adEvent, adEventAccounting.getAdEvent());
        assertEquals(campaign, adEventAccounting.getCampaign());
        assertEquals(advertiser, adEventAccounting.getAdvertiser());
        assertEquals(publisher, adEventAccounting.getPublisher());
        assertNull(adEventAccounting.getCost());
        assertNull(adEventAccounting.getPayout());
        assertNull(adEventAccounting.getAdvertiserVat());
        assertNull(adEventAccounting.getPublisherVat());
    }
    
    @Test
    public void testCurrentBidStartsAfterEventTime() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final CampaignBidDto campaignBid1 = mock(CampaignBidDto.class, "campaignBid1");
        final CampaignBidDto campaignBid2 = mock(CampaignBidDto.class, "campaignBid2");
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);

        expect(new Expectations() {{
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(false));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(null));
            allowing (adEvent).getAdSpaceId(); will(returnValue(0L));
            allowing (adEvent).getCreativeId(); will(returnValue(0L));
            allowing (adEvent).getImpressionExternalID(); will(returnValue(null));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (publisher).getId(); will(returnValue(0L));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
            
            // first bid hasn't started as of the event time
            oneOf (campaign).getCurrentBid(); will(returnValue(campaignBid1));
            oneOf (campaignBid1).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, 1)));

            // it should try to grab the bid applicable to the eventTime
            oneOf (campaign).getBidForDate(eventTime); will(returnValue(campaignBid2));
            
            allowing (campaignBid2).getBidType(); will(returnValue(BidType.CPC));
            
            allowing (campaignBid2).getAmount(); will(returnValue(new BigDecimal(2.3)));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
        assertEquals(adEvent, adEventAccounting.getAdEvent());
        assertEquals(campaign, adEventAccounting.getCampaign());
        assertEquals(advertiser, adEventAccounting.getAdvertiser());
        assertEquals(publisher, adEventAccounting.getPublisher());
        assertNull(adEventAccounting.getCost());
        assertNull(adEventAccounting.getPayout());
        assertNull(adEventAccounting.getAdvertiserVat());
        assertNull(adEventAccounting.getPublisherVat());
    }
    
    @Test
    public void testCurrentRevShareStartsAfterEventTime() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final PublisherRevShareDto publisherRevShare1 = mock(PublisherRevShareDto.class, "publisherRevShare1");
        final BigDecimal publisherRevShare2 = mock(BigDecimal.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);

        expect(new Expectations() {{
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(false));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(null));
            allowing (adEvent).getAdSpaceId(); will(returnValue(0L));
            allowing (adEvent).getCreativeId(); will(returnValue(0L));
            allowing (adEvent).getImpressionExternalID(); will(returnValue(null));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (campaign).getCurrentBid(); will(returnValue(null));
            allowing (publisher).getId(); will(returnValue(0L));

            // current rev share hasn't started as of the event time
            oneOf (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare1));
            oneOf (publisherRevShare1).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, 1)));

            // it should try to grab the rev share applicable to the eventTime
            allowing (publisher).getRevShareForDate(eventTime); will(returnValue(publisherRevShare2));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
        assertEquals(adEvent, adEventAccounting.getAdEvent());
        assertEquals(campaign, adEventAccounting.getCampaign());
        assertEquals(advertiser, adEventAccounting.getAdvertiser());
        assertEquals(publisher, adEventAccounting.getPublisher());
        assertNull(adEventAccounting.getCost());
        assertNull(adEventAccounting.getPayout());
        assertNull(adEventAccounting.getAdvertiserVat());
        assertNull(adEventAccounting.getPublisherVat());
    }
    
    @Test
    public void testAssignCost() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final BigDecimal discount = BigDecimal.valueOf(0.20);

        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.CLICK));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(false));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(null));
            allowing (adEvent).getAdSpaceId(); will(returnValue(0L));
            allowing (adEvent).getCreativeId(); will(returnValue(0L));
            allowing (adEvent).getImpressionExternalID(); will(returnValue(null));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(null));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getId(); will(returnValue(0L));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));

            // Test 1

            // Test 2

            // Test 3
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));

            // Test 4
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
            oneOf (advertiserCompany).getMarginShareDSP(); will(returnValue(BigDecimal.ZERO));
            oneOf (publisherCompany).isTaxablePublisher(); will(returnValue(false));

            // Test 5
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
            oneOf (advertiserCompany).getMarginShareDSP(); will(returnValue(BigDecimal.valueOf(0.2)));
            oneOf (publisherCompany).isTaxablePublisher(); will(returnValue(false));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);

        BigDecimal payout = mock(BigDecimal.class, "payout");
        BigDecimal publisherVat = mock(BigDecimal.class, "publisherVat");
        
        // Test 1
        inject(adEventAccounting, "payout", payout);
        inject(adEventAccounting, "publisherVat", publisherVat);
        adEventAccounting.assignCost(null, false, null);
        assertNull(adEventAccounting.getCost());
        assertNull(adEventAccounting.getAdvertiserVat());
        assertEquals(payout, adEventAccounting.getPayout());
        assertEquals(publisherVat, adEventAccounting.getPublisherVat());
        
        // Test 2
        inject(adEventAccounting, "payout", payout);
        inject(adEventAccounting, "publisherVat", publisherVat);
        adEventAccounting.assignCost(null, true, null);
        assertNull(adEventAccounting.getCost());
        assertNull(adEventAccounting.getAdvertiserVat());
        assertNull(adEventAccounting.getPayout());
        assertNull(adEventAccounting.getPublisherVat());

        BigDecimal cost = BigDecimal.valueOf(0.50);
        
        // Test 3
        inject(adEventAccounting, "payout", payout);
        inject(adEventAccounting, "publisherVat", publisherVat);
        adEventAccounting.assignCost(cost, false, null);
        assertEquals(cost, adEventAccounting.getCost());
        assertNull(adEventAccounting.getAdvertiserVat());
        assertEquals(payout, adEventAccounting.getPayout());
        assertEquals(publisherVat, adEventAccounting.getPublisherVat());
        
        // Test 4 (no discount)
        inject(adEventAccounting, "payout", null);
        inject(adEventAccounting, "publisherVat", publisherVat);
        adEventAccounting.assignCost(cost, true, BigDecimal.ZERO);
        assertEquals(cost, adEventAccounting.getCost());
        assertNull(adEventAccounting.getAdvertiserVat());
        assertEquals(0.0, adEventAccounting.getPayout().doubleValue(), 0.0);
        assertNull(adEventAccounting.getPublisherVat());
        
        // Test 5 (with 20% discount)
        inject(adEventAccounting, "payout", null);
        inject(adEventAccounting, "publisherVat", publisherVat);
        adEventAccounting.assignCost(cost, true, discount);
        assertEquals(cost, adEventAccounting.getCost());
        assertNull(adEventAccounting.getAdvertiserVat());
        assertEquals(0.0923, adEventAccounting.getPayout().doubleValue(), 0.0);
        assertNull(adEventAccounting.getPublisherVat());
        assertEquals(new BigDecimal("0.0923"), adEventAccounting.getPayout());
    }
    
    @Test
    public void testGetAdvertiserSpend() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");

        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.CLICK));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(false));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(null));
            allowing (adEvent).getAdSpaceId(); will(returnValue(0L));
            allowing (adEvent).getCreativeId(); will(returnValue(0L));
            allowing (adEvent).getImpressionExternalID(); will(returnValue(null));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(null));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getId(); will(returnValue(0L));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));

            // Test 1
            
            // Test 2
            oneOf (advertiserCompany).isPostPay(); will(returnValue(true));
            
            // Test 3
            oneOf (advertiserCompany).isPostPay(); will(returnValue(false));
            
            // Test 4
            oneOf (advertiserCompany).isPostPay(); will(returnValue(false));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);

        // Test 1
        inject(adEventAccounting, "cost", null);
        assertNull(adEventAccounting.getAdvertiserSpend());

        BigDecimal cost = BigDecimal.valueOf(0.123);
        
        // Test 2
        inject(adEventAccounting, "cost", cost);
        assertEquals(cost, adEventAccounting.getAdvertiserSpend());
        
        BigDecimal advertiserVat = BigDecimal.valueOf(0.0246);
        
        // Test 3
        inject(adEventAccounting, "cost", cost);
        inject(adEventAccounting, "advertiserVat", null);
        assertEquals(cost, adEventAccounting.getAdvertiserSpend());
        
        // Test 4
        inject(adEventAccounting, "cost", cost);
        inject(adEventAccounting, "advertiserVat", advertiserVat);
        assertEquals(cost.add(advertiserVat), adEventAccounting.getAdvertiserSpend());
        assertNull(adEventAccounting.getPayout());
    }

    @Test
    public void testRTBCalculationCPM() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final CompanyDto company = mock(CompanyDto.class);
        final CampaignBidDto bid = mock(CampaignBidDto.class);
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");

        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(new BigDecimal("0.01"));
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(true));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(new BigDecimal("0.80")));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (campaign).getBidForDate(eventTime); will(returnValue(bid));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(bid));
            allowing (campaign).getRmAdServingFee(); will(returnValue(new BigDecimal("0.02")));
            allowing (campaign).getAgencyDiscount(); will(returnValue(new BigDecimal("0.04")));
	    allowing (campaign).isPMP(); will(returnValue(false));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getCompany(); will(returnValue(company));
            allowing (publisher).getBuyerPremium(); will(returnValue(new BigDecimal("0.08")));
            allowing (company).isTaxablePublisher(); will(returnValue(true));
            allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(new BigDecimal("0.20")));
            allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));
            allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            allowing (bid).getAmount(); will(returnValue(new  BigDecimal("0.60")));
            allowing (bid).getBidType(); will(returnValue(BidType.CPM));
            allowing (bid).getStartDate(); will(returnValue(eventTime));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);

        // Test 1
        assertEquals(new BigDecimal("0.000600"), adEventAccounting.getCost());
        assertEquals(new BigDecimal("0.0008"), adEventAccounting.getPayout());
    }

    @Test
    @Ignore
    public void testRTBCalculationCPMNoSettlement() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final CompanyDto company = mock(CompanyDto.class);
        final CampaignBidDto bid = mock(CampaignBidDto.class);
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");

        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(new BigDecimal("0.13"));
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(true));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(null));
            allowing (adEvent).getAdSpaceId(); will(returnValue(0L));
            allowing (adEvent).getCreativeId(); will(returnValue(0L));
            allowing (adEvent).getImpressionExternalID(); will(returnValue(null));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (campaign).getBidForDate(eventTime); will(returnValue(bid));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(bid));
            allowing (campaign).getRmAdServingFee(); will(returnValue(new BigDecimal("0.10")));
            allowing (campaign).getAgencyDiscount(); will(returnValue(new BigDecimal("0.04")));
            allowing (publisher).getId(); will(returnValue(0L));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getCompany(); will(returnValue(company));
            allowing (publisher).getBuyerPremium(); will(returnValue(new BigDecimal("0.08")));
            allowing (company).isTaxablePublisher(); will(returnValue(true));
            allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(new BigDecimal("0.40")));
            allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));
            allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            allowing (bid).getAmount(); will(returnValue(new  BigDecimal("0.90")));
            allowing (bid).getBidType(); will(returnValue(BidType.CPM));
            allowing (bid).getStartDate(); will(returnValue(eventTime));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);

        assertEquals(new BigDecimal("0.0009"), adEventAccounting.getCost());
        assertNull(adEventAccounting.getPayout());
    }

	@Test
	public void testRTBCalculationCPMwoMediaCostMargin() {
	    final AdEvent adEvent = mock(AdEvent.class);
	    final CampaignDto campaign = mock(CampaignDto.class);
	    final AdvertiserDto advertiser = mock(AdvertiserDto.class);
	    final PublisherDto publisher = mock(PublisherDto.class);
	    final CompanyDto company = mock(CompanyDto.class);
	    final CampaignBidDto bid = mock(CampaignBidDto.class);
	    final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
	    final Date eventTime = DateUtils.addSeconds(new Date(), -3);
	    final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
	
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(new BigDecimal("0.13"));
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
	    expect(new Expectations() {{
	        allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
	        allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
	        allowing (adEvent).getEventTime(); will(returnValue(eventTime));
	        allowing (adEvent).isRtb(); will(returnValue(true));
	        allowing (adEvent).getRtbSettlementPrice(); will(returnValue(new BigDecimal("0.80")));
		allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
	        allowing (campaign).getId(); will(returnValue(randomLong()));
	        allowing (campaign).getCurrentDataFee(); will(returnValue(null));
	        allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
	        allowing (campaign).getBidForDate(eventTime); will(returnValue(bid));
	        allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
	        allowing (campaign).getCurrentBid(); will(returnValue(bid));
	        allowing (campaign).getRmAdServingFee(); will(returnValue(new BigDecimal("0.10")));
	        allowing (campaign).getAgencyDiscount(); will(returnValue(new BigDecimal("0.04")));
	        allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
	        allowing (publisher).getCompany(); will(returnValue(company));
	        allowing (publisher).getBuyerPremium(); will(returnValue(new BigDecimal("0.08")));
	        allowing (company).isTaxablePublisher(); will(returnValue(true));
	        allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(BigDecimal.ZERO));
	        allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));
	        allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
	        allowing (bid).getAmount(); will(returnValue(new  BigDecimal("0.60")));
	        allowing (bid).getBidType(); will(returnValue(BidType.CPM));
	        allowing (bid).getStartDate(); will(returnValue(eventTime));
	        allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
	        allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
	    }});
	
	    AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
	
	    assertEquals(new BigDecimal("0.0006"), adEventAccounting.getCost());
	    assertEquals(new BigDecimal("0.0008"), adEventAccounting.getPayout());
	}

	@Test
	public void testRTBCalculationCPMwoDirectCost() {
	    final AdEvent adEvent = mock(AdEvent.class);
	    final CampaignDto campaign = mock(CampaignDto.class);
	    final AdvertiserDto advertiser = mock(AdvertiserDto.class);
	    final PublisherDto publisher = mock(PublisherDto.class);
	    final CompanyDto company = mock(CompanyDto.class);
	    final CampaignBidDto bid = mock(CampaignBidDto.class);
	    final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
	    final Date eventTime = DateUtils.addSeconds(new Date(), -3);
	    final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
	
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(BigDecimal.ZERO);
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
	    Logger.getLogger(AdEventAccounting.class.getName()).setLevel(Level.INFO);
	    expect(new Expectations() {{
	        allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
	        allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
	        allowing (adEvent).getEventTime(); will(returnValue(eventTime));
	        allowing (adEvent).isRtb(); will(returnValue(true));
	        allowing (adEvent).getRtbSettlementPrice(); will(returnValue(new BigDecimal("0.80")));
		allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
	        allowing (campaign).getId(); will(returnValue(randomLong()));
	        allowing (campaign).getCurrentDataFee(); will(returnValue(null));
	        allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
	        allowing (campaign).getBidForDate(eventTime); will(returnValue(bid));
	        allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
	        allowing (campaign).getCurrentBid(); will(returnValue(bid));
	        allowing (campaign).getRmAdServingFee(); will(returnValue(new BigDecimal("0.10")));
	        allowing (campaign).getAgencyDiscount(); will(returnValue(new BigDecimal("0.04")));
		allowing (campaign).isPMP(); will(returnValue(false));
	        allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
	        allowing (publisher).getCompany(); will(returnValue(company));
	        allowing (publisher).getBuyerPremium(); will(returnValue(new BigDecimal("0.08")));
	        allowing (company).isTaxablePublisher(); will(returnValue(true));
	        allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(new BigDecimal("0.40")));
	        allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));
	        allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
	        allowing (bid).getAmount(); will(returnValue(new  BigDecimal("0.60")));
	        allowing (bid).getBidType(); will(returnValue(BidType.CPM));
	        allowing (bid).getStartDate(); will(returnValue(eventTime));
	        allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
	        allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
	    }});
	
	    AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
	
	    assertEquals(new BigDecimal("0.000600"), adEventAccounting.getCost());
	    assertEquals(new BigDecimal("0.0008"), adEventAccounting.getPayout());
	}

	@Test
	public void testRTBCalculationCPMwoAgencyDiscount() {
	    final AdEvent adEvent = mock(AdEvent.class);
	    final CampaignDto campaign = mock(CampaignDto.class);
	    final AdvertiserDto advertiser = mock(AdvertiserDto.class);
	    final PublisherDto publisher = mock(PublisherDto.class);
	    final CompanyDto company = mock(CompanyDto.class);
	    final CampaignBidDto bid = mock(CampaignBidDto.class);
	    final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
	    final Date eventTime = DateUtils.addSeconds(new Date(), -3);
	    final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
	
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(BigDecimal.ZERO);
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
	    Logger.getLogger(AdEventAccounting.class.getName()).setLevel(Level.INFO);
	    expect(new Expectations() {{
	        allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
	        allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
	        allowing (adEvent).getEventTime(); will(returnValue(eventTime));
	        allowing (adEvent).isRtb(); will(returnValue(true));
	        allowing (adEvent).getRtbSettlementPrice(); will(returnValue(new BigDecimal("0.80")));
		allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
	        allowing (campaign).getId(); will(returnValue(randomLong()));
	        allowing (campaign).getCurrentDataFee(); will(returnValue(null));
	        allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
	        allowing (campaign).getBidForDate(eventTime); will(returnValue(bid));
	        allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
	        allowing (campaign).getCurrentBid(); will(returnValue(bid));
	        allowing (campaign).getRmAdServingFee(); will(returnValue(new BigDecimal("0.10")));
	        allowing (campaign).getAgencyDiscount(); will(returnValue(BigDecimal.ZERO));
		allowing (campaign).isPMP(); will(returnValue(false));
	        allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
	        allowing (publisher).getCompany(); will(returnValue(company));
	        allowing (publisher).getBuyerPremium(); will(returnValue(new BigDecimal("0.08")));
	        allowing (company).isTaxablePublisher(); will(returnValue(true));
	        allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(new BigDecimal("0.40")));
	        allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));
	        allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
	        allowing (bid).getAmount(); will(returnValue(new  BigDecimal("0.60")));
	        allowing (bid).getBidType(); will(returnValue(BidType.CPM));
	        allowing (bid).getStartDate(); will(returnValue(eventTime));
	        allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
	        allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
	    }});
	
	    AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
	
	    assertEquals(new BigDecimal("0.000600"), adEventAccounting.getCost());
	    assertEquals(new BigDecimal("0.0008"), adEventAccounting.getPayout());
	}

	@Test
	public void testRTBCalculationPMPnoBuyerPremiumOrDirectCost() {
	    final AdEvent adEvent = mock(AdEvent.class);
	    final CampaignDto campaign = mock(CampaignDto.class);
	    final AdvertiserDto advertiser = mock(AdvertiserDto.class);
	    final PublisherDto publisher = mock(PublisherDto.class);
	    final CompanyDto company = mock(CompanyDto.class);
	    final CampaignBidDto bid = mock(CampaignBidDto.class);
	    final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
	    final Date eventTime = DateUtils.addSeconds(new Date(), -3);
	    final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
	
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(new BigDecimal("0.09"));
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
	    Logger.getLogger(AdEventAccounting.class.getName()).setLevel(Level.INFO);
	    expect(new Expectations() {{
	        allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
	        allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
	        allowing (adEvent).getEventTime(); will(returnValue(eventTime));
	        allowing (adEvent).isRtb(); will(returnValue(true));
	        allowing (adEvent).getRtbSettlementPrice(); will(returnValue(new BigDecimal("1.00")));
		allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
	        allowing (campaign).getId(); will(returnValue(randomLong()));
	        allowing (campaign).getCurrentDataFee(); will(returnValue(null));
	        allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
	        allowing (campaign).getBidForDate(eventTime); will(returnValue(bid));
	        allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
	        allowing (campaign).getCurrentBid(); will(returnValue(bid));
	        allowing (campaign).getRmAdServingFee(); will(returnValue(new BigDecimal("0.50")));
	        allowing (campaign).getAgencyDiscount(); will(returnValue(BigDecimal.ZERO));
		allowing (campaign).isPMP(); will(returnValue(true));
	        allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
	        allowing (publisher).getCompany(); will(returnValue(company));
	        allowing (publisher).getBuyerPremium(); will(returnValue(new BigDecimal("0.08")));
	        allowing (company).isTaxablePublisher(); will(returnValue(true));
	        allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(new BigDecimal("0.20")));
	        allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));
	        allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
	        allowing (bid).getAmount(); will(returnValue(new  BigDecimal("10.00")));
	        allowing (bid).getBidType(); will(returnValue(BidType.CPM));
	        allowing (bid).getStartDate(); will(returnValue(eventTime));
	        allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
	        allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
	    }});
	
	    AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
	
	    assertEquals(new BigDecimal("0.001750"), adEventAccounting.getCost());
	    assertEquals(new BigDecimal("0.001"), adEventAccounting.getPayout());
	}

	@Test
    public void testRTBCalculationCPC() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final CompanyDto company = mock(CompanyDto.class);
        final CampaignBidDto bid = mock(CampaignBidDto.class);
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");

        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(new BigDecimal("0.13"));
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(true));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(new BigDecimal("0.80")));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (campaign).getBidForDate(eventTime); will(returnValue(bid));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(bid));
            allowing (campaign).getRmAdServingFee(); will(returnValue(new BigDecimal("0.10")));
            allowing (campaign).getAgencyDiscount(); will(returnValue(new BigDecimal("0.04")));
	    allowing (campaign).isPMP(); will(returnValue(false));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getCompany(); will(returnValue(company));
            allowing (publisher).getBuyerPremium(); will(returnValue(new BigDecimal("0.08")));
            allowing (company).isTaxablePublisher(); will(returnValue(true));
            allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(new BigDecimal("0.40")));
            allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));
            allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            allowing (bid).getAmount(); will(returnValue(new  BigDecimal("0.60")));
            allowing (bid).getBidType(); will(returnValue(BidType.CPC));
            allowing (bid).getStartDate(); will(returnValue(eventTime));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
        
        assertEquals(new BigDecimal("0.001799"), adEventAccounting.getCost());
        assertEquals(new BigDecimal("0.0008"), adEventAccounting.getPayout());
    }
    
    @Test
    public void testRTBCalculationCPMwDataFee() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final CompanyDto company = mock(CompanyDto.class);
        final CampaignBidDto bid = mock(CampaignBidDto.class);
        final CampaignDataFeeDto dataFee = mock(CampaignDataFeeDto.class);
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");

        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(new BigDecimal("0.13"));
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(true));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(new BigDecimal("0.80")));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(dataFee));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(dataFee));
            allowing (campaign).getBidForDate(eventTime); will(returnValue(bid));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(bid));
            allowing (campaign).getRmAdServingFee(); will(returnValue(new BigDecimal("0.10")));
            allowing (campaign).getAgencyDiscount(); will(returnValue(new BigDecimal("0.04")));
	    allowing (campaign).isPMP(); will(returnValue(false));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getCompany(); will(returnValue(company));
            allowing (publisher).getBuyerPremium(); will(returnValue(new BigDecimal("0.08")));
            allowing (company).isTaxablePublisher(); will(returnValue(true));
            allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(new BigDecimal("0.40")));
            allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));
            allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            allowing (bid).getAmount(); will(returnValue(new  BigDecimal("0.60")));
            allowing (bid).getBidType(); will(returnValue(BidType.CPC));
            allowing (bid).getStartDate(); will(returnValue(eventTime));
            allowing (dataFee).getId(); will(returnValue(123L));
            allowing (dataFee).getAmount(); will(returnValue(new BigDecimal("0.02")));
            allowing (dataFee).getStartDate(); will(returnValue(eventTime));
            allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(152L));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);

        assertEquals(new BigDecimal("0.001820"), adEventAccounting.getCost());
        assertEquals(new BigDecimal("0.0008"), adEventAccounting.getPayout());
    }

    @Test
    public void testRTBCalculationCPMwDataFeeLocal() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final CompanyDto company = mock(CompanyDto.class);
        final CampaignBidDto bid = mock(CampaignBidDto.class);
        final CampaignDataFeeDto dataFee = mock(CampaignDataFeeDto.class);
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");

        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(new BigDecimal("0.13"));
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(true));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(new BigDecimal("0.80")));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(dataFee));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(dataFee));
            allowing (campaign).getBidForDate(eventTime); will(returnValue(bid));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(bid));
            allowing (campaign).getRmAdServingFee(); will(returnValue(new BigDecimal("0.10")));
            allowing (campaign).getAgencyDiscount(); will(returnValue(new BigDecimal("0.04")));
	    allowing (campaign).isPMP(); will(returnValue(false));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getCompany(); will(returnValue(company));
            allowing (publisher).getBuyerPremium(); will(returnValue(new BigDecimal("0.08")));
            allowing (company).isTaxablePublisher(); will(returnValue(true));
            allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(new BigDecimal("0.40")));
            allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));
            allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            allowing (bid).getAmount(); will(returnValue(new  BigDecimal("0.60")));
            allowing (bid).getBidType(); will(returnValue(BidType.CPC));
            allowing (bid).getStartDate(); will(returnValue(eventTime));
            allowing (dataFee).getId(); will(returnValue(123L));
            allowing (dataFee).getAmount(); will(returnValue(new BigDecimal("0.02")));
            allowing (dataFee).getStartDate(); will(returnValue(eventTime));
            allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(152L));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
        adEventAccounting.setDataFeeId(152L);

        assertEquals(new BigDecimal("0.001820"), adEventAccounting.getCost());
        assertEquals(new BigDecimal("0.0008"), adEventAccounting.getPayout());
    }

    @Test
    public void testSetAdjustedAdvertiserSpend() {
        final AdEvent adEvent = mock(AdEvent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");

        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.CLICK));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(null));
            allowing (adEvent).getAdSpaceId(); will(returnValue(0L));
            allowing (adEvent).getCreativeId(); will(returnValue(0L));
            allowing (adEvent).getImpressionExternalID(); will(returnValue(null));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(null));
            allowing (publisherCompany).isTaxablePublisher(); will(returnValue(false));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getId(); will(returnValue(0L));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(BigDecimal.valueOf(0.60)));
            //allowing (advertiserCompany).getDiscount(); will(returnValue(null));
            oneOf (adEvent).isRtb(); will(returnValue(false));

            // Test 1: non-RTB
            oneOf (adEvent).isRtb(); will(returnValue(false));

            // Test 2: non-RTB
            oneOf (adEvent).isRtb(); will(returnValue(false));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            oneOf (advertiserCompany).isPostPay(); will(returnValue(true));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));

            // Test 3: non-RTB
            oneOf (adEvent).isRtb(); will(returnValue(false));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            oneOf (advertiserCompany).isPostPay(); will(returnValue(false));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            
            // Test 4: non-RTB
            oneOf (adEvent).isRtb(); will(returnValue(false));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
            
            // Test 5: RTB
            oneOf (adEvent).isRtb(); will(returnValue(true));
            
            // Test 6: RTB
            oneOf (adEvent).isRtb(); will(returnValue(true));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            oneOf (advertiserCompany).isPostPay(); will(returnValue(true));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));

            // Test 7: RTB
            oneOf (adEvent).isRtb(); will(returnValue(true));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            oneOf (advertiserCompany).isPostPay(); will(returnValue(false));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            
            // Test 8: RTB
            oneOf (adEvent).isRtb(); will(returnValue(true));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);

        BigDecimal advertiserSpend = BigDecimal.valueOf(0.05);
        
        // Test 1
        adEventAccounting.setAdjustedAdvertiserSpend(null, null);
        assertNull(adEventAccounting.getCost());

        // Test 2
        adEventAccounting.setAdjustedAdvertiserSpend(advertiserSpend, null);
        assertEquals(advertiserSpend, adEventAccounting.getCost());

        // Test 3
        adEventAccounting.setAdjustedAdvertiserSpend(advertiserSpend, null);
        assertEquals(advertiserSpend.divide(BigDecimal.valueOf(1.2), RoundingMode.HALF_UP).doubleValue(), adEventAccounting.getCost().doubleValue(), 0.0);
        
        // Test 4
        adEventAccounting.setAdjustedAdvertiserSpend(advertiserSpend, null);
        assertEquals(advertiserSpend, adEventAccounting.getCost());

        // Test 5
        adEventAccounting.setAdjustedAdvertiserSpend(null, null);
        assertNull(adEventAccounting.getCost());

        // Test 6
        adEventAccounting.setAdjustedAdvertiserSpend(advertiserSpend, null);
        assertEquals(advertiserSpend, adEventAccounting.getCost());

        // Test 7
        adEventAccounting.setAdjustedAdvertiserSpend(advertiserSpend, null);
        assertEquals(advertiserSpend.divide(BigDecimal.valueOf(1.2), RoundingMode.HALF_UP).doubleValue(), adEventAccounting.getCost().doubleValue(), 0.0);
        
        // Test 8
        adEventAccounting.setAdjustedAdvertiserSpend(advertiserSpend, null);
        assertEquals(advertiserSpend, adEventAccounting.getCost());
        assertEquals(new BigDecimal("0.030"), adEventAccounting.getPayout());
    }
    
    @Test
    public void testGetPublisherCreditMultiplier() {
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final CampaignDto campaign = mock(CampaignDto.class);
        final CampaignBidDto campaignBid1 = mock(CampaignBidDto.class, "campaignBid1");
        final CampaignBidDto campaignBid2 = mock(CampaignBidDto.class, "campaignBid2");
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
        final PublisherDto publisher = mock(PublisherDto.class);
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final BigDecimal revShare = BigDecimal.valueOf(0.60);

        expect(new Expectations() {{
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.CLICK));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).isRtb(); will(returnValue(false));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(null));
            allowing (adEvent).getAdSpaceId(); will(returnValue(0L));
            allowing (adEvent).getCreativeId(); will(returnValue(0L));
            allowing (adEvent).getImpressionExternalID(); will(returnValue(null));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (campaign).getCurrentBid(); will(returnValue(null));
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
            //allowing (advertiserCompany).getDiscount(); will(returnValue(null));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getId(); will(returnValue(0L));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -30)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(revShare));
            allowing (publisherCompany).isTaxablePublisher(); will(returnValue(false));
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);
        // 1st call does the calculations
        assertEquals(revShare.doubleValue(), adEventAccounting.getPublisherCreditMultiplier(null).doubleValue(), 0.0);
        // 2nd call doesn't do anything, value is cached
        assertEquals(revShare.doubleValue(), adEventAccounting.getPublisherCreditMultiplier(null).doubleValue(), 0.0);
    }
    
    @Test
    public void testCalculatePublisherCreditMultiplier() {
        final Date eventTime = new Date();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
        final PublisherDto publisher = mock(PublisherDto.class);
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final BigDecimal publisherRevShare = BigDecimal.valueOf(0.60);
        final BigDecimal discount = BigDecimal.valueOf(0.20);

        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisherCompany).isTaxablePublisher(); will(returnValue(false));
            allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
        }});

        // Test 1: no discount
        assertEquals(publisherRevShare.doubleValue(), AdEventAccounting.calculatePublisherCreditMultiplier(advertiser, publisher, publisherRevShare, null, eventTime).doubleValue(), 0.0);

        // Test 2: 20% discount
        assertEquals(publisherRevShare.multiply(BigDecimal.ONE.subtract(discount)).doubleValue(), AdEventAccounting.calculatePublisherCreditMultiplier(advertiser, publisher, publisherRevShare, discount, eventTime).doubleValue(), 0.0);
    }

    @Test
    public void testGetPublisherTaxRate() {
        final Date eventTime = new Date();
        final PublisherDto publisher = mock(PublisherDto.class);
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");

        expect(new Expectations() {{
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));

            // Test 1
            oneOf (publisherCompany).isTaxablePublisher(); will(returnValue(true));

            // Test 2
            oneOf (publisherCompany).isTaxablePublisher(); will(returnValue(false));
        }});

        // Test 1
        assertEquals(0.20, AdEventAccounting.getPublisherTaxRate(publisher, eventTime).doubleValue(), 0.0);

        // Test 2
        assertEquals(0.0, AdEventAccounting.getPublisherTaxRate(publisher, eventTime).doubleValue(), 0.0);
    }

    @Test
    public void testGetEffectiveTaxRateOnSpend() {
        final Date eventTime = new Date();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");

        expect(new Expectations() {{
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            
            // Test 1
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
            
            // Test 2
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
            
            // Test 3
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            oneOf (advertiserCompany).isPostPay(); will(returnValue(false));
            
            // Test 4
            oneOf (advertiserCompany).isTaxableAdvertiser(); will(returnValue(true));
            oneOf (advertiserCompany).isPostPay(); will(returnValue(true));
        }});

        // Test 1
        assertEquals(0.0, AdEventAccounting.getEffectiveTaxRateOnSpend(advertiser, eventTime).doubleValue(), 0.0);

        // Test 2
        assertEquals(0.0, AdEventAccounting.getEffectiveTaxRateOnSpend(advertiser, eventTime).doubleValue(), 0.0);

        // Test 3
        assertEquals(0.20, AdEventAccounting.getEffectiveTaxRateOnSpend(advertiser, eventTime).doubleValue(), 0.0);

        // Test 4
        assertEquals(0.0, AdEventAccounting.getEffectiveTaxRateOnSpend(advertiser, eventTime).doubleValue(), 0.0);
    }
    
    private static class TestCase {
        private final AdAction adAction;
        private final BidType bidType;
        private final BigDecimal bidAmount;
        private final boolean taxableAdvertiser;
        private final BigDecimal campaignAgencyDiscount;
        private final BigDecimal revShare;
        private final BigDecimal rtbSettlementPrice;
        private final boolean taxablePublisher;
        private final Double expectedCost;
        private final Double expectedAdvertiserVat;
        private final Double expectedPayout;
        private final Double expectedPublisherVat;
        private final BigDecimal mediaCostMargin;
        private final BigDecimal rmAdServingFee;
        private final BigDecimal marginShareDSP;

        TestCase(AdAction adAction,
                 BidType bidType,
                 Double bidAmount,
                 boolean taxableAdvertiser,
                 Double campaignAgencyDiscount,
                 boolean taxablePublisher,
                 double revShare,
                 Double rtbSettlementPrice,
                 Double expectedCost,
                 Double expectedAdvertiserVat,
                 Double expectedPayout,
                 Double expectedPublisherVat,
                 Double mediaCostMargin,
                 Double rmAdServingFee,
                 Double marginShareDSP)
        {
            this.adAction = adAction;
            this.bidType = bidType;
            this.bidAmount = bidAmount == null ? null : BigDecimal.valueOf(bidAmount);
            this.taxableAdvertiser = taxableAdvertiser;
            this.campaignAgencyDiscount = campaignAgencyDiscount == null ? null : BigDecimal.valueOf(campaignAgencyDiscount);
            this.taxablePublisher = taxablePublisher;
            this.revShare = BigDecimal.valueOf(revShare);
            this.rtbSettlementPrice = rtbSettlementPrice == null ? null : BigDecimal.valueOf(rtbSettlementPrice);
            this.expectedCost = expectedCost;
            this.expectedAdvertiserVat = expectedAdvertiserVat;
            this.expectedPayout = expectedPayout;
            this.expectedPublisherVat = expectedPublisherVat;
            this.mediaCostMargin = mediaCostMargin == null ? null : BigDecimal.valueOf(mediaCostMargin);
            this.rmAdServingFee = rmAdServingFee == null ? null : BigDecimal.valueOf(rmAdServingFee);
            this.marginShareDSP = marginShareDSP == null ? null : BigDecimal.valueOf(marginShareDSP);
        }
    }

    @Test
    public void testAccountingVariations() {
        for (int k = 0; k < TEST_CASES.length; ++k) {
            testAccountingVariation(k);
        }
    }
    
    private void testAccountingVariation(int testCaseIndex) {
        final TestCase testCase = TEST_CASES[testCaseIndex];
        final Mockery mockery = new JUnit4Mockery() {{
            // Allows mocking of classes and not just interfaces
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final AdEvent adEvent = mockery.mock(AdEvent.class);
        final Date eventTime = DateUtils.addSeconds(new Date(), -3);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mockery.mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
        final AdvertiserDto advertiser = mockery.mock(AdvertiserDto.class);
        final CampaignDto campaign = mockery.mock(CampaignDto.class);
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mockery.mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final PublisherDto publisher = mockery.mock(PublisherDto.class);
        final PublisherRevShareDto publisherRevShare = mockery.mock(PublisherRevShareDto.class);
        final Date revShareStartDate = DateUtils.addDays(eventTime, -7);

        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto cdc = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto();
        cdc.setDirectCost(new BigDecimal("0.13"));
        cdc.setStartDate(DateUtils.addDays(eventTime, -30));
        
        mockery.checking(new Expectations() {{
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).getAdSpaceId(); will(returnValue(0L));
            allowing (adEvent).getCreativeId(); will(returnValue(0L));
            allowing (adEvent).getImpressionExternalID(); will(returnValue(null));
	    allowing (adEvent).getCampaignHistoryDataFeeId(); will(returnValue(null));
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
	    allowing (campaign).isPMP(); will(returnValue(false));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisher).getId(); will(returnValue(0L));
            allowing (publisherRevShare).getStartDate(); will(returnValue(revShareStartDate));
            allowing (campaign).getCurrentDataFee(); will(returnValue(null));
            allowing (campaign).getDataFeeForDate(eventTime); will(returnValue(null));
            allowing (advertiserCompany).getDirectCost(); will(returnValue(cdc));

            // TestCase specific values
            allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(testCase.taxableAdvertiser));
            allowing (advertiserCompany).getMarginShareDSP(); will(returnValue(testCase.marginShareDSP));
            allowing(campaign).getAgencyDiscount(); will(returnValue(testCase.campaignAgencyDiscount));
            allowing (publisherCompany).isTaxablePublisher(); will(returnValue(testCase.taxablePublisher));
            allowing (publisherRevShare).getRevShare(); will(returnValue(testCase.revShare));
            allowing (adEvent).getAdAction(); will(returnValue(testCase.adAction));
            allowing (adEvent).getRtbSettlementPrice(); will(returnValue(testCase.rtbSettlementPrice));
            allowing (adEvent).isRtb(); will(returnValue(testCase.rtbSettlementPrice != null));
            if (testCase.bidType != null) {
                CampaignBidDto campaignBid = mockery.mock(CampaignBidDto.class);
                Date bidStartDate = DateUtils.addDays(eventTime, -7);
                allowing (campaignBid).getBidType(); will(returnValue(testCase.bidType));
                //allowing (campaignBid).getBidModelType(); will(returnValue(testCase.bidModelType));
                allowing (campaignBid).getStartDate(); will(returnValue(bidStartDate));
                allowing (campaignBid).getAmount(); will(returnValue(testCase.bidAmount));
                if(testCase.mediaCostMargin.doubleValue() > 0) {
                //if(testCase.bidModelType.equals(CampaignBid.BidModelType.DSP_LIC)) {
                	// both are ZERO as not being used at the moment 
                	// but could do with a few test cases with values, to check the formula
                	allowing (publisher).getBuyerPremium(); will(returnValue(BigDecimal.ZERO));
                	allowing (campaign).getCurrentDataFee(); will(returnValue(null));
                	
                	allowing (campaign).getRmAdServingFee(); will(returnValue(testCase.rmAdServingFee));
                	allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(testCase.mediaCostMargin));
                }
                allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(testCase.mediaCostMargin));
                allowing (campaignBid).isMaximum(); will(returnValue(false));
                allowing (campaign).getCurrentBid(); will(returnValue(campaignBid));
            } else {
                allowing (campaign).getCurrentBid(); will(returnValue(null));
            }
        }});

        AdEventAccounting adEventAccounting = new AdEventAccounting(adEvent, campaign, publisher);

        String desc = "TestCase " + testCaseIndex;
        
        if (testCase.expectedCost == null) {
            assertNull(desc, adEventAccounting.getCost());
        } else {
            assertNotNull(desc, adEventAccounting.getCost());
            assertEquals(desc, testCase.expectedCost, adEventAccounting.getCost().doubleValue(), 0.0);
        }
        
        if (testCase.expectedAdvertiserVat == null) {
            assertNull(desc, adEventAccounting.getAdvertiserVat());
        } else {
            assertNotNull(desc, adEventAccounting.getAdvertiserVat());
            assertEquals(desc, testCase.expectedAdvertiserVat, adEventAccounting.getAdvertiserVat().doubleValue(), 0.0);
        }
        
        if (testCase.expectedPayout == null) {
            assertNull(desc, adEventAccounting.getPayout());
        } else {
            assertNotNull(desc, adEventAccounting.getPayout());
            assertEquals(desc, testCase.expectedPayout, adEventAccounting.getPayout().doubleValue(), 0.0);
        }
        
        if (testCase.expectedPublisherVat == null) {
            assertNull(desc, adEventAccounting.getPublisherVat());
        } else {
            assertNotNull(desc, adEventAccounting.getPublisherVat());
            assertEquals(desc, testCase.expectedPublisherVat, adEventAccounting.getPublisherVat().doubleValue(), 0.0);
        }

        mockery.assertIsSatisfied();
    }

    private static final TestCase[] TEST_CASES = new TestCase[] {
        // =========================================================================
        // CPC, non-RTB, no discount, no tax on either end
        // =========================================================================
        new TestCase(AdAction.AD_SERVED, BidType.CPC, 0.05,      //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                                //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                                //boolean taxablePublisher, double revShare,                                                              
                     null,                                       //Double rtbSettlementPrice,                                                                              
                null, null, null, null,                          //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.0, null, null),                                //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.CLICK, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     null,
                0.05, null, 0.03, null,
                0.0, null, null),
        new TestCase(AdAction.INSTALL, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.CONVERSION, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        // =========================================================================
        // CPM, non-RTB, no discount, no tax on either end
        // =========================================================================
        new TestCase(AdAction.AD_SERVED, BidType.CPM, 0.10,      //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                                //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                                //boolean taxablePublisher, double revShare,                                                              
                     null,                                       //Double rtbSettlementPrice,                                                                              
                0.0001, null, 0.00006, null,                     //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.0, null, null),                                //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPM, 0.10,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.CLICK, BidType.CPM, 0.10,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.INSTALL, BidType.CPM, 0.10,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.CONVERSION, BidType.CPM, 0.10,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),

        // =========================================================================
        // CPI, non-RTB, no discount, no tax on either end
        // =========================================================================
        new TestCase(AdAction.AD_SERVED, BidType.CPI, 0.43,        //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                                  //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                                  //boolean taxablePublisher, double revShare,                                                              
                     null,                                         //Double rtbSettlementPrice,                                                                              
                null, null, null, null,                            //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.0, null, null),                                  //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.CLICK, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.INSTALL, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     null,
                0.43, null, 0.258, null,
                0.0, null, null),
        new TestCase(AdAction.CONVERSION, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),

        // =========================================================================
        // CPA, non-RTB, no discount, no tax on either end
        // =========================================================================
        new TestCase(AdAction.AD_SERVED, BidType.CPA, 0.43,       //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                                 //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                                 //boolean taxablePublisher, double revShare,                                                              
                     null,                                        //Double rtbSettlementPrice,                                                                              
                null, null, null, null,                           //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.0, null, null),                                 //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.CLICK, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.INSTALL, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     null,
                null, null, null, null,
                0.0, null, null),
        new TestCase(AdAction.CONVERSION, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     null,
                0.43, null, 0.258, null,
                0.0, null, null),
        
        // =========================================================================
        // CPC, RTB, no discount, no tax on either end, 0.2 mediaCost Margin, 0.15 rm_adservingfee, no pub buyer premium, no datafee
        // =========================================================================
    	// DSP_LIC and CPC campaign charged at AD_SERVED time
        new TestCase(AdAction.AD_SERVED, BidType.CPC, 0.0001,        //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                                    //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                                    //boolean taxablePublisher, double revShare,                                                              
                     0.2,                                            //Double rtbSettlementPrice,                                                                              
                0.0001, null, 0.0002, null,                          //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.2, 0.15, null),                                    //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        // DSP_LIC and CPC campaign not charged at AD_SERVED time
        new TestCase(AdAction.CLICK, BidType.CPC, 0.0001,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.2, 0.15, null),
        new TestCase(AdAction.INSTALL, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.CONVERSION, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),

        // =========================================================================
        // CPC, RTB, no discount, no tax on either end, 0.2 mediaCost Margin, 0.15 rm_adservingfee, no pub buyer premium, no datafee
        // bidAmount > cost
        // =========================================================================
    	// DSP_LIC and CPC campaign charged at AD_SERVED time
        new TestCase(AdAction.AD_SERVED, BidType.CPC, 0.05,         //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                                   //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                                   //boolean taxablePublisher, double revShare,                                                              
                     0.2,                                           //Double rtbSettlementPrice,                                                                              
                0.000433, null, 0.0002, null,                         //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.2, 0.15, null),                                   //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        // DSP_LIC and CPC campaign not charged at CLICK time
        new TestCase(AdAction.CLICK, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.2, 0.15, null),
        new TestCase(AdAction.INSTALL, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.CONVERSION, BidType.CPC, 0.05,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),                
        // =========================================================================
        // CPM, RTB, no discount, no tax on either end, 0.2 mediaCost Margin, 0.15 rm_adservingfee, no pub buyer premium, no datafee
        // =========================================================================
        // DSP_LIC and CPM campaign charged at AD_SERVED time
        new TestCase(AdAction.AD_SERVED, BidType.CPM, 0.10,        //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                                  //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                                  //boolean taxablePublisher, double revShare,                                                              
                     0.2,                                          //Double rtbSettlementPrice,                                                                              
                0.0001, null, 0.0002, null,                        //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.2, 0.15, null),                                  //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        // DSP_LIC and CPM campaign not charged at IMPRESSION time
        new TestCase(AdAction.IMPRESSION, BidType.CPM, 0.10,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.2, 0.15, null),
        new TestCase(AdAction.CLICK, BidType.CPM, 0.10,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.2, 0.15, null),
        new TestCase(AdAction.INSTALL, BidType.CPM, 0.10,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.2, 0.15, null),
        new TestCase(AdAction.CONVERSION, BidType.CPM, 0.10,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.2, 0.15, null),
        // =========================================================================
        // CPM, RTB, no discount, no tax on either end, 0.2 mediaCost Margin, 0.15 rm_adservingfee, no pub buyer premium, no datafee; 
        // bidamount > cost
        // =========================================================================
        // DSP_LIC and CPM campaign charged at AD_SERVED time
        new TestCase(AdAction.AD_SERVED, BidType.CPM, 0.50,        //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                                  //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                                  //boolean taxablePublisher, double revShare,                                                              
                     0.2,                                          //Double rtbSettlementPrice,                                                                              
                0.000433, null, 0.0002, null,                        //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.2, 0.15, null),                                  //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        // DSP_LIC and CPM campaign not charged at IMPRESSION time
        new TestCase(AdAction.IMPRESSION, BidType.CPM, 0.50,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.CLICK, BidType.CPM, 0.50,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.INSTALL, BidType.CPM, 0.50,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.CONVERSION, BidType.CPM, 0.50,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
                
        // =========================================================================
        // CPI, RTB, no discount, no tax on either end, 0.2 mediaCost Margin, 0.15 rm_adservingfee, no pub buyer premium, no datafee;
        // =========================================================================
    	// CPI charged at AD_SERVED time
        new TestCase(AdAction.AD_SERVED, BidType.CPI, 0.0001,  //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                              //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                              //boolean taxablePublisher, double revShare,                                                              
                     0.2,                                      //Double rtbSettlementPrice,                                                                              
                0.0001, null, 0.0002, null,                    //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.2, 0.15, null),                              //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.CLICK, BidType.CPI, 0.43,		
                     false, null,                           
                     false, 0.60,                           
                     0.2,                                   
                null, null, null, null,                     
                0.1, null, null),                           
                
        //CPI no cost at INSTALL time as already charged at AD_SERVED time
        new TestCase(AdAction.INSTALL, BidType.CPI, 0.33,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.CONVERSION, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        // =========================================================================
        // CPI, RTB, no discount, no tax on either end, 0.2 mediaCost Margin, 0.15 rm_adservingfee, no pub buyer premium, no datafee;
        // bidAmount > cost
        // =========================================================================
    	// CPI charged at AD_SERVED time
        new TestCase(AdAction.AD_SERVED, BidType.CPI, 0.53,  //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                            //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                            //boolean taxablePublisher, double revShare,                                                              
                     0.2,                                    //Double rtbSettlementPrice,                                                                              
                0.000433, null, 0.0002, null,                  //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.2, 0.15, null),                            //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.CLICK, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
                
        //CPI no cost at INSTALL time as already charged at AD_SERVED time
        new TestCase(AdAction.INSTALL, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.01, null, null),
        new TestCase(AdAction.CONVERSION, BidType.CPI, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),

        // =========================================================================
        // CPA, RTB, no discount, no tax on either end, 0.2 mediaCost Margin, 0.15 rm_adservingfee, no pub buyer premium, no datafee;
        // =========================================================================
        // CPA charged at AD_SERVED time
        new TestCase(AdAction.AD_SERVED, BidType.CPA, 0.0001,   //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                               //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                               //boolean taxablePublisher, double revShare,                                                              
                     0.2,                                       //Double rtbSettlementPrice,                                                                              
                0.0001, null, 0.0002, null,                     //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.2, 0.15, null),                               //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.CLICK, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.INSTALL, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        //CPA no cost at CONVERSION time as already charged at AD_SERVED time
        new TestCase(AdAction.CONVERSION, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        // =========================================================================
        // CPA, RTB, no discount, no tax on either end, 0.2 mediaCost Margin, 0.15 rm_adservingfee, no pub buyer premium, no datafee;
        // bidAmount > cost        
        // =========================================================================
        // CPA charged at AD_SERVED time
        new TestCase(AdAction.AD_SERVED, BidType.CPA, 0.53,     //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, null,                               //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                               //boolean taxablePublisher, double revShare,                                                              
                     0.2,                                       //Double rtbSettlementPrice,                                                                              
                0.000433, null, 0.0002, null,                     //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.2, 0.15, null),                               //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        new TestCase(AdAction.IMPRESSION, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        new TestCase(AdAction.CLICK, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
                //60
        new TestCase(AdAction.INSTALL, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),
        //CPA no cost at CONVERSION time as already charged at AD_SERVED time
        new TestCase(AdAction.CONVERSION, BidType.CPA, 0.43,
                     false, null,
                     false, 0.60,
                     0.2,
                null, null, null, null,
                0.1, null, null),                
        
        // =========================================================================
        // CPC, non-RTB, 20% discount, no tax on either end 20% marginshareDSP
        // =========================================================================
        new TestCase(AdAction.CLICK, BidType.CPC, 0.05,                 //AdAction adAction, BidType bidType, Double bidAmount,		                                              
                     false, 0.20,                                       //boolean taxableAdvertiser, Double campaignAgencyDiscount,                                               
                     false, 0.60,                                       //boolean taxablePublisher, double revShare,                                                              
                     null,                                              //Double rtbSettlementPrice,                                                                              
                0.05, null, 0.00923, null,                              //Double expectedCost, Double expectedAdvertiserVat, Double expectedPayout, Double expectedPublisherVat,  
                0.0, null,0.20),                                        //Double mediaCostMargin, Double rmAdServingFee,Double marginShareDSP                                     
        
        // =========================================================================
        // CPC, non-RTB, no discount, advertiser taxable, publisher not taxable no marginshareDsp
        // =========================================================================
        new TestCase(AdAction.CLICK, BidType.CPC, 0.05,
                     true, null,
                     false, 0.60,
                     null,
                0.05, 0.01, 0.03, null,
                0.0, null, null),

        // =========================================================================
        // CPC, non-RTB, no discount, advertiser not taxable, publisher taxable, no marginsharedsp
        // =========================================================================
        new TestCase(AdAction.CLICK, BidType.CPC, 0.05,
                     false, null,
                     true, 0.60,
                     null,
                0.05, null, 0.03, 0.006,
                0.0, null, null),
        
        // =========================================================================
        // CPC, non-RTB, 20% discount, advertiser and publisher both taxable, 40% marginsharedsp
        // =========================================================================
        new TestCase(AdAction.CLICK, BidType.CPC, 0.05,
                     true, 0.20,
                     true, 0.60,
                     null,
                0.05, 0.01, 0.015, 0.003,
                0.0, null,0.40),
    };
}

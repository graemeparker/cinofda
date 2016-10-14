package com.adfonic.datacollector;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.AdEvent;
import com.adfonic.datacollector.dao.AccountingDao;
import com.adfonic.datacollector.dao.ClusterDao;
import com.adfonic.datacollector.dao.ToolsDao;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.StopAction;
import com.adfonic.domain.cache.dto.datacollector.campaign.AdvertiserDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherDto;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.util.stats.CounterManager;

public class TestBatchManager extends AbstractAdfonicTest {
    private AccountingDao accountingDao;
    private ClusterDao clusterDao;
    private ToolsDao toolsDao;
    private StoppageManager stoppageManager;
    private CounterManager counterManager;
    private BatchManager batchManager;
    private final long batchDurationMs = 3000L;
    private final int threadPoolSize = 1;

    @Before
    public void runBeforeEachTest() throws java.io.IOException {
        accountingDao = mock(AccountingDao.class);
        clusterDao = mock(ClusterDao.class);
        toolsDao = mock(ToolsDao.class);
        stoppageManager = mock(StoppageManager.class);
        counterManager = mock(CounterManager.class);
        
        batchManager = new BatchManager(batchDurationMs, threadPoolSize);
        inject(batchManager, "accountingDao", accountingDao);
        inject(batchManager, "clusterDao", clusterDao);
        inject(batchManager, "toolsDao", toolsDao);
        inject(batchManager, "stoppageManager", stoppageManager);
        inject(batchManager, "counterManager", counterManager);
    }

    @Test
    public void test02_getCurrentBatchId() throws Exception {
        long batchIdA = batchManager.getCurrentBatchId();
        TimeUnit.MILLISECONDS.sleep(batchDurationMs);
        long batchIdB = batchManager.getCurrentBatchId();
        assertTrue(batchIdB > batchIdA);
    }

    @Test
    public void test03_add_then_flush_click_non_rtb_non_backfill() throws Exception {
        final AdEventAccounting accounting = mock(AdEventAccounting.class, "accounting");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final long publisherId = randomLong();
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final long campaignId = randomLong();
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = new Date();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class, "advertiser");
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final Long userAgentId = randomLong();
        final BigDecimal advertiserSpend = new BigDecimal("0.1234");
        
        final BigDecimal adjustedAdvertiserSpend = new BigDecimal("0.1111");
        final AccountingDao.UpdateBudgetsResult updateBudgetsResult = new AccountingDao.UpdateBudgetsResult(adjustedAdvertiserSpend);
        updateBudgetsResult.addStopAction(StopAction.STOP_CAMPAIGN_FOR_HOUR);
        updateBudgetsResult.addStopAction(StopAction.STOP_CAMPAIGN_FOR_TODAY);
        updateBudgetsResult.addStopAction(StopAction.STOP_CAMPAIGN_FOREVER);
        updateBudgetsResult.addStopAction(StopAction.STOP_ADVERTISER_FOR_TODAY);
        updateBudgetsResult.addStopAction(StopAction.STOP_ADVERTISER_ZERO_BALANCE);
        
        final AdAction adAction = AdAction.CLICK;
        final boolean rtb = false;
        final boolean advertiserCompanyBackfill = false;
        final BigDecimal publisherCreditMultiplier = new BigDecimal("0.6");
        
        final BigDecimal agencyDiscount = mock(BigDecimal.class); 

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (accounting).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getId(); will(returnValue(publisherId));
            allowing (accounting).getCampaign(); will(returnValue(campaign));
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (accounting).getAdEvent(); will(returnValue(adEvent));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).getAdAction(); will(returnValue(adAction));
            allowing (adEvent).isRtb(); will(returnValue(rtb));
            allowing (accounting).getAdvertiser(); will(returnValue(advertiser));
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (advertiserCompany).getDefaultTimeZone(); will(returnValue(TimeZone.getDefault()));
            allowing (accounting).getAdvertiserSpend(); will(returnValue(advertiserSpend));
            allowing (advertiserCompany).isBackfill(); will(returnValue(advertiserCompanyBackfill));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisherCompany).getDefaultTimeZone(); will(returnValue(TimeZone.getDefault()));
            allowing (campaign).getAgencyDiscount(); will(returnValue(agencyDiscount));
            allowing (accounting).getPublisherCreditMultiplier(with(agencyDiscount)); will(returnValue(publisherCreditMultiplier));
            allowing (accountingDao).updateBudgets(with(campaign), with(publisher), with(any(BigDecimal.class)), with(any(BigDecimal.class)), with(any(Integer.class)), with(any(Integer.class)), with(any(Integer.class)), with(any(Integer.class))); will(returnValue(updateBudgetsResult));
            allowing (accounting).setAdjustedAdvertiserSpend(with(any(BigDecimal.class)),with(any(BigDecimal.class)));
            allowing (clusterDao).createAdEventLog(with(accounting), with(userAgentId), with(any(Integer.class)), with(any(Integer.class)));
            allowing (stoppageManager).stopCampaign(eventTime, campaign, CampaignStoppage.Reason.HOURLY_BUDGET);
            allowing (stoppageManager).stopCampaign(eventTime, campaign, CampaignStoppage.Reason.DAILY_BUDGET);
            allowing (stoppageManager).stopCampaign(eventTime, campaign, CampaignStoppage.Reason.OVERALL_BUDGET);
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (stoppageManager).stopAdvertiser(eventTime, advertiser, AdvertiserStoppage.Reason.DAILY_BUDGET);
            allowing (stoppageManager).stopAdvertiser(eventTime, advertiser, AdvertiserStoppage.Reason.ZERO_BALANCE);
        }});
        
        for (int k = 0; k < 100; ++k) {
            if (k > 0 && k % 25 == 0) {
                TimeUnit.SECONDS.sleep(1);
            }
            batchManager.addToCurrentBatch(accounting, userAgentId);
        }

        batchManager.flushBatches();

        TimeUnit.SECONDS.sleep(2); // wait for things to flush async'ly
    }

    @Test
    public void test04_add_then_flush_install_rtb_backfill() throws Exception {
        final AdEventAccounting accounting = mock(AdEventAccounting.class, "accounting");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final long publisherId = randomLong();
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final long campaignId = randomLong();
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = new Date();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class, "advertiser");
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final Long userAgentId = randomLong();
        final BigDecimal advertiserSpend = new BigDecimal("0.1234");
        
        final BigDecimal adjustedAdvertiserSpend = new BigDecimal("0.1111");
        final AccountingDao.UpdateBudgetsResult updateBudgetsResult = new AccountingDao.UpdateBudgetsResult(adjustedAdvertiserSpend);
        
        final AdAction adAction = AdAction.INSTALL;
        final boolean rtb = true;
        final boolean advertiserCompanyBackfill = true;
        final BigDecimal publisherCreditMultiplier = new BigDecimal("0.6");
        
        final BigDecimal agencyDiscount = mock(BigDecimal.class); 

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (accounting).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getId(); will(returnValue(publisherId));
            allowing (accounting).getCampaign(); will(returnValue(campaign));
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (accounting).getAdEvent(); will(returnValue(adEvent));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).getAdAction(); will(returnValue(adAction));
            allowing (adEvent).isRtb(); will(returnValue(rtb));
            allowing (accounting).getAdvertiser(); will(returnValue(advertiser));
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (advertiserCompany).getDefaultTimeZone(); will(returnValue(TimeZone.getDefault()));
            allowing (accounting).getAdvertiserSpend(); will(returnValue(advertiserSpend));
            allowing (advertiserCompany).isBackfill(); will(returnValue(advertiserCompanyBackfill));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisherCompany).getDefaultTimeZone(); will(returnValue(TimeZone.getDefault()));
            allowing (accounting).getPublisherCreditMultiplier(with(any(BigDecimal.class))); will(returnValue(publisherCreditMultiplier));
            allowing (accountingDao).updateBudgets(with(campaign), with(publisher), with(any(BigDecimal.class)), with(any(BigDecimal.class)), with(any(Integer.class)), with(any(Integer.class)), with(any(Integer.class)), with(any(Integer.class))); will(returnValue(updateBudgetsResult));
            allowing (campaign).getAgencyDiscount(); will(returnValue(agencyDiscount));
            allowing (accounting).setAdjustedAdvertiserSpend(with(any(BigDecimal.class)),with(agencyDiscount));
            allowing (clusterDao).createAdEventLog(with(accounting), with(userAgentId), with(any(Integer.class)), with(any(Integer.class)));
            allowing (toolsDao).markCampaignInstallTrackingVerified(campaignId);
        }});
        
        for (int k = 0; k < 100; ++k) {
            if (k > 0 && k % 25 == 0) {
                TimeUnit.SECONDS.sleep(1);
            }
            batchManager.addToCurrentBatch(accounting, userAgentId);
        }

        batchManager.flushBatches();

        TimeUnit.SECONDS.sleep(2); // wait for things to flush async'ly
    }

    @Test
    public void test05_add_then_flush_conversion_non_rtb_backfill() throws Exception {
        final AdEventAccounting accounting = mock(AdEventAccounting.class, "accounting");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final long publisherId = randomLong();
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final long campaignId = randomLong();
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = new Date();
        final AdvertiserDto advertiser = mock(AdvertiserDto.class, "advertiser");
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final Long userAgentId = randomLong();
        final BigDecimal advertiserSpend = new BigDecimal("0.1234");
        
        final BigDecimal adjustedAdvertiserSpend = new BigDecimal("0.1111");
        final AccountingDao.UpdateBudgetsResult updateBudgetsResult = new AccountingDao.UpdateBudgetsResult(adjustedAdvertiserSpend);
        
        final AdAction adAction = AdAction.CONVERSION;
        final boolean rtb = false;
        final boolean advertiserCompanyBackfill = true;
        final BigDecimal publisherCreditMultiplier = new BigDecimal("0.6");
        final BigDecimal payout = new BigDecimal("1.234");
        final BigDecimal publisherVat = new BigDecimal("0.088");

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (accounting).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getId(); will(returnValue(publisherId));
            allowing (accounting).getCampaign(); will(returnValue(campaign));
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (accounting).getAdEvent(); will(returnValue(adEvent));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).getAdAction(); will(returnValue(adAction));
            allowing (adEvent).isRtb(); will(returnValue(rtb));
            allowing (accounting).getAdvertiser(); will(returnValue(advertiser));
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (advertiserCompany).getDefaultTimeZone(); will(returnValue(TimeZone.getDefault()));
            allowing (accounting).getAdvertiserSpend(); will(returnValue(advertiserSpend));
            allowing (advertiserCompany).isBackfill(); will(returnValue(advertiserCompanyBackfill));
            allowing (accounting).getPayout(); will(returnValue(payout));
            allowing (accounting).getPublisherVat(); will(returnValue(publisherVat));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisherCompany).getDefaultTimeZone(); will(returnValue(TimeZone.getDefault()));
            allowing (accounting).getPublisherCreditMultiplier(with(any(BigDecimal.class))); will(returnValue(publisherCreditMultiplier));
            allowing (accountingDao).updateBudgets(with(campaign), with(publisher), with(any(BigDecimal.class)), with(any(BigDecimal.class)), with(any(Integer.class)), with(any(Integer.class)), with(any(Integer.class)), with(any(Integer.class))); will(returnValue(updateBudgetsResult));
            allowing (accounting).setAdjustedAdvertiserSpend(with(any(BigDecimal.class)),with(any(BigDecimal.class)));
            allowing (clusterDao).createAdEventLog(with(accounting), with(userAgentId), with(any(Integer.class)), with(any(Integer.class)));
            allowing (accountingDao).incrementPublisherBalance(with(publisher), with(any(BigDecimal.class)));
            allowing (toolsDao).markCampaignConversionTrackingVerified(campaignId);
        }});
        
        for (int k = 0; k < 100; ++k) {
            if (k > 0 && k % 25 == 0) {
                TimeUnit.SECONDS.sleep(1);
            }
            batchManager.addToCurrentBatch(accounting, userAgentId);
        }

        batchManager.flushBatches();

        TimeUnit.SECONDS.sleep(2); // wait for things to flush async'ly
    }
}

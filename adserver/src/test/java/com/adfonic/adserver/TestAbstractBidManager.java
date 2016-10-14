package com.adfonic.adserver;

import static com.adfonic.adserver.test.util.ConcurrentUtils.runInParallel;
import static com.adfonic.adserver.test.util.DtoStubBuilder.createAdEvent;
import static com.adfonic.adserver.test.util.DtoStubBuilder.createAdSpaceRandomStub;
import static com.adfonic.adserver.test.util.DtoStubBuilder.createBidDetailsRandomStub;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.impl.FrequencyCapper;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.test.util.ParallelTestRunnable;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestAbstractBidManager extends BaseAdserverTest {

    private AtomicLong batchId = new AtomicLong(1);

    private static final class BidManagerImpl extends AbstractBidManager {
        private List<BidDetails> failedBids = new ArrayList<>();

        BidManagerImpl(BidCacheService bidCacheService) {
            super(bidCacheService);
        }

        @Override
        protected void onBidFailed(BidDetails bidDetails, Impression impression, TargetingContext context, AdEvent bidFailedEvent, String lossReason) {
            failedBids.add(bidDetails);
        }
    }

    private AdEventFactory adEventFactory;
    private BackupLogger backupLogger;
    private TargetingContextFactory targetingContextFactory;
    private BidCacheService bidCacheService;
    private BidManagerImpl bidManager;
    private AdserverDomainCache adserverDomainCache;
    private TargetingContext context;
    private FrequencyCapper frequencyCapper;
    private CreativeDto creative;
    private CampaignDto campaign;
    private AdServerStats astats;

    @Before
    public void runBeforeEachTest() throws InterruptedException {
        adEventFactory = mock(AdEventFactory.class);
        backupLogger = mock(BackupLogger.class);
        targetingContextFactory = mock(TargetingContextFactory.class);
        bidCacheService = mock(BidCacheService.class);
        bidManager = new BidManagerImpl(bidCacheService);
        frequencyCapper = mock(FrequencyCapper.class);
        creative = mock(CreativeDto.class);
        campaign = mock(CampaignDto.class);
        astats = mock(AdServerStats.class);
        inject(bidManager, "adEventFactory", adEventFactory);
        inject(bidManager, "backupLogger", backupLogger);
        inject(bidManager, "frequencyCapper", frequencyCapper);
        inject(bidManager, "targetingContextFactory", targetingContextFactory);
        inject(bidManager, "astats", astats);

        context = mock(TargetingContext.class, "context");
        adserverDomainCache = mock(AdserverDomainCache.class);

    }

    @Test
    public void savesBidDetailsAndImpressionUsingService() {

        final BidDetails bidDetails = createBidDetailsRandomStub(context);

        expect(new Expectations() {
            {
                oneOf(bidCacheService).saveBidDetails(bidDetails.getImpression().getExternalID(), bidDetails);
            }
        });
        bidManager.saveBidDetails(bidDetails, 1);
        assertThat(bidManager.getQueueSize(), is(1));

    }

    @Test
    public void getsAndRemovesWonBidDetailsFromService() {
        final BidDetails bidDetails = createBidDetailsRandomStub(context);
        final String impressionExternalID = bidDetails.getImpression().getExternalID();

        expect(new Expectations() {
            {
                oneOf(bidCacheService).getAndRemoveBidDetails(impressionExternalID);
                will(returnValue(bidDetails));
            }
        });
        assertEquals(bidDetails, bidManager.removeBidDetails(impressionExternalID));

    }

    public void addsNewBidDetailsToCurrentBidBatch() throws Exception {

        for (int k = 0; k < 100; ++k) {
            bidManager.addToCurrentBidBatch(createBidDetailsRandomStub(context), 1);
        }
        assertThat(bidManager.getQueueSize(), is(100));
        Thread.sleep(1000);
        bidManager.logFailedBids();
        assertThat(bidManager.failedBids.size(), is(0));
    }

    @Test
    public void addsBidDetailsConcurrently() throws InterruptedException {

        assertThat(bidManager.getQueueSize(), is(0));

        final int times = 2000;

        final BidDetails[] bidDetails = createRandomBidDetails(times);

        ParallelTestRunnable addBidTask = new ParallelTestRunnable() {
            @Override
            public void run(int taskNumber) {
                for (int i = 0; i < times; i++) {
                    bidManager.addToCurrentBidBatch(bidDetails[i], 1);
                }
            }
        };
        int threadNumber = 20;
        runInParallel(threadNumber, addBidTask);

        incrementBatchId();

        runInParallel(threadNumber, addBidTask);

        incrementBatchId();

        runInParallel(threadNumber, addBidTask);

        assertThat(bidManager.getQueueSize(), is(threadNumber * times * 3));

        Thread.sleep(1000);
    }

    private void incrementBatchId() {
        batchId.incrementAndGet();
    }

    public void addsBidDetailsConcurrentlyWithoutLocks() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);

        System.gc();

        expect(new Expectations() {
            {
                allowing(bidCacheService).removeBidDetails(with(any(String.class)));
                will(takeSomeTime(counter));
            }
        });

        //prepare some bids for the logFailed to cleanUp
        int previousBids = 10;
        for (int i = 0; i < previousBids; i++) {
            bidManager.addToCurrentBidBatch(createBidDetailsRandomStub(context), 1);
        }

        assertThat(bidManager.getQueueSize(), is(previousBids));

        incrementBatchId();

        final int times = 100; //1000
        int threadNumber = 200;
        final BidDetails[] bidDetails = createRandomBidDetails(times);

        ParallelTestRunnable addBidTask = new ParallelTestRunnable() {

            @Override
            public void run(int taskNumber) {
                for (int i = 0; i < times; i++) {
                    bidManager.addToCurrentBidBatch(bidDetails[i], 1);
                }
            }
        };

        ParallelTestRunnable logFailedTask = new ParallelTestRunnable() {
            @Override
            public void run(int taskNumber) {
                for (int i = 0; i < 10; i++) {

                    try {
                        bidManager.logFailedBids();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        runInParallel(threadNumber, addBidTask, logFailedTask);

        assertThat(counter.get() + bidManager.getQueueSize(), is(threadNumber * times + previousBids));
    }

    private BidDetails[] createRandomBidDetails(int times) {
        final BidDetails[] bidDetails = new BidDetails[times];
        for (int i = 0; i < times; i++) {
            bidDetails[i] = createBidDetailsRandomStub(context);
        }
        return bidDetails;
    }

    private Action takeSomeTime(final AtomicInteger counter) {
        return new Action() {
            @Override
            public Object invoke(org.jmock.api.Invocation invocation) throws Throwable {
                counter.incrementAndGet();
                Thread.sleep(100);
                //                System.out.println("slept");
                return false; //we don't really care
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Simulating slow network connection");
            }
        };
    }

    public void removesLostBidsFromOldBatchesOnly() throws Exception {

        final BidDetails bidDetails1 = createBidDetailsRandomStub(context);
        final BidDetails bidDetails2 = createBidDetailsRandomStub(context);
        final AdSpaceDto adSpace = createAdSpaceRandomStub();
        final AdEvent newEvent = createAdEvent(AdAction.BID_FAILED);
        final Impression impression = bidDetails1.getImpression();
        final String externalID = impression.getExternalID();
        final Map<Long, String> deviceIdentifiers = Collections.emptyMap();
        final String secureTrackingId = randomAlphaNumericString(10);
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.PUBLISHER_GENERATED;
        final Integer periodSecond = 3600;
        final Long creativeId = 0L;
        final Date eventTime = new Date();

        expect(new Expectations() {
            {
                // Since the bid lost, it will be found in cache
                oneOf(bidCacheService).removeBidDetails(externalID);
                will(returnValue(true));
                allowing(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(context).setAdSpace(adSpace);
                oneOf(adserverDomainCache).getAdSpaceById(impression.getAdSpaceId());
                will(returnValue(adSpace));
                oneOf(adserverDomainCache).getCreativeById(impression.getCreativeId());
                will(returnValue(creative));
                oneOf(adEventFactory).newInstance(AdAction.BID_FAILED, impression.getCreationTime(), impression.getUserTimeZone());
                will(returnValue(newEvent));
                allowing(context).populateAdEvent(newEvent, impression, creative);
                allowing(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(deviceIdentifiers));
                allowing(context).getAttribute(TargetingContext.SECURE_TRACKING_ID);
                will(returnValue(secureTrackingId));
                allowing(context).getAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE);
                will(returnValue(trackingIdentifierType));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getCapPeriodSeconds();
                will(returnValue(periodSecond));
                allowing(creative).getId();
                will(returnValue(creativeId));
                oneOf(frequencyCapper).checkAndDecrement(context, creative);
                oneOf(newEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logRtbLoss(impression, eventTime, context);

            }
        });

        bidManager.addToCurrentBidBatch(bidDetails1, 1);

        assertThat(bidManager.getQueueSize(), is(1));

        // And make sure there's a 2nd, current batch that won't be flushed
        incrementBatchId();
        bidManager.addToCurrentBidBatch(bidDetails2, 1);
        bidManager.addToCurrentBidBatch(bidDetails2, 1);
        bidManager.addToCurrentBidBatch(bidDetails2, 1);
        assertThat(bidManager.getQueueSize(), is(4));

        Thread.sleep(1000);
        bidManager.logFailedBids();

        assertThat(bidManager.getQueueSize(), is(3));
        assertThat(bidManager.failedBids.size(), is(1)); //everything apart current batch is flushed
    }

    @Test
    public void doesntLogWonBids() throws Exception {
        final BidDetails bidDetails = createBidDetailsRandomStub(context);
        final String impressionExternalID = bidDetails.getImpression().getExternalID();

        expect(new Expectations() {
            {
                // Since the bid won, it won't be found in cache
                oneOf(bidCacheService).removeBidDetails(impressionExternalID);
                will(returnValue(false));
            }
        });

        bidManager.addToCurrentBidBatch(bidDetails, 1);
        Thread.sleep(2000);
        bidManager.logFailedBids();

        assertThat(bidManager.failedBids.size(), is(0)); //no failed bids

    }

    @Test
    public void doesntLogLostBidsWithNotExistingAdSpace() throws Exception {
        final BidDetails bidDetails = createBidDetailsRandomStub(context);
        final long adSpaceId = bidDetails.getImpression().getAdSpaceId();
        Impression impression = bidDetails.getImpression();
        expect(new Expectations() {
            {
                allowing(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getAdSpaceById(adSpaceId);
                will(returnValue(null));
                oneOf(astats).loss(impression, "my reason");
            }
        });
        bidManager.logFailedBid(bidDetails, "my reason");

        assertThat(bidManager.failedBids.size(), is(0)); //no failed bids
    }

    @Test
    public void doesLogLostBidsWithRecentlyStoppedCreatives() throws Exception {

        final BidDetails bidDetails = createBidDetailsRandomStub(context);
        final Impression impression = bidDetails.getImpression();
        final AdSpaceDto adSpace = createAdSpaceRandomStub();
        final AdEvent newEvent = createAdEvent(AdAction.BID_FAILED);
        final Map<Long, String> deviceIdentifiers = Collections.emptyMap();
        final String secureTrackingId = randomAlphaNumericString(10);
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.PUBLISHER_GENERATED;
        final Integer periodSecond = 3600;
        final Long creativeId = 0L;

        expect(new Expectations() {
            {
                allowing(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getAdSpaceById(impression.getAdSpaceId());
                will(returnValue(adSpace));
                oneOf(context).setAdSpace(adSpace);
                oneOf(adserverDomainCache).getCreativeById(impression.getCreativeId());
                will(returnValue(null));
                oneOf(adserverDomainCache).getRecentlyStoppedCreativeById(impression.getCreativeId());
                will(returnValue(creative));
                oneOf(adEventFactory).newInstance(AdAction.BID_FAILED, impression.getCreationTime(), impression.getUserTimeZone());
                will(returnValue(newEvent));
                oneOf(context).populateAdEvent(newEvent, impression, creative);
                allowing(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(deviceIdentifiers));
                allowing(context).getAttribute(TargetingContext.SECURE_TRACKING_ID);
                will(returnValue(secureTrackingId));
                allowing(context).getAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE);
                will(returnValue(trackingIdentifierType));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).isCapPerCampaign();
                will(returnValue(false));
                allowing(creative).getId();
                will(returnValue(creativeId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).isCapPerCampaign();
                will(returnValue(false));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getCapPeriodSeconds();
                will(returnValue(periodSecond));

                oneOf(frequencyCapper).checkAndDecrement(context, creative);
                oneOf(astats).loss(impression, "my reason");
            }
        });
        bidManager.logFailedBid(bidDetails, "my reason");

        assertThat(bidManager.failedBids.size(), is(1));

    }

    @Test
    public void doesntLogLostBidsWithNotExistingCreative() throws Exception {

        final BidDetails bidDetails = createBidDetailsRandomStub(context);
        final long adSpaceId = bidDetails.getImpression().getAdSpaceId();
        final long creativeId = bidDetails.getImpression().getCreativeId();
        final AdSpaceDto adSpace = createAdSpaceRandomStub();

        Impression impression = bidDetails.getImpression();
        expect(new Expectations() {
            {
                allowing(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getAdSpaceById(adSpaceId);
                will(returnValue(adSpace));
                oneOf(context).setAdSpace(adSpace);
                oneOf(adserverDomainCache).getCreativeById(creativeId);
                will(returnValue(null));
                oneOf(adserverDomainCache).getRecentlyStoppedCreativeById(creativeId);
                will(returnValue(null));
                oneOf(astats).loss(impression, "my reason");
            }
        });
        bidManager.logFailedBid(bidDetails, "my reason");

        assertThat(bidManager.failedBids.size(), is(0));
    }

    @Test
    public void createsLogEventAndRemovesLostBids() throws Exception {
        final BidDetails bidDetails = createBidDetailsRandomStub(context);
        final AdSpaceDto adSpace = createAdSpaceRandomStub();
        final AdEvent newEvent = createAdEvent(AdAction.BID_FAILED);
        final Impression impression = bidDetails.getImpression();
        final Map<Long, String> deviceIdentifiers = Collections.emptyMap();
        final String secureTrackingId = randomAlphaNumericString(10);
        final TrackingIdentifierType trackingIdentifierType = TrackingIdentifierType.PUBLISHER_GENERATED;
        final Integer periodSecond = 3600;
        final Long creativeId = 0L;

        expect(new Expectations() {
            {
                allowing(context).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getAdSpaceById(impression.getAdSpaceId());
                will(returnValue(adSpace));
                oneOf(context).setAdSpace(adSpace);
                oneOf(adserverDomainCache).getCreativeById(impression.getCreativeId());
                will(returnValue(creative));
                oneOf(adEventFactory).newInstance(AdAction.BID_FAILED, impression.getCreationTime(), impression.getUserTimeZone());
                will(returnValue(newEvent));
                oneOf(context).populateAdEvent(newEvent, impression, creative);
                allowing(context).getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
                will(returnValue(deviceIdentifiers));
                allowing(context).getAttribute(TargetingContext.SECURE_TRACKING_ID);
                will(returnValue(secureTrackingId));
                allowing(context).getAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE);
                will(returnValue(trackingIdentifierType));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).isCapPerCampaign();
                will(returnValue(false));
                allowing(creative).getId();
                will(returnValue(creativeId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).isCapPerCampaign();
                will(returnValue(false));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getCapPeriodSeconds();
                will(returnValue(periodSecond));
                allowing(frequencyCapper).checkAndDecrement(context, creative);
                oneOf(astats).loss(impression, "my reason");
            }
        });
        bidManager.logFailedBid(bidDetails, "my reason");
    }

    @Test
    public void getsContextFromBidDetailsIfPresent() {
        final BidDetails bidDetails = createBidDetailsRandomStub(context);

        assertEquals(context, bidManager.getTargetingContextFromBidDetails(bidDetails));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfBidDetailsHasNoContextAndInvalidIp() throws Exception {
        final BidDetails bidDetails = createBidDetailsRandomStub(null);
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext();
                will(returnValue(context));
                oneOf(context).setIpAddress(bidDetails.getIpAddress());
                will(throwException(new InvalidIpAddressException("bummer")));
            }
        });
        bidManager.getTargetingContextFromBidDetails(bidDetails);
    }

    @Test
    public void createTargetContextWithIpFromBidDetailsIfBidDetailsHasNoContext() throws Exception {
        final BidDetails bidDetails = createBidDetailsRandomStub(null);
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext();
                will(returnValue(context));
                oneOf(context).setSslRequired(false);

                oneOf(context).setAttribute("\\vp", null); // VAST protocol
                oneOf(context).setIpAddress(bidDetails.getIpAddress());
                oneOf(context).setAttribute("\\di", Collections.EMPTY_MAP);
            }
        });

        assertEquals(context, bidManager.getTargetingContextFromBidDetails(bidDetails));
    }
}

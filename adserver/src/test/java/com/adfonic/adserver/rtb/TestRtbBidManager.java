package com.adfonic.adserver.rtb;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.test.AbstractAdfonicTest;

public class TestRtbBidManager extends AbstractAdfonicTest {
    private RtbCacheService rtbCacheService;
    private RtbBidManager rtbBidManager;
    private DomainCache domainCache;
    private TargetingContext context;
    private Impression impression;
    private TargetingContextFactory targetingContextFactory;

    @Before
    public void runBeforeEachTest() {
        rtbCacheService = mock(RtbCacheService.class);
        rtbBidManager = new RtbBidManager(rtbCacheService);
        targetingContextFactory = mock(TargetingContextFactory.class);
        inject(rtbBidManager, "targetingContextFactory", targetingContextFactory);
        domainCache = mock(DomainCache.class);
        context = mock(TargetingContext.class, "context");
        impression = mock(Impression.class);
    }

    @Test
    public void test00_create_context_from_rtb_details() throws Exception {
        final RtbBidDetails bidDetails = mock(RtbBidDetails.class, "bidDetails");
        expect(new Expectations() {
            {
                oneOf(bidDetails).getBidTimeTargetingContext();
                will(returnValue(null));
                oneOf(targetingContextFactory).createTargetingContext();
                will(returnValue(context));
                allowing(bidDetails).getPlatformId();
                will(returnValue(null));

                oneOf(bidDetails).getIpAddress();
                will(returnValue("30.30.30.30"));

                oneOf(context).setIpAddress("30.30.30.30");

                oneOf(bidDetails).getImpression();
                will(returnValue(impression));
                oneOf(impression).getDeviceIdentifiers();
                will(returnValue(null));
                oneOf(context).setAttribute("\\di", null);

                oneOf(impression).getSslRequired();
                will(returnValue(true));
                oneOf(context).setSslRequired(true);

                oneOf(impression).getVideoProtocol();
                will(returnValue(5));
                oneOf(context).setAttribute("\\vp", 5);

            }
        });
        assertEquals(context, rtbBidManager.getTargetingContextFromBidDetails(bidDetails));
    }

    @Test
    public void test01_getTargetingContextFromBidDetails_no_platform_id() {
        final RtbBidDetails bidDetails = mock(RtbBidDetails.class, "bidDetails");
        expect(new Expectations() {
            {
                oneOf(bidDetails).getBidTimeTargetingContext();
                will(returnValue(context));
                allowing(bidDetails).getPlatformId();
                will(returnValue(null));
            }
        });
        assertEquals(context, rtbBidManager.getTargetingContextFromBidDetails(bidDetails));
    }

    @Test
    public void test02_getTargetingContextFromBidDetails_platform_not_found() {
        final RtbBidDetails bidDetails = mock(RtbBidDetails.class, "bidDetails");
        final long platformId = randomLong();
        expect(new Expectations() {
            {
                oneOf(bidDetails).getBidTimeTargetingContext();
                will(returnValue(context));
                allowing(bidDetails).getPlatformId();
                will(returnValue(platformId));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getPlatformById(platformId);
                will(returnValue(null));
            }
        });
        assertEquals(context, rtbBidManager.getTargetingContextFromBidDetails(bidDetails));
    }

    @Test
    public void test03_getTargetingContextFromBidDetails_platform_found() {
        final RtbBidDetails bidDetails = mock(RtbBidDetails.class, "bidDetails");
        final long platformId = randomLong();
        final PlatformDto platform = mock(PlatformDto.class, "platform");
        expect(new Expectations() {
            {
                oneOf(bidDetails).getBidTimeTargetingContext();
                will(returnValue(context));
                allowing(bidDetails).getPlatformId();
                will(returnValue(platformId));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getPlatformById(platformId);
                will(returnValue(platform));
                allowing(platform).getId();
                will(returnValue(platformId));
                oneOf(context).setAttribute(TargetingContext.PLATFORM, platform);
            }
        });
        assertEquals(context, rtbBidManager.getTargetingContextFromBidDetails(bidDetails));
    }
}

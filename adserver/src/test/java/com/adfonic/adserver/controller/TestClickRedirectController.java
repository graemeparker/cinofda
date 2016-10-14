package com.adfonic.adserver.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.impl.ClickUtils;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class TestClickRedirectController extends BaseAdserverTest {

    private ClickRedirectController clickRedirectController;
    private TargetingContextFactory targetingContextFactory;
    private PreProcessor preProcessor;
    private ImpressionService impressionService;
    private BackupLogger backupLogger;
    private ClickUtils clickUtils;
    private AdServerStats astats;

    @Before
    public void initTests() {
        clickRedirectController = new ClickRedirectController();
        targetingContextFactory = mock(TargetingContextFactory.class, "targetingContextFactory");
        preProcessor = mock(PreProcessor.class, "preProcessor");
        impressionService = mock(ImpressionService.class, "impressionService");
        backupLogger = mock(BackupLogger.class);
        clickUtils = mock(ClickUtils.class, "clickUtils");
        astats = mock(AdServerStats.class);
        inject(clickRedirectController, "targetingContextFactory", targetingContextFactory);
        inject(clickRedirectController, "preProcessor", preProcessor);
        inject(clickRedirectController, "impressionService", impressionService);
        inject(clickRedirectController, "clickUtils", clickUtils);
        inject(clickRedirectController, "astats", astats);
        inject(clickRedirectController, "backupLogger", backupLogger);
    }

    /*
     * When impression Do not exists in cache for given impressionExternalId
     * OUTPUT: Client should be redirected to fallback URL
     */
    @Test
    public void testClickRedirectController01_handleClickRedirectRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final String redir = "http://www.newurl.com/newpage/redirected";
        final String fallBackUrl = "http://www.errorurl.com/errorpage/redirected";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class);
        final Impression impression = null;
        final AdSpaceDto adSpace = null;
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                oneOf(clickUtils).redirectToFallbackUrl(request, response);
                will(new Action() {
                    @Override
                    public Object invoke(Invocation invocation) throws Throwable {
                        response.sendRedirect(fallBackUrl);
                        return null;
                    }

                    @Override
                    public void describeTo(Description arg0) {
                    }
                });
                allowing(targetingContext).getAdSpace();
                will(returnValue(adSpace));
                oneOf(astats).increment(adSpace, AsCounter.ClickImpressionNotFound);
                oneOf(backupLogger).logClickFailure(impressionExternalID, "Impression not found", targetingContext);
            }
        });

        clickRedirectController.handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
        assertNotNull(response.getRedirectedUrl());
        assertEquals(fallBackUrl, response.getRedirectedUrl());
    }

    /*
     * When impression DO exists in cache for given impressionExternalId
     * and adSpace do not exists for given adSpaceExternalid
     * OUTPUT: Client should be redirected to redirect url, given in request as parameter redir
     */
    @Test
    public void testClickRedirectController02a_handleClickRedirectRequest_noCreative() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final String redir = "http://www.newurl.com/newpage/redirected";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Impression impression = mock(Impression.class, "impression");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = null;
        final long creativeId = randomLong();

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(adserverDomainCache).getAdSpaceById(1L);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);

                allowing(impression).getCreativeId();
                will(returnValue(creativeId));
                allowing(impression).getAdSpaceId();
                will(returnValue(1L));
                oneOf(adserverDomainCache).getCreativeById(creativeId);
                will(returnValue(null));
                oneOf(adserverDomainCache).getRecentlyStoppedCreativeById(creativeId);
                will(returnValue(null));

                oneOf(clickUtils).setClickIdCookie(response, impression, null);
                allowing(clickUtils).processRedirectUrl(redir, false, null, null, impression, targetingContext, false);
                will(returnValue(redir));

                oneOf(astats).click(impression);
                oneOf(astats).increment(AsCounter.ClickAdSpaceNotFound);
                oneOf(astats).increment(adSpace, AsCounter.ClickCreativeNotFound);
                oneOf(astats).clickCompleted(1L, creativeId);

                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        clickRedirectController.handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
        //System.out.println("response.getRedirectedUrl()="+response.getRedirectedUrl());
        assertNotNull(response.getRedirectedUrl());
        assertEquals(redir, response.getRedirectedUrl());
    }

    /*
     * When impression DO exists in cache for given impressionExternalId
     * and adSpace do not exists for given adSpaceExternalid
     * OUTPUT: Client should be redirected to redirect url, given in request as parameter redir
     */
    @Test
    public void testClickRedirectController02b_handleClickRedirectRequest_withCreative() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final String redir = "http://www.newurl.com/newpage/redirected";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Impression impression = mock(Impression.class, "impression");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = null;
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final long creativeId = randomLong();
        final CampaignDto campaign = mock(CampaignDto.class);
        final DomainCache domainCache = mock(DomainCache.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(adserverDomainCache).getAdSpaceById(1L);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);

                allowing(impression).getCreativeId();
                will(returnValue(creativeId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).isConversionTrackingEnabled();
                will(returnValue(true));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                oneOf(adserverDomainCache).getCreativeById(creativeId);
                will(returnValue(creative));
                oneOf(clickUtils).setClickIdCookie(response, impression, creative);
                oneOf(clickUtils).trackClick(adSpace, creative, impression, targetingContext, null);
                allowing(clickUtils).processRedirectUrl(redir, false, null, creative, impression, targetingContext, false);
                will(returnValue(redir));

                oneOf(astats).click(impression);
                oneOf(astats).increment(AsCounter.ClickAdSpaceNotFound);
                oneOf(astats).clickCompleted(1L, creativeId);

                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getCreativeId();
                will(returnValue(1L));
                allowing(impression).getAdSpaceId();
                will(returnValue(1L));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(creative));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(creative).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
            }
        });

        clickRedirectController.handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
        //System.out.println("response.getRedirectedUrl()=" + response.getRedirectedUrl());
        assertNotNull(response.getRedirectedUrl());
        assertEquals(redir, response.getRedirectedUrl());

    }

    /*
     * When impression DO exists in cache for given impressionExternalId
     * and adSpace DO exists, but impression.getAdSpace != adSpace.id
     * Output : Error HttpServletResponse.SC_UNAUTHORIZED should be sent to client
     */
    @Test
    public void testClickRedirectController03_handleClickRedirectRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final String redir = "http://www.newurl.com/newpage/redirected";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Impression impression = mock(Impression.class, "impression");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final long adSpaceId = randomLong();
        final long impresionAdSpaceId = adSpaceId + 1;

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                oneOf(astats).click(impression);
                oneOf(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(impression).getAdSpaceId();
                will(returnValue(impresionAdSpaceId));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getCreativeId();
                will(returnValue(1L));
                allowing(adserverDomainCache).getCreativeById(1L);
                will(returnValue(null));
                oneOf(astats).increment(adSpace, AsCounter.ClickAdSpaceMismatch);
                oneOf(backupLogger).logClickFailure(impression, "AdSpace mismatch", targetingContext);
            }
        });

        clickRedirectController.handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
        //System.out.println("response.getRedirectedUrl()="+response.getRedirectedUrl());
        //System.out.println("response.getStatus()="+response.getStatus());
        assertNull(response.getRedirectedUrl());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    /*
     * When impression DO exists in cache for given impressionExternalId
     * and adSpace DO exists, but impression.getAdSpace == adSpace.id
     * OUTPUT: Client should be redirected to redirect url, given in request as parameter redir
     */
    @Test
    public void testClickRedirectController04_handleClickRedirectRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final String redir = "http://www.newurl.com/newpage/redirected";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Impression impression = mock(Impression.class, "impression");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final long adSpaceId = randomLong();
        final long impresionAdSpaceId = adSpaceId;
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final long creativeId = randomLong();

        final CampaignDto campaign = mock(CampaignDto.class);
        final DomainCache domainCache = mock(DomainCache.class);

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(impression).getAdSpaceId();
                will(returnValue(impresionAdSpaceId));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                allowing(impression).getCreativeId();
                will(returnValue(creativeId));
                oneOf(targetingContext).setAdSpace(adSpace);

                allowing(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).isConversionTrackingEnabled();
                will(returnValue(true));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));

                oneOf(adserverDomainCache).getCreativeById(creativeId);
                will(returnValue(creative));
                oneOf(clickUtils).setClickIdCookie(response, impression, creative);
                oneOf(clickUtils).trackClick(adSpace, creative, impression, targetingContext, null);
                allowing(clickUtils).processRedirectUrl(redir, false, adSpace, creative, impression, targetingContext, false);
                will(returnValue(redir));

                oneOf(astats).click(impression);
                oneOf(astats).clickCompleted(adSpaceId, creativeId);
            }
        });

        clickRedirectController.handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
        //System.out.println("response.getRedirectedUrl()=" + response.getRedirectedUrl());
        assertNotNull(response.getRedirectedUrl());
        assertEquals(redir, response.getRedirectedUrl());
    }

    /*
     * When creating TaregttingContext throws InvalidIpAddresssException
     * Output : Error HttpServletResponse.SC_BAD_REQUEST should be sent to client
     */
    @Test
    public void testClickRedirectController05_handleClickRedirectRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final String redir = "http://www.newurl.com/newpage/redirected";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(new InvalidIpAddressException("Invalid IP Address")));
                oneOf(backupLogger).logClickFailure(impressionExternalID, "Invalid IP Address", null);
            }
        });

        clickRedirectController.handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
        //System.out.println("response.getRedirectedUrl()=" + response.getRedirectedUrl());
        //System.out.println("response.getStatus()=" + response.getStatus());
        assertNull(response.getRedirectedUrl());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    /*
     * When creating TaregttingContext throws Exception(any)
     * Output : Error HttpServletResponse.SC_BAD_REQUEST should be sent to client
     */
    @Test
    public void testClickRedirectController06_handleClickRedirectRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final String redir = "http://www.newurl.com/newpage/redirected";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        RuntimeException runtimeException = new RuntimeException("Just a regular exception, catch this");
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(runtimeException));
                oneOf(backupLogger).logClickFailure(impressionExternalID, "exception", null, runtimeException.getClass().getName().toString(), runtimeException.getMessage());
            }
        });

        clickRedirectController.handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
        //System.out.println("response.getRedirectedUrl()=" + response.getRedirectedUrl());
        //System.out.println("response.getStatus()=" + response.getStatus());
        assertNull(response.getRedirectedUrl());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    /*
     * When preProcessing request throws com.adfonic.adserver.BlacklistedException
     * Output : Error HttpServletResponse.SC_UNAUTHORIZED should be sent to client
     */
    @Test
    public void testClickRedirectController07_handleClickRedirectRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final String redir = "http://www.newurl.com/newpage/redirected";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                will(throwException(new BlacklistedException("Black listed IP address")));
                oneOf(backupLogger).logClickFailure(impressionExternalID, "blacklisted", null, "Black listed IP address");
            }
        });

        clickRedirectController.handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
        //System.out.println("response.getRedirectedUrl()=" + response.getRedirectedUrl());
        //System.out.println("response.getStatus()=" + response.getStatus());
        assertNull(response.getRedirectedUrl());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
}

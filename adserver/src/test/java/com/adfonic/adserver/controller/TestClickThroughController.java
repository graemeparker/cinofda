package com.adfonic.adserver.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.AdResponseLogic;
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
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class TestClickThroughController extends BaseAdserverTest {
    private ClickThroughController clickThroughController;
    private TargetingContextFactory targetingContextFactory;
    private PreProcessor preProcessor;
    private ImpressionService impressionService;
    private AdResponseLogic adResponseLogic;
    private ClickUtils clickUtils;
    private AdServerStats astats;
    private BackupLogger backupLogger;

    @Before
    public void initTests() throws IOException {
        clickThroughController = new ClickThroughController();
        targetingContextFactory = mock(TargetingContextFactory.class, "targetingContextFactory");
        preProcessor = mock(PreProcessor.class, "preProcessor");
        impressionService = mock(ImpressionService.class, "impressionService");
        adResponseLogic = mock(AdResponseLogic.class);
        clickUtils = mock(ClickUtils.class);
        backupLogger = mock(BackupLogger.class);
        astats = mock(AdServerStats.class);

        inject(clickThroughController, "targetingContextFactory", targetingContextFactory);
        inject(clickThroughController, "preProcessor", preProcessor);
        inject(clickThroughController, "impressionService", impressionService);
        inject(clickThroughController, "clickUtils", clickUtils);
        inject(clickThroughController, "backupLogger", backupLogger);
        inject(clickThroughController, "astats", astats);

    }

    @Test
    public void testClickThroughController01_handleClickThroughRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String exceptionMessage = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(new InvalidIpAddressException(exceptionMessage)));
                oneOf(backupLogger).logClickFailure(impressionExternalID, exceptionMessage, null);
            }
        });

        clickThroughController.handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, null);
    }

    @Test
    public void testClickThroughController02_handleClickThroughRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final String exceptionMessage = "your phone blocked";
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                will(throwException(new BlacklistedException(exceptionMessage)));
                oneOf(backupLogger).logClickFailure(impressionExternalID, "blacklisted", null, exceptionMessage);
            }
        });

        clickThroughController.handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, null);
    }

    @Test
    public void testClickThroughController03_handleClickThroughRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class);
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(null));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(null));
                allowing(clickUtils).redirectToFallbackUrl(request, response);
                oneOf(astats).increment((AdSpaceDto) null, AsCounter.ClickImpressionNotFound);
                oneOf(backupLogger).logClickFailure(impressionExternalID, "Impression not found", targetingContext);
            }
        });

        clickThroughController.handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, null);
    }

    @Test
    public void testClickThroughController04_handleClickThroughRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Impression impression = mock(Impression.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class); //this has to be null from next;
        final CreativeDto creative = mock(CreativeDto.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();
        final String publicationExternalID = randomAlphaNumericString(10);

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                oneOf(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(impression).getExternalID();
                will(returnValue("hello"));
                oneOf(backupLogger).logClickFailure(impression, "AdSpace mismatch", targetingContext);
                oneOf(astats).click(impression);
                oneOf(astats).increment(adSpace, AsCounter.ClickAdSpaceMismatch);

                allowing(adSpace).getId();
                will(returnValue(20L));
                /*
                allowing(targetingContext).getAdSpace();
                will(returnValue(adSpace));
                
                allowing(adSpace).getPublication();
                will(returnValue(pub));
                oneOf(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getCreativeId();
                will(returnValue(1L));
                oneOf(adserverDomainCache).getCreativeById(1L);
                will(returnValue(creative));
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getAdvertiser();
                will(returnValue(advertiser));
                oneOf(advertiser).getExternalID();
                will(returnValue(advertiserExternalID));
                oneOf(creative).getExternalID();
                will(returnValue(creativeExternalID));
                oneOf(pub).getExternalID();
                will(returnValue(publicationExternalID));

                allowing(adSpace).getPublication();
                will(returnValue(pub));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));
                */
            }
        });

        clickThroughController.handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, null);
    }

    @Test
    public void testClickThroughController05_handleClickThroughRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Impression impression = mock(Impression.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class); //this has to be null from next;
        final long impressionCreativeId = randomLong();
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();
        final String publicationExternalID = randomAlphaNumericString(10);

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
                will(returnValue(20L));
                allowing(impression).getAdSpaceId();
                will(returnValue(20L));
                allowing(impression).getExternalID();
                will(returnValue("hello"));

                allowing(targetingContext).setAdSpace(with(any(AdSpaceDto.class)));

                allowing(adserverDomainCache).getRecentlyStoppedCreativeById(impressionCreativeId);
                will(returnValue(null));
                allowing(clickUtils).redirectToFallbackUrl(request, response);
                allowing(impression).getCreativeId();
                will(returnValue(impressionCreativeId));
                allowing(adserverDomainCache).getCreativeById(impressionCreativeId);
                will(returnValue(null));
                allowing(impression).getExternalID();
                will(returnValue("hello"));
                oneOf(astats).click(impression);
                oneOf(astats).increment(adSpace, AsCounter.ClickCreativeNotFound);

                oneOf(backupLogger).logClickFailure(impression, "Creative not found", targetingContext, String.valueOf(impressionCreativeId));
                allowing(targetingContext).getAdSpace();
                will(returnValue(adSpace));
                allowing(adSpace).getId();
                will(returnValue(20L));
                allowing(adSpace).getPublication();
                will(returnValue(pub));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));

            }
        });

        clickThroughController.handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, null);
    }

    @Test
    public void testClickThroughController06_handleClickThroughRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Impression impression = mock(Impression.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class); //this has to be null from next;
        final long impressionCreativeId = randomLong();
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();
        final String publicationExternalID = randomAlphaNumericString(10);

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
                will(returnValue(null));
                allowing(impression).getAdSpaceId();
                will(returnValue(20L));
                allowing(impression).getExternalID();
                will(returnValue("hello"));

                allowing(targetingContext).setAdSpace(with(any(AdSpaceDto.class)));

                allowing(adserverDomainCache).getRecentlyStoppedCreativeById(impressionCreativeId);
                will(returnValue(null));
                allowing(clickUtils).redirectToFallbackUrl(request, response);
                allowing(impression).getCreativeId();
                will(returnValue(impressionCreativeId));
                allowing(adserverDomainCache).getCreativeById(impressionCreativeId);
                will(returnValue(null));
                allowing(adserverDomainCache).getAdSpaceById(20L);
                will(returnValue(adSpace));
                allowing(impression).getExternalID();
                will(returnValue("hello"));
                oneOf(backupLogger).logClickFailure(impression, "Creative not found", targetingContext, String.valueOf(impressionCreativeId));
                oneOf(astats).click(impression);
                oneOf(astats).increment(adSpace, AsCounter.ClickCreativeNotFound);

                allowing(targetingContext).getAdSpace();
                will(returnValue(adSpace));
                allowing(adSpace).getId();
                will(returnValue(20L));
                allowing(adSpace).getPublication();
                will(returnValue(pub));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));
                allowing(pub).getExternalID();
                will(returnValue(publicationExternalID));
            }
        });

        clickThroughController.handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, null);
    }

    @Test
    public void testClickThroughController07_handleClickThroughRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String impressionExternalID = randomAlphaNumericString(10);
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Impression impression = mock(Impression.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final CreativeDto creative = mock(CreativeDto.class);
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final Boolean isInstallTrackingAdXEnabled = false;
        final DomainCache domainCache = mock(DomainCache.class);

        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(impression).getIntegrationTypeId();
                will(returnValue(2L));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(adserverDomainCache).getAdSpaceById(2L);
                will(returnValue(adSpace));
                allowing(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).isInstallTrackingAdXEnabled();
                will(returnValue(isInstallTrackingAdXEnabled));
                allowing(adSpace).getId();
                will(returnValue(2L));
                allowing(targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));

                oneOf(adserverDomainCache).getCreativeById(2L);
                will(returnValue(null));
                allowing(adserverDomainCache).getRecentlyStoppedCreativeById(2L);
                will(returnValue(creative));
                allowing(impression).getCreativeId();
                will(returnValue(2L));
                allowing(creative).getId();
                will(returnValue(2L));

                allowing(impression).getExternalID();
                will(returnValue("hello"));

                oneOf(clickUtils).getTargetUrl(impression, creative);
                will(returnValue(null));
                oneOf(astats).click(impression);

                oneOf(clickUtils).redirectToFallbackUrl(request, response);
                oneOf(backupLogger).logClickFailure(impression, "no target URL", targetingContext);
                //2nd test case
                oneOf(adserverDomainCache).getCreativeById(2L);
                will(returnValue(creative));
                oneOf(clickUtils).getTargetUrl(impression, creative);
                will(returnValue("tel:something"));
                oneOf(campaign).isConversionTrackingEnabled();
                will(returnValue(true));
                oneOf(clickUtils).setClickIdCookie(response, impression, creative);
                //oneOf(adResponseLogic).postProcessVariables("tel:something", adSpace, creative, impression, targetingContext, null, true, null);
                //will(returnValue("tel://testingURL"));
                oneOf(clickUtils).trackClick(adSpace, creative, impression, targetingContext, null);
                oneOf(astats).click(impression);
                oneOf(astats).clickCompleted(2L, 2L);

                allowing(targetingContext).getAdSpace();
                will(returnValue(adSpace));
                allowing(adSpace).getId();
                will(returnValue(20L));
                allowing(adSpace).getPublication();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getCreativeId();
                will(returnValue(1L));
                allowing(adserverDomainCache).getCreativeById(2L);
                will(returnValue(creative));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).getExternalID();
                will(returnValue(advertiserExternalID));
                allowing(creative).getExternalID();
                will(returnValue(creativeExternalID));

                allowing(clickUtils).processRedirectUrl("tel:something", false, adSpace, creative, impression, targetingContext, true);
                will(returnValue("tel://testingURL"));
            }
        });

        clickThroughController.handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, null);
        clickThroughController.handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, null);
        assertTrue(response.getRedirectedUrl().equals("tel://testingURL"));
    }

    @Test
    public void testClickThroughController08_handleClickThroughRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Impression impression = mock(Impression.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final CreativeDto creative = mock(CreativeDto.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final DomainCache domainCache = mock(DomainCache.class);
        final AdSpaceDto nullAdSpaceDto = null;

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(impression).getIntegrationTypeId();
                will(returnValue(2L));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(adserverDomainCache).getAdSpaceById(2L);
                will(returnValue(null));
                allowing(adserverDomainCache).getAdSpaceByExternalID(null);
                will(returnValue(null));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(null));
                allowing(targetingContext).setAdSpace(with(nullAdSpaceDto));

                oneOf(adserverDomainCache).getCreativeById(2L);
                will(returnValue(null));
                allowing(adserverDomainCache).getRecentlyStoppedCreativeById(2L);
                will(returnValue(creative));
                allowing(impression).getCreativeId();
                will(returnValue(2L));
                allowing(creative).getId();
                will(returnValue(2L));
                allowing(impression).getExternalID();
                will(returnValue("hello"));
                allowing(creative).getCampaign();
                will(returnValue(campaign));

                oneOf(clickUtils).getTargetUrl(impression, creative);
                will(returnValue("something"));
                oneOf(campaign).isConversionTrackingEnabled();
                will(returnValue(false)); // for code coverage
                oneOf(clickUtils).trackClick(null, creative, impression, targetingContext, null);
                oneOf(clickUtils).processRedirectUrl("something", false, null, creative, impression, targetingContext, true);
                will(returnValue("http://testingURL"));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(adserverDomainCache).getCreativeById(2L);
                will(returnValue(creative));
                oneOf(astats).click(impression);
                oneOf(astats).increment(AsCounter.ClickAdSpaceNotFound);
                oneOf(astats).clickCompleted(2L, 2L);

            }
        });

        clickThroughController.handleClickThroughRequest(request, response, null, impressionExternalID, null);
        assertTrue(response.getRedirectedUrl().equals("http://testingURL"));
    }

    @Test
    public void testClickThroughController09_handleClickThroughRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String impressionExternalID = randomAlphaNumericString(10);
        final String adspaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");

        final String adxPrefix = "https://ad-x.com/";
        final String dpid = randomAlphaNumericString(10);
        final Impression impression = mock(Impression.class);
        final String impressionExternalId = randomAlphaNumericString(10);
        final CreativeDto creative = mock(CreativeDto.class);
        final String creativeExternalId = randomAlphaNumericString(10);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final String advertiserExternalId = randomAlphaNumericString(10);
        final String expectedUrl = adxPrefix + "click/adfonic/" + advertiserExternalId + "/" + creativeExternalId + "/" + impressionExternalId + "?dpid=" + dpid;
        final DomainCache domainCache = mock(DomainCache.class);
        final AdSpaceDto nullAdSpaceDto = null;

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(impression).getIntegrationTypeId();
                will(returnValue(2L));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(adserverDomainCache).getAdSpaceById(2L);
                will(returnValue(null));
                allowing(adserverDomainCache).getAdSpaceByExternalID(adspaceExternalID);
                will(returnValue(null));
                allowing(impression).getAdSpaceId();
                will(returnValue(null));
                allowing(targetingContext).setAdSpace(with(nullAdSpaceDto));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(domainCache).getDeviceIdentifierTypeIdsBySystemName();
                will(returnValue(Collections.emptyMap()));
                allowing(targetingContext).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(Collections.emptyMap()));
                allowing(targetingContext).isFlagTrue(TargetingContext.TRACKING_DISABLED);
                will(returnValue(false));

                oneOf(adserverDomainCache).getCreativeById(2L);
                will(returnValue(null));
                allowing(adserverDomainCache).getRecentlyStoppedCreativeById(2L);
                will(returnValue(creative));
                allowing(impression).getCreativeId();
                will(returnValue(2L));
                allowing(creative).getId();
                will(returnValue(2L));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getExternalID();
                will(returnValue("x-x-x-x"));
                oneOf(clickUtils).getTargetUrl(impression, creative);
                will(returnValue("adxUrl"));

                oneOf(campaign).isConversionTrackingEnabled();
                will(returnValue(true));
                oneOf(clickUtils).setClickIdCookie(response, impression, creative);

                oneOf(clickUtils).processRedirectUrl("adxUrl", false, null, creative, impression, targetingContext, true);
                will(returnValue(expectedUrl));

                oneOf(clickUtils).trackClick(null, creative, impression, targetingContext, null);
                oneOf(astats).click(impression);
                oneOf(astats).increment(AsCounter.ClickAdSpaceNotFound);
                oneOf(astats).clickCompleted(2L, 2L);

                allowing(impression).getTrackingIdentifier();
                will(returnValue(dpid));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalId));
                allowing(creative).getExternalID();
                will(returnValue(creativeExternalId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).getExternalID();
                will(returnValue(advertiserExternalId));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(adserverDomainCache).getCreativeById(2L);
                will(returnValue(creative));

                allowing(clickUtils).processRedirectUrl("adxUrl", false, null, creative, impression, targetingContext, true);
                will(returnValue("http://adxUrl"));

            }
        });
        clickThroughController.handleClickThroughRequest(request, response, adspaceExternalID, impressionExternalID, null);
        assertEquals(expectedUrl, response.getRedirectedUrl());
    }
}

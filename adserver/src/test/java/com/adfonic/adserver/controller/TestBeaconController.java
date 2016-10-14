package com.adfonic.adserver.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.Feature;
import com.adfonic.domain.RtbConfig.RtbWinNoticeMode;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class TestBeaconController extends BaseAdserverTest {

    private BeaconController beaconController;
    private TargetingContextFactory targetingContextFactory;
    private PreProcessor preProcessor;
    private ImpressionService impressionService;
    private AdEventFactory adEventFactory;
    private byte[] gifContent;
    private AdServerStats astats;
    private BackupLogger backupLogger;

    @Before
    public void initTests() throws IOException {
        beaconController = new BeaconController();
        targetingContextFactory = mock(TargetingContextFactory.class, "targetingContextFactory");
        preProcessor = mock(PreProcessor.class, "preProcessor");
        impressionService = mock(ImpressionService.class, "impressionService");
        adEventFactory = mock(AdEventFactory.class);
        String imgageByte = new String("anything is byte dude");
        gifContent = imgageByte.getBytes();
        backupLogger = mock(BackupLogger.class);
        astats = mock(AdServerStats.class);

        inject(beaconController, "targetingContextFactory", targetingContextFactory);
        inject(beaconController, "preProcessor", preProcessor);
        inject(beaconController, "impressionService", impressionService);
        inject(beaconController, "adEventFactory", adEventFactory);
        inject(beaconController, "backupLogger", backupLogger);
        inject(beaconController, "astats", astats);
        beaconController.pixelBytes = gifContent;
    }

    @Test
    public void testbeaconController01_handleBeacon() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final InvalidIpAddressException e1 = new InvalidIpAddressException("InvalidIP");
        final Exception e2 = new IllegalStateException(randomAlphaNumericString(10));
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(e1));
                oneOf(backupLogger).logBeaconFailure(impressionExternalID, e1.getMessage(), null);
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(e2));
                oneOf(backupLogger).logBeaconFailure(impressionExternalID, "exception", null, e2.getClass().getName(), e2.getMessage());
            }
        });

        beaconController.handleBeacon(request, response, adSpaceExternalID, impressionExternalID, null);
        beaconController.handleBeacon(request, response, adSpaceExternalID, impressionExternalID, null);
    }

    @Test
    public void testBeaconController06_handleBeacon() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");
        final Impression impression = mock(Impression.class);
        final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);//this has to be null from next;
        final Set<Feature> featureSet = new HashSet<Feature>();
        featureSet.add(Feature.BEACON);
        final long adSpaceId = randomLong();
        final long impressionModelId = randomLong();
        final long impressionCreativeId = randomLong();
        final CreativeDto creative = mock(CreativeDto.class);
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
                oneOf(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                oneOf(impression).getIntegrationTypeId();
                will(returnValue(2L));
                oneOf(domainCache).getIntegrationTypeById(2L);
                will(returnValue(integrationType));
                oneOf(integrationType).getSupportedFeatures();
                will(returnValue(featureSet));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(impression).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(targetingContext).setAdSpace(adSpace);
                oneOf(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(null));//again cases
                allowing(impression).getModelId();
                will(returnValue(impressionModelId));
                oneOf(targetingContext).getEffectiveUserAgent();
                will(returnValue(null));
                allowing(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(null));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(backupLogger).logBeaconFailure(impression, "Model mismatch", targetingContext, String.valueOf(impressionModelId), "null");
                oneOf(astats).increment(adSpace, AsCounter.BeaconDeviceModelMismatch);

                allowing(impressionService).trackBeacon(with(any(Impression.class)));
                will(returnValue(true));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                oneOf(astats).beacon(impression);

                allowing(impression).getCreativeId();
                will(returnValue(impressionCreativeId));

                allowing(adserverDomainCache).isRtbEnabled();
                will(returnValue(false));
                oneOf(adserverDomainCache).getCreativeById(impressionCreativeId);
                will(returnValue(creative));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getCreativeId();
                will(returnValue(impressionCreativeId));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));

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

        beaconController.handleBeacon(request, response, adSpaceExternalID, impressionExternalID, null);
    }

    public void testBeaconController16_handleBeacon() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");
        final Impression impression = mock(Impression.class);
        final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);//this has to be null from next;
        final Set<Feature> featureSet = new HashSet<Feature>();
        final ModelDto model = mock(ModelDto.class);
        final CountryDto country = mock(CountryDto.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final AdEvent adEvent = mock(AdEvent.class);
        featureSet.add(Feature.BEACON);
        final Date eventTime = new Date();
        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(impression).getIntegrationTypeId();
                will(returnValue(2L));
                allowing(domainCache).getIntegrationTypeById(2L);
                will(returnValue(integrationType));
                allowing(integrationType).getSupportedFeatures();
                will(returnValue(featureSet));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(adserverDomainCache).getAdSpaceById(2L);
                will(returnValue(adSpace));

                allowing(impression).getAdSpaceId();
                will(returnValue(20L));
                allowing(targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
                allowing(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));//again cases
                allowing(impression).getModelId();
                will(returnValue(null));
                allowing(model).getId();
                will(returnValue(2L));
                allowing(impression).getCountryId();
                will(returnValue(null));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                allowing(country).getId();
                will(returnValue(002L));
                allowing(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(null));
                oneOf(impressionService).trackBeacon(with(any(Impression.class)));
                will(returnValue(true));
                allowing(impression).getCreativeId();
                will(returnValue(123L));
                oneOf(adserverDomainCache).getCreativeById(123L);
                will(returnValue(creative));
                allowing(adserverDomainCache).isRtbEnabled();
                will(returnValue(false));
                oneOf(adEventFactory).newInstance(AdAction.IMPRESSION);
                will(returnValue(adEvent));
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logBeaconSuccess(impression, eventTime, targetingContext);
            }
        });

        beaconController.handleBeacon(request, response, null, impressionExternalID, null);
    }

    public void testBeaconController17_handleBeacon() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");
        final Impression impression = mock(Impression.class);
        final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);//this has to be null from next;
        final Set<Feature> featureSet = new HashSet<Feature>();
        final ModelDto model = mock(ModelDto.class);
        final CountryDto country = mock(CountryDto.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final AdEvent adEvent = mock(AdEvent.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        featureSet.add(Feature.BEACON);
        final Date eventTime = new Date();
        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(impression).getIntegrationTypeId();
                will(returnValue(2L));
                allowing(domainCache).getIntegrationTypeById(2L);
                will(returnValue(integrationType));
                allowing(integrationType).getSupportedFeatures();
                will(returnValue(featureSet));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(adserverDomainCache).getAdSpaceById(2L);
                will(returnValue(adSpace));

                allowing(impression).getAdSpaceId();
                will(returnValue(20L));
                allowing(targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
                allowing(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));//again cases
                allowing(impression).getModelId();
                will(returnValue(null));
                allowing(model).getId();
                will(returnValue(2L));
                allowing(impression).getCountryId();
                will(returnValue(null));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                allowing(country).getId();
                will(returnValue(002L));
                allowing(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(null));
                oneOf(impressionService).trackBeacon(with(any(Impression.class)));
                will(returnValue(true));
                allowing(impression).getCreativeId();
                will(returnValue(123L));
                oneOf(adserverDomainCache).getCreativeById(123L);
                will(returnValue(creative));
                allowing(adserverDomainCache).isRtbEnabled();
                will(returnValue(true));

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(null));
                oneOf(adEventFactory).newInstance(AdAction.IMPRESSION);
                will(returnValue(adEvent));
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logBeaconSuccess(impression, eventTime, targetingContext);
            }
        });
        beaconController.handleBeacon(request, response, null, impressionExternalID, null);
    }

    public void testBeaconController18_handleBeacon() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");
        final Impression impression = mock(Impression.class);
        final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);//this has to be null from next;
        final Set<Feature> featureSet = new HashSet<Feature>();
        final ModelDto model = mock(ModelDto.class);
        final CountryDto country = mock(CountryDto.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final AdEvent adEvent = mock(AdEvent.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class);
        featureSet.add(Feature.BEACON);
        final Date eventTime = new Date();
        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(impression).getIntegrationTypeId();
                will(returnValue(2L));
                allowing(domainCache).getIntegrationTypeById(2L);
                will(returnValue(integrationType));
                allowing(integrationType).getSupportedFeatures();
                will(returnValue(featureSet));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(adserverDomainCache).getAdSpaceById(2L);
                will(returnValue(adSpace));
                allowing(impression).getAdSpaceId();
                will(returnValue(20L));
                allowing(targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
                allowing(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));//again cases
                allowing(impression).getModelId();
                will(returnValue(null));
                allowing(model).getId();
                will(returnValue(2L));
                allowing(impression).getCountryId();
                will(returnValue(null));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                allowing(country).getId();
                will(returnValue(002L));
                allowing(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(null));
                oneOf(impressionService).trackBeacon(with(any(Impression.class)));
                will(returnValue(true));
                allowing(impression).getCreativeId();
                will(returnValue(123L));
                oneOf(adserverDomainCache).getCreativeById(123L);
                will(returnValue(creative));
                allowing(adserverDomainCache).isRtbEnabled();
                will(returnValue(true));

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(rtbConfig).getWinNoticeMode();
                will(returnValue(RtbWinNoticeMode.OPEN_RTB));
                oneOf(adEventFactory).newInstance(AdAction.IMPRESSION);
                will(returnValue(adEvent));
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logBeaconSuccess(impression, eventTime, targetingContext);
            }
        });

        beaconController.handleBeacon(request, response, null, impressionExternalID, null);
    }

    public void testBeaconController19_handleBeacon() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");
        final Impression impression = mock(Impression.class);
        final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);//this has to be null from next;
        final Set<Feature> featureSet = new HashSet<Feature>();
        final ModelDto model = mock(ModelDto.class);
        final CountryDto country = mock(CountryDto.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final AdEvent adEvent = mock(AdEvent.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class);
        featureSet.add(Feature.BEACON);
        final Date eventTime = new Date();
        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(impression).getIntegrationTypeId();
                will(returnValue(2L));
                allowing(domainCache).getIntegrationTypeById(2L);
                will(returnValue(integrationType));
                allowing(integrationType).getSupportedFeatures();
                will(returnValue(featureSet));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(adserverDomainCache).getAdSpaceById(2L);
                will(returnValue(adSpace));
                allowing(impression).getAdSpaceId();
                will(returnValue(20L));
                allowing(targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
                allowing(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));//again cases
                allowing(impression).getModelId();
                will(returnValue(null));
                allowing(model).getId();
                will(returnValue(2L));
                allowing(impression).getCountryId();
                will(returnValue(null));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                allowing(country).getId();
                will(returnValue(002L));
                allowing(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(null));
                oneOf(impressionService).trackBeacon(with(any(Impression.class)));
                will(returnValue(true));
                allowing(impression).getCreativeId();
                will(returnValue(123L));
                oneOf(adserverDomainCache).getCreativeById(123L);
                will(returnValue(creative));
                allowing(adserverDomainCache).isRtbEnabled();
                will(returnValue(true));

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(rtbConfig).getWinNoticeMode();
                will(returnValue(RtbWinNoticeMode.BEACON));
                allowing(adEventFactory).newInstance(with(any(AdAction.class)));
                will(returnValue(adEvent));
                allowing(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logRtbWinSuccess(impression, null, eventTime, targetingContext);
                oneOf(backupLogger).logBeaconSuccess(impression, eventTime, targetingContext);
            }
        });
        beaconController.handleBeacon(request, response, null, impressionExternalID, null);
    }

    public void testBeaconController20_handleBeacon_AF_1342_rtbSettlementPrice() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");
        final Impression impression = mock(Impression.class);
        final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);//this has to be null from next;
        final Set<Feature> featureSet = new HashSet<Feature>();
        final ModelDto model = mock(ModelDto.class);
        final CountryDto country = mock(CountryDto.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final AdEvent adEvent = mock(AdEvent.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class);
        final BigDecimal rtbSettlementPrice = new BigDecimal("0.123");
        featureSet.add(Feature.BEACON);
        final Date eventTime = new Date();
        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(impression).getIntegrationTypeId();
                will(returnValue(2L));
                allowing(domainCache).getIntegrationTypeById(2L);
                will(returnValue(integrationType));
                allowing(integrationType).getSupportedFeatures();
                will(returnValue(featureSet));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(adserverDomainCache).getAdSpaceById(2L);
                will(returnValue(adSpace));
                allowing(impression).getAdSpaceId();
                will(returnValue(20L));
                allowing(targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
                allowing(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));//again cases
                allowing(impression).getModelId();
                will(returnValue(null));
                allowing(model).getId();
                will(returnValue(2L));
                allowing(impression).getCountryId();
                will(returnValue(null));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                allowing(country).getId();
                will(returnValue(002L));
                allowing(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(null));

                // Expect that Impression.rtbSettlementPrice gets set
                oneOf(impression).setRtbSettlementPrice(rtbSettlementPrice);

                oneOf(impressionService).trackBeacon(with(any(Impression.class)));
                will(returnValue(true));
                allowing(impression).getCreativeId();
                will(returnValue(123L));
                oneOf(adserverDomainCache).getCreativeById(123L);
                will(returnValue(creative));
                allowing(adserverDomainCache).isRtbEnabled();
                will(returnValue(true));

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(rtbConfig).getWinNoticeMode();
                will(returnValue(RtbWinNoticeMode.BEACON));
                allowing(adEventFactory).newInstance(with(any(AdAction.class)));
                will(returnValue(adEvent));
                allowing(adEvent).getEventTime();
                will(returnValue(eventTime));
                allowing(impression).getRtbBidPrice();
                will(returnValue(rtbSettlementPrice));
                oneOf(backupLogger).logRtbWinSuccess(impression, rtbSettlementPrice, eventTime, targetingContext);
                oneOf(backupLogger).logBeaconSuccess(impression, eventTime, targetingContext);
            }
        });
        beaconController.handleBeacon(request, response, null, impressionExternalID, rtbSettlementPrice.toString());
    }

    public void openXEncryptedPrice() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String impressionExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final DomainCache domainCache = mock(DomainCache.class, "domainCache");
        final Impression impression = mock(Impression.class);
        final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);//this has to be null from next;
        final Set<Feature> featureSet = new HashSet<Feature>();
        final ModelDto model = mock(ModelDto.class);
        final CountryDto country = mock(CountryDto.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final AdEvent adEvent = mock(AdEvent.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class);
        final BigDecimal rtbSettlementPrice = new BigDecimal("0.123");
        featureSet.add(Feature.BEACON);
        final Date eventTime = new Date();
        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(impressionService).getImpression(impressionExternalID);
                will(returnValue(impression));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(impression).getIntegrationTypeId();
                will(returnValue(2L));
                allowing(domainCache).getIntegrationTypeById(2L);
                will(returnValue(integrationType));
                allowing(integrationType).getSupportedFeatures();
                will(returnValue(featureSet));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(impression).getAdSpaceId();
                will(returnValue(2L));
                allowing(adserverDomainCache).getAdSpaceById(2L);
                will(returnValue(adSpace));
                allowing(impression).getAdSpaceId();
                will(returnValue(20L));
                allowing(targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
                allowing(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));//again cases
                allowing(impression).getModelId();
                will(returnValue(null));
                allowing(model).getId();
                will(returnValue(2L));
                allowing(impression).getCountryId();
                will(returnValue(null));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                allowing(country).getId();
                will(returnValue(002L));
                allowing(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(null));

                // Expect that Impression.rtbSettlementPrice gets set
                oneOf(impression).setRtbSettlementPrice(rtbSettlementPrice);

                oneOf(impressionService).trackBeacon(with(any(Impression.class)));
                will(returnValue(true));
                allowing(impression).getCreativeId();
                will(returnValue(123L));
                oneOf(adserverDomainCache).getCreativeById(123L);
                will(returnValue(creative));
                allowing(adserverDomainCache).isRtbEnabled();
                will(returnValue(true));

                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                oneOf(rtbConfig).getWinNoticeMode();
                will(returnValue(RtbWinNoticeMode.BEACON));
                allowing(adEventFactory).newInstance(with(any(AdAction.class)));
                will(returnValue(adEvent));
                allowing(adEvent).getEventTime();
                will(returnValue(eventTime));
                allowing(impression).getRtbBidPrice();
                will(returnValue(rtbSettlementPrice));
                oneOf(backupLogger).logRtbWinSuccess(impression, rtbSettlementPrice, eventTime, targetingContext);
                oneOf(backupLogger).logBeaconSuccess(impression, eventTime, targetingContext);
            }
        });
        beaconController.handleBeacon(request, response, null, impressionExternalID, null);
    }
}

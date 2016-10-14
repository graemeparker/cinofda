package com.adfonic.adserver.controller.rtb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.jms.Queue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.adserver.controller.dbg.DebugBidController.DebugRtbBidEventListener;
import com.adfonic.adserver.controller.dbg.DebugBidController.DebugTargetingEventListener;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto;
import com.adfonic.adserver.controller.rtb.AbstractBidTest.LogCapturingHandler;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.impl.TargetingContextImpl;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidManager;
import com.adfonic.adserver.rtb.RtbIdService;
import com.adfonic.adserver.rtb.impl.BidRateThrottler;
import com.adfonic.adserver.rtb.impl.RtbBidLogicImpl;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.PendingAdType;
import com.adfonic.domain.Publication;
import com.adfonic.domain.RtbConfig.AdmProfile;
import com.adfonic.domain.RtbConfig.RtbAdMode;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.RtbConfig.RtbWinNoticeMode;
import com.adfonic.domain.UnfilledAction;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.geo.PostalCodeIdManager;
import com.adfonic.jms.JmsUtils;
import com.adfonic.util.stats.CounterManager;
import com.google.common.collect.ImmutableMap;

/**
 * 
 * @author mvanek
 *
 */
public class TestCornersOfRtbLogic {

    @Mock
    private JmsTemplate centralJmsTemplate;
    @Mock
    private Queue rtbPublicationPersistenceQueue;
    @Mock
    private Queue rtbAdSpaceFormatQueue;
    @Mock
    private JmsUtils jmsUtils;
    @Mock
    private Map<String, MarkupGenerator> markupGenMap;
    @Mock
    private RtbBidManager rtbBidManager;
    @Mock
    private RtbIdService rtbIdService;
    @Mock
    private ImpressionService impressionService;
    @Mock
    private AdEventFactory adEventFactory;
    @Mock
    private BackupLogger backupLogger;
    @Mock
    private AdResponseLogic adResponseLogic;
    @Mock
    private DisplayTypeUtils displayTypeUtils;
    @Mock
    private PreProcessor preProcessor;
    @Mock
    private TargetingContextFactory targetingContextFactory;
    @Mock
    private TargetingEngine targetingEngine;
    @Mock
    private TrackingIdentifierLogic trackingIdentifierLogic;
    @Mock
    private VhostManager vhostManager;
    @Mock
    private CounterManager counterManager;
    @Mock
    private AdserverDataCacheManager adserverDataCacheManager;
    @Mock
    private BidRateThrottler throttler;

    @InjectMocks
    private RtbBidLogicImpl rtbLogic;

    // TargetingContextImpl dependencies

    @Mock
    private DeriverManager deriverManager;
    @Mock
    private PostalCodeIdManager postalManager;
    @Mock
    private DomainCache domainCache;
    @Mock
    private AdserverDomainCache adserverCacheMock;
    @Mock
    private HttpServletRequest httpRequestMock;
    @Mock
    private HttpServletResponse httpResponseMock;

    private TargetingContextImpl targetingContext;

    private static final String EXISTING_PUBLISHER_EXTID = "valid-publisher-extid-" + System.currentTimeMillis();
    private static final Long EXISTING_PUBLISHER_ID = 320200l;

    private static final String EXISTING_PUBLICATION_EXTID = "valid-publication-extid-" + System.currentTimeMillis();
    private static final Long EXISTING_PUBLICATION_ID = 640480l;

    private static final String EXISTING_ADSPACE_EXTID = "valid-adspace-extid-" + System.currentTimeMillis();
    private static final Long EXISTING_ADSPACE_ID = 800600l;

    private static final String EXISTING_RTB_ID = "valid-rtb-id-" + System.currentTimeMillis();

    private static final Long INTEGRATION_TYPE_ID = 100l;
    private static final Long PUBLICATION_TYPE_ID = 100l;
    private static final Long CATEGORY_ID = 1l;

    private List<String> messages;
    private DbgBidDto dbgBidDto;
    private DebugRtbBidEventListener bidListener;
    private DebugTargetingEventListener targetListener;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        //Mockito.when(adserverDataCacheManager.getCache()).thenReturn(adserverCache);
        targetingContext = new TargetingContextImpl(domainCache, adserverCacheMock, deriverManager, postalManager);
        Mockito.when(targetingContextFactory.createTargetingContext()).thenReturn(targetingContext);
        Mockito.when(adserverCacheMock.isRtbEnabled()).thenReturn(true);

        messages = new ArrayList<String>();
        dbgBidDto = new DbgBidDto(RtbExchange.Mopub);
        bidListener = new DebugRtbBidEventListener(dbgBidDto, messages);
        targetListener = new DebugTargetingEventListener(dbgBidDto, messages);

        AdSpaceDto adSpaceDto = getAdSpaceDto();

        Mockito.when(adserverCacheMock.getAdSpaceByPublicationRtbId(EXISTING_PUBLISHER_ID, EXISTING_RTB_ID)).thenReturn(adSpaceDto);
        Mockito.when(adserverCacheMock.getAdSpaceById(EXISTING_ADSPACE_ID)).thenReturn(adSpaceDto);
        Mockito.when(adserverCacheMock.getAdSpaceByExternalID(EXISTING_ADSPACE_EXTID)).thenReturn(adSpaceDto);
        Mockito.when(adserverCacheMock.getAdSpaceByExternalID(EXISTING_ADSPACE_EXTID)).thenReturn(adSpaceDto);
        Mockito.when(adserverCacheMock.getAllAdSpaces()).thenReturn(new AdSpaceDto[] { adSpaceDto });

        Mockito.when(adserverCacheMock.getPublisherRtbAdSpacesMap(EXISTING_PUBLISHER_ID)).thenReturn(ImmutableMap.of(EXISTING_RTB_ID, adSpaceDto));
        Mockito.when(adserverCacheMock.getPublisherIdByExternalID(EXISTING_PUBLISHER_EXTID)).thenReturn(EXISTING_PUBLISHER_ID);

        LogCapturingHandler.install(true);
    }

    @After
    public void after() {
        targetingContext = null;
        LogCapturingHandler.remove();
    }

    @Test
    public void noBidOnPublisherExternalIdNotFound() throws NoBidException {
        String publisherExtId = "publ-extid-notin-cache-7890";

        Mockito.when(adserverCacheMock.getPublisherIdByExternalID(publisherExtId)).thenReturn(null);

        ByydRequest byydRequest = new ByydRequest(publisherExtId, "byyd-req-" + System.currentTimeMillis());

        // When 
        try {
            RtbHttpContext httpContext = new RtbHttpContext(RtbEndpoint.ORTBv1, publisherExtId, httpRequestMock, httpResponseMock, null);
            RtbExecutionContext rtbContext = new RtbExecutionContext(httpContext, false);
            rtbContext.setByydRequest(byydRequest);
            rtbLogic.bid(rtbContext, null, null);
            Assert.fail("NoBidException expected");
        } catch (NoBidException nbx) {
            Assertions.assertThat(nbx.getByydRequest().getId()).isEqualTo(byydRequest.getId());
            Assertions.assertThat(nbx.getNoBidReason()).isEqualTo(NoBidReason.REQUEST_INVALID);
            Assertions.assertThat(nbx.getOffenceName()).isEqualTo(AdSrvCounter.UNKNOWN_PUBLISHER.name());
            Assertions.assertThat(nbx.getOffenceValue()).isEqualTo(publisherExtId);
        }

        Assertions.assertThat(LogCapturingHandler.get().list()).hasSize(1);
        LogRecord logRecord = LogCapturingHandler.get().last();
        Assertions.assertThat(logRecord.getLevel()).isEqualTo(Level.WARNING);
        Assertions.assertThat(logRecord.getMessage()).endsWith("Publisher not found: " + publisherExtId);
    }

    @Test
    public void noBidOnPublicationRtbIdNotFound() throws NoBidException {

        ByydRequest byydRequest = new ByydRequest("pub-ext-id-zx-zx-zx-zx", "byyd-req-" + System.currentTimeMillis());
        byydRequest.setPublicationRtbId("rtb-id-" + System.currentTimeMillis());

        try {
            RtbHttpContext httpContext = new RtbHttpContext(RtbEndpoint.ORTBv1, EXISTING_PUBLISHER_EXTID, httpRequestMock, httpResponseMock, null);
            RtbExecutionContext rtbContext = new RtbExecutionContext(httpContext, false);
            rtbContext.setByydRequest(byydRequest);
            rtbLogic.bid(rtbContext, null, null);
            Assert.fail("NoBidException expected");
        } catch (NoBidException nbx) {
            Assertions.assertThat(nbx.getByydRequest().getId()).isEqualTo(byydRequest.getId());
            Assertions.assertThat(nbx.getNoBidReason()).isEqualTo(NoBidReason.REQUEST_DROPPED);
            Assertions.assertThat(nbx.getOffenceName()).isEqualTo(AdSrvCounter.UNKNOWN_PUBLICATION.name());
            Assertions.assertThat(nbx.getOffenceValue()).isEqualTo(byydRequest.getPublicationRtbId());
        }
        /*
        Assertions.assertThat(LogCapturingHandler.get().list()).hasSize(0);
        LogRecord logRecord = LogCapturingHandler.get().last();
        Assertions.assertThat(logRecord.getLevel()).isEqualTo(Level.INFO);
        System.out.println(LogCapturingHandler.get().first().getMessage());
        Assertions.assertThat(LogCapturingHandler.get().list()).isEmpty();
        */
    }

    //@Test
    public void test() throws NoBidException {

        ByydRequest byydRequest = new ByydRequest("pub-ext-id-zx-zx-zx-zx", "byyd-req-" + System.currentTimeMillis());
        ByydDevice device = new ByydDevice();
        device.setUserAgent("Mozilla/5.0 (iPad; CPU OS 8_1 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12B410 Safari/600.1.4");
        device.setIp("95.172.8.2");
        byydRequest.setDevice(device);
        byydRequest.setPublicationRtbId(EXISTING_RTB_ID);

        RtbHttpContext httpContext = new RtbHttpContext(RtbEndpoint.ORTBv1, EXISTING_PUBLISHER_EXTID, httpRequestMock, httpResponseMock, null);
        RtbExecutionContext rtbContext = new RtbExecutionContext(httpContext, false);
        rtbContext.setByydRequest(byydRequest);
        rtbLogic.bid(rtbContext, null, null);
    }

    protected static AdSpaceDto getAdSpaceDto() {
        CompanyDto companyDto = new CompanyDto();

        //SELECT * FROM adfonic.RTB_CONFIG ORDER BY ID DESC;
        RtbConfigDto rtbConfigDto = new RtbConfigDto();
        rtbConfigDto.setAdMode(RtbAdMode.BID);
        rtbConfigDto.setAuctionType(RtbAuctionType.SECOND_PRICE);
        rtbConfigDto.setWinNoticeMode(RtbWinNoticeMode.BEACON);
        rtbConfigDto.setAdmProfile(AdmProfile.STANDARD);
        rtbConfigDto.setBidCurrency("USD");
        rtbConfigDto.setSpMacro("${AUCTION_PRICE}");
        rtbConfigDto.setRtbLostTimeDuration(60);

        PublisherDto publisherDto = new PublisherDto();
        publisherDto.setId(EXISTING_PUBLISHER_ID);
        publisherDto.setExternalId(EXISTING_PUBLISHER_EXTID);
        publisherDto.setDefaultAdRequestTimeout(5000l);
        //publisherDto.setCurrentRevShare(0);
        //publisherDto.setOperatingPublisherId(null);
        //publisherDto.setBuyerPremium(0);
        //publisherDto.setRequiresRealDestination();
        publisherDto.setEcpmTargetRateCard(null);
        publisherDto.setCompany(companyDto);
        publisherDto.setPendingAdType(PendingAdType.NO_AD);
        publisherDto.setRtbConfig(rtbConfigDto);

        PublicationDto publicationDto = new PublicationDto();
        publicationDto.setId(EXISTING_PUBLICATION_ID);
        publicationDto.setExternalID(EXISTING_PUBLICATION_EXTID);
        publicationDto.setRtbId(EXISTING_RTB_ID);
        publicationDto.setName("UTest Publication " + EXISTING_PUBLICATION_ID);
        publicationDto.setStatus(Publication.Status.ACTIVE);
        publicationDto.setAdRequestTimeout(5000l);
        publicationDto.setApproveDate(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        publicationDto.setDefaultIntegrationTypeId(INTEGRATION_TYPE_ID);
        publicationDto.setPublicationTypeId(PUBLICATION_TYPE_ID);
        publicationDto.setCategoryId(CATEGORY_ID);
        publicationDto.setSamplingRate(3);
        publicationDto.setEcpmTargetRateCard(null);
        publicationDto.setTransparentNetwork(null);
        publicationDto.setInstallTrackingDisabled(false);
        publicationDto.setUseSoftFloor(false);

        publicationDto.setPublisher(publisherDto);

        AdSpaceDto adSpaceDto = new AdSpaceDto();
        adSpaceDto.setExternalID(EXISTING_ADSPACE_EXTID);
        adSpaceDto.setId(EXISTING_ADSPACE_ID);
        adSpaceDto.setName("UTest AdSpace " + EXISTING_ADSPACE_ID);
        adSpaceDto.setStatus(AdSpace.Status.VERIFIED);
        adSpaceDto.setColorScheme(AdSpace.ColorScheme.grey);
        adSpaceDto.setUnfilledAction(UnfilledAction.NO_AD);
        adSpaceDto.setPublication(publicationDto);
        return adSpaceDto;
    }
}

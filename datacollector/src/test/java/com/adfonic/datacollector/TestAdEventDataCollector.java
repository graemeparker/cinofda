package com.adfonic.datacollector;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.jms.Queue;

import net.byyd.archive.model.v1.V1DomainModelMapper;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.Click;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.datacollector.dao.ClusterDao;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.DataCollectorDomainCache;
import com.adfonic.domain.cache.DataCollectorDomainCacheManager;
import com.adfonic.domain.cache.dto.datacollector.campaign.AdvertiserDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignBidDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublicationDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherRevShareDto;
import com.adfonic.jms.ClickMessage;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.UserAgentUpdatedMessage;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.tracker.ClickService;
import com.adfonic.util.TimeZoneUtils;
import com.adfonic.util.stats.CounterManager;

public class TestAdEventDataCollector extends AbstractAdfonicTest {
    private static final String FAILED_LOG_FILE = "TestAdEventDataCollector-failed-failed-ad-events-%d{yyyy-MM-dd}.log";
    private static final int CLICK_TTL = 7200;
    private static final int INSTALL_TTL = 2592000;
    private static final int CONVERSION_TTL = 2592000;
    private static final int USER_AGENT_ID_CACHE_MAX_SIZE = 100000;

    private AdEventDataCollector adEventDataCollector;
    private ClusterDao clusterDao;
    private Ehcache missingCampaignsCache;
    private BatchManager batchManager;
    private DataCollectorDomainCacheManager dataCollectorDomainCacheManager;
    private DataCollectorDomainCache dataCollectorDomainCache;
    private AdEventFactory adEventFactory;
    private KryoManager kryoManager;
    private ClickService clickService;
    private CounterManager counterManager;
    private JmsUtils jmsUtils;
    private V1DomainModelMapper mapper;
    
    private int currentDate;

    @Before
    public void runBeforeEachTest() throws java.io.IOException {
        clusterDao = mock(ClusterDao.class);
        missingCampaignsCache = mock(Ehcache.class);
        batchManager = mock(BatchManager.class);
        dataCollectorDomainCacheManager = mock(DataCollectorDomainCacheManager.class);
        adEventFactory = mock(AdEventFactory.class);
        kryoManager = mock(KryoManager.class);
        clickService = mock(ClickService.class);
        counterManager = mock(CounterManager.class);
        jmsUtils = mock(JmsUtils.class);
        mapper = mock(V1DomainModelMapper.class);
        
        java.util.logging.Logger.getLogger(AdEventDataCollector.class.getName()).setLevel(Level.INFO);
        
        adEventDataCollector = new AdEventDataCollector(new File(FAILED_LOG_FILE), CLICK_TTL, INSTALL_TTL, CONVERSION_TTL, USER_AGENT_ID_CACHE_MAX_SIZE);
        
        inject(adEventDataCollector, "clusterDao", clusterDao);
        inject(adEventDataCollector, "missingCampaignsCache", missingCampaignsCache);
        inject(adEventDataCollector, "batchManager", batchManager);
        inject(adEventDataCollector, "dataCollectorDomainCacheManager", dataCollectorDomainCacheManager);
        inject(adEventDataCollector, "adEventFactory", adEventFactory);
        inject(adEventDataCollector, "kryoManager", kryoManager);
        inject(adEventDataCollector, "clickService", clickService);
        inject(adEventDataCollector, "counterManager", counterManager);
        inject(adEventDataCollector, "jmsUtils", jmsUtils);
        
        dataCollectorDomainCache = mock(DataCollectorDomainCache.class);
        
        currentDate = Integer.valueOf(FastDateFormat.getInstance("yyyyMMdd").format(com.adfonic.util.DateUtils.getStartOfDay(new Date(), TimeZone.getDefault()).getTime()));
    }

    @After
    public void runAfterEachTest() {
        adEventDataCollector.destroy();

        File dot = new File(".");
        for (String filename : dot.list(new PrefixFileFilter("TestAdEventDataCollector"))) {
            FileUtils.deleteQuietly(new File(dot, filename));
        }
    }

    @Test
    public void testHandleAdEvent01_no_publication() throws java.sql.SQLException {
        final AdEvent adEvent = mock(AdEvent.class);
        final long modelId = randomLong();
        final String userAgentHeader = randomAlphaNumericString(10);
        final long userAgentId = randomLong();
        final long publicationId = randomLong();
        final UserAgent userAgent = mock(UserAgent.class);

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(userAgentHeader));
            allowing (adEvent).getPublicationId(); will(returnValue(publicationId));
            allowing (clusterDao).getOrCreateUserAgent(userAgentHeader, modelId, currentDate); will(returnValue(userAgent));
            allowing (dataCollectorDomainCacheManager).getCache(); will(returnValue(dataCollectorDomainCache));
            allowing (userAgent).getUserAgentId(); will(returnValue(userAgentId));
            
            oneOf (dataCollectorDomainCache).getPublicationById(publicationId); will(returnValue(null));
        }});

        adEventDataCollector.handleAdEvent(adEvent);
    }
    
    @Test
    public void testHandleAdEvent02_campaignId_null() throws java.sql.SQLException {
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = new Date();
        final long modelId = randomLong();
        final String userAgentHeader = randomAlphaNumericString(10);
        final long userAgentId = randomLong();
        final long publicationId = randomLong();
        final PublicationDto publication = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final TimeZone publisherTimeZone = TimeZone.getDefault();
        final UserAgent userAgent = mock(UserAgent.class);
        final AdEventAccounting accounting = null;
        final Integer advertiserTimeId = null;

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(userAgentHeader));
            allowing (adEvent).getPublicationId(); will(returnValue(publicationId));
            allowing (clusterDao).getOrCreateUserAgent(userAgentHeader, modelId, currentDate); will(returnValue(userAgent));
            allowing (dataCollectorDomainCacheManager).getCache(); will(returnValue(dataCollectorDomainCache));
            allowing (dataCollectorDomainCache).getPublicationById(publicationId); will(returnValue(publication));
            allowing (publication).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisherCompany).getDefaultTimeZone(); will(returnValue(publisherTimeZone));

            allowing (adEvent).getCampaignId(); will(returnValue(null));
            allowing (userAgent).getUserAgentId(); will(returnValue(userAgentId));

            oneOf (clusterDao).createAdEventLog(with(adEvent), with(accounting), with(userAgentId), with(advertiserTimeId), with(any(Integer.class)));
        }});

        adEventDataCollector.handleAdEvent(adEvent);
    }
    
    @Test
    public void testHandleAdEvent03_campaign_null() throws java.sql.SQLException {
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = new Date();
        final long modelId = randomLong();
        final String userAgentHeader = randomAlphaNumericString(10);
        final long userAgentId = randomLong();
        final long publicationId = randomLong();
        final PublicationDto publication = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final TimeZone publisherTimeZone = TimeZone.getDefault();
        final long campaignId = randomLong();
        final UserAgent userAgent = mock(UserAgent.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final CompanyDto company = mock(CompanyDto.class);
        final Element element = new Element(campaignId, campaign);
        final AdEventAccounting accounting = null;
        
        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (missingCampaignsCache).get(campaignId); will(returnValue(element));
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(userAgentHeader));
            allowing (adEvent).getPublicationId(); will(returnValue(publicationId));
            allowing (clusterDao).getOrCreateUserAgent(userAgentHeader, modelId, currentDate); will(returnValue(userAgent));
            allowing (dataCollectorDomainCacheManager).getCache(); will(returnValue(dataCollectorDomainCache));
            allowing (dataCollectorDomainCache).getPublicationById(publicationId); will(returnValue(publication));
            allowing (publication).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisherCompany).getDefaultTimeZone(); will(returnValue(publisherTimeZone));
            allowing (adEvent).getCampaignId(); will(returnValue(campaignId));
            allowing (userAgent).getUserAgentId(); will(returnValue(userAgentId));
            allowing (dataCollectorDomainCache).getCampaignById(campaignId); will(returnValue(null));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (advertiser).getCompany(); will(returnValue(company));
            allowing (company).getDefaultTimeZone(); will(returnValue(TimeZoneUtils.getDefaultTimeZone()));

            oneOf (clusterDao).createAdEventLog(with(adEvent), with(accounting), with(userAgentId), with(any(Integer.class)), with(any(Integer.class)));
        }});

        adEventDataCollector.handleAdEvent(adEvent);
    }
    
    @Test
    @Ignore("Breaks")
    public void testHandleAdEvent04_non_batch() throws java.sql.SQLException {
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = new Date();
        final long modelId = randomLong();
        final String userAgentHeader = randomAlphaNumericString(10);
        final long userAgentId = randomLong();
        final long publicationId = randomLong();
        final PublicationDto publication = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final TimeZone publisherTimeZone = TimeZone.getDefault();
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final BigDecimal revShare = BigDecimal.valueOf(0.60);
        final long campaignId = randomLong();
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
        final TimeZone advertiserTimeZone = TimeZone.getDefault();
        final CampaignBidDto campaignBid = mock(CampaignBidDto.class);
        final UserAgent userAgent = mock(UserAgent.class);

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(userAgentHeader));
            allowing (adEvent).getPublicationId(); will(returnValue(publicationId));
            allowing (clusterDao).getOrCreateUserAgent(userAgentHeader, modelId, currentDate); will(returnValue(userAgent));
            allowing (dataCollectorDomainCacheManager).getCache(); will(returnValue(dataCollectorDomainCache));
            allowing (dataCollectorDomainCache).getPublicationById(publicationId); will(returnValue(publication));
            allowing (publication).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisherCompany).getDefaultTimeZone(); will(returnValue(publisherTimeZone));
            allowing (adEvent).getCampaignId(); will(returnValue(campaignId));
            allowing (dataCollectorDomainCache).getCampaignById(campaignId); will(returnValue(campaign));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (advertiserCompany).getDefaultTimeZone(); will(returnValue(advertiserTimeZone));

            // Needed by AdEventAccounting
            allowing (adEvent).isRtb(); will(returnValue(false));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.AD_SERVED));
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (campaign).getCurrentBid(); will(returnValue(campaignBid));
            allowing (campaignBid).getBidType(); will(returnValue(BidType.CPC));
            allowing (campaignBid).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -1)));
            allowing (campaignBid).getAmount(); will(returnValue(new BigDecimal(1.2)));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -1)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(revShare));
            
            allowing (userAgent).getUserAgentId(); will(returnValue(userAgentId));

            // Non-batch...we just log the ad event
            oneOf (clusterDao).createAdEventLog(with(adEvent), with(any(AdEventAccounting.class)), with(userAgentId), with(any(Integer.class)), with(any(Integer.class)));
        }});

        adEventDataCollector.handleAdEvent(adEvent);
    }
    
    @Test
    @Ignore("Breaks")
    public void testHandleAdEvent05_batch() throws java.sql.SQLException {
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = new Date();
        final long modelId = randomLong();
        final String userAgentHeader = randomAlphaNumericString(10);
        final long userAgentId = randomLong();
        final long publicationId = randomLong();
        final PublicationDto publication = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto publisherCompany = mock(com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto.class, "publisherCompany");
        final TimeZone publisherTimeZone = TimeZone.getDefault();
        final PublisherRevShareDto publisherRevShare = mock(PublisherRevShareDto.class);
        final BigDecimal revShare = BigDecimal.valueOf(0.60);
        final long campaignId = randomLong();
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        final com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto advertiserCompany = mock(com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto.class, "advertiserCompany");
        final TimeZone advertiserTimeZone = TimeZone.getDefault();
        final CampaignBidDto campaignBid = mock(CampaignBidDto.class);
        final UserAgent userAgent = mock(UserAgent.class);

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getEventTime(); will(returnValue(eventTime));
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(userAgentHeader));
            allowing (adEvent).getPublicationId(); will(returnValue(publicationId));
            allowing (clusterDao).getOrCreateUserAgent(userAgentHeader, modelId, currentDate); will(returnValue(userAgent));
            allowing (dataCollectorDomainCacheManager).getCache(); will(returnValue(dataCollectorDomainCache));
            allowing (dataCollectorDomainCache).getPublicationById(publicationId); will(returnValue(publication));
            allowing (publication).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getCompany(); will(returnValue(publisherCompany));
            allowing (publisherCompany).getDefaultTimeZone(); will(returnValue(publisherTimeZone));
            allowing (adEvent).getCampaignId(); will(returnValue(campaignId));
            allowing (dataCollectorDomainCache).getCampaignById(campaignId); will(returnValue(campaign));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (advertiser).getCompany(); will(returnValue(advertiserCompany));
            allowing (advertiserCompany).getDefaultTimeZone(); will(returnValue(advertiserTimeZone));
            
            allowing (userAgent).getUserAgentId(); will(returnValue(userAgentId));

            // Needed by AdEventAccounting
            allowing (adEvent).isRtb(); will(returnValue(false));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.CLICK));
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (campaign).getCurrentBid(); will(returnValue(campaignBid));
            allowing (campaignBid).getBidType(); will(returnValue(BidType.CPC));
            allowing (campaignBid).getAmount(); will(returnValue(BigDecimal.valueOf(0.10)));
            allowing (campaignBid).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -1)));
            allowing (advertiserCompany).getMediaCostMargin(); will(returnValue(BigDecimal.ZERO));
            allowing (publisher).getCurrentPublisherRevShare(); will(returnValue(publisherRevShare));
            allowing (publisherRevShare).getStartDate(); will(returnValue(DateUtils.addDays(eventTime, -1)));
            allowing (publisherRevShare).getRevShare(); will(returnValue(revShare));
            allowing (advertiserCompany).isTaxableAdvertiser(); will(returnValue(false));
            allowing (campaign).getAgencyDiscount(); will(returnValue(null));
            allowing (advertiserCompany).isPostPay(); will(returnValue(false));
            allowing (publisherCompany).isTaxablePublisher(); will(returnValue(false));

            // Batch
            oneOf (batchManager).addToCurrentBatch(with(any(AdEventAccounting.class)), with(userAgentId));
        }});

        adEventDataCollector.handleAdEvent(adEvent);
    }
    
    @Test
    public void testEstablishUserAgentId01_null_modelId() {
        final AdEvent adEvent = mock(AdEvent.class);

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getModelId(); will(returnValue(null));
        }});

        assertNull(adEventDataCollector.establishUserAgentId(adEvent));
    }
    
    @Test
    public void testEstablishUserAgentId02_null_userAgent() {
        final AdEvent adEvent = mock(AdEvent.class);
        final long modelId = randomLong();

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(null));
        }});

        assertNull(adEventDataCollector.establishUserAgentId(adEvent));
    }
    
    @Test
    public void testEstablishUserAgentId03_normal() throws Exception {
        final AdEvent adEvent = mock(AdEvent.class);
        final long modelId = randomLong();
        final String userAgentHeader = randomAlphaNumericString(AdEventDataCollector.MAX_USER_AGENT_LENGTH - 1);
        final long userAgentId = randomLong();
        final UserAgent userAgent = mock(UserAgent.class);

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(userAgentHeader));
            oneOf(clusterDao).getOrCreateUserAgent(userAgentHeader, modelId, currentDate); will(returnValue(userAgent));
            allowing (userAgent).getUserAgentId(); will(returnValue(userAgentId));
            allowing (userAgent).getLastSeen(); will(returnValue(currentDate));
        }});

        assertEquals((Object)userAgentId, adEventDataCollector.establishUserAgentId(adEvent));
        
        // 2nd call should use the cached value
        assertEquals((Object)userAgentId, adEventDataCollector.establishUserAgentId(adEvent));
    }
    
    @Test
    public void testEstablishUserAgentId04_truncate() throws Exception {
        final AdEvent adEvent = mock(AdEvent.class);
        final long modelId = randomLong();
        final String userAgentHeader = randomAlphaNumericString(AdEventDataCollector.MAX_USER_AGENT_LENGTH + 1);
        final long userAgentId = randomLong();
        final String truncated = userAgentHeader.substring(0, AdEventDataCollector.MAX_USER_AGENT_LENGTH);
        final UserAgent userAgent = mock(UserAgent.class);
        
        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(userAgentHeader));
            oneOf (clusterDao).getOrCreateUserAgent(truncated, modelId, currentDate); will(returnValue(userAgent));
            allowing (userAgent).getUserAgentId(); will(returnValue(userAgentId));
            allowing (userAgent).getLastSeen(); will(returnValue(currentDate));
        }});

        assertEquals((Object)userAgentId, adEventDataCollector.establishUserAgentId(adEvent));
        
        // 2nd call should use the cached value
        assertEquals((Object)userAgentId, adEventDataCollector.establishUserAgentId(adEvent));
    }
    
    @Test
    public void testEstablishUserAgentId05_clusterDao_throws() throws Exception {
        final AdEvent adEvent = mock(AdEvent.class);
        final long modelId = randomLong();
        final String userAgentHeader = randomAlphaNumericString(AdEventDataCollector.MAX_USER_AGENT_LENGTH - 1);

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(userAgentHeader));
            oneOf (clusterDao).getOrCreateUserAgent(userAgentHeader, modelId, currentDate); will(throwException(new java.sql.SQLException("bummer")));
        }});

        assertNull(adEventDataCollector.establishUserAgentId(adEvent));
    }
    
    //TODO test update in cache
    @Test
    public void testEstablishUserAgentId06_update_cache() throws Exception {
        final AdEvent adEvent = mock(AdEvent.class);
        final long modelId = randomLong();
        final String userAgentHeader = randomAlphaNumericString(AdEventDataCollector.MAX_USER_AGENT_LENGTH - 1);
        final long userAgentId = randomLong();
        final UserAgent userAgent = mock(UserAgent.class);

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).getModelId(); will(returnValue(modelId));
            allowing (adEvent).getUserAgentHeader(); will(returnValue(userAgentHeader));
            oneOf(clusterDao).getOrCreateUserAgent(userAgentHeader, modelId, currentDate); will(returnValue(userAgent));
            allowing (userAgent).getUserAgentId(); will(returnValue(userAgentId));
            allowing (userAgent).getLastSeen(); will(returnValue(currentDate-1));
            oneOf(clusterDao).updateUserAgent(null, userAgentHeader, modelId, currentDate);
            allowing (userAgent).setLastSeen(currentDate);
        }});

        assertEquals((Object)userAgentId, adEventDataCollector.establishUserAgentId(adEvent));
        // 2nd call should use the cached value
        assertEquals((Object)userAgentId, adEventDataCollector.establishUserAgentId(adEvent));
    }

    @Test
    public void testOnUserAgentUpdated01_delete() {
        final UserAgentUpdatedMessage message = mock(UserAgentUpdatedMessage.class);
        final UserAgentUpdatedMessage.ChangeType changeType = UserAgentUpdatedMessage.ChangeType.DELETE;
        final long userAgentId1 = uniqueLong("userAgentId");
        long userAgentId2 = uniqueLong("userAgentId");
        final UserAgent userAgent1 = new UserAgent(userAgentId1, currentDate);
        final UserAgent userAgent2 = new UserAgent(userAgentId2, currentDate);
        
        expect(new Expectations() {{
            allowing (message).getChangeType(); will(returnValue(changeType));
            allowing (message).getUserAgentId(); will(returnValue(userAgentId1));
        }});

        String userAgentHeader1 = uniqueAlphaNumericString(10, "userAgentHeader");
        String userAgentHeader2 = uniqueAlphaNumericString(10, "userAgentHeader");

        long modelId = randomLong();

        adEventDataCollector.getUserAgentMap().clear();
        adEventDataCollector.getUserAgentMap().put(userAgentHeader1, modelId, userAgent1);
        adEventDataCollector.getUserAgentMap().put(userAgentHeader2, modelId, userAgent2);

        adEventDataCollector.onUserAgentUpdated(message);

        assertFalse(adEventDataCollector.getUserAgentMap().containsKey(userAgentHeader1, modelId));
        assertTrue(adEventDataCollector.getUserAgentMap().containsKey(userAgentHeader2, modelId));
    }

    @Test
    public void testOnUserAgentUpdated01_update() {
        final UserAgentUpdatedMessage message = mock(UserAgentUpdatedMessage.class);
        final UserAgentUpdatedMessage.ChangeType changeType = UserAgentUpdatedMessage.ChangeType.UPDATE;
        final long userAgentId1 = uniqueLong("userAgentId");
        final long newModelId = uniqueLong("modelId");
        long userAgentId2 = uniqueLong("userAgentId");
        long modelId2 = uniqueLong("modelId");
        final UserAgent userAgent1 = new UserAgent(userAgentId1, currentDate);
        final UserAgent userAgent2 = new UserAgent(userAgentId2, currentDate);

        
        expect(new Expectations() {{
            allowing (message).getChangeType(); will(returnValue(changeType));
            allowing (message).getUserAgentId(); will(returnValue(userAgentId1));
            allowing (message).getNewModelId(); will(returnValue(newModelId));
        }});

        String userAgentHeader1 = uniqueAlphaNumericString(10, "userAgentHeader");
        String userAgentHeader2 = uniqueAlphaNumericString(10, "userAgentHeader");
        String userAgentHeader3 = uniqueAlphaNumericString(10, "userAgentHeader");


        adEventDataCollector.getUserAgentMap().clear();
        // This entry can stay, same ua id and model id
        adEventDataCollector.getUserAgentMap().put(userAgentHeader1, newModelId, userAgent1);
        // This entry can stay, different ua id (same model id, which doesn't matter)
        adEventDataCollector.getUserAgentMap().put(userAgentHeader2, newModelId, userAgent2);
        // This entry must get removed, same ua id, different model id
        adEventDataCollector.getUserAgentMap().put(userAgentHeader3, modelId2, userAgent1);

        adEventDataCollector.onUserAgentUpdated(message);

        assertTrue(adEventDataCollector.getUserAgentMap().containsKey(userAgentHeader1, newModelId));
        assertTrue(adEventDataCollector.getUserAgentMap().containsKey(userAgentHeader2, newModelId));
        assertFalse(adEventDataCollector.getUserAgentMap().containsKey(userAgentHeader3, modelId2));
    }

    @Test
    public void testOnAdEvent01_testMode() {
        final AdEvent adEvent = mock(AdEvent.class);

        expect(new Expectations() {{
            ignoring (counterManager);
            allowing (adEvent).isTestMode(); will(returnValue(true));
            allowing (adEvent).getAdAction(); will(returnValue(AdAction.UNFILLED_REQUEST));
        }});

        adEventDataCollector.onAdEvent(adEvent, false);
    }

    @Test
    public void testOnAdEvent03_throws_failedPreviously() {
        final AdEvent adEvent = mock(AdEvent.class);

        expect(new Expectations() {{
            ignoring (counterManager);
            oneOf (adEvent).isTestMode(); will(returnValue(false));
            oneOf (adEvent).getModelId(); will(throwException(new RuntimeException("bummer")));
            // No admqJmsTemplate.sendBody call, just logged to the failure log
            oneOf (adEvent).toCsv(); will(returnValue(randomAlphaNumericString(10)));
        }});
        
        adEventDataCollector.onAdEvent(adEvent, true);
    }
}

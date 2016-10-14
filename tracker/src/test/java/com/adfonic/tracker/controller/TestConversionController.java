package com.adfonic.tracker.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publication;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.tracker.ClickService;
import com.adfonic.tracker.ConversionService;
import com.adfonic.tracker.kafka.TrackerKafka;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.publication.service.PublicationManager;

// The JPA metamodel state must be initialized before use, and that requires
// that we activate the persistence context.  The simplest way to do that is
// with a simple EntityManagerFactory config with an H2 in-memory db.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/h2-jpa-context.xml" })
@SuppressWarnings("deprecation")
public class TestConversionController extends AbstractAdfonicTest {

    private ConversionController conversionController;
    private ClickService clickService;
    private ConversionService conversionService;
    private AdEventFactory adEventFactory;
    private TrackerKafka trackerKafka;
    private PublicationManager publicationManager;
    private CreativeManager creativeManager;
    private V1DomainModelMapper mapper;

    @Before
    public void setup() {
        conversionController = new ConversionController();
        clickService = mock(ClickService.class);
        conversionService = mock(ConversionService.class);
        adEventFactory = mock(AdEventFactory.class);
        trackerKafka = mock(TrackerKafka.class);
        publicationManager = mock(PublicationManager.class);
        creativeManager = mock(CreativeManager.class);
        mapper = mock(V1DomainModelMapper.class);

        inject(conversionController, "clickService", clickService);
        inject(conversionController, "conversionService", conversionService);
        inject(conversionController, "adEventFactory", adEventFactory);
        inject(conversionController, "trackerKafka", trackerKafka);
        inject(conversionController, "publicationManager", publicationManager);
        inject(conversionController, "creativeManager", creativeManager);
        inject(conversionController, "mapper", mapper);
    }

    @Test
    public void testConversionController01a_deDupConversion() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Click click = null;
        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                oneOf(conversionService).scheduleConversionRetry(clickExternalID);
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversion(clickExternalID);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertNotNull(returnMap.get("error"));
    }

    @Test
    public void testConversionController01b_deDupConversion() {
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String clickExternalID = randomAlphaNumericString(10);
        final Click click = null;
        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                oneOf(conversionService).scheduleConversionRetry(clickExternalID);
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversion(advertiserExternalID, clickExternalID);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertNotNull(returnMap.get("error"));
    }

    @Test
    public void testConversionController02_deDupConversion_enabled() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class);
        final boolean conversionTracked = false;
        final long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final Campaign campaign = mock(Campaign.class);
        final Advertiser advertiser = mock(Advertiser.class);

        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, ConversionController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, ConversionController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(adSpace));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(creative).getId();
                will(returnValue(creativeId));
                oneOf(campaign).isConversionTrackingEnabled();
                will(returnValue(true));
                oneOf(conversionService).trackConversion(click);
                will(returnValue(conversionTracked));
                oneOf(click).getExternalID();
                will(returnValue(clickExternalID));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).isConversionProtected();
                will(returnValue(false));
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversion(clickExternalID);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertEquals("Duplicate conversion", returnMap.get("error"));
    }

    @Test
    public void testConversionController03_deDupConversion_disabled() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final Campaign campaign = mock(Campaign.class);
        final Advertiser advertiser = mock(Advertiser.class);

        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, ConversionController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, ConversionController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(adSpace));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(creative).getId();
                will(returnValue(creativeId));
                oneOf(campaign).isConversionTrackingEnabled();
                will(returnValue(false));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).isConversionProtected();
                will(returnValue(false));
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversion(clickExternalID);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertEquals("Conversion tracking not enabled on campaign", returnMap.get("error"));
    }

    @Test
    public void testConversionController04_deDupConversion() throws Exception {
        final String clickExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class);
        final boolean conversionTracked = true;
        final AdEvent adEvent = mock(AdEvent.class, "adevent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");
        final Creative creative = mock(Creative.class);
        final AdSpace adSpace = mock(AdSpace.class);
        final Long creativeId = randomLong();
        final Long adSpaceId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();
        final Map<Long, String> deviceIdentifiers = new HashMap<>();
        final Advertiser advertiser = mock(Advertiser.class);

        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, ConversionController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, ConversionController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(adSpace));
                oneOf(campaign).isConversionTrackingEnabled();
                will(returnValue(true));
                oneOf(conversionService).trackConversion(click);
                will(returnValue(conversionTracked));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                oneOf(clickService).loadDeviceIdentifiers(click);
                allowing(click).getDeviceIdentifiers();
                will(returnValue(deviceIdentifiers));
                oneOf(adEventFactory).newInstance(AdAction.CONVERSION);
                will(returnValue(adEvent));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getId();
                will(returnValue(publicationId));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(mapper).map(adEvent);
                will(returnValue(ae));
                oneOf(trackerKafka).logAdEvent(ae);
                allowing(ae).getCreativeId();
                allowing(ae).getAdSpaceId();
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).isConversionProtected();
                will(returnValue(false));
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversion(clickExternalID);
        assertNotNull(returnMap);
        assertEquals(1, returnMap.get("success"));
    }

    @Test
    public void testConversionController05_deDupConversion_creative_null() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class);
        final Long creativeId = randomLong();

        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, ConversionController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(null));
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversion(clickExternalID);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertEquals("Internal error", returnMap.get("error"));
    }

    @Test
    public void testConversionController06_deDupConversion_adSpace_null() {
        final String clickExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class);
        final Long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final Long adSpaceId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final Advertiser advertiser = mock(Advertiser.class);

        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, ConversionController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, ConversionController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(null));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).isConversionProtected();
                will(returnValue(false));
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversion(clickExternalID);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertEquals("Internal error", returnMap.get("error"));
    }

    @Test
    public void testConversionController07_deDupConversion() throws IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        final String clickExternalID = randomAlphaNumericString(10);
        final Click click = null;
        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                oneOf(conversionService).scheduleConversionRetry(clickExternalID);
            }
        });

        conversionController.deDupConversionFromUser(request, mockHttpServletResponse, clickExternalID);
        assertEquals("image/gif", mockHttpServletResponse.getContentType());
    }

    @Test
    public void testConversionController08_deDupConversion() throws IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        final String clickExternalID = null;
        final String referer = randomUrl();

        expect(new Expectations() {
            {
                oneOf(request).getHeader("Referer");
                will(returnValue(referer));
            }
        });

        conversionController.deDupConversionFromUser(request, mockHttpServletResponse, clickExternalID);
        assertEquals("image/gif", mockHttpServletResponse.getContentType());
    }

    @Test
    public void test10_deDupConversionFromUser_no_cookie() throws IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final HttpServletResponse response = mock(HttpServletResponse.class, "response");
        final ServletOutputStream outputStream = mock(ServletOutputStream.class, "outputStream");
        final String referer = randomUrl();
        final String clickExternalID = null;
        expect(new Expectations() {
            {
                oneOf(response).setContentType("image/gif");
                oneOf(response).getOutputStream();
                will(returnValue(outputStream));
                oneOf(outputStream).write(with(equal(conversionController.pixelBytes)));
                oneOf(outputStream).flush();
                oneOf(request).getHeader("Referer");
                will(returnValue(referer));
            }
        });

        conversionController.deDupConversionFromUser(request, response, clickExternalID);
    }

    @Test
    public void test10_deDupConversionFromUserSecure_no_cookie() throws IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final HttpServletResponse response = mock(HttpServletResponse.class, "response");
        final ServletOutputStream outputStream = mock(ServletOutputStream.class, "outputStream");
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String referer = randomUrl();
        final String clickExternalID = null;
        expect(new Expectations() {
            {
                oneOf(response).setContentType("image/gif");
                oneOf(response).getOutputStream();
                will(returnValue(outputStream));
                oneOf(outputStream).write(with(equal(conversionController.pixelBytes)));
                oneOf(outputStream).flush();
                oneOf(request).getHeader("Referer");
                will(returnValue(referer));
            }
        });

        conversionController.deDupConversionFromUserSecure(request, response, advertiserExternalID, clickExternalID);
    }

    @Test
    public void test11_deDupConversionFromUser_code_coverage() throws IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final HttpServletResponse response = mock(HttpServletResponse.class, "response");
        final ServletOutputStream outputStream = mock(ServletOutputStream.class, "outputStream");
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String clickExternalID = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(response).setContentType("image/gif");
                oneOf(response).getOutputStream();
                will(returnValue(outputStream));
                oneOf(outputStream).write(with(equal(conversionController.pixelBytes)));
                oneOf(outputStream).flush();
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(null));
                oneOf(conversionService).scheduleConversionRetry(clickExternalID);
            }
        });

        conversionController.deDupConversionFromUser(request, response, advertiserExternalID, clickExternalID);
    }

    @Test
    public void test11_deDupConversionFromUserSecure_code_coverage() throws IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final HttpServletResponse response = mock(HttpServletResponse.class, "response");
        final ServletOutputStream outputStream = mock(ServletOutputStream.class, "outputStream");
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String clickExternalID = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(response).setContentType("image/gif");
                oneOf(response).getOutputStream();
                will(returnValue(outputStream));
                oneOf(outputStream).write(with(equal(conversionController.pixelBytes)));
                oneOf(outputStream).flush();
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(null));
                oneOf(conversionService).scheduleConversionRetry(clickExternalID);
            }
        });

        conversionController.deDupConversionFromUserSecure(request, response, advertiserExternalID, clickExternalID);
    }

    @Test
    public void testConversionController_deDupConversionSecure() throws Exception {
        final String clickExternalID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class);
        final boolean conversionTracked = true;
        final AdEvent adEvent = mock(AdEvent.class, "adevent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");
        final Creative creative = mock(Creative.class);
        final Advertiser advertiser = mock(Advertiser.class);
        final AdSpace adSpace = mock(AdSpace.class);
        final Long creativeId = randomLong();
        final Long adSpaceId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();
        final Map<Long, String> deviceIdentifiers = new HashMap<>();

        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, ConversionController.CREATIVE_FETCH_STRATEGY_SECURE);
                will(returnValue(creative));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, ConversionController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(adSpace));
                oneOf(campaign).isConversionTrackingEnabled();
                will(returnValue(true));
                oneOf(conversionService).trackConversion(click);
                will(returnValue(conversionTracked));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).getExternalID();
                will(returnValue(advertiserExternalID));
                oneOf(clickService).loadDeviceIdentifiers(click);
                allowing(click).getDeviceIdentifiers();
                will(returnValue(deviceIdentifiers));
                oneOf(adEventFactory).newInstance(AdAction.CONVERSION);
                will(returnValue(adEvent));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getId();
                will(returnValue(publicationId));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(mapper).map(adEvent);
                will(returnValue(ae));
                oneOf(trackerKafka).logAdEvent(ae);
                allowing(ae).getCreativeId();
                allowing(ae).getAdSpaceId();
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversionSecure(advertiserExternalID, clickExternalID);
        assertNotNull(returnMap);
        assertEquals(1, returnMap.get("success"));
    }

    @Test
    public void testConversionController_deDupConversionSecure_AdvertiserID_Null() throws Exception {
        final String clickExternalID = randomAlphaNumericString(10);
        final String advertiserExternalID = null;
        final Click click = mock(Click.class);
        final Creative creative = mock(Creative.class);
        final Advertiser advertiser = mock(Advertiser.class);
        final Long creativeId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();

        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, ConversionController.CREATIVE_FETCH_STRATEGY_SECURE);
                will(returnValue(creative));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).getExternalID();
                will(returnValue(advertiserExternalID));
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversionSecure(advertiserExternalID, clickExternalID);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
    }

    @Test
    public void testConversionController_deDupConversionSecure_AdvertiserID_DoesnotMatch() throws Exception {
        final String clickExternalID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class);
        final Creative creative = mock(Creative.class);
        final Advertiser advertiser = mock(Advertiser.class);
        final Long creativeId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();

        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, ConversionController.CREATIVE_FETCH_STRATEGY_SECURE);
                will(returnValue(creative));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversionSecure(advertiserExternalID, clickExternalID);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
    }

    @Test
    public void testConversionController_deDupConversionSercure_AdvertiserIsConversionProtected() throws Exception {
        final String clickExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class);
        final Long creativeId = randomLong();
        final Creative creative = mock(Creative.class);
        final Campaign campaign = mock(Campaign.class);
        final Advertiser advertiser = mock(Advertiser.class);
        final Long advertiserId = randomLong();

        expect(new Expectations() {
            {
                oneOf(clickService).getClickByExternalID(clickExternalID);
                will(returnValue(click));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, ConversionController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getAdvertiser();
                will(returnValue(advertiser));
                allowing(advertiser).isConversionProtected();
                will(returnValue(true));
                allowing(advertiser).getId();
                will(returnValue(advertiserId));
            }
        });

        Map<String, Object> returnMap = conversionController.deDupConversion(clickExternalID);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertEquals("Internal error", returnMap.get("error"));
    }
}

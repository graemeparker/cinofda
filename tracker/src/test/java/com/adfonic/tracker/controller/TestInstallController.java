package com.adfonic.tracker.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.apache.commons.codec.digest.DigestUtils;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Publication;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.tracker.ClickService;
import com.adfonic.tracker.InstallService;
import com.adfonic.tracker.controller.view.HtmlView;
import com.adfonic.tracker.controller.view.JsonView;
import com.adfonic.tracker.kafka.TrackerKafka;
import com.adfonic.util.BasicAuthUtils;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.publication.service.PublicationManager;
import com.deviceinsight.utils.DeviceInsightUtils;

// The JPA metamodel state must be initialized before use, and that requires
// that we activate the persistence context.  The simplest way to do that is
// with a simple EntityManagerFactory config with an H2 in-memory db.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/h2-jpa-context.xml" })
@PrepareForTest(DeviceInsightUtils.class)
@SuppressWarnings({ "unchecked", "serial", "rawtypes", "deprecation" })
public class TestInstallController extends AbstractAdfonicTest {

    private InstallController installController;

    private ClickService clickService;
    private InstallService installService;
    private AdEventFactory adEventFactory;
    private TrackerKafka trackerKafka;
    private CreativeManager creativeManager;
    private PublicationManager publicationManager;
    private DeviceManager deviceManager;
    private Properties trackerProperties;
    private V1DomainModelMapper mapper;

    private HtmlView htmlView;
    private JsonView jsonView;

    @Before
    public void setup() {
        installController = new InstallController();
        clickService = mock(ClickService.class, "clickService");
        installService = mock(InstallService.class, "installService");
        adEventFactory = mock(AdEventFactory.class, "adEventFactory");
        trackerKafka = mock(TrackerKafka.class);
        creativeManager = mock(CreativeManager.class, "creativeManager");
        publicationManager = mock(PublicationManager.class, "publicationManager");
        deviceManager = mock(DeviceManager.class, "deviceManager");
        trackerProperties = mock(Properties.class, "trackerProperties");
        htmlView = mock(HtmlView.class);
        jsonView = mock(JsonView.class);
        mapper = mock(V1DomainModelMapper.class);

        inject(installController, "clickService", clickService);
        inject(installController, "installService", installService);
        inject(installController, "adEventFactory", adEventFactory);
        inject(installController, "trackerKafka", trackerKafka);
        inject(installController, "creativeManager", creativeManager);
        inject(installController, "publicationManager", publicationManager);
        inject(installController, "deviceManager", deviceManager);
        inject(installController, "trackerProperties", trackerProperties);
        inject(installController, "mapper", mapper);
        inject(installController, "jsonView", jsonView);
        inject(installController, "htmlView", htmlView);
    }

    @Test
    public void testInstallController_trackAuthenticatedInstall_noAuthorizationHeader() {
        final String authorizationHeader = null;
        final String applicationID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
            }
        });

        ResponseEntity<String> responseEntity = installController.trackAuthenticatedInstall(authorizationHeader, applicationID, advertiserExternalID, creativeExternalID,
                impressionExternalID);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testInstallController_trackAuthenticatedInstall_invalidAuthorizationHeader() {
        final String authorizationHeader = "";
        final String applicationID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
            }
        });

        ResponseEntity<String> responseEntity = installController.trackAuthenticatedInstall(authorizationHeader, applicationID, advertiserExternalID, creativeExternalID,
                impressionExternalID);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testInstallController_trackAuthenticatedInstall_usernameNotRecognized() {
        final String username = randomAlphaNumericString(10);
        final String password = randomAlphaNumericString(10);
        final String authorizationHeader = BasicAuthUtils.generateAuthorizationHeader(username, password);
        final String applicationID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(trackerProperties).getProperty(with(any(String.class)));
                will(returnValue(null));
            }
        });

        ResponseEntity<String> responseEntity = installController.trackAuthenticatedInstall(authorizationHeader, applicationID, advertiserExternalID, creativeExternalID,
                impressionExternalID);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testInstallController_trackAuthenticatedInstall_invalidPassword() {
        final String username = randomAlphaNumericString(10);
        final String password = "suppliedPassword";
        final String authorizationHeader = BasicAuthUtils.generateAuthorizationHeader(username, password);
        final String applicationID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(trackerProperties).getProperty(with(any(String.class)));
                will(returnValue("validPassword"));
            }
        });

        ResponseEntity<String> responseEntity = installController.trackAuthenticatedInstall(authorizationHeader, applicationID, advertiserExternalID, creativeExternalID,
                impressionExternalID);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testInstallController_trackAuthenticatedInstall_clickNotFound() {
        final String username = randomAlphaNumericString(10);
        final String password = randomAlphaNumericString(10);
        final String authorizationHeader = BasicAuthUtils.generateAuthorizationHeader(username, password);
        final String applicationID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(trackerProperties).getProperty(with(any(String.class)));
                will(returnValue(password));
                oneOf(clickService).getClickByExternalID(impressionExternalID);
                will(returnValue(null));
                oneOf(installService).scheduleAuthenticatedInstallRetry(impressionExternalID);
            }
        });

        ResponseEntity<String> responseEntity = installController.trackAuthenticatedInstall(authorizationHeader, applicationID, advertiserExternalID, creativeExternalID,
                impressionExternalID);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testInstallController_trackAuthenticatedInstall_applicationIdMismatch() {
        final String username = randomAlphaNumericString(10);
        final String password = randomAlphaNumericString(10);
        final String authorizationHeader = BasicAuthUtils.generateAuthorizationHeader(username, password);
        final String applicationID = "match";
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class, "click");
        final Long creativeId = randomLong();
        final Creative creative = mock(Creative.class, "creative");
        final Campaign campaign = mock(Campaign.class, "campaign");
        final Long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class, "adSpace");
        expect(new Expectations() {
            {
                oneOf(trackerProperties).getProperty(with(any(String.class)));
                will(returnValue(password));
                oneOf(clickService).getClickByExternalID(impressionExternalID);
                will(returnValue(click));
                oneOf(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, InstallController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                oneOf(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, InstallController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(adSpace));
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getApplicationID();
                will(returnValue("mismatch"));
            }
        });

        ResponseEntity<String> responseEntity = installController.trackAuthenticatedInstall(authorizationHeader, applicationID, advertiserExternalID, creativeExternalID,
                impressionExternalID);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testInstallController_trackAuthenticatedInstall_duplicate() {
        final String username = randomAlphaNumericString(10);
        final String password = randomAlphaNumericString(10);
        final String authorizationHeader = BasicAuthUtils.generateAuthorizationHeader(username, password);
        final String applicationID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class, "click");
        final Long creativeId = randomLong();
        final Creative creative = mock(Creative.class, "creative");
        final Campaign campaign = mock(Campaign.class, "campaign");
        final Long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class, "adSpace");
        final AdEvent adEvent = mock(AdEvent.class, "adEvent");
        final String clickExternalID = randomAlphaNumericString(10);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class, "publication");
        final long publicationId = randomLong();
        expect(new Expectations() {
            {
                oneOf(trackerProperties).getProperty(with(any(String.class)));
                will(returnValue(password));
                oneOf(clickService).getClickByExternalID(impressionExternalID);
                will(returnValue(click));
                oneOf(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, InstallController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                oneOf(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, InstallController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(adSpace));
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getApplicationID();
                will(returnValue(applicationID));
                oneOf(adEventFactory).newInstance(AdAction.INSTALL);
                will(returnValue(adEvent));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getId();
                will(returnValue(publicationId));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(installService).trackInstall(click);
                will(returnValue(false));
                atLeast(0).of(click).getExternalID();
                will(returnValue(clickExternalID));
            }
        });

        ResponseEntity<String> responseEntity = installController.trackAuthenticatedInstall(authorizationHeader, applicationID, advertiserExternalID, creativeExternalID,
                impressionExternalID);
        assertNotNull(responseEntity);
        // For duplicates we expect a 200 OK response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testInstallController_trackAuthenticatedInstall_allGood() {
        final String username = randomAlphaNumericString(10);
        final String password = randomAlphaNumericString(10);
        final String authorizationHeader = BasicAuthUtils.generateAuthorizationHeader(username, password);
        final String applicationID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class, "click");
        final Long creativeId = randomLong();
        final Creative creative = mock(Creative.class, "creative");
        final Campaign campaign = mock(Campaign.class, "campaign");
        final Long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class, "adSpace");
        final AdEvent adEvent = mock(AdEvent.class, "adEvent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");
        final String clickExternalID = randomAlphaNumericString(10);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class, "publication");
        final long publicationId = randomLong();

        final Map<Long, String> clickDeviceIdentifiers = new HashMap<Long, String>();
        expect(new Expectations() {
            {
                oneOf(trackerProperties).getProperty(with(any(String.class)));
                will(returnValue(password));
                oneOf(clickService).getClickByExternalID(impressionExternalID);
                will(returnValue(click));
                oneOf(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, InstallController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                oneOf(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, InstallController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(adSpace));
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getApplicationID();
                will(returnValue(applicationID));
                oneOf(adEventFactory).newInstance(AdAction.INSTALL);
                will(returnValue(adEvent));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getId();
                will(returnValue(publicationId));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(installService).trackInstall(click);
                will(returnValue(true));
                oneOf(clickService).loadDeviceIdentifiers(click);
                allowing(click).getDeviceIdentifiers();
                will(returnValue(clickDeviceIdentifiers));
                atLeast(0).of(click).getExternalID();
                will(returnValue(clickExternalID));
                oneOf(mapper).map(adEvent);
                will(returnValue(ae));
                oneOf(trackerKafka).logAdEvent(ae);
                allowing(ae).getCreativeId();
                allowing(ae).getAdSpaceId();
            }
        });

        ResponseEntity<String> responseEntity = installController.trackAuthenticatedInstall(authorizationHeader, applicationID, advertiserExternalID, creativeExternalID,
                impressionExternalID);
        assertNotNull(responseEntity);
        // For valid non-duplicates we expect a 201 Created response
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testInstallController_trackAuthenticatedInstall_retargeting_exception() {
        final String username = randomAlphaNumericString(10);
        final String password = randomAlphaNumericString(10);
        final String authorizationHeader = BasicAuthUtils.generateAuthorizationHeader(username, password);
        final String applicationID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
        final Click click = mock(Click.class, "click");
        final Long creativeId = randomLong();
        final Creative creative = mock(Creative.class, "creative");
        final Campaign campaign = mock(Campaign.class, "campaign");
        final Long adSpaceId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class, "adSpace");
        final AdEvent adEvent = mock(AdEvent.class, "adEvent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");
        final String clickExternalID = randomAlphaNumericString(10);

        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class, "publication");
        final long publicationId = randomLong();

        final Map<Long, String> clickDeviceIdentifiers = new HashMap<Long, String>();
        expect(new Expectations() {
            {
                oneOf(trackerProperties).getProperty(with(any(String.class)));
                will(returnValue(password));
                oneOf(clickService).getClickByExternalID(impressionExternalID);
                will(returnValue(click));
                oneOf(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, InstallController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                oneOf(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, InstallController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(adSpace));
                oneOf(creative).getCampaign();
                will(returnValue(campaign));
                oneOf(campaign).getApplicationID();
                will(returnValue(applicationID));
                oneOf(adEventFactory).newInstance(AdAction.INSTALL);
                will(returnValue(adEvent));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getId();
                will(returnValue(publicationId));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(installService).trackInstall(click);
                will(returnValue(true));
                oneOf(clickService).loadDeviceIdentifiers(click);
                allowing(click).getDeviceIdentifiers();
                will(returnValue(clickDeviceIdentifiers));
                allowing(click).getExternalID();
                will(returnValue(clickExternalID));
                oneOf(mapper).map(adEvent);
                will(returnValue(ae));
                oneOf(trackerKafka).logAdEvent(ae);
                allowing(ae).getCreativeId();
                allowing(ae).getAdSpaceId();
            }
        });

        ResponseEntity<String> responseEntity = installController.trackAuthenticatedInstall(authorizationHeader, applicationID, advertiserExternalID, creativeExternalID,
                impressionExternalID);
        assertNotNull(responseEntity);
        // For valid non-duplicates we expect a 201 Created response
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testInstallController05_deDupInstallByAppIdAndUdid() {
        final String appId = randomAlphaNumericString(10).toUpperCase();
        final String udId = randomAlphaNumericString(10).toUpperCase();
        final DeviceIdentifierType dit = mock(DeviceIdentifierType.class);
        final long ditId = randomLong();
        final Click click = null;
        final String claimParam = null;
        final boolean claim = true;
        expect(new Expectations() {
            {
                oneOf(deviceManager).getDeviceIdentifierTypeBySystemName("dpid");
                will(returnValue(dit));
                allowing(dit).getId();
                will(returnValue(ditId));
                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, ditId, DigestUtils.shaHex(udId.toLowerCase()));
                will(returnValue(click));
                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(udId, ditId, DigestUtils.shaHex(appId.toLowerCase()));
                will(returnValue(click));
                oneOf(installService).scheduleInstallRetry(appId, ditId, DigestUtils.shaHex(udId.toLowerCase()), claim);
            }
        });

        Map<String, Object> returnMap = installController.deDupInstallByAppIdAndUdid(appId, udId, claimParam);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertNotNull(returnMap.get("error"));
    }

    @Test
    public void testInstallController06_deDupInstallByAppIdAndUdid() {
        final String appId = randomAlphaNumericString(10);
        final String udId = randomAlphaNumericString(10);
        final DeviceIdentifierType dit = mock(DeviceIdentifierType.class);
        final long ditId = randomLong();
        final Click click = mock(Click.class, "click");
        final boolean clickTracked = false;
        final String clickExternalID = randomAlphaNumericString(10);
        final Long creativeID = randomLong();
        final String claimParam = null;
        expect(new Expectations() {
            {
                oneOf(deviceManager).getDeviceIdentifierTypeBySystemName("dpid");
                will(returnValue(dit));
                allowing(dit).getId();
                will(returnValue(ditId));
                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, ditId, DigestUtils.shaHex(udId.toLowerCase()));
                will(returnValue(null));
                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(udId, ditId, DigestUtils.shaHex(appId.toLowerCase()));
                will(returnValue(click));
                oneOf(clickService).loadDeviceIdentifiers(click);
                oneOf(installService).trackInstall(click);
                will(returnValue(clickTracked));
                atLeast(0).of(click).getExternalID();
                will(returnValue(clickExternalID));
                atLeast(0).of(click).getCreativeId();
                will(returnValue(creativeID));
            }
        });

        Map<String, Object> returnMap = installController.deDupInstallByAppIdAndUdid(appId, udId, claimParam);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertEquals("Duplicate", returnMap.get("error"));
    }

    @Test
    public void testInstallController07_deDupInstallByAppIdAndUdid() {
        final String appId = randomAlphaNumericString(10);
        final String udId = randomAlphaNumericString(10);
        final DeviceIdentifierType dit = mock(DeviceIdentifierType.class);
        final long ditId = randomLong();
        final Click click = mock(Click.class, "click");
        final boolean clickTracked = true;
        final String clickExternalID = randomAlphaNumericString(10);
        final AdEvent adEvent = mock(AdEvent.class, "adEvent");
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");
        final Creative creative = mock(Creative.class, "creative");
        final AdSpace adSpace = mock(AdSpace.class, "adSpace");
        final Long creativeId = randomLong();
        final Long adSpaceId = randomLong();

        final Campaign campaign = mock(Campaign.class, "campaign");
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class, "publication");
        final long publicationId = randomLong();
        final String claimParam = null;

        expect(new Expectations() {
            {
                oneOf(deviceManager).getDeviceIdentifierTypeBySystemName("dpid");
                will(returnValue(dit));
                oneOf(dit).getId();
                will(returnValue(ditId));
                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, ditId, DigestUtils.shaHex(udId.toLowerCase()));
                will(returnValue(click));
                oneOf(installService).trackInstall(click);
                will(returnValue(clickTracked));
                oneOf(clickService).loadDeviceIdentifiers(click);
                oneOf(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, InstallController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                oneOf(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, InstallController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(adSpace));
                atLeast(0).of(click).getExternalID();
                will(returnValue(clickExternalID));
                oneOf(adEventFactory).newInstance(AdAction.INSTALL);
                will(returnValue(adEvent));
                allowing(creative).getId();
                will(returnValue(creativeId));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
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

        Map<String, Object> returnMap = installController.deDupInstallByAppIdAndUdid(appId, udId, claimParam);
        assertNotNull(returnMap);
        assertEquals(1, returnMap.get("success"));
    }

    @Test
    public void testInstallController08_deDupInstallByAppIdAndUdid() {
        final String appId = randomAlphaNumericString(10);
        final String udId = randomAlphaNumericString(10);
        final DeviceIdentifierType dit = mock(DeviceIdentifierType.class);
        final long ditId = randomLong();
        final Click click = mock(Click.class, "click");
        final boolean clickTracked = true;
        final String clickExternalID = randomAlphaNumericString(10);
        final Long creativeId = randomLong();
        final Long adSpaceId = randomLong();
        final String claimParam = null;

        expect(new Expectations() {
            {
                oneOf(deviceManager).getDeviceIdentifierTypeBySystemName("dpid");
                will(returnValue(dit));
                oneOf(dit).getId();
                will(returnValue(ditId));
                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, ditId, DigestUtils.sha1Hex(udId.toLowerCase()));
                will(returnValue(click));
                oneOf(installService).trackInstall(click);
                will(returnValue(clickTracked));
                oneOf(clickService).loadDeviceIdentifiers(click);
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(creativeId, InstallController.CREATIVE_FETCH_STRATEGY);
                will(returnValue(null));
                /*
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(adSpaceId, InstallController.AD_SPACE_FETCH_STRATEGY);
                will(returnValue(null));
                atLeast(0).of(click).getExternalID();
                will(returnValue(clickExternalID));
                */
            }
        });

        Map<String, Object> returnMap = installController.deDupInstallByAppIdAndUdid(appId, udId, claimParam);
        assertNotNull(returnMap);
        assertEquals(0, returnMap.get("success"));
        assertEquals("Internal error", returnMap.get("error"));
    }

    @Test
    public void testDeDupInstallByDeviceIdentifiers01_none_supplied() {
        final String appId = randomAlphaNumericString(10);
        final DeviceIdentifierType dpidDit = mock(DeviceIdentifierType.class, "dpidDit");
        final DeviceIdentifierType odin1Dit = mock(DeviceIdentifierType.class, "odin1Dit");
        final DeviceIdentifierType openudidDit = mock(DeviceIdentifierType.class, "openudidDit");
        final DeviceIdentifierType hifaDit = mock(DeviceIdentifierType.class, "hifaDit");
        final DeviceIdentifierType ifaDit = mock(DeviceIdentifierType.class, "ifaDit");
        final DeviceIdentifierType androidDit = mock(DeviceIdentifierType.class, "androidDit");
        final DeviceIdentifierType udidDit = mock(DeviceIdentifierType.class, "udidDit");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(dpidDit);
                add(odin1Dit);
                add(openudidDit);
                add(hifaDit);
                add(ifaDit);
                add(androidDit);
                add(udidDit);
            }
        };
        final String claimParam = null;

        expect(new Expectations() {
            {
                allowing(dpidDit).getSystemName();
                will(returnValue("dpid"));
                allowing(odin1Dit).getSystemName();
                will(returnValue("odin-1"));
                allowing(openudidDit).getSystemName();
                will(returnValue("openudid"));
                allowing(hifaDit).getSystemName();
                will(returnValue("hifa"));
                allowing(ifaDit).getSystemName();
                will(returnValue("ifa"));
                allowing(androidDit).getSystemName();
                will(returnValue("android"));
                allowing(udidDit).getSystemName();
                will(returnValue("udid"));

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));
                allowing(request).getParameter(with(any(String.class)));
                will(returnValue(null));
            }
        });

        Map<String, Object> response = installController.deDupInstallByDeviceIdentifiers(request, appId, claimParam);
        assertNotNull(response);
        assertEquals(0, response.get("success"));
        assertNotNull(response.get("error"));
    }

    @Test
    public void testDeDupInstallByDeviceIdentifiers02_click_not_found() {
        final String appId = randomAlphaNumericString(10);
        final DeviceIdentifierType dpidDit = mock(DeviceIdentifierType.class, "dpidDit");
        final long dpidDitId = uniqueLong("DeviceIdentifierType.id");
        final DeviceIdentifierType odin1Dit = mock(DeviceIdentifierType.class, "odin1Dit");
        final DeviceIdentifierType openudidDit = mock(DeviceIdentifierType.class, "openudidDit");
        final DeviceIdentifierType hifaDit = mock(DeviceIdentifierType.class, "hifaDit");
        final DeviceIdentifierType ifaDit = mock(DeviceIdentifierType.class, "ifaDit");
        final DeviceIdentifierType androidDit = mock(DeviceIdentifierType.class, "androidDit");
        final DeviceIdentifierType udidDit = mock(DeviceIdentifierType.class, "udidDit");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(dpidDit);
                add(odin1Dit);
                add(openudidDit);
                add(hifaDit);
                add(ifaDit);
                add(androidDit);
                add(udidDit);
            }
        };
        final String dpid = randomHexString(40).toLowerCase(); // will be lowercased anyway
        final String claimParam = null;
        final boolean claim = true;

        expect(new Expectations() {
            {
                allowing(dpidDit).getSystemName();
                will(returnValue("dpid"));
                allowing(dpidDit).getValidationRegex();
                will(returnValue("[0-9,a-z]+"));
                allowing(dpidDit).isSecure();
                will(returnValue(true));
                allowing(dpidDit).getId();
                will(returnValue(dpidDitId));
                allowing(odin1Dit).getSystemName();
                will(returnValue("odin-1"));
                allowing(openudidDit).getSystemName();
                will(returnValue("openudid"));
                allowing(hifaDit).getSystemName();
                will(returnValue("hifa"));
                allowing(ifaDit).getSystemName();
                will(returnValue("ifa"));
                allowing(androidDit).getSystemName();
                will(returnValue("android"));
                allowing(udidDit).getSystemName();
                will(returnValue("udid"));

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));

                oneOf(request).getParameter("d.dpid");
                will(returnValue(dpid));
                oneOf(request).getParameter("d.odin-1");
                will(returnValue(null));
                oneOf(request).getParameter("d.openudid");
                will(returnValue(null));
                oneOf(request).getParameter("d.hifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.ifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.android");
                will(returnValue(null));
                oneOf(request).getParameter("d.udid");
                will(returnValue(null));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, dpidDitId, dpid);
                will(returnValue(null));
                oneOf(deviceManager).getDeviceIdentifierTypeBySystemName("dpid");
                will(returnValue(dpidDit));
                oneOf(installService).scheduleInstallRetry(appId, dpidDitId, dpid, claim);
            }
        });

        Map<String, Object> response = installController.deDupInstallByDeviceIdentifiers(request, appId, claimParam);
        assertNotNull(response);
        assertEquals(0, response.get("success"));
        assertNotNull(response.get("error"));
    }

    @Test
    public void testDeDupInstallByDeviceIdentifiers03_click_found_duplicate() {
        final String appId = randomAlphaNumericString(10);
        final DeviceIdentifierType dpidDit = mock(DeviceIdentifierType.class, "dpidDit");
        final long dpidDitId = uniqueLong("DeviceIdentifierType.id");
        final DeviceIdentifierType odin1Dit = mock(DeviceIdentifierType.class, "odin1Dit");
        final DeviceIdentifierType openudidDit = mock(DeviceIdentifierType.class, "openudidDit");
        final DeviceIdentifierType hifaDit = mock(DeviceIdentifierType.class, "hifaDit");
        final DeviceIdentifierType ifaDit = mock(DeviceIdentifierType.class, "ifaDit");
        final DeviceIdentifierType androidDit = mock(DeviceIdentifierType.class, "androidDit");
        final DeviceIdentifierType udidDit = mock(DeviceIdentifierType.class, "udidDit");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(dpidDit);
                add(odin1Dit);
                add(openudidDit);
                add(hifaDit);
                add(ifaDit);
                add(androidDit);
                add(udidDit);
            }
        };
        final String dpid = randomHexString(40).toLowerCase(); // will be lowercased anyway
        final Click click = mock(Click.class);

        final String claimParam = null;

        expect(new Expectations() {
            {
                allowing(dpidDit).getSystemName();
                will(returnValue("dpid"));
                allowing(dpidDit).getValidationRegex();
                will(returnValue("[0-9,a-z]+"));
                allowing(dpidDit).isSecure();
                will(returnValue(true));
                allowing(dpidDit).getId();
                will(returnValue(dpidDitId));
                allowing(odin1Dit).getSystemName();
                will(returnValue("odin-1"));
                allowing(openudidDit).getSystemName();
                will(returnValue("openudid"));
                allowing(hifaDit).getSystemName();
                will(returnValue("hifa"));
                allowing(ifaDit).getSystemName();
                will(returnValue("ifa"));
                allowing(androidDit).getSystemName();
                will(returnValue("android"));
                allowing(udidDit).getSystemName();
                will(returnValue("udid"));

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));

                oneOf(request).getParameter("d.dpid");
                will(returnValue(dpid));
                oneOf(request).getParameter("d.odin-1");
                will(returnValue(null));
                oneOf(request).getParameter("d.openudid");
                will(returnValue(null));
                oneOf(request).getParameter("d.hifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.ifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.android");
                will(returnValue(null));
                oneOf(request).getParameter("d.udid");
                will(returnValue(null));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, dpidDitId, dpid);
                will(returnValue(click));
                allowing(click).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                oneOf(installService).trackInstall(click);
                will(returnValue(false));
            }
        });

        Map<String, Object> response = installController.deDupInstallByDeviceIdentifiers(request, appId, claimParam);
        assertNotNull(response);
        assertEquals(0, response.get("success"));
        assertNotNull(response.get("error"));
    }

    @Test
    public void testDeDupInstallByDeviceIdentifiers04_click_found_data_dependency() {
        final String appId = randomAlphaNumericString(10);
        final DeviceIdentifierType dpidDit = mock(DeviceIdentifierType.class, "dpidDit");
        final long dpidDitId = uniqueLong("DeviceIdentifierType.id");
        final DeviceIdentifierType odin1Dit = mock(DeviceIdentifierType.class, "odin1Dit");
        final DeviceIdentifierType openudidDit = mock(DeviceIdentifierType.class, "openudidDit");
        final DeviceIdentifierType hifaDit = mock(DeviceIdentifierType.class, "hifaDit");
        final DeviceIdentifierType ifaDit = mock(DeviceIdentifierType.class, "ifaDit");
        final DeviceIdentifierType androidDit = mock(DeviceIdentifierType.class, "androidDit");
        final DeviceIdentifierType udidDit = mock(DeviceIdentifierType.class, "udidDit");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(dpidDit);
                add(odin1Dit);
                add(openudidDit);
                add(hifaDit);
                add(ifaDit);
                add(androidDit);
                add(udidDit);
            }
        };
        final String dpid = randomHexString(40).toLowerCase(); // will be lowercased anyway
        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final long adSpaceId = randomLong();
        final String claimParam = null;

        expect(new Expectations() {
            {
                allowing(dpidDit).getSystemName();
                will(returnValue("dpid"));
                allowing(dpidDit).getValidationRegex();
                will(returnValue("[0-9,a-z]+"));
                allowing(dpidDit).isSecure();
                will(returnValue(true));
                allowing(dpidDit).getId();
                will(returnValue(dpidDitId));
                allowing(odin1Dit).getSystemName();
                will(returnValue("odin-1"));
                allowing(openudidDit).getSystemName();
                will(returnValue("openudid"));
                allowing(hifaDit).getSystemName();
                will(returnValue("hifa"));
                allowing(ifaDit).getSystemName();
                will(returnValue("ifa"));
                allowing(androidDit).getSystemName();
                will(returnValue("android"));
                allowing(udidDit).getSystemName();
                will(returnValue("udid"));

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));

                oneOf(request).getParameter("d.dpid");
                will(returnValue(dpid));
                oneOf(request).getParameter("d.odin-1");
                will(returnValue(null));
                oneOf(request).getParameter("d.openudid");
                will(returnValue(null));
                oneOf(request).getParameter("d.hifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.ifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.android");
                will(returnValue(null));
                oneOf(request).getParameter("d.udid");
                will(returnValue(null));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, dpidDitId, dpid);
                will(returnValue(click));
                oneOf(installService).trackInstall(click);
                will(returnValue(true));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(with(creativeId), with(any(FetchStrategy[].class)));
                will(returnValue(null)); // creative not found
            }
        });

        Map<String, Object> response = installController.deDupInstallByDeviceIdentifiers(request, appId, claimParam);
        assertNotNull(response);
        assertEquals(0, response.get("success"));
        assertNotNull(response.get("error"));
    }

    @Test
    public void testDeDupInstallByDeviceIdentifiers05_click_found_all_good() {
        final String appId = randomAlphaNumericString(10);
        final DeviceIdentifierType dpidDit = mock(DeviceIdentifierType.class, "dpidDit");
        final long dpidDitId = uniqueLong("DeviceIdentifierType.id");
        final DeviceIdentifierType odin1Dit = mock(DeviceIdentifierType.class, "odin1Dit");
        final DeviceIdentifierType openudidDit = mock(DeviceIdentifierType.class, "openudidDit");
        final DeviceIdentifierType hifaDit = mock(DeviceIdentifierType.class, "hifaDit");
        final DeviceIdentifierType ifaDit = mock(DeviceIdentifierType.class, "ifaDit");
        final DeviceIdentifierType androidDit = mock(DeviceIdentifierType.class, "androidDit");
        final DeviceIdentifierType udidDit = mock(DeviceIdentifierType.class, "udidDit");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(dpidDit);
                add(odin1Dit);
                add(openudidDit);
                add(hifaDit);
                add(ifaDit);
                add(androidDit);
                add(udidDit);
            }
        };
        final String dpid = randomHexString(40).toLowerCase(); // will be lowercased anyway
        final Click click = mock(Click.class);
        final Creative creative = mock(Creative.class);
        final long creativeId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final long adSpaceId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();
        final AdEvent adEvent = mock(AdEvent.class);
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");
        final String claimParam = null;

        expect(new Expectations() {
            {
                allowing(dpidDit).getSystemName();
                will(returnValue("dpid"));
                allowing(dpidDit).getValidationRegex();
                will(returnValue("[0-9,a-z]+"));
                allowing(dpidDit).isSecure();
                will(returnValue(true));
                allowing(dpidDit).getId();
                will(returnValue(dpidDitId));
                allowing(odin1Dit).getSystemName();
                will(returnValue("odin-1"));
                allowing(openudidDit).getSystemName();
                will(returnValue("openudid"));
                allowing(hifaDit).getSystemName();
                will(returnValue("hifa"));
                allowing(ifaDit).getSystemName();
                will(returnValue("ifa"));
                allowing(androidDit).getSystemName();
                will(returnValue("android"));
                allowing(udidDit).getSystemName();
                will(returnValue("udid"));

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));

                oneOf(request).getParameter("d.dpid");
                will(returnValue(dpid));
                oneOf(request).getParameter("d.odin-1");
                will(returnValue(null));
                oneOf(request).getParameter("d.openudid");
                will(returnValue(null));
                oneOf(request).getParameter("d.hifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.ifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.android");
                will(returnValue(null));
                oneOf(request).getParameter("d.udid");
                will(returnValue(null));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, dpidDitId, dpid);
                will(returnValue(click));
                oneOf(installService).trackInstall(click);
                will(returnValue(true));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(with(creativeId), with(any(FetchStrategy[].class)));
                will(returnValue(creative));
                allowing(creative).getId();
                will(returnValue(creativeId));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(with(adSpaceId), with(any(FetchStrategy[].class)));
                will(returnValue(adSpace));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getId();
                will(returnValue(publicationId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                oneOf(adEventFactory).newInstance(AdAction.INSTALL);
                will(returnValue(adEvent));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(mapper).map(adEvent);
                will(returnValue(ae));
                oneOf(trackerKafka).logAdEvent(ae);
                allowing(ae).getCreativeId();
                allowing(ae).getAdSpaceId();
            }
        });

        Map<String, Object> response = installController.deDupInstallByDeviceIdentifiers(request, appId, claimParam);
        assertNotNull(response);
        assertEquals(1, response.get("success"));
        assertNull(response.get("error"));
    }

    @Test
    public void testDeDupInstallByDeviceIdentifiers06_click_found_retargeting_exception() {
        final String appId = randomAlphaNumericString(10);
        final DeviceIdentifierType dpidDit = mock(DeviceIdentifierType.class, "dpidDit");
        final long dpidDitId = uniqueLong("DeviceIdentifierType.id");
        final DeviceIdentifierType odin1Dit = mock(DeviceIdentifierType.class, "odin1Dit");
        final DeviceIdentifierType openudidDit = mock(DeviceIdentifierType.class, "openudidDit");
        final DeviceIdentifierType hifaDit = mock(DeviceIdentifierType.class, "hifaDit");
        final DeviceIdentifierType ifaDit = mock(DeviceIdentifierType.class, "ifaDit");
        final DeviceIdentifierType androidDit = mock(DeviceIdentifierType.class, "androidDit");
        final DeviceIdentifierType udidDit = mock(DeviceIdentifierType.class, "udidDit");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(dpidDit);
                add(odin1Dit);
                add(openudidDit);
                add(hifaDit);
                add(ifaDit);
                add(androidDit);
                add(udidDit);
            }
        };
        final String dpid = randomHexString(40).toLowerCase(); // will be lowercased anyway
        final Click click = mock(Click.class);
        final Creative creative = mock(Creative.class);
        final long creativeId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final long adSpaceId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();
        final AdEvent adEvent = mock(AdEvent.class);
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");
        final String claimParam = null;

        expect(new Expectations() {
            {
                allowing(dpidDit).getSystemName();
                will(returnValue("dpid"));
                allowing(dpidDit).getValidationRegex();
                will(returnValue("[0-9,a-z]+"));
                allowing(dpidDit).isSecure();
                will(returnValue(true));
                allowing(dpidDit).getId();
                will(returnValue(dpidDitId));
                allowing(odin1Dit).getSystemName();
                will(returnValue("odin-1"));
                allowing(openudidDit).getSystemName();
                will(returnValue("openudid"));
                allowing(hifaDit).getSystemName();
                will(returnValue("hifa"));
                allowing(ifaDit).getSystemName();
                will(returnValue("ifa"));
                allowing(androidDit).getSystemName();
                will(returnValue("android"));
                allowing(udidDit).getSystemName();
                will(returnValue("udid"));

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));

                oneOf(request).getParameter("d.dpid");
                will(returnValue(dpid));
                oneOf(request).getParameter("d.odin-1");
                will(returnValue(null));
                oneOf(request).getParameter("d.openudid");
                will(returnValue(null));
                oneOf(request).getParameter("d.hifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.ifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.android");
                will(returnValue(null));
                oneOf(request).getParameter("d.udid");
                will(returnValue(null));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, dpidDitId, dpid);
                will(returnValue(click));
                oneOf(installService).trackInstall(click);
                will(returnValue(true));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(with(creativeId), with(any(FetchStrategy[].class)));
                will(returnValue(creative));
                allowing(creative).getId();
                will(returnValue(creativeId));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(with(adSpaceId), with(any(FetchStrategy[].class)));
                will(returnValue(adSpace));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getId();
                will(returnValue(publicationId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                oneOf(adEventFactory).newInstance(AdAction.INSTALL);
                will(returnValue(adEvent));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(mapper).map(adEvent);
                will(returnValue(ae));
                oneOf(trackerKafka).logAdEvent(ae);
                allowing(ae).getCreativeId();
                allowing(ae).getAdSpaceId();
            }
        });

        Map<String, Object> response = installController.deDupInstallByDeviceIdentifiers(request, appId, claimParam);
        assertNotNull(response);
        assertEquals(1, response.get("success"));
        assertNull(response.get("error"));
    }

    @Test
    public void testPromoteDeviceIdentifiers01_dpid_already_supplied() {
        final String dpid = randomHexString(40);
        final Map<String, String> suppliedDeviceIdentifiers = new HashMap<String, String>() {
            {
                put("dpid", dpid);
            }
        };

        InstallController.promoteDeviceIdentifiers(suppliedDeviceIdentifiers);

        // Make sure no promotions occurred
        assertEquals(1, suppliedDeviceIdentifiers.size());
        assertEquals(dpid, suppliedDeviceIdentifiers.get("dpid"));
    }

    @Test
    public void testPromoteDeviceIdentifiers02_android_to_dpid() {
        final String android = randomHexString(16);
        final Map<String, String> suppliedDeviceIdentifiers = new HashMap<String, String>() {
            {
                put("android", android);
            }
        };

        InstallController.promoteDeviceIdentifiers(suppliedDeviceIdentifiers);
        // Make sure it got promoted to dpid
        assertEquals(DigestUtils.shaHex(android), suppliedDeviceIdentifiers.get("dpid"));
        // Make sure android got removed
        assertNull(suppliedDeviceIdentifiers.get("android"));
    }

    @Test
    public void testPromoteDeviceIdentifiers03_udid_to_dpid() {
        final String udid = randomHexString(40);
        final Map<String, String> suppliedDeviceIdentifiers = new HashMap<String, String>() {
            {
                put("udid", udid);
            }
        };

        InstallController.promoteDeviceIdentifiers(suppliedDeviceIdentifiers);
        // Make sure it got promoted to dpid
        assertEquals(DigestUtils.shaHex(udid), suppliedDeviceIdentifiers.get("dpid"));
        // Make sure udid got removed
        assertNull(suppliedDeviceIdentifiers.get("udid"));
    }

    @Test
    public void testPromoteDeviceIdentifiers04_odin1_already_supplied() {
        final String odin1 = randomHexString(40);
        final Map<String, String> suppliedDeviceIdentifiers = new HashMap<String, String>() {
            {
                put("odin1", odin1);
            }
        };

        InstallController.promoteDeviceIdentifiers(suppliedDeviceIdentifiers);

        // Make sure no promotions occurred
        assertEquals(1, suppliedDeviceIdentifiers.size());
        assertEquals(odin1, suppliedDeviceIdentifiers.get("odin1"));
    }

    @Test
    public void testPromoteDeviceIdentifiers05_android_to_odin1() {
        final String android = randomHexString(16);
        final Map<String, String> suppliedDeviceIdentifiers = new HashMap<String, String>() {
            {
                put("android", android);
            }
        };

        InstallController.promoteDeviceIdentifiers(suppliedDeviceIdentifiers);
        // Make sure it got promoted to odin1
        assertEquals(DigestUtils.shaHex(android), suppliedDeviceIdentifiers.get("odin-1"));
        // Make sure android got removed
        assertNull(suppliedDeviceIdentifiers.get("android"));
    }

    @Test
    public void testPromoteDeviceIdentifiers06_none_supplied() {
        final Map<String, String> suppliedDeviceIdentifiers = new HashMap<String, String>();

        InstallController.promoteDeviceIdentifiers(suppliedDeviceIdentifiers);

        // No promotions should have occurred since there's nothing to promote
        assertTrue(suppliedDeviceIdentifiers.isEmpty());
    }

    @Test
    public void testPromoteDeviceIdentifiers07_no_promotions_applicable() {
        final String openudid = randomHexString(40);
        final Map<String, String> suppliedDeviceIdentifiers = new HashMap<String, String>() {
            {
                put("openudid", openudid);
            }
        };

        InstallController.promoteDeviceIdentifiers(suppliedDeviceIdentifiers);

        // No promotions should have occurred
        assertEquals(1, suppliedDeviceIdentifiers.size());
        assertEquals(openudid, suppliedDeviceIdentifiers.get("openudid"));
    }

    @Test
    public void testPromoteDeviceIdentifiers08_ifa_to_hifa() {
        final String ifa = randomHexString(32);
        final Map<String, String> suppliedDeviceIdentifiers = new HashMap<String, String>() {
            {
                put("ifa", ifa);
            }
        };

        InstallController.promoteDeviceIdentifiers(suppliedDeviceIdentifiers);
        // Make sure it got promoted to hifa
        assertEquals(DigestUtils.shaHex(ifa), suppliedDeviceIdentifiers.get("hifa"));
        // Make sure ifa got removed
        assertNull(suppliedDeviceIdentifiers.get("ifa"));
    }

    @Test
    public void test_canClaim() {
        assertTrue(InstallController.canClaim(null));
        assertTrue(InstallController.canClaim("1"));
        assertFalse(InstallController.canClaim("0"));
    }

    @Test
    public void testDeDupInstallByDeviceIdentifiers07_claim_1() {
        final String appId = randomAlphaNumericString(10);
        final DeviceIdentifierType dpidDit = mock(DeviceIdentifierType.class, "dpidDit");
        final long dpidDitId = uniqueLong("DeviceIdentifierType.id");
        final DeviceIdentifierType odin1Dit = mock(DeviceIdentifierType.class, "odin1Dit");
        final DeviceIdentifierType openudidDit = mock(DeviceIdentifierType.class, "openudidDit");
        final DeviceIdentifierType hifaDit = mock(DeviceIdentifierType.class, "hifaDit");
        final DeviceIdentifierType ifaDit = mock(DeviceIdentifierType.class, "ifaDit");
        final DeviceIdentifierType androidDit = mock(DeviceIdentifierType.class, "androidDit");
        final DeviceIdentifierType udidDit = mock(DeviceIdentifierType.class, "udidDit");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(dpidDit);
                add(odin1Dit);
                add(openudidDit);
                add(hifaDit);
                add(ifaDit);
                add(androidDit);
                add(udidDit);
            }
        };
        final String dpid = randomHexString(40).toLowerCase(); // will be lowercased anyway
        final Click click = mock(Click.class);
        final Creative creative = mock(Creative.class);
        final long creativeId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final long adSpaceId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();
        final AdEvent adEvent = mock(AdEvent.class);
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");
        final String claimParam = "1";
        
        expect(new Expectations() {
            {
                allowing(dpidDit).getSystemName();
                will(returnValue("dpid"));
                allowing(dpidDit).getValidationRegex();
                will(returnValue("[0-9,a-z]+"));
                allowing(dpidDit).isSecure();
                will(returnValue(true));
                allowing(dpidDit).getId();
                will(returnValue(dpidDitId));
                allowing(odin1Dit).getSystemName();
                will(returnValue("odin-1"));
                allowing(openudidDit).getSystemName();
                will(returnValue("openudid"));
                allowing(hifaDit).getSystemName();
                will(returnValue("hifa"));
                allowing(ifaDit).getSystemName();
                will(returnValue("ifa"));
                allowing(androidDit).getSystemName();
                will(returnValue("android"));
                allowing(udidDit).getSystemName();
                will(returnValue("udid"));

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));

                oneOf(request).getParameter("d.dpid");
                will(returnValue(dpid));
                oneOf(request).getParameter("d.odin-1");
                will(returnValue(null));
                oneOf(request).getParameter("d.openudid");
                will(returnValue(null));
                oneOf(request).getParameter("d.hifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.ifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.android");
                will(returnValue(null));
                oneOf(request).getParameter("d.udid");
                will(returnValue(null));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, dpidDitId, dpid);
                will(returnValue(click));
                oneOf(installService).trackInstall(click);
                will(returnValue(true));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(with(creativeId), with(any(FetchStrategy[].class)));
                will(returnValue(creative));
                allowing(creative).getId();
                will(returnValue(creativeId));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(with(adSpaceId), with(any(FetchStrategy[].class)));
                will(returnValue(adSpace));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getId();
                will(returnValue(publicationId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                oneOf(adEventFactory).newInstance(AdAction.INSTALL);
                will(returnValue(adEvent));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(mapper).map(adEvent);
                will(returnValue(ae));
                oneOf(trackerKafka).logAdEvent(ae);
                allowing(ae).getCreativeId();
                allowing(ae).getAdSpaceId();
            }
        });

        Map<String, Object> response = installController.deDupInstallByDeviceIdentifiers(request, appId, claimParam);
        assertNotNull(response);
        assertEquals(1, response.get("success"));
        assertNull(response.get("error"));
    }

    @Test
    public void testDeDupInstallByDeviceIdentifiers08_claim_0() {
        final String appId = randomAlphaNumericString(10);
        final DeviceIdentifierType dpidDit = mock(DeviceIdentifierType.class, "dpidDit");
        final long dpidDitId = uniqueLong("DeviceIdentifierType.id");
        final DeviceIdentifierType odin1Dit = mock(DeviceIdentifierType.class, "odin1Dit");
        final DeviceIdentifierType openudidDit = mock(DeviceIdentifierType.class, "openudidDit");
        final DeviceIdentifierType hifaDit = mock(DeviceIdentifierType.class, "hifaDit");
        final DeviceIdentifierType ifaDit = mock(DeviceIdentifierType.class, "ifaDit");
        final DeviceIdentifierType androidDit = mock(DeviceIdentifierType.class, "androidDit");
        final DeviceIdentifierType udidDit = mock(DeviceIdentifierType.class, "udidDit");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(dpidDit);
                add(odin1Dit);
                add(openudidDit);
                add(hifaDit);
                add(ifaDit);
                add(androidDit);
                add(udidDit);
            }
        };
        final String dpid = randomHexString(40).toLowerCase(); // will be lowercased anyway
        final Click click = mock(Click.class);
        final String claimParam = "0";

        expect(new Expectations() {
            {
                allowing(dpidDit).getSystemName();
                will(returnValue("dpid"));
                allowing(dpidDit).getValidationRegex();
                will(returnValue("[0-9,a-z]+"));
                allowing(dpidDit).isSecure();
                will(returnValue(true));
                allowing(dpidDit).getId();
                will(returnValue(dpidDitId));
                allowing(odin1Dit).getSystemName();
                will(returnValue("odin-1"));
                allowing(openudidDit).getSystemName();
                will(returnValue("openudid"));
                allowing(hifaDit).getSystemName();
                will(returnValue("hifa"));
                allowing(ifaDit).getSystemName();
                will(returnValue("ifa"));
                allowing(androidDit).getSystemName();
                will(returnValue("android"));
                allowing(udidDit).getSystemName();
                will(returnValue("udid"));

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));

                oneOf(request).getParameter("d.dpid");
                will(returnValue(dpid));
                oneOf(request).getParameter("d.odin-1");
                will(returnValue(null));
                oneOf(request).getParameter("d.openudid");
                will(returnValue(null));
                oneOf(request).getParameter("d.hifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.ifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.android");
                will(returnValue(null));
                oneOf(request).getParameter("d.udid");
                will(returnValue(null));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, dpidDitId, dpid);
                will(returnValue(click));
            }
        });

        Map<String, Object> response = installController.deDupInstallByDeviceIdentifiers(request, appId, claimParam);
        assertNotNull(response);
        assertEquals(1, response.get("success"));
        assertNull(response.get("error"));
    }

    @Test
    public void testDeDupInstallByAdTruth_no_AdTruthData() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final String claim = null;
        final String appId = randomAlphaNumericString(10);
        final String adTruthRedirectHarness = randomAlphaNumericString(10);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>();
        final Map<String, Object> model = new LinkedHashMap<>();
        model.put("app_id", appId);
        model.put("success", 0);
        model.put("error", "Unrecognized device identifier(s)");
        final ModelAndView mandv = new ModelAndView(adTruthRedirectHarness, model);
        expect(new Expectations() {
            {
                oneOf(request).getParameter(with(any(String.class)));
                will(returnValue(null));
                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));
                oneOf(htmlView).render(with(model));
                will(returnValue(mandv));
            }
        });

        installController.deDupInstallByAdTruth(request, response, appId, claim);
    }

    @Test
    public void testDeDupInstallByAdTruth_no_AdTruthData_success_with_DeviceIdentifiers() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final String claim = null;
        final String appId = randomAlphaNumericString(10);
        final String adTruthRedirectHarness = randomAlphaNumericString(10);
        final DeviceIdentifierType dpidDit = mock(DeviceIdentifierType.class, "dpidDit");
        final long dpidDitId = uniqueLong("DeviceIdentifierType.id");
        final DeviceIdentifierType odin1Dit = mock(DeviceIdentifierType.class, "odin1Dit");
        final DeviceIdentifierType openudidDit = mock(DeviceIdentifierType.class, "openudidDit");
        final DeviceIdentifierType hifaDit = mock(DeviceIdentifierType.class, "hifaDit");
        final DeviceIdentifierType ifaDit = mock(DeviceIdentifierType.class, "ifaDit");
        final DeviceIdentifierType androidDit = mock(DeviceIdentifierType.class, "androidDit");
        final DeviceIdentifierType udidDit = mock(DeviceIdentifierType.class, "udidDit");

        final String dpid = randomHexString(40).toLowerCase(); // will be lowercased anyway

        final Click click = mock(Click.class);
        final long creativeId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final long adSpaceId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();
        final AdEvent adEvent = mock(AdEvent.class);
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");
        final Creative creative = mock(Creative.class);
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(dpidDit);
                add(odin1Dit);
                add(openudidDit);
                add(hifaDit);
                add(ifaDit);
                add(androidDit);
                add(udidDit);
            }
        };
        final Map<String, Object> model = new LinkedHashMap<>();
        model.put("success", 1);
        final ModelAndView mandv = new ModelAndView(adTruthRedirectHarness, model);
        expect(new Expectations() {
            {
                allowing(dpidDit).getSystemName();
                will(returnValue("dpid"));
                allowing(dpidDit).getValidationRegex();
                will(returnValue("[0-9,a-z]+"));
                allowing(dpidDit).isSecure();
                will(returnValue(true));
                allowing(dpidDit).getId();
                will(returnValue(dpidDitId));
                allowing(odin1Dit).getSystemName();
                will(returnValue("odin-1"));
                allowing(openudidDit).getSystemName();
                will(returnValue("openudid"));
                allowing(hifaDit).getSystemName();
                will(returnValue("hifa"));
                allowing(ifaDit).getSystemName();
                will(returnValue("ifa"));
                allowing(androidDit).getSystemName();
                will(returnValue("android"));
                allowing(udidDit).getSystemName();
                will(returnValue("udid"));

                oneOf(request).getParameter("d.dpid");
                will(returnValue(dpid));
                oneOf(request).getParameter("d.odin-1");
                will(returnValue(null));
                oneOf(request).getParameter("d.openudid");
                will(returnValue(null));
                oneOf(request).getParameter("d.hifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.ifa");
                will(returnValue(null));
                oneOf(request).getParameter("d.android");
                will(returnValue(null));
                oneOf(request).getParameter("d.udid");
                will(returnValue(null));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, dpidDitId, dpid);
                will(returnValue(click));

                oneOf(installService).trackInstall(click);
                will(returnValue(true));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(with(creativeId), with(any(FetchStrategy[].class)));
                will(returnValue(creative));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(with(adSpaceId), with(any(FetchStrategy[].class)));
                will(returnValue(adSpace));
                oneOf(adEventFactory).newInstance(AdAction.INSTALL);
                will(returnValue(adEvent));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getId();
                will(returnValue(publicationId));
                allowing(creative).getId();
                will(returnValue(creativeId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(mapper).map(adEvent);
                will(returnValue(ae));
                oneOf(trackerKafka).logAdEvent(ae);
                allowing(ae).getCreativeId();
                allowing(ae).getAdSpaceId();

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));
                oneOf(request).getParameter(with(any(String.class)));
                will(returnValue(null));
                oneOf(jsonView).render(with(model), with(response));
                will(returnValue(mandv));
            }
        });

        installController.deDupInstallByAdTruth(request, response, appId, claim);
    }

    @Test
    public void testDeDupInstallByAdTruth_with_AdTruthData() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final Enumeration headerNames = mock(Enumeration.class);
        final String claim = null;
        final String appId = randomAlphaNumericString(10);

        final DeviceIdentifierType atidDit = mock(DeviceIdentifierType.class, "atidDit");
        final long atidDitId = uniqueLong("DeviceIdentifierType.id");
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(atidDit);
            }
        };

        final Click click = mock(Click.class);
        final Creative creative = mock(Creative.class);
        final long creativeId = randomLong();
        final AdSpace adSpace = mock(AdSpace.class);
        final long adSpaceId = randomLong();
        final Campaign campaign = mock(Campaign.class);
        final long campaignId = randomLong();
        final Publication publication = mock(Publication.class);
        final long publicationId = randomLong();
        final AdEvent adEvent = mock(AdEvent.class);
        final net.byyd.archive.model.v1.AdEvent ae = mock(net.byyd.archive.model.v1.AdEvent.class, "adevent2");

        final ModelAndView mandv = mock(ModelAndView.class, "mandv");

        expect(new Expectations() {
            {
                allowing(atidDit).getSystemName();
                will(returnValue("atid"));
                allowing(atidDit).isSecure();
                will(returnValue(true));
                allowing(atidDit).getId();
                will(returnValue(atidDitId));
                oneOf(request).getParameter(with(any(String.class)));
                will(returnValue("mock_data"));

                oneOf(request).getHeader(with(any(String.class)));
                will(returnValue("header2Value"));
                oneOf(request).getHeaderNames();
                will(returnValue(headerNames));
                atMost(1).of(headerNames).hasMoreElements();
                will(returnValue(true));
                atMost(1).of(headerNames).hasMoreElements();
                will(returnValue(false));
                oneOf(headerNames).nextElement();
                will(returnValue("header1"));
                oneOf(request).getHeader(with(any(String.class)));
                will(returnValue("header1Value"));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(with(any(String.class)), with(any(Long.class)), with(any(String.class)));
                will(returnValue(click));

                oneOf(installService).trackInstall(click);
                will(returnValue(true));
                allowing(click).getCreativeId();
                will(returnValue(creativeId));
                oneOf(creativeManager).getCreativeById(with(creativeId), with(any(FetchStrategy[].class)));
                will(returnValue(creative));
                allowing(click).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(publicationManager).getAdSpaceById(with(adSpaceId), with(any(FetchStrategy[].class)));
                will(returnValue(adSpace));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(publication).getId();
                will(returnValue(publicationId));
                allowing(creative).getId();
                will(returnValue(creativeId));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                oneOf(adEventFactory).newInstance(AdAction.INSTALL);
                will(returnValue(adEvent));
                oneOf(adEvent).populate(click, campaignId, publicationId);
                oneOf(mapper).map(adEvent);
                will(returnValue(ae));
                oneOf(trackerKafka).logAdEvent(ae);
                allowing(ae).getCreativeId();
                allowing(ae).getAdSpaceId();
                oneOf(deviceManager).getDeviceIdentifierTypeBySystemName("atid");
                will(returnValue(atidDit));
                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));
                oneOf(request).getParameter(with(any(String.class)));
                will(returnValue(null));
                oneOf(jsonView).render(with(any(Map.class)), with(any(HttpServletResponse.class)));
                will(returnValue(mandv));
            }
        });

        installController.deDupInstallByAdTruth(request, response, appId, claim);
    }

    @Test
    public void testDeDupInstallByAdTruth_with_AdTruthData_ScheduleInstallRetry_With_Atid() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final Enumeration headerNames = mock(Enumeration.class);
        final boolean claim = true;
        final String claimParam = null;
        final String appId = randomAlphaNumericString(10);

        // have to hard code for the atid value returned from a static method
        // not sure how to implement that in jmock. atid is fetched via a call DeviceIdentifierUtils.getAtid(request, adTruthData);
        final String atid = "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709"; //randomHexString(40).toLowerCase();
        final DeviceIdentifierType atidDit = mock(DeviceIdentifierType.class, "atidDit");
        final long atidDitId = uniqueLong("DeviceIdentifierType.id");
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(atidDit);
            }
        };

        final Map<String, Object> model = new LinkedHashMap<>();
        model.put("success", 0);
        model.put("error", "Unrecognized device identifier(s)");

        final ModelAndView mandv = mock(ModelAndView.class, "mandv");
        expect(new Expectations() {
            {
                allowing(atidDit).getSystemName();
                will(returnValue("atid"));
                allowing(atidDit).isSecure();
                will(returnValue(true));
                allowing(atidDit).getId();
                will(returnValue(atidDitId));
                oneOf(request).getParameter("d.adtruth_data");
                will(returnValue("mock_data"));
                oneOf(request).getHeader(with(any(String.class)));
                will(returnValue("header1Value"));

                oneOf(request).getHeaderNames();
                will(returnValue(headerNames));
                atMost(1).of(headerNames).hasMoreElements();
                will(returnValue(true));
                atMost(1).of(headerNames).hasMoreElements();
                will(returnValue(false));
                oneOf(headerNames).nextElement();
                will(returnValue("header1"));
                oneOf(request).getHeader(with(any(String.class)));
                will(returnValue("header1Value"));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(appId, atidDitId, atid);
                will(returnValue(null));
                oneOf(deviceManager).getDeviceIdentifierTypeBySystemName("atid");
                will(returnValue(atidDit));
                oneOf(deviceManager).getDeviceIdentifierTypeBySystemName("atid");
                will(returnValue(atidDit));
                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));
                oneOf(request).getParameter(with(any(String.class)));
                will(returnValue(null));
                oneOf(installService).scheduleInstallRetry(appId, atidDitId, atid, claim);
                oneOf(jsonView).render(with(model), with(any(HttpServletResponse.class)));
                will(returnValue(mandv));

            }
        });

        installController.deDupInstallByAdTruth(request, response, appId, claimParam);
    }

    @Test
    public void testDeDupInstallByAdTruth_with_AdTruthData_DuplicateException() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final Enumeration headerNames = mock(Enumeration.class);
        final String claim = null;
        final String appId = randomAlphaNumericString(10);
        final String clickExternalID = randomAlphaNumericString(10);

        final DeviceIdentifierType atidDit = mock(DeviceIdentifierType.class, "atidDit");
        final long atidDitId = uniqueLong("DeviceIdentifierType.id");
        final List<DeviceIdentifierType> allDeviceIdentifierTypes = new ArrayList<DeviceIdentifierType>() {
            {
                add(atidDit);
            }
        };

        final Click click = mock(Click.class);

        final ModelAndView mandv = mock(ModelAndView.class, "mandv");
        expect(new Expectations() {
            {
                allowing(atidDit).getSystemName();
                will(returnValue("atid"));
                allowing(atidDit).isSecure();
                will(returnValue(true));
                allowing(atidDit).getId();
                will(returnValue(atidDitId));
                oneOf(request).getParameter(with(any(String.class)));
                will(returnValue("mock_data"));
                oneOf(request).getHeader(with(any(String.class)));
                will(returnValue("header1Value"));

                oneOf(request).getHeaderNames();
                will(returnValue(headerNames));
                atMost(1).of(headerNames).hasMoreElements();
                will(returnValue(true));
                atMost(1).of(headerNames).hasMoreElements();
                will(returnValue(false));
                oneOf(headerNames).nextElement();
                will(returnValue("header1"));
                oneOf(request).getHeader(with(any(String.class)));
                will(returnValue("header1Value"));

                oneOf(clickService).getClickByAppIdAndDeviceIdentifier(with(any(String.class)), with(any(Long.class)), with(any(String.class)));
                will(returnValue(click));

                oneOf(installService).trackInstall(click);
                will(returnValue(false));

                oneOf(deviceManager).getAllDeviceIdentifierTypes();
                will(returnValue(allDeviceIdentifierTypes));
                oneOf(request).getParameter(with(any(String.class)));
                will(returnValue(null));
                oneOf(deviceManager).getDeviceIdentifierTypeBySystemName("atid");
                will(returnValue(atidDit));
                oneOf(jsonView).render(with(any(Map.class)), with(any(HttpServletResponse.class)));
                will(returnValue(mandv));
                oneOf(click).getExternalID();
                will(returnValue(clickExternalID));
            }
        });

        installController.deDupInstallByAdTruth(request, response, appId, claim);
    }
}

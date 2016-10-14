package com.adfonic.adserver.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.rtb.impl.AdsquareWorker;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;

public class TestClickUtilsImpl extends BaseAdserverTest {
    private ClickUtils clickUtilsImpl;
    private BackupLogger backupLogger;
    private KryoManager kryoManager;
    private AdServerStats adServerStats;
    private TargetingContext targetingContext;
    private DomainCache domainCache;
    private AdsquareWorker adsquareWorker;
    private DeviceIdentifierTypeDto dpidDeviceIdentifierType;
    private DeviceIdentifierTypeDto odin1DeviceIdentifierType;
    private DeviceIdentifierTypeDto openudidDeviceIdentifierType;
    private DeviceIdentifierTypeDto hifaDeviceIdentifierType;
    private DeviceIdentifierTypeDto adidDeviceIdentifierType;
    private DeviceIdentifierTypeDto adid_md5DeviceIdentifierType;
    private DeviceIdentifierTypeDto gouidDeviceIdentifierType;
    private DeviceIdentifierTypeDto idfaDeviceIdentifierType;
    private DeviceIdentifierTypeDto idfa_md5DeviceIdentifierType;
    private long dpidDeviceIdentifierTypeId;
    private long odin1DeviceIdentifierTypeId;
    private long openudidDeviceIdentifierTypeId;
    private long hifaDeviceIdentifierTypeId;
    private long adidDeviceIdentifierTypeId;
    private long adid_md5DeviceIdentifierTypeId;
    private long gouidDeviceIdentifierTypeId;
    private long idfaDeviceIdentifierTypeId;
    private long idfa_md5DeviceIdentifierTypeId;

    @Before
    public void initTests() {
        adServerStats = mock(AdServerStats.class);
        backupLogger = mock(BackupLogger.class);
        kryoManager = mock(KryoManager.class);
        adsquareWorker = mock(AdsquareWorker.class);
        clickUtilsImpl = new ClickUtils("^[^:]+:/(/|/.+\\.)yospace\\.com[/:].+$");
        inject(clickUtilsImpl, "astats", adServerStats);
        inject(clickUtilsImpl, "kryoManager", kryoManager);
        inject(clickUtilsImpl, "backupLogger", backupLogger);
        inject(clickUtilsImpl, "adsquareWorker", adsquareWorker);

        targetingContext = mock(TargetingContext.class);
        domainCache = mock(DomainCache.class);
        dpidDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "dpidDeviceIdentifierType");
        odin1DeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "odin1DeviceIdentifierType");
        openudidDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "openudidDeviceIdentifierType");
        hifaDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "hifaDeviceIdentifierType");
        adidDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "adidDeviceIdentifierType");
        adid_md5DeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "adid_md5DeviceIdentifierType");
        gouidDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "gouidDeviceIdentifierType");
        idfaDeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "idfaDeviceIdentifierType");
        idfa_md5DeviceIdentifierType = mock(DeviceIdentifierTypeDto.class, "idfa_md5DeviceIdentifierType");
        dpidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
        odin1DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
        openudidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
        hifaDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
        adidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
        adid_md5DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
        gouidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
        idfaDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
        idfa_md5DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    }

    // These expectations are common to many of the unit test points in this class.
    // By using a base class we can shrink the size of this class
    private class CommonExpectations extends Expectations {
        {
            allowing(targetingContext).getDomainCache();
            will(returnValue(domainCache));
            allowing(domainCache).getDeviceIdentifierTypeBySystemName("dpid");
            will(returnValue(dpidDeviceIdentifierType));
            allowing(domainCache).getDeviceIdentifierTypeBySystemName("odin-1");
            will(returnValue(odin1DeviceIdentifierType));
            allowing(domainCache).getDeviceIdentifierTypeBySystemName("openudid");
            will(returnValue(openudidDeviceIdentifierType));
            allowing(domainCache).getDeviceIdentifierTypeBySystemName("hifa");
            will(returnValue(hifaDeviceIdentifierType));
            allowing(domainCache).getDeviceIdentifierTypeBySystemName("adid");
            will(returnValue(adidDeviceIdentifierType));
            allowing(domainCache).getDeviceIdentifierTypeBySystemName("adid_md5");
            will(returnValue(adid_md5DeviceIdentifierType));
            allowing(domainCache).getDeviceIdentifierTypeBySystemName("gouid");
            will(returnValue(gouidDeviceIdentifierType));
            allowing(domainCache).getDeviceIdentifierTypeBySystemName("idfa");
            will(returnValue(idfaDeviceIdentifierType));
            allowing(domainCache).getDeviceIdentifierTypeBySystemName("idfa_md5");
            will(returnValue(idfa_md5DeviceIdentifierType));
            allowing(dpidDeviceIdentifierType).getId();
            will(returnValue(dpidDeviceIdentifierTypeId));
            allowing(odin1DeviceIdentifierType).getId();
            will(returnValue(odin1DeviceIdentifierTypeId));
            allowing(openudidDeviceIdentifierType).getId();
            will(returnValue(openudidDeviceIdentifierTypeId));
            allowing(hifaDeviceIdentifierType).getId();
            will(returnValue(hifaDeviceIdentifierTypeId));
            allowing(adidDeviceIdentifierType).getId();
            will(returnValue(adidDeviceIdentifierTypeId));
            allowing(adid_md5DeviceIdentifierType).getId();
            will(returnValue(adid_md5DeviceIdentifierTypeId));
            allowing(gouidDeviceIdentifierType).getId();
            will(returnValue(gouidDeviceIdentifierTypeId));
            allowing(idfaDeviceIdentifierType).getId();
            will(returnValue(idfaDeviceIdentifierTypeId));
            allowing(idfa_md5DeviceIdentifierType).getId();
            will(returnValue(idfa_md5DeviceIdentifierTypeId));
        }
    }

    @Test
    public void testClickUtilsImpl01_redirectToFallbackUrl() throws IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final HttpServletResponse response = mock(HttpServletResponse.class, "response");
        final String fallBackurl = "http://www.something.com/fallback.html";
        expect(new Expectations() {
            {
                oneOf(response).sendRedirect(fallBackurl);
            }
        });
        inject(clickUtilsImpl, "fallbackURL", fallBackurl);
        clickUtilsImpl.redirectToFallbackUrl(request, response);
    }

    @Test
    public void testClickUtilsImpl02_redirectToFallbackUrl() throws IOException {
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final HttpServletResponse response = mock(HttpServletResponse.class, "response");
        final String fallBackurl = "fallback.html";
        final String contextPath = "http://www.something.com/";
        final String expectedUrl = "http://www.something.com/fallback.html";
        expect(new Expectations() {
            {
                oneOf(request).getContextPath();
                will(returnValue(contextPath));
                oneOf(response).sendRedirect(expectedUrl);
            }
        });
        inject(clickUtilsImpl, "fallbackURL", fallBackurl);
        clickUtilsImpl.redirectToFallbackUrl(request, response);
    }

    @Test
    public void testClickUtilsImpl03_trackClick() throws IOException {
        AdSpaceDto adSpace = null;
        CreativeDto creative = mock(CreativeDto.class, "creative");
        ;
        final Impression impression = mock(Impression.class);
        final String impressionExternalID = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(backupLogger).logClickFailure(impression, "no AdSpace", targetingContext);
            }
        });
        clickUtilsImpl.trackClick(adSpace, creative, impression, targetingContext, null);
    }

    @Test
    public void testClickUtilsImpl04_trackClick() throws IOException {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final Impression impression = mock(Impression.class);
        final ModelDto model = mock(ModelDto.class, "model");
        final Long modelId = null;
        final CountryDto country = mock(CountryDto.class, "country");
        final Long countryId = null;
        final byte[] serializedImpression = new byte[100];
        final String ipAddress = "178.23.45.67";
        final String effectiveUserAgent = randomAlphaNumericString(40);
        final String impressionExternalId = randomAlphaNumericString(40);
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final long campaignId = randomLong();
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationId = randomLong();

        expect(new Expectations() {
            {
                oneOf(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                oneOf(impression).getModelId();
                will(returnValue(modelId));
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                oneOf(impression).getCountryId();
                will(returnValue(countryId));
                oneOf(impression).getExternalID();
                will(returnValue(impressionExternalId));
                oneOf(adsquareWorker).isCountryWhitelisted(with(any(CountryDto.class)));
                will(returnValue(true));
                oneOf(adsquareWorker).reportClick(impressionExternalId);
                oneOf(kryoManager).writeObject(impression);
                will(returnValue(serializedImpression));
                oneOf(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(ipAddress));
                oneOf(targetingContext).getEffectiveUserAgent();
                will(returnValue(effectiveUserAgent));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getId();
                will(returnValue(publicationId));
                oneOf(backupLogger).logClickSuccess(with(impression), with(adSpace), with(any(Date.class)), with(campaignId), with(targetingContext));
            }
        });
        clickUtilsImpl.trackClick(adSpace, creative, impression, targetingContext, null);
    }

    @Test
    public void testClickUtilsImpl05_trackClick() throws IOException {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final Impression impression = mock(Impression.class);
        final ModelDto model = null;
        final Long impressionModelId = randomLong();
        final String effectiveUserAgent = randomAlphaNumericString(40);
        final long adSpaceId = randomLong();
        final String impressionExternalID = randomAlphaNumericString(20);
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();

        expect(new Expectations() {
            {
                oneOf(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                allowing(impression).getModelId();
                will(returnValue(impressionModelId));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(impression).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(targetingContext).getEffectiveUserAgent();
                will(returnValue(effectiveUserAgent));
                oneOf(backupLogger).logClickFailure(impression, "Model mismatch", targetingContext, String.valueOf(impressionModelId), "null");
                oneOf(adServerStats).increment(adSpace, AsCounter.ClickDeviceModelMismatch);
            }
        });
        clickUtilsImpl.trackClick(adSpace, creative, impression, targetingContext, null);
    }

    @Test
    public void testClickUtilsImpl06_trackClick() throws IOException {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final Impression impression = mock(Impression.class);
        final ModelDto model = mock(ModelDto.class, "model");
        ;
        final Long impressionModelId = randomLong();
        final Long modelId = impressionModelId + 1;
        final String effectiveUserAgent = randomAlphaNumericString(40);
        final long adSpaceId = randomLong();
        final String impressionExternalID = randomAlphaNumericString(20);
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();

        expect(new Expectations() {
            {
                oneOf(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                allowing(impression).getModelId();
                will(returnValue(impressionModelId));
                allowing(model).getId();
                will(returnValue(modelId));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(impression).getAdSpaceId();
                will(returnValue(adSpaceId));
                /*
                oneOf(adSpace).getPublication();
                will(returnValue(pub));
                oneOf(pub).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getId();
                will(returnValue(publisherId));
                */
                oneOf(targetingContext).getEffectiveUserAgent();
                will(returnValue(effectiveUserAgent));
                oneOf(backupLogger).logClickFailure(impression, "Model mismatch", targetingContext, String.valueOf(impressionModelId), String.valueOf(modelId));
                oneOf(adServerStats).increment(adSpace, AsCounter.ClickDeviceModelMismatch);
            }
        });
        clickUtilsImpl.trackClick(adSpace, creative, impression, targetingContext, null);
    }

    @Test
    public void testClickUtilsImpl07_trackClick() throws IOException {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final Impression impression = mock(Impression.class);
        final ModelDto model = mock(ModelDto.class, "model");
        final Long modelId = randomLong();
        final CountryDto country = null;
        final Long impressionCountryId = randomLong();
        final String ipAddress = "178.23.45.67";
        final long adSpaceId = randomLong();
        final String impressionExternalID = randomAlphaNumericString(20);

        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();

        expect(new Expectations() {
            {
                oneOf(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                oneOf(model).getId();
                will(returnValue(modelId));
                allowing(impression).getModelId();
                will(returnValue(modelId));
                allowing(impression).getCountryId();
                will(returnValue(impressionCountryId));
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                /*
                oneOf(adSpace).getPublication();
                will(returnValue(pub));
                oneOf(pub).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getId();
                will(returnValue(publisherId));
                */
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                oneOf(impression).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(ipAddress));
                oneOf(backupLogger).logClickFailure(impression, "Country mismatch", targetingContext, String.valueOf(impressionCountryId), "null");
                oneOf(adServerStats).increment(adSpace, AsCounter.ClickCountryMismatch);
            }
        });
        clickUtilsImpl.trackClick(adSpace, creative, impression, targetingContext, null);
    }

    @Test
    public void testClickUtilsImpl08_trackClick() throws IOException {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final Impression impression = mock(Impression.class);
        final ModelDto model = mock(ModelDto.class, "model");
        final Long modelId = randomLong();
        final CountryDto country = mock(CountryDto.class, "country");
        final Long countryId = randomLong();
        final Long impressionCountryId = countryId + 1;
        final String ipAddress = "178.23.45.67";
        final long adSpaceId = randomLong();
        final String impressionExternalID = randomAlphaNumericString(20);
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();

        expect(new Expectations() {
            {
                oneOf(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                oneOf(model).getId();
                will(returnValue(modelId));
                allowing(country).getId();
                will(returnValue(countryId));
                allowing(impression).getModelId();
                will(returnValue(modelId));
                allowing(impression).getCountryId();
                will(returnValue(impressionCountryId));
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                allowing(impression).getExternalID();
                will(returnValue(impressionExternalID));
                /*
                oneOf(adSpace).getPublication();
                will(returnValue(pub));
                oneOf(pub).getPublisher();
                will(returnValue(publisher));
                oneOf(publisher).getId();
                will(returnValue(publisherId));
                */
                oneOf(impression).getAdSpaceId();
                will(returnValue(adSpaceId));
                oneOf(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(ipAddress));
                oneOf(backupLogger).logClickFailure(impression, "Country mismatch", targetingContext, String.valueOf(impressionCountryId), String.valueOf(countryId));
                oneOf(adServerStats).increment(adSpace, AsCounter.ClickCountryMismatch);
            }
        });
        clickUtilsImpl.trackClick(adSpace, creative, impression, targetingContext, null);
    }

    @Test
    public void testClickUtilsImpl09_trackClick() throws IOException {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final CreativeDto creative = mock(CreativeDto.class, "creative");
        final Impression impression = mock(Impression.class);
        final ModelDto model = mock(ModelDto.class, "model");
        final Long modelId = randomLong();
        final CountryDto country = mock(CountryDto.class, "country");
        final Long countryId = randomLong();
        final byte[] serializedImpression = new byte[100];
        final String ipAddress = "178.23.45.67";
        final String effectiveUserAgent = randomAlphaNumericString(40);
        final String impressionExternalId = randomAlphaNumericString(40);
        final CampaignDto campaign = mock(CampaignDto.class, "campaign");
        final long campaignId = randomLong();
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final long publicationId = randomLong();

        expect(new Expectations() {
            {
                oneOf(targetingContext).getAttribute(TargetingContext.MODEL);
                will(returnValue(model));
                oneOf(model).getId();
                will(returnValue(modelId));
                allowing(country).getId();
                will(returnValue(countryId));
                allowing(impression).getModelId();
                will(returnValue(modelId));
                allowing(impression).getCountryId();
                will(returnValue(countryId));
                oneOf(adsquareWorker).isCountryWhitelisted(with(any(CountryDto.class)));
                will(returnValue(false)); // do not report click to adsquare
                oneOf(targetingContext).getAttribute(TargetingContext.COUNTRY);
                will(returnValue(country));
                oneOf(kryoManager).writeObject(impression);
                will(returnValue(serializedImpression));
                //
                oneOf(targetingContext).getEffectiveUserAgent();
                will(returnValue(effectiveUserAgent));
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).getId();
                will(returnValue(campaignId));
                oneOf(adSpace).getPublication();
                will(returnValue(publication));
                oneOf(publication).getId();
                will(returnValue(publicationId));
                oneOf(targetingContext).getAttribute(Parameters.IP);
                will(returnValue(ipAddress));
                oneOf(backupLogger).logClickSuccess(with(impression), with(adSpace), with(any(Date.class)), with(campaignId), with(targetingContext));
            }
        });
        clickUtilsImpl.trackClick(adSpace, creative, impression, targetingContext, null);
    }

    // MAD-730 - Delete ignored tests in Adserver project 	
    //	@Ignore
    //	@Test
    //	public void testClickUtilsImpl10_setClickIdCookie_nullCreative() throws IOException{
    //		//Instead of using Mock Object from Mocking framework, i am using Mock response object from Spring
    //		//Later we can check if Cookie has been set or not
    //		final MockHttpServletResponse response = new MockHttpServletResponse();
    //		final CreativeDto creative = null;
    //		final Impression impression = mock(Impression.class);
    //
    //		final int clickDefaultTtlSeconds = randomInteger();
    //		final int installTrackingTtlSeconds = randomInteger();
    //		final int conversionTrackingTtlSeconds = randomInteger();
    //		final String impressionExternalID = randomAlphaNumericString(10);
    //		
    //		
    //		expect(new Expectations() {{
    //		    allowing (impression).getExternalID(); will(returnValue(impressionExternalID));
    //
    //		}});
    //		inject(clickUtilsImpl, "clickDefaultTtlSeconds", clickDefaultTtlSeconds);
    //		inject(clickUtilsImpl, "conversionTrackingTtlSeconds", conversionTrackingTtlSeconds);
    //		inject(clickUtilsImpl, "installTrackingTtlSeconds", installTrackingTtlSeconds);
    //		clickUtilsImpl.setClickIdCookie(response, impression, creative);
    //		assertEquals(1, response.getCookies().length);
    //		Cookie cookie1 = response.getCookies()[0];
    //		assertNotNull(cookie1);
    //		assertEquals(impressionExternalID, cookie1.getValue());
    //		assertEquals(clickDefaultTtlSeconds, cookie1.getMaxAge());
    //		assertNull(cookie1.getDomain());
    //
    //	}
    //	
    //	@Ignore
    //	@Test
    //	public void testClickUtilsImpl10_setClickIdCookie() throws IOException{
    //		//Instead of using Mock Object from Mocking framework, i am using Mock response object from Spring
    //		//Later we can check if Cookie has been set or not
    //		final MockHttpServletResponse response = new MockHttpServletResponse();
    //		final CreativeDto creative = mock(CreativeDto.class,"creative");
    //		final Impression impression = mock(Impression.class);
    //		final CampaignDto campaign = null;
    //
    //		final int clickDefaultTtlSeconds = randomInteger();
    //		final int installTrackingTtlSeconds = randomInteger();
    //		final int conversionTrackingTtlSeconds = randomInteger();
    //		final String impressionExternalID = randomAlphaNumericString(10);
    //		
    //		
    //		expect(new Expectations() {{
    //		    oneOf (creative).getCampaign(); will(returnValue(campaign));
    //		    allowing (impression).getExternalID(); will(returnValue(impressionExternalID));
    //
    //		}});
    //		inject(clickUtilsImpl, "clickDefaultTtlSeconds", clickDefaultTtlSeconds);
    //		inject(clickUtilsImpl, "conversionTrackingTtlSeconds", conversionTrackingTtlSeconds);
    //		inject(clickUtilsImpl, "installTrackingTtlSeconds", installTrackingTtlSeconds);
    //		clickUtilsImpl.setClickIdCookie(response, impression, creative);
    //		assertEquals(1, response.getCookies().length);
    //		Cookie cookie1 = response.getCookies()[0];
    //		assertNotNull(cookie1);
    //		assertEquals(impressionExternalID, cookie1.getValue());
    //		assertEquals(clickDefaultTtlSeconds, cookie1.getMaxAge());
    //		assertNull(cookie1.getDomain());
    //
    //	}
    //	
    //	@Ignore
    //	@Test
    //	public void testClickUtilsImpl11_setClickIdCookie() throws IOException{
    //		//Instead of using Mock Object from Mocking framework, i am using Mock response object from Spring
    //		//Later we can check if Cookie has been set or not
    //		final MockHttpServletResponse response = new MockHttpServletResponse();
    //		final CreativeDto creative = mock(CreativeDto.class,"creative");
    //		final Impression impression = mock(Impression.class);
    //		final CampaignDto campaign = null;
    //
    //		final int clickDefaultTtlSeconds = randomInteger();
    //		final int installTrackingTtlSeconds = randomInteger();
    //		final int conversionTrackingTtlSeconds = randomInteger();
    //		final String impressionExternalID = randomAlphaNumericString(10);
    //		final String additionalCookieDomain = "www.mydomain.com";
    //		
    //		
    //		expect(new Expectations() {{
    //		    oneOf (creative).getCampaign(); will(returnValue(campaign));
    //		    allowing (impression).getExternalID(); will(returnValue(impressionExternalID));
    //		}});
    //		inject(clickUtilsImpl, "clickDefaultTtlSeconds", clickDefaultTtlSeconds);
    //		inject(clickUtilsImpl, "conversionTrackingTtlSeconds", conversionTrackingTtlSeconds);
    //		inject(clickUtilsImpl, "installTrackingTtlSeconds", installTrackingTtlSeconds);
    //		inject(clickUtilsImpl, "additionalCookieDomain", additionalCookieDomain);
    //		
    //		clickUtilsImpl.setClickIdCookie(response, impression, creative);
    //		
    //		assertEquals(2, response.getCookies().length);
    //		Cookie cookie1 = response.getCookies()[0];
    //		assertNotNull(cookie1);
    //		assertEquals(impressionExternalID, cookie1.getValue());
    //		assertEquals(clickDefaultTtlSeconds, cookie1.getMaxAge());
    //		assertNull(cookie1.getDomain());
    //		
    //		
    //		Cookie cookie2 = response.getCookies()[1];
    //		assertNotNull(cookie2);
    //		assertEquals(impressionExternalID, cookie2.getValue());
    //		assertEquals(clickDefaultTtlSeconds, cookie2.getMaxAge());
    //		System.out.println("cookie1.getDomain()="+cookie2.getDomain());
    //		assertNotNull(cookie2.getDomain());
    //		assertEquals(additionalCookieDomain, cookie2.getDomain());
    //		
    //		
    //	}

    @Test
    public void test_getTargetUrl_proxiedDestination() {
        final CreativeDto creative = mock(CreativeDto.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final Impression impression = mock(Impression.class);
        final String pdDestinationUrl = randomAlphaNumericString(10);

        expect(new Expectations() {
            {
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).isInstallTrackingAdXEnabled();
                will(returnValue(false));
                allowing(impression).isProxiedDestination();
                will(returnValue(true));
                allowing(impression).getPdDestinationUrl();
                will(returnValue(pdDestinationUrl));
            }
        });
        String returnUrl = clickUtilsImpl.getTargetUrl(impression, creative);
        assertEquals(pdDestinationUrl, returnUrl);
    }

    @Test
    public void test_getTargetUrl_call() {
        final CreativeDto creative = mock(CreativeDto.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final Impression impression = mock(Impression.class);
        final DestinationDto destination = mock(DestinationDto.class);
        final String destinationData = randomAlphaNumericString(10);

        expect(new Expectations() {
            {
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).isInstallTrackingAdXEnabled();
                will(returnValue(false));
                allowing(impression).isProxiedDestination();
                will(returnValue(false));
                allowing(creative).getDestination();
                will(returnValue(destination));
                allowing(destination).getDestinationType();
                will(returnValue(DestinationType.CALL));
                allowing(destination).getData();
                will(returnValue(destinationData));
            }
        });
        String returnUrl = clickUtilsImpl.getTargetUrl(impression, creative);
        assertEquals("tel:" + destinationData, returnUrl);
    }

    // MAD-730 - Delete ignored tests in Adserver project 
    //    @Ignore
    //    @Test
    //    public void test_getTargetUrl_video_withYospaceMatch_noQueryString() {
    //		final CreativeDto creative = mock(CreativeDto.class);
    //		final CampaignDto campaign = mock(CampaignDto.class);
    //		final Impression impression = mock(Impression.class);
    //        final DestinationDto destination = mock(DestinationDto.class);
    //		final String destinationData = "http://yospace.com/foo";
    //        final String clickId = randomAlphaNumericString(10);
    //        final String urlId = randomAlphaNumericString(10);
    //		
    //        expect(new Expectations() {{
    //            allowing (creative).getCampaign(); will(returnValue(campaign));	
    //            allowing (campaign).isInstallTrackingAdXEnabled(); will(returnValue(false));
    //            allowing (impression).isProxiedDestination(); will(returnValue(false));
    //            allowing (creative).getDestination(); will(returnValue(destination));
    //            allowing (destination).getDestinationType(); will(returnValue(DestinationType.VIDEO));
    //            allowing (destination).getData(); will(returnValue(destinationData));
    //            allowing (impression).getExternalID(); will(returnValue(clickId));
    //		}});
    //        inject(clickUtilsImpl, "yospaceTrackerUrlId", urlId);
    //        String returnUrl = clickUtilsImpl.getTargetUrl(impression, creative, targetingContext);
    //        assertEquals(destinationData + "?clickId=" + clickId + "&urlId=" + urlId, returnUrl);
    //    }
    //    
    //    @Ignore
    //    @Test
    //    public void test_getTargetUrl_video_withYospaceMatch_withQueryString() {
    //		final CreativeDto creative = mock(CreativeDto.class);
    //		final CampaignDto campaign = mock(CampaignDto.class);
    //		final Impression impression = mock(Impression.class);
    //        final DestinationDto destination = mock(DestinationDto.class);
    //		final String destinationData = "http://yospace.com/foo?hello=there";
    //        final String clickId = randomAlphaNumericString(10);
    //        final String urlId = randomAlphaNumericString(10);
    //		
    //        expect(new Expectations() {{
    //            allowing (creative).getCampaign(); will(returnValue(campaign));	
    //            allowing (campaign).isInstallTrackingAdXEnabled(); will(returnValue(false));
    //            allowing (impression).isProxiedDestination(); will(returnValue(false));
    //            allowing (creative).getDestination(); will(returnValue(destination));
    //            allowing (destination).getDestinationType(); will(returnValue(DestinationType.VIDEO));
    //            allowing (destination).getData(); will(returnValue(destinationData));
    //            allowing (impression).getExternalID(); will(returnValue(clickId));
    //		}});
    //        inject(clickUtilsImpl, "yospaceTrackerUrlId", urlId);
    //        String returnUrl = clickUtilsImpl.getTargetUrl(impression, creative, targetingContext);
    //        assertEquals(destinationData + "&clickId=" + clickId + "&urlId=" + urlId, returnUrl);
    //    }

    @Test
    public void test_getTargetUrl_video_withoutYospaceMatch() {
        final CreativeDto creative = mock(CreativeDto.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final Impression impression = mock(Impression.class);
        final DestinationDto destination = mock(DestinationDto.class);
        final String destinationData = "not a match";

        expect(new Expectations() {
            {
                allowing(creative).getCampaign();
                will(returnValue(campaign));
                allowing(campaign).isInstallTrackingAdXEnabled();
                will(returnValue(false));
                allowing(impression).isProxiedDestination();
                will(returnValue(false));
                allowing(creative).getDestination();
                will(returnValue(destination));
                allowing(destination).getDestinationType();
                will(returnValue(DestinationType.VIDEO));
                allowing(destination).getData();
                will(returnValue(destinationData));
            }
        });
        String returnUrl = clickUtilsImpl.getTargetUrl(impression, creative);
        assertEquals(destinationData, returnUrl);
    }

}

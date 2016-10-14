package com.adfonic.adserver.rtb.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.mapper.OpenXMapper.CreativeType;
import com.adfonic.adserver.rtb.nativ.AdType;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.adserver.rtb.openx.OpenX.AdId;
import com.adfonic.adserver.rtb.openx.OpenX.BidRequest;
import com.adfonic.adserver.rtb.openx.OpenX.Device;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.DestinationType;
import com.adfonic.test.AbstractAdfonicTest;

public class TestOpenXMapper extends AbstractAdfonicTest {

    private final OpenXMapper rtbMapper = OpenXMapper.instance();

    @Test
    public void testCreativeType_getByOrdinal() {
        for (CreativeType creativeType : CreativeType.values()) {
            assertEquals(creativeType, CreativeType.getByOrdinal(creativeType.ordinal()));
        }
    }

    @Test
    public void requireUserAgent_deviceNotInitialized_but_requestHasIt() throws NoBidException {
        String userAgent = randomAlphaNumericString(10);
        BidRequest request = BidRequest.newBuilder().setApiVersion(7).setAuctionId(randomAlphaNumericString(10)).setPubWebsiteId(randomAlphaNumericString(10)).setAdHeight(123)
                .setAdWidth(123).setUserAgent(userAgent).build();
        assertEquals(userAgent, rtbMapper.digUserAgent(request));
    }

    @Test
    public void requireUserAgent_deviceNotInitialized_and_requestDoesNotHaveIt() throws NoBidException {
        BidRequest request = BidRequest.newBuilder().setApiVersion(7).setAuctionId(randomAlphaNumericString(10)).setPubWebsiteId(randomAlphaNumericString(10)).setAdHeight(123)
                .setAdWidth(123).build();
        String userAgent = rtbMapper.digUserAgent(request);
        Assertions.assertThat(userAgent).isNull();
    }

    @Test
    public void requireUserAgentWithDevice() throws NoBidException {
        String userAgent = randomAlphaNumericString(10);
        BidRequest request = BidRequest.newBuilder().setApiVersion(7).setAuctionId(randomAlphaNumericString(10)).setPubWebsiteId(randomAlphaNumericString(10)).setAdHeight(123)
                .setAdWidth(123).setDevice(Device.newBuilder().setUa(userAgent).build()).build();
        assertEquals(userAgent, rtbMapper.digUserAgent(request));
    }

    @Test
    public void requireIpAddress_deviceHasIt() throws NoBidException {
        String ip = randomAlphaNumericString(10);
        BidRequest request = BidRequest.newBuilder().setApiVersion(7).setAuctionId(randomAlphaNumericString(10)).setPubWebsiteId(randomAlphaNumericString(10)).setAdHeight(123)
                .setAdWidth(123).setDevice(Device.newBuilder().setIp(ip).build()).build();
        com.adfonic.adserver.rtb.nativ.ByydRequest byydRequest = new com.adfonic.adserver.rtb.nativ.ByydRequest("x", "y");
        assertEquals(ip, rtbMapper.digIpAddress(request, byydRequest));
    }

    @Test
    public void requireIpAddress_deviceNotInitialized_and_requestDoesNotHaveIt() throws NoBidException {
        BidRequest request = BidRequest.newBuilder().setApiVersion(7).setAuctionId(randomAlphaNumericString(10)).setPubWebsiteId(randomAlphaNumericString(10)).setAdHeight(123)
                .setAdWidth(123).build();
        com.adfonic.adserver.rtb.nativ.ByydRequest byydRequest = new com.adfonic.adserver.rtb.nativ.ByydRequest("x", "y");
        String ipAddress = rtbMapper.digIpAddress(request, byydRequest);
        Assertions.assertThat(ipAddress).isNull();
    }

    @Test(expected = NoBidException.class)
    public void abort() throws NoBidException {
        com.adfonic.adserver.rtb.nativ.ByydRequest byydRequest = new com.adfonic.adserver.rtb.nativ.ByydRequest("x", "y");
        rtbMapper.abort(byydRequest, AdSrvCounter.MISS_IP, Level.INFO);
    }

    @Test
    public void addOneValueToSet_setIsNull() {
        Set<String> out = OpenXMapper.addToSet(null, "hello");
        assertNotNull(out);
        assertTrue(out.contains("hello"));
    }

    @Test
    public void addOneValueToSet_setIsNotNull() {
        Set<String> set = new HashSet<String>();
        assertEquals(set, OpenXMapper.addToSet(set, "hello"));
        assertTrue(set.contains("hello"));
    }

    @Test
    public void copyUser_all_empty() {
        BidRequest request = BidRequest.newBuilder().setApiVersion(7).setAuctionId(randomAlphaNumericString(10)).setPubWebsiteId(randomAlphaNumericString(10)).setAdHeight(123)
                .setAdWidth(123).build();
        ByydUser user = OpenXMapper.mapUser(request);
        assertNotNull(user);
        assertNull(user.getUid());
        assertNull(user.getCountryCode());
        assertNull(user.getState());
        assertNull(user.getDma());
    }

    @Test
    public void copyUser_all_not_empty() {
        String uid = randomAlphaNumericString(10);
        String countryCode = randomAlphaString(2).toUpperCase();
        String state = randomAlphaNumericString(10);
        int dmaId = randomInteger();
        BidRequest request = BidRequest.newBuilder().setApiVersion(7).setAuctionId(randomAlphaNumericString(10)).setPubWebsiteId(randomAlphaNumericString(10)).setAdHeight(123)
                .setAdWidth(123).setUserCookieId(uid).setUserGeoCountry(countryCode.toLowerCase()).setUserGeoState(state).setUserGeoDma(dmaId).build();
        ByydUser user = OpenXMapper.mapUser(request);
        assertNotNull(user);
        assertEquals(uid, user.getUid());
        assertEquals(countryCode, user.getCountryCode());
        assertEquals(state, user.getState());
        assertEquals(String.valueOf(dmaId), user.getDma());
    }

    @Test
    public void copyImp_with_ad_dimensions() {
        Integer bidRequestAdWidth = 123;
        Integer bidRequestAdHeight = 234;
        Integer adIdAdWidth = 345;
        Integer adIdAdHeight = 456;
        BidRequest request = BidRequest.newBuilder().setApiVersion(7).setAuctionId(randomAlphaNumericString(10)).setPubWebsiteId(randomAlphaNumericString(10))
                .setAdHeight(bidRequestAdHeight).setAdWidth(bidRequestAdWidth).build();
        AdId adId = AdId.newBuilder().setCampaignId(randomInteger()).setPlacementId(randomInteger()).setCreativeId(randomInteger()).setAdWidth(adIdAdWidth)
                .setAdHeight(adIdAdHeight).build();
        Set<AdType> blockedAdTypes = Collections.singleton(AdType.XHTML_BANNER_AD);
        boolean blockExtendedCreatives = true;
        Set<DestinationType> blockedDestinationTypes = Collections.singleton(DestinationType.VIDEO);
        Set<ContentForm> contentFormWhiteList = new LinkedHashSet<>(Collections.singleton(ContentForm.MRAID_1_0));
        ByydImp imp = new ByydImp("imp-id-1234");
        OpenXMapper.mapImp(imp, adId, request);
        assertNotNull(imp);
        assertEquals(adIdAdWidth, imp.getW());
        assertEquals(adIdAdHeight, imp.getH());
        assertEquals(true, imp.isStrictBannerSize());
    }

    @Test
    public void copyImp_without_ad_dimensions() {
        Integer bidRequestAdWidth = 123;
        Integer bidRequestAdHeight = 234;
        BidRequest request = BidRequest.newBuilder().setApiVersion(7).setAuctionId(randomAlphaNumericString(10)).setPubWebsiteId(randomAlphaNumericString(10))
                .setAdHeight(bidRequestAdHeight).setAdWidth(bidRequestAdWidth).build();
        AdId adId = AdId.newBuilder().setCampaignId(randomInteger()).setPlacementId(randomInteger()).setCreativeId(randomInteger()).build();
        ByydImp imp = new ByydImp("imp-id-1234");
        OpenXMapper.mapImp(imp, adId, request);
        assertEquals(bidRequestAdWidth, imp.getW());
        assertEquals(bidRequestAdHeight, imp.getH());
    }
}

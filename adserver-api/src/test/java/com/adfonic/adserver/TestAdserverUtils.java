package com.adfonic.adserver;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

public class TestAdserverUtils {
    @Test
    public void testGetClickTtlSeconds_allNulls() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, null, null, null, null, 123, 3000, 5000);
        assertEquals(123, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_nothingEnabled() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, null, false, false, false, 123, 3000, 5000);
        assertEquals(123, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_only_installTrackingEnabled() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, "appid", true, false, false, 123, 3000, 5000);
        assertEquals(3000, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_only_installTrackingEnabledWithNulls() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, "appid", true, false, false, 123, 3000, 5000);
        assertEquals(3000, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_only_installTrackingEnabled_but_no_tracking_id() {
        Impression impression = new Impression();

        int result = AdserverUtils.getClickTtlSeconds(impression, "appid", true, false, false, 123, 3000, 5000);
        assertEquals(123, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_only_installTrackingEnabled_but_appId_is_null() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, null, true, false, false, 123, 3000, 5000);
        assertEquals(123, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_installTrackingEnabled_and_installTrackingAdXEnabled() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, "appid", true, true, false, 123, 3000, 5000);
        assertEquals(3000, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_installTrackingEnabled_and_conversionTrackingEnabled() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, "appid", true, false, true, 123, 3000, 5000);
        assertEquals(5000, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_installTrackingEnabled_and_conversionTrackingEnabled_but_appId_is_null() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, null, true, false, true, 123, 9999, 5000);
        assertEquals(5000, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_installTrackingEnabled_and_installTrackingAdXEnabled_and_conversionTrackingEnabled() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, "appid", true, true, true, 123, 3000, 5000);
        assertEquals(5000, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_installTrackingAdXEnabled_and_conversionTrackingEnabled() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, "appid", false, true, true, 123, 3000, 5000);
        assertEquals(5000, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_only_installTrackingAdXEnabled() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, "appid", false, true, false, 123, 3000, 5000);
        assertEquals(3000, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_only_installTrackingAdXEnabled_no_app_id() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, null, false, true, false, 123, 3000, 5000);
        assertEquals(123, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_only_installTrackingAdXEnabled_no_tracking_id() {
        Impression impression = new Impression();

        int result = AdserverUtils.getClickTtlSeconds(impression, "appid", false, true, false, 123, 3000, 5000);
        assertEquals(123, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_only_conversionTrackingEnabled() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));

        int result = AdserverUtils.getClickTtlSeconds(impression, null, false, false, true, 123, 3000, 5000);
        assertEquals(5000, result);
    }
    
    @Test
    public void testGetClickTtlSeconds_only_conversionTrackingEnabled_no_tracking_id() {
        Impression impression = new Impression();

        int result = AdserverUtils.getClickTtlSeconds(impression, null, false, false, true, 123, 9000, 7000);
        assertEquals(7000, result);
    }

    @Test
    public void testGetClickExpireTime() {
        Impression impression = new Impression();
        impression.setDeviceIdentifiers(Collections.singletonMap(1L, DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567")));
        Date result = AdserverUtils.getClickExpireTime(impression, "blah", true, true, true, 123, 3000, 5000);
        assertEquals(DateUtils.addSeconds(impression.getCreationTime(), 5000), result);
    }
}

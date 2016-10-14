package com.adfonic.adserver.rtb.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.impl.icache.NamespaceAwareRtbCacheService;
import com.adfonic.adserver.rtb.RtbBidDetails;
import com.adfonic.cache.CacheManager;

public class TestNamespaceAwareRtbCacheService extends BaseAdserverTest {

    private NamespaceAwareRtbCacheService namespaceAwareRtbCacheService;
    private static final String RTB_BID_DETAILS_CACHE_NAME = "RtbBidDetails";
    private static final String IMPRESSION = "imp";
    private CacheManager cacheManager;
    private KryoManager kryoManager;
    private int ttlSeconds;

    @Before
    public void initTest() {
        cacheManager = mock(CacheManager.class, "cacheManager");
        kryoManager = mock(KryoManager.class, "kryoManager");
        ttlSeconds = randomInteger(60);
        namespaceAwareRtbCacheService = new NamespaceAwareRtbCacheService(cacheManager, kryoManager, ttlSeconds);

    }

    /**
     * Test where setting a value for 10 seconds in cache
     */
    @Test
    public void testNamespaceAwareRtbCacheService01() {
        final String key = "SomeKey";
        final RtbBidDetails rtbBidDetails = mock(RtbBidDetails.class, "rtbBidDetails");
        final HashMap<String, Serializable> map = new HashMap<String, Serializable>();
        final Impression impression = null;
        final byte[] objectBytes = new byte[10];

        map.put(IMPRESSION, objectBytes);
        expect(new Expectations() {
            {
                oneOf(cacheManager).set(key, map, RTB_BID_DETAILS_CACHE_NAME, ttlSeconds);
                oneOf(rtbBidDetails).toMap(kryoManager);
                will(returnValue(map));
                oneOf(cacheManager).get(key, RTB_BID_DETAILS_CACHE_NAME, HashMap.class);
                will(returnValue(map));
                oneOf(cacheManager).get(key, RTB_BID_DETAILS_CACHE_NAME, HashMap.class);
                will(returnValue(null));
                oneOf(kryoManager).readObject(objectBytes, Impression.class);
                will(returnValue(impression));
            }
        });

        namespaceAwareRtbCacheService.saveBidDetails(key, rtbBidDetails);
        RtbBidDetails bidDetailsFound = namespaceAwareRtbCacheService.getBidDetails(key);
        assertNotNull(bidDetailsFound);

        bidDetailsFound = namespaceAwareRtbCacheService.getBidDetails(key);
        assertNull(bidDetailsFound);
    }

    /**
     * Test where setting a value in cache and then remove it
     */
    @Test
    public void testNamespaceAwareRtbCacheService02() {

        final String key = "SomeKey";
        final RtbBidDetails rtbBidDetails = mock(RtbBidDetails.class, "rtbBidDetails");
        final HashMap<String, Serializable> map = new HashMap<String, Serializable>();

        expect(new Expectations() {
            {
                oneOf(cacheManager).set(key, map, RTB_BID_DETAILS_CACHE_NAME, ttlSeconds);
                oneOf(rtbBidDetails).toMap(kryoManager);
                will(returnValue(map));
                oneOf(cacheManager).remove(key, RTB_BID_DETAILS_CACHE_NAME);
                will(returnValue(true));
            }
        });

        namespaceAwareRtbCacheService.saveBidDetails(key, rtbBidDetails);
        Boolean valueRemoved = namespaceAwareRtbCacheService.removeBidDetails(key);
        //System.out.println("Value Found = "+valueRemoved);
        assertTrue(valueRemoved);

    }

    /**
     * Test where setting a value in cache and then remove it
     */
    @Test
    public void testNamespaceAwareRtbCacheService03() {

        final String key = "SomeKey";
        final RtbBidDetails rtbBidDetails = mock(RtbBidDetails.class, "rtbBidDetails");

        final HashMap<String, Serializable> map = new HashMap<String, Serializable>();
        final Impression impression = null;
        final byte[] objectBytes = new byte[10];

        map.put(IMPRESSION, objectBytes);

        expect(new Expectations() {
            {
                oneOf(cacheManager).set(key, map, RTB_BID_DETAILS_CACHE_NAME, ttlSeconds);
                oneOf(rtbBidDetails).toMap(kryoManager);
                will(returnValue(map));
                oneOf(cacheManager).get(key, RTB_BID_DETAILS_CACHE_NAME, HashMap.class);
                will(returnValue(map));
                oneOf(cacheManager).remove(key, RTB_BID_DETAILS_CACHE_NAME);
                will(returnValue(true));
                oneOf(kryoManager).readObject(objectBytes, Impression.class);
                will(returnValue(impression));

            }
        });
        namespaceAwareRtbCacheService.saveBidDetails(key, rtbBidDetails);
        RtbBidDetails valueRemoved = namespaceAwareRtbCacheService.getAndRemoveBidDetails(key);
        //System.out.println("Value Found = "+valueRemoved);
        assertTrue(valueRemoved != null);

    }

    /**
     * Test where setting a value in cache and then remove it
     */
    @Test
    public void testNamespaceAwareRtbCacheService04() {

        final String key = "SomeKey";
        final RtbBidDetails rtbBidDetails = mock(RtbBidDetails.class, "rtbBidDetails");

        final HashMap<String, Serializable> map = null;

        expect(new Expectations() {
            {
                oneOf(cacheManager).set(key, map, RTB_BID_DETAILS_CACHE_NAME, ttlSeconds);
                oneOf(rtbBidDetails).toMap(kryoManager);
                will(returnValue(map));
                oneOf(cacheManager).get(key, RTB_BID_DETAILS_CACHE_NAME, HashMap.class);
                will(returnValue(map));
                oneOf(cacheManager).remove(key, RTB_BID_DETAILS_CACHE_NAME);
                will(returnValue(false));

            }
        });
        namespaceAwareRtbCacheService.saveBidDetails(key, rtbBidDetails);
        RtbBidDetails valueRemoved = namespaceAwareRtbCacheService.getAndRemoveBidDetails(key);
        //System.out.println("Value Found = "+valueRemoved);
        assertNull(valueRemoved);

    }

}

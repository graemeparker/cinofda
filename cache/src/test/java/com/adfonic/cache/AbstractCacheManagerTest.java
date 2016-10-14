package com.adfonic.cache;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractCacheManagerTest {
    private static final transient Logger LOG = Logger.getLogger(AbstractCacheManagerTest.class.getName());
    
    private CacheManager cacheManager;
    
    protected abstract CacheManager createCacheManager();

    @Before
    public void setup() {
        cacheManager = createCacheManager();
        assumeNotNull(cacheManager);
        LOG.info("Testing with " + cacheManager.getClass().getName());
    }

    @Test
    public void testBasicOps() {
        String key = UUID.randomUUID().toString();
        String value = "this is a bunch of random text, nothing specific here, blah blah blah";

        // Basic set/get/remove test
        cacheManager.set(key, value, 999);
        assertEquals("Basic set/get test", value, cacheManager.get(key, value.getClass()));
        assertTrue("Remove should succeed when key is present", cacheManager.remove(key));
        assertNull("Should be null after remove was called", cacheManager.get(key, value.getClass()));

        // Same but with a cache name
        String cacheName = "Impression";
        cacheManager.set(key, value, cacheName, 999);
        assertEquals("Cache name set/get test", value, cacheManager.get(key, cacheName, value.getClass()));
        assertTrue("Remove with cache name should succeed when key is present", cacheManager.remove(key, cacheName));
        assertNull("Should be null after remove with cache name was called", cacheManager.get(key, value.getClass()));

        // Remove with class
        cacheManager.set(key, value, 999);
        assertTrue("Remove with class should succeed when key is present", cacheManager.remove(key, value.getClass()));
        assertNull("Should be null after remove with class was called", cacheManager.get(key, value.getClass()));
    }

    @Test
    public void testTtlSeconds() throws InterruptedException {
        String key = UUID.randomUUID().toString();
        String value = "irrelevant, since this should expire on its own";
        int ttlSeconds = 2;
        cacheManager.set(key, value, ttlSeconds);
        long extraSleepTime = 500L; // sleep an extra half second
        Thread.sleep((ttlSeconds * 1000L) + extraSleepTime);
        assertNull("Get should return null after TTL expires", cacheManager.get(key, value.getClass()));
    }

    @Test
    public void testExpiryDate() throws InterruptedException {
        String key = UUID.randomUUID().toString();
        String value = "irrelevant, since this should expire on its own";
        int ttlSeconds = 2;
        Date expiryDate = new Date();
        expiryDate.setTime(System.currentTimeMillis() + (ttlSeconds * 1000L));
        cacheManager.set(key, value, expiryDate);
        long extraSleepTime = 500L; // sleep an extra half second
        Thread.sleep((ttlSeconds * 1000L) + extraSleepTime);
        assertNull("Get should return null after expiry date expires", cacheManager.get(key, value.getClass()));
    }
}
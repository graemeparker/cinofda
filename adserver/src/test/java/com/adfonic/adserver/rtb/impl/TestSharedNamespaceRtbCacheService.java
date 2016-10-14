package com.adfonic.adserver.rtb.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.impl.icache.SharedNamespaceRtbCacheService;
import com.adfonic.adserver.rtb.RtbBidDetails;
import com.adfonic.cache.CacheManager;

public class TestSharedNamespaceRtbCacheService extends BaseAdserverTest {

	private CacheManager cacheManager;
	private KryoManager kryoManager;
	private int ttlSeconds;
	private SharedNamespaceRtbCacheService sharedNamespaceRtbCacheService; 
	private static final String KEY_PREFIX = "RTB.";
	@Before
	public void initTests(){
		cacheManager = mock(CacheManager.class,"cacheManager");
		kryoManager = mock(KryoManager.class,"kryoManager");
		ttlSeconds = randomInteger(60);
		sharedNamespaceRtbCacheService = new SharedNamespaceRtbCacheService(cacheManager, kryoManager, ttlSeconds);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService01_getBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(KEY_PREFIX+key, Map.class); 
		    	will(returnValue(null));
		}});
		RtbBidDetails rtbBidDetails = sharedNamespaceRtbCacheService.getBidDetails(key);
		assertNull(rtbBidDetails);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService02_getBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = mock(Impression.class,"impression");
		final Map<String,Serializable> map = new HashMap<String, Serializable>();
		String ipAddress = "145.67.89.12";
		final byte[] serializedImpression = null;
		map.put("ip", ipAddress);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(KEY_PREFIX+key, Map.class); 
		    	will(returnValue(map));
		    oneOf (kryoManager).readObject(serializedImpression, Impression.class); 
	    	will(returnValue(impression));
		}});
		RtbBidDetails rtbBidDetails = sharedNamespaceRtbCacheService.getBidDetails(key);
		assertEquals(ipAddress,rtbBidDetails.getIpAddress());
		assertEquals(impression,rtbBidDetails.getImpression());
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService03_getAndRemoveBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(KEY_PREFIX+key, Map.class);
	    	will(returnValue(null));
		    oneOf (cacheManager).remove(KEY_PREFIX+key);
		}});
		RtbBidDetails rtbBidDetails = sharedNamespaceRtbCacheService.getAndRemoveBidDetails(key);
		assertNull(rtbBidDetails);
	}
	@Test
	public void testSharedNamespaceRtbCacheService04_getAndRemoveBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = mock(Impression.class,"impression");
		final Map<String,Serializable> map = new HashMap<String, Serializable>();
		String ipAddress = "145.67.89.12";
		final byte[] serializedImpression = null;
		map.put("ip", ipAddress);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(KEY_PREFIX+key, Map.class); 
		    	will(returnValue(map));
		    oneOf (kryoManager).readObject(serializedImpression, Impression.class); 
	    	will(returnValue(impression));
	    	oneOf (cacheManager).remove(KEY_PREFIX+key);
		}});
		RtbBidDetails rtbBidDetails = sharedNamespaceRtbCacheService.getAndRemoveBidDetails(key);
		assertEquals(ipAddress,rtbBidDetails.getIpAddress());
		assertEquals(impression,rtbBidDetails.getImpression());
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService05_removeBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
	    	oneOf (cacheManager).remove(KEY_PREFIX+key);
	    	will(returnValue(true));
		}});
		Boolean returnValue = sharedNamespaceRtbCacheService.removeBidDetails(key);
		assertTrue(returnValue);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService06_removeBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
	    	oneOf (cacheManager).remove(KEY_PREFIX+key);
	    	will(returnValue(false));
		}});
		Boolean returnValue = sharedNamespaceRtbCacheService.removeBidDetails(key);
		assertFalse(returnValue);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService07_saveBidDetails(){
		final String key = randomAlphaNumericString(10);
		final RtbBidDetails rtbBidDetails = mock(RtbBidDetails.class,"rtbBidDetails");
		final Map<String, Serializable> map = new HashMap<String, Serializable>();
		expect(new Expectations() {{
	    	oneOf (cacheManager).set(KEY_PREFIX+key, map, ttlSeconds);
	    	oneOf (rtbBidDetails).toMap(kryoManager);
	    		will(returnValue(map));
	    	
		}});
		sharedNamespaceRtbCacheService.saveBidDetails(key, rtbBidDetails);
	}
}

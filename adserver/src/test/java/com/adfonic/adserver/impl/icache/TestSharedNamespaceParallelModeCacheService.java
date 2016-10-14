package com.adfonic.adserver.impl.icache;

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
import com.adfonic.adserver.ParallelModeBidDetails;
import com.adfonic.cache.CacheManager;

public class TestSharedNamespaceParallelModeCacheService extends BaseAdserverTest{
	
	private CacheManager cacheManager;
	private KryoManager kryoManager;
	private int ttlSeconds;
	private SharedNamespaceParallelModeCacheService sharedNamespaceParallelModeCacheService; 
	private static final String KEY_PREFIX = "par.";
	@Before
	public void initTests(){
		cacheManager = mock(CacheManager.class,"cacheManager");
		kryoManager = mock(KryoManager.class,"kryoManager");
		ttlSeconds = randomInteger(60);
		sharedNamespaceParallelModeCacheService = new SharedNamespaceParallelModeCacheService(cacheManager, kryoManager, ttlSeconds);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService01_getBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(KEY_PREFIX+key, Map.class); 
		    	will(returnValue(null));
		}});
		ParallelModeBidDetails parallelModeBidDetails = sharedNamespaceParallelModeCacheService.getBidDetails(key);
		assertNull(parallelModeBidDetails);
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
		ParallelModeBidDetails parallelModeBidDetails = sharedNamespaceParallelModeCacheService.getBidDetails(key);
		assertEquals(ipAddress,parallelModeBidDetails.getIpAddress());
		assertEquals(impression,parallelModeBidDetails.getImpression());
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService03_getAndRemoveBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(KEY_PREFIX+key, Map.class);
	    	will(returnValue(null));
		    oneOf (cacheManager).remove(KEY_PREFIX+key);
		}});
		ParallelModeBidDetails parallelModeBidDetails = sharedNamespaceParallelModeCacheService.getAndRemoveBidDetails(key);
		assertNull(parallelModeBidDetails);
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
		ParallelModeBidDetails parallelModeBidDetails = sharedNamespaceParallelModeCacheService.getAndRemoveBidDetails(key);
		assertEquals(ipAddress,parallelModeBidDetails.getIpAddress());
		assertEquals(impression,parallelModeBidDetails.getImpression());
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService05_removeBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
	    	oneOf (cacheManager).remove(KEY_PREFIX+key);
	    	will(returnValue(true));
		}});
		Boolean returnValue = sharedNamespaceParallelModeCacheService.removeBidDetails(key);
		assertTrue(returnValue);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService06_removeBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
	    	oneOf (cacheManager).remove(KEY_PREFIX+key);
	    	will(returnValue(false));
		}});
		Boolean returnValue = sharedNamespaceParallelModeCacheService.removeBidDetails(key);
		assertFalse(returnValue);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService07_saveBidDetails(){
		final String key = randomAlphaNumericString(10);
		final ParallelModeBidDetails parallelModeBidDetails = mock(ParallelModeBidDetails.class,"parallelModeBidDetails");
		final Map<String, Serializable> map = new HashMap<String, Serializable>();
		expect(new Expectations() {{
	    	oneOf (cacheManager).set(KEY_PREFIX+key, map, ttlSeconds);
	    	oneOf (parallelModeBidDetails).toMap(kryoManager);
	    		will(returnValue(map));
	    	
		}});
		sharedNamespaceParallelModeCacheService.saveBidDetails(key, parallelModeBidDetails);
	}

}

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

public class TestNamespaceAwareParallelModeCacheService extends
		BaseAdserverTest {

	private CacheManager cacheManager;
	private KryoManager kryoManager;
	private int ttlSeconds;
	private NamespaceAwareParallelModeCacheService namespaceAwareParallelModeCacheService; 

	@Before
	public void initTests(){
		cacheManager = mock(CacheManager.class,"cacheManager");
		kryoManager = mock(KryoManager.class,"kryoManager");
		ttlSeconds = randomInteger(60);
		namespaceAwareParallelModeCacheService = new NamespaceAwareParallelModeCacheService(cacheManager, kryoManager, ttlSeconds);
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService01_getBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(key, NamespaceAwareParallelModeCacheService.PARALLEL_MODE_BID_DETAILS_CACHE_NAME, HashMap.class); 
		    	will(returnValue(null));
		}});
		ParallelModeBidDetails parallelModeBidDetails = namespaceAwareParallelModeCacheService.getBidDetails(key);
		assertNull(parallelModeBidDetails);
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService02_getBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = mock(Impression.class,"impression");
		final Map<String,Serializable> map = new HashMap<String, Serializable>();
		String ipAddress = "145.67.89.12";
		final byte[] serializedImpression = null;
		map.put("ip", ipAddress);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(key, NamespaceAwareParallelModeCacheService.PARALLEL_MODE_BID_DETAILS_CACHE_NAME, HashMap.class); 
		    	will(returnValue(map));
		    oneOf (kryoManager).readObject(serializedImpression, Impression.class); 
	    	will(returnValue(impression));
		}});
		ParallelModeBidDetails returnParallelModeBidDetails = namespaceAwareParallelModeCacheService.getBidDetails(key);
		assertEquals(ipAddress,returnParallelModeBidDetails.getIpAddress());
		assertEquals(impression,returnParallelModeBidDetails.getImpression());
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService03_getAndRemoveBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(key, NamespaceAwareParallelModeCacheService.PARALLEL_MODE_BID_DETAILS_CACHE_NAME, HashMap.class);
	    	will(returnValue(null));
		    oneOf (cacheManager).remove(key, NamespaceAwareParallelModeCacheService.PARALLEL_MODE_BID_DETAILS_CACHE_NAME);
		}});
		ParallelModeBidDetails parallelModeBidDetails = namespaceAwareParallelModeCacheService.getAndRemoveBidDetails(key);
		assertNull(parallelModeBidDetails);
	}
	@Test
	public void testNamespaceAwareParallelModeCacheService04_getAndRemoveBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = mock(Impression.class,"impression");
		final Map<String,Serializable> map = new HashMap<String, Serializable>();
		String ipAddress = "145.67.89.12";
		final byte[] serializedImpression = null;
		map.put("ip", ipAddress);
		expect(new Expectations() {{
		    oneOf (cacheManager).get(key, NamespaceAwareParallelModeCacheService.PARALLEL_MODE_BID_DETAILS_CACHE_NAME, HashMap.class); 
		    	will(returnValue(map));
		    oneOf (kryoManager).readObject(serializedImpression, Impression.class); 
	    	will(returnValue(impression));
	    	oneOf (cacheManager).remove(key, NamespaceAwareParallelModeCacheService.PARALLEL_MODE_BID_DETAILS_CACHE_NAME);
		}});
		ParallelModeBidDetails returnParallelModeBidDetails = namespaceAwareParallelModeCacheService.getAndRemoveBidDetails(key);
		assertEquals(ipAddress,returnParallelModeBidDetails.getIpAddress());
		assertEquals(impression,returnParallelModeBidDetails.getImpression());
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService05_removeBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
	    	oneOf (cacheManager).remove(key,NamespaceAwareParallelModeCacheService.PARALLEL_MODE_BID_DETAILS_CACHE_NAME);
	    	will(returnValue(true));
		}});
		Boolean returnValue = namespaceAwareParallelModeCacheService.removeBidDetails(key);
		assertTrue(returnValue);
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService06_removeBidDetails(){
		final String key = randomAlphaNumericString(10);
		expect(new Expectations() {{
	    	oneOf (cacheManager).remove(key,NamespaceAwareParallelModeCacheService.PARALLEL_MODE_BID_DETAILS_CACHE_NAME);
	    	will(returnValue(false));
		}});
		Boolean returnValue = namespaceAwareParallelModeCacheService.removeBidDetails(key);
		assertFalse(returnValue);
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService07_saveBidDetails(){
		final String key = randomAlphaNumericString(10);
		final ParallelModeBidDetails parallelModeBidDetails = mock(ParallelModeBidDetails.class,"parallelModeBidDetails");
		final Map<String, Serializable> map = new HashMap<String, Serializable>();
		expect(new Expectations() {{
	    	oneOf (cacheManager).set(key, map, NamespaceAwareParallelModeCacheService.PARALLEL_MODE_BID_DETAILS_CACHE_NAME, ttlSeconds);
	    	oneOf (parallelModeBidDetails).toMap(kryoManager);
	    		will(returnValue(map));
	    	
		}});
		namespaceAwareParallelModeCacheService.saveBidDetails(key, parallelModeBidDetails);
	}
}

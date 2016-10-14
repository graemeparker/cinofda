package com.adfonic.adserver.impl.icache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.ParallelModeBidDetails;
import com.adfonic.cache.CacheManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:adfonic-adserver-test-context.xml")
@Ignore
public class TestSharedNamespaceParallelModeCacheServiceIT extends BaseAdserverTest{
	
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private KryoManager kryoManager;
	private int ttlSeconds;
	private SharedNamespaceParallelModeCacheService sharedNamespaceParallelModeCacheService; 
	private static final String KEY_PREFIX = "par.";
	@Before
	public void initTests(){
		ttlSeconds = randomInteger(60);
		sharedNamespaceParallelModeCacheService = new SharedNamespaceParallelModeCacheService(cacheManager, kryoManager, ttlSeconds);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService01_getBidDetails(){
		final String key = randomAlphaNumericString(10);
		ParallelModeBidDetails parallelModeBidDetails = sharedNamespaceParallelModeCacheService.getBidDetails(key);
		assertNull(parallelModeBidDetails);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService02_getBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = getImpression();
		final Map<String,Serializable> map = new HashMap<String, Serializable>();
		String ipAddress = "145.67.89.12";
		map.put("ip", ipAddress);
		sharedNamespaceParallelModeCacheService.saveBidDetails(key, getBidDetails(ipAddress, impression));
		ParallelModeBidDetails parallelModeBidDetails = sharedNamespaceParallelModeCacheService.getBidDetails(key);
		assertEquals(ipAddress,parallelModeBidDetails.getIpAddress());
		assertEquals(impression,parallelModeBidDetails.getImpression());
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService03_getAndRemoveBidDetails(){
		final String key = randomAlphaNumericString(10);
		ParallelModeBidDetails parallelModeBidDetails = sharedNamespaceParallelModeCacheService.getAndRemoveBidDetails(key);
		assertNull(parallelModeBidDetails);
	}
	@Test
	public void testSharedNamespaceRtbCacheService04_getAndRemoveBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = getImpression();
		final Map<String,Serializable> map = new HashMap<String, Serializable>();
		String ipAddress = "145.67.89.12";
		map.put("ip", ipAddress);
		sharedNamespaceParallelModeCacheService.saveBidDetails(key, getBidDetails(ipAddress, impression));
		ParallelModeBidDetails parallelModeBidDetails = sharedNamespaceParallelModeCacheService.getAndRemoveBidDetails(key);
		assertEquals(ipAddress,parallelModeBidDetails.getIpAddress());
		assertEquals(impression,parallelModeBidDetails.getImpression());
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService05_removeBidDetails(){
		final String key = randomAlphaNumericString(10);
		Boolean returnValue = sharedNamespaceParallelModeCacheService.removeBidDetails(key);
		assertFalse(returnValue);
	}
	
	@Test
	public void testSharedNamespaceRtbCacheService06_saveBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = getImpression();
		String ipAddress = "145.67.89.12";
		final ParallelModeBidDetails parallelModeBidDetails = getBidDetails(ipAddress, impression);
		final Map<String, Serializable> map = new HashMap<String, Serializable>();
		sharedNamespaceParallelModeCacheService.saveBidDetails(key, parallelModeBidDetails);
	}

}

package com.adfonic.adserver.impl.icache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
public class TestNamespaceAwareParallelModeCacheServiceIT extends
		BaseAdserverTest {

	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private KryoManager kryoManager;
	private int ttlSeconds;
	private NamespaceAwareParallelModeCacheService namespaceAwareParallelModeCacheService; 

	@Before
	public void initTests(){
		ttlSeconds = randomInteger(60);
		namespaceAwareParallelModeCacheService = new NamespaceAwareParallelModeCacheService(cacheManager, kryoManager, ttlSeconds);
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService01_getBidDetails(){
		final String key = randomAlphaNumericString(10);
		ParallelModeBidDetails parallelModeBidDetails = namespaceAwareParallelModeCacheService.getBidDetails(key);
		assertNull(parallelModeBidDetails);
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService02_getBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = getImpression();
		final Map<String,Serializable> map = new HashMap<String, Serializable>();
		String ipAddress = "145.67.89.12";
		final byte[] serializedImpression = null;
		map.put("ip", ipAddress);
		namespaceAwareParallelModeCacheService.saveBidDetails(key, getBidDetails(ipAddress, impression));
		ParallelModeBidDetails returnParallelModeBidDetails = namespaceAwareParallelModeCacheService.getBidDetails(key);
		assertEquals(ipAddress,returnParallelModeBidDetails.getIpAddress());
		assertEquals(impression,returnParallelModeBidDetails.getImpression());
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService03_getAndRemoveBidDetails(){
		final String key = randomAlphaNumericString(10);
		ParallelModeBidDetails parallelModeBidDetails = namespaceAwareParallelModeCacheService.getAndRemoveBidDetails(key);
		assertNull(parallelModeBidDetails);
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService04_getAndRemoveBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = getImpression();
		final Map<String,Serializable> map = new HashMap<String, Serializable>();
		String ipAddress = "145.67.89.12";
		final byte[] serializedImpression = null;
		map.put("ip", ipAddress);
		namespaceAwareParallelModeCacheService.saveBidDetails(key, getBidDetails(ipAddress, impression));
		ParallelModeBidDetails returnParallelModeBidDetails = namespaceAwareParallelModeCacheService.getAndRemoveBidDetails(key);
		assertEquals(ipAddress,returnParallelModeBidDetails.getIpAddress());
		assertEquals(impression,returnParallelModeBidDetails.getImpression());
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService05_removeBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = getImpression();
		String ipAddress = "145.67.89.12";
		namespaceAwareParallelModeCacheService.saveBidDetails(key, getBidDetails(ipAddress, impression));
		Boolean returnValue = namespaceAwareParallelModeCacheService.removeBidDetails(key);
		assertTrue(returnValue);
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService06_removeBidDetails(){
		final String key = randomAlphaNumericString(10);
		Boolean returnValue = namespaceAwareParallelModeCacheService.removeBidDetails(key);
		assertFalse(returnValue);
	}
	
	@Test
	public void testNamespaceAwareParallelModeCacheService07_saveBidDetails(){
		final String key = randomAlphaNumericString(10);
		final Impression impression = getImpression();
		String ipAddress = "145.67.89.12";
		final ParallelModeBidDetails parallelModeBidDetails = getBidDetails(ipAddress, impression);
		namespaceAwareParallelModeCacheService.saveBidDetails(key, parallelModeBidDetails);
		ParallelModeBidDetails returnParallelModeBidDetails = namespaceAwareParallelModeCacheService.getBidDetails(key);
		assertEquals(ipAddress,returnParallelModeBidDetails.getIpAddress());
		assertEquals(impression,returnParallelModeBidDetails.getImpression());
	}
}

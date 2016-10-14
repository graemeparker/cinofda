package com.adfonic.adserver.impl.icache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import com.adfonic.cache.CacheManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:adfonic-adserver-test-context.xml")
@Ignore
public class TestNamespaceAwareImpressionServiceIT extends BaseAdserverTest{
	
	private NamespaceAwareImpressionService namespaceAwareImpressionService;
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private KryoManager kryoManager;
	
	@Before
	public void initTests(){
		namespaceAwareImpressionService = new NamespaceAwareImpressionService(cacheManager, kryoManager);
	}
	
	@Test
	public void testNamespaceAwareImpressionService01_doGetImpression(){
		final Impression impression = getImpression();
		namespaceAwareImpressionService.saveImpression(impression);
		Impression returnImpression = namespaceAwareImpressionService.getImpression(impression.getExternalID());
		assertNotNull(returnImpression);
		assertEquals(impression, returnImpression);
	}
	
	@Test
	public void testNamespaceAwareImpressionService02_doGetImpression(){
		String impressionExternalId = randomAlphaNumericString(10);
		Impression returnImpression = namespaceAwareImpressionService.doGetImpression(impressionExternalId);
		assertNull(returnImpression);
	}
	
	@Test
	public void testNamespaceAwareImpressionService04_getStaticImpression(){
		final Long adSpaceId = randomLong();
		final String staticImpressionId = randomAlphaNumericString(10);
		Impression returnImpression = namespaceAwareImpressionService.getStaticImpression(adSpaceId, staticImpressionId);
		assertNull(returnImpression);
	}
	

	@Test
	public void testNamespaceAwareImpressionService05_getStaticImpression(){
		final Long adSpaceId = randomLong();
		final String staticImpressionId = randomAlphaNumericString(10);
		Impression returnImpression = namespaceAwareImpressionService.getStaticImpression(adSpaceId, staticImpressionId);
		assertNull(returnImpression);
	}
	
	
	@Test
	public void testNamespaceAwareImpressionService06_saveStaticImpression(){
		final String staticImpressionId = randomAlphaNumericString(10);
		final Impression impression = getImpression();
		namespaceAwareImpressionService.saveImpression(impression);
		namespaceAwareImpressionService.saveStaticImpression(impression.getAdSpaceId(), staticImpressionId,impression);
		Impression returnImpression = namespaceAwareImpressionService.getStaticImpression(impression.getAdSpaceId(), staticImpressionId);
		assertNotNull(returnImpression);
		assertEquals(impression, returnImpression);
	}
	
	@Test
	public void testNamespaceAwareImpressionService07_trackBeacon(){
		final Impression impression = getImpression();
		Boolean returnValue = namespaceAwareImpressionService.trackBeacon(impression);
		assertTrue(returnValue);
	}
	
	@Test
	public void testNamespaceAwareImpressionService08_trackBeacon(){
		final Impression impression = getImpression();
		Boolean returnValue = namespaceAwareImpressionService.trackBeacon(impression);
		assertTrue(returnValue);
		returnValue = namespaceAwareImpressionService.trackBeacon(impression);
		assertFalse(returnValue);
	}
}

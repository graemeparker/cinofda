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
public class TestSharedNamespaceImpressionServiceIT extends BaseAdserverTest {

	private SharedNamespaceImpressionService sharedNamespaceImpressionService;
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private KryoManager kryoManager;
	

	@Before
	public void initTests(){
		sharedNamespaceImpressionService = new SharedNamespaceImpressionService(cacheManager, kryoManager);
	}
	
	@Test
	public void testSharedNamespaceImpressionService01_doGetImpression(){
		final Impression impression = getImpression();
		sharedNamespaceImpressionService.doSaveImpression(impression, impression.getExternalID());
		Impression returnImpression = sharedNamespaceImpressionService.doGetImpression(impression.getExternalID());
		assertNotNull(returnImpression);
		assertEquals(impression, returnImpression);
	}
	
	@Test
	public void testSharedNamespaceImpressionService02_doGetImpression(){
		String impressionExternalId = randomAlphaNumericString(10);
		Impression returnImpression = sharedNamespaceImpressionService.doGetImpression(impressionExternalId);
		assertNull(returnImpression);
	}
	
	@Test
	public void testSharedNamespaceImpressionService03_doSaveImpression(){
		final Impression impression = getImpression();
		sharedNamespaceImpressionService.doSaveImpression(impression, impression.getExternalID());
	}
	
	@Test
	public void testSharedNamespaceImpressionService04_getStaticImpression(){
		final Long adSpaceId = randomLong();
		final String staticImpressionId = randomAlphaNumericString(10);
		Impression returnImpression = sharedNamespaceImpressionService.getStaticImpression(adSpaceId, staticImpressionId);
		assertNull(returnImpression);
	}
	
	@Test
	public void testSharedNamespaceImpressionService05_getStaticImpression(){
		final Long adSpaceId = randomLong();
		final String staticImpressionId = randomAlphaNumericString(10);
		Impression returnImpression = sharedNamespaceImpressionService.getStaticImpression(adSpaceId, staticImpressionId);
		assertNull(returnImpression);
	}
	
	@Test
	public void testSharedNamespaceImpressionService06_saveStaticImpression(){
		final Impression impression = getImpression();
		sharedNamespaceImpressionService.saveStaticImpression(impression.getAdSpaceId(),impression.getExternalID(),impression);
	}
	
	@Test
	public void testSharedNamespaceImpressionService07_trackBeacon(){
		final Impression impression = getImpression();
		Boolean returnValue = sharedNamespaceImpressionService.trackBeacon(impression);
		assertTrue(returnValue);
		returnValue = sharedNamespaceImpressionService.trackBeacon(impression);
		assertFalse(returnValue);
	}
	
	@Test
	public void testSharedNamespaceImpressionService08_trackBeacon(){
		final Impression impression = getImpression();
		Boolean returnValue = sharedNamespaceImpressionService.trackBeacon(impression);
		assertTrue(returnValue);
	}
}

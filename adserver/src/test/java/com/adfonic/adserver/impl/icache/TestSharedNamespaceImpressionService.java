package com.adfonic.adserver.impl.icache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.cache.CacheManager;

public class TestSharedNamespaceImpressionService extends BaseAdserverTest {

	private SharedNamespaceImpressionService sharedNamespaceImpressionService;
	private CacheManager cacheManager;
	private KryoManager kryoManager;
	

	@Before
	public void initTests(){
		cacheManager = mock(CacheManager.class,"cacheManager");
		kryoManager = mock(KryoManager.class,"kryoManager");
		sharedNamespaceImpressionService = new SharedNamespaceImpressionService(cacheManager, kryoManager);
	}
	
	@Test
	public void testSharedNamespaceImpressionService01_doGetImpression(){
		final byte[] data = new byte[20];
		final Impression impression = mock(Impression.class);
		String impressionExternalId = randomAlphaNumericString(10);
		
		expect(new Expectations() {{
		    oneOf (cacheManager).get(with(any(String.class)), with(data.getClass())); will(returnValue(data));
		    oneOf (kryoManager).readObject(data, Impression.class); will(returnValue(impression));
		}});
		
		Impression returnImpression = sharedNamespaceImpressionService.doGetImpression(impressionExternalId);
		assertNotNull(returnImpression);
		assertEquals(impression, returnImpression);
	}
	
	@Test
	public void testSharedNamespaceImpressionService02_doGetImpression(){
		final byte[] data = null;
		String impressionExternalId = randomAlphaNumericString(10);
		
		expect(new Expectations() {{
		    oneOf (cacheManager).get(with(any(String.class)), with((new byte[1]).getClass())); will(returnValue(data));
		}});
		
		Impression returnImpression = sharedNamespaceImpressionService.doGetImpression(impressionExternalId);
		assertNull(returnImpression);
	}
	
	@Test
	public void testSharedNamespaceImpressionService03_doSaveImpression(){
		final byte[] data = new byte[10];
		final Impression impression = mock(Impression.class);
		String impressionExternalId = randomAlphaNumericString(10);
		
		expect(new Expectations() {{
		    oneOf (cacheManager).set(with(any(String.class)), with(any((new byte[1]).getClass())),with(any(int.class)));
		    oneOf (kryoManager).writeObject(impression); will(returnValue(data));
		}});

		sharedNamespaceImpressionService.doSaveImpression(impression, impressionExternalId);
	}
	@Test
	public void testSharedNamespaceImpressionService04_getStaticImpression(){
		final Long adSpaceId = randomLong();
		final String impressionExternalId = null;
		final String staticImpressionId = randomAlphaNumericString(10);
		
		expect(new Expectations() {{
			oneOf (cacheManager).get(with(any(String.class)), with(String.class)); will(returnValue(impressionExternalId));

		}});
		Impression returnImpression = sharedNamespaceImpressionService.getStaticImpression(adSpaceId, staticImpressionId);
		assertNull(returnImpression);
	}
	
	@Test
	public void testSharedNamespaceImpressionService05_getStaticImpression(){
		final Long adSpaceId = randomLong();
		final String impressionExternalId = randomAlphaNumericString(10);
		final String staticImpressionId = randomAlphaNumericString(10);
		final byte[] data = null;
		
		expect(new Expectations() {{
			oneOf (cacheManager).get(with(any(String.class)), with(String.class)); will(returnValue(impressionExternalId));

		    oneOf (cacheManager).get(with(any(String.class)), with((new byte[1]).getClass())); will(returnValue(data));

		}});
		Impression returnImpression = sharedNamespaceImpressionService.getStaticImpression(adSpaceId, staticImpressionId);
		assertNull(returnImpression);
	}
	
	@Test
	public void testSharedNamespaceImpressionService06_saveStaticImpression(){
		final String staticImpressionId = randomAlphaNumericString(10);
		final Impression impression = mock(Impression.class,"impression");
		final String impressionExternalId = randomAlphaNumericString(10);
		
		final Long adSpaceId = randomLong();
		
		expect(new Expectations() {{
			oneOf (impression).getExternalID(); will(returnValue(impressionExternalId));

			oneOf (cacheManager).set(with(any(String.class)), with(any(String.class)),with(any(Integer.class))); 

		}});
		sharedNamespaceImpressionService.saveStaticImpression(adSpaceId, staticImpressionId,impression);
	}
	@Test
	public void testSharedNamespaceImpressionService07_trackBeacon(){
		final Impression impression = mock(Impression.class,"impression");
		final String impressionExternalId = randomAlphaNumericString(10);
		final Boolean tracked = true;
		
		expect(new Expectations() {{
			oneOf (impression).getExternalID(); will(returnValue(impressionExternalId));
			oneOf (cacheManager).get(with(any(String.class)), with(Boolean.class)); will(returnValue(tracked));
		}});
		Boolean returnValue = sharedNamespaceImpressionService.trackBeacon(impression);
		assertFalse(returnValue);
	}
	
	@Test
	public void testSharedNamespaceImpressionService08_trackBeacon(){
		final Impression impression = mock(Impression.class,"impression");
		final String impressionExternalId = randomAlphaNumericString(10);
		final Boolean tracked = null;
		
		expect(new Expectations() {{
			oneOf (impression).getExternalID(); will(returnValue(impressionExternalId));
			oneOf (cacheManager).get(with(any(String.class)), with(Boolean.class)); will(returnValue(tracked));
			
			oneOf (cacheManager).set(with(any(String.class)), with(any(Boolean.class)),with(any(Integer.class)));
		}});
		Boolean returnValue = sharedNamespaceImpressionService.trackBeacon(impression);
		assertTrue(returnValue);
	}
}

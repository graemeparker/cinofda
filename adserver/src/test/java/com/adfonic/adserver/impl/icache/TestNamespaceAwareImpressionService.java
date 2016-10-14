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

public class TestNamespaceAwareImpressionService extends BaseAdserverTest{
	
	private NamespaceAwareImpressionService namespaceAwareImpressionService;
	private CacheManager cacheManager;
	private KryoManager kryoManager;
	

	@Before
	public void initTests(){
		cacheManager = mock(CacheManager.class,"cacheManager");
		kryoManager = mock(KryoManager.class,"kryoManager");
		namespaceAwareImpressionService = new NamespaceAwareImpressionService(cacheManager, kryoManager);
		
	}
	
	@Test
	public void testNamespaceAwareImpressionService01_doGetImpression(){
		final byte[] data = new byte[20];
		final Impression impression = mock(Impression.class);
		String impressionExternalId = randomAlphaNumericString(10);
		
		expect(new Expectations() {{
		    oneOf (cacheManager).get(with(any(String.class)), with(any(String.class)), with.is(any(Class.class))); will(returnValue(data));
		    oneOf (kryoManager).readObject(data, Impression.class); will(returnValue(impression));
		}});
		
		Impression returnImpression = namespaceAwareImpressionService.doGetImpression(impressionExternalId);
		assertNotNull(returnImpression);
		assertEquals(impression, returnImpression);
	}
	
	@Test
	public void testNamespaceAwareImpressionService02_doGetImpression(){
		final byte[] data = null;
		String impressionExternalId = randomAlphaNumericString(10);
		
		expect(new Expectations() {{
		    oneOf (cacheManager).get(with(any(String.class)), with(any(String.class)), with.is(any(Class.class))); will(returnValue(data));
		}});
		
		Impression returnImpression = namespaceAwareImpressionService.doGetImpression(impressionExternalId);
		assertNull(returnImpression);
	}

	@Test
	public void testNamespaceAwareImpressionService03_doGetImpression(){
		final byte[] data = new byte[10];
		final Impression impression = mock(Impression.class);
		String impressionExternalId = randomAlphaNumericString(10);
		
		expect(new Expectations() {{
		    oneOf (cacheManager).set(with(any(String.class)), with(any((new byte[1]).getClass())),with(any(String.class)),with(any(int.class)));
		    oneOf (kryoManager).writeObject(impression); will(returnValue(data));
		}});

		namespaceAwareImpressionService.doSaveImpression(impression, impressionExternalId);
	}
	@Test
	public void testNamespaceAwareImpressionService04_getStaticImpression(){
		final Long adSpaceId = randomLong();
		final String impressionExternalId = null;
		final String staticImpressionId = randomAlphaNumericString(10);
		
		expect(new Expectations() {{
			oneOf (cacheManager).get(with(any(String.class)), with(any(String.class)), with.is(any(Class.class))); will(returnValue(impressionExternalId));

		}});
		Impression returnImpression = namespaceAwareImpressionService.getStaticImpression(adSpaceId, staticImpressionId);
		assertNull(returnImpression);
	}
	
	@Test
	public void testNamespaceAwareImpressionService05_getStaticImpression(){
		final Long adSpaceId = randomLong();
		final String impressionExternalId = randomAlphaNumericString(10);
		final String staticImpressionId = randomAlphaNumericString(10);
		final byte[] data = null;
		
		expect(new Expectations() {{
			oneOf (cacheManager).get(with(any(String.class)), with(any(String.class)), with.is(any(Class.class))); will(returnValue(impressionExternalId));

		    oneOf (cacheManager).get(with(any(String.class)), with(any(String.class)), with.is(any(Class.class))); will(returnValue(data));

		}});
		Impression returnImpression = namespaceAwareImpressionService.getStaticImpression(adSpaceId, staticImpressionId);
		assertNull(returnImpression);
	}
	
	@Test
	public void testNamespaceAwareImpressionService06_saveStaticImpression(){
		final String staticImpressionId = randomAlphaNumericString(10);
		final Impression impression = mock(Impression.class,"impression");
		final String impressionExternalId = randomAlphaNumericString(10);
		
		final Long adSpaceId = randomLong();
		
		expect(new Expectations() {{
			oneOf (impression).getExternalID(); will(returnValue(impressionExternalId));

			oneOf (cacheManager).set(with(any(String.class)), with(any(String.class)),with(any(String.class)),with(any(Integer.class))); 

		}});
		namespaceAwareImpressionService.saveStaticImpression(adSpaceId, staticImpressionId,impression);
	}
	
	@Test
	public void testNamespaceAwareImpressionService07_trackBeacon(){
		final Impression impression = mock(Impression.class,"impression");
		final String impressionExternalId = randomAlphaNumericString(10);
		final Boolean tracked = true;
		
		expect(new Expectations() {{
			oneOf (impression).getExternalID(); will(returnValue(impressionExternalId));
			oneOf (cacheManager).get(with(any(String.class)), with(any(String.class)), with.is(any(Class.class))); will(returnValue(tracked));
		}});
		Boolean returnValue = namespaceAwareImpressionService.trackBeacon(impression);
		assertFalse(returnValue);
	}
	
	@Test
	public void testNamespaceAwareImpressionService08_trackBeacon(){
		final Impression impression = mock(Impression.class,"impression");
		final String impressionExternalId = randomAlphaNumericString(10);
		final Boolean tracked = null;
		
		expect(new Expectations() {{
			oneOf (impression).getExternalID(); will(returnValue(impressionExternalId));
			oneOf (cacheManager).get(with(any(String.class)), with(any(String.class)), with.is(any(Class.class))); will(returnValue(tracked));
			
			oneOf (cacheManager).set(with(any(String.class)), with(any(Boolean.class)),with(any(String.class)),with(any(Integer.class)));
		}});
		Boolean returnValue = namespaceAwareImpressionService.trackBeacon(impression);
		assertTrue(returnValue);
	}
}

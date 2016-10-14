package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.DomainCache;

public class TestPlatformDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	PlatformDeriver platformDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		platformDeriver = new PlatformDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void testPlatformDeriver01(){
		assertNull(platformDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testPlatformDeriver02(){
		final Map<String,String> props = new HashMap<String, String>();
		final DomainCache domainCache = mock(DomainCache.class);
		
		expect(new Expectations() {{
			//1
			oneOf (context).getAttribute(TargetingContext.DEVICE_PROPERTIES); will(returnValue(null));
			
			//2
			oneOf (context).getAttribute(TargetingContext.DEVICE_PROPERTIES); will(returnValue(props));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getPlatforms(); will(returnValue(null));
		}});
		
		assertNull(platformDeriver.getAttribute(TargetingContext.PLATFORM, context));
		assertNull(platformDeriver.getAttribute(TargetingContext.PLATFORM, context));
	}
	
}

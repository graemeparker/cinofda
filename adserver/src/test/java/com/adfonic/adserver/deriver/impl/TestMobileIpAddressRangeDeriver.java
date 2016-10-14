package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertNull;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.DomainCache;

public class TestMobileIpAddressRangeDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	MobileIpAddressRangeDeriver mobileIpAddressRangeDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		mobileIpAddressRangeDeriver = new MobileIpAddressRangeDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void testMobileIpAddressRangeDeriver01(){
		assertNull(mobileIpAddressRangeDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testMobileIpAddressRangeDeriver02(){
		final DomainCache domainCache = mock(DomainCache.class);
		expect(new Expectations() {{
			
			//1
			oneOf (context).getAttribute(Parameters.IP); will(returnValue(null));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getMobileIpAddressRange(null); will(returnValue(null));
			
			//2
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("localhost"));
			oneOf (context).getDomainCache(); will(returnValue(domainCache));
			oneOf (domainCache).getMobileIpAddressRange("localhost"); will(returnValue(null));
		}});
		
		assertNull(mobileIpAddressRangeDeriver.getAttribute(TargetingContext.MOBILE_IP_ADDRESS_RANGE, context));
		
		assertNull(mobileIpAddressRangeDeriver.getAttribute(TargetingContext.MOBILE_IP_ADDRESS_RANGE, context));
		
	}
	
}

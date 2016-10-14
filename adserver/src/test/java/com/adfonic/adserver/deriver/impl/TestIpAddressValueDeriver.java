package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;

public class TestIpAddressValueDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	IpAddressValueDeriver ipAddressValueDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		ipAddressValueDeriver = new IpAddressValueDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void testIpAddressValueDeriver01(){
		assertNull(ipAddressValueDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testIpAddressValueDeriver02(){
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("127.88.99.1"));
			oneOf (context).getAttribute(Parameters.IP); will(returnValue("hello"));
		}});
		
		long l = (Long)ipAddressValueDeriver.getAttribute(TargetingContext.IP_ADDRESS_VALUE, context);
		assertTrue(l != 0);
		ipAddressValueDeriver.getAttribute(TargetingContext.IP_ADDRESS_VALUE, context);	
	}
	
	

}

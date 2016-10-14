package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.Feature;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public class TestUseBeaconsDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	UseBeaconsDeriver useBeaconsDeriver;
	private TargetingContext context;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		useBeaconsDeriver = new UseBeaconsDeriver(deriverManager);
		context = mock(TargetingContext.class);
	}

	@Test
	public void testUseBeaconsDeriver01(){
		assertNull(useBeaconsDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}
	
	@Test
	public void testUseBeaconsDeriver02(){
		final IntegrationTypeDto integrationType = mock(IntegrationTypeDto.class);
		final Set<Feature> acceptedFeatures = new HashSet<Feature>();
		acceptedFeatures.add(Feature.ANIMATED_GIF);
		acceptedFeatures.add(Feature.BEACON);
		
		expect(new Expectations() {{
			//1
			oneOf (context).getAttribute(TargetingContext.INTEGRATION_TYPE); will(returnValue(null));
			//2
			oneOf (context).getAttribute(TargetingContext.INTEGRATION_TYPE); will(returnValue(integrationType));
			oneOf (integrationType).getSupportedFeatures(); will(returnValue(acceptedFeatures));
		}});
		boolean value = (Boolean)useBeaconsDeriver.getAttribute(TargetingContext.USE_BEACONS, context);
		assertTrue(value);
	    value = (Boolean)useBeaconsDeriver.getAttribute(TargetingContext.USE_BEACONS, context);
	    assertTrue(value);
	}
	
}

package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.Feature;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

@SuppressWarnings("unchecked")
public class TestAcceptedFeaturesDeriver extends BaseAdserverTest {

	DeriverManager deriverManager;
	AcceptedFeaturesDeriver acceptedFeaturesDeriver;
	private TargetingContext context;
	private IntegrationTypeDto integType;

	@Before
	public void initTests() {
		deriverManager = new DeriverManager();
		acceptedFeaturesDeriver = new AcceptedFeaturesDeriver(deriverManager);
		context = mock(TargetingContext.class);
		integType = mock(IntegrationTypeDto.class);
	}

	@Test
	public void testAcceptedFeaturesDeriver01(){

		assertNull(acceptedFeaturesDeriver.getAttribute(TargetingContext.MARKUP_AVAILABLE, context));
	}

	@Test
	public void testAcceptedFeaturesDeriver02(){
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.INTEGRATION_TYPE); will(returnValue(null));
		}});
		assertTrue(acceptedFeaturesDeriver.getAttribute(TargetingContext.ACCEPTED_FEATURES, context).equals(Collections.EMPTY_SET));
	}

	@Test
	public void testAcceptedFeaturesDeriver03(){
		final Set<Feature> acceptedFeatures = new HashSet<Feature>();
		acceptedFeatures.add(Feature.ANIMATED_GIF);
		acceptedFeatures.add(Feature.BEACON);
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.INTEGRATION_TYPE); will(returnValue(integType));
			oneOf (integType).getSupportedFeatures(); will(returnValue(acceptedFeatures));
			oneOf (context).getAttribute(Parameters.EXCLUDED_FEATURES); will(returnValue(""));
		}});
		assertTrue(acceptedFeaturesDeriver.getAttribute(TargetingContext.ACCEPTED_FEATURES, context).equals(acceptedFeatures));
	}

	@Test
	public void testAcceptedFeaturesDeriver04(){
		final Set<Feature> acceptedFeatures = new HashSet<Feature>();
		acceptedFeatures.add(Feature.ANIMATED_GIF);
		acceptedFeatures.add(Feature.BEACON);
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.INTEGRATION_TYPE); will(returnValue(integType));
			oneOf (integType).getSupportedFeatures(); will(returnValue(acceptedFeatures));
			oneOf (context).getAttribute(Parameters.EXCLUDED_FEATURES); will(returnValue("BEACON"));
		}});

		Set<Feature> expectedFeatures = (Set<Feature>)acceptedFeaturesDeriver.getAttribute(TargetingContext.ACCEPTED_FEATURES, context);
		assertTrue(expectedFeatures.size() == 1);
		assertTrue(expectedFeatures.contains(Feature.BEACON) == false);
	}

	@Test
	public void testAcceptedFeaturesDeriver05(){
		final Set<Feature> acceptedFeatures = new HashSet<Feature>();
		acceptedFeatures.add(Feature.ANIMATED_GIF);
		acceptedFeatures.add(Feature.BEACON);
		expect(new Expectations() {{
			oneOf (context).getAttribute(TargetingContext.INTEGRATION_TYPE); will(returnValue(integType));
			oneOf (integType).getSupportedFeatures(); will(returnValue(acceptedFeatures));
			oneOf (context).getAttribute(Parameters.EXCLUDED_FEATURES); will(returnValue("something"));
		}});

		Set<Feature> expectedFeatures = (Set<Feature>)acceptedFeaturesDeriver.getAttribute(TargetingContext.ACCEPTED_FEATURES, context);
		assertTrue(expectedFeatures.size() == 2);
		assertTrue(expectedFeatures.contains(Feature.BEACON) == true);
	}

}

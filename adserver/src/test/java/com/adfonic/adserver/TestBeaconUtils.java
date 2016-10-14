package com.adfonic.adserver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adfonic.domain.Feature;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public class TestBeaconUtils extends BaseAdserverTest {

	@Test
	public void testBeaconUtils1(){
		IntegrationTypeDto integrationType = null;
		assertTrue(BeaconUtils.shouldUseBeacons(integrationType));
	}
	
	@Test
	public void testBeaconUtils2(){
		IntegrationTypeDto integrationType = new IntegrationTypeDto();
        integrationType.setName("name");
        integrationType.setSystemName("systemName");
		assertFalse(BeaconUtils.shouldUseBeacons(integrationType));
	}
	@Test
	public void testBeaconUtils3(){
		IntegrationTypeDto integrationType = new IntegrationTypeDto();
        integrationType.setName("name");
        integrationType.setSystemName("systemName");
		integrationType.getSupportedFeatures().add(Feature.BEACON);
		assertTrue(BeaconUtils.shouldUseBeacons(integrationType));
	}
}

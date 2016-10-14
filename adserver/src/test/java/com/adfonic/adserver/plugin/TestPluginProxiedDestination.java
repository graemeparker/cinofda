package com.adfonic.adserver.plugin;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;

public class TestPluginProxiedDestination extends BaseAdserverTest {

	private CreativeDto creative;
	private AdserverPluginDto adserverPlugin;
	private DestinationType destinationType;
	private String destinationUrl;
	private DestinationDto destination;

	

	@Before
	public void setup() {
		
		creative = mock(CreativeDto.class,"creative");
		adserverPlugin = mock(AdserverPluginDto.class,"adserverPlugin");
		destination = mock(DestinationDto.class,"destination");
    	destinationType = DestinationType.ANDROID;

	}
	
	@Test
	public void testPluginProxiedDestination01(){
		
		PluginProxiedDestination ppd = new PluginProxiedDestination(adserverPlugin, destinationType, destinationUrl);
		assertEquals(adserverPlugin, ppd.getAdserverPlugin());
	}
	@Test
	public void testPluginProxiedDestination02(){
		final String systemName = randomAlphaString(20);
		expect(new Expectations() {{
			allowing (creative).getDestination();
				will(returnValue(destination));
			oneOf (destination).getDestinationType();
				will(returnValue(destinationType));
			oneOf (adserverPlugin).getSystemName();
				will(returnValue(systemName));
		}});
		PluginProxiedDestination ppd = new PluginProxiedDestination(adserverPlugin, creative, destinationUrl);
		assertEquals(adserverPlugin, ppd.getAdserverPlugin());
		System.out.println(ppd);
	}
}

package com.adfonic.adserver.impl;

import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.data.cache.util.Properties;
import com.adfonic.data.cache.util.PropertiesFactory;
import com.adfonic.test.AbstractAdfonicTest;

@RunWith(MockitoJUnitRunner.class)
public class MarkupGeneratorImplTest extends AbstractAdfonicTest {
	
	@InjectMocks
	private MarkupGeneratorImpl testObj;
	
	@Mock
	private PropertiesFactory propertiesFactory;
	@Mock
	private VelocityEngine velocityEngine;
	@Mock
	private DisplayTypeUtils displayTypeUtils;

	@Mock
	private Properties properties;
	
	final DateTime now = new DateTime(2014, 5, 19, 10, 59);
	
    @After
    public void after() {
        DateTimeUtils.setCurrentMillisSystem();
    }

	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Ignore
    @Test
    public void initTrusteePropertiesInitializesFromPropertiesFactory() throws Exception {
    	
//    	Mockito.when(propertiesFactory.getProperties()).thenReturn(properties);
//    	Mockito.when(properties.getProperty("truste.weve.aeskey")).thenReturn("truste.weve.aeskey");
//    	Mockito.when(properties.getProperty("truste.default.aeskey")).thenReturn("truste.default.aeskey");
//    	Mockito.when(properties.getProperty("truste.weve.pid")).thenReturn("truste.weve.pid");
//    	Mockito.when(properties.getProperty("truste.weve.web-aid")).thenReturn("truste.weve.web-aid");
//    	Mockito.when(properties.getProperty("truste.weve.app-aid")).thenReturn("truste.weve.app-aid");
//    	Mockito.when(properties.getProperty("truste.default.web-aid")).thenReturn("truste.default.web-aid");
//    	Mockito.when(properties.getProperty("truste.default.app-aid")).thenReturn("truste.default.app-aid");
//    	
//    	// act
//		testObj.initTrusteeProperties();
//    	
//		Assert.assertEquals("truste.weve.aeskey", testObj. trusteWeveAeskey);
//		Assert.assertEquals("truste.default.aeskey", testObj. trusteDefaultAeskey);
//		Assert.assertEquals("truste.weve.pid", testObj. trusteWevePid);
//		Assert.assertEquals("truste.weve.web-aid", testObj. trusteWeveWebAid);
//		Assert.assertEquals("truste.weve.app-aid", testObj. trusteWeveAppAid);
//		Assert.assertEquals("truste.default.web-aid", testObj. trusteDefaultWebAid );
//		Assert.assertEquals("truste.default.app-aid", testObj. trusteDefaultAppAid );
//		
//        Mockito.verify(propertiesFactory).getProperties();
    }
    
}

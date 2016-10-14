package com.adfonic.tasks.combined;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.domain.DeviceIdentifierType;
import com.byyd.middleware.device.service.DeviceManager;

@RunWith(MockitoJUnitRunner.class)
public class DeviceIdentifierValidatorTest {

	@Mock
	private DeviceManager deviceManager;

	private DeviceIdentifierValidator testObj;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        DeviceIdentifierType type1 = Mockito.mock(DeviceIdentifierType.class);
        Mockito.when(type1.getId()).thenReturn(1L);
        Mockito.when(type1.getValidationRegex()).thenReturn("^[0-9A-Fa-f]{40}$");
        
        DeviceIdentifierType type6 = Mockito.mock(DeviceIdentifierType.class);
        Mockito.when(type6.getId()).thenReturn(6L);
        Mockito.when(type6.getValidationRegex()).thenReturn("^([0-9A-Fa-f]{32}|[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12})$");
        
        List<DeviceIdentifierType> value = Arrays.asList(type1, type6);
        Mockito.when(deviceManager.getAllDeviceIdentifierTypes()).thenReturn(value);

        // act
        testObj = new DeviceIdentifierValidator(deviceManager);
        
        // verify constructor called deviceManager
        Mockito.verify(deviceManager).getAllDeviceIdentifierTypes();
        
    }
	
	@Test
	public void testValidPatterns() {

		Assert.assertTrue(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", 1));
		Assert.assertTrue(testObj.isDeviceIdValid("68753A44-4D6F-1226-9C60-0050E4C00067", 6));// apple
	}
	
	@Test
	public void testInvalidTypes() {
		
		Assert.assertFalse(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", -2));
		Assert.assertFalse(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", 0));
		Assert.assertFalse(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", 666));
	}
	
	@Test
	public void testInvalidPatterns() {

		Assert.assertFalse(testObj.isDeviceIdValid(null, 1));
		Assert.assertFalse(testObj.isDeviceIdValid("", 1));
		Assert.assertFalse(testObj.isDeviceIdValid("\n", 1));
		Assert.assertFalse(testObj.isDeviceIdValid("2b INVALID 137be2e1730235f5664094b831186", 1));
		Assert.assertFalse(testObj.isDeviceIdValid("2b INVALID 380be2e1730235f5664094b831186", 1));
		Assert.assertFalse(testObj.isDeviceIdValid("2b6 INVALID 37be2e1730235f5664094b831186", 1));
		Assert.assertFalse(testObj.isDeviceIdValid("6 invalid 226", 6));
		Assert.assertFalse(testObj.isDeviceIdValid("X b6f0cc904d137be2e1730235f5664094b831186", 6));
		Assert.assertFalse(testObj.isDeviceIdValid("8753A44-apple-1226-9C60-0050E4C00067", 6));
		Assert.assertFalse(testObj.isDeviceIdValid("X b6f0cc904d137be2e1730235f5664094b831186", 6));
		Assert.assertFalse(testObj.isDeviceIdValid("X b6f0cc904d137be2e1730235f5664094b831186", 6));
	}
}

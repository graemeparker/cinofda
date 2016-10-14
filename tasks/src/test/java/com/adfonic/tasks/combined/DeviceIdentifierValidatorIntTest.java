package com.adfonic.tasks.combined;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
		"classpath:/device-identifier-validator-test-context.xml"
		})
@Ignore
public class DeviceIdentifierValidatorIntTest {

	@Autowired
	private DeviceIdentifierValidator testObj;

	@Test
	public void testIsDeviceIdValid() {
		
		Assert.assertNotNull(testObj);

		// act
		Assert.assertTrue(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", 1));
		Assert.assertTrue(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", 2));
		Assert.assertTrue(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", 3));
		Assert.assertTrue(testObj.isDeviceIdValid("68753A444D6F1226", 4));
		Assert.assertTrue(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", 5));
		Assert.assertTrue(testObj.isDeviceIdValid("68753A44-4D6F-1226-9C60-0050E4C00067", 6));// apple
		Assert.assertTrue(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", 7));
		Assert.assertTrue(testObj.isDeviceIdValid("2b6f0cc904d137be2e1730235f5664094b831186", 8));

		Assert.assertFalse(testObj.isDeviceIdValid(null, 1));
		Assert.assertFalse(testObj.isDeviceIdValid("", 1));
		Assert.assertFalse(testObj.isDeviceIdValid("\n", 1));
		Assert.assertFalse(testObj.isDeviceIdValid("2b INVALID 137be2e1730235f5664094b831186", 1));
		Assert.assertFalse(testObj.isDeviceIdValid("2b INVALID 380be2e1730235f5664094b831186", 2));
		Assert.assertFalse(testObj.isDeviceIdValid("2b6 INVALID 37be2e1730235f5664094b831186", 3));
		Assert.assertFalse(testObj.isDeviceIdValid("6 invalid 226", 4));
		Assert.assertFalse(testObj.isDeviceIdValid("X b6f0cc904d137be2e1730235f5664094b831186", 5));
		Assert.assertFalse(testObj.isDeviceIdValid("8753A44-apple-1226-9C60-0050E4C00067", 6));
		Assert.assertFalse(testObj.isDeviceIdValid("X b6f0cc904d137be2e1730235f5664094b831186", 7));
		Assert.assertFalse(testObj.isDeviceIdValid("X b6f0cc904d137be2e1730235f5664094b831186", 8));
	}
	
}

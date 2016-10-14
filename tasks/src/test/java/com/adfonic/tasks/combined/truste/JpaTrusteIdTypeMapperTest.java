package com.adfonic.tasks.combined.truste;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.domain.DeviceIdentifierType;
import com.byyd.middleware.device.service.DeviceManager;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class JpaTrusteIdTypeMapperTest {

	@Mock
	private DeviceManager deviceManager;
	
	private JpaTrusteIdTypeMapper testObj;
	
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        DeviceIdentifierType type1 = Mockito.mock(DeviceIdentifierType.class);
        Mockito.when(type1.getId()).thenReturn(1L);
        Mockito.when(type1.getTrusteIdType()).thenReturn("AnID-SHA1");
        
        DeviceIdentifierType type6 = Mockito.mock(DeviceIdentifierType.class);
        Mockito.when(type6.getId()).thenReturn(6L);
        Mockito.when(type6.getTrusteIdType()).thenReturn("IFA");
        
        List<DeviceIdentifierType> value = Arrays.asList(type1, type6);
        Mockito.when(deviceManager.getAllDeviceIdentifierTypes()).thenReturn(value);
    }
	
	@Test
	public void testMapAdfonicIdType() {
		// act
		testObj = new JpaTrusteIdTypeMapper(deviceManager);
		
		// verify constructor called deviceManager
		Mockito.verify(deviceManager).getAllDeviceIdentifierTypes();
		
		// assert mapping
		Assert.assertEquals(1, testObj.mapAdfonicIdType("AnID-SHA1"));
		Assert.assertEquals(6, testObj.mapAdfonicIdType("IFA"));  

		Assert.assertEquals(0, testObj.mapAdfonicIdType("anyOtherId"));     
		Assert.assertEquals(0, testObj.mapAdfonicIdType("xyz"));     
		Assert.assertEquals(0, testObj.mapAdfonicIdType(""));     
		Assert.assertEquals(0, testObj.mapAdfonicIdType(null));  
		
	}

}

package com.adfonic.adserver.deriver.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.retargeting.redis.DeviceData;
import com.adfonic.util.stats.CounterManager;

@RunWith(MockitoJUnitRunner.class)
public class DeviceAudiencesDeriverTest {

    @Mock
    private DeriverManager deriverManager;

    @Mock
    private CounterManager counterManager;
    @Mock
    private TargetingContext context;

    private DeviceAudiencesDeriver testObj;

    @Before()
    public void before() {
        MockitoAnnotations.initMocks(this);
        testObj = new DeviceAudiencesDeriver(deriverManager);
        testObj.counterManager = counterManager;
    }

    @Test
    public void testGetWrongAttributeReturnsNull() {
        Object result = testObj.getAttribute("some unknown attribute", context);

        Assert.assertNull(result);
        Mockito.verifyZeroInteractions(counterManager);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAttribute_DEVICE_AUDIENCES() {
        Map<Long, String> deviceIdentifiers = Mockito.mock(Map.class);
        Set<Long> eligibleAudienceIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));

        DeviceData deviceData = new DeviceData();
        deviceData.setAudienceIds(eligibleAudienceIds);
        Set<DeviceData> ddSet = new HashSet<>(Arrays.asList(deviceData));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA)).thenReturn(ddSet);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS)).thenReturn(deviceIdentifiers);

        // act
        Object result = testObj.getAttribute(TargetingContext.DEVICE_AUDIENCES, context);

        Assert.assertEquals(eligibleAudienceIds, result);
        Mockito.verify(counterManager).incrementCounter("DeviceAudiencesDeriverTotalCall");
        Mockito.verify(counterManager).incrementCounter(Mockito.eq("DeviceAudiencesDeriverTotalTime"), Mockito.anyLong());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAttributeReturnsEmptySetWhenContextReturnsNull() {
        Map<Long, String> deviceIdentifiers = Mockito.mock(Map.class);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA)).thenReturn(null);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS)).thenReturn(deviceIdentifiers);

        // act
        Set<Long> result = (Set<Long>) testObj.getAttribute(TargetingContext.DEVICE_AUDIENCES, context);

        Assert.assertTrue(result.isEmpty());
        Mockito.verify(counterManager).incrementCounter("DeviceAudiencesDeriverTotalCall");
        Mockito.verify(counterManager).incrementCounter(Mockito.eq("DeviceAudiencesDeriverTotalTime"), Mockito.anyLong());
    }

    @Test
    public void testCanDeriveMoreThanOnce() {
        Assert.assertFalse(testObj.canDeriveMoreThanOnce(TargetingContext.DEVICE_IDENTIFIERS));
        Mockito.verifyZeroInteractions(counterManager);
    }

    @Test
    public void testCanDeriveMoreThanOnceUnknownAttribute() {
        Assert.assertFalse(testObj.canDeriveMoreThanOnce("some unknown attribute"));
        Mockito.verifyZeroInteractions(counterManager);
    }

}

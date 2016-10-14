package com.adfonic.adserver.deriver.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import com.adfonic.dmp.cache.OptOutType;
import com.adfonic.retargeting.redis.DeviceData;
import com.adfonic.util.stats.CounterManager;

@RunWith(MockitoJUnitRunner.class)
public class DeviceOptOutDeriverTest {

    @Mock
    private DeriverManager deriverManager;

    @Mock
    private CounterManager counterManager;
    @Mock
    private TargetingContext context;

    private DeviceOptOutDeriver testObj;

    @Before()
    public void before() {
        MockitoAnnotations.initMocks(this);
        testObj = new DeviceOptOutDeriver(deriverManager);
        testObj.counterManager = counterManager;
    }

    @Test
    public void testGetAttributeNoOptOuts() {

        String deviceId = "deviceId";
        long deviceIdentifierTypeId = 7;

        Map<Long, String> deviceIdentifiers = new HashMap<Long, String>();
        deviceIdentifiers.put(deviceIdentifierTypeId, deviceId);

        Set<DeviceData> ddSet = new HashSet<>();
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA)).thenReturn(ddSet);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS)).thenReturn(deviceIdentifiers);

        // act
        Object result = testObj.getAttribute(TargetingContext.DEVICE_OPT_OUT, context);
        Assert.assertEquals(Collections.emptySet(), result);

        Mockito.verify(counterManager).incrementCounter("DeviceOptOutDeriverTotalCall");
    }

    @Test
    public void testGetAttributeWeveOptOut() {

        String deviceId = "deviceId";
        long deviceIdentifierTypeId = 7;

        Map<Long, String> deviceIdentifiers = new HashMap<Long, String>();
        deviceIdentifiers.put(deviceIdentifierTypeId, deviceId);

        DeviceData deviceData = new DeviceData();
        deviceData.setOptOutType(OptOutType.weve);
        Set<DeviceData> ddSet = new HashSet<>(Arrays.asList(deviceData));
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_DATA)).thenReturn(ddSet);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS)).thenReturn(deviceIdentifiers);

        // act
        Object result = testObj.getAttribute(TargetingContext.DEVICE_OPT_OUT, context);
        Assert.assertEquals(new HashSet<>(Arrays.asList(OptOutType.weve)), result);

        Mockito.verify(counterManager).incrementCounter("DeviceOptOutDeriverTotalCall");
    }

    @Test
    public void testCanDeriveMoreThanOnce() {
        Assert.assertFalse(testObj.canDeriveMoreThanOnce(TargetingContext.DEVICE_OPT_OUT));
        Mockito.verifyZeroInteractions(counterManager);
    }

    @Test
    public void testCanDeriveMoreThanOnceUnknownAttribute() {
        Assert.assertFalse(testObj.canDeriveMoreThanOnce("some unknown attribute"));
        Mockito.verifyZeroInteractions(counterManager);
    }

}

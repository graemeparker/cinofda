package com.adfonic.adserver.deriver.impl;

import java.util.HashMap;
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
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.dmp.cache.OptOutType;
import com.adfonic.retargeting.redis.DeviceData;
import com.adfonic.retargeting.redis.DeviceDataRedisReader;
import com.adfonic.util.stats.CounterManager;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DeviceDataDeriverTest {

    @Mock
    private DeriverManager deriverManager;
    @Mock
    private DeviceDataRedisReader cacheReader;

    @Mock
    private CounterManager counterManager;
    @Mock
    private TargetingContext context;

    private DeviceDataDeriver testObj;

    @Before()
    public void before() {
        MockitoAnnotations.initMocks(this);
        testObj = new DeviceDataDeriver(deriverManager, cacheReader);
        testObj.counterManager = counterManager;
    }

    @Test
    public void testGetWrongAttributeReturnsNull() {
        Object result = testObj.getAttribute("some unknown attribute", context);

        Assert.assertNull(result);
        Mockito.verifyZeroInteractions(counterManager);
    }

    @Test
    public void testHappyPath() {
        Map<Long, String> deviceIdentifiers = new HashMap<>();
        deviceIdentifiers.put(1L, "device1");
        deviceIdentifiers.put(7L, "device7");

        DeviceData dd1 = new DeviceData();
        dd1.setOptOutType(OptOutType.weve);
        DeviceData dd7 = new DeviceData();
        dd7.setOptOutType(OptOutType.global);

        Mockito.when(cacheReader.getData("1.device1")).thenReturn(dd1);
        Mockito.when(cacheReader.getData("7.device7")).thenReturn(dd7);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS)).thenReturn(deviceIdentifiers);

        //act
        Set<DeviceData> result = (Set<DeviceData>) testObj.getAttribute(TargetingContext.DEVICE_DATA, context);

        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains(dd1));
        Assert.assertTrue(result.contains(dd7));

        Mockito.verify(counterManager, Mockito.times(2)).incrementCounter(AsCounter.DeviceRedisCall);
    }

    @Test
    public void testWithNoData() {
        Map<Long, String> deviceIdentifiers = new HashMap<>();
        deviceIdentifiers.put(1L, "device1");
        deviceIdentifiers.put(7L, "device7");

        Mockito.when(cacheReader.getData("1.device1")).thenReturn(null);
        Mockito.when(cacheReader.getData("7.device7")).thenReturn(null);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS)).thenReturn(deviceIdentifiers);

        //act
        Set<DeviceData> result = (Set<DeviceData>) testObj.getAttribute(TargetingContext.DEVICE_DATA, context);

        Assert.assertTrue(result.isEmpty());

        Mockito.verify(counterManager, Mockito.times(2)).incrementCounter(AsCounter.DeviceRedisCall);
    }

    @Test
    public void testWithNoIdentifiers() {
        Map<Long, String> deviceIdentifiers = null;

        Mockito.when(context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS)).thenReturn(deviceIdentifiers);

        //act
        Set<DeviceData> result = (Set<DeviceData>) testObj.getAttribute(TargetingContext.DEVICE_DATA, context);

        Assert.assertTrue(result.isEmpty());

        Mockito.verifyZeroInteractions(counterManager);
    }

}

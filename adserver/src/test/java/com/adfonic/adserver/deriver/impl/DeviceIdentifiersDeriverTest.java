package com.adfonic.adserver.deriver.impl;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;

@RunWith(MockitoJUnitRunner.class)
public class DeviceIdentifiersDeriverTest {

    DeviceIdentifierTypeDto ifa = makeType(6L, "ifa", 500, false);
    DeviceIdentifierTypeDto hifa = makeType(7L, "hifa", 499, true);
    DeviceIdentifierTypeDto atid = makeType(8L, "atid", 99, true);
    DeviceIdentifierTypeDto adid = makeType(9L, "adid", 600, true);
    DeviceIdentifierTypeDto gouid = makeType(10L, "gouid", 700, true);

    @Mock
    private DeriverManager deriverManager;
    
    @Mock
    private TargetingContext context;
    @Mock
    private DomainCache domainCache;
    
    DeviceIdentifiersDeriver testObj;

    Map<String, Long> typeIdsBySystemName = new TreeMap<>();
    private SortedSet<DeviceIdentifierTypeDto> allTypes = populateTypes();

    @Before
    public void before() {
        testObj = new DeviceIdentifiersDeriver(deriverManager);
        
        Mockito.when(context.getDomainCache()).thenReturn(domainCache);
        Mockito.when(domainCache.getAllDeviceIdentifierTypes()).thenReturn(allTypes);
        Mockito.when(domainCache.getDeviceIdentifierTypeIdsBySystemName()).thenReturn(typeIdsBySystemName);
        
        Mockito.when(context.getAttribute("d.atid")).thenReturn("");
        Mockito.when(context.getAttribute("d.android")).thenReturn("");
    }

    @Test
    public void testGetUnsupportedAttribute() {
        Object attribute = testObj.getAttribute("unsupported", context);
        Assert.assertNull(attribute);
    }
    
    @Test
    public void testGetAttribute_givenIFA() {
        Mockito.when(context.getAttribute("d.ifa")).thenReturn("12345678-abcd-1234-abcd-1234567890EF");
        Mockito.when(context.getAttribute("d.hifa")).thenReturn(null);
        
        // act
        @SuppressWarnings("unchecked")
        Map<Long, String> result = (Map<Long, String>) testObj.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, context);
        
        Assert.assertEquals("12345678-ABCD-1234-ABCD-1234567890EF", result.get(ifa.getId()));
        Assert.assertEquals("20a50a8cd73d40a6b884a135f1850dfc1387055f", result.get(hifa.getId()));
    }
    
    @Test
    public void testGetAttribute_givenBoth_IFA_HIFA() {
        Mockito.when(context.getAttribute("d.ifa")).thenReturn("12345678-abcd-1234-abcd-1234567890EF");
        Mockito.when(context.getAttribute("d.hifa")).thenReturn("AAAAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaFFFFFF");
        
        // act
        @SuppressWarnings("unchecked")
        Map<Long, String> result = (Map<Long, String>) testObj.getAttribute(TargetingContext.DEVICE_IDENTIFIERS, context);
        
        Assert.assertEquals("12345678-ABCD-1234-ABCD-1234567890EF", result.get(ifa.getId()));
        Assert.assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaffffff", result.get(hifa.getId()));
    }

    @Test
    public void testMakeParameterName() {
        Assert.assertEquals("d.ifa",DeviceIdentifiersDeriver.makeParameterName(ifa));
        Assert.assertEquals("d.hifa",DeviceIdentifiersDeriver.makeParameterName(hifa));
        Assert.assertEquals("d.atid",DeviceIdentifiersDeriver.makeParameterName(atid));
        Assert.assertEquals("d.adid",DeviceIdentifiersDeriver.makeParameterName(adid));
        Assert.assertEquals("d.gouid",DeviceIdentifiersDeriver.makeParameterName(gouid));
    }

    
    private SortedSet<DeviceIdentifierTypeDto> populateTypes() {
        SortedSet<DeviceIdentifierTypeDto> set = new TreeSet<>();
        
        set.add(ifa);
        set.add(hifa);
        set.add(atid);
        set.add(adid);
        set.add(gouid);
        
        for(DeviceIdentifierTypeDto dto:set) {
            String systemName = dto.getSystemName();
            long id = dto.getId();
            typeIdsBySystemName.put(systemName, id);
        }
        return set;
    }

    private DeviceIdentifierTypeDto makeType(Long id, String systemName, int precedenceOrder, boolean secure) {
        DeviceIdentifierTypeDto type = new DeviceIdentifierTypeDto();
        type.setId(id);
        type.setSystemName(systemName);
        type.setPrecedenceOrder(precedenceOrder);
//        type.setValidationPattern(validationPattern);
        type.setSecure(secure);

        return type;
    }
}

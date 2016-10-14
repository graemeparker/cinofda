package com.adfonic.adserver;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.domain.cache.DomainCache;

@RunWith(MockitoJUnitRunner.class)
public class DestinationUrlUtilsTest {

    @Mock
    private TargetingContext context;

    @Mock
    private Impression impression;

    @Mock
    private DomainCache domainCache;

    private Map<String, Long> typeIdsBySystemName = populateTypes();

    @Before
    public void before() {
        Map<Long, String> impressionIds = new TreeMap<>();
        impressionIds.put(6L, "ifa-123");
        impressionIds.put(7L, "hifa-123");
        impressionIds.put(9L, "adid-123");
        impressionIds.put(10L, "adid_md5-123");
        impressionIds.put(13L, "idfa_md5-123");

        Mockito.when(context.getDomainCache()).thenReturn(domainCache);
        Mockito.when(domainCache.getDeviceIdentifierTypeIdsBySystemName()).thenReturn(typeIdsBySystemName);
        Mockito.when(impression.getDeviceIdentifiers()).thenReturn(impressionIds);
        Mockito.when(context.getAttribute(TargetingContext.DEVICE_PROPERTIES)).thenReturn(Collections.emptyMap());
    }

    @Test
    public void testGetPropertiesForMarkup_IFA() {
        Map<String, String> result = MarkupGenerator.getVelocityMacroProps(context, null, null, impression);

        Assert.assertEquals("ifa-123", result.get("ifa"));
    }

    @Test
    public void testGetPropertiesForMarkup_HIFA() {
        Map<String, String> result = MarkupGenerator.getVelocityMacroProps(context, null, null, impression);

        Assert.assertEquals("hifa-123", result.get("hifa"));
    }

    @Test
    public void testGetPropertiesForMarkup_ADID() {
        Map<String, String> result = MarkupGenerator.getVelocityMacroProps(context, null, null, impression);

        Assert.assertEquals("adid-123", result.get("adid"));
    }

    @Test
    public void testGetPropertiesForMarkup_ADID_MD5() {
        Map<String, String> result = MarkupGenerator.getVelocityMacroProps(context, null, null, impression);

        Assert.assertEquals("adid_md5-123", result.get("adid_md5"));
    }

    @Test
    public void testGetPropertiesForMarkup_IDFA_MD5() {
        Map<String, String> result = MarkupGenerator.getVelocityMacroProps(context, null, null, impression);

        Assert.assertEquals("idfa_md5-123", result.get("idfa_md5"));
    }

    private Map<String, Long> populateTypes() {
        Map<String, Long> types = new TreeMap<String, Long>();

        types.put("dpid", 1L);
        types.put("odin-1", 2L);
        types.put("openudid", 3L);
        types.put("hifa", 7L); // to be removed
        types.put("idfa", 12L);
        types.put("ifa", 6L);
        types.put("adid", 9L);
        types.put("adid_md5", 10L);
        types.put("idfa_md5", 13L);
        return types;
    }

}

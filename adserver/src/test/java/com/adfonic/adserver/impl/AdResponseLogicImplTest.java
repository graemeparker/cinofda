package com.adfonic.adserver.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.IconManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

@RunWith(MockitoJUnitRunner.class)
public class AdResponseLogicImplTest {

    @Mock
    private TargetingContext context;
    @Mock
    private AdSpaceDto adSpace;
    @Mock
    private HttpServletRequest request;

    @Mock
    private DisplayTypeUtils displayTypeUtils;
    @Mock
    private IconManager iconManager;
    @Mock
    private VhostManager vhostManager;
    @Mock
    private AdserverDataCacheManager adserverDataCacheManager;
    @Mock
    private DynamicProperties dProperties;

    private String testAdDestinationUrl = "test-AdDestination-Url";

    private AdResponseLogicImpl testObj;

    @Before()
    public void before() {
        MockitoAnnotations.initMocks(this);
        testObj = new AdResponseLogicImpl(displayTypeUtils, iconManager, vhostManager, dProperties);
        testObj.testAdDestinationUrl = testAdDestinationUrl;
    }

    @Test
    public void testGenerateTestAdComponents() throws IOException {

        // act
        AdComponents adComponents = testObj.generateTestAdComponents(context, adSpace, request);

        // assert
        Assert.assertEquals("text", adComponents.getFormat());
        Assert.assertEquals(DestinationType.URL, adComponents.getDestinationType());
        Assert.assertEquals(testAdDestinationUrl, adComponents.getDestinationUrl());

        Map<String, Map<String, String>> components = adComponents.getComponents();
        Map<String, String> text = components.get("text");
        Map<String, String> backgroundImage = components.get("backgroundImage");

        Assert.assertEquals("Congratulations! Ad slot verified.", text.get("content"));
        Assert.assertEquals("data:image/gif;base64,null", backgroundImage.get("url"));
    }

}

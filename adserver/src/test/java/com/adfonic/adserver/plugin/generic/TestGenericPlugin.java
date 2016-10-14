package com.adfonic.adserver.plugin.generic;

import static org.junit.Assert.assertNotNull;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;
import com.adfonic.util.ThreadLocalRandom;

public class TestGenericPlugin extends BaseAdserverTest {

    private GenericPlugin     generic;
    private AdSpaceDto         adSpace;
    private CreativeDto        creative;
    private AdserverPluginDto  adserverPlugin;
    private PluginCreativeInfo pluginCreativeInfo;
    private TargetingContext   context;
    private TimeLimit          timeLimit;

    @Before
    public void initTests() {
        generic = new GenericPlugin();

        adSpace = mock(AdSpaceDto.class, "adSpace");
        creative = mock(CreativeDto.class, "creative");
        adserverPlugin = mock(AdserverPluginDto.class, "adserverPlugin");
        pluginCreativeInfo = mock(PluginCreativeInfo.class,
                "pluginCreativeInfo");
        context = mock(TargetingContext.class, "context");
        timeLimit = mock(TimeLimit.class, "timeLimit");

    }

    @Test
    public void testGengericPlugin01_doGenerateAd() throws Exception {
        final String click = randomAlphaString(30);
        final String image = randomAlphaString(30);
        final String cache = String.valueOf(ThreadLocalRandom.getRandom().nextInt(Integer.MAX_VALUE));
        final String cacheBusterParam = cache;
        final String cbParam = cache;
        final String ordParam = cache;

        final DestinationDto destination = mock(DestinationDto.class,
                "destination");
        
        final DestinationType destinationType = DestinationType.ANDROID;

        expect(new Expectations() {
            {
                allowing(pluginCreativeInfo).getParameterRequired("click");
                will(returnValue(click));
                allowing(pluginCreativeInfo).getParameterRequired("image");
                will(returnValue(image));
                oneOf (pluginCreativeInfo).getParameterOptional("cachebuster");
                will(returnValue(cacheBusterParam));
                oneOf (pluginCreativeInfo).getParameterOptional("cb");
                will(returnValue(cbParam));
                oneOf (pluginCreativeInfo).getParameterOptional("ord");
                will(returnValue(ordParam));
                oneOf(creative).getDestination();
                will(returnValue(destination));
                oneOf(destination).getDestinationType();
                will(returnValue(destinationType));
            }
        });

        ProxiedDestination pd = generic.doGenerateAd(adSpace, creative,
                adserverPlugin, pluginCreativeInfo, context, timeLimit);
        assertNotNull(pd);
        assertNotNull(pd.getComponents());
        assertNotNull(pd.getComponents().get("image"));
        assertNotNull(pd.getComponents().get("image").get("url"));
    }
}

package com.adfonic.adserver.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.ContentSpecDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;

public class TestAbstractPlugin extends BaseAdserverTest {

	
	private AbstractPlugin abstractPlugin;
	private AdSpaceDto adSpace;
	private CreativeDto creative;
	private AdserverPluginDto adserverPlugin;
	private PluginCreativeInfo pluginCreativeInfo;
	private TargetingContext context;
	private TimeLimit timeLimit;
	private DisplayTypeUtils displayTypeUtils;
    private ProxiedDestination pd;
    private DomainCache domainCache;

	

	@Before
	public void initTests(){
		
		adSpace = mock(AdSpaceDto.class,"adSpace");
		creative = mock(CreativeDto.class,"creative");
		adserverPlugin = mock(AdserverPluginDto.class,"adserverPlugin");
		pluginCreativeInfo = mock(PluginCreativeInfo.class,"pluginCreativeInfo");
		context = mock(TargetingContext.class,"context");
		timeLimit = mock(TimeLimit.class,"timeLimit");
		displayTypeUtils = mock(DisplayTypeUtils.class,"displayTypeUtils");
        domainCache = mock(DomainCache.class,"domainCache");
		
    	final DestinationDto destination = mock(DestinationDto.class,"destination");
    	final DestinationType destinationType = DestinationType.ANDROID;

    	expect(new Expectations() {{
			allowing (creative).getDestination();
				will(returnValue(destination));
			oneOf (destination).getDestinationType();
				will(returnValue(destinationType));
		}});
		pd = new PluginProxiedDestination(adserverPlugin, creative, "");
		abstractPlugin = new AbstractPlugin() {
			@Override
			protected ProxiedDestination doGenerateAd(AdSpaceDto adSpace,
					CreativeDto creative, AdserverPluginDto adserverPlugin,
					PluginCreativeInfo pluginCreativeInfo, TargetingContext context,
					TimeLimit timeLimit) throws Exception {
				return pd;
			}
		};
		

		inject(abstractPlugin, "displayTypeUtils", displayTypeUtils);
		
	}
	
	@Test(expected=PluginException.class)
	public void testAbstractPlugin01_generateAd() throws Exception{
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();
    	final String formatSystemName = "text";
    	final String adserverPluginSystemName = randomAlphaString(20);
    	
    	
    	expect(new Expectations() {{
            allowing (creative).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
			allowing (creativeFormat).getSystemName();
				will(returnValue(formatSystemName));
			allowing (adserverPlugin).getSystemName();
				will(returnValue(adserverPluginSystemName));
		}});
    	abstractPlugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, timeLimit);

	}
	@Test
	public void testAbstractPlugin02_generateAd() throws Exception{
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();
    	final String formatSystemName = "text";
    	
    	//make sure text componet exists in pd
    	pd.getComponents().put(SystemName.COMPONENT_TEXT, new HashMap<String, String>());
    	pd.setFormat(randomAlphaString(20));//set some format
    	
    	expect(new Expectations() {{
            allowing (creative).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
			allowing (creativeFormat).getSystemName();
				will(returnValue(formatSystemName));
		}});
    	abstractPlugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, timeLimit);
	}
	
	@Test(expected=PluginException.class)
	public void testAbstractPlugin03_generateAd() throws Exception{
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();
    	final String formatSystemName = "image";
    	final String adserverPluginSystemName = randomAlphaString(20);
    	
    	
    	expect(new Expectations() {{
            allowing (creative).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
			allowing (creativeFormat).getSystemName();
				will(returnValue(formatSystemName));
			allowing (adserverPlugin).getSystemName();
				will(returnValue(adserverPluginSystemName));
		}});
    	abstractPlugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, timeLimit);
	}
	
	@Test
	public void testAbstractPlugin04_generateAd() throws Exception{
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();
    	final String formatSystemName = "image or anythiung nut not text";
    	
    	//make sure text componet exists in pd
    	Map<String, String> imageMap = new HashMap<String, String>();
    	pd.getComponents().put("image", imageMap );
    	imageMap.put("width", "100");
    	imageMap.put("height", "200");
    	
    	expect(new Expectations() {{
            allowing (creative).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
			allowing (creativeFormat).getSystemName();
				will(returnValue(formatSystemName));
		}});
    	abstractPlugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, timeLimit);
	}
	
	@Test
	public void testAbstractPlugin05_generateAd() throws Exception{
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();
    	final String formatSystemName = "image or anythiung nut not text";
    	final int displayTypeIndex = -1;
    	final List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();
    	final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
    	displayTypes.add(displayType);
    	final List<ComponentDto> components = new ArrayList<ComponentDto>();
    	final ComponentDto component = mock(ComponentDto.class,"component");
    	components.add(component);
    	final String componentSystemName = "anything but not image";
    	//make sure text componet exists in pd
    	Map<String, String> imageMap = new HashMap<String, String>();
    	pd.getComponents().put("image", imageMap );
    	imageMap.put("height", "200");
    	
    	expect(new Expectations() {{
            allowing (creative).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
			allowing (creativeFormat).getSystemName();
				will(returnValue(formatSystemName));
			oneOf (displayTypeUtils).getDisplayTypeIndex(creativeFormat, context);
				will(returnValue(displayTypeIndex));
			allowing (creativeFormat).getDisplayTypes();
				will(returnValue(displayTypes));
			allowing (creativeFormat).getComponents();
				will(returnValue(components));
			allowing (component).getSystemName();
				will(returnValue(componentSystemName));
		}});
    	abstractPlugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, timeLimit);
	}
	
	@Test
	public void testAbstractPlugin06_generateAd() throws Exception{
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();
    	final String formatSystemName = "image or anythiung nut not text";
    	final int displayTypeIndex = 0;
    	final List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();
    	final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
    	displayTypes.add(displayType);
    	final List<ComponentDto> components = new ArrayList<ComponentDto>();
    	final ComponentDto component = mock(ComponentDto.class,"component");
    	components.add(component);
    	final String componentSystemName = "image";
    	//make sure text componet exists in pd
    	Map<String, String> imageMap = new HashMap<String, String>();
    	pd.getComponents().put("image", imageMap );
    	imageMap.put("width", "200");
    	final ContentSpecDto contentSpec = null; 
    	
    	expect(new Expectations() {{
            allowing (creative).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
			allowing (creativeFormat).getSystemName();
				will(returnValue(formatSystemName));
			oneOf (displayTypeUtils).getDisplayTypeIndex(creativeFormat, context);
				will(returnValue(displayTypeIndex));
			allowing (creativeFormat).getDisplayTypes();
				will(returnValue(displayTypes));
			allowing (creativeFormat).getComponents();
				will(returnValue(components));
			allowing (component).getSystemName();
				will(returnValue(componentSystemName));
			allowing (component).getContentSpec(displayType);
				will(returnValue(contentSpec));
		}});
    	abstractPlugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, timeLimit);
	}
	
	@Test
	public void testAbstractPlugin07_generateAd() throws Exception{
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();
    	final String formatSystemName = "image or anythiung nut not text";
    	final int displayTypeIndex = 0;
    	final List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();
    	final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
    	displayTypes.add(displayType);
    	final List<ComponentDto> components = new ArrayList<ComponentDto>();
    	final ComponentDto component = mock(ComponentDto.class,"component");
    	components.add(component);
    	final String componentSystemName = "image";
    	//make sure text componet exists in pd
    	Map<String, String> imageMap = new HashMap<String, String>();
    	pd.getComponents().put("image", imageMap );
    	final ContentSpecDto contentSpec = mock(ContentSpecDto.class,"contentSpec");
    	final Map<String,String> props = new HashMap<String, String>();
    	
    	expect(new Expectations() {{
            allowing (creative).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
			allowing (creativeFormat).getSystemName();
				will(returnValue(formatSystemName));
			oneOf (displayTypeUtils).getDisplayTypeIndex(creativeFormat, context);
				will(returnValue(displayTypeIndex));
			allowing (creativeFormat).getDisplayTypes();
				will(returnValue(displayTypes));
			allowing (creativeFormat).getComponents();
				will(returnValue(components));
			allowing (component).getSystemName();
				will(returnValue(componentSystemName));
			allowing (component).getContentSpec(displayType);
				will(returnValue(contentSpec));
			allowing (context).getDomainCache();
				will(returnValue(domainCache));
            allowing (contentSpec).getManifestProperties();
				will(returnValue(props));
            allowing (displayType).getSystemName(); will(returnValue(randomAlphaNumericString(10)));
            allowing (contentSpec).getId(); will(returnValue(randomLong()));
		}});
    	abstractPlugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, timeLimit);
	}
	
	@Test
	public void testAbstractPlugin08_generateAd() throws Exception{
    	final FormatDto creativeFormat = mock(FormatDto.class,"creativeFormat");
        final long creativeFormatId = randomLong();
    	final String formatSystemName = "image or anythiung nut not text";
    	final int displayTypeIndex = 0;
    	final List<DisplayTypeDto> displayTypes = new ArrayList<DisplayTypeDto>();
    	final DisplayTypeDto displayType = mock(DisplayTypeDto.class,"displayType");
    	displayTypes.add(displayType);
    	final List<ComponentDto> components = new ArrayList<ComponentDto>();
    	final ComponentDto component = mock(ComponentDto.class,"component");
    	components.add(component);
    	final String componentSystemName = "image";
    	//make sure text componet exists in pd
    	Map<String, String> imageMap = new HashMap<String, String>();
    	pd.getComponents().put("image", imageMap );
    	final ContentSpecDto contentSpec = mock(ContentSpecDto.class,"contentSpec");
    	final Map<String,String> props = new HashMap<String, String>();
    	props.put("width", "100");
    	props.put("height", "100");
    	
    	expect(new Expectations() {{
            allowing (creative).getFormatId(); will(returnValue(creativeFormatId));
            allowing (context).getDomainCache(); will(returnValue(domainCache));
            allowing (domainCache).getFormatById(creativeFormatId); will(returnValue(creativeFormat));
			allowing (creativeFormat).getSystemName();
				will(returnValue(formatSystemName));
			oneOf (displayTypeUtils).getDisplayTypeIndex(creativeFormat, context);
				will(returnValue(displayTypeIndex));
			allowing (creativeFormat).getDisplayTypes();
				will(returnValue(displayTypes));
			allowing (creativeFormat).getComponents();
				will(returnValue(components));
			allowing (component).getSystemName();
				will(returnValue(componentSystemName));
			allowing (component).getContentSpec(displayType);
				will(returnValue(contentSpec));
			allowing (context).getDomainCache();
				will(returnValue(domainCache));
            allowing (contentSpec).getManifestProperties();
				will(returnValue(props));
            allowing (displayType).getSystemName(); will(returnValue(randomAlphaNumericString(10)));
            allowing (contentSpec).getId(); will(returnValue(randomLong()));
		}});
    	abstractPlugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, timeLimit);
	}
}

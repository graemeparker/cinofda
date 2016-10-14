package com.adfonic.adserver.controller;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.adserver.plugin.Plugin;
import com.adfonic.adserver.plugin.PluginManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class TestPluginTestController extends BaseAdserverTest {

	private TargetingContextFactory targetingContextFactory;
	private PreProcessor preProcessor;
	private ImpressionService impressionService;
	private StatusChangeManager statusChangeManager;
	private AdResponseLogic adResponseLogic;
	private TrackingIdentifierLogic trackingIdentifierLogic;
	private TargetingEngine targetingEngine;
	private AdEventFactory adEventFactory;
	private PluginTestController pluginTestController;
	private MarkupGenerator markupGenerator;
    private PluginManager pluginManager;

    @Before
	public void initTests() throws IOException, ServletException{
		pluginTestController = new PluginTestController();
		targetingContextFactory = mock(TargetingContextFactory.class,"targetingContextFactory");
		preProcessor = mock(PreProcessor.class,"preProcessor");
		impressionService = mock(ImpressionService.class,"impressionService");
		statusChangeManager = mock(StatusChangeManager.class);
		adResponseLogic = mock(AdResponseLogic.class);
		trackingIdentifierLogic = mock(TrackingIdentifierLogic.class);
		targetingEngine = mock(TargetingEngine.class);
		adEventFactory = mock(AdEventFactory.class);
		markupGenerator = mock(MarkupGenerator.class);
		pluginManager = mock(PluginManager.class);
		
		inject(pluginTestController, "targetingContextFactory", targetingContextFactory);
		inject(pluginTestController, "preProcessor", preProcessor);
		inject(pluginTestController, "impressionService", impressionService);
		inject(pluginTestController, "statusChangeManager", statusChangeManager);
		inject(pluginTestController, "adResponseLogic", adResponseLogic);
		inject(pluginTestController, "adEventFactory", adEventFactory);
		inject(pluginTestController, "targetingEngine", targetingEngine);
		inject(pluginTestController, "trackingIdentifierLogic", trackingIdentifierLogic);
		inject(pluginTestController, "markupGenerator", markupGenerator);
		inject(pluginTestController, "pluginManager", pluginManager);
	}
	
	@Test
	public void testPluginTestController01_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException, ServletException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		
        expect(new Expectations() {{
	    	oneOf (targetingContextFactory).createTargetingContext(request, false);
	    		will(throwException(new Exception("Failed to createTargetingContext")));
		}});
        
        pluginTestController.handleRequest(request, response, adSpaceExternalID,creativeExternalID);
	}
	
	@Test (expected = ServletException.class)
	public void testPluginTestController02_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		
        expect(new Expectations() {{
         oneOf (targetingContextFactory).createTargetingContext(request, false);	will(returnValue(targetingContext));
		 oneOf (targetingContext).getAdserverDomainCache();	will(returnValue(adserverDomainCache));	
		 oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID); will(returnValue(null));	
		
		}});
        
        pluginTestController.handleRequest(request, response, adSpaceExternalID, creativeExternalID);
	}
	
	@Test (expected = ServletException.class)
	public void testPluginTestController03_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		
        expect(new Expectations() {{
        	oneOf (targetingContextFactory).createTargetingContext(request, false); will(returnValue(targetingContext));
    		allowing (targetingContext).getAdserverDomainCache(); will(returnValue(adserverDomainCache));	
    		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID); will(returnValue(adSpace));	
    		oneOf (targetingContext).setAdSpace(adSpace);
            allowing (adSpace).getName(); will(returnValue(randomAlphaNumericString(10)));
            allowing (adSpace).getExternalID(); will(returnValue(adSpaceExternalID));
		    oneOf (adserverDomainCache).getCreativeByExternalID(creativeExternalID); will(returnValue(null));	
		}});
        
        pluginTestController.handleRequest(request, response, adSpaceExternalID, creativeExternalID);
	}
	
	@Test (expected = ServletException.class)
	public void testPluginTestController04_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		final CreativeDto creative = mock(CreativeDto.class);
		final PluginCreativeInfo pluginCreativeInfo = mock(PluginCreativeInfo.class);
		
        expect(new Expectations() {{
        oneOf (targetingContextFactory).createTargetingContext(request, false); will(returnValue(targetingContext));
		allowing (targetingContext).getAdserverDomainCache(); will(returnValue(adserverDomainCache));	
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID); will(returnValue(adSpace));	
		oneOf (targetingContext).setAdSpace(adSpace);
            allowing (adSpace).getName(); will(returnValue(randomAlphaNumericString(10)));
            allowing (adSpace).getExternalID(); will(returnValue(adSpaceExternalID));
		oneOf (adserverDomainCache).getCreativeByExternalID(creativeExternalID); will(returnValue(creative));
        allowing (creative).getName(); will(returnValue(randomAlphaNumericString(10)));
        allowing (creative).getExternalID(); will(returnValue(creativeExternalID));
		oneOf (adserverDomainCache).getPluginCreativeInfo(creative); will(returnValue(pluginCreativeInfo));
		allowing (pluginCreativeInfo).getPluginName(); will(returnValue("Testing"));
		oneOf (pluginManager).getPluginByName("Testing"); will(returnValue(null));
		}});
        
        pluginTestController.handleRequest(request, response, adSpaceExternalID, creativeExternalID);
	}

	@Test
	public void testPluginTestController05_handleRequest() throws Exception{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DomainCache domainCache = mock(DomainCache.class,"domainCache");
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		final CreativeDto creative = mock(CreativeDto.class);
		final PluginCreativeInfo pluginCreativeInfo = mock(PluginCreativeInfo.class);
		final Plugin plugin = mock(Plugin.class);
		final AdserverPluginDto adserverPlugin = mock(AdserverPluginDto.class);
		final ProxiedDestination pdDestination = mock(ProxiedDestination.class);
	
        expect(new Expectations() {{
        oneOf (targetingContextFactory).createTargetingContext(request, false); will(returnValue(targetingContext));
		allowing (targetingContext).getAdserverDomainCache(); will(returnValue(adserverDomainCache));	
        allowing (targetingContext).getDomainCache(); will(returnValue(domainCache));
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID); will(returnValue(adSpace));	
		oneOf (targetingContext).setAdSpace(adSpace);
            allowing (adSpace).getName(); will(returnValue(randomAlphaNumericString(10)));
            allowing (adSpace).getExternalID(); will(returnValue(adSpaceExternalID));
		oneOf (adserverDomainCache).getCreativeByExternalID(creativeExternalID); will(returnValue(creative));
        allowing (creative).getName(); will(returnValue(randomAlphaNumericString(10)));
        allowing (creative).getExternalID(); will(returnValue(creativeExternalID));
		oneOf (adserverDomainCache).getPluginCreativeInfo(creative); will(returnValue(pluginCreativeInfo));
		allowing (pluginCreativeInfo).getPluginName(); will(returnValue("Testing"));
		oneOf (pluginManager).getPluginByName("Testing"); will(returnValue(plugin));
		oneOf (domainCache).getAdserverPluginBySystemName("Testing"); will(returnValue(adserverPlugin));
		oneOf (plugin).generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, targetingContext, null); will(returnValue(pdDestination));
		oneOf (markupGenerator).generateMarkup(pdDestination, targetingContext, adSpace, creative, null, false); will(returnValue("Hello Markup"));
		}});
        
        pluginTestController.handleRequest(request, response, adSpaceExternalID, creativeExternalID);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("Hello Markup"));
	}
	
	@Test
	public void testPluginTestController06_handleRequest() throws Exception{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DomainCache domainCache = mock(DomainCache.class,"domainCache");
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		final CreativeDto creative = mock(CreativeDto.class);
		final PluginCreativeInfo pluginCreativeInfo = mock(PluginCreativeInfo.class);
		final Plugin plugin = mock(Plugin.class);
		final AdserverPluginDto adserverPlugin = mock(AdserverPluginDto.class);
	
        expect(new Expectations() {{
        oneOf (targetingContextFactory).createTargetingContext(request, false); will(returnValue(targetingContext));
		allowing (targetingContext).getAdserverDomainCache(); will(returnValue(adserverDomainCache));	
        allowing (targetingContext).getDomainCache(); will(returnValue(domainCache));
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID); will(returnValue(adSpace));	
		oneOf (targetingContext).setAdSpace(adSpace);
            allowing (adSpace).getName(); will(returnValue(randomAlphaNumericString(10)));
            allowing (adSpace).getExternalID(); will(returnValue(adSpaceExternalID));
		oneOf (adserverDomainCache).getCreativeByExternalID(creativeExternalID); will(returnValue(creative));
        allowing (creative).getName(); will(returnValue(randomAlphaNumericString(10)));
        allowing (creative).getExternalID(); will(returnValue(creativeExternalID));
		oneOf (adserverDomainCache).getPluginCreativeInfo(creative); will(returnValue(pluginCreativeInfo));
		allowing (pluginCreativeInfo).getPluginName(); will(returnValue("Testing"));
		oneOf (pluginManager).getPluginByName("Testing"); will(returnValue(plugin));
		oneOf (domainCache).getAdserverPluginBySystemName("Testing"); will(returnValue(adserverPlugin));
		oneOf (plugin).generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, targetingContext, null); will(throwException(new Exception("TestingException")));
		}});
        
        pluginTestController.handleRequest(request, response, adSpaceExternalID, creativeExternalID);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("TestingException"));
	}
	
	@Test (expected = ServletException.class)
	public void testPluginTestController07_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, ServletException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final DomainCache domainCache = mock(DomainCache.class,"domainCache");
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		final CreativeDto creative = mock(CreativeDto.class);
		final PluginCreativeInfo pluginCreativeInfo = mock(PluginCreativeInfo.class);
		final Plugin plugin = mock(Plugin.class);
		
        expect(new Expectations() {{
        oneOf (targetingContextFactory).createTargetingContext(request, false); will(returnValue(targetingContext));
		allowing (targetingContext).getAdserverDomainCache(); will(returnValue(adserverDomainCache));	
        allowing (targetingContext).getDomainCache(); will(returnValue(domainCache));
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID); will(returnValue(adSpace));	
		oneOf (targetingContext).setAdSpace(adSpace);
            allowing (adSpace).getName(); will(returnValue(randomAlphaNumericString(10)));
            allowing (adSpace).getExternalID(); will(returnValue(adSpaceExternalID));
		oneOf (adserverDomainCache).getCreativeByExternalID(creativeExternalID); will(returnValue(creative));
        allowing (creative).getName(); will(returnValue(randomAlphaNumericString(10)));
        allowing (creative).getExternalID(); will(returnValue(creativeExternalID));
		oneOf (adserverDomainCache).getPluginCreativeInfo(creative); will(returnValue(pluginCreativeInfo));
		allowing (pluginCreativeInfo).getPluginName(); will(returnValue("Testing"));
		oneOf (pluginManager).getPluginByName("Testing"); will(returnValue(plugin));
		oneOf (domainCache).getAdserverPluginBySystemName("Testing"); will(returnValue(null));
		}});
        
        pluginTestController.handleRequest(request, response, adSpaceExternalID, creativeExternalID);
	}
}

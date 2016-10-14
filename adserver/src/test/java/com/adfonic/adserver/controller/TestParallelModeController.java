package com.adfonic.adserver.controller;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.ParallelModeBidDetails;
import com.adfonic.adserver.ParallelModeBidManager;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

public class TestParallelModeController extends BaseAdserverTest {

	private ParallelModeController parallelModeController;
	private TargetingContextFactory targetingContextFactory;
	private PreProcessor preProcessor;
	private ImpressionService impressionService;
	private StatusChangeManager statusChangeManager;
	private AdResponseLogic adResponseLogic;
	private TrackingIdentifierLogic trackingIdentifierLogic;
	private TargetingEngine targetingEngine;
	private AdEventFactory adEventFactory;
	private BackupLogger backupLogger;
	private ParallelModeBidManager parallelModeBidManager;

    @Before
	public void initTests() throws IOException{
		parallelModeController = new ParallelModeController();
		targetingContextFactory = mock(TargetingContextFactory.class,"targetingContextFactory");
		preProcessor = mock(PreProcessor.class,"preProcessor");
		impressionService = mock(ImpressionService.class,"impressionService");
		statusChangeManager = mock(StatusChangeManager.class);
		adResponseLogic = mock(AdResponseLogic.class);
		trackingIdentifierLogic = mock(TrackingIdentifierLogic.class);
		targetingEngine = mock(TargetingEngine.class);
		adEventFactory = mock(AdEventFactory.class);
		backupLogger = mock(BackupLogger.class);
		parallelModeBidManager = mock(ParallelModeBidManager.class);
		
		inject(parallelModeController, "targetingContextFactory", targetingContextFactory);
		inject(parallelModeController, "preProcessor", preProcessor);
		inject(parallelModeController, "impressionService", impressionService);
		inject(parallelModeController, "statusChangeManager", statusChangeManager);
		inject(parallelModeController, "adResponseLogic", adResponseLogic);
		inject(parallelModeController, "adEventFactory", adEventFactory);
		inject(parallelModeController, "targetingEngine", targetingEngine);
		inject(parallelModeController, "backupLogger", backupLogger);
		inject(parallelModeController, "trackingIdentifierLogic", trackingIdentifierLogic);
		inject(parallelModeController, "parallelModeBidManager", parallelModeBidManager);
	}
	
	@Test
	public void testParallelModeController01_handleParallelModeWinNotice() throws InvalidIpAddressException, BlacklistedException, IOException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		
        expect(new Expectations() {{
	    	oneOf (targetingContextFactory).createTargetingContext(request, false);
	    		will(throwException(new Exception("Failed to createTargetingContext")));
		}});
        
        parallelModeController.handleParallelModeWinNotice(request, response, adSpaceExternalID,impressionExternalID);
	}
	
	
	@Test
	public void testParallelModeController02_handleParallelModeWinNotice() throws InvalidIpAddressException, BlacklistedException, IOException{
		final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		
        expect(new Expectations() {{
	    	oneOf (targetingContextFactory).createTargetingContext(request, false);
	    		will(returnValue(targetingContext));
			oneOf (preProcessor).preProcessRequest(targetingContext);will(throwException(new BlacklistedException("your phone blocked")));
		}});
        
        parallelModeController.handleParallelModeWinNotice(request, response, adSpaceExternalID,impressionExternalID);
	}
	
	@Test
	public void testParallelModeController03_handleParallelModeWinNotice() throws InvalidIpAddressException, BlacklistedException, IOException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		
        expect(new Expectations() {{
        	oneOf (targetingContextFactory).createTargetingContext(request, false);
    		will(returnValue(targetingContext));
		oneOf (preProcessor).preProcessRequest(targetingContext);
		oneOf (targetingContext).getAdserverDomainCache();
			will(returnValue(adserverDomainCache));	
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
			will(returnValue(null));	
		
		}});
        
        parallelModeController.handleParallelModeWinNotice(request, response, adSpaceExternalID, impressionExternalID);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("\"success\":\"0\",\"error\":\"Invalid AdSpace ID:"));
	}
	
	@Test
	public void testParallelModeController04_handleParallelModeWinNotice() throws InvalidIpAddressException, BlacklistedException, IOException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);//this has to be null from next;
		
        expect(new Expectations() {{
        	oneOf (targetingContextFactory).createTargetingContext(request, false);
    		will(returnValue(targetingContext));
		oneOf (preProcessor).preProcessRequest(targetingContext);
		oneOf (targetingContext).getAdserverDomainCache();
			will(returnValue(adserverDomainCache));	
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
			will(returnValue(adSpace));	
		atLeast(0).of (adSpace).getId();
			will(returnValue(20L));
		oneOf (parallelModeBidManager).removeBidDetails(impressionExternalID);
			will(returnValue(null));
		
		}});
        
        parallelModeController.handleParallelModeWinNotice(request, response, adSpaceExternalID, impressionExternalID);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("\"success\":\"0\",\"error\":\"Bid not found\""));
	}
	
	@Test
	public void testParallelModeController05_handleParallelModeWinNotice() throws InvalidIpAddressException, BlacklistedException, IOException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final Impression impression = mock(Impression.class);
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final ParallelModeBidDetails bidDetails = mock(ParallelModeBidDetails.class);
		
        expect(new Expectations() {{
        	oneOf (targetingContextFactory).createTargetingContext(request, false);
    		will(returnValue(targetingContext));
		oneOf (preProcessor).preProcessRequest(targetingContext);
		oneOf (targetingContext).getAdserverDomainCache();
			will(returnValue(adserverDomainCache));	
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
			will(returnValue(adSpace));	
		
		oneOf (parallelModeBidManager).removeBidDetails(impressionExternalID);
			will(returnValue(bidDetails));
		oneOf (bidDetails).getImpression();
			will(returnValue(impression));
		allowing (adSpace).getId();
			will(returnValue(2L));
		allowing (impression).getAdSpaceId();
			will(returnValue(20L));	
		
		}});
        
        parallelModeController.handleParallelModeWinNotice(request, response, adSpaceExternalID, impressionExternalID);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("\"success\":\"0\",\"error\":\"Bid not found\""));
	}
	
	@Test
	public void testParallelModeController06_handleParallelModeWinNotice() throws InvalidIpAddressException, BlacklistedException, IOException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final Impression impression = mock(Impression.class);
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final ParallelModeBidDetails bidDetails = mock(ParallelModeBidDetails.class);
		
        expect(new Expectations() {{
        oneOf (targetingContextFactory).createTargetingContext(request, false);
    		will(returnValue(targetingContext));
		oneOf (preProcessor).preProcessRequest(targetingContext);
		allowing (targetingContext).getAdserverDomainCache();
			will(returnValue(adserverDomainCache));	
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
			will(returnValue(adSpace));	
		
		oneOf (parallelModeBidManager).removeBidDetails(impressionExternalID);
			will(returnValue(bidDetails));
		oneOf (bidDetails).getImpression();
			will(returnValue(impression));
		allowing (adSpace).getId();
			will(returnValue(20L));
		allowing (impression).getAdSpaceId();
			will(returnValue(20L));	
		oneOf (parallelModeBidManager).getTargetingContextFromBidDetails(bidDetails);
			will(returnValue(targetingContext));
			
		allowing (targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
		
		allowing (adserverDomainCache).getCreativeById(20L);
			will(returnValue(null));
		allowing (impression).getCreativeId();
			will(returnValue(20L));
		allowing (adserverDomainCache).getRecentlyStoppedCreativeById(20L);
			will(returnValue(null));
            allowing(impression).getExternalID(); will(returnValue(randomAlphaNumericString(10)));
		}});
        
        parallelModeController.handleParallelModeWinNotice(request, response, adSpaceExternalID, impressionExternalID);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("\"success\":\"0\",\"error\":\"Creative not found\""));
	}
	
	@Test
	public void testParallelModeController07_handleParallelModeWinNotice() throws InvalidIpAddressException, BlacklistedException, IOException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final Impression impression = mock(Impression.class);
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final ParallelModeBidDetails bidDetails = mock(ParallelModeBidDetails.class);
		final AdEvent adEvent = mock(AdEvent.class);
		final CreativeDto creative = mock(CreativeDto.class);
		
        expect(new Expectations() {{
        oneOf (targetingContextFactory).createTargetingContext(request, false);
    		will(returnValue(targetingContext));
		oneOf (preProcessor).preProcessRequest(targetingContext);
		allowing (targetingContext).getAdserverDomainCache();
			will(returnValue(adserverDomainCache));	
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
			will(returnValue(adSpace));	
		
		oneOf (parallelModeBidManager).removeBidDetails(impressionExternalID);
			will(returnValue(bidDetails));
		oneOf (bidDetails).getImpression();
			will(returnValue(impression));
		allowing (adSpace).getId();
			will(returnValue(20L));
		allowing (impression).getAdSpaceId();
			will(returnValue(20L));	
		oneOf (parallelModeBidManager).getTargetingContextFromBidDetails(bidDetails);
			will(returnValue(targetingContext));
			
		allowing (targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
		
		allowing (adserverDomainCache).getCreativeById(20L);
			will(returnValue(null));
		allowing (impression).getCreativeId();
			will(returnValue(20L));
		allowing (adserverDomainCache).getRecentlyStoppedCreativeById(20L);
			will(returnValue(creative));
		
		
		allowing(backupLogger).logAdServed(with(impression), with(any(Date.class)), with(targetingContext));
		allowing (creative).getId();
			will(returnValue(20L));
            allowing(impression).getExternalID(); will(returnValue(randomAlphaNumericString(10)));
		}});
        
        parallelModeController.handleParallelModeWinNotice(request, response, adSpaceExternalID, impressionExternalID);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("\"success\":\"1\""));
	}
	
	@Test
	public void testParallelModeController08_handleParallelModeWinNotice() throws InvalidIpAddressException, BlacklistedException, IOException{
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String impressionExternalID = randomAlphaNumericString(10);
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final TargetingContext targetingContext = mock(TargetingContext.class,"targetingContext");
		final Impression impression = mock(Impression.class);
		final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class,"adserverDomainCache");
		final AdSpaceDto adSpace = mock(AdSpaceDto.class);
		final ParallelModeBidDetails bidDetails = mock(ParallelModeBidDetails.class);
		final AdEvent adEvent = mock(AdEvent.class);
		final CreativeDto creative = mock(CreativeDto.class);
		
        expect(new Expectations() {{
        oneOf (targetingContextFactory).createTargetingContext(request, false);
    		will(returnValue(targetingContext));
		oneOf (preProcessor).preProcessRequest(targetingContext);
		allowing (targetingContext).getAdserverDomainCache();
			will(returnValue(adserverDomainCache));	
		oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
			will(returnValue(adSpace));	
		
		oneOf (parallelModeBidManager).removeBidDetails(impressionExternalID);
			will(returnValue(bidDetails));
		oneOf (bidDetails).getImpression();
			will(returnValue(impression));
		allowing (impression).getCreativeId();
			will(returnValue(20L));
		allowing (adSpace).getId();
			will(returnValue(20L));
		allowing (impression).getAdSpaceId();
			will(returnValue(20L));	
		oneOf (parallelModeBidManager).getTargetingContextFromBidDetails(bidDetails);
			will(returnValue(targetingContext));
			
		allowing (targetingContext).setAdSpace(with(any(AdSpaceDto.class)));
		
		allowing (adserverDomainCache).getCreativeById(20L);
			will(returnValue(creative));
		
		oneOf(backupLogger).logAdServed(with(impression), with(any(Date.class)), with(targetingContext));
            allowing(impression).getExternalID(); will(returnValue(randomAlphaNumericString(10)));
		}});
        
        parallelModeController.handleParallelModeWinNotice(request, response, adSpaceExternalID, impressionExternalID);
        String responseString = response.getContentAsString();
        assertTrue(responseString.contains("\"success\":\"1\""));
	}
	
	
}

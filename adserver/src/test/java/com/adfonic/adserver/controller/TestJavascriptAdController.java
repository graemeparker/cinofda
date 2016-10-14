package com.adfonic.adserver.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.AdSpaceUtils;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.InvalidTrackingIdentifierException;
import com.adfonic.adserver.ParallelModeBidManager;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.adserver.monitor.AdserverMonitor;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Publication;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.util.stats.CounterManager;

public class TestJavascriptAdController extends BaseAdserverTest {

    private JavascriptAdController javascriptAdController;
    private TargetingContextFactory targetingContextFactory;
    private PreProcessor preProcessor;
    private ImpressionService impressionService;
    private ModelMap modelMap;
    private StatusChangeManager statusChangeManager;
    private AdResponseLogic adResponseLogic;
    private TrackingIdentifierLogic trackingIdentifierLogic;
    private TargetingEngine targetingEngine;
    private AdEventFactory adEventFactory;
    private ParallelModeBidManager parallelModeBidManager;
    private AdSpaceUtils adSpaceUtils;
    private CounterManager counterManager;
    private BackupLogger backupLogger;
    private AdserverMonitor adserverMonitor;

    @Before
    public void initTests() {
        javascriptAdController = new JavascriptAdController();
        targetingContextFactory = mock(TargetingContextFactory.class, "targetingContextFactory");
        preProcessor = mock(PreProcessor.class, "preProcessor");
        impressionService = mock(ImpressionService.class, "impressionService");
        statusChangeManager = mock(StatusChangeManager.class);
        adResponseLogic = mock(AdResponseLogic.class);
        trackingIdentifierLogic = mock(TrackingIdentifierLogic.class);
        targetingEngine = mock(TargetingEngine.class);
        adEventFactory = mock(AdEventFactory.class);
        parallelModeBidManager = mock(ParallelModeBidManager.class);
        adSpaceUtils = mock(AdSpaceUtils.class);
        counterManager = mock(CounterManager.class);
        backupLogger = mock(BackupLogger.class);
        modelMap = new ModelMap();
        adserverMonitor = new AdserverMonitor();

        inject(javascriptAdController, "targetingContextFactory", targetingContextFactory);
        inject(javascriptAdController, "preProcessor", preProcessor);
        inject(javascriptAdController, "impressionService", impressionService);
        inject(javascriptAdController, "statusChangeManager", statusChangeManager);
        inject(javascriptAdController, "adResponseLogic", adResponseLogic);
        inject(javascriptAdController, "adEventFactory", adEventFactory);
        inject(javascriptAdController, "targetingEngine", targetingEngine);
        inject(javascriptAdController, "trackingIdentifierLogic", trackingIdentifierLogic);
        inject(javascriptAdController, "parallelModeBidManager", parallelModeBidManager);
        inject(javascriptAdController, "adSpaceUtils", adSpaceUtils);
        inject(javascriptAdController, "counterManager", counterManager);
        inject(javascriptAdController, "backupLogger", backupLogger);
        inject(javascriptAdController, "adserverMonitor", adserverMonitor);
    }

    @Test
    public void testJavascriptAdController01_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final String exceptionMessage = "your phone blocked";
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                will(throwException(new BlacklistedException(exceptionMessage)));
                oneOf(backupLogger).logAdRequestFailure("blacklisted", targetingContext, exceptionMessage);
            }
        });

        String responseString = javascriptAdController.handleRequest(request, response, modelMap, adSpaceExternalID);
        assertNull(responseString);
    }

    @Test
    public void testJavascriptAdController02_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, InvalidTrackingIdentifierException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();
        final long adSpaceId = randomLong();
        final DomainCache domainCache = mock(DomainCache.class);
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = new Date();
        final UnfilledReason unfilledReason = UnfilledReason.EXCEPTION;
        final ByydImp byydImp = null;
        final TargetingEventListener nullTargetingEventListener = null;
        final Collection<Long> nullCollection = null;
        final CreativeDto nullCreative = null;
        
        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                allowing(adSpace).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getExternalID();
                will(returnValue(adSpaceExternalID));
                allowing(targetingContext).setAdSpace(adSpace);
                allowing(targetingContext).getEffectiveUserAgent();
                will(returnValue("Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1 Mobile/5A345 Safari/525.20"));
                allowing(targetingContext).getAttribute(TargetingContext.DEVICE_IS_ROBOT_CHECKER_OR_SPAM, Boolean.class);
                will(returnValue(false));

                allowing(adSpace).getPublication();
                will(returnValue(pub));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(statusChangeManager).getStatus(pub);
                will(returnValue(Publication.Status.ACTIVE));

                oneOf(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue(null));
                oneOf(trackingIdentifierLogic).establishTrackingIdentifier(targetingContext, response, true);
                oneOf(pub).getEffectiveAdRequestTimeout();
                will(returnValue(2L));
                
                oneOf(targetingEngine).selectCreative(with(any(AdSpaceDto.class)), with(nullCollection), with(any(TargetingContext.class)), with(any(boolean.class)),
                        with(any(boolean.class)), with(any(TimeLimit.class)), with(nullTargetingEventListener));will(throwException(new Exception("Dummy TEsting Exception")));
                oneOf(adEventFactory).newInstance(AdAction.UNFILLED_REQUEST);
                will(returnValue(adEvent));
                oneOf(adEvent).setUnfilledReason(unfilledReason);
                oneOf(targetingContext).populateAdEvent(with(any(AdEvent.class)), with(any(Impression.class)), with(nullCreative));
                oneOf(adEvent).getAdAction();
                will(returnValue(AdAction.UNFILLED_REQUEST));
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(adEvent).getUnfilledReason();
                will(returnValue(unfilledReason));
                oneOf(backupLogger).logUnfilledRequest(unfilledReason, eventTime, targetingContext);
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));

            }
        });

        javascriptAdController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertEquals(error, "No ad available due to internal error");
    }
}

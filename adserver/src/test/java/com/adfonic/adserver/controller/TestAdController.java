package com.adfonic.adserver.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jms.Queue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import com.adfonic.adserver.AdComponents;
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
import com.adfonic.adserver.ParallelModeBidDetails;
import com.adfonic.adserver.ParallelModeBidManager;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.SelectedCreative;
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
import com.adfonic.domain.PendingAdType;
import com.adfonic.domain.Publication;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.jms.AdSpaceVerifiedMessage;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.util.stats.CounterManager;

public class TestAdController extends BaseAdserverTest {

    private AdController adController;
    private TargetingContextFactory targetingContextFactory;
    private PreProcessor preProcessor;
    private ImpressionService impressionService;
    private ModelMap modelMap;
    private StatusChangeManager statusChangeManager;
    private JmsTemplate centralJmsTemplate;
    private Queue adSpaceVerifiedQueue;
    private AdResponseLogic adResponseLogic;
    private TrackingIdentifierLogic trackingIdentifierLogic;
    private TargetingEngine targetingEngine;
    private AdEventFactory adEventFactory;
    private ParallelModeBidManager parallelModeBidManager;
    private AdSpaceUtils adSpaceUtils;
    private CounterManager counterManager;
    private JmsUtils jmsUtils;
    private BackupLogger backupLogger;
    private AdserverMonitor adserverMonitor;

    @Before
    public void initTests() {
        adController = new AdController();
        targetingContextFactory = mock(TargetingContextFactory.class, "targetingContextFactory");
        preProcessor = mock(PreProcessor.class, "preProcessor");
        impressionService = mock(ImpressionService.class, "impressionService");
        statusChangeManager = mock(StatusChangeManager.class);
        centralJmsTemplate = mock(JmsTemplate.class, "centralJmsTemplate");
        adSpaceVerifiedQueue = mock(Queue.class);
        adResponseLogic = mock(AdResponseLogic.class);
        trackingIdentifierLogic = mock(TrackingIdentifierLogic.class);
        targetingEngine = mock(TargetingEngine.class);
        adEventFactory = mock(AdEventFactory.class);
        parallelModeBidManager = mock(ParallelModeBidManager.class);
        adSpaceUtils = mock(AdSpaceUtils.class);
        counterManager = mock(CounterManager.class);
        jmsUtils = mock(JmsUtils.class);
        backupLogger = mock(BackupLogger.class);
        modelMap = new ModelMap();
        adserverMonitor = new AdserverMonitor();

        inject(adController, "targetingContextFactory", targetingContextFactory);
        inject(adController, "preProcessor", preProcessor);
        inject(adController, "impressionService", impressionService);
        inject(adController, "statusChangeManager", statusChangeManager);
        inject(adController, "centralJmsTemplate", centralJmsTemplate);
        inject(adController, "adResponseLogic", adResponseLogic);
        inject(adController, "adEventFactory", adEventFactory);
        inject(adController, "targetingEngine", targetingEngine);
        inject(adController, "trackingIdentifierLogic", trackingIdentifierLogic);
        inject(adController, "parallelModeBidManager", parallelModeBidManager);
        inject(adController, "adSpaceUtils", adSpaceUtils);
        inject(adController, "counterManager", counterManager);
        inject(adController, "jmsUtils", jmsUtils);
        inject(adController, "backupLogger", backupLogger);
        inject(adController, "adserverMonitor", adserverMonitor);

        expect(new Expectations() {
            {
                allowing(backupLogger).startControllerRequest();
            }
        });

    }

    @Test
    public void testAdController01_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final InvalidIpAddressException e1 = new InvalidIpAddressException(randomAlphaNumericString(10));
        final IllegalStateException e2 = new IllegalStateException(randomAlphaNumericString(10));
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(throwException(e1));
                oneOf(backupLogger).logAdRequestFailure(e1.getMessage(), null);

                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(throwException(e2));
                oneOf(backupLogger).logAdRequestFailure("exception", null, e2.getClass().getName(), e2.getMessage());
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertEquals(error, e1.getMessage());

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        error = (String) modelMap.get("error");
        assertEquals(error, "No ad available due to internal error");
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testAdController02_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = null;
        final Set<String> dormantAdSpaceExternalIds = new HashSet<String>();
        final Set<String> dormantAdSpaceExternalIds2 = new HashSet<String>();
        dormantAdSpaceExternalIds2.add(adSpaceExternalID);

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));

                oneOf(adserverDomainCache).getDormantAdSpaceExternalIds();
                will(returnValue(dormantAdSpaceExternalIds));
                oneOf(backupLogger).logAdRequestFailure("AdSpace not found", targetingContext, adSpaceExternalID);

                oneOf(adserverDomainCache).getDormantAdSpaceExternalIds();
                will(returnValue(dormantAdSpaceExternalIds2));
                oneOf(backupLogger).logAdRequestFailure("AdSpace dormant", targetingContext, adSpaceExternalID);
                oneOf(adSpaceUtils).reactivateDormantAdSpace(adSpaceExternalID);
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertEquals(error, "Invalid AdSpace ID: " + adSpaceExternalID);

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        error = (String) modelMap.get("error");
        assertEquals(error, "Reactivating dormant AdSpace, try again later");
    }

    @Test
    public void testAdController03_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto publication = mock(PublicationDto.class, "publication");
        final PublisherDto publisher = mock(PublisherDto.class, "publisher");
        final long publisherId = randomLong();

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                allowing(adSpace).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(adSpace).getExternalID();
                will(returnValue(adSpaceExternalID));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(publication).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));

                allowing(targetingContext).setAdSpace(adSpace);
                oneOf(targetingContext).getEffectiveUserAgent();
                will(returnValue(null));
                oneOf(backupLogger).logAdRequestFailure("no User-Agent", targetingContext);

                oneOf(targetingContext).getEffectiveUserAgent();
                will(returnValue("Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1 Mobile/5A345 Safari/525.20"));
                oneOf(targetingContext).setAttribute(with(any(String.class)), with(any(Object.class)));
                oneOf(targetingContext).getAttribute(TargetingContext.DEVICE_IS_ROBOT_CHECKER_OR_SPAM, Boolean.class);
                will(returnValue(true));
                oneOf(backupLogger).logAdRequestFailure("robot/checker/spam", targetingContext);

                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertEquals(error, "No User-Agent supplied");

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        error = (String) modelMap.get("error");
        assertEquals(error, "Your User-Agent is not welcome here.");
    }

    @Test
    public void testAdController04_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long adSpaceId = randomLong();
        final long publisherId = randomLong();

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
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

                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(pub));
                allowing(pub).getId();
                will(returnValue(randomLong()));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(statusChangeManager).getStatus(pub);
                will(returnValue(Publication.Status.PENDING));
                allowing(publisher).getId();
                will(returnValue(publisherId));

                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));

                allowing(adSpace).setStatus(AdSpace.Status.VERIFIED);
                oneOf(publisher).getPendingAdType();
                will(returnValue(PendingAdType.HOLDING_AD));
                oneOf(adResponseLogic).generateTestAdComponents(targetingContext, adSpace, request);
                will(throwException(new Exception()));

                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.UNVERIFIED));
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.UNVERIFIED));
                oneOf(jmsUtils).sendObject(with(centralJmsTemplate), with(JmsResource.ADSPACE_VERIFIED), with(any(AdSpaceVerifiedMessage.class)));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        //System.out.println(error);
        assertEquals(error, "No ad available due to internal error");
    }

    @Test
    public void testAdController05_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final AdComponents adComponents = mock(AdComponents.class);
        final long adSpaceId = randomLong();
        final long publisherId = randomLong();

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
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

                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(pub));
                allowing(pub).getId();
                will(returnValue(randomLong()));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(statusChangeManager).getStatus(pub);
                will(returnValue(Publication.Status.PENDING));
                allowing(publisher).getId();
                will(returnValue(publisherId));

                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.UNVERIFIED));
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.UNVERIFIED));
                oneOf(jmsUtils).sendObject(with(centralJmsTemplate), with(JmsResource.ADSPACE_VERIFIED), with(any(AdSpaceVerifiedMessage.class)));
                oneOf(adResponseLogic).generateTestAdComponents(targetingContext, adSpace, request);
                will(returnValue(adComponents));

                allowing(adSpace).setStatus(AdSpace.Status.VERIFIED);

                oneOf(publisher).getPendingAdType();
                will(returnValue(PendingAdType.HOLDING_AD));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertNull(error);
        assertNotNull(modelMap.get("adComponents"));
    }

    @Test
    public void testAdController06_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long adSpaceId = randomLong();
        final long publisherId = randomLong();

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
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

                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(pub));
                allowing(pub).getId();
                will(returnValue(randomLong()));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(statusChangeManager).getStatus(pub);
                will(returnValue(Publication.Status.PENDING));
                allowing(publisher).getId();
                will(returnValue(publisherId));
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.UNVERIFIED));
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.UNVERIFIED));
                oneOf(jmsUtils).sendObject(with(centralJmsTemplate), with(JmsResource.ADSPACE_VERIFIED), with(any(AdSpaceVerifiedMessage.class)));

                allowing(adSpace).setStatus(AdSpace.Status.VERIFIED);
                oneOf(publisher).getPendingAdType();
                will(returnValue(PendingAdType.NO_AD));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertNull(error);
        assertNull(modelMap.get("adComponents"));
    }

    @Test
    public void testAdController07_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
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
        final FormatDto format = mock(FormatDto.class);
        final long formatId = randomLong();
        final String formatName = randomAlphaNumericString(10);
        final String invalidFormatName = randomAlphaNumericString(11);

        expect(new Expectations() {
            {
                allowing(format).getId();
                will(returnValue(formatId));
                allowing(targetingContextFactory).createTargetingContext(request, false);
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
                allowing(pub).getId();
                will(returnValue(randomLong()));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(statusChangeManager).getStatus(pub);
                will(returnValue(Publication.Status.ACTIVE));

                oneOf(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue(" ," + formatName + "," + invalidFormatName));

                oneOf(domainCache).getFormatBySystemName(formatName);
                will(returnValue(format));
                oneOf(domainCache).getFormatBySystemName(invalidFormatName);
                will(returnValue(null));
                oneOf(backupLogger).logAdRequestFailure("invalid Format", targetingContext, invalidFormatName);
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertEquals(error, "Invalid format: " + invalidFormatName);
    }

    @Test
    public void testAdController08_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, InvalidTrackingIdentifierException {
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
        final UnfilledReason unfilledReason = UnfilledReason.EXCEPTION;
        final Date eventTime = new Date();
        final Collection<Long> nullCollection = null;
        final TargetingEventListener nullTargetingEventListener = null;
        final ByydImp nullByydImp = null;
        final CreativeDto nullCreativeDto = null;

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
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
                allowing(pub).getId();
                will(returnValue(randomLong()));
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
                oneOf(trackingIdentifierLogic).establishTrackingIdentifier(targetingContext, response, false);
                oneOf(pub).getEffectiveAdRequestTimeout();
                will(returnValue(2L));
                oneOf(targetingEngine).selectCreative(with(any(AdSpaceDto.class)), with(nullCollection), with(any(TargetingContext.class)), with(any(boolean.class)),
                        with(any(boolean.class)), with(any(TimeLimit.class)), with(nullTargetingEventListener));
                will(throwException(new Exception("Dummy TEsting Exception")));
                oneOf(adEventFactory).newInstance(AdAction.UNFILLED_REQUEST);
                will(returnValue(adEvent));
                oneOf(adEvent).setUnfilledReason(unfilledReason);
                oneOf(targetingContext).populateAdEvent(with(any(AdEvent.class)), with(any(Impression.class)), with(nullCreativeDto));
//                oneOf(adEventLogger).logAdEvent(adEvent, targetingContext);
                oneOf(adEvent).getAdAction();
                will(returnValue(AdAction.UNFILLED_REQUEST));
                oneOf(adEvent).getUnfilledReason();
                will(returnValue(unfilledReason));
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logUnfilledRequest(unfilledReason, eventTime, targetingContext);
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertEquals(error, "No ad available due to internal error");
    }

    @Test
    public void testAdController09_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, InvalidTrackingIdentifierException {
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
        final FormatDto format = mock(FormatDto.class);
        final long formatId = randomLong();
        final Set<Long> adSpaceFormatIds = new HashSet<Long>();
        @SuppressWarnings("serial")
        final Set<Long> allowedFormatIds = new HashSet<Long>() {
            {
                add(formatId);
            }
        };
        expect(new Expectations() {
            {
                allowing(format).getId();
                will(returnValue(formatId));
                allowing(targetingContextFactory).createTargetingContext(request, false);
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
                allowing(pub).getId();
                will(returnValue(randomLong()));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getFormatIds();
                will(returnValue(adSpaceFormatIds));
                allowing(statusChangeManager).getStatus(pub);
                will(returnValue(Publication.Status.ACTIVE));

                oneOf(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue("banner"));

                oneOf(domainCache).getFormatBySystemName("banner");
                will(returnValue(format));
                oneOf(backupLogger).logAdRequestFailure("Format mismatch", targetingContext, StringUtils.join(adSpaceFormatIds, ','), StringUtils.join(allowedFormatIds, ','));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });
        request.setParameter(Parameters.FORMAT, "wml");
        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertEquals(error, "Format(s) not supported by ad slot");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAdController10_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, InvalidTrackingIdentifierException {
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
        final FormatDto format = mock(FormatDto.class);
        final long formatId = randomLong();
        final Set<Long> allowedFormatIds = new HashSet<Long>();
        allowedFormatIds.add(formatId);
        final UnfilledReason unfilledReason = UnfilledReason.EXCEPTION;
        final Date eventTime = new Date();
        final TargetingEventListener nullTargetingEventListener = null;
        final ByydImp nullByydImp = null;
        final CreativeDto nullCreativeDto = null;

        expect(new Expectations() {
            {
                allowing(format).getId();
                will(returnValue(formatId));
                allowing(targetingContextFactory).createTargetingContext(request, false);
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
                allowing(pub).getId();
                will(returnValue(randomLong()));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                oneOf(adSpace).getFormatIds();
                will(returnValue(allowedFormatIds));
                allowing(statusChangeManager).getStatus(pub);
                will(returnValue(Publication.Status.ACTIVE));

                oneOf(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue("banner"));

                oneOf(domainCache).getFormatBySystemName("banner");
                will(returnValue(format));

                oneOf(trackingIdentifierLogic).establishTrackingIdentifier(targetingContext, response, false);
                oneOf(pub).getEffectiveAdRequestTimeout();
                will(returnValue(2L));
                oneOf(targetingEngine).selectCreative(with(any(AdSpaceDto.class)), with(any(Collection.class)), with(any(TargetingContext.class)), with(any(boolean.class)),
                        with(any(boolean.class)), with(any(TimeLimit.class)), with(nullTargetingEventListener));
                will(throwException(new Exception()));
                oneOf(adEventFactory).newInstance(AdAction.UNFILLED_REQUEST);
                will(returnValue(adEvent));
                oneOf(adEvent).setUnfilledReason(unfilledReason);
                oneOf(targetingContext).populateAdEvent(with(any(AdEvent.class)), with(any(Impression.class)), with(nullCreativeDto));
//                oneOf(adEventLogger).logAdEvent(adEvent, targetingContext);
                oneOf(adEvent).getAdAction();
                will(returnValue(AdAction.UNFILLED_REQUEST));
                oneOf(adEvent).getUnfilledReason();
                will(returnValue(unfilledReason));
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logUnfilledRequest(unfilledReason, eventTime, targetingContext);
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });
        request.setParameter(Parameters.FORMAT, "xml");
        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        //System.out.println(error);
        assertEquals(error, "No ad available due to internal error");
    }

    @Test
    public void testAdController11_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String responseFormatName = randomAlphaNumericString(10);
        expect(new Expectations() {
            {
                oneOf(backupLogger).logAdRequestFailure("invalid ResponseFormat", null, responseFormatName);
            }
        });
        request.setParameter(Parameters.FORMAT, responseFormatName);
        String value = adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        assertEquals(response.getStatus(), HttpServletResponse.SC_BAD_REQUEST);
        assertNull(value);
    }

    @Test
    public void testAdController12_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, InvalidTrackingIdentifierException {
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
        final UnfilledReason unfilledReason = UnfilledReason.EXCEPTION;
        final Date eventTime = new Date();
        final CreativeDto nullCreativeDto = null;

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
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
                allowing(pub).getId();
                will(returnValue(randomLong()));
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

                oneOf(trackingIdentifierLogic).establishTrackingIdentifier(targetingContext, response, false);
                will(throwException(new InvalidTrackingIdentifierException("Testing")));
                oneOf(adEventFactory).newInstance(AdAction.UNFILLED_REQUEST);
                will(returnValue(adEvent));
                oneOf(adEvent).setUnfilledReason(unfilledReason);
                oneOf(targetingContext).populateAdEvent(with(any(AdEvent.class)), with(any(Impression.class)), with(nullCreativeDto));
//                oneOf(adEventLogger).logAdEvent(adEvent, targetingContext);
                oneOf(adEvent).getAdAction();
                will(returnValue(AdAction.UNFILLED_REQUEST));
                oneOf(adEvent).getUnfilledReason();
                will(returnValue(unfilledReason));
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logUnfilledRequest(unfilledReason, eventTime, targetingContext);
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertEquals(error, "Testing");
    }

    @Test
    public void testAdController13_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, InvalidTrackingIdentifierException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();
        final AdComponents adComponents = mock(AdComponents.class);
        final long adSpaceId = randomLong();
        final DomainCache domainCache = mock(DomainCache.class);
        final AdEvent adEvent = mock(AdEvent.class);
        final SelectedCreative selectedCreative = mock(SelectedCreative.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final ProxiedDestination proxiedDestination = mock(ProxiedDestination.class);
        final Date eventTime = new Date();
        final Collection<Long> nullCollection = null;
        final TargetingEventListener nullTargetingEventListener = null;
        final ByydImp nullByydImp = null;

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
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
                allowing(pub).getId();
                will(returnValue(randomLong()));
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

                oneOf(trackingIdentifierLogic).establishTrackingIdentifier(targetingContext, response, false);
                oneOf(pub).getEffectiveAdRequestTimeout();
                will(returnValue(2L));
                oneOf(targetingEngine).selectCreative(with(any(AdSpaceDto.class)), with(nullCollection), with(any(TargetingContext.class)), with(any(boolean.class)),
                        with(any(boolean.class)), with(any(TimeLimit.class)), with(nullTargetingEventListener));
                will(returnValue(selectedCreative));
                oneOf(targetingContext).setAttribute(TargetingContext.SELECTED_CREATIVE, selectedCreative);
                oneOf(targetingContext).populateImpression(with(any(Impression.class)), with(any(SelectedCreative.class)));
                atLeast(1).of(selectedCreative).getCreative();
                will(returnValue(creative));
                allowing(creative).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(creative).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(creative).getPriority();
                will(returnValue(randomInteger()));
                allowing(creative).getFormatId();
                will(returnValue(randomLong()));
                oneOf(selectedCreative).getProxiedDestination();
                will(returnValue(proxiedDestination));

                oneOf(adResponseLogic).generateFullAdComponents(with(any(TargetingContext.class)), with(any(AdSpaceDto.class)), with(any(CreativeDto.class)),
                        with(any(ProxiedDestination.class)), with(any(Impression.class)), with(any(HttpServletRequest.class)));
                will(returnValue(adComponents));
                oneOf(adResponseLogic).postProcessAdComponents(adComponents, targetingContext);
                oneOf(impressionService).saveImpression(with(any(Impression.class)));
                oneOf(targetingContext).getAttribute(Parameters.PARALLEL);
                will(returnValue("2"));

                oneOf(adEventFactory).newInstance(AdAction.AD_SERVED);
                will(returnValue(adEvent));
                oneOf(targetingContext).populateAdEvent(with(any(AdEvent.class)), with(any(Impression.class)), with(any(CreativeDto.class)));
//                oneOf(adEventLogger).logAdEvent(adEvent, targetingContext);
                oneOf(adEvent).getAdAction();
                will(returnValue(AdAction.AD_SERVED));
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logAdServed(with(any(Impression.class)), with(eventTime), with(targetingContext));
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));

                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));

            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertNull(error);
    }

    @Test
    public void testAdController14_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, InvalidTrackingIdentifierException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final AdserverDomainCache adserverDomainCache = mock(AdserverDomainCache.class, "adserverDomainCache");
        final AdSpaceDto adSpace = mock(AdSpaceDto.class, "adSpace");
        final PublicationDto pub = mock(PublicationDto.class);
        final PublisherDto publisher = mock(PublisherDto.class);
        final long publisherId = randomLong();
        final AdComponents adComponents = mock(AdComponents.class);
        final long adSpaceId = randomLong();
        final DomainCache domainCache = mock(DomainCache.class);
        final SelectedCreative selectedCreative = mock(SelectedCreative.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final ProxiedDestination proxiedDestination = mock(ProxiedDestination.class);
        final RtbConfigDto rtbConfig = mock(RtbConfigDto.class, "RtbConfigDto");
        final Long rtbTimeLost = 10L;
        final Collection<Long> nullCollection = null;
        final TargetingEventListener nullTargetingEventListener = null;
        final ByydImp nullByydImp = null;

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
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
                allowing(pub).getId();
                will(returnValue(randomLong()));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));
                allowing(publisher).getRtbConfig();
                will(returnValue(rtbConfig));
                allowing(rtbConfig).getRtbLostTimeDuration();
                will(returnValue(rtbTimeLost));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(statusChangeManager).getStatus(pub);
                will(returnValue(Publication.Status.ACTIVE));

                oneOf(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue(null));
                oneOf(trackingIdentifierLogic).establishTrackingIdentifier(targetingContext, response, false);
                oneOf(pub).getEffectiveAdRequestTimeout();
                will(returnValue(2L));
                oneOf(targetingEngine).selectCreative(with(any(AdSpaceDto.class)), with(nullCollection), with(any(TargetingContext.class)), with(any(boolean.class)),
                        with(any(boolean.class)), with(any(TimeLimit.class)), with(nullTargetingEventListener));
                will(returnValue(selectedCreative));
                oneOf(targetingContext).setAttribute(TargetingContext.SELECTED_CREATIVE, selectedCreative);
                oneOf(targetingContext).populateImpression(with(any(Impression.class)), with(any(SelectedCreative.class)));
                atLeast(1).of(selectedCreative).getCreative();
                will(returnValue(creative));
                allowing(creative).getExternalID();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(creative).getName();
                will(returnValue(randomAlphaNumericString(10)));
                allowing(creative).getPriority();
                will(returnValue(randomInteger()));
                allowing(creative).getFormatId();
                will(returnValue(randomLong()));
                oneOf(selectedCreative).getProxiedDestination();
                will(returnValue(proxiedDestination));

                oneOf(adResponseLogic).generateFullAdComponents(with(any(TargetingContext.class)), with(any(AdSpaceDto.class)), with(any(CreativeDto.class)),
                        with(any(ProxiedDestination.class)), with(any(Impression.class)), with(any(HttpServletRequest.class)));
                will(returnValue(adComponents));
                oneOf(adResponseLogic).postProcessAdComponents(adComponents, targetingContext);
                oneOf(impressionService).saveImpression(with(any(Impression.class)));
                oneOf(targetingContext).getAttribute(Parameters.PARALLEL);
                will(returnValue("1"));
                oneOf(parallelModeBidManager).saveBidDetails(with(any(ParallelModeBidDetails.class)), with(10L));
                oneOf(targetingContext).getAttribute(Parameters.IP);
                will(returnValue("someIP"));

                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));

                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertNull(error);
    }

    @Test
    public void testAdController15_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException, InvalidTrackingIdentifierException {
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
        final UnfilledReason unfilledReason = UnfilledReason.UNKNOWN;
        final Date eventTime = new Date();
        final Collection<Long> nullCollection = null;
        final TargetingEventListener nullTargetingEventListener = null;
        final ByydImp nullByydImp = null;
        final SelectedCreative nullSelectedCreative = null;
        final CreativeDto nullCreativeDto = null;

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, false);
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
                allowing(pub).getId();
                will(returnValue(randomLong()));
                allowing(pub).getPublisher();
                will(returnValue(publisher));
                allowing(publisher).getId();
                will(returnValue(publisherId));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(statusChangeManager).getStatus(pub);
                will(returnValue(Publication.Status.ACTIVE));
                allowing(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue(null));

                allowing(trackingIdentifierLogic).establishTrackingIdentifier(targetingContext, response, false);
                allowing(pub).getEffectiveAdRequestTimeout();
                will(returnValue(2L));
                allowing(targetingEngine).selectCreative(with(any(AdSpaceDto.class)), with(nullCollection), with(any(TargetingContext.class)), with(any(boolean.class)),
                        with(any(boolean.class)), with(any(TimeLimit.class)), with(nullTargetingEventListener));
                will(returnValue(null));
                allowing(targetingContext).populateImpression(with(any(Impression.class)), with(nullSelectedCreative));
                oneOf(targetingContext).getAttribute(TargetingContext.UNFILLED_REASON);
                will(returnValue(null));

                allowing(adEventFactory).newInstance(AdAction.UNFILLED_REQUEST);
                will(returnValue(adEvent));
                allowing(adEvent).setUnfilledReason(unfilledReason);
                allowing(targetingContext).populateAdEvent(with(any(AdEvent.class)), with(any(Impression.class)), with(nullCreativeDto));
//                allowing(adEventLogger).logAdEvent(adEvent, targetingContext);
                allowing(adEvent).getAdAction();
                will(returnValue(AdAction.UNFILLED_REQUEST));
                allowing(adEvent).getUnfilledReason();
                will(returnValue(unfilledReason));
                allowing(adEvent).getEventTime();
                will(returnValue(eventTime));
                allowing(backupLogger).logUnfilledRequest(unfilledReason, eventTime, targetingContext);
                allowing(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));

                oneOf(targetingContext).getAttribute(TargetingContext.UNFILLED_REASON);
                will(returnValue(unfilledReason));

                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertNull(error);
        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        error = (String) modelMap.get("error");
        assertNull(error);
    }

    @Test
    public void testAdController16_handleRequest() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final String exceptionMessage = "your phone blocked";
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                will(throwException(new BlacklistedException(exceptionMessage)));
                oneOf(backupLogger).logAdRequestFailure("blacklisted", targetingContext, exceptionMessage);
            }
        });

        adController.handleRequest(request, response, modelMap, adSpaceExternalID);
        String error = (String) modelMap.get("error");
        assertEquals(error, "Sorry, we cannot process your request.");
    }
}

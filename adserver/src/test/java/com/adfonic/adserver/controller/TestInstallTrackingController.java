package com.adfonic.adserver.controller;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.ParallelModeBidManager;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TrackerClient;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.adserver.impl.DataCacheProperties;

public class TestInstallTrackingController extends BaseAdserverTest {

    private InstallTrackingController installTrackingController;
    private TargetingContextFactory targetingContextFactory;
    private PreProcessor preProcessor;
    private ImpressionService impressionService;
    private StatusChangeManager statusChangeManager;
    private AdResponseLogic adResponseLogic;
    private TrackingIdentifierLogic trackingIdentifierLogic;
    private TargetingEngine targetingEngine;
    private AdEventFactory adEventFactory;
    private ParallelModeBidManager parallelModeBidManager;
    private TrackerClient trackerClient;
    private DataCacheProperties dcProperties;

    //    private final String trackerBaseUrl = randomUrl();

    @Before
    public void initTests() {
        targetingContextFactory = mock(TargetingContextFactory.class, "targetingContextFactory");
        preProcessor = mock(PreProcessor.class, "preProcessor");
        impressionService = mock(ImpressionService.class, "impressionService");
        statusChangeManager = mock(StatusChangeManager.class);
        adResponseLogic = mock(AdResponseLogic.class);
        trackingIdentifierLogic = mock(TrackingIdentifierLogic.class);
        targetingEngine = mock(TargetingEngine.class);
        adEventFactory = mock(AdEventFactory.class);
        parallelModeBidManager = mock(ParallelModeBidManager.class);
        trackerClient = mock(TrackerClient.class);
        dcProperties = mock(DataCacheProperties.class);
        expect(new Expectations() {
            {
                oneOf(dcProperties).getProperty(DcProperty.TrackerRedirection);
                will(returnValue(null));
            }
        });
        installTrackingController = new InstallTrackingController(trackerClient, dcProperties);

        inject(installTrackingController, "targetingContextFactory", targetingContextFactory);
        inject(installTrackingController, "preProcessor", preProcessor);
        inject(installTrackingController, "impressionService", impressionService);
        inject(installTrackingController, "statusChangeManager", statusChangeManager);
        inject(installTrackingController, "adResponseLogic", adResponseLogic);
        inject(installTrackingController, "adEventFactory", adEventFactory);
        inject(installTrackingController, "targetingEngine", targetingEngine);
        inject(installTrackingController, "trackingIdentifierLogic", trackingIdentifierLogic);
        inject(installTrackingController, "parallelModeBidManager", parallelModeBidManager);
    }

    @Test
    public void testHandleInstallTrackingRequest01_exception_creating_context() throws Exception {
        final String appID = randomAlphaNumericString(10);
        final String uuid = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(throwException(new Exception("Failed to createTargetingContext")));
            }
        });

        installTrackingController.handleInstallTrackingRequest(request, response, appID, uuid);
    }

    @Test
    public void testHandleInstallTrackingRequest02_blacklisted() throws Exception {
        final String appID = randomAlphaNumericString(10);
        final String uuid = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                will(throwException(new BlacklistedException("your phone blocked")));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        installTrackingController.handleInstallTrackingRequest(request, response, appID, uuid);
    }

    @Test
    public void testHandleInstallTrackingRequest06_test_mode() throws Exception {
        final String appID = randomAlphaNumericString(10);
        final String uuid = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(targetingContext).isFlagTrue(Parameters.TEST_MODE);
                will(returnValue(true));
            }
        });

        installTrackingController.handleInstallTrackingRequest(request, response, appID, uuid);
        String responseString = response.getContentAsString();
        assertEquals(responseString, "success=1");

    }

    @Test
    public void testHandleInstallTrackingRequest03_IllegalArgumentException_tracking() throws Exception {
        final String appID = randomAlphaNumericString(10);
        final String uuid = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(targetingContext).isFlagTrue(Parameters.TEST_MODE);
                will(returnValue(false));
                oneOf(trackerClient).trackInstall(appID, uuid);
                will(throwException(new IllegalArgumentException("wrong request for conversion")));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        installTrackingController.handleInstallTrackingRequest(request, response, appID, uuid);
        String responseString = response.getContentAsString();
        assertEquals(responseString, "success=0&error=wrong+request+for+conversion");
    }

    @Test
    public void testHandleInstallTrackingRequest04_IOException_tracking() throws Exception {
        final String appID = randomAlphaNumericString(10);
        final String uuid = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(targetingContext).isFlagTrue(Parameters.TEST_MODE);
                will(returnValue(false));
                oneOf(trackerClient).trackInstall(appID, uuid);
                will(throwException(new IOException("Something IO messed-up request for conversion")));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        installTrackingController.handleInstallTrackingRequest(request, response, appID, uuid);
        String responseString = response.getContentAsString();
        assertEquals(responseString, "success=0&error=Internal+error");
    }

    @Test
    public void testHandleInstallTrackingRequest05_valid() throws Exception {
        final String appID = randomAlphaNumericString(10);
        final String uuid = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");
        final Map<String, String> responseMap = new HashMap<String, String>();
        responseMap.put("success", "1");
        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(targetingContext).isFlagTrue(Parameters.TEST_MODE);
                will(returnValue(false));
                oneOf(trackerClient).trackInstall(appID, uuid);
                will(returnValue(responseMap));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        installTrackingController.handleInstallTrackingRequest(request, response, appID, uuid);
        String responseString = response.getContentAsString();
        assertEquals(responseString, "success=1");
    }

    // MAD-730 - Delete ignored tests in Adserver project    
    //    @Ignore
    //    @Test
    //    public void testHandleInstallTrackingRequest06_trackerRedirect() throws Exception {
    //        inject(installTrackingController, "trackerRedirectEnabled", true);
    //        inject(installTrackingController, "trackerBaseUrl", trackerBaseUrl);
    //        final String requestUri = randomAlphaNumericString(10);
    //        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
    //        final HttpServletResponse response = mock(HttpServletResponse.class, "response");
    //        expect(new Expectations() {{
    //            // Expect the redirect to tracker and nothing more
    //            oneOf (request).getRequestURI(); will(returnValue(requestUri));
    //            oneOf (response).sendRedirect(trackerBaseUrl + requestUri);
    //        }});
    //        installTrackingController.handleInstallTrackingRequest(request, response, null, null);
    //    }
}

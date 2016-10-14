package com.adfonic.adserver.controller;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.ParallelModeBidManager;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TrackerClient;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.adserver.impl.DataCacheProperties;

public class TestConversionTrackingController extends BaseAdserverTest {

    private ConversionTrackingController conversionTrackingController;
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
    private byte[] gifContent;
    private final String trackerBaseUrl = "http://tracker.byyd.net";
    private DataCacheProperties dcProperties;

    @Before
    public void initTests() throws IOException {
        targetingContextFactory = mock(TargetingContextFactory.class, "targetingContextFactory");
        preProcessor = mock(PreProcessor.class, "preProcessor");
        impressionService = mock(ImpressionService.class, "impressionService");
        statusChangeManager = mock(StatusChangeManager.class);
        adResponseLogic = mock(AdResponseLogic.class);
        trackingIdentifierLogic = mock(TrackingIdentifierLogic.class);
        targetingEngine = mock(TargetingEngine.class);
        adEventFactory = mock(AdEventFactory.class);
        parallelModeBidManager = mock(ParallelModeBidManager.class);
        String imgageByte = new String("anything is byte dude");
        gifContent = imgageByte.getBytes();
        trackerClient = mock(TrackerClient.class);
        dcProperties = mock(DataCacheProperties.class);
        expect(new Expectations() {
            {
                oneOf(dcProperties).getProperty(DcProperty.TrackerRedirection);
                will(returnValue("false"));

            }
        });

        conversionTrackingController = new ConversionTrackingController(trackerClient, dcProperties);

        inject(conversionTrackingController, "targetingContextFactory", targetingContextFactory);
        inject(conversionTrackingController, "preProcessor", preProcessor);
        inject(conversionTrackingController, "impressionService", impressionService);
        inject(conversionTrackingController, "statusChangeManager", statusChangeManager);
        inject(conversionTrackingController, "adResponseLogic", adResponseLogic);
        inject(conversionTrackingController, "adEventFactory", adEventFactory);
        inject(conversionTrackingController, "targetingEngine", targetingEngine);
        inject(conversionTrackingController, "trackingIdentifierLogic", trackingIdentifierLogic);
        inject(conversionTrackingController, "parallelModeBidManager", parallelModeBidManager);
        conversionTrackingController.pixelBytes = gifContent;
    }

    @Test
    public void testConversionTrackingController01_handleConversionFromUser() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String clickExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(new InvalidIpAddressException("InvalidIP")));
            }
        });

        conversionTrackingController.handleConversionFromUser(request, response, clickExternalID);
    }

    @Test
    public void testConversionTrackingController02_handleConversionFromUser() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String clickExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(new Exception("Failed to createTargetingContext")));
            }
        });

        conversionTrackingController.handleConversionFromUser(request, response, clickExternalID);
    }

    @Test
    public void testConversionTrackingController03_handleConversionFromServer() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String clickExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(throwException(new Exception("Failed to createTargetingContext")));
            }
        });

        conversionTrackingController.handleConversionFromServer(request, response, clickExternalID);
    }

    @Test
    public void testConversionTrackingController04_handleConversionFromUser() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String clickExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                will(throwException(new BlacklistedException("your phone blocked")));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        conversionTrackingController.handleConversionFromUser(request, response, clickExternalID);
    }

    @Test
    public void testConversionTrackingController05_handleConversionFromServer() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String clickExternalID = randomAlphaNumericString(10);
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

        conversionTrackingController.handleConversionFromServer(request, response, clickExternalID);
    }

    @Test
    public void testConversionTrackingController06_handleConversionFromUser() throws InvalidIpAddressException, BlacklistedException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        conversionTrackingController.handleConversionFromUser(request, response, null);

    }

    @Test
    public void testConversionTrackingController07_handleConversionFromUser() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String clickExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                allowing(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                allowing(preProcessor).preProcessRequest(targetingContext);
                oneOf(trackerClient).trackConversion(clickExternalID);
                will(throwException(new IllegalArgumentException("wrong request for conversion")));
                oneOf(trackerClient).trackConversion(clickExternalID);
                will(throwException(new IOException("Something IO messed-up request for conversion")));
                oneOf(trackerClient).trackConversion(clickExternalID);
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        conversionTrackingController.handleConversionFromUser(request, response, clickExternalID);
        conversionTrackingController.handleConversionFromUser(request, response, clickExternalID);
        conversionTrackingController.handleConversionFromUser(request, response, clickExternalID);
    }

    @Test
    public void testConversionTrackingController08_handleConversionFromServer() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String clickExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(trackerClient).trackConversion(clickExternalID);
                will(throwException(new IllegalArgumentException("wrong request for conversion")));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        conversionTrackingController.handleConversionFromServer(request, response, clickExternalID);
        String responseString = response.getContentAsString();
        assertEquals(responseString, "success=0&error=wrong+request+for+conversion");
    }

    @Test
    public void testConversionTrackingController09_handleConversionFromServer() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String clickExternalID = randomAlphaNumericString(10);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TargetingContext targetingContext = mock(TargetingContext.class, "targetingContext");

        expect(new Expectations() {
            {
                oneOf(targetingContextFactory).createTargetingContext(request, false);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(trackerClient).trackConversion(clickExternalID);
                will(throwException(new IOException("Something IO messed-up request for conversion")));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        conversionTrackingController.handleConversionFromServer(request, response, clickExternalID);
        String responseString = response.getContentAsString();
        assertEquals(responseString, "success=0&error=Internal+error");
    }

    @Test
    public void testConversionTrackingController10_handleConversionFromServer() throws InvalidIpAddressException, BlacklistedException, IOException {
        final String clickExternalID = randomAlphaNumericString(10);
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
                oneOf(trackerClient).trackConversion(clickExternalID);
                will(returnValue(responseMap));
                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        conversionTrackingController.handleConversionFromServer(request, response, clickExternalID);
        String responseString = response.getContentAsString();
        assertEquals(responseString, "success=1");
    }

    @Test
    public void testHandleConversionFromUser11_trackerRedirect() throws Exception {
        inject(conversionTrackingController, "trackerRedirectEnabled", true);
        //inject(conversionTrackingController, "trackerBaseUrl", trackerBaseUrl);
        final String requestUri = randomAlphaNumericString(10);
        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
        final HttpServletResponse response = mock(HttpServletResponse.class, "response");
        expect(new Expectations() {
            {
                // Expect the redirect to tracker and nothing more
                oneOf(dcProperties).getProperty(DcProperty.TrackerBaseUrl);
                will(returnValue(trackerBaseUrl));
                oneOf(request).getRequestURI();
                will(returnValue(requestUri));
                oneOf(response).sendRedirect(trackerBaseUrl + requestUri);

            }
        });
        conversionTrackingController.handleConversionFromUser(request, response, null);
    }

    // MAD-730 - Delete ignored tests in Adserver project
    //    @Ignore
    //    @Test
    //    public void testHandleConversionFromServer12_trackerRedirect() throws Exception {
    //        inject(conversionTrackingController, "trackerRedirectEnabled", true);
    //        //inject(conversionTrackingController, "trackerBaseUrl", trackerBaseUrl);
    //        final String requestUri = randomAlphaNumericString(10);
    //        final HttpServletRequest request = mock(HttpServletRequest.class, "request");
    //        final HttpServletResponse response = mock(HttpServletResponse.class, "response");
    //        expect(new Expectations() {{
    //            // Expect the redirect to tracker and nothing more
    //            oneOf (request).getRequestURI(); will(returnValue(requestUri));
    //            oneOf (response).sendRedirect(trackerBaseUrl + requestUri);
    //            oneOf (response).setHeader("Expires", "0");
    //            oneOf (response).setHeader("Pragma", "No-Cache");
    //            oneOf (targetingContextFactory).createTargetingContext(request, false);
    //            allowing (preProcessor).preProcessRequest(null);
    //        }});
    //        conversionTrackingController.handleConversionFromServer(request, response, null);
    //    }

}

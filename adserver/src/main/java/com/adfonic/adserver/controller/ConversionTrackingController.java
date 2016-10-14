package com.adfonic.adserver.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.AdserverConstants;
import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TrackerClient;
import com.adfonic.adserver.impl.DataCacheProperties;
import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.util.HttpUtils;

/**
 * Superseeded by com.adfonic.tracker.controller.ConversionController
 * Some legacy stuff might be still out there, so do not remove yet...
 */
@Deprecated
@Controller
public class ConversionTrackingController extends AbstractAdServerController {
    private static final transient Logger LOG = Logger.getLogger(ConversionTrackingController.class.getName());

    byte[] pixelBytes = WebConfig.loadPixel();

    private final TrackerClient trackerClient;
    private final boolean trackerRedirectEnabled;
    private final DataCacheProperties dcProperties;

    @Autowired
    public ConversionTrackingController(TrackerClient trackerClient, DataCacheProperties dcProperties) throws IOException {
        this.trackerClient = trackerClient;
        this.dcProperties = dcProperties;
        this.trackerRedirectEnabled = Boolean.valueOf(dcProperties.getProperty(DcProperty.TrackerRedirection));
    }

    // TODO: phase this out once advertisers are transitioned to the longer form
    @RequestMapping("/cb/conversion.gif")
    public void handleConversionFromUser(HttpServletRequest request, HttpServletResponse response,
            @CookieValue(value = AdserverConstants.CLICK_ID_COOKIE, required = false) String clickExternalID) throws java.io.IOException {
        handleConversionFromUser(request, response, null, clickExternalID);
    }

    @RequestMapping("/cb/{advertiserExternalID}/conversion.gif")
    public void handleConversionFromUser(HttpServletRequest request, HttpServletResponse response, @PathVariable String advertiserExternalID,
            @CookieValue(value = AdserverConstants.CLICK_ID_COOKIE, required = false) String clickExternalID) throws java.io.IOException {
        if (trackerRedirectEnabled) {
            response.sendRedirect(dcProperties.getProperty(DcProperty.TrackerBaseUrl) + request.getRequestURI());
            return;
        }

        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        // This is kinda funky, since we're not actually targeting anything
        // at this point, but the TargetingContext is really just a simple
        // container of attributes, and it allows us to interact with the
        // derivers most easily.  We also use it to grab the "actual" IP
        // address of the request, which has been derived intelligently
        // for us at this point.
        // We also need the context in order to pre-process, which does
        // User-Agent munging and blacklist blocking, etc.
        TargetingContext context;
        try {
            context = getTargetingContextFactory().createTargetingContext(request, true);
        } catch (InvalidIpAddressException e) {
            // We're specifically logging this exception this way so it
            // shows up in the logs but won't trip up the error scavenger.
            //LOG.warning(e.getMessage());
            LoggingUtils.log(LOG, Level.WARNING, null, null, this.getClass(), "handleConversionFromUser", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (Exception e) {
            //LOG.log(Level.WARNING, "Failed to createTargetingContext", e);
            LoggingUtils.log(LOG, Level.WARNING, null, null, this.getClass(), "handleConversionFromUser", "Failed to createTargetingContext", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // By pre-processing, the main thing we're doing here (other than
        // enforcing the blacklist) is munging the effective User-Agent
        // however needed.
        // Pre-process the request, which will throw a BlacklistedException
        // if the request should be denied.
        try {
            getPreProcessor().preProcessRequest(context);
        } catch (com.adfonic.adserver.BlacklistedException e) {
            //LOG.warning("Dropping blacklisted request (clickExternalID=" + clickExternalID + ") due to " + e.getMessage());
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleConversionFromUser", "Dropping blacklisted request (clickExternalID=" + clickExternalID
                    + ") due to " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Go ahead and serve the 1x1 transparent GIF now, before we
        // bother tracking anything.  That way we can do convenient
        // return calls if we want to "abort" below.
        response.setContentType("image/gif");
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(pixelBytes);
        outputStream.flush();

        if (clickExternalID == null) {
            //LOG.warning("User-to-server conversion tracking invoked with no click cookie");
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleConversionFromUser", "User-to-server conversion tracking invoked with no click cookie");
            return;
        }

        // Since this isn't a server-to-server call, we don't need to do anything
        // with the response map.  We already served the 1x1 gif to the caller.
        // So don't bother grabbing the return value from this call...
        try {
            trackerClient.trackConversion(clickExternalID);
        } catch (IllegalArgumentException e) {
            // The clickExternalID value must be invalid
            //LOG.warning("Tracker request for conversion of clickExternalID=" + clickExternalID + " failed: " + e.getMessage());
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleConversionFromUser", "Tracker request for conversion of clickExternalID=" + clickExternalID
                    + " failed: " + e.getMessage());
        } catch (java.io.IOException e) {
            //LOG.log(Level.WARNING, "Tracker request for conversion of clickExternalID=" + clickExternalID + " failed", e);
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleConversionFromUser", "Tracker request for conversion of clickExternalID=" + clickExternalID
                    + " failed", e);
        }
    }

    // TODO: phase this out once advertisers are transitioned to the longer form
    @RequestMapping("/cs/{clickExternalID}")
    public void handleConversionFromServer(HttpServletRequest request, HttpServletResponse response, @PathVariable String clickExternalID) throws java.io.IOException {
        handleConversionFromServer(request, response, null, clickExternalID);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping("/cs/{advertiserExternalID}/{clickExternalID}")
    public void handleConversionFromServer(HttpServletRequest request, HttpServletResponse response, @PathVariable String advertiserExternalID, @PathVariable String clickExternalID)
            throws java.io.IOException {
        if (trackerRedirectEnabled) {
            response.sendRedirect(dcProperties.getProperty(DcProperty.TrackerBaseUrl) + request.getRequestURI());
        }

        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        // This is kinda funky, since we're not actually targeting anything
        // at this point, but the TargetingContext is really just a simple
        // container of attributes, and it allows us to interact with the
        // derivers most easily.  We also use it to grab the "actual" IP
        // address of the request, which has been derived intelligently
        // for us at this point.
        // We also need the context in order to pre-process, which does
        // User-Agent munging and blacklist blocking, etc.
        TargetingContext context;
        try {
            context = getTargetingContextFactory().createTargetingContext(request, false);
        } catch (Exception e) {
            //LOG.log(Level.WARNING, "Failed to createTargetingContext", e);
            LoggingUtils.log(LOG, Level.WARNING, null, null, this.getClass(), "handleConversionFromServer", "Failed to createTargetingContext", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // By pre-processing, the main thing we're doing here (other than
        // enforcing the blacklist) is munging the effective User-Agent
        // however needed.
        // Pre-process the request, which will throw a BlacklistedException
        // if the request should be denied.
        try {
            getPreProcessor().preProcessRequest(context);
        } catch (com.adfonic.adserver.BlacklistedException e) {
            //LOG.warning("Dropping blacklisted request (clickExternalID=" + clickExternalID + ") due to " + e.getMessage());
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleConversionFromServer", "Dropping blacklisted request (clickExternalID=" + clickExternalID
                    + ") due to " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Map responseMap;
        try {
            responseMap = trackerClient.trackConversion(clickExternalID);
        } catch (IllegalArgumentException e) {
            // The clickExternalID value must be invalid
            //LOG.warning("Tracker request for conversion of clickExternalID=" + clickExternalID + " failed: " + e.getMessage());
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleConversionFromServer", "Tracker request for conversion of clickExternalID="
                    + clickExternalID + " failed: " + e.getMessage());
            responseMap = new LinkedHashMap<String, String>();
            responseMap.put("success", "0");
            responseMap.put("error", e.getMessage());
        } catch (java.io.IOException e) {
            //LOG.log(Level.WARNING, "Tracker request for conversion of clickExternalID=" + clickExternalID + " failed", e);
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleConversionFromServer", "Tracker request for conversion of clickExternalID="
                    + clickExternalID + " failed", e);
            responseMap = new LinkedHashMap<String, String>();
            responseMap.put("success", "0");
            responseMap.put("error", "Internal error");
        }

        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Response map: " + responseMap);
            LoggingUtils.log(LOG, Level.FINE, null, context, this.getClass(), "handleConversionFromServer", "Response map: " + responseMap);
        }

        // Write the response map entries as a URL-encoded string
        response.getWriter().append(HttpUtils.encodeParams(responseMap)).flush();
    }
}

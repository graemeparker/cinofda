package com.adfonic.adserver.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TrackerClient;
import com.adfonic.adserver.impl.DataCacheProperties;
import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.util.HttpUtils;

/**
 * Superseeded by com.adfonic.tracker.controller.InstallController
 * There is still adserver access to this context so leave it...
 */
@Deprecated
@Controller
public class InstallTrackingController extends AbstractAdServerController {
    private static final transient Logger LOG = Logger.getLogger(InstallTrackingController.class.getName());

    private final TrackerClient trackerClient;
    private final boolean trackerRedirectEnabled;
    private final DataCacheProperties dcProperties;

    @Autowired
    public InstallTrackingController(TrackerClient trackerClient, DataCacheProperties dcProperties) {
        this.trackerClient = trackerClient;
        this.dcProperties = dcProperties;
        this.trackerRedirectEnabled = Boolean.valueOf(dcProperties.getProperty(DcProperty.TrackerRedirection));
    }

    @RequestMapping("/is/{appId}/{udid}")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void handleInstallTrackingRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String appId, @PathVariable String udid)
            throws java.io.IOException {
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
            context = getTargetingContextFactory().createTargetingContext(request, false);
        } catch (Exception e) {
            //LOG.log(Level.WARNING, "Failed to createTargetingContext", e);
            LoggingUtils.log(LOG, Level.WARNING, null, null, this.getClass(), "handleInstallTrackingRequest", "Failed to createTargetingContext", e);
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
            //LOG.warning("Dropping blacklisted request: " + e.getMessage());
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleInstallTrackingRequest", "Dropping blacklisted request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (context.isFlagTrue(Parameters.TEST_MODE)) {
            // Don't bother tracking the install in test mode
            response.getWriter().append("success=1").flush();
            return;
        }

        // Hit the tracker web service to de-dup, log the event if necessary,
        // and provide us with the response values to send
        Map responseMap;
        try {
            responseMap = trackerClient.trackInstall(appId, udid);
        } catch (IllegalArgumentException e) {
            // One or both of the arguments were invalid
            //LOG.warning("Tracker request for install of appId=" + appId + ", udid=" + udid + " failed: " + e.getMessage());
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleInstallTrackingRequest", "Tracker request for install of appId=" + appId + ", udid=" + udid
                    + " failed: " + e.getMessage());
            responseMap = new LinkedHashMap<String, String>();
            responseMap.put("success", "0");
            responseMap.put("error", e.getMessage());
        } catch (java.io.IOException e) {
            // Something went wrong with the tracker call
            //LOG.log(Level.WARNING, "Tracker request for install of appId=" + appId + ", udid=" + udid + " failed", e);
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleInstallTrackingRequest", "Tracker request for install of appId=" + appId + ", udid=" + udid
                    + " failed", e);
            responseMap = new LinkedHashMap<String, String>();
            responseMap.put("success", "0");
            responseMap.put("error", "Internal error");
        }

        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Response map: " + responseMap);
            LoggingUtils.log(LOG, Level.WARNING, null, context, this.getClass(), "handleInstallTrackingRequest", "Response map: " + responseMap);
        }

        // Write the response map entries as a URL-encoded string
        response.getWriter().append(HttpUtils.encodeParams(responseMap)).flush();
    }
}

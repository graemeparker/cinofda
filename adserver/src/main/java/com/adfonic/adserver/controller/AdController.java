package com.adfonic.adserver.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.ResponseFormat;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.logging.LoggingUtils;

@Controller
public class AdController extends AbstractAdServerController {

    private static final transient Logger LOG = Logger.getLogger(AdController.class.getName());

    @Override
    protected void setupTargetingContext(TargetingContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Make sure tracking identifier stuff is set up on the targeting context.
        getTrackingIdentifierLogic().establishTrackingIdentifier(context, response, false); // cookies are not allowed
    }

    @RequestMapping("/ad/{adSpaceExternalId}")
    public String handleRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap, @PathVariable String adSpaceExternalId) throws java.io.IOException {
        getBackupLogger().startControllerRequest();
        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        // Determine the desired response format, i.e. urlencode/xml/json.
        ResponseFormat responseFormat = null;
        String responseFormatName = request.getParameter(Parameters.FORMAT);
        if (responseFormatName != null) {
            try {
                responseFormat = ResponseFormat.valueOf(responseFormatName);
            } catch (Exception e) {
                if ("wml".equals(responseFormatName)) {
                    // As far as our ad response is concerned, WML is the same
                    // as HTML.  We're just serving <a> <img> and <br> tags,
                    // which are all supported in WML.
                    if (LOG.isLoggable(Level.FINE)) {
                        //LOG.fine("Mapping response format WML -> HTML");
                        LoggingUtils.log(LOG, Level.FINE, null, null, this.getClass(), "handleRequest", "Mapping response format WML -> HTML");
                    }
                    responseFormat = ResponseFormat.html;
                } else {
                    //LOG.warning("Invalid ResponseFormat: " + responseFormatName);
                    LoggingUtils.log(LOG, Level.WARNING, null, null, this.getClass(), "handleRequest", "Invalid ResponseFormat: " + responseFormatName);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    getBackupLogger().logAdRequestFailure("invalid ResponseFormat", null, responseFormatName);
                    return null;
                }
            }
        }

        if (responseFormat == null) {
            // Response format hasn't been specified...which isn't a problem.
            // We can default the response format to the regular urlencode
            responseFormat = ResponseFormat.urlencode;
        }

        // Generate the ad using the common base class logic
        try {
            // Pass false so direct headers aren't used, only proxied headers
            handleGenerateAd(request, response, adSpaceExternalId, modelMap, false);
        } catch (com.adfonic.adserver.BlacklistedException e) {
            if (LOG.isLoggable(Level.INFO)) {
                //LOG.info("Blacklisted request: " + e.getMessage());
                LoggingUtils.log(LOG, Level.INFO, null, null, this.getClass(), "handleRequest", "Blacklisted request: " + e.getMessage());
            }
            // Give them a generic/ambiguous error message.  I tried it at
            // first with the exception message, but imho that gives the
            // offending party too much info, i.e.:
            // "IP is blacklisted: 127.0.0.1 (subnet=127.0.0.0/24)"
            // We don't want to tell 'em our config...so just give them
            // a vanilla error message.
            modelMap.addAttribute("error", "Sorry, we cannot process your request.");
        }

        // Determine the appropriate view name to render the response
        String viewName = responseFormat.name() + "AdView";
        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Using view: " + viewName);
            LoggingUtils.log(LOG, Level.FINE, null, null, this.getClass(), "handleRequest", "Using view: " + viewName);
        }
        return viewName;
    }
}

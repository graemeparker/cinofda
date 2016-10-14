package com.adfonic.adserver.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.logging.LoggingUtils;

@Controller
public class JavascriptAdController extends AbstractAdServerController {
    private static final transient Logger LOG = Logger.getLogger(JavascriptAdController.class.getName());

    @Override
    protected void setupTargetingContext(TargetingContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Make sure tracking identifier stuff is set up on the targeting context.
        getTrackingIdentifierLogic().establishTrackingIdentifier(context, response, true); // cookies are allowed
    }

    @RequestMapping("/js/{adSpaceExternalId}")
    public String handleRequest(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap, @PathVariable String adSpaceExternalId) throws java.io.IOException {
        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        // Generate the ad using the common base class logic
        try {
            // Pass true so direct headers are used
            handleGenerateAd(request, response, adSpaceExternalId, modelMap, true);
        } catch (com.adfonic.adserver.BlacklistedException e) {
            //LOG.warning("Dropping blacklisted request: " + e.getMessage());
            LoggingUtils.log(LOG, Level.WARNING, null, null, this.getClass(), "handleRequest", "Dropping blacklisted request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        // Let the javascript view take care of rendering the response
        return "javascriptAdView";
    }
}

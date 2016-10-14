package com.adfonic.adserver.controller;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.impl.MarkupGeneratorImpl;
import com.adfonic.adserver.plugin.Plugin;
import com.adfonic.adserver.plugin.PluginManager;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;

@Controller
public class PluginTestController extends AbstractAdServerController {
    private static final transient Logger LOG = Logger.getLogger(PluginTestController.class.getName());

    @Autowired
    @Qualifier(MarkupGeneratorImpl.BEAN_NAME)
    private MarkupGenerator markupGenerator;
    @Autowired
    private PluginManager pluginManager;

    /** Generic ad-generating request handler */
    @RequestMapping("/plugin/{adSpaceExternalID}/{creativeExternalID}")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID, @PathVariable String creativeExternalID)
            throws ServletException, java.io.IOException {
        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        // Create the targeting context
        TargetingContext context;
        try {
            context = getTargetingContextFactory().createTargetingContext(request, false);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // Look up the AdSpace
        AdSpaceDto adSpace = context.getAdserverDomainCache().getAdSpaceByExternalID(adSpaceExternalID);
        if (adSpace == null) {
            throw new ServletException("No such AdSpace: " + adSpaceExternalID);
        }
        context.setAdSpace(adSpace);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("AdSpace \"" + adSpace.getName() + "\" externalID=" + adSpace.getExternalID());
        }

        // Look up the Creative
        CreativeDto creative = context.getAdserverDomainCache().getCreativeByExternalID(creativeExternalID);
        if (creative == null) {
            throw new ServletException("No such Creative: " + creativeExternalID);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Creative \"" + creative.getName() + "\" externalID=" + creative.getExternalID());
        }

        PluginCreativeInfo pluginCreativeInfo = context.getAdserverDomainCache().getPluginCreativeInfo(creative);

        // Snag the Plugin
        Plugin plugin = pluginManager.getPluginByName(pluginCreativeInfo.getPluginName());
        if (plugin == null) {
            throw new ServletException("No such plugin: " + pluginCreativeInfo.getPluginName());
        }

        // Look up the AdserverPlugin
        AdserverPluginDto adserverPlugin = context.getDomainCache().getAdserverPluginBySystemName(pluginCreativeInfo.getPluginName());
        if (adserverPlugin == null) {
            throw new ServletException("No such AdserverPlugin: " + pluginCreativeInfo.getPluginName());
        }

        response.setContentType("text/html");
        PrintStream out = new PrintStream(response.getOutputStream());
        out.println("<html><head><title>Plugin Test</title></head><body>");

        // Invoke the plugin to generate an ad
        ProxiedDestination pd;
        try {
            pd = plugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, null);
            out.println("<p>" + pd.toString() + "</p>");
            out.println("<p>Markup:</p>");
            out.println(markupGenerator.generateMarkup(pd, context, adSpace, creative, null, false));
        } catch (Exception e) {
            out.print("<pre>");
            e.printStackTrace(out);
            out.print("</pre>");
        }

        out.println("</body></html>");
        out.close();
    }
}

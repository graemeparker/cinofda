package com.adfonic.adserver.controller;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ParallelModeBidDetails;
import com.adfonic.adserver.ParallelModeBidManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@Controller
public class ParallelModeController extends AbstractAdServerController {
    private static final transient Logger LOG = Logger.getLogger(ParallelModeController.class.getName());

    @Autowired
    private ParallelModeBidManager parallelModeBidManager;

    @RequestMapping("/pw/{adSpaceExternalID}/{impressionExternalID}")
    public void handleParallelModeWinNotice(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID,
            @PathVariable String impressionExternalID) throws java.io.IOException {
        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        response.setContentType("application/json");

        // This is kinda funky, since we're not actually targeting anything
        // at this point, but the TargetingContext is required in order to
        // do pre-processing for blacklist enforcement.
        TargetingContext context;
        try {
            context = getTargetingContextFactory().createTargetingContext(request, false);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to createTargetingContext", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // Pre-process the request, which will throw a BlacklistedException
        // if the request should be denied.
        try {
            getPreProcessor().preProcessRequest(context);
        } catch (com.adfonic.adserver.BlacklistedException e) {
            LOG.warning("Dropping blacklisted request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        AdSpaceDto adSpace = context.getAdserverDomainCache().getAdSpaceByExternalID(adSpaceExternalID);
        if (adSpace == null) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Invalid adSpaceExternalID: " + adSpaceExternalID);
            }
            response.getWriter().append("{\"success\":\"0\",\"error\":\"Invalid AdSpace ID: " + adSpaceExternalID + "\"}");
            return;
        }

        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Win notice for AdSpace id=" + adSpace.getId() + ", impressionExternalID=" + impressionExternalID);
        }

        // Grab the BidDetails from cache, and simultaneously remove them from cache.
        // By removing, we ensure that our batch flush won't log a BID_FAILED later.
        ParallelModeBidDetails bidDetails = parallelModeBidManager.removeBidDetails(impressionExternalID);
        if (bidDetails == null) {
            LOG.warning("ParallelModeBidDetails not found: " + impressionExternalID + " (adSpace: " + adSpaceExternalID + ")");
            response.getWriter().append("{\"success\":\"0\",\"error\":\"Bid not found\"}");
            return;
        }

        Impression impression = bidDetails.getImpression();

        // Make sure the impression's AdSpace matches
        if (!adSpace.getId().equals(impression.getAdSpaceId())) {
            LOG.warning("AdSpace mismatch on parallel mode win notice, Impression " + impressionExternalID + " references AdSpace id=" + impression.getAdSpaceId()
                    + " but request is for AdSpace id=" + adSpace.getId());
            response.getWriter().append("{\"success\":\"0\",\"error\":\"Bid not found\"}");
            return;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Impression " + impression.getExternalID() + ", Creative id=" + impression.getCreativeId());
        }

        // Reconstruct the TargetingContext from the BidDetails
        context = parallelModeBidManager.getTargetingContextFromBidDetails(bidDetails);

        context.setAdSpace(adSpace);

        CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
        if (creative == null) {
            if ((creative = context.getAdserverDomainCache().getRecentlyStoppedCreativeById(impression.getCreativeId())) != null) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Win notice on recently stopped Creative id=" + creative.getId());
                }
            } else {
                LOG.warning("Win notice impression references unknown Creative id=" + impression.getCreativeId());
                response.getWriter().append("{\"success\":\"0\",\"error\":\"Creative not found\"}");
                return;
            }
        }

        // Log the AD_SERVED event
        //TODO Clean up code when decomissioning JMS
//        AdEvent event = getAdEventFactory().newInstance(AdAction.AD_SERVED);
//        context.populateAdEvent(event, impression, creative);
//        getAdEventLogger().logAdEvent(event, context);
        getBackupLogger().logAdServed(impression, new Date(), context);

        response.getWriter().append("{\"success\":\"1\"}");
    }
}

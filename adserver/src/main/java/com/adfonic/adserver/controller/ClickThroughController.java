package com.adfonic.adserver.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adresponse.AdMarkupRenderer;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.impl.ClickUtils;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.util.stats.FreqLogr;

@Controller
public class ClickThroughController {

    private static final transient Logger LOG = LoggerFactory.getLogger(ClickThroughController.class.getName());

    @Autowired
    private TargetingContextFactory targetingContextFactory;

    @Autowired
    private PreProcessor preProcessor;

    @Autowired
    private ImpressionService impressionService;

    @Autowired
    private ClickUtils clickUtils;

    @Autowired
    private AdServerStats astats;

    @Autowired
    private BackupLogger backupLogger;

    @RequestMapping(Constant.CLICK_THROUGH_PATH + "/{adSpaceExternalID}/{impressionExternalID}")
    public void handleClickThroughRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID,
            @PathVariable String impressionExternalID, @RequestParam(value = AdMarkupRenderer.CLICK_FORWARD_URL_PARAM, required = false) String clickForwardURL)
            throws java.io.IOException {
        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        try {
            // This is kinda funky, since we're not actually targeting anything
            // at this point, but the TargetingContext is really just a simple
            // container of attributes, and it allows us to interact with the
            // derivers most easily.  We also use it to grab the "actual" IP
            // address of the request, which has been derived intelligently
            // for us at this point.
            // We also need the context in order to pre-process, which does
            // User-Agent munging and blacklist blocking, etc.
            TargetingContext context = targetingContextFactory.createTargetingContext(request, true); // throws InvalidIpAddressException

            // By pre-processing, the main thing we're doing here (other than
            // enforcing the blacklist) is munging the effective User-Agent
            // however needed.
            // Pre-process the request, which will throw a BlacklistedException
            // if the request should be denied.

            preProcessor.preProcessRequest(context); // throws BlacklistedException

            doClickThrough(context, request, response, adSpaceExternalID, impressionExternalID, clickForwardURL);

        } catch (InvalidIpAddressException iiap) {
            LOG.info("ClickThrough request IP is invalid: " + iiap.getMessage());
            backupLogger.logClickFailure(impressionExternalID, iiap.getMessage(), null);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

        } catch (com.adfonic.adserver.BlacklistedException bx) {
            LOG.info("ClickThrough request is blacklisted: " + bx.getMessage());
            backupLogger.logClickFailure(impressionExternalID, "blacklisted", null, bx.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception x) {
            FreqLogr.report(x, "ClickThrough request failed");
            backupLogger.logClickFailure(impressionExternalID, "exception", null, x.getClass().getName(), x.getMessage());
            //astats.increment(adSpace, AsCounter.ClickError);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * This is only tiny extension that smuggles exchange name into click url so it can be easily find in access log.
     * Otherwise it works exactly same as original click handler
     */
    @RequestMapping(Constant.CLICK_THROUGH_PATH + "/{ExchangeID}/{adSpaceExternalID}/{impressionExternalID}")
    public void handleClickThroughExchange(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID,
            @PathVariable String impressionExternalID, @RequestParam(value = AdMarkupRenderer.CLICK_FORWARD_URL_PARAM, required = false) String clickForwardURL)
            throws java.io.IOException {
        // Just call original handler
        handleClickThroughRequest(request, response, adSpaceExternalID, impressionExternalID, clickForwardURL);
    }

    private void doClickThrough(TargetingContext context, HttpServletRequest request, HttpServletResponse response, String adSpaceExternalID, String impressionExternalID,
            String clickForwardURL) throws InvalidIpAddressException, BlacklistedException, IOException {

        AdserverDomainCache adCache = context.getAdserverDomainCache();

        // Be aware that AdSpace might be null - ExternalID may be broken or AdSpace is no longer in cache
        AdSpaceDto adSpace = adCache.getAdSpaceByExternalID(adSpaceExternalID);

        // Look up the Impression from cache
        Impression impression = impressionService.getImpression(impressionExternalID);
        if (impression == null) {
            // Either the Impression externalID provided is broken or the Impression already expired from cache
            LOG.warn("Impression not found: " + impressionExternalID + ", AdSpace: " + adSpaceExternalID);
            astats.increment(adSpace, AsCounter.ClickImpressionNotFound);

            // #556 - we can't find the Impression, but we shouldn't just
            // dead-end the user when they click.  Toss 'em to the fallback URL.
            clickUtils.redirectToFallbackUrl(request, response);
            // Don't log this as a click or anything, since...well...we can't.
            // Without the Impression we don't know enough to log the event
            // intelligently.

            backupLogger.logClickFailure(impressionExternalID, "Impression not found", context);
            return;
        }

        astats.click(impression);

        // Not having AdSpace is NOT showstopper in Click processing (contrary to Beacon tracker processing) 
        if (adSpace != null) {
            // Usual path
            if (!adSpace.getId().equals(impression.getAdSpaceId())) {
                // If we have AdSpace from both parameter and impression, verify that they are same. Bit of paranoia...
                LOG.warn("AdSpace parameter mismatch: " + adSpaceExternalID + "/" + adSpace.getId() + " vs Impression stored AdSpaceId: " + impression.getAdSpaceId());
                astats.increment(adSpace, AsCounter.ClickAdSpaceMismatch);
                backupLogger.logClickFailure(impression, "AdSpace mismatch", context);

                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return; // 
            }
        } else {
            // Well, it is highly unlikely that AdSpace is NOT found by ExternalID parameter but it IS found by ID from Impression
            // Could happen only if AdSpace ExternalID parameter got messed up somehow
            LOG.info("AdSpace not found by External ID parameter: " + adSpaceExternalID + " Getting it from Impression stored AdSpaceId: " + impression.getAdSpaceId());
            adSpace = adCache.getAdSpaceById(impression.getAdSpaceId());
            if (adSpace == null) {
                // OK we tried both ids - AdSpace is really NOT in cache
                LOG.warn("AdSpace not found: " + adSpaceExternalID + "/" + impression.getAdSpaceId() + ", Impression: " + impressionExternalID);
                astats.increment(AsCounter.ClickAdSpaceNotFound);
                // let it be...
            }
        }

        context.setAdSpace(adSpace); // May be null

        // Creative is must have of course. We need destination URL from it
        CreativeDto creative = adCache.getCreativeById(impression.getCreativeId());
        if (creative == null) {
            creative = adCache.getRecentlyStoppedCreativeById(impression.getCreativeId());
            if (creative == null) {
                LOG.warn("Creative not found: " + impression.getCreativeId() + ", AdSpace: " + adSpaceExternalID + ", Impression:" + impressionExternalID);
                astats.increment(adSpace, AsCounter.ClickCreativeNotFound);
                backupLogger.logClickFailure(impression, "Creative not found", context, String.valueOf(impression.getCreativeId()));

                clickUtils.redirectToFallbackUrl(request, response);
                return;
            }
        }

        // Determine the destination URL where we need to redirect the user
        String targetURL = clickUtils.getTargetUrl(impression, creative);
        if (StringUtils.isBlank(targetURL)) {
            LOG.warn("Click target url in null for Creative: " + creative.getId() + ", AdSpace: " + adSpace.getId());
            backupLogger.logClickFailure(impression, "no target URL", context);

            clickUtils.redirectToFallbackUrl(request, response);
            return;
        }

        // AF-1483 - only cookie if conversion tracking is enabled
        if (creative.getCampaign().isConversionTrackingEnabled()) {
            clickUtils.setClickIdCookie(response, impression, creative);
        }

        // Invoke the click tracking logic...which also calls the appropriate BackupLogger method for the respective outcome
        boolean isTracked = clickUtils.trackClick(adSpace, creative, impression, context, clickForwardURL);

        // - post-process the targetURL and redirect
        targetURL = clickUtils.processRedirectUrl(targetURL, isTracked, adSpace, creative, impression, context, true);
        response.sendRedirect(targetURL);

        astats.clickCompleted(impression.getAdSpaceId(), impression.getCreativeId());
    }

}

package com.adfonic.adserver.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adserver.BackupLogger;
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
public class ClickRedirectController {

    private static final transient Logger LOG = LoggerFactory.getLogger(ClickRedirectController.class.getName());

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

    @RequestMapping(Constant.CLICK_REDIRECT_PATH + "/{adSpaceExternalID}/{impressionExternalID}")
    public void handleClickRedirectRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID,
            @PathVariable String impressionExternalID, @RequestParam(Constant.CLICK_REDIRECT_URL_PARAM) String redir) throws java.io.IOException {
        TargetingContext context;

        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");
        try {
            context = targetingContextFactory.createTargetingContext(request, true);

            // By pre-processing, the main thing we're doing here (other than
            // enforcing the blacklist) is munging the effective User-Agent
            // however needed.
            // Pre-process the request, which will throw a BlacklistedException
            // if the request should be denied.

            preProcessor.preProcessRequest(context);

            doClickRedirect(context, request, response, adSpaceExternalID, impressionExternalID, redir);

        } catch (InvalidIpAddressException iiap) {
            LOG.info("ClickRedirect request IP is invalid: " + iiap.getMessage());
            backupLogger.logClickFailure(impressionExternalID, iiap.getMessage(), null);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

        } catch (com.adfonic.adserver.BlacklistedException bx) {
            LOG.info("ClickRedirect request is blacklisted: " + bx.getMessage());
            backupLogger.logClickFailure(impressionExternalID, "blacklisted", null, bx.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception x) {
            FreqLogr.report(x, "ClickRedirect request failed");
            backupLogger.logClickFailure(impressionExternalID, "exception", null, x.getClass().getName(), x.getMessage());
            //TODO astats.increment(AsCounter.ClickError);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    /**
     * This is only tiny extension that smuggles exchange name into click url so it can be easily find in access log.
     * Otherwise it works exactly same as original click handler
     */
    @RequestMapping(Constant.CLICK_REDIRECT_PATH + "/{ExchangeID}/{adSpaceExternalID}/{impressionExternalID}")
    public void handleClickRedirectExchange(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID,
            @PathVariable String impressionExternalID, @RequestParam(Constant.CLICK_REDIRECT_URL_PARAM) String redir) throws java.io.IOException {
        // Just call original handler
        handleClickRedirectRequest(request, response, adSpaceExternalID, impressionExternalID, redir);
    }

    private void doClickRedirect(TargetingContext context, HttpServletRequest request, HttpServletResponse response, String adSpaceExternalID, String impressionExternalID,
            String redirectUrl) throws IOException {

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
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
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

        // We have destination url passed as parameter so it is not necessary to have Creative
        CreativeDto creative = adCache.getCreativeById(impression.getCreativeId());
        if (creative == null) {
            creative = adCache.getRecentlyStoppedCreativeById(impression.getCreativeId());
            if (creative == null) {
                LOG.warn("Creative not found: " + impression.getCreativeId() + ", AdSpace: " + adSpaceExternalID + ", Impression:" + impressionExternalID);
                astats.increment(adSpace, AsCounter.ClickCreativeNotFound);
                // can continue
            }
        }

        if (creative == null || creative.getCampaign().isConversionTrackingEnabled()) {
            clickUtils.setClickIdCookie(response, impression, creative);
        }

        boolean isTracked = false;
        if (creative != null) {
            isTracked = clickUtils.trackClick(adSpace, creative, impression, context, null);
        }

        redirectUrl = clickUtils.processRedirectUrl(redirectUrl, isTracked, adSpace, creative, impression, context, false);
        response.sendRedirect(redirectUrl);

        astats.clickCompleted(impression.getAdSpaceId(), impression.getCreativeId());
    }

}

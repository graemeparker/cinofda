package com.adfonic.adserver.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BeaconUtils;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.rtb.impl.AdsquareWorker;
import com.adfonic.adserver.rtb.impl.RtbBidLogicImpl;
import com.adfonic.adserver.rtb.impl.RtbWinLogicImpl;
import com.adfonic.adserver.rtb.impl.RtbWinLogicImpl.RubiconVastRtbConfig;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.RtbConfig.RtbWinNoticeMode;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;

@Controller
public class BeaconController {

    private static final transient Logger LOG = LoggerFactory.getLogger(BeaconController.class.getName());

    byte[] pixelBytes = WebConfig.loadPixel();

    @Autowired
    private RtbWinLogicImpl rtbWinLogic;

    @Autowired
    private AdsquareWorker adsquareWorker;

    @Autowired
    private TargetingContextFactory targetingContextFactory;

    @Autowired
    private PreProcessor preProcessor;

    @Autowired
    private ImpressionService impressionService;

    @Autowired
    private AdEventFactory adEventFactory;

    @Autowired
    private AdServerStats astats;

    @Autowired
    private BackupLogger backupLogger;

    @RequestMapping(Constant.BEACON_URI_PATH + "/{adSpaceExternalID}/{impressionExternalID}.gif")
    public void handleBeacon(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID, @PathVariable String impressionExternalID,
            @RequestParam(value = Constant.SP_URL_PARAM, required = false) String rtbSettlementPrice) throws java.io.IOException {
        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        // Go ahead and write the response image out first, which allows us
        // simply to return from any point in the logic below.
        response.setContentType("image/gif");
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(pixelBytes);
        outputStream.flush();

        if ("SCREENING".equals(rtbSettlementPrice)) {
            //Ignore Rubicon Ad screening - MAD-1753 
            //Rubicon is displaying Ad for screening purposes and sends 'SCREENING' instead of price to distinguish from real impression  
            //Screening can happen long after original Bid request and response were processes so impression will be most likely expired
            //getCounterManager().incrementCounter(BeaconController.class, Counter.RUBICON_SCREENING);
            return;
        }

        TargetingContext context;
        try {
            // This is kinda funky, since we're not actually targeting anything
            // at this point, but the TargetingContext is really just a simple
            // container of attributes, and it allows us to interact with the
            // derivers most easily.  We also use it to grab the "actual" IP
            // address of the request, which has been derived intelligently
            // for us at this point.
            // We also need the context in order to pre-process, which does
            // User-Agent munging and blacklist blocking, etc.
            context = targetingContextFactory.createTargetingContext(request, true);
            // By pre-processing, the main thing we're doing here (other than enforcing the blacklist) is munging the effective User-Agent however needed.
            // Pre-process the request, which will throw a BlacklistedException if the request should be denied.
            preProcessor.preProcessRequest(context);

            doBeacon(context, request, response, adSpaceExternalID, impressionExternalID, rtbSettlementPrice);

        } catch (InvalidIpAddressException iiap) {
            LOG.info("Beacon request IP is invalid: " + iiap.getMessage());
            backupLogger.logBeaconFailure(impressionExternalID, iiap.getMessage(), null);

        } catch (com.adfonic.adserver.BlacklistedException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Dropping blacklisted request (impressionExternalID=" + impressionExternalID + ") due to " + e.getMessage());
            }
            backupLogger.logBeaconFailure(impressionExternalID, "blacklisted", null, e.getMessage());
            return;
        } catch (Exception x) {
            LOG.info("Failed to create targering context. AdSpace: " + adSpaceExternalID + ", Impression: " + impressionExternalID, x);
            backupLogger.logBeaconFailure(impressionExternalID, "exception", null, x.getClass().getName(), x.getMessage());
            return;
        }

    }

    /**
     * This is only tiny extension that smuggles exchange name into beacon url so it can be easily find in access log.
     * Otherwise it works exactly same as original beacon handler
     */
    @RequestMapping(Constant.BEACON_URI_PATH + "/{ExchangeID}/{adSpaceExternalID}/{impressionExternalID}.gif")
    public void handleBeaconExchange(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @PathVariable String adSpaceExternalID,
            @PathVariable String impressionExternalID, @RequestParam(value = Constant.SP_URL_PARAM, required = false) String settlementPrice) throws java.io.IOException {
        // Just call original handler
        handleBeacon(httpRequest, httpResponse, adSpaceExternalID, impressionExternalID, settlementPrice);
    }

    private void doBeacon(TargetingContext context, HttpServletRequest request, HttpServletResponse response, String adSpaceExternalID, String impressionExternalID,
            String rtbSettlementPrice) throws IOException {

        AdserverDomainCache adCache = context.getAdserverDomainCache();
        // Be aware that AdSpace might be null - ExternalID may be broken or AdSpace is no longer in cache
        AdSpaceDto adSpace = adCache.getAdSpaceByExternalID(adSpaceExternalID);

        Impression impression = impressionService.getImpression(impressionExternalID);
        if (impression == null) {
            LOG.warn("Impression not found: " + impressionExternalID + ", AdSpace: " + adSpaceExternalID);
            if (adSpace != null) {
                astats.increment(adSpace, AsCounter.BeaconImpressionNotFound);
            } else {
                astats.increment(RtbExchange.Unknown, AsCounter.BeaconImpressionNotFound);
            }

            backupLogger.logBeaconFailure(impressionExternalID, "Impression not found", null);
            return; // Cannot continue
        }

        astats.beacon(impression);

        if (adSpace != null) {
            // Usual path
            if (!adSpace.getId().equals(impression.getAdSpaceId())) {
                // If we have AdSpace from both parameter and impression, verify that they are same. Bit of paranoia...
                LOG.warn("AdSpace parameter mismatch: " + adSpaceExternalID + "/" + adSpace.getId() + " vs Impression stored AdSpaceId: " + impression.getAdSpaceId());
                astats.increment(adSpace, AsCounter.BeaconAdSpaceMismatch);
                backupLogger.logBeaconFailure(impression, "AdSpace mismatch", context, String.valueOf(impression.getAdSpaceId()), String.valueOf(adSpace.getId()));
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
                astats.increment(AsCounter.BeaconAdSpaceNotFound);
                backupLogger.logBeaconFailure(impression, "no AdSpace", context, adSpaceExternalID, String.valueOf(impression.getAdSpaceId()));
                return;
            }
        }

        context.setAdSpace(adSpace);

        // Track and disallow repeated beacon invocations
        if (impressionService.trackBeacon(impression) == false) {
            LOG.info("Repeated tracking request for AdSpace: " + adSpaceExternalID + ", Creative: " + impression.getCreativeId() + ", Impression:" + impressionExternalID);
            astats.increment(adSpace, AsCounter.BeaconRepeated);
            backupLogger.logBeaconFailure(impression, "Duplicate", null);
            return;
        }

        // Make sure the creative exists
        CreativeDto creative = adCache.getCreativeById(impression.getCreativeId());
        if (creative == null) {
            creative = adCache.getRecentlyStoppedCreativeById(impression.getCreativeId());
            if (creative == null) {
                LOG.warn("Creative not found: " + impression.getCreativeId() + ", AdSpace: " + adSpaceExternalID + ", Impression:" + impressionExternalID);
                astats.increment(adSpace, AsCounter.BeaconCreativeNotFound);
                backupLogger.logBeaconFailure(impression, "Creative not found", context, String.valueOf(impression.getCreativeId()));
                return;
            }
        }

        // Make sure the IntegrationTypeDto from the impression actually uses beacons
        IntegrationTypeDto integrationType = context.getDomainCache().getIntegrationTypeById(impression.getIntegrationTypeId());
        if (!BeaconUtils.shouldUseBeacons(integrationType)) {
            String integrationTypeSystemName = integrationType == null ? "null" : integrationType.getSystemName();
            LOG.warn("Impression's IntegrationType (" + integrationTypeSystemName + ") doesn't use beacons");
            backupLogger.logBeaconFailure(impression, "IntegrationType", context, integrationTypeSystemName);
            return;
        }

        // Before we go any further, make sure the beacon request isn't coming from some device/country other than the one from which the impression came.  
        // Wes bumped into some fraud issues where impressions from South Africa were then followed up with clicks from India, and from what sounded like desktop browsers to boot.
        ModelDto model = context.getAttribute(TargetingContext.MODEL);
        if (impression.getModelId() != null && (model == null || !model.getId().equals(impression.getModelId()))) {
            // The beacon is coming from a different model than the impression
            String modelId = model == null ? "null" : String.valueOf(model.getId());
            LOG.warn("Beacon detected with a Model mismatch! adSpace=" + impression.getAdSpaceId() + ", bid.model=" + impression.getModelId() + ", beacon.model=" + modelId
                    + ", beacon.ua=" + context.getEffectiveUserAgent() + ", beacon.ip=" + context.getAttribute(Parameters.IP) + ", impression=" + impressionExternalID);
            backupLogger.logBeaconFailure(impression, "Model mismatch", context, String.valueOf(impression.getModelId()), modelId);
            astats.increment(adSpace, AsCounter.BeaconDeviceModelMismatch);
            return;
        }

        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (impression.getCountryId() != null && (country == null || (!country.getId().equals(impression.getCountryId())))) {
            // The beacon request is coming from a different country than the impression
            String countryId = country == null ? "null" : String.valueOf(country.getId());
            LOG.warn("Beacon detected with a Country mismatch! adSpace=" + impression.getAdSpaceId() + ", bid.country=" + impression.getCountryId() + ", beacon.country="
                    + countryId + ", beacon.ip=" + context.getAttribute(Parameters.IP) + ", impression=" + impressionExternalID);
            backupLogger.logBeaconFailure(impression, "Country mismatch", context, String.valueOf(impression.getCountryId()), countryId);
            astats.increment(adSpace, AsCounter.BeaconCountryMismatch);
            return;
        }

        checkAndReportRtbWin(rtbSettlementPrice, context, impression, adSpace, creative);

        // Query Cassandra and call adsquare api if necessary - later this should be in same query as for impression cache
        if (adsquareWorker.isCountryWhitelisted(country)) {
            adsquareWorker.reportImpression(impressionExternalID);
        }

        // Log the impression event
        //TODO Clean up code when decomissioning JMS
        //        AdEvent event = getAdEventFactory().newInstance(AdAction.IMPRESSION);
        //        context.populateAdEvent(event, impression, creative);
        //        getAdEventLogger().logAdEvent(event, context);
        backupLogger.logBeaconSuccess(impression, new Date(), context);

        astats.beaconCompleted(adSpace, creative);
    }

    private void checkAndReportRtbWin(String rtbSettlementPrice, TargetingContext context, Impression impression, AdSpaceDto adSpace, CreativeDto creative) {
        boolean isContextRtbEnabled = RtbBidLogicImpl.isRtbEnabled(context);
        // If RTB enabled server and RtbConfig Win Type is beacon, then send the AD_SERVED event
        if (isContextRtbEnabled) {
            PublisherDto publisher = adSpace.getPublication().getPublisher();
            RtbConfigDto rtbConfig = publisher.getRtbConfig();
            // Use "special" RtbConfigDto for Rubicon video - http://kb.rubiconproject.com/index.php/RTB/OpenRTB#Use_of_adm_vs_nurl
            // Normally Rubicon uses "markup on bid" and "beacon win notification" but only for video (VAST) Rubicon hacked it into "markup on rtb win notification"
            if (RtbExchange.Rubicon == RtbExchange.getByPublisherId(publisher.getId()) && creative.getExtendedData().get("duration") != null) {
                rtbConfig = new RubiconVastRtbConfig(rtbConfig);
            }
            if (rtbConfig != null && rtbConfig.getWinNoticeMode() == RtbWinNoticeMode.BEACON) {
                rtbWinLogic.winOnImpression(rtbSettlementPrice, context, impression, creative, rtbConfig);
            }
        }
    }
}

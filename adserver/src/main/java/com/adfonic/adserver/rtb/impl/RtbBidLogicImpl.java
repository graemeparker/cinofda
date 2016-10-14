package com.adfonic.adserver.rtb.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.adresponse.AdMarkupRenderer;
import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdServerFeatureFlag;
import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.DeviceIdentifierLogic;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.InvalidTrackingIdentifierException;
import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.ReservePot;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.adserver.controller.dbg.DebugBidContext;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.controller.rtb.RtbHttpContext;
import com.adfonic.adserver.impl.AdsquareTargetingChecks.AdsquareEnrichCreatives;
import com.adfonic.adserver.impl.CreativeBidDeal;
import com.adfonic.adserver.monitor.AdserverMonitor;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidDetails;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.RtbBidManager;
import com.adfonic.adserver.rtb.RtbIdService;
import com.adfonic.adserver.rtb.impl.RtbWinLogicImpl.RubiconVastRtbConfig;
import com.adfonic.adserver.rtb.nativ.AdObject;
import com.adfonic.adserver.rtb.nativ.AdType;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.adserver.rtb.nativ.IntegrationTypeLookup;
import com.adfonic.adserver.rtb.open.v2.VideoV2;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.RtbConfig.RtbAdMode;
import com.adfonic.domain.RtbConfig.RtbWinNoticeMode;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.jms.JmsUtils;
import com.byyd.adsquare.v2.AdsqrEnrichQueryRequest;
import com.byyd.ortb.CreativeAttribute;

@org.springframework.stereotype.Component
public class RtbBidLogicImpl implements RtbBidLogic {
    private static final transient Logger LOG = Logger.getLogger(RtbBidLogicImpl.class.getName());

    // We pre-cache these arrays, since we need to index into them numerically
    // for btype and battr.  I wasn't 100% sure if calling .values() would
    // construct a new array every time, and I didn't want to take that chance.
    // We need to do range checking before dereferencing, so it meant 2 calls
    // to values per reference.  So yeah, let's use use static references.
    // Refactored into RtbEnumDeserializer

    @Autowired
    private AdserverMonitor adserverMonitor;

    // These sets locally cache "we already cried bloody murder about this unrecognized thing"
    // Refactored into RtbEnumDeserializer

    @Autowired
    private JmsUtils jmsUtils;
    @Autowired
    private RtbBidManager rtbBidManager;
    @Autowired
    private RtbIdService rtbIdService;
    @Autowired
    private AdEventFactory adEventFactory;
    @Autowired
    private BackupLogger backupLogger;
    @Autowired
    private DisplayTypeUtils displayTypeUtils;
    @Autowired
    private PreProcessor preProcessor;
    @Autowired
    private TargetingContextFactory targetingContextFactory;
    @Autowired
    private TargetingEngine targetingEngine;
    @Autowired
    private TrackingIdentifierLogic trackingIdentifierLogic;
    @Autowired
    private AdMarkupRenderer adMarkupRenderer;
    @Autowired
    private ImpressionService impressionService;
    @Autowired
    private AdServerStats counterManager;
    @Autowired
    private DynamicProperties dProperties;
    @Autowired
    private BidRateThrottler throttler;
    @Autowired
    private LocalBudgetManager budgetManager;
    @Autowired
    private ReservePot reservePot;
    @Autowired
    private AdsquareWorker adsquareWorker;
    @Autowired
    private AdServerStats statsManager;

    // statically assigned internal iab-id to adfonic not categorized. defined for rtb use
    public static final String ADFONIC_NOT_CATEGORIZED_CAT_IAB_ID = "ADF-066";

    public static final String MESSAGE_NO_PUBLICATION_FOUND = "Publication not found RtbId: ";

    @Override
    public ByydResponse bid(RtbExecutionContext<?, ?> rtbContext, RtbBidEventListener bidEventListener, TargetingEventListener bidTargetListener) throws NoBidException {
        RtbHttpContext httpContext = rtbContext.getHttpContext();
        ByydRequest byydRequest = rtbContext.getByydRequest();

        HttpServletRequest httpRequest = httpContext.getHttpRequest();

        TargetingContext tgtContext = targetingContextFactory.createTargetingContext();

        DebugBidContext debugBidContext = (DebugBidContext) httpRequest.getAttribute(TargetingContext.DEBUG_CONTEXT);
        // Bbid debuging can turn off throttling ...
        boolean doThrottling = true;
        if (debugBidContext != null) {
            doThrottling = !debugBidContext.isSkipThrottling();
            // Send DebugBidContext further into bidding and targeting...
            tgtContext.setAttribute(TargetingContext.DEBUG_CONTEXT, debugBidContext);
        }

        if (byydRequest.isTestMode()) {
            tgtContext.setFlagTrue(Parameters.TEST_MODE);
        }

        boolean trackNoBid = false;
        try {
            // Check nobid mode before publication lookup
            int loopIdx = doThrottling ? throttler.throttleRtbRate(byydRequest) : 0;

            AdserverDomainCache adCache = getRtbAdserverDomainCache(tgtContext, byydRequest, bidEventListener);

            // Look up the Publisher
            long publisherId = getEffectivePublisherId(tgtContext, adCache, byydRequest, bidEventListener);

            // Look up the AdSpace by Publisher and the Publication.rtbId
            AdSpaceDto adSpace = getAdspace(adCache, publisherId, tgtContext, byydRequest, bidEventListener);

            long timeout = byydRequest.getTmax() != null ? byydRequest.getTmax().longValue() : adSpace.getPublication().getEffectiveAdRequestTimeout();
            TimeLimit timeLimit = new TimeLimit(rtbContext.getExecutionStartedAt(), timeout);

            byydRequest.setAdSpace(adSpace);
            tgtContext.setAdSpace(adSpace);

            if (doThrottling) {
                // Publication sampling
                throttler.throttlePublicationRate(loopIdx, byydRequest, adSpace);
            }

            RtbConfigDto rtbConfig = adSpace.getPublication().getPublisher().getRtbConfig();

            if (byydRequest.getCurrencies() != null && !byydRequest.getCurrencies().contains(rtbConfig.getBidCurrency())) {
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, adSpace, "Unsupported currency", byydRequest.getCurrencies());
            }

            // We require device data in order to determine IP and User-Agent
            checkAndSetDeviceTargetingContext(tgtContext, byydRequest, bidEventListener);

            setUserTargetingContext(tgtContext, byydRequest);

            setDeviceIdentifierTargetingContext(tgtContext, byydRequest, bidEventListener);

            // Grab any restrictions
            setRestrictionsInContext(byydRequest, tgtContext);

            if (byydRequest.arePluginsBlocked()) {
                tgtContext.setAttribute(TargetingContext.BLOCK_PLUGINS, TargetingContext.SET_FLAG);
            }

            tgtContext.setFlagFalse(TargetingContext.CREATIVE_AUDIT);
            tgtContext.setAttribute(TargetingContext.BYYD_REQUEST, byydRequest);
            tgtContext.setAttribute(TargetingContext.RTB_PMP, byydRequest.getMarketPlace());

            trackNoBid = true; // Since now, when NoBidException happens it will be reported

            /**
             * For Adsqaure, to allow them to make forecasts based on real traffic, 
             * we query their Enrich API with every bid request from whitelisted country
             */
            CountryDto country = tgtContext.getAttribute(TargetingContext.COUNTRY);
            if (adsquareWorker.isCountryWhitelisted(country)) {
                // Invokes and make sure that result is cached in context so it can be reused if there is some capmaign targeting Adsquare audience 
                tgtContext.getAttribute(TargetingContext.ADSQUARE_ENRICH_AUDIENCES);
            }

            ByydBid byydBid = bidForImpression(rtbContext, byydRequest, adSpace, tgtContext, httpRequest, bidEventListener, bidTargetListener, httpContext.getWinUrlPath(),
                    rtbConfig, timeLimit);

            if (byydBid == null) {
                UnfilledReason reason = tgtContext.getAttribute(TargetingContext.UNFILLED_REASON);
                throw NoBidException.build(byydRequest, adSpace, reason);
            } else {
                ByydResponse byydResponse = new ByydResponse(byydRequest, byydBid);
                tgtContext.setAttribute(TargetingContext.BYYD_RESPONSE, byydResponse);

                // The defaults are USD and CPM, so don't bloat the response with defaults
                if (!Constant.USD.equals(rtbConfig.getBidCurrency())) {
                    byydResponse.setBidCurrencyIso4217(rtbConfig.getBidCurrency());
                }
                // copy context -> byyd 
                byydResponse.setImpTrackUrls(tgtContext.getAttribute(TargetingContext.IMP_TRACK_LIST));
                byydBid.setNativeAdResponse(tgtContext.getAttribute(TargetingContext.NATIVE_RESPONSE));

                return byydResponse;
            }
        } catch (NoBidException ex) {
            if (bidEventListener != null && trackNoBid) {
                bidEventListener.bidNotMade(tgtContext, byydRequest, null, ex.getMessage());
            }
            throw ex;
        }
    }

    public static boolean isRtbEnabled(TargetingContext context) {
        return context.getAdserverDomainCache().isRtbEnabled();
    }

    /**
     * get the RtbAdServerDomainCahce, if its not Rtb enabled server then it will throw NoBidException
     */
    AdserverDomainCache getRtbAdserverDomainCache(TargetingContext context, ByydRequest byydRequest, RtbBidEventListener listener) throws NoBidException {
        // The only time we should ever get RTB requests is on an RTB-enabled
        // deployment of adserver.  Make sure our AdserverDomainCache is actually
        // an instance of RtbAdserverDomainCache, since we'll be casting it below.
        if (!isRtbEnabled(context)) {
            if (listener != null) {
                listener.bidRequestRejected(context, byydRequest, "AdCache is not RTB enabled");
            }
            throw new NoBidException(byydRequest, NoBidReason.TECHNICAL_ERROR, AdSrvCounter.NON_RTB_CACHE);
        }
        return context.getAdserverDomainCache();
    }

    /**
     * Lookup Adspace by publicationRtbId/fallbackRtbId 
     */
    private AdSpaceDto getAdspace(AdserverDomainCache adCache, long publisherId, TargetingContext context, ByydRequest byydRequest, RtbBidEventListener listener)
            throws NoBidException {

        String publicationRtbId = byydRequest.getPublicationRtbId();

        AdSpaceDto adSpace = adCache.getAdSpaceByPublicationRtbId(publisherId, publicationRtbId);
        if (adSpace == null) {
            if (byydRequest.getFallbackPublicationRtbId() != null) {
                adSpace = adCache.getAdSpaceByPublicationRtbId(publisherId, byydRequest.getFallbackPublicationRtbId());
                if (adSpace != null) {
                    return adSpace;
                }
            }

            // The AdSpace doesn't exist in AdserverDomainCache, but there are
            // many reasons why that may be the case.  It doesn't necessarily
            // mean the publication hasn't been persisted yet.  Let the RTB_ID
            // service figure out what to do with it.
            rtbIdService.handleUnrecognizedRtbId(byydRequest, publisherId, publicationRtbId);

            // For new Publications/AdSpaces, we can't serve anything until the domain relaods
            // anyway, so there's no point in going any further.
            String message = MESSAGE_NO_PUBLICATION_FOUND + publicationRtbId;
            if (listener != null) {
                listener.bidRequestRejected(context, byydRequest, message);
            }

            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AdSrvCounter.UNKNOWN_PUBLICATION, publicationRtbId);
        } else {
            // checking bundle association for APPs Publications, if it is not present we need to create it
            if ((Medium.APPLICATION.equals(byydRequest.getMedium())) && (StringUtils.isEmpty(adSpace.getPublication().getBundleName()))) {
                rtbIdService.handleBundleAssociation(byydRequest, adSpace.getPublication().getId());
            }
        }
        return adSpace;
    }

    /**
     * Check Request Device
     * We require device data in order to determine IP and User-Agent 
     * If no device info found then throw NoBidException
     */
    private ByydDevice checkAndSetDeviceTargetingContext(TargetingContext context, ByydRequest byydRequest, RtbBidEventListener bidListener) throws NoBidException {
        ByydDevice device = byydRequest.getDevice();
        String errorMesage = null;
        if (device == null) {
            errorMesage = "Missing device object in bid";
        } else if (StringUtils.isBlank(device.getIp())) {
            errorMesage = "Missing device.ip in bid";
        } else if (StringUtils.isBlank(device.getUserAgent())) {
            // We could possibly fall back on make/model, but for now let's
            // just always enforce that a User-Agent be passed to us.
            errorMesage = "Missing device.ua in bid";
        }

        if (errorMesage != null) {
            if (bidListener != null) {
                bidListener.bidRequestRejected(context, byydRequest, errorMesage);
            }
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, context.getAdSpace(), errorMesage);
        }

        // Set the effective User-Agent
        String effectiveUserAgent = preProcessor.getModifiedUserAgent(device.getUserAgent());
        context.setUserAgent(effectiveUserAgent);
        context.setAttribute(Parameters.HTTP_HEADER_PREFIX + "user-agent", effectiveUserAgent);

        // Bail on requests from robot/checker/spam
        // NOTE: this call must not occur prior to setting the effective User-Agent
        // in the targeting context!!!
        checkIfRequestFromSpamCheckersOrBots(context, byydRequest, bidListener);

        // Set the end user's IP address on the TargetingContext
        setDeviceIpOncontext(context, device.getIp(), byydRequest, bidListener);

        // Now that we've set the IP and User-Agent on the context, check for blacklisted stuff
        checkBlockedIpsSubnetsUserAgents(context, byydRequest, bidListener);

        // Set the device attributes (i.e. coordinates) when specified
        setDeviceAttributesOnContext(context, device);

        setDeviceIdentifiersInContext(context, byydRequest);
        return device;
    }

    /**
     * Check if request came from spam, checkers or bots
     * @param context
     * @throws NoBidException
     */
    private void checkIfRequestFromSpamCheckersOrBots(TargetingContext context, ByydRequest bidRequest, RtbBidEventListener listener) throws NoBidException {
        // Bail on requests from robot/checker/spam
        Boolean isEvil = context.getAttribute(TargetingContext.DEVICE_IS_ROBOT_CHECKER_OR_SPAM, Boolean.class);
        if (isEvil != null && isEvil.booleanValue()) {
            if (listener != null) {
                listener.bidRequestRejected(context, bidRequest, "Device isRobot|isChecker|isSpam");
            }
            throw new NoBidException(bidRequest, NoBidReason.REQUEST_INVALID, context.getAdSpace(), "Device isRobot|isChecker|isSpam");
        }
    }

    /**
     * Check for blocked IPs, subnets and user agents
     */
    private void checkBlockedIpsSubnetsUserAgents(TargetingContext context, ByydRequest byydRequest, RtbBidEventListener listener) throws NoBidException {
        try {
            preProcessor.preProcessRequest(context);
        } catch (com.adfonic.adserver.BlacklistedException e) {
            LOG.warning("Dropping blacklisted request (publisherExternalID=" + byydRequest.getPublisherExternalId() + ") due to " + e.getMessage() + ". BidId: "
                    + byydRequest.getId() + ", AdSpace: " + context.getAdSpace().getId());
            if (listener != null) {
                listener.bidRequestRejected(context, byydRequest, e.getMessage());
            }
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, context.getAdSpace(), " IP/Subnet/UserAgent blacklisted", e.getMessage());
        }
    }

    /**
     * Set device ip in context
     */
    private void setDeviceIpOncontext(TargetingContext context, String deviceIp, ByydRequest bidRequest, RtbBidEventListener listener) throws NoBidException {
        try {
            context.setIpAddress(deviceIp);
        } catch (InvalidIpAddressException e) {
            // We're specifically logging this exception this way so it
            // shows up in the logs but won't trip up the error scavenger.
            LOG.warning(e.getMessage() + ". BidId: " + bidRequest.getId() + ", AdSpace: " + context.getAdSpace().getId());
            if (listener != null) {
                listener.bidRequestRejected(context, bidRequest, e.getMessage());
            }
            throw new NoBidException(bidRequest, NoBidReason.REQUEST_DROPPED, context.getAdSpace(), "Invalid IP", deviceIp);
        }
    }

    /**
     * Set device location in context
     * 
     * Making function as package visible so that it can be tested from unit test cases
     */
    static void setDeviceAttributesOnContext(TargetingContext context, ByydDevice device) {
        if (device.getCoordinates() != null) {
            context.setAttribute(Parameters.DEVICE_LATITUDE, String.valueOf(device.getCoordinates().getLatitude()));
            context.setAttribute(Parameters.DEVICE_LONGITUDE, String.valueOf(device.getCoordinates().getLongitude()));
        }

        if (StringUtils.isNotEmpty(device.getNetworkType())) {
            context.setAttribute(Parameters.NETWORK_TYPE, device.getNetworkType());
        }

        if (StringUtils.isNotEmpty(device.getMccMnc())) {
            context.setAttribute(Parameters.MCC_MNC, device.getMccMnc());
        }
    }

    /**
     * This function will set all available User info into context
     */
    static void setUserTargetingContext(TargetingContext context, ByydRequest byydRequest) {
        ByydUser user = byydRequest.getUser();
        // user is completely optional in bid request
        if (user != null) {
            // If we were passed a "uid" then use that as the tracking identifier
            String uid = user.getUid();
            if (StringUtils.isNotBlank(uid)) {
                // Make sure it gets lowercased
                context.setAttribute(Parameters.TRACKING_ID, String.valueOf(uid).toLowerCase());
            }

            if (user.getDateOfBirth() != null) {
                context.setAttribute(Parameters.DATE_OF_BIRTH, user.getDateOfBirth());
            }

            if (user.getAge() != null) {
                context.setAttribute(Parameters.AGE, String.valueOf(user.getAge()));
            }

            if (user.getAgeRange() != null) {
                context.setAttribute(Parameters.AGE_LOW, String.valueOf(user.getAgeRange().getStart()));
                context.setAttribute(Parameters.AGE_HIGH, String.valueOf(user.getAgeRange().getEnd()));
            }

            String userGender = user.getGender();
            if (userGender != null) {
                context.setAttribute(Parameters.GENDER, userGender);
            }

            // Add the user keywords to our running set of tags, if specified
            //moved up, to make sure tags will be created only in one function

            String postalCode = user.getPostalCode();
            if (postalCode != null) {
                context.setAttribute(Parameters.POSTAL_CODE, postalCode);
            }

            String countryCode = user.getCountryCode();
            if (countryCode != null) {
                context.setAttribute(Parameters.COUNTRY_CODE, countryCode);
            }

            // Use the provided state when specified (Nexage specific)
            String state = user.getState();
            if (state != null) {
                context.setAttribute(Parameters.STATE, state);
            }

            // Use the provided DMA when specified (Nexage specific)
            String dma = user.getDma();
            if (dma != null) {
                context.setAttribute(Parameters.DMA, dma);
            }
        }
    }

    /**
     * This function will make sure that context has tracking indentifier info else it will thrown
     * NoBidException, which means an empty result will be sent back.
     * @param context
     * @throws NoBidException
     */
    void setDeviceIdentifierTargetingContext(TargetingContext context, ByydRequest byydRequest, RtbBidEventListener listener) throws NoBidException {
        try {
            // Make sure tracking identifier stuff is set up on the targeting context.
            // We don't need to pass the response into this call, since we're saying
            // cookiesAllowed=false.  We never deal with cookies directly for RTB.
            trackingIdentifierLogic.establishTrackingIdentifier(context, null, false);
        } catch (InvalidTrackingIdentifierException e) {
            // Log just the message so it won't trip up scavenger
            LOG.warning(e.getMessage() + ". BidId: " + byydRequest.getId() + ", AdSpace: " + context.getAdSpace().getId());
            if (listener != null) {
                listener.bidRequestRejected(context, byydRequest, e.getMessage());
            }
            throw new NoBidException(byydRequest, NoBidReason.TECHNICAL_ERROR, AdSrvCounter.TRACKING_ID_INVALID, String.valueOf(e));
        }
    }

    /**
     * This function will make sure that context has device indentifiers if any
     * have been provided.  This method is an alternative to using the usual
     * DeviceIdentifiersDeriver.  Since RTB doesn't deal with HTTP request
     * parameters per se, we handle this in our RTB-specific way, but we need
     * to make sure the device identifier map in the context is set up the same
     * as it would have been had this been a regular ad request.
     */
    private void setDeviceIdentifiersInContext(TargetingContext context, ByydRequest byydRequest) {

        if (byydRequest.isTrackingDisabled()) {
            context.setFlagTrue(TargetingContext.TRACKING_DISABLED);
            // Simply do not pass DIDs from bid request into context if tracking is disabled 
            context.setAttribute(TargetingContext.DEVICE_IDENTIFIERS, Collections.EMPTY_MAP);
            return;
        }

        Map<Long, String> didValuesByTypeId = null; // lazily constructed only if needed

        Map<String, String> deviceIdentifiers = byydRequest.getDevice().getDeviceIdentifiers();
        for (Map.Entry<String, String> entry : deviceIdentifiers.entrySet()) {
            final String deviceIdName = entry.getKey();
            String deviceIdValue = entry.getValue();
            if (StringUtils.isNotBlank(deviceIdValue)) {
                DeviceIdentifierTypeDto didType = context.getDomainCache().getDeviceIdentifierTypeBySystemName(deviceIdName);
                if (didType == null) {
                    LOG.log(Level.WARNING, "Unsupported  " + deviceIdName + " of device id:" + deviceIdValue + "  Adspace: " + context.getAdSpace().getId());
                    continue;
                }

                if (DeviceIdentifierType.SYSTEM_NAME_IFA.equals(didType.getSystemName())) {
                    deviceIdValue = deviceIdValue.toUpperCase(); // IDFA should be uppercase
                } else if (DeviceIdentifierType.SYSTEM_NAME_ADID.equals(didType.getSystemName())) {
                    deviceIdValue = deviceIdValue.toLowerCase(); // ADID should be lowercase
                }

                // Make sure it conforms to the validation regex if there is one
                Pattern pattern = didType.getValidationPattern();
                if (pattern != null && !pattern.matcher(deviceIdValue).matches()) {
                    // counter here ?
                    LOG.warning("Device Id (" + deviceIdValue + ") doesn't match the validation pattern for " + deviceIdName + ". Adspace: " + context.getAdSpace().getId());
                    continue;
                }

                // It's an acceptable value, so save it in a map
                if (didValuesByTypeId == null) {
                    didValuesByTypeId = new LinkedHashMap<Long, String>();
                }
                didValuesByTypeId.put(didType.getId(), deviceIdValue);
            }
        }

        if (didValuesByTypeId != null) {
            // Compute hashes of raw ids as needed
            Map<String, Long> didTypesIdsByName = context.getDomainCache().getDeviceIdentifierTypeIdsBySystemName();
            DeviceIdentifierLogic.promoteIFA(didValuesByTypeId, didTypesIdsByName);
            DeviceIdentifierLogic.promoteADID(didValuesByTypeId, didTypesIdsByName);

            // AF-1467 - Prune any blacklisted device identifiers
            DeviceIdentifierLogic.enforceBlacklist(didValuesByTypeId, context.getDomainCache());
        }

        // Set the map in the context for subsequent access
        context.setAttribute(TargetingContext.DEVICE_IDENTIFIERS, didValuesByTypeId != null ? didValuesByTypeId : Collections.EMPTY_MAP);
    }

    /**
     * If any restrictions are defined and this function will set those into context.
     * @param rtbAdserverDomainCache
     * @param br
     * @param context
     */
    static void setRestrictionsInContext(ByydRequest br, TargetingContext context) {
        // Grab any restrictions

        // Factor in any blocked categories
        if (CollectionUtils.isNotEmpty(br.getBlockedCategoryIabIds())) {
            Set<Long> blockedCategoryIds = new LinkedHashSet<Long>();
            for (String iabId : br.getBlockedCategoryIabIds()) {
                // Look up the category by IAB id
                Long categoryId = getCategoryIdByIabId(iabId, context);
                if (categoryId != null) {
                    // Not only add the blocked category, but add all of its children
                    blockedCategoryIds.addAll(context.getDomainCache().getExpandedCategoryIds(categoryId));
                }
            }
            if (!blockedCategoryIds.isEmpty()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Blocked Category ids: " + blockedCategoryIds);
                }
                // Set this on the context for use at targeting time
                context.setAttribute(TargetingContext.BLOCKED_CATEGORY_IDS, blockedCategoryIds);
            }
        }

        // Factor in any blocked advertiser domains
        if (CollectionUtils.isNotEmpty(br.getBlockedAdvertiserDomains())) {
            Set<String> blockedAdvertiserDomains = new HashSet<String>();
            for (String blockedDomain : br.getBlockedAdvertiserDomains()) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Blocking advertiser domain: " + blockedDomain);
                }
                blockedAdvertiserDomains.add(blockedDomain);
            }
            // Set this on the context for use at targeting time
            context.setAttribute(TargetingContext.BLOCKED_ADVERTISER_DOMAINS, blockedAdvertiserDomains);
        }

        // Factor in blocked languages
        if (CollectionUtils.isNotEmpty(br.getBlockedLanguageIsoCodes())) {
            Set<String> blockedLanguageIsoCodes = new HashSet<String>(br.getBlockedLanguageIsoCodes());
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Blocking language(s): " + blockedLanguageIsoCodes);
            }
            context.setAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, blockedLanguageIsoCodes);
        }

        // Factor in accepted languages
        if (CollectionUtils.isNotEmpty(br.getAcceptedLanguageIsoCodes())) {
            context.setAttribute(Parameters.LANGUAGE, StringUtils.join(br.getAcceptedLanguageIsoCodes(), ","));
        }
    }

    /**
     * This function will create bid for given impression and all bids will be put into incoming 
     * list parameter bidList 
     */
    ByydBid bidForImpression(RtbExecutionContext<?, ?> rtbContext, ByydRequest byydRequest, final AdSpaceDto adSpace, TargetingContext context, HttpServletRequest httpRequest,
            RtbBidEventListener bidListener, TargetingEventListener targetListener, String winUrlPath, RtbConfigDto rtbConfig, TimeLimit timeLimit) throws NoBidException {

        ByydImp byydImp = byydRequest.getImp();
        context.setAttribute(TargetingContext.BYYD_IMP, byydImp);
        context.setAttribute(TargetingContext.IS_NATIVE, byydImp.getAdObject() == AdObject.NATIVE);
        context.setAttribute(TargetingContext.NATIVE_REQUEST, byydImp.getNativeAdRequest());

        // Detect any blocked AdTypes
        Set<AdType> blockedAdTypes = byydImp.getBtype();
        // Detect any blocked CreativeAttributes
        Set<Integer> blockedCreativeAttributes = byydImp.getBattr();

        // See if we need to block either text or banner
        boolean textBlocked = false;
        if ((blockedAdTypes != null && blockedAdTypes.contains(AdType.XHTML_TEXT_AD))
                || (blockedCreativeAttributes != null && blockedCreativeAttributes.contains(CreativeAttribute.TEXT_ONLY.ordinal()))) {
            textBlocked = true;
        }

        context.setAttribute(TargetingContext.BLOCKED_CREATIVE_ATTRIBUTES, blockedCreativeAttributes);

        boolean bannerBlocked = blockedAdTypes != null && blockedAdTypes.contains(AdType.XHTML_BANNER_AD);

        // text and banner xhtml is all we support now. So no real point going if neither is allowed
        //  - checks inherited from existing code
        if (textBlocked && bannerBlocked) {
            // We need to fast fail.  Banners and text are blocked...
            String message = "Banners and text ads are blocked. Nothing to bid";
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(message + ", AdSpace: " + adSpace.getId() + ", Impid: " + byydImp.getImpid());
            }
            if (bidListener != null) {
                bidListener.bidNotMade(context, byydRequest, byydImp, message);
            }
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, adSpace, message);
        }

        Set<Long> formatIds;

        try {
            // If width & height were specified, try to determine any applicable Formats
            formatIds = deriveFormatsFromWidthAndHeight(byydImp, adSpace, textBlocked, bannerBlocked, context, true);
            if (formatIds == null) {
                // Try to derive Format from AdSapce
                formatIds = deriveAllowedFormatIds(adSpace, textBlocked, bannerBlocked, context);
            }

        } catch (FormatRelatedException fx) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(fx.getMessage() + ", AdSpace: " + adSpace.getId() + ", Impid: " + byydImp.getImpid());
            }
            if (bidListener != null) {
                bidListener.bidNotMade(context, byydRequest, byydImp, fx);
            }
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AdSrvCounter.FORMAT_INVALID, fx.getMessage());
        }

        Set<Integer> videoProtocols = byydImp.getVideoProtocols();
        if (videoProtocols == null) {
            // If not sent in bid request default to VAST2 InLine
            context.setAttribute(TargetingContext.VIDEO_PROTOCOL, VideoV2.VideoProtocol.VAST_2_0_CODE);
        } else {
            /**
             * Trim set of allowed protocols into one that is supported and preffered (VAST2 InLine over Wrapper)
             */
            if (videoProtocols.contains(VideoV2.VideoProtocol.VAST_2_0_CODE)) {
                context.setAttribute(TargetingContext.VIDEO_PROTOCOL, VideoV2.VideoProtocol.VAST_2_0_CODE);
            } else if (videoProtocols.contains(VideoV2.VideoProtocol.VAST_2_0_WRAPPER_CODE)) {
                context.setAttribute(TargetingContext.VIDEO_PROTOCOL, VideoV2.VideoProtocol.VAST_2_0_WRAPPER_CODE);
            } else {
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AdSrvCounter.FORMAT_INVALID, "No supported VAST protocol " + videoProtocols);
            }
        }

        // Generate the Impression object
        final Impression impression = new Impression();
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Generated Impression externalID=" + impression.getExternalID());
        }
        impression.setAdSpaceId(adSpace.getId());

        context.setAttribute(TargetingContext.MIME_TYPE_WHITELIST, byydImp.getMimeTypeWhiteList());

        if (!byydImp.bypassCFRestrictions()) {
            Set<ContentForm> contentFormWhiteList = byydImp.getContentFormWhiteList();
            if (contentFormWhiteList == null) {
                contentFormWhiteList = ByydImp.CF_MOBILE_WEB;
            }
            context.setAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET, contentFormWhiteList);
        }

        AdserverDomainCache adCache = context.getAdserverDomainCache();

        //SC-450 and BL-670
        String convImpTime = null; // Also serves as marker that we did currency conversion...
        BigDecimal bidFloor = byydImp.getBidfloor();
        if (bidFloor != null) {
            String floorCur = byydImp.getBidfloorcur();
            if (floorCur == null || floorCur.equals(Constant.USD)) {
                // USD
            } else if (floorCur.equals(rtbConfig.getBidCurrency())) {
                convImpTime = gmtTimeYMDHFormat.format(impression.getCreationTime());
                bidFloor = adCache.convertFromBidCurrencyToUsd(adSpace, convImpTime, bidFloor);
            } else {
                String msg = "Floor currency " + floorCur + " neither USD or as in config." + floorCur + ", AdSpace: " + adSpace.getId();
                LOG.warning(msg);
                if (bidListener != null) {
                    bidListener.bidNotMade(context, byydRequest, byydImp, msg);
                }
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, adSpace, msg);
            }

            if (adSpace.getPublication().isUseSoftFloor()) {
                double softBidMultiplier = context.getAdserverDomainCache().getSystemVariableDoubleValue("soft_floor_multiplier", 0.0);
                if (softBidMultiplier > 0.0) {
                    bidFloor = bidFloor.multiply(new BigDecimal(1 - softBidMultiplier));
                }
            }
        }

        context.setAttribute(TargetingContext.ECPM_FLOOR, bidFloor);

        context.setAttribute(TargetingContext.BLOCKED_BID_TYPES, byydImp.getbBidTypes());
        context.setAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES, byydImp.getbDestTypes());

        context.setSslRequired(byydImp.isSslRequired());

        if (byydImp.isBlockExtendedCreatives()) {
            context.setAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES, Boolean.TRUE);
        } else {
            String integrationTypePrefix = rtbConfig.getIntegrationTypePrefix();
            IntegrationTypeDto dynamicIntegrationType = null;
            if (integrationTypePrefix != null) {
                IntegrationTypeLookup integrationTypeDeriver = byydImp.getIntegrationTypeDeriver();
                if (integrationTypeDeriver != null) {
                    DomainCache domainCache = context.getDomainCache();
                    dynamicIntegrationType = integrationTypeDeriver.deriveBasedOnPrefix(integrationTypePrefix, domainCache);
                    context.setAttribute(TargetingContext.INTEGRATION_TYPE, dynamicIntegrationType);
                }
            }

            Set<String> blockedExtCreativeTyeSet = byydImp.getBlockedExtendedCreativeTypes();
            if (blockedExtCreativeTyeSet != null || (dynamicIntegrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE)) != null // derive if needed
                    && !(blockedExtCreativeTyeSet = dynamicIntegrationType.getBlockedExtendedCreativeTypes()).isEmpty()) {
                context.setAttribute(TargetingContext.BLOCKED_EXT_CRT_TYP_SET, blockedExtCreativeTyeSet);
            }
        }

        statsManager.increment(rtbContext.getPublisherExternalId(), AsCounter.BidCapable);

        CreativeDto creative = null;
        try {
            if (targetListener == null) {
                targetListener = adserverMonitor.getTargetingEventListener();
            }
            rtbContext.setTargetingStarted(adSpace);
            SelectedCreative selectedCreative = targetingEngine.selectCreative(adSpace, formatIds, context, false, byydImp.isStrictBannerSize(), timeLimit, targetListener);
            rtbContext.setTargetingCompleted(selectedCreative != null ? selectedCreative.getCreative() : null);

            // Populate Impression even for nobids -> unfilled AdEvents in finally section of this try !!!
            context.populateImpression(impression, selectedCreative);

            if (selectedCreative == null) {
                UnfilledReason unfilledReason = context.getAttribute(TargetingContext.UNFILLED_REASON);
                unfilledAdEvent(unfilledReason, impression, context);
                return null; // bail on this impression request
            }

            ByydBid byydBid = new ByydBid(byydImp);

            // Grab the selected creative
            creative = selectedCreative.getCreative();
            byydBid.setCreative(creative);

            CampaignDto campaign = creative.getCampaign();
            PlatformDto platform = context.getAttribute(TargetingContext.PLATFORM);
            CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
            long countryId = 0;
            if (country != null) {
                countryId = country.getId();
            }
            EcpmInfo ecpmInfo = new EcpmInfo();
            adCache.computeEcpmInfo(adSpace, creative, platform, countryId, bidFloor, ecpmInfo);
            BigDecimal bidPriceUSD = new BigDecimal(ecpmInfo.getBidPrice());

            BigDecimal price;
            if (convImpTime != null) {
                // Convert back to bid currency
                price = adCache.convertToBidCurrencyFromUsd(adSpace, convImpTime, bidPriceUSD);
            } else {
                price = bidPriceUSD;
            }

            // AF-1025 - don't bid with 0.00
            /*.....*/
            //  - replaced with AI-25 implementation 

            // No formatting required here, just add the numeric value to the JSON
            // response.  With fractional cent bids, the most we should see here
            // is about 4 decimal places...Nexage allows up to 8 decimal places
            byydBid.setPrice(price);

            BigDecimal maxBidThreshold = campaign.getMaxBidThreshold();
            boolean isVariableBid = maxBidThreshold != null && campaign.isBudgetManagerEnabled() && maxBidThreshold.compareTo(bidPriceUSD) > 0;
            if (isVariableBid) {
                BigDecimal priceBoost = reservePot.getPriceBoost(bidPriceUSD, campaign.getId(), maxBidThreshold);
                bidPriceUSD = bidPriceUSD.add(priceBoost);
                impression.setPriceBoost(priceBoost);
                LOG.info("campaignId " + campaign.getId() + " priceBoost " + priceBoost + " boostedPrice " + bidPriceUSD);
            }
            impression.setRtbBidPrice(bidPriceUSD);

            // AF-424 / AF-427 - Nexage RTB compliance
            byydBid.setAdomain(campaign.getAdvertiserDomain());

            // budget manager
            if (!budgetManager.verifyAndReserveBudget(impression.getExternalID(), creative.getCampaign(), impression.getRtbBidPrice())) {
                counterManager.increment(rtbContext.getPublisherExternalId(), "OutOfLocalBudget." + campaign.getId());

                if (isVariableBid) {
                    LOG.info("out-of-budget deposit id " + campaign.getId() + " priceBoost " + impression.getPriceBoost());
                    reservePot.deposit(campaign.getId(), impression.getPriceBoost());
                }
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, adSpace, "Out of Budget", "campaign: " + campaign.getId() + ", creative: " + creative.getId()
                        + ", price: " + impression.getRtbBidPrice());
            }

            counterManager.bid(adSpace, creative, bidPriceUSD);

            // Layer 4 attributes
            byydBid.setCid(campaign.getExternalID());
            byydBid.setCrid(creative.getExternalID());

            DomainCache domainCache = context.getDomainCache();

            byydBid.setIabId(domainCache.getIabIdByCategoryId(campaign.getCategoryId()));

            // Add CreativeAttributes
            Set<Integer> creativeAttributes = creative.getCreativeAttributes();
            Set<Integer> attrList = new HashSet<Integer>();

            FormatDto format = domainCache.getFormatById(creative.getFormatId());
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Selected Creative.id=" + creative.getId() + ", priority=" + creative.getPriority() + ", format=" + format.getSystemName());
            }

            if (format.getSystemName().equals(SystemName.FORMAT_TEXT)) {
                String assetBaseUrl = dProperties.getProperty(DcProperty.AssetBaseUrl);
                byydBid.setTxtIUrl(assetBaseUrl
                        + "/"
                        + creative.getAsset(format.getDisplayTypes().get(0).getId(), domainCache.getComponentByFormatAndSystemName(format, SystemName.COMPONENT_TEXT).getId())
                                .getExternalID() + "?size=xl");
                if (!creativeAttributes.contains(CreativeAttribute.TEXT_ONLY.ordinal())) {
                    attrList.add(CreativeAttribute.TEXT_ONLY.ordinal());
                }
            }

            if (!creativeAttributes.isEmpty()) {
                attrList.addAll(creativeAttributes);
            }

            if (!attrList.isEmpty()) {
                byydBid.setAttr(attrList);
            }

            // Derive the correct DisplayTypeDto for the format/device combo, falling back on first available
            DisplayTypeDto displayType = displayTypeUtils.getDisplayType(format, context, true);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Derived DisplayType: " + displayType.getSystemName());
            }

            // AF-424 / AF-427 - Nexage RTB compliance
            // If the creative has an "image" component, add the respective asset URL
            // as the "iurl" attribute on the bid response.
            ComponentDto imageComponent = domainCache.getComponentByFormatAndSystemName(format, SystemName.COMPONENT_IMAGE);
            if (imageComponent != null) {
                // AF-1325 - we need to do the XXL -> XL fallback here as well
                AssetDto asset = null;
                for (DisplayTypeDto displayTypeDto : displayTypeUtils.getAllDisplayTypes(format, context)) {
                    asset = creative.getAsset(displayTypeDto.getId(), imageComponent.getId());
                    if (asset != null) {
                        displayType = displayTypeDto;
                        break;
                    }
                }

                //try to fall back
                if (asset == null) {
                    asset = creative.getAsset(displayType.getId(), imageComponent.getId());
                    String message = "Creative: " + creative.getId() + " without Asset for image component: " + imageComponent.getId() + " and DisplayTypes: "
                            + displayTypeUtils.getAllDisplayTypes(format, context) + " with fallback to DisplayType: " + displayType.getId() + "-" + displayType.getSystemName();

                    if (asset == null) {
                        if (bidListener != null) {
                            bidListener.bidNotMade(context, byydRequest, byydImp, message);
                        }
                        throw new NoBidException(byydRequest, NoBidReason.TECHNICAL_ERROR, adSpace, message);
                    }
                }

                String assetExternalId = asset.getExternalID();
                String assetBaseUrl = dProperties.getProperty(DcProperty.AssetBaseUrl);
                byydBid.setIurl(assetBaseUrl + "/" + assetExternalId);
                byydBid.setAssetId(assetExternalId);
                byydBid.setPublisherCreativeId(creative.getExternalCreativeReferenceByPublisherId(adSpace.getPublication().getPublisher().getOperatingPublisherId()));
            }

            if (byydImp.getAdObject() == AdObject.VIDEO) {
                byydBid.setDuration(Integer.parseInt(creative.getExtendedData().get("duration")));

                // Use "special" RtbConfigDto for Rubicon video - http://kb.rubiconproject.com/index.php/RTB/OpenRTB#Use_of_adm_vs_nurl
                // Normally Rubicon uses "markup on bid" and "beacon win notification" but only for video (VAST) Rubicon hacked it into "markup on rtb win notification"
                if (adSpace.getPublication().getPublisher().getId().equals(RtbExchange.Rubicon.getPublisherId())) {
                    rtbConfig = new RubiconVastRtbConfig(rtbConfig);
                }
            }

            Map<Long, String> factualCreatives = context.getAttribute(TargetingContext.FACTUAL_CREATIVES);
            if (factualCreatives != null) {
                String factualPixelUrl = factualCreatives.get(selectedCreative.getCreative().getId());
                if (factualPixelUrl != null) {
                    // If we are bidding with Creative that used Factual DMP - we need to add pixel url into trackers list
                    List<String> itrackers = context.getAttribute(TargetingContext.DYNAMIC_IMP_TRACKERS);
                    if (itrackers == null) {
                        itrackers = new ArrayList<String>();
                        context.setAttribute(TargetingContext.DYNAMIC_IMP_TRACKERS, itrackers);
                    }
                    itrackers.add(factualPixelUrl);
                }
            }

            AdsquareEnrichCreatives adsquareCreatives = context.getAttribute(TargetingContext.ADSQUARE_ENRICH_CREATIVES, AdsquareEnrichCreatives.class);
            if (adsquareCreatives != null) {
                Integer adsqAudienceId = adsquareCreatives.getCreativesAudiencesIds().get(selectedCreative.getCreative().getId());
                // We are bidding with Creative that has audience targeted using Adsquare Enrich API query
                if (adsqAudienceId != null) {
                    AdsqrEnrichQueryRequest query = adsquareCreatives.getQueryRequest();
                    adsquareWorker.store(impression.getExternalID(), adsqAudienceId, query, rtbConfig.getRtbLostTimeDuration());
                }
            }

            boolean serveAdOnBid = (rtbConfig != null && rtbConfig.getAdMode() == RtbAdMode.BID);
            // Cache the details of this bid for later in case we get a win notice
            ProxiedDestination proxiedDestination = selectedCreative.getProxiedDestination();
            RtbBidDetails bidDetails = new RtbBidDetails(context, impression, displayType, serveAdOnBid ? null : proxiedDestination);

            // Here we finally store into cache
            rtbBidManager.saveBidDetails(bidDetails, rtbConfig.getRtbLostTimeDuration());

            if (byydRequest.doIncludeDestination()) {
                if (byydRequest.useOnlyRealDestination() && !creative.getDestination().hasRealDestination()) {
                    // ideally we should not bid but it should not be a problem - they'll turn down.
                    // check publisher config
                    LOG.warning("Forced to bid without destination. Creative:" + creative.getId() + ". AdSpace: " + context.getAdSpace().getId());
                } else {// backward compatible with non adx which uses destination - admeld, openx and yieldlab
                    String destinationUrl = creative.getDestination().getRealDestination();
                    if (destinationUrl == null) {
                        if (proxiedDestination != null) {
                            destinationUrl = proxiedDestination.getDestinationUrl();
                        } else {
                            destinationUrl = creative.getDestination().getData();
                        }
                    }
                    // Some Advertisers (Marktjagd namely) put placeholders into destination URL which makes AdX filter out bids
                    String processedUrl = MarkupGenerator.resolveMacros(destinationUrl, context, adSpace, creative, impression);
                    byydBid.setDestination(processedUrl);
                }
            }

            if (serveAdOnBid) {
                String[] rendered = adMarkupRenderer.createMarkup(context, impression, creative, format, adSpace, displayType, httpRequest, proxiedDestination, rtbConfig);
                String adMarkup = rendered[0];
                // Save the Impression object for subsequent clickthrough calls
                impressionService.saveImpression(impression);
                if (byydImp.getAdObject() == AdObject.NATIVE) {
                    if (byydImp.getNativeAdRequest() != null) {
                        byydBid.setAdm(adMarkup); // OpenRTB 2.3
                    } else {
                        byydBid.setExt(adMarkup); // Mopub 2.1
                    }

                } else {
                    byydBid.setAdm(adMarkup); // default location
                }
            }

            Map<Long, CreativeBidDeal> creativeDeals = context.getAttribute(TargetingContext.PMP_CREATIVES_DEALS);
            if (creativeDeals != null) {
                CreativeBidDeal creativeBidDeal = creativeDeals.get(creative.getId());
                // Bidding using PMP with this creative
                if (creativeBidDeal != null) {
                    byydBid.setDealId(creativeBidDeal.getDealId());
                    byydBid.setSeat(creativeBidDeal.getSeatId());
                }
            } else {
                // MAD-3168 - Handling RTB Bid Seats
                String rtbSeatId = getRtbSeatId(context.getExchangePublisherId(), campaign.getAdvertiser());
                if (rtbSeatId != null) {
                    byydBid.setSeat(rtbSeatId);
                }
            }

            // Some exchanges need extra flag in response if ad markup is Mraid
            ContentForm contentForm = context.getAttribute(TargetingContext.RENDERED_TRANSFORM);
            byydBid.setContentForm(contentForm);

            byydBid.setAdid(impression.getExternalID()); // Used for MoPub Rtb loss notifications

            if (rtbConfig.getWinNoticeMode() == RtbWinNoticeMode.OPEN_RTB) {
                StringBuilder builder = VhostManager.makeBaseUrl(httpRequest, byydRequest.getImp().isSslRequired()).append(winUrlPath);
                if (AdServerFeatureFlag.EXCHANGE_IN_URL.isEnabled()) {
                    builder.append('/').append(adSpace.getPublication().getPublisher().getExternalId());
                }
                builder.append('/').append(impression.getExternalID()).append("?sp=").append(rtbConfig.getSpMacro());

                byydBid.setNurl(builder.toString());

            } else if (!serveAdOnBid) { // ad markup served on win notification
                StringBuilder builder = VhostManager.makeBaseUrl(httpRequest, byydRequest.getImp().isSslRequired()).append(winUrlPath);
                if (AdServerFeatureFlag.EXCHANGE_IN_URL.isEnabled()) {
                    builder.append('/').append(adSpace.getPublication().getPublisher().getExternalId());
                }
                builder.append('/').append(impression.getExternalID());
                byydBid.setNurl(builder.toString());
            }

            if (bidListener != null) {
                bidListener.bidMade(context, byydRequest, byydImp, byydBid, impression, selectedCreative);
            }

            backupLogger.logBidServed(impression, new Date(), context, byydRequest);
            return byydBid;
        } catch (NoBidException nbx) {
            // This is signal to nobid even when we already have selected creative
            if (creative != null) {
                counterManager.increment(rtbContext.getPublisherExternalId(), "RtbLogicImpl.NBX.CR." + creative.getId());
            }
            unfilledAdEvent(context.getAttribute(TargetingContext.UNFILLED_REASON), impression, context);
            throw nbx; // And rethrow

        } catch (Exception x) {
            // This is rather rare, but serious - record exception
            LOG.log(Level.WARNING, "Failed to generate ad", x);
            if (bidListener != null) {
                bidListener.bidNotMade(context, byydRequest, byydImp, x);
            }

            counterManager.bidError(adSpace, creative, x);

            counterManager.increment(rtbContext.getPublisherExternalId(), "RtbLogicImpl.X.PC." + adSpace.getPublication().getId());
            if (creative != null) {
                counterManager.increment(rtbContext.getPublisherExternalId(), "RtbLogicImpl.X.CR." + creative.getId());
            }

            unfilledAdEvent(UnfilledReason.EXCEPTION, impression, context);
            return null; // Do NOT rethrow
        }
    }

    private void unfilledAdEvent(UnfilledReason unfilledReason, Impression impression, TargetingContext context) {
        AdEvent event = adEventFactory.newInstance(AdAction.UNFILLED_REQUEST);
        event.setUnfilledReason(unfilledReason == null ? UnfilledReason.UNKNOWN : unfilledReason);

        // AF-1345
        // One last backstop to make sure UNFILLED_REQUEST events never
        // have CREATIVE_ID set (which causes them to end up in the
        // agg_l_adv* tables, which is bad).  Even though creative may be
        // null at this point, it might have just been nulled out, and
        // populateImpression could have been called before that.  Let's
        // make darn sure we "null it out" if that's the case.  Yes, the
        // zero here is intentional.
        impression.setCreativeId(0);

        //      context.populateAdEvent(event, impression, creative);
        //      adEventLogger.logAdEvent(event, context);
        backupLogger.logUnfilledRequest(event.getUnfilledReason(), event.getEventTime(), context);
    }

    /**
     * @param addIfNotFound whether or not we should call addFormatToAdSpace if
     * the format is found but isn't present in AdSpace.formats.  This would be
     * passed as true in the case of a primary format lookup, or false in the
     * case of a fallback lookup (i.e. 320x50 falling back on 300x50)
     * update: fallback not used any more - let the param stay
     */
    Set<Long> deriveFormatsFromWidthAndHeight(ByydImp byydImp, AdSpaceDto adSpace, boolean textBlocked, boolean bannerBlocked, TargetingContext context, boolean addIfNotFound)
            throws FormatRelatedException {

        Set<FormatDto> formats;
        if (byydImp.getAdObject() == AdObject.NATIVE) {
            formats = new HashSet<FormatDto>(1);
            FormatDto nativeFormat = context.getDomainCache().getFormatBySystemName(SystemName.FORMAT_NATIVE);
            formats.add(nativeFormat);
        } else {
            Integer width = byydImp.getW();
            Integer height = byydImp.getH();
            if (width == null || height == null) {
                // No dimensions in bid request, return null to allow fallback to adspace derived formats 
                return null;
            }

            formats = context.getDomainCache().getBoxableFormatsBySize(width, height);
            /*
             * We do not have embedability mapping table between formats. Standard banner 300x50 into 320x50 adspace is hacked inside banner FORMAT internal hierarchy (xl, xxl,...)
             * Only one other case is 300x75 creative into 320x75 adslot that exist only Yieldlab. To be precise YieldLab never sends 300x75 bid requests 
             * but apparently some German Advertisers want to use creatives with such dimensions. My gut feeling is that in next few years, we will see maybe thee of them.
             */
            if (formats != null) {
                if (width == 320 && height == 75) {
                    formats = new HashSet<FormatDto>(formats); // make a copy of that set!
                    Set<FormatDto> formats300x75 = context.getDomainCache().getBoxableFormatsBySize(300, 75);
                    if (formats300x75 != null) {
                        formats.addAll(formats300x75);
                    }
                }
            }

            if (formats == null) {
                throw new FormatRelatedException("No Format for: " + width + "x" + height);
            }
        }

        Set<Long> formatIds = null;

        // Before we bother trying to bid, let's see if any of the determined formats is
        // even among the AdSpace's supported formats.  If it's not, then we're not
        // going to have any eligible creatives ready for targeting.
        for (FormatDto format : formats) {
            boolean notBlocked = !(SystemName.FORMAT_TEXT.equals(format.getSystemName()) ? textBlocked : bannerBlocked);
            if (notBlocked) {
                if (adSpace.getFormatIds().contains(format.getId())) {
                    if (formatIds == null) {// initialize lazy
                        formatIds = new HashSet<Long>();
                    }
                    formatIds.add(format.getId());
                } else {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("AdSpace: " + adSpace.getId() + " doesn't have Format: " + format.getSystemName() + ", queueing for update");
                    }

                    if (addIfNotFound) {
                        // Queue the addition of this new FormatDto to the given AdSpace
                        rtbIdService.addFormatToAdSpace(adSpace, format);
                    }
                }
            }
        }

        if (formatIds == null) {
            // We can't serve this impression, since there won't be any creatives eligible for this format
            throw new FormatRelatedException("AdSpace id=" + adSpace.getId() + " doesn't have any of the formats for size: " + byydImp.getW() + " x " + byydImp.getH());
        }

        // Good to go, try these
        return formatIds;
    }

    /**
     * Derive a subset of FormatDto ids that are allowed to be targeted for a given
     * AdSpace when a specific format was NOT specified using the size parameters
     * at request time. We examine the blocked ad types and blocked creative
     * attributes to see if text and/or banner is blocked, and we apply those
     * blocks to AdSpace.formats.
     * @returns a set of FormatDto ids that can be used to restrict targeting, or null
     * if no restrictions apply (use all of AdSpace.formats)
     */
    Set<Long> deriveAllowedFormatIds(AdSpaceDto adSpace, boolean textBlocked, boolean bannerBlocked, TargetingContext context) throws FormatRelatedException {
        if (!textBlocked && !bannerBlocked) {
            // Nothing is blocked...every one of AdSpace.formats is allowed,
            // so we can just return null
            return null;
        }

        // Allow all formats associated with the AdSpace, minus any blocked formats
        Set<Long> allowedFormatIds = new HashSet<Long>();
        for (Long adSpaceFormatId : adSpace.getFormatIds()) {
            FormatDto adSpaceFormat = context.getDomainCache().getFormatById(adSpaceFormatId);
            boolean textFormat = SystemName.FORMAT_TEXT.equals(adSpaceFormat.getSystemName());
            if (textBlocked && textFormat) {
                continue;
            } else if (bannerBlocked && !textFormat) {
                continue;
            } else {
                allowedFormatIds.add(adSpaceFormatId);
            }
        }

        // Make sure we actually are allowing at least one format
        if (allowedFormatIds.isEmpty()) {
            // Nothing left that's not blocked...suckage
            throw new FormatRelatedException("AdSpace id=" + adSpace.getId() + " supports no formats acceptable by the request");
        }

        return allowedFormatIds;
    }

    /**
     * Pattern for recognizing OpenRTB 2.0 IAB category ids with dash suffixes
     */
    private static final Pattern IAB_CATEGORY_2_0_PATTERN = Pattern.compile("^(IAB\\d+)-(\\d+)$");

    /*
     * Pattern strings for different types to be ignored. Keep an eye on CATEGORY_IGNORE_PATTERN while specifyin
     * 
     * Pattern for valid open rtb2 categories which we don't maintain. Using regexp to exclude them for now
     * If the regexp gets complex (i.e. makes it slow), just feed in dummies from the domainserializer itself
     */
    private static final String LEGAL_IAB_CATS_UNUSED = "^IAB2(2-4|3-[78]|[56]|5-[1-7]|6-[1-4])$";

    /* Known patterns not yet supported; To prevent warnings obscuring the logs 
     *  Defining separate - may be plugged in from props later on
     * */
    private static final String KNOWN_YET_UNMAPPED_IGN = "^(?:APL|AND)\\d+(?:-\\d)?\\d*$";

    private static final Pattern CATEGORY_IGNORE_PATTERN = Pattern.compile(LEGAL_IAB_CATS_UNUSED + "|" + KNOWN_YET_UNMAPPED_IGN);

    public static Long getCategoryIdByIabId(String iabId, TargetingContext context) {
        Long categoryId = context.getDomainCache().getCategoryIdByIabId(iabId);
        if (categoryId != null) {
            return categoryId;
        }

        // We know about these...don't log warnings for them
        if (CATEGORY_IGNORE_PATTERN.matcher(iabId).matches()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Category not found by IAB id: " + iabId + " on Adspace: " + context.getAdSpace().getId());
            }
            return null;
        }

        //Now that we've taken the trouble to assign open rtb 2 categories to all our categories, it should
        //  not get here if the category passed was valid. Keeping some of the code below for backward compatibility
        //  Will log a warning in any case

        // TODO: properly handle OpenRTB 2.0 IAB category ids
        // TEMPORARY HACK for AF-1110.  MoPub is passing us IAB ids with dash
        // suffixes such as "IAB14-1" or "IAB7-39".  Strip off the dash suffix
        // if present, since we don't have those categories in our system yet.
        Matcher matcher = IAB_CATEGORY_2_0_PATTERN.matcher(iabId);
        if (matcher.matches()) {
            // Try just the first "IABnn" portion
            categoryId = getCategoryIdByIabId(matcher.group(1), context);
            if (categoryId != null) {
                // Just for some sort of backward compatibility - monitor this for a release and get rid of it
                LOG.warning("TR4CKM3: Invalid category. Wrong conversion. Treating " + iabId + " as " + matcher.group(1) + " (Category id=" + categoryId + ")");
                return categoryId;
            }
        }
        // Changing to fine logging, we are getting both IAB and category name from MoPub.
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Invalid category - not found by IAB id: " + iabId + ". Adspace: " + context.getAdSpace().getId());
        }
        return null;
    }

    public static final class FormatRelatedException extends Exception {
        private static final long serialVersionUID = 1L;

        /**
         * Default behaviour is to skip stack trace creation.
         * For debuging purposes here goes way to reenable it using System property
         */
        private static final boolean stacktrace = Boolean.valueOf(System.getProperty("FormatRelatedException.stacktrace"));

        private FormatRelatedException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            if (stacktrace) {
                return super.fillInStackTrace();
            } else {
                return this;
            }
        }
    }

    public long getEffectivePublisherId(TargetingContext context, AdserverDomainCache adCache, ByydRequest byydRequest, RtbBidEventListener listener) throws NoBidException {

        long effectivePublisherId;

        String publisherExtId = byydRequest.getPublisherExternalId();

        Long exchangeId = adCache.getPublisherIdByExternalID(publisherExtId);
        if (exchangeId == null) {
            String reason = "Publisher not found: " + publisherExtId;
            LOG.warning(reason); //Serious offence -> log it right here
            if (listener != null) {
                listener.bidRequestRejected(context, byydRequest, reason);
            }
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.UNKNOWN_PUBLISHER, publisherExtId);
        }

        context.setExchangePublisherId(exchangeId);

        String associateRef = byydRequest.getAssociateReference();
        if (associateRef == null) {
            effectivePublisherId = exchangeId;
        } else {
            Long associateId = adCache.getAssociatePublisherID(exchangeId, associateRef);
            if (associateId == null) {
                String reason = "Associate publisher mapping not in cache " + publisherExtId + " / " + exchangeId + " / " + associateRef + " / " + associateId;
                LOG.warning(reason);
                if (listener != null) {
                    listener.bidRequestRejected(context, byydRequest, reason);
                }
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.UNKNOWN_PUBLISHER, publisherExtId + " / " + exchangeId + " / " + associateRef
                        + " / " + associateId);
            }
            effectivePublisherId = associateId;
        }

        Map<String, AdSpaceDto> adSpacesMap = adCache.getPublisherRtbAdSpacesMap(effectivePublisherId);
        if (adSpacesMap != null && !adSpacesMap.isEmpty()) {
            AdSpaceDto anyAdSpace = adSpacesMap.values().iterator().next();
            context.setEffectivePublisher(anyAdSpace.getPublication().getPublisher());
        }

        return effectivePublisherId;
    }

    private String getRtbSeatId(Long publisherId, AdvertiserDto advertiser) {
        String rtbSeatId = null;

        // First try to find seat id at advertiser level
        if (advertiser.isEnableRtbBidSeat()) {
            rtbSeatId = advertiser.getRtbBidSeats().get(publisherId);
        }

        // if rtbSeat has not found then try to find it at agency level (company)
        if ((rtbSeatId == null) && (advertiser.getCompany().isEnableRtbBidSeat())) {
            rtbSeatId = advertiser.getCompany().getRtbBidSeats().get(publisherId);
        }

        return rtbSeatId;
    }

}

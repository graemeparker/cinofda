package com.adfonic.adserver.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AbstractAdComponents;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.FrequencyCounter;
import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.adserver.MutableWeightedCreative;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.StoppageManager;
import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.controller.dbg.DebugBidContext;
import com.adfonic.adserver.controller.dbg.DebugBidContext.CreativePurpose;
import com.adfonic.adserver.plugin.Plugin;
import com.adfonic.adserver.plugin.PluginFillRateTracker;
import com.adfonic.adserver.plugin.PluginManager;
import com.adfonic.adserver.rtb.nativ.AdObject;
import com.adfonic.adserver.rtb.nativ.ByydDeal;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydMarketPlace;
import com.adfonic.domain.BeaconMode;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.Gender;
import com.adfonic.domain.Medium;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.ContentSpecDto;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.LanguageDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RateCardDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;
import com.adfonic.domain.cache.dto.adserver.creative.PrivateMarketPlaceDealDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.ortb.nativead.NativeAdRequest;
import com.adfonic.ortb.nativead.NativeAdRequestAsset;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.DataAsset;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.DataAssetType;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.ImageAsset;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.ImageAssetType;
import com.adfonic.ortb.nativead.NativeAdRequestAsset.TitleAsset;
import com.adfonic.util.AcceptedLanguages;
import com.adfonic.util.FastLinkedList;
import com.adfonic.util.Range;
import com.adfonic.util.ThreadLocalRandom;
import com.adfonic.util.Weighted;
import com.adfonic.util.WeightedUtils;
import com.adfonic.util.stats.CounterManager;

@Component
public class BasicTargetingEngineImpl implements TargetingEngine {
    private static final transient Logger LOG = Logger.getLogger(BasicTargetingEngineImpl.class.getName());

    private final boolean fineLogging = LOG.isLoggable(Level.FINE);
    private final boolean infoLogging = LOG.isLoggable(Level.INFO);
    private final DisplayTypeUtils displayTypeUtils;
    private final PluginManager pluginManager;
    private final StoppageManager stoppageManager;
    private final FrequencyCapper frequencyCapper;
    private final PluginFillRateTracker pluginFillRateTracker;
    private final StatusChangeManager statusChangeManager;
    private final CounterManager counterManager;

    @Value("${appnxs.operatingpub.id}")
    private long appNexusOperatingPublisherId;
    @Value("${adx.exchange.id}")
    private long adXExchangeId;

    private final LocalBudgetManager budgetManager;

    private final DeviceLocationTargetingChecks geoChecks;

    private final DeviceIdentifierTargetingChecks didChecks;

    private final AdsquareTargetingChecks adsquareChecks;

    @Autowired
    public BasicTargetingEngineImpl(DisplayTypeUtils displayTypeUtils, PluginManager pluginManager, StoppageManager stoppageManager, FrequencyCapper frequencyCapper,
            PluginFillRateTracker pluginFillRateTracker, StatusChangeManager statusChangeManager, CounterManager counterManager, LocalBudgetManager budgetManager,
            DeviceLocationTargetingChecks geoTargeting, DeviceIdentifierTargetingChecks didTargeting, AdsquareTargetingChecks adsquareChecks) {
        this.displayTypeUtils = displayTypeUtils;
        this.pluginManager = pluginManager;
        this.stoppageManager = stoppageManager;
        this.frequencyCapper = frequencyCapper;
        this.pluginFillRateTracker = pluginFillRateTracker;
        this.statusChangeManager = statusChangeManager;
        this.counterManager = counterManager;
        this.budgetManager = budgetManager;
        this.geoChecks = geoTargeting;
        this.didChecks = didTargeting;
        this.adsquareChecks = adsquareChecks;
    }

    // This is a list of pools of reusable MutableWeightedCreative instances.
    // Optimization to limit garbage generation and promote reuse.
    private static final ConcurrentLinkedQueue<FastLinkedList<MutableWeightedCreative>> REUSABLE_POOLS = new ConcurrentLinkedQueue<FastLinkedList<MutableWeightedCreative>>();

    /**
     * Be aware that @param byydImp is null for non rtb requests
     */
    @Override
    public SelectedCreative selectCreative(AdSpaceDto adSpace, Collection<Long> allowedFormatIds, TargetingContext context, boolean diagnosticMode,
            boolean strictlyUseFirstDisplayType, TimeLimit timeLimit, TargetingEventListener listener) {
        if (fineLogging) {
            LOG.fine("Selecting creative for AdSpace.name=" + adSpace.getName() + " (externalID=" + adSpace.getExternalID() + ", Publication=" + adSpace.getPublication().getName()
                    + ")");
        }

        // This may look like a duplicate of the check performed in selectCreative above,
        // but please leave it.  There are separate entry points into the targeting engine.
        if (Boolean.TRUE.equals(context.getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class))) {
            if (fineLogging) {
                LOG.fine("Not targeting request from private network");
            }
            context.setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.PRIVATE_NETWORK);
            if (listener != null) {
                listener.unfilledRequest(adSpace, context);
            }
            return null;
        }

        // We need to build a per-FormatDto mapping to DisplayTypeDto for the
        // given device that's going to view the ad.
        // To do that we'll need to derive the device properties
        Map<String, String> deviceProps = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
        if (deviceProps == null) {
            if (fineLogging) {
                LOG.fine("Could not derive device properties");
            }
            context.setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_DEVICE_PROPS);
            if (listener != null) {
                listener.unfilledRequest(adSpace, context);
            }
            return null;
        }

        // Special case for the PSP browser, for which we override the
        // value of "mobileDevice" to be "0" in the DeviceAtlas data
        if ("0".equals(deviceProps.get("mobileDevice"))) {
            if (fineLogging) {
                LOG.fine("Not serving ads to device having mobileDevice=0");
            }
            context.setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NOT_MOBILE_DEVICE);
            if (listener != null) {
                listener.unfilledRequest(adSpace, context);
            }
            return null;
        }

        // Also make sure there's a ModelDto before going any further
        ModelDto model = context.getAttribute(TargetingContext.MODEL);
        if (model == null) {
            if (fineLogging) {
                LOG.fine("Failed to derive Model");
            }
            context.setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_MODEL);
            if (listener != null) {
                listener.unfilledRequest(adSpace, context);
            }
            return null;
        }
        if (fineLogging) {
            LOG.fine("Derived model: " + model.getVendor().getName() + " " + model.getName() + " (externalID=" + model.getExternalID() + ")");
        }

        // AO-271 - don't serve ads to hidden models
        if (model.isHidden()) {
            if (fineLogging) {
                LOG.fine("Not serving ad to hidden Model");
            }
            context.setAttribute(TargetingContext.UNFILLED_REASON, UnfilledReason.NO_MODEL);
            if (listener != null) {
                listener.unfilledRequest(adSpace, context);
            }
            return null;
        }

        // Derive the viewer's country
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (fineLogging) {
            if (country != null) {
                LOG.fine("Derived country: " + country.getIsoCode());
            } else {
                LOG.fine("Failed to derive country");
            }
        }

        // Derive the viewer's operator
        OperatorDto operator = context.getAttribute(TargetingContext.OPERATOR);
        if (fineLogging) {
            if (operator != null) {
                LOG.fine("Derived operator: " + operator.getName());
            } else {
                LOG.fine("Failed to derive operator");
            }
        }

        // Derive the viewer's platform
        PlatformDto platform = context.getAttribute(TargetingContext.PLATFORM);
        if (fineLogging) {
            if (platform != null) {
                LOG.fine("Derived platform: " + platform.getSystemName());
            } else {
                LOG.fine("Failed to derive platform");
            }
        }

        // Derive the viewer's gender
        Gender gender = context.getAttribute(TargetingContext.GENDER);
        if (gender != null && fineLogging) {
            LOG.fine("Derived gender: " + gender);
        }

        // Derive the viewer's age range
        Range<Integer> ageRange = context.getAttribute(TargetingContext.AGE_RANGE);
        if (ageRange != null && fineLogging) {
            LOG.fine("Derived age range: " + ageRange);
        }

        // Derive the viewer's capabilities
        Set<Long> capabilityIds = context.getAttribute(TargetingContext.CAPABILITY_IDS);

        // Look for a medium override
        Medium medium = context.getAttribute(TargetingContext.MEDIUM);
        if (medium == null) {
            // Fall back on the one configured on adSpace
            medium = context.getDomainCache().getPublicationTypeById(adSpace.getPublication().getPublicationTypeId()).getMedium();
            context.setAttribute(TargetingContext.MEDIUM, medium);
        }

        // Notify the listener that we're done deriving attributes
        if (listener != null) {
            listener.attributesDerived(adSpace, context);
        }

        AdspaceWeightedCreative[] eligibleCreativesByPriority = context.getAdserverDomainCache().getEligibleCreatives(adSpace.getId());
        // Notify the listener of the creatives that are eligible
        if (listener != null) {
            listener.creativesEligible(adSpace, context, eligibleCreativesByPriority);
        }

        BigDecimal ecpmFloor = context.getAttribute(TargetingContext.ECPM_FLOOR);

        // Create a master list of reusable MutableWeightedCreative objects.
        // Instances will get added and removed from this pool below.
        // This is an optimization to reduce garbage generation.
        FastLinkedList<MutableWeightedCreative> reusablePool = acquireReusablePool();
        try {
            // At this point we need to walk the priority tiers, attempting to pick a creative from each priority tier until we find one.
            UnfilledReason lastUnfilledReason = UnfilledReason.NO_CREATIVES;

            for (AdspaceWeightedCreative entry : eligibleCreativesByPriority) {

                if (!checkTimeLimit(timeLimit, adSpace, context, listener)) {
                    lastUnfilledReason = UnfilledReason.TIMEOUT;
                    if (fineLogging) {
                        LOG.fine("Timeout while targeting for adspace " + adSpace.getId());
                    }
                    break;
                }

                int priority = entry.getPriority();
                Long[] eligibleCreativeIds = entry.getCreativeIds();

                // target provided creatives instead of eligible
                Long[] providedCreativesIds = context.getAttribute(TargetingContext.PROVIDED_CREATIVE_ID);
                if (providedCreativesIds != null) {
                    eligibleCreativeIds = providedCreativesIds;
                    if (infoLogging) {
                        LOG.log(Level.INFO, "providedCreativesIds.length=" + providedCreativesIds.length);
                    }
                }

                DebugBidContext debugContext = context.getAttribute(TargetingContext.DEBUG_CONTEXT);
                if (debugContext != null) {
                    Long debugCreativeId = debugContext.getCreativeId();
                    if (debugCreativeId != null && debugContext.getCreativePurpose() == CreativePurpose.Enforce) {
                        // Add creativeId into creativeIds if not already there - performance is irelevant here
                        List<Long> cridsAsList = new LinkedList<Long>(Arrays.asList(eligibleCreativeIds));
                        boolean contains = cridsAsList.contains(debugCreativeId);
                        if (!contains) {
                            cridsAsList.add(debugCreativeId);
                            eligibleCreativeIds = cridsAsList.toArray(new Long[cridsAsList.size()]);
                        }
                    }
                }
                BidDerivedData data = new BidDerivedData();
                data.allowedFormatIds = allowedFormatIds;
                data.deviceProps = deviceProps;
                data.model = model;
                data.country = country;
                data.operator = operator;
                data.platform = platform;
                data.gender = gender;
                data.ageRange = ageRange;
                data.capabilityIds = capabilityIds;
                data.medium = medium;
                data.strictlyUseFirstDisplayType = strictlyUseFirstDisplayType;
                data.ecpmFloor = ecpmFloor;

                try {
                    SelectedCreative selected = targetAndSelectCreative(priority, eligibleCreativeIds, reusablePool, adSpace, context, data, diagnosticMode, timeLimit, listener);
                    if (fineLogging) {
                        LOG.fine("Selected creative " + selected.getCreative().getId() + ", campaign: " + selected.getCreative().getCampaign().getId() + " priority: " + priority);
                    }

                    // Notify the listener that we have selected a creative
                    if (listener != null) {
                        listener.creativeSelected(adSpace, context, selected.getCreative());
                    }

                    return selected;
                } catch (NoCreativesException ncx) {
                    // Keep track of the "latest" reason we've seen
                    if (fineLogging) {
                        LOG.fine("Failed to pick creative for priority=" + priority + " due to " + ncx.getUnfilledReason());
                    }
                    lastUnfilledReason = ncx.getUnfilledReason();
                }

                if (providedCreativesIds != null) {
                    //Break the loop as we have already targetted the provided creative List
                    break;
                }
            }

            // We walked through all of the available priority tiers and didn't
            // find a single creative.  Mark in the context why we were unable
            // to fill the request.
            if (fineLogging) {
                LOG.fine("No creatives available for any priority, lastUnfilledReason=" + lastUnfilledReason);
            }
            context.setAttribute(TargetingContext.UNFILLED_REASON, lastUnfilledReason);

            if (listener != null) {
                listener.unfilledRequest(adSpace, context);
            }

            return null;
        } finally {
            releaseReusablePool(reusablePool);
        }
    }

    public static class BidDerivedData {
        Collection<Long> allowedFormatIds;
        Map<String, String> deviceProps;
        ModelDto model;
        CountryDto country;
        OperatorDto operator;
        PlatformDto platform;
        Gender gender;
        Range<Integer> ageRange;
        Set<Long> capabilityIds;
        Medium medium;
        BigDecimal ecpmFloor;
        boolean strictlyUseFirstDisplayType;

        // Reused in targeting cycle
        Date now = new Date();
        Map<Long, GeotargetDto> matchingGeotargetByCreativeId = new HashMap<Long, GeotargetDto>();
        double globalRevenueFloor;

    }

    /**
     * Be aware that @param byydImp is null for non rtb requests
     */
    SelectedCreative targetAndSelectCreative(int priority, Long[] eligibleCreativeIds, FastLinkedList<MutableWeightedCreative> reusablePool, AdSpaceDto adSpace,
            TargetingContext context, BidDerivedData data, boolean diagnosticMode, TimeLimit timeLimit, TargetingEventListener listener) throws NoCreativesException {

        if (fineLogging) {
            LOG.fine("Targeting eligible creatives: " + Arrays.asList(eligibleCreativeIds) + ", priority: " + priority);
        }
        //get adops key
        String adopsKey = context.getAttribute(Parameters.ADOPS_KEY, String.class);
        if (adopsKey != null && infoLogging) {
            LOG.log(Level.INFO, "adopsKey=" + adopsKey);
        }

        // Prune the list of creatives based on targeting criteria...populating a list
        // of MutableWeightedCreative as we discover eligiblecreatives that don't get
        // bounced by targeting exclusions.  Set this list up with the reusablePool as
        // its removal receiver, so that when elements are removed from the list, or
        // when the list gets cleared, they go back into the reusablePool.
        FastLinkedList<MutableWeightedCreative> mwcs = new FastLinkedList<MutableWeightedCreative>(reusablePool);

        AdserverDomainCache adserverCache = context.getAdserverDomainCache();
        Long countryId = data.country != null ? data.country.getId() : Constant.UNKNOWN_COUNTRY_ID;

        Set<Long> creativesForBidCountry = adserverCache.getEligibleCreativeIdsForCountry(countryId);
        if (creativesForBidCountry.isEmpty()) {
            if (fineLogging) {
                LOG.fine("Targeting NoCreativesException for country: " + countryId);
            }
            throw new NoCreativesException(UnfilledReason.NO_CREATIVES);
        }
        //assigning the map size as maximum number of creatives to avoid any rehashing inbetween
        //may move it up to ThreadLocal and clear everytime before doing this loop
        //but only if creation of this Map and Gcing becomes overhead
        Map<Long, CreativeEliminatedReason> segmentTargetedMap = new HashMap<Long, CreativeEliminatedReason>(eligibleCreativeIds.length);

        Map<Long, Boolean> segmentIdBrowserEligibilityMap = new HashMap<Long, Boolean>();

        //SC-150
        data.globalRevenueFloor = adserverCache.getDefaultDoubleValue("global_revenue_floor", 0.0);

        //SC-151
        //For performance we will create only one object which will get used
        //for each iteration for following loop. In future make
        //sure u never use this object outside the loop.
        EcpmInfo ecpmInfo = new EcpmInfo();
        try {
            for (Long creativeId : eligibleCreativeIds) {

                if (!checkTimeLimit(timeLimit, adSpace, context, listener)) {
                    throw new NoCreativesException(UnfilledReason.TIMEOUT);
                }

                CreativeDto creative = adserverCache.getCreativeById(creativeId);
                if (creative == null) {
                    // SC-433, now ad request can come with an creative id, and it could be possible that such creative do not exists so by pass such creatives
                    if (infoLogging) {
                        LOG.info("Creative Id not found in cache :" + creativeId);
                    }
                    continue;
                }
                // If adops key is present then no need to run the targeting check, directly mark creative as Eligible
                if (adopsKey != null) {
                    // If adops key is present then only compute ECPM info or we may need to fake this calculation later
                    if (infoLogging) {
                        LOG.info("Skipped Targeting Checks for this request");
                    }
                    adserverCache.computeEcpmInfo(adSpace, creative, data.platform, countryId, data.ecpmFloor, ecpmInfo);
                } else {
                    if (!check(adSpace, creative, adserverCache, ecpmInfo, data, context, listener, segmentTargetedMap, creativesForBidCountry, segmentIdBrowserEligibilityMap)) {
                        continue; // not targeted -> skip
                    } else {
                        // targeted -> do not skip
                    }
                }

                if (listener != null) {
                    listener.creativeSelected(adSpace, context, creative);
                }
                addTargetedCreative(adSpace, creative, mwcs, reusablePool, context, ecpmInfo.getWeight());
            }

            if (fineLogging) {
                LOG.fine("Targeted creatives: " + new LinkedList<>(mwcs));
            }

            // Notify the listener that we have pruned the set of eligible creatives
            if (listener != null) {
                listener.creativesTargeted(adSpace, context, priority, mwcs);
            }

            SelectedCreative selectedDebugged = selectDebuggedCreative(adSpace, context, mwcs);

            if (mwcs.isEmpty()) {
                if (fineLogging) {
                    LOG.fine("No creatives left for priority=" + priority + " after targeting");
                }
                if (selectedDebugged != null) {
                    return selectedDebugged; // return debugged creative 
                } else {
                    throw new NoCreativesException(UnfilledReason.NO_CREATIVES);
                }
            }

            // Select a creative from among those targeted
            SelectedCreative selectedTargeted = selectFromTargeted(priority, mwcs, adSpace, context, diagnosticMode, timeLimit, listener, selectedDebugged);

            if (data.matchingGeotargetByCreativeId != null) {
                GeotargetDto matchingGeotarget = data.matchingGeotargetByCreativeId.get(selectedTargeted.getCreative().getId());
                if (matchingGeotarget != null) {
                    // Store the matching GeotargetDto in the context so it can be logged with the ad event later
                    context.setAttribute(TargetingContext.GEOTARGET, matchingGeotarget);
                }
            }
            return selectedTargeted;

        } finally {
            // Clear the MutableWeightedCreative list we were working with so that its
            // elements get returned back to the reusablePool for reuse.  Also, make sure
            // every instance we worked with has nulled-out references, i.e. to Creative.
            // Otherwise, we run the risk of memory leaks.
            for (MutableWeightedCreative mwc : mwcs) {
                mwc.setCreative(null);
            }
            // This is the most efficient way to return all elements back to the reusablePool.
            // It does an efficient node transfer instead of creating new nodes.
            mwcs.clear();
        }
    }

    private boolean check(AdSpaceDto adSpace, CreativeDto creative, AdserverDomainCache adserverCache, EcpmInfo ecpmInfo, BidDerivedData data, TargetingContext context,
            TargetingEventListener listener, Map<Long, CreativeEliminatedReason> segmentTargetMap, Set<Long> creativeIdsForCountry,
            Map<Long, Boolean> segmentIdBrowserEligibilityMap) {

        // Normal bidding flow...
        SegmentDto segment = creative.getSegment();

        if (fineLogging) {
            LOG.log(Level.FINE, "Targeting creative: " + creative.getId() + ", segment: " + segment.getId() + ", campaign: " + creative.getCampaign().getId());
        }

        // By default segment check Required for all creatives
        boolean doSegmentCheck = true;
        if (segment == null) {
            //XXX I do not believe this can happen...but let's see counter first before deleting...
            counterManager.incrementCounter("TragetingCreativeWithNullSegment");
            doSegmentCheck = false;
        } else {
            CreativeEliminatedReason segmentEliminatedReason = segmentTargetMap.get(segment.getId());
            if (segmentEliminatedReason == null) {
                // First time we see this segment - need to be evaluated
            } else if (segmentEliminatedReason == CreativeEliminatedReason.SegmentIsTargeted) {
                // Segment already targeted - no need to repeate segment checks
                doSegmentCheck = false;
            } else {
                // Segment is already eliminated
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, segmentEliminatedReason, "Marked segment: " + segment.getId());
                }
                return false;
            }
        }

        long countryId = data.country != null ? data.country.getId() : Constant.UNKNOWN_COUNTRY_ID;

        // CountryDto and PlatformDto checks are most likely to bounce a creative, so we hammer those out up front.
        if (doSegmentCheck) {

            if (!creativeIdsForCountry.contains(creative.getId())) {
                segmentTargetMap.put(segment.getId(), CreativeEliminatedReason.countryNotTargeted);
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.countryNotTargeted, "Bid country " + countryId + " & Segment marked: "
                            + segment.getId());
                }
                return false;
            }

            CreativeEliminatedReason elimination = DeviceFeaturesTargetingChecks.checkPlatformModel(adSpace, context, creative, segment, data.platform, data.model, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = DeviceFeaturesTargetingChecks.checkConnectionType(adSpace, context, creative, segment, data.operator, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = DeviceFeaturesTargetingChecks.checkVendorsAndDevice(adSpace, context, creative, segment, data.model, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = DeviceFeaturesTargetingChecks.checkDeviceGroup(adSpace, context, creative, segment, data.model, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }
        }

        // Medium Selective Checks

        CampaignDto campaign = creative.getCampaign();

        if (creative.isClosedMode()) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ClosedMode, "Creative is in Closed Mode");
            }
            return false;
        }

        ByydImp byydImp = context.getAttribute(TargetingContext.BYYD_IMP);
        if (byydImp != null && byydImp.isSslRequired() && !creative.isSslCompliant()) {
            if (listener != null) {
                listener.creativeEliminated(context.getAdSpace(), context, creative, CreativeEliminatedReason.notSslCompliant, "Creative not SslCompliant");
            }
            return false;
        }

        if (campaign.isBudgetManagerEnabled() && campaign.getCurrentBid().getBidType() == BidType.CPM && budgetManager.isRecentlyUnderfunded(campaign)) {
            // TODO this actually is also "segment" wide elimination....
            counterManager.incrementCounter("BM.RU." + campaign.getId());
            return false;
        }

        if (!checkAllowedFormats(adSpace, context, creative, data.allowedFormatIds, listener)) {
            return false;
        }

        //MAD-952
        if (byydImp != null) {
            if (!checkCreativeAssetSize(context, creative, byydImp, listener)) {
                return false;
            }
        }

        if (!checkAdvancedScheduling(adSpace, context, creative, listener)) {
            return false;
        }

        adserverCache.computeEcpmInfo(adSpace, creative, data.platform, countryId, data.ecpmFloor, ecpmInfo);

        if (!checkPmpDeals(adSpace, creative, ecpmInfo.getBidPrice(), context, listener)) {
            return false;
        }

        if (!campaign.isHouseAd()
                && !isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(adSpace, creative, countryId, adserverCache, context, listener, data.ecpmFloor, ecpmInfo,
                        data.globalRevenueFloor)) {
            return false;
        }
        if (!campaign.isHouseAd() && !isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher(adSpace, creative, countryId, context, listener)) {
            return false;
        }
        if (!checkBlockedAdvertiserDomains(adSpace, context, creative, listener)) {
            return false;
        }

        if (!checkDisplayTypeAndAssetBundle(adSpace, context, creative, data.strictlyUseFirstDisplayType, listener)) {
            return false;
        }

        if (byydImp != null && byydImp.getAdObject() == AdObject.VIDEO) {
            if (!checkVideoDuration(adSpace, creative, byydImp, context, listener)) {
                return false;
            }
        }

        if (!checkNative(adSpace, context, creative, listener)) {
            return false;
        }

        if (!DeviceFeaturesTargetingChecks.checkSmsSupport(adSpace, context, creative, listener)) {
            return false;
        }
        if (!checkActive(adSpace, context, creative, listener)) {
            return false;
        }
        if (!checkPluginBased(adSpace, context, creative, data.model, listener)) {
            return false;
        }
        if (!DeviceFeaturesTargetingChecks.checkClickToCallSupport(adSpace, context, creative, data.deviceProps, listener)) {
            return false;
        }
        if (!checkBlockedCategories(adSpace, context, creative, listener)) {
            return false;
        }
        if (!checkExtendedCapabilities(adSpace, context, creative, listener)) {
            return false;
        }
        if (!checkBlockedDestinationAttributes(adSpace, context, creative, listener)) {
            return false;
        }
        if (!checkBlockedCreativeAttributes(adSpace, context, creative, listener)) {
            return false;
        }
        if (doSegmentCheck) {
            CreativeEliminatedReason elimination = checkMedium(adSpace, context, creative, segment, data.medium, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = DeviceFeaturesTargetingChecks.checkIpAddress(adSpace, context, creative, segment, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = checkIsSegmentTimeTargetted(adSpace, context, creative, segment, data.operator, data.country, data.now, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = DeviceFeaturesTargetingChecks.checkCapabilities(adSpace, context, creative, segment, data.capabilityIds, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = checkGenderAndAge(adSpace, context, creative, segment, data.gender, data.ageRange, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

        }

        Double languageQuality = checkLanguage(adSpace, context, creative, listener);
        if (languageQuality == null) {
            return false;
        }
        if (!checkDestinationBeaconUrl(adSpace, context, creative, listener)) {
            return false;
        }
        if (!checkStoppages(adSpace, context, creative, listener)) {
            return false;
        }
        if (!checkEndDate(adSpace, context, creative, listener)) {
            return false;
        }

        if (!checkAndroidMarketMedium(adSpace, context, creative, data.medium, listener)) {
            return false;
        }

        if (DeviceIdentifierTargetingChecks.checkDeviceIdentifiers(adSpace, context, creative, listener) != null) {
            return false;
        }

        // Segment checks that are more time consuming... (accessing Redis or any other backend or DBs)

        if (doSegmentCheck) {

            CreativeEliminatedReason elimination = didChecks.checkDeviceIdAudienceTargeting(adSpace, creative, context, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = DeviceFeaturesTargetingChecks.checkBrowsers(adSpace, context, creative, segment, listener, segmentIdBrowserEligibilityMap);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = adsquareChecks.check(context, adSpace, creative, segment, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = FactualTargetingChecks.check(context, adSpace, creative, segment, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            elimination = geoChecks.checkGeoTargetting(adSpace, context, creative, segment, data.matchingGeotargetByCreativeId, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }

            // SC-524
            elimination = didChecks.checkTrusteWeveIcon(adSpace, context, creative, listener);
            if (elimination != null) {
                segmentTargetMap.put(segment.getId(), elimination);
                return false;
            }
        }

        if (byydImp != null) {
            // Fix for MAX-2089/MAD-2379 - When Campaign's creatives have destination like mopubnativebrowser://navigate?url=http://www.mopub.com
            // This is implicitly targeting Mopub only as no other exchange is sending such flag
            if (creative.getDestination().getData().startsWith("mopubnativebrowser:") && !byydImp.isNativeBrowserClick()) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.CapabilityNotRequired,
                            "Creative +mopubnativebrowser vs Bid -nativebrowserclick");
                }
                return false;
            }
        }

        // Ok, this WeightedCreative is eligible, after all that rigamarole.  Time to
        // create a MutableWeightedCreative so we can update the eligibility time weights
        // with our targeting time weights.  We can't modify the WeightedCreative itself,
        // since that's shared across all threads and needs its initial weights preserved.
        // First try to reuse an instance, if we have one available

        if (segment != null) {
            // If this creative is eligible that means all its segment passed and segment is targeted for current request
            segmentTargetMap.put(segment.getId(), CreativeEliminatedReason.SegmentIsTargeted);
        }
        return true;
    }

    /**
     * @return null or selected debugged creative
     */
    private SelectedCreative selectDebuggedCreative(AdSpaceDto adSpace, TargetingContext context, List<MutableWeightedCreative> mwcs) {
        DebugBidContext debugContext = context.getAttribute(TargetingContext.DEBUG_CONTEXT);
        if (debugContext != null) {
            Long debugCreativeId = debugContext.getCreativeId();
            if (debugCreativeId != null) {
                MutableWeightedCreative debugMwc = null;
                for (MutableWeightedCreative mwc : mwcs) {

                    if (mwc.getCreative().getId().longValue() == debugCreativeId.longValue()) {
                        debugMwc = mwc;
                    }
                }

                // Can happen that debuged creative is not targeted and not even eligible, but it will fail if bid (exapmle: ad native) is incompatible with creative (example: vast video)
                if (debugMwc == null) {
                    // Fake-select it when enforced
                    if (debugContext.getCreativePurpose() == CreativePurpose.Enforce) {
                        debugMwc = new MutableWeightedCreative(adSpace, context.getAdserverDomainCache().getCreativeById(debugCreativeId));
                        debugMwc.setEcpmWeight(0.1);
                    }
                }
                if (debugMwc != null) {
                    // Return Prefered/Enforced instead of weighting and randomising...
                    // DiagnosticProxiedDestination pd = new DiagnosticProxiedDestination(context.getDomainCache().getFormatById(debugMwc.getCreative().getFormatId()).getSystemName());
                    DiagnosticProxiedDestination pd = null;
                    return new SelectedCreative(debugMwc, pd);
                }
            }
        }
        return null; // normal bidding
    }

    boolean isEligibleBasedOnMinimumTargettedPayputRateCardOfPublicationPublisher(AdSpaceDto adSpace, CreativeDto creative, long countryId, TargetingContext context,
            TargetingEventListener listener) {
        BigDecimal payout = null;
        CampaignBidDto campaignBid = creative.getCampaign().getCurrentBid();
        RateCardDto pubRateCard = adSpace.getPublication().getEffectiveRateCard(campaignBid.getBidType());
        if (pubRateCard != null) {
            BigDecimal minimumBid = pubRateCard.getMinimumBid(countryId);
            if (minimumBid != null) {
                payout = context.getAdserverDomainCache().getPayout(adSpace.getPublication().getPublisher().getId(), creative.getCampaign().getId());
                if (payout.compareTo(minimumBid) < 0) {
                    if (listener != null) {
                        listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.payoutLessThanMinBid, "Payout < Pub minimum bid in countryId=" + countryId);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param adSpace
     * @param creative
     * @param cache
     * @return Making it package visible  so that we can test it
     */
    boolean isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher(AdSpaceDto adSpace, CreativeDto creative, long countryId, AdserverDomainCache cache,
            TargetingContext context, TargetingEventListener listener, BigDecimal ecpmFloor, EcpmInfo ecpmInfo, double globalRevenueFloor) {

        if (ecpmInfo.getWeight() <= 0) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.creativeWeightZero, "Creative ecmp weight: " + ecpmInfo.getWeight()
                        + " <= 0 Bid Floor: " + ecpmFloor + " vs Bid Price: " + ecpmInfo.getBidPrice());
            }
            return false;
        }
        Boolean dealFloorExists = context.getAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.class);
        if (dealFloorExists != null && dealFloorExists) {
            //No need to check other floors as its already passed deal floor check
            return true;
        }

        PublicationDto pub = adSpace.getPublication();
        //This is check for minimum ECPM
        //If publication or publisher has ECPM Target Rate card then do the check
        RateCardDto effectiveEcpmTargetRateCard = pub.getEcpmTargetRateCard();
        if (effectiveEcpmTargetRateCard == null) {
            effectiveEcpmTargetRateCard = pub.getPublisher().getEcpmTargetRateCard();
        }
        boolean applyGlobalRevenueFloor = true;
        //for NON RTB apply the globalRevenueFloor only if effectiveEcpmTargetRateCard is null
        //For RTB apply the globalRevenueFloor always
        if (pub.getPublisher().getRtbConfig() == null && effectiveEcpmTargetRateCard != null) {
            //If for NON-RTB, publication or publisher rate card exists then don't apply global revenue floor
            applyGlobalRevenueFloor = false;
        }
        if (applyGlobalRevenueFloor && ecpmInfo.getExpectedRevenue() < globalRevenueFloor) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.revenueLessThenRevenueFloor, "Expected Revenue(" + ecpmInfo.getExpectedRevenue()
                        + ") is < " + globalRevenueFloor + "(globalRevenueFloor) and we dont serve ads which are under performing");
            }
            return false;
        }
        if (effectiveEcpmTargetRateCard == null && ecpmFloor == null) {
            // Its eligible
            return true;
        }

        if (ecpmFloor != null && ecpmInfo.getBidPrice() < ecpmFloor.doubleValue()) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.bidPriceLessThenFloorValue, "Bid Price(" + ecpmInfo.getBidPrice()
                        + ") is less than the floor value(" + ecpmFloor.doubleValue() + ") specified in request context");
            }
            return false;
        }

        if (effectiveEcpmTargetRateCard == null) {
            return true;
        }

        BigDecimal minimumBid = effectiveEcpmTargetRateCard.getMinimumBid(countryId);
        if (minimumBid == null) {
            //if no Minimum bid set then return true;
            return true;
        }
        //Now make sure this campaign can be served in this country on this publication based on ECPM
        if (ecpmInfo.getBidPrice() < minimumBid.doubleValue()) {
            //Check if this country's campaign ecpm value is less then
            //publication/publisher targeted ecpm value
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ecpmLessThenMinimumDefault,
                        "ECPM is less then minimum Default ECPM rate card for the Publication/Publisher");
            }
            return false;
        }
        return true;
    }

    /**
     * Encapsulation of allowing a creative through the targeting phase.
     */
    void addTargetedCreative(AdSpaceDto adspace, CreativeDto creative, FastLinkedList<MutableWeightedCreative> mwcs, FastLinkedList<MutableWeightedCreative> reusablePool,
            TargetingContext context, double ecpmWeight) {

        if (fineLogging) {
            LOG.fine("Creative is targeted: " + creative.getId() + ", ecpmWeight: " + ecpmWeight);
        }
        MutableWeightedCreative mwc;
        if (!reusablePool.isEmpty()) {
            mwc = reusablePool.remove(0);
            mwc.copyFrom(adspace, creative);
        } else {
            // The reusablePool is empty, so let's create one on the fly
            mwc = new MutableWeightedCreative(adspace, creative);
        }

        mwc.setEcpmWeight(ecpmWeight);
        // Add it to our list of targeted creatives
        mwcs.add(mwc);
    }

    private SelectedCreative selectFromTargeted(int priority, List<MutableWeightedCreative> mwcs, AdSpaceDto adSpace, TargetingContext context, boolean diagnosticMode,
            TimeLimit timeLimit, TargetingEventListener listener, SelectedCreative debuggedCreative) throws NoCreativesException {
        // We have some creatives left after the targeting process.
        // Pick one at weight-biased random and make sure it's not frequency capped.

        // The new method of doing this entails deriving an average
        // weight for each campaign, and doing weighted random selection
        // on a per-campaign basis first...then selected a weighted
        // random creative from that campaign.  So let's build the
        // list of AverageWeightedCampaign objects...
        // We're only using this map as a faster lookup while building the list

        //Get the Creative/Campaign Exponent
        double campaignExponent = 1.0;
        double creativeExponent = 1.0;
        AdserverDomainCache adserverCache = context.getAdserverDomainCache();
        if (adSpace.getPublication().getPublisher().isRtbEnabled()) {
            campaignExponent = adserverCache.getSystemVariableDoubleValue("rtb_campaign_exponent", 2.0);
            creativeExponent = adserverCache.getSystemVariableDoubleValue("rtb_creative_exponent", 2.0);
        } else {
            campaignExponent = adserverCache.getSystemVariableDoubleValue("campaign_exponent", 1.2);
            creativeExponent = adserverCache.getSystemVariableDoubleValue("creative_exponent", 1.2);
        }

        Map<Long, AverageWeightedCampaign> weightedCampaignsById = new HashMap<Long, AverageWeightedCampaign>();
        for (MutableWeightedCreative mwc : mwcs) {
            CampaignDto campaign = mwc.getCreative().getCampaign();
            AverageWeightedCampaign weightedCampaign = weightedCampaignsById.get(campaign.getId());
            if (weightedCampaign == null) {
                weightedCampaign = new AverageWeightedCampaign(campaign, creativeExponent);
                weightedCampaignsById.put(campaign.getId(), weightedCampaign);
            }
            weightedCampaign.addMutableWeightedCreative(mwc);
        }
        // Done building/adding...grab the weighted campaigns
        Collection<AverageWeightedCampaign> weightedCampaigns = weightedCampaignsById.values();

        if (fineLogging) {
            LOG.fine("Selecting from creatives: " + mwcs.size() + ", campaigns: " + weightedCampaigns.size());
        }

        boolean freqCappedAny = false;
        do {
            // Only proceed if we haven't blown through our time limit yet
            if (!checkTimeLimit(timeLimit, adSpace, context, listener)) {
                break;
            }

            // Pick a campaign first...
            AverageWeightedCampaign weightedCampaign = WeightedUtils.getRandomWeighted(weightedCampaigns, campaignExponent);
            if (fineLogging) {
                LOG.fine("Weighted random pick: campaign: " + weightedCampaign.getCampaign().getId() + " (weight=" + weightedCampaign.getWeight() + ")");
            }

            CampaignDto campaign = weightedCampaign.getCampaign();

            // Enforce Campaign.throttle, if it's less than 100
            if (campaign.getThrottle() < 100) {
                // Random.nextInt(100) returns a value between 0 and 99 (inclusive),
                // but we're looking for a random value between 1 and 100 (inclusive).
                // So instead of doing a +1 on it and using >, we'll just use >=.
                if (ThreadLocalRandom.getRandom().nextInt(100) >= campaign.getThrottle()) {
                    if (fineLogging) {
                        LOG.fine("Throttling campaign: " + campaign.getId() + " (throttle=" + campaign.getThrottle() + ")");
                    }
                    // This campaign is being throttled
                    weightedCampaigns.remove(weightedCampaign);
                    continue;
                }
            }

            // Pick the creative from that campaign's creatives...
            MutableWeightedCreative mwc = WeightedUtils.getRandomWeighted(weightedCampaign.getMutableWeightedCreatives(), creativeExponent);
            if (fineLogging) {
                LOG.fine("Weighted random pick: creative: " + mwc.getCreative().getId() + " (weight=" + mwc.getWeight() + ")");
            }

            // #654 - test mode bypasses the frequency cap
            // Also...don't bother doing frequency cap on backfill
            Integer effectiveMaxImpressions = frequencyCapper.getImpressionCapCount(campaign);
            Integer effectiveFreqCapPeriodSec = frequencyCapper.getImpressionCapPeriod(campaign);

            if (!context.isFlagTrue(Parameters.TEST_MODE) && !campaign.getAdvertiser().getCompany().isBackfill()) {
                // Enforce the frequency cap on each creative + user combo

                // <= 0 means no cap
                if (effectiveMaxImpressions > 0) {
                    int impressionCount;
                    long freqCapEntityId = (campaign.isCapPerCampaign() ? campaign.getId() : mwc.getCreative().getId());
                    FrequencyCounter.FrequencyEntity frequencyEntity = (campaign.isCapPerCampaign() ? FrequencyCounter.FrequencyEntity.CAMPAIGN
                            : FrequencyCounter.FrequencyEntity.CREATIVE);
                    if (mwc.getCreative().isPluginBased()) {
                        // For plugins we have to wait to do the increment, to make sure
                        // we actually have a successful response.  So for now just make
                        // sure we're under the cap.
                        impressionCount = frequencyCapper.getImpressionCount(context, freqCapEntityId, effectiveFreqCapPeriodSec, frequencyEntity);
                    } else {
                        impressionCount = frequencyCapper.incrementAndGetImpressionCount(context, freqCapEntityId, effectiveFreqCapPeriodSec, frequencyEntity);
                    }
                    if (impressionCount > effectiveMaxImpressions) {

                        if (fineLogging) {
                            LOG.fine("Frequency cap exceeded (" + impressionCount + " > " + effectiveMaxImpressions + "), discarding Creative");
                        }
                        weightedCampaign.removeMutableWeightedCreative(mwc);
                        if (weightedCampaign.isEmpty()) {
                            // No more creatives for this campaign, drop the campaign
                            weightedCampaigns.remove(weightedCampaign);
                        }
                        freqCappedAny = true;
                        if (listener != null) {
                            listener.creativeEliminated(adSpace, context, mwc.getCreative(), CreativeEliminatedReason.frequenyCapping, "Max Frequency cap reached");
                        }
                        frequencyCapper.decrementAndGetImpressionCount(context, freqCapEntityId, effectiveFreqCapPeriodSec, frequencyEntity);
                        continue;
                    }
                }
            }

            // If it's plugin-based, we'll need to resolve the proxied
            // destination by invoking the plugin...
            ProxiedDestination pd = null;
            if (mwc.getCreative().isPluginBased()) {
                PluginCreativeInfo pluginCreativeInfo = context.getAdserverDomainCache().getPluginCreativeInfo(mwc.getCreative());
                /*
                  if (domainMgr.isPluginDisabled(pluginCreativeInfo.getPluginName())) {
                  // The plugin has been disabled...can't pick this creative
                  weightedCampaign.removeMutableWeightedCreative(mwc);
                  if (weightedCampaign.isEmpty()) {
                  // No more creatives for this campaign, drop the campaign
                  weightedCampaigns.remove(weightedCampaign);
                  }
                  continue;
                  }
                */

                AdserverPluginDto adserverPlugin = context.getDomainCache().getAdserverPluginBySystemName(pluginCreativeInfo.getPluginName());
                if (adserverPlugin == null) {
                    // TODO: maybe call AdserverDomainCache.stopCampaign(), or maybe add a stopCreative()
                    // call on there, which would probably be better.  I think what really loads us up is
                    // all the logging more than anything else, so maybe just adding a little in-memory set
                    // of stuff we already warned about, and don't warn more than once.   The more I think
                    // about it, the less I want to do anything about this.  It seems like a rare scenario,
                    // especially now that we're well aware of it.
                    LOG.warning("Creative id=" + mwc.getCreative().getId() + " references non-existent AdserverPlugin.systemName=" + pluginCreativeInfo.getPluginName());
                    weightedCampaign.removeMutableWeightedCreative(mwc);
                    if (weightedCampaign.isEmpty()) {
                        // No more creatives for this campaign, drop the campaign
                        weightedCampaigns.remove(weightedCampaign);
                    }
                    continue;
                } else if (!adserverPlugin.isEnabled()) {
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("AdserverPluginDto " + adserverPlugin.getSystemName() + " is disabled");
                    }
                    weightedCampaign.removeMutableWeightedCreative(mwc);
                    if (weightedCampaign.isEmpty()) {
                        // No more creatives for this campaign, drop the campaign
                        weightedCampaigns.remove(weightedCampaign);
                    }
                    continue;
                } else if (timeLimit != null && adserverPlugin.getExpectedResponseTimeMillis() > timeLimit.getTimeLeft()) {
                    if (fineLogging) {
                        LOG.fine("AdserverPluginDto " + adserverPlugin.getSystemName() + " expected response time exceeds time left");
                    }
                    weightedCampaign.removeMutableWeightedCreative(mwc);
                    if (weightedCampaign.isEmpty()) {
                        // No more creatives for this campaign, drop the campaign
                        weightedCampaigns.remove(weightedCampaign);
                    }
                    continue;
                }

                Plugin plugin = pluginManager.getPluginByName(pluginCreativeInfo.getPluginName());
                if (plugin == null) {
                    LOG.warning("Creative id=" + mwc.getCreative().getId() + " references unknown plugin: " + pluginCreativeInfo.getPluginName());
                    weightedCampaign.removeMutableWeightedCreative(mwc);
                    if (weightedCampaign.isEmpty()) {
                        // No more creatives for this campaign, drop the campaign
                        weightedCampaigns.remove(weightedCampaign);
                    }
                    continue;
                }

                if (fineLogging) {
                    LOG.fine("Creative id=" + mwc.getCreative().getId() + " selected is plugin=" + pluginCreativeInfo.getPluginName());
                }
                if (diagnosticMode) {
                    // We can't invoke the plugin, since we're running in
                    // diagnostic mode.  We don't want our partners to see
                    // hits when we're simply running tests.  So in this case,
                    // we need to return a "fake" ProxiedDestination so that
                    // the system works normally...it's expecting a PD when
                    // the creative is plugin-based.
                    LOG.warning("Diagnostic mode, not invoking plugin " + pluginCreativeInfo.getPluginName());
                    pd = new DiagnosticProxiedDestination(context.getDomainCache().getFormatById(mwc.getCreative().getFormatId()).getSystemName());
                } else {
                    // Invoke the plugin
                    try {
                        pd = plugin.generateAd(adSpace, mwc.getCreative(), adserverPlugin, pluginCreativeInfo, context, timeLimit);

                        if (pluginFillRateTracker != null) {
                            // Notify the plugin fill rate tracker in case it's on
                            pluginFillRateTracker.trackOutcome(pluginCreativeInfo.getPluginName(), PluginFillRateTracker.Outcome.FILLED);
                        }

                        // Since frequency cap is a two-step process for plugin-based creatives,
                        // we need to do the second step now that the request was actually filled.
                        // See if we need to increment the impression count...
                        if (effectiveMaxImpressions != null && effectiveMaxImpressions > 0) {
                            // Yup, but we don't care about the return value, just increment
                            long freqCapEntityId = (campaign.isCapPerCampaign() ? campaign.getId() : mwc.getCreative().getId());
                            FrequencyCounter.FrequencyEntity frequencyEntity = (campaign.isCapPerCampaign() ? FrequencyCounter.FrequencyEntity.CAMPAIGN
                                    : FrequencyCounter.FrequencyEntity.CREATIVE);
                            frequencyCapper.incrementAndGetImpressionCount(context, freqCapEntityId, effectiveFreqCapPeriodSec, frequencyEntity);
                        }
                    } catch (Exception e) {
                        if (fineLogging) {
                            // Show the full stack trace if we're logging FINE
                            LOG.log(Level.FINE, "Plugin " + pluginCreativeInfo.getPluginName() + " failed, daisy chaining", e);
                        }

                        Throwable rootCause;
                        if (e.getCause() != null) {
                            // We don't always do this, since commons-lang builds a list
                            // as it goes in order to avoid recursion and all that...which
                            // is a "good thing" but has a cost.  So only bother digging
                            // for the unique root cause if there was a sub-cause at all.
                            rootCause = ExceptionUtils.getRootCause(e);
                        } else {
                            rootCause = e;
                        }

                        if (pluginFillRateTracker != null) {
                            // Notify the plugin fill rate tracker in case it's on
                            PluginFillRateTracker.Outcome outcome;
                            if (rootCause instanceof java.net.SocketTimeoutException) {
                                outcome = PluginFillRateTracker.Outcome.TIMEOUT;
                            } else {
                                outcome = PluginFillRateTracker.Outcome.UNFILLED;
                            }
                            pluginFillRateTracker.trackOutcome(pluginCreativeInfo.getPluginName(), outcome);
                        }

                        // The plugin failed...this creative is no good
                        weightedCampaign.removeMutableWeightedCreative(mwc);
                        if (weightedCampaign.isEmpty()) {
                            // No more creatives for this campaign, drop the campaign
                            weightedCampaigns.remove(weightedCampaign);
                        }
                        continue;
                    }
                }
            }

            if (debuggedCreative != null && !debuggedCreative.getCreative().getId().equals(mwc.getCreative().getId())) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, mwc.getCreative(), CreativeEliminatedReason.NotSelected, "Not preferred");
                }
                // Random picked another creative - remove it
                weightedCampaign.removeMutableWeightedCreative(mwc);
                if (weightedCampaign.isEmpty()) {
                    weightedCampaigns.remove(weightedCampaign);
                }
                continue;
            }

            // Good to go...return this creative, along with whatever proxied
            // destination its respective plugin may have generated
            return new SelectedCreative(mwc, pd);
        } while (!weightedCampaigns.isEmpty());

        if (debuggedCreative != null) {
            // It can happen that debugged creative is removed by frequency capping for example -> return it anyway
            return debuggedCreative;
        }

        if (freqCappedAny) {
            // We literally frequency capped every creative that was eligible
            // *after* segment/targeting filtering was done.  Distinguish this
            // unfilled reason from the regular "NO_CREATIVES" case.
            throw new NoCreativesException(UnfilledReason.FREQUENCY_CAP);
        } else if (!checkTimeLimit(timeLimit, adSpace, context, listener)) {
            // The time limit expired...distinguish this case from the normal
            // "no creatives" case, since there very well may have been some
            throw new NoCreativesException(UnfilledReason.TIMEOUT);
        } else {
            // This would be the case when we were only left with plugin-based
            // creatives, and they all were (or the only one was) eliminated
            // due to the plugin being disabled.  This should be treated the
            // same as if there were no creatives eligible after targeting,
            // since that's really the case.
            throw new NoCreativesException(UnfilledReason.NO_CREATIVES);
        }
    }

    public static final class NoCreativesException extends Exception {
        private static final long serialVersionUID = 1L;

        /**
         * Default behaviour is to skip stack trace creation.
         * For debuging purposes here goes way to reenable it using System property
         */
        private static final boolean stacktrace = Boolean.valueOf(System.getProperty("NoCreativesException.stacktrace"));

        private final UnfilledReason unfilledReason;

        private NoCreativesException(UnfilledReason unfilledReason) {
            this.unfilledReason = unfilledReason;
        }

        public UnfilledReason getUnfilledReason() {
            return unfilledReason;
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

    /**
     * Get or create a pool of MutableWeightedCreative objects that will be reused
     * from request to request.
     */
    private static FastLinkedList<MutableWeightedCreative> acquireReusablePool() {
        FastLinkedList<MutableWeightedCreative> pool = REUSABLE_POOLS.poll();
        if (pool == null) {
            pool = new FastLinkedList<MutableWeightedCreative>();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Creating new reusable pool instance");
            }
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Acquired reusable pool instance with size=" + pool.size());
            }
        }
        return pool;
    }

    /**
     * Release a pool of MutableWeightedCreative objects for reuse
     */
    private static void releaseReusablePool(FastLinkedList<MutableWeightedCreative> pool) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Releasing reusable pool instance with size=" + pool.size());
        }

        // Add it back to our available list of pools
        REUSABLE_POOLS.add(pool);
    }

    /**
     * Make sure that if there's a time limit, it hasn't expired yet
     */
    static boolean checkTimeLimit(TimeLimit timeLimit, AdSpaceDto adSpace, TargetingContext context, TargetingEventListener listener) {
        if (timeLimit != null && timeLimit.hasExpired()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("TimeLimit(" + timeLimit.getDuration() + ") expired");
            }
            if (listener != null) {
                listener.timeLimitExpired(adSpace, context, timeLimit);
            }
            return false;
        }
        return true;
    }

    boolean checkActive(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        if (!Creative.Status.ACTIVE.equals(statusChangeManager.getStatus(creative))) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.creativeStatusChanged, "creative.statusChange");
            }
            return false;
        } else if (!Campaign.Status.ACTIVE.equals(statusChangeManager.getStatus(creative.getCampaign()))) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.campaignStatusChanged, "campaign.statusChange");
            }
            return false;
        } else {
            return true;
        }
    }

    static boolean checkSegmentCountries(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, CountryDto country, TargetingEventListener listener) {
        if (segment.getCountryIds().isEmpty()) {
            return true; // No country targeting
        }
        if (country == null) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.countryNotPresent, "!country");
            }
            return false;
        }
        if (segment.getCountryListIsWhitelist()) {
            // Whitelist: if countries is non-empty the country must be in it
            if (!segment.getCountryIds().contains(country.getId())) {
                // CountryDto isn't supported
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.countryNotWhiteListed, "country !whitelisted");
                }
                return false;
            }
        } else {
            // Otherwise it's a blacklist
            if (segment.getCountryIds().contains(country.getId())) {
                // CountryDto isn't supported
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.countryBlackListed, "country blacklisted");
                }
                return false;
            }
        }

        return true;
    }

    static boolean checkPmpDeals(AdSpaceDto adSpace, CreativeDto creative, double creativeBidPrice, TargetingContext context, TargetingEventListener listener) {
        // Here we set TargetingContext.DEAL_FLOOR_EXISTS and then check that value in other function where we do other floor checks.
        // So we must make sure checkSeatAndDeals always called before isEligibleBasedOnMinimumTargetedEcpmRateCardOfPublicationPublisher

        ByydMarketPlace bidPmp = context.getAttribute(TargetingContext.RTB_PMP);
        PrivateMarketPlaceDealDto campaignDeal = creative.getCampaign().getPrivateMarketPlaceDeal();
        if (campaignDeal == null) {
            if (bidPmp == null || !bidPmp.isPrivateDeal()) {
                // Non PMP Campaign can bid if Bid PMP is public (not private) 
                return true;
            } else {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.PmpDealMismatch, "Bid private PMP vs Campaign non PMP");
                }
                return false;
            }
        } else {
            if (bidPmp == null) {
                // Maybe we can introduce some flag here, that would alow PMP campaign to bid on non PMP request
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.PmpDealMismatch, "Bid non PMP vs Campaign is PMP");
                }
                return false;
            } else if (!campaignDeal.getPublisherId().equals(adSpace.getPublication().getPublisher().getId())) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.PmpDealMismatch, "Bid PMP exchange: "
                            + adSpace.getPublication().getPublisher().getId() + " vs Campaign PMP exchange: " + campaignDeal.getPublisherId());
                }
                return false;
            }
        }
        // We eliminated PMP vs non PMP bids vs campaigns. Now check the Deals
        CreativeBidDeal matchingDeal = null;
        String advertiserSeatId = creative.getCampaign().getAdvertiser().getPmpBidSeat().getSeatId();
        for (ByydDeal bidDeal : bidPmp.getDeals()) {
            if (campaignDeal.getDealId().equals(bidDeal.getId())) { // Deal ID match
                // Seat in PMP Deal is optional, but must be respected when present
                String matchingSeat = null;
                boolean seatIdOk = false;
                if (bidDeal.getSeats() == null) {
                    matchingSeat = null;
                    seatIdOk = true;
                } else if (bidDeal.getSeats().contains(advertiserSeatId)) {
                    matchingSeat = advertiserSeatId;
                    seatIdOk = true;
                }
                if (seatIdOk) {
                    if (bidDeal.getBidFloor() == null) {
                        // We have to believe that Campaing PMP setup price is correct
                        matchingDeal = new CreativeBidDeal(bidDeal.getId(), matchingSeat);
                        break;
                    } else if (creativeBidPrice >= bidDeal.getBidFloor()) {
                        context.setAttribute(TargetingContext.DEAL_FLOOR_EXISTS, Boolean.TRUE);
                        matchingDeal = new CreativeBidDeal(bidDeal.getId(), matchingSeat);
                        break;
                    } else {
                        // This is probably Campaign PMP setup mistake 
                        if (listener != null) {
                            listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.PmpDealMismatch, "Bid PMP Deal " + bidDeal.getId() + " bid floor "
                                    + bidDeal.getBidFloor() + " > ecmp price " + creativeBidPrice);
                        }
                        return false;
                    }
                } // else let it go on for the case with two deals with same name but different seat list. Don't let bidfloor be distinguishing prop
            }
        }
        if (matchingDeal != null) {
            // For RTB response, we need to track if creative is bidding using PMP Deal and Seat (or just public PMP bid)
            Map<Long, CreativeBidDeal> creativeDeals = context.getAttribute(TargetingContext.PMP_CREATIVES_DEALS);
            if (creativeDeals == null) {
                creativeDeals = new HashMap<Long, CreativeBidDeal>();
                context.setAttribute(TargetingContext.PMP_CREATIVES_DEALS, creativeDeals);
            }
            creativeDeals.put(creative.getId(), matchingDeal);
            return true;
        } else {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.PmpDealMismatch, "Campaign Deal/Seat " + campaignDeal.getDealId() + "/"
                        + advertiserSeatId + " vs " + bidPmp);
            }
            return false;
        }
    }

    boolean checkStoppages(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        // NOTE: this also checks for campaign stoppages, so one call does it all.
        // TODO: when we optimize by campaign, this call should change.
        CampaignDto campaign = creative.getCampaign();
        if (stoppageManager.isCampaignStopped(campaign)) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.creativeOrAdvertiserStopped, "Campaign stoppage");
            }
            return false;
        }
        if (stoppageManager.isAdvertiserStopped(campaign.getAdvertiser())) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.creativeOrAdvertiserStopped, "Advertiser stoppage");
            }
            return false;
        }

        return true;
    }

    /**
     * Make sure the creative's endDate, if specified, hasn't passed
     */
    static boolean checkEndDate(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        if (creative.getEndDate() != null && creative.getEndDate().getTime() <= System.currentTimeMillis()) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.creativeEndDateExpired, "Creative endDate " + creative.getEndDate());
            }
            return false;
        }
        return true;
    }

    /**
     * Make sure the creative's format is allowed, in case the caller
     * requested specific format(s).
     */
    static boolean checkAllowedFormats(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, Collection<Long> allowedFormatIds, TargetingEventListener listener) {
        if (CollectionUtils.isNotEmpty(allowedFormatIds) && !allowedFormatIds.contains(creative.getFormatId())) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.formatNotAllowed, "Format " + creative.getFormatId() + " not in "
                        + allowedFormatIds);
            }
            return false;
        }
        return true;
    }

    boolean checkCreativeAssetSize(TargetingContext context, CreativeDto creative, ByydImp byydImp, TargetingEventListener listener) {
        DomainCache cache = context.getDomainCache();
        FormatDto formatDto = cache.getFormatById(creative.getFormatId());

        //We only check size for standard banners
        if (SystemName.FORMAT_BANNER.equals(formatDto.getSystemName())) {
            if (byydImp != null) {
                Integer impWidth = byydImp.getW();
                Integer impHeight = byydImp.getH();
                if (impWidth != null && impHeight != null) {
                    String[] dtSystemNames = DisplayTypeUtilsImpl.getBannerDisplayTypeSystemName(impWidth, impHeight);
                    for (String dtSystemName : dtSystemNames) {
                        DisplayTypeDto displayType = cache.getDisplayTypeBySystemName(dtSystemName);
                        ComponentDto componentDto = cache.getComponentByFormatAndSystemName(formatDto, SystemName.COMPONENT_IMAGE);
                        AssetDto asset = creative.getAsset(displayType.getId(), componentDto.getId());
                        if (asset != null) {
                            return true; // ok we have asset of fitting size
                        }
                    }

                    if (listener != null) {
                        listener.creativeEliminated(context.getAdSpace(), context, creative, CreativeEliminatedReason.formatNotAllowed, "Bid Imp " + impWidth + "x" + impHeight
                                + " vs no Asset with banner DisplayType: " + Arrays.asList(dtSystemNames));
                    }
                    return false;
                    /*
                    DisplayTypeDto displayType = displayTypeUtils.getDisplayType(formatDto, context, true);
                    ContentSpecDto contentSpec = componentDto.getContentSpec(displayType);
                    if (displayType != null && contentSpec != null) {
                        Map<String, String> manifestProperties = contentSpec.getManifestProperties();
                        Integer creativeWidth = parseSize(manifestProperties.get(ContentSpecDto.CONTENT_SPEC_MANIFEST_WIDTH));
                        Integer creativeHeight = parseSize(manifestProperties.get(ContentSpecDto.CONTENT_SPEC_MANIFEST_HEIGHT));
                        // We shouldn't serve an ad bigger than the width or height in the request
                        if (impWidth.compareTo(creativeWidth) < 0 || impHeight.compareTo(creativeHeight) < 0) {
                            isSmall = false;
                            if (listener != null) {
                                listener.creativeEliminated(context.getAdSpace(), context, creative, CreativeEliminatedReason.formatNotAllowed, "Banner " + creativeWidth + "x"
                                        + creativeHeight + " > Slot " + impWidth + "x" + impHeight);
                            }
                        }
                    }
                    */
                }
            }
            return true;
        } else {
            return true;
        }
    }

    boolean checkVideoDuration(AdSpaceDto adSpace, CreativeDto creative, ByydImp imp, TargetingContext context, TargetingEventListener listener) {
        String sDuration = creative.getExtendedData().get("duration");
        if (StringUtils.isNotEmpty(sDuration)) {
            int duration = Integer.parseInt(sDuration);
            Integer minduration = imp.getMinduration();
            Integer maxduration = imp.getMaxduration();
            if ((minduration == null || duration >= minduration) && (maxduration == null || duration <= maxduration)) {
                return true;
            } else {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.formatNotAllowed, "Video duration " + duration + " outside of <" + minduration
                            + "," + maxduration + ">");
                }
            }
        } else {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.formatNotAllowed, "Video duration not in ExtendedData");
            }
        }
        return false;
    }

    /**
     * Bounce any creative whose destination has a beaconUrl if the
     * integration type doesn't support beacons.
     */
    static boolean checkDestinationBeaconUrl(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        if (CollectionUtils.isNotEmpty(creative.getDestination().getBeaconUrls()) && !context.getAttribute(TargetingContext.IS_NATIVE, Boolean.class)
                && !context.getAttribute(TargetingContext.USE_BEACONS, Boolean.class)) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.beaconsNotPresent, "!beacons");
            }
            return false;
        }
        return true;
    }

    static CreativeEliminatedReason checkGenderAndAge(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, Gender gender,
            Range<Integer> ageRange, TargetingEventListener listener) {
        // Factor in the viewer's gender, if we know it
        if (gender != null && segment.getGenderMix() != null) {
            double genderMix = segment.getGenderMix().doubleValue();
            double genderDelta = Math.abs(gender.getMixValue() - genderMix);
            if (genderDelta == 1.0) {
                // Disallow the ad if the gender & genderMix are
                // complete mismatches.
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.GenderMismatch, "genderDelta==1.0");
                }
                return CreativeEliminatedReason.GenderMismatch;
            }
        }

        // Factor in the viewer's age, if we know it
        if (ageRange != null) {
            // Disallow the ad if the age ranges are mutually exclusive
            if (segment.getMaxAge() < ageRange.getStart() || segment.getMinAge() > ageRange.getEnd()) {
                // Age ranges don't work
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.AgeRangeMismatch, "ageRange");
                }
                return CreativeEliminatedReason.AgeRangeMismatch;
            }
        }
        return null;
    }

    static CreativeEliminatedReason checkIsSegmentTimeTargetted(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, OperatorDto operator,
            CountryDto country, Date now, TargetingEventListener listener) {
        if (!segment.isEveryDayEveryHourTargeted()) {
            boolean resolvedTimeZone = false;
            TimeZone timeZone = null;
            // See if we can determine a discrete TimeZone based on the
            // user's location...but we only bother doing this for
            // non-operator requests (operator IPs just use country)
            if (operator == null) {
                if (!resolvedTimeZone) {
                    timeZone = context.getAttribute(TargetingContext.TIME_ZONE);
                    resolvedTimeZone = true; // even if it's null
                }
            }

            if (now == null) {
                now = new Date();
            }

            if (timeZone != null) {
                if (!segment.isTimeEnabled(timeZone, now)) {
                    // It's not a valid time of day right now
                    if (listener != null) {
                        listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.differentTimeOfTheDay, "!isTimeEnabled(tz=" + timeZone.getID() + ")");
                    }
                    return CreativeEliminatedReason.differentTimeOfTheDay;
                }
            } else if (!segment.isTimeEnabled(country == null ? null : country.getIsoCode(), now)) {
                // It's not a valid time of day right now
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.differentTimeOfTheDay, "!isTimeEnabled(country="
                            + (country == null ? "null" : country.getIsoCode()) + ")");
                }
                return CreativeEliminatedReason.differentTimeOfTheDay;
            }
        }
        return null;
    }

    /**
     * Site or Application
     */
    static CreativeEliminatedReason checkMedium(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, SegmentDto segment, Medium medium,
            TargetingEventListener listener) {
        Medium targetedMedium = segment.getMedium();
        if (targetedMedium != null && targetedMedium != medium) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.SiteAppMismatch, "medium!=segment.getMedium()");
            }
            return CreativeEliminatedReason.SiteAppMismatch;
        }
        return null;
    }

    static boolean checkBlockedDestinationAttributes(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        Set<DestinationType> destinationTypes = context.getAttribute(TargetingContext.BLOCKED_DESTINATION_TYPES);
        Set<BidType> bidTypes = context.getAttribute(TargetingContext.BLOCKED_BID_TYPES);
        if (destinationTypes != null && destinationTypes.contains(creative.getDestination().getDestinationType())) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.blockedDestinationType,
                        "destinationTypes.contains(creative.getDestination().getDestinationType()");
            }
            return false;
        }

        CampaignBidDto currentBid;// for AF-629
        if (bidTypes != null && (currentBid = creative.getCampaign().getCurrentBid()) != null && bidTypes.contains(currentBid.getBidType())) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.blockedBidType, "Bid block BidTypes: " + bidTypes + " vs Campaign BidType: "
                        + currentBid.getBidType());
            }
            return false;
        }

        return true;
    }

    static boolean checkBlockedCreativeAttributes(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        Set<Integer> blockedCreativeAttributes = context.getAttribute(TargetingContext.BLOCKED_CREATIVE_ATTRIBUTES);

        if (blockedCreativeAttributes == null || Collections.disjoint(blockedCreativeAttributes, creative.getCreativeAttributes())) {
            return true;
        }

        if (listener != null) {
            listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.blockedCreativeAttributes, "Bid block attributes: " + blockedCreativeAttributes
                    + " vs Creative " + creative.getCreativeAttributes());
        }
        return false;
    }

    // ----------------

    static boolean checkNative(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        // This will tell us if the request is native
        boolean isNativeRequest = context.getAttribute(TargetingContext.IS_NATIVE, Boolean.class);
        boolean isNativeCreative = context.getDomainCache().getFormatById(creative.getFormatId()).getSystemName().equals(SystemName.FORMAT_NATIVE);

        if (isNativeRequest && !isNativeCreative) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.RequestOrCreativeNotNative, "Creative not Native");
            }
            return false;
        }

        if (!isNativeRequest && isNativeCreative) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.RequestOrCreativeNotNative, "Request not Native");
            }
            return false;
        }

        // OpenRtb 2.3 bid request with native details
        NativeAdRequest nativeAdRequest = context.getAttribute(TargetingContext.NATIVE_REQUEST);
        if (nativeAdRequest != null && !checkNativeAssets(nativeAdRequest, adSpace, creative, context, listener)) {
            return false;
        }
        return true;
    }

    /**
     * We need to check if creative has all required assets
     */
    static boolean checkNativeAssets(NativeAdRequest nativeAdRequest, AdSpaceDto adSpace, CreativeDto creative, TargetingContext context, TargetingEventListener listener) {

        DomainCache domainCache = context.getDomainCache();
        FormatDto nativeFormat = domainCache.getFormatBySystemName(SystemName.FORMAT_NATIVE);
        DisplayTypeDto genericDisplayType = domainCache.getDisplayTypeBySystemName(SystemName.DISPLAY_TYPE_GENERIC);

        for (NativeAdRequestAsset nativeAdAsset : nativeAdRequest.getAssets()) {
            if (Constant.ONE.equals(nativeAdAsset.getRequired())) {
                ImageAsset imageAsset = nativeAdAsset.getImg();
                DataAsset dataAsset = nativeAdAsset.getData();
                TitleAsset titleAsset = nativeAdAsset.getTitle();
                // VideoAsset videoAsset = requestAsset.getVideo(); // Do not even try as we have no video in native supported at all...
                boolean assetValid = false;
                if (titleAsset != null) {
                    String title = creative.getExtendedData().get("title");
                    assetValid = title != null && title.length() <= nativeAdAsset.getTitle().getLen();
                } else if (imageAsset != null) {
                    ImageAssetType type = ImageAssetType.valueOf(imageAsset.getType());
                    if (type == ImageAssetType.Icon) {
                        ComponentDto componentIcon = domainCache.getComponentByFormatAndSystemName(nativeFormat, SystemName.COMPONENT_APP_ICON);
                        ContentSpecDto contentSpec = componentIcon.getContentSpec(genericDisplayType);
                        AssetDto creativeAsset = creative.getAsset(genericDisplayType.getId(), componentIcon.getId());
                        assetValid = creativeAsset != null && checkNativeImageSize(contentSpec, nativeAdAsset.getImg());
                    } else if (type == ImageAssetType.Main) {
                        ComponentDto componentImage = domainCache.getComponentByFormatAndSystemName(nativeFormat, SystemName.COMPONENT_IMAGE);
                        ContentSpecDto contentSpec = componentImage.getContentSpec(genericDisplayType);
                        AssetDto creativeAsset = creative.getAsset(genericDisplayType.getId(), componentImage.getId());
                        assetValid = creativeAsset != null && checkNativeImageSize(contentSpec, nativeAdAsset.getImg());
                    } else {
                        // unrecognized ImageAssetType imageAsset.getType()
                    }

                } else if (dataAsset != null) {
                    DataAssetType type = DataAssetType.valueOf(dataAsset.getType());
                    Integer len = dataAsset.getLen();
                    if (type == DataAssetType.ctatext) {
                        String value = creative.getExtendedData().get("click_to_action");
                        assetValid = value != null && (len == null ? true : value.length() <= len.intValue());
                    } else if (type == DataAssetType.desc) {
                        String value = creative.getExtendedData().get("description");
                        assetValid = value != null && (len == null ? true : value.length() <= len.intValue());
                    } else {
                        // unrecognized DataAssetType dataAsset.getType()
                    }

                } else {
                    // unrecognized nativeAdAsset. Likely a video
                }

                if (assetValid == false) {
                    if (listener != null) {
                        listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoAssetBundle,
                                "Missing or missized native asset for " + nativeAdAsset.getId());
                    }
                    return false;
                }
            }
        }
        return true;
    }

    static boolean checkNativeImageSize(ContentSpecDto contentSpec, NativeAdRequestAsset.ImageAsset nativeAdImage) {
        String assetWidth = contentSpec.getManifestProperties().get(SystemName.CONTENT_SPEC_WIDTH);
        int aw = Integer.parseInt(assetWidth);
        Integer nw = nativeAdImage.getW();
        if (nw != null && nw != aw) {
            return false;
        }
        Integer nwmin = nativeAdImage.getWmin();
        if (nwmin != null && nwmin > aw) {
            return false;
        }

        String assetHeight = contentSpec.getManifestProperties().get(SystemName.CONTENT_SPEC_HEIGHT);
        int ah = Integer.parseInt(assetHeight);
        Integer nh = nativeAdImage.getH();
        if (nh != null && nh != ah) {
            return false;
        }
        Integer nhmin = nativeAdImage.getHmin();
        if (nhmin != null && nhmin > ah) {
            return false;
        }
        return true;
    }

    static boolean checkPluginBased(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, ModelDto model, TargetingEventListener listener) {
        if (creative.isPluginBased()) {
            /*
             * No longer need to do this check, since we're bailing on private network
             * traffic at the very start of targeting now.
             // #1125 - don't serve plugin ads to private network IPs
            if (Boolean.TRUE.equals(context.getAttribute(TargetingContext.IS_PRIVATE_NETWORK, Boolean.class))) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, wc, "private network");
                }
                return false;
            }
            */

            // Can't backfill without a known model per Adfonic policy
            if (model == null) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.modelNotPresentForBackfill, "backfill needs model");
                }
                return false;
            }

            if (context.getAttribute(TargetingContext.BLOCK_PLUGINS) != null) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.blockedPlugin, "plugins blocked");
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Make sure the campaign is currently active...this takes the
     * new "advanced scheduling" concepts into account.
     */
    static boolean checkAdvancedScheduling(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        CampaignDto campaign = creative.getCampaign();
        if (!campaign.isCurrentlyActive()) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.campaignNotCurrentlyActive, "Campaign not running: " + campaign.getStatus() + ", "
                        + campaign.getSortedTimePeriods());
            }
            return false;
        }
        return true;
    }

    /**
     * #799 - Special handling for Android market URLs.  We only
     * allow "market:" style destination URLs for publications whose
     * publicationType.medium is APPLICATION (i.e. not SITE).
     */
    static boolean checkAndroidMarketMedium(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, Medium medium, TargetingEventListener listener) {
        if (medium != Medium.APPLICATION) {
            String destinationData = creative.getDestination().getData();
            if (destinationData != null && destinationData.startsWith("market:")) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.marketUrlForNotApplication, "market:");
                }
                return false;
            }
        }
        return true;
    }

    /**
     * If any categories have been blocked, make sure this campaign doesn't
     * have any of them associated directly.
     */
    static boolean checkBlockedCategories(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        Set<Long> blockedCategoryIds = context.getAttribute(TargetingContext.BLOCKED_CATEGORY_IDS);
        if (blockedCategoryIds != null && blockedCategoryIds.contains(creative.getCampaign().getCategoryId())) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.blockedCampaignCategory, "Bid block categories: " + blockedCategoryIds
                        + " vs Campaign category: " + creative.getCampaign().getCategoryId());
            }
            return false;
        }
        return true;
    }

    /**
     * Derive a "language quality" value from 0.0 (unacceptable) to 1.0 (preferred).
     * By default we'll assume this creative's language is just fine.  We do that
     * so that campaign's that DON'T care about language don't get penalized relative
     * to those campaigns that do.
     */
    @SuppressWarnings("unchecked")
    static Double checkLanguage(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        LanguageDto creativeLang = null;

        // Make sure the creative's language hasn't specifically been blocked
        Set<String> blockedLanguageIsoCodes = context.getAttribute(TargetingContext.BLOCKED_LANGUAGE_ISO_CODES, Set.class);
        if (CollectionUtils.isNotEmpty(blockedLanguageIsoCodes)) {
            creativeLang = context.getDomainCache().getLanguageById(creative.getLanguageId());
            if (blockedLanguageIsoCodes.contains(creativeLang.getISOCode())) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.LanguageBlocked, "Bid block language: " + creativeLang.getISOCode());
                }
                return null;
            }
        }

        boolean langMatchDisabled = creative.getCampaign().getDisableLanguageMatch();
        if (langMatchDisabled) {
            return 1.0;
        }

        // #631 - Apply new language match logic
        // Grab the languages that the end user accepts (no worries, this is derive-once)
        AcceptedLanguages bidAcceptedLangs = context.getAttribute(TargetingContext.ACCEPTED_LANGUAGES);
        if (bidAcceptedLangs != null) {
            // The user accepts only certain languages.  Honor them and derive
            // a "quality" value for this creative's language.  A value of 0.0
            // means unacceptable, and this creative should be eliminated.
            // Otherwise the quality will be somewhere >0.0 and <=1.0, and the
            // creative should be weighted accordingly.  NOTE: it's not expensive
            // to call getQuality over and over for the same language, i.e. in
            // this creatives loop, where many creatives probably have the same
            // language, since the quality gets cached by language in the
            // AcceptedLanguages instance.
            if (creativeLang == null) {
                creativeLang = context.getDomainCache().getLanguageById(creative.getLanguageId());
            }
            double languageQuality = bidAcceptedLangs.getQuality(creativeLang.getISOCode());
            if (languageQuality <= 0.0) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.differentUserLanguage, "Bid accepted language: " + bidAcceptedLangs + " vs "
                            + creativeLang.getISOCode());
                }
                return null;
            } else {
                return languageQuality;
            }
        }

        // For the purposes of enforcing language match logic, we consider
        // "none" (not specified) to be the same as "any".  In this case,
        // the user didn't specify, so anything goes...but at least make
        // sure the publication allows this creative's language.
        Set<Long> pubLangIds = adSpace.getPublication().getLanguageIds();
        if (!pubLangIds.isEmpty() && !pubLangIds.contains(creative.getLanguageId())) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.differentPubblicationLanguage, "Languages of Publication: " + pubLangIds
                        + " vs Creative: " + creative.getLanguageId());
            }
            return null;
        } else {
            return 1.0;
        }
    }

    boolean checkDisplayTypeAndAssetBundle(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, final boolean strictlyUseFirstDisplayType,
            TargetingEventListener listener) {
        // Make sure the creative has an AssetBundle for the right
        // DisplayTypeDto based on the creative's Format.
        FormatDto format = context.getDomainCache().getFormatById(creative.getFormatId());
        DisplayTypeDto displayType = displayTypeUtils.getDisplayType(format, context);
        if (displayType == null) {
            // DisplayTypeDto not known for this creative's format
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.displayTypeNotPresent, "No DisplayType");
            }
            return false;
        }

        List<String> mimeTypes = context.getAttribute(TargetingContext.MIME_TYPE_WHITELIST);

        //List<String> failedDisplayTypes = new ArrayList<String>();
        boolean assetFound = false;
        for (DisplayTypeDto displayTypeDto : displayTypeUtils.getAllDisplayTypes(format, context)) {
            if (creative.hasAssets(displayTypeDto.getId(), mimeTypes, null, context.getDomainCache())) {
                displayTypeUtils.setDisplayType(format, context, displayTypeDto);
                assetFound = true; // this is found mark
                break;
            } else if (strictlyUseFirstDisplayType) {
                break;
            }
            //failedDisplayTypes.add(displayTypeDto.getSystemName());
        }

        if (!assetFound) {
            // Nope, it doesn't have the AssetBundle we would need
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoAssetBundle,
                        "No Asset for Format: " + format.getId() + "-" + format.getSystemName() + " DisplayType: " + displayType.getId() + "-" + displayType.getSystemName()
                                + " MimeTypes: " + mimeTypes);
            }
        }

        return assetFound;
    }

    /**
     * Apply extended capabilities rules as needed
     */
    public static boolean checkExtendedCapabilities(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        if (creative.getExtendedCreativeTypeId() == null) {
            return true; // Not extended? Just leave!
        }

        if (Boolean.TRUE.equals(context.getAttribute(TargetingContext.BLOCK_EXTENDED_CREATIVES))) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.ExtendedCreativeBlocked, "Block any ExtendedCreativeType");
            }
            return false;
        }

        // There must be a derived IntegrationType, otherwise the creative isn't eligible
        IntegrationTypeDto integrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE);
        if (integrationType == null) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.NoIntegrationType, "ExtendedCreativeType but not IntegrationType");
            }
            return false;
        }

        ExtendedCreativeTypeDto creativeExtendedType = context.getDomainCache().getExtendedCreativeTypeById(creative.getExtendedCreativeTypeId());

        // If the MediaType requires markup, make sure it's available
        if (creativeExtendedType.getMediaType().isMarkupRequired() && !context.getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class)) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.notAvailableMediaType, "MediaType." + creativeExtendedType.getMediaType()
                        + " requires markup");
            }
            return false;
        }

        Set<ContentForm> bidContentForms = context.getAttribute(TargetingContext.CONTENT_FORM_RESTRICTION_SET);

        if (creativeExtendedType.getUseDynamicTemplates()) {
            String dynamicExtendedTemplate = ExtendedCapabilitiesUtils.getDynamicTemplate(creative.getExtendedCreativeTemplates(), creativeExtendedType, integrationType,
                    bidContentForms, null);
            if (dynamicExtendedTemplate == null) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.missingContentForm,
                            "!dynamic ExtendedCreativeType: " + creativeExtendedType.getId() + "/" + creativeExtendedType.getMediaType() + ", Templates: "
                                    + creative.getExtendedCreativeTemplates().keySet() + " vs IntegrationType: " + integrationType.getId() + "/"
                                    + integrationType.getContentFormsByMediaType().keySet() + ", BidContentForms: " + bidContentForms);
                }
                return false;
            }
        } else {
            String staticTemplateName = ExtendedCapabilitiesUtils.getTransformTemplate(creativeExtendedType, integrationType, bidContentForms, null);
            if (staticTemplateName == null) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.missingContentForm,
                            "!static ExtendedCreativeType: " + creativeExtendedType.getId() + "/" + creativeExtendedType.getMediaType() + ", Templates: "
                                    + creativeExtendedType.getTemplateMap().keySet() + " vs IntegrationType: " + integrationType.getId() + ", BidContentForms: " + bidContentForms);
                }
                return false;
            }
        }
        /*
        // Make sure the IntegrationTypeDto supports the given MediaType.  Specifically,
        // ensure that there's at least one ContentForm supported by the IntegrationType
        // for the given MediaType.  i.e. we need to be able to transform this creative.
        if (!ExtendedCapabilitiesUtils.canTransform(creative.getExtendedCreativeTemplates(), creativeExtendedType, integrationType, bidContentForms)) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.missingContentForm, "!transforms ExtendedType: " + creativeExtendedType.getId()
                        + " IntegrationType: " + integrationType.getId() + " RestrictToContentForms: " + bidContentForms);
            }
            return false;
        }
        */
        Set<String> bidBlockedExtTypes = context.getAttribute(TargetingContext.BLOCKED_EXT_CRT_TYP_SET);
        if (bidBlockedExtTypes != null && bidBlockedExtTypes.contains(creativeExtendedType.getName())) {
            if (listener != null) {
                listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.blockedExtendedCreativeType, "Block specific ExtendedCreativeType "
                        + bidBlockedExtTypes);
            }
            return false;
        }

        if (creativeExtendedType.getFeatures().contains(Feature.BEACON)) {
            // The creative requires beacon support.  Make sure we can actually serve
            // beacons.  Specifically, ensure that if the IntegrationTypeDto only supports
            // BeaconMode.MARKUP (as opposed to BeaconMode.METADATA instead or as well),
            // markup must be available.
            boolean markupRequired = integrationType.getSupportedBeaconModes().size() == 1 && integrationType.getSupportedBeaconModes().contains(BeaconMode.MARKUP);
            if (markupRequired && !context.getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class)) {
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.noSupportForBeacon,
                            "extended.features(BEACON) and IntegrationTypeDto requires markup");
                }
                return false;
            }
        }

        // Derive the set of accepted features from the IntegrationType, factoring in
        // any excluded features (i.e. via t.exclude=...).  Ensure that each of the
        // features of the extended creative type is accepted.
        Set<Feature> acceptedFeatures = context.getAttribute(TargetingContext.ACCEPTED_FEATURES);
        for (Feature feature : creativeExtendedType.getFeatures()) {
            if (!acceptedFeatures.contains(feature)) {
                // Nope, this feature of the creative isn't accepted...not eligible
                if (listener != null) {
                    listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.notAcceptedFeature, "!accepted(" + feature + ")");
                }
                return false;
            }
        }

        return true;
    }

    /**
     * If there are any blocked advertiser domains, make sure this
     * creative's destination hostname doesn't match any of them.
     */
    static boolean checkBlockedAdvertiserDomains(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, TargetingEventListener listener) {
        String advertiserDomain = creative.getCampaign().getAdvertiserDomain();
        if (StringUtils.isNotBlank(advertiserDomain)) {
            Set<String> blockedAdvertiserDomains = context.getAttribute(TargetingContext.BLOCKED_ADVERTISER_DOMAINS);
            if (blockedAdvertiserDomains != null) {
                for (String blockedDomain : blockedAdvertiserDomains) {
                    if (advertiserDomain.equals(blockedDomain) || advertiserDomain.endsWith("." + blockedDomain)) {
                        if (listener != null) {
                            listener.creativeEliminated(adSpace, context, creative, CreativeEliminatedReason.DomainBlocked, "domain blocked");
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * This is a "fake" ProxiedDestination that we return in the case where
     * we selected a creative where pluginBased=true, but we don't want to
     * invoke the plugin since we're in diagnosticMode=true (don't want to
     * induce real hits on our partners' sites when we're just testing).
     */
    private static final class DiagnosticProxiedDestination extends AbstractAdComponents implements ProxiedDestination {
        private static final long serialVersionUID = 1L;

        private DiagnosticProxiedDestination(String formatSystemName) {
            setFormat(formatSystemName);
            setDestinationType(DestinationType.URL);
            setDestinationUrl("http://diagnostic");

            Map<String, String> component = new LinkedHashMap<String, String>();
            component.put("content", "Diagnostic Text");
            getComponents().put(SystemName.COMPONENT_TEXT, component);
        }
    }

    /**
     * This is an encapsulation of a Campaign and all of its associated
     * WeightedCreatives.  The weight of this object is the average of
     * all WeightedCreatives for the given Campaign.
     */
    static final class AverageWeightedCampaign implements Weighted {
        private final CampaignDto campaign;
        // NOTE: ArrayList is the most efficient choice here, even though the
        // size isn't known up front.  We're not going to be removing anything
        // from this list once we add to it, just iterating it.  It's worth taking
        // the reallocation hit for faster iteration.  Test confirmed in sandbox.
        private final List<MutableWeightedCreative> mwcs = new ArrayList<MutableWeightedCreative>();
        private double creativeExponent = 0;
        private transient Double weight;

        AverageWeightedCampaign(CampaignDto campaign, double creativeExponent) {
            this.campaign = campaign;
            this.creativeExponent = creativeExponent;
        }

        public CampaignDto getCampaign() {
            return campaign;
        }

        public List<MutableWeightedCreative> getMutableWeightedCreatives() {
            return mwcs;
        }

        public boolean isEmpty() {
            return mwcs.isEmpty();
        }

        /**
         * @return the average weight of all the WeightedCreatives
         */
        @Override
        public double getWeight() {
            // It's assumed that getWeight won't be called until after modifications are
            // done being made.  "Cache" the weight value so we don't have to recalculate
            // on subsequent calls.  When sorting these objects, this method could
            // theoretically be called hundreds or even thousands of times.
            if (weight == null) {
                double numerator = 0.0;
                double denominator = 0.0;
                double creativeExponentForNumerator = 1.0 + creativeExponent;
                for (MutableWeightedCreative oneWeigtedCreative : mwcs) {
                    numerator = numerator + Math.pow(oneWeigtedCreative.getWeight(), creativeExponentForNumerator);
                    denominator = denominator + Math.pow(oneWeigtedCreative.getWeight(), creativeExponent);
                }
                if (denominator == 0) {
                    weight = 0.0;
                } else {
                    weight = numerator / denominator;
                }
            }
            return weight;
        }

        public void addMutableWeightedCreative(MutableWeightedCreative mwc) {
            mwcs.add(mwc);
            weight = null;
        }

        public void removeMutableWeightedCreative(MutableWeightedCreative mwc) {
            mwcs.remove(mwc);
            weight = null;
        }
    }

}

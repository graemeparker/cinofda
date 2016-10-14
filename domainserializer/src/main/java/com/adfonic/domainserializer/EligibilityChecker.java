package com.adfonic.domainserializer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publication.PublicationSafetyLevel;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RateCardDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.adspace.TransparentNetworkDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.domain.cache.ext.AdserverDomainCacheExt;
import com.adfonic.domain.cache.listener.DSRejectionListener;
import com.adfonic.domainserializer.loader.AdCacheBuildContext;
import com.adfonic.domainserializer.loader.AdCacheBuildContext.TransientPublicationAttributes;
import com.adfonic.domainserializer.xaudit.AuditEligibilityCheck;
import com.adfonic.util.AgeRangeTargetingLogic;
import com.adfonic.util.DaemonThreadFactory;

public class EligibilityChecker {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final boolean showProgress;

    private final int threadCount;

    private final AuditEligibilityCheck auditCheck;

    private final RejectedExecutionHandler rejectionHandler = new QueuingHandler();

    static class QueuingHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public EligibilityChecker(AuditEligibilityCheck auditCheck, int threadCount, boolean showProgress) {
        this.auditCheck = auditCheck;
        this.threadCount = threadCount;
        this.showProgress = showProgress;
    }

    public void deriveEligibleCreativesConcurrently(final AdserverDomainCacheExt adserverDomainCache, final AdCacheBuildContext td) throws Exception {
        td.startWatch("Deriving Eligible Creatives for " + adserverDomainCache.getAllAdSpaces().length + " AdSpaces");
        LOG.info("Eligibility started. Creatives: " + adserverDomainCache.getAllCreatives().length + ", AdSpaces: " + td.allAdSpacesById.size() + ", Threads: " + threadCount);

        DaemonThreadFactory threadFactory = new DaemonThreadFactory("Eligibility-");
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(threadCount * 10),
                threadFactory, rejectionHandler);
        try {
            final int errorLimit = 10;
            final AtomicInteger doneCounter = new AtomicInteger(0);
            final AtomicInteger errorCounter = new AtomicInteger(0);
            final CountDownLatch countDownLatch = new CountDownLatch(td.allAdSpacesById.size());
            // NOTE: we're using the transientData's AdSpaces collection for this iteration,
            // since we're responsible for building eligibility sets for ELIGIBLE_CREATIVE
            // as well as for adserver.  deriveEligibleCreatives won't cache anything
            // for adserver that adserver doesn't care about.
            for (final AdSpaceDto adSpace : td.allAdSpacesById.values()) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (errorCounter.get() >= errorLimit) {
                                return; // Quit on too many errors
                            }
                            checkAdSpace(adSpace, adserverDomainCache, td);
                        } catch (Exception x) {
                            errorCounter.incrementAndGet();
                            LOG.error("Eligibility failed for AdSpace id=" + adSpace.getId(), x);
                        } finally {
                            int count = doneCounter.incrementAndGet();
                            if (showProgress && count % 1000 == 0) {
                                LOG.debug("Eligibility completed for " + count + " of " + td.allAdSpacesById.size() + " adspaces");
                            }
                            countDownLatch.countDown();
                        }
                    }
                });
            }

            // Wait for all of them to finish
            countDownLatch.await();

            // Bail if an exception occurred during concurrent eligibility derivation
            if (errorCounter.get() > errorLimit) {
                throw new IllegalStateException("Cache production cancelled as " + errorCounter.get() + " exceptions happened");
            }
        } finally {
            executorService.shutdownNow();
        }
        td.stopWatch("Deriving Eligible Creatives for " + adserverDomainCache.getAllAdSpaces().length + " AdSpaces");
    }

    private void checkAdSpace(AdSpaceDto adSpace, AdserverDomainCacheExt cache, AdCacheBuildContext td) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Collecting creatives for Publication: " + adSpace.getPublication().getId() + " AdSpace: " + adSpace.getId() + " Formats: " + adSpace.getFormatIds());
        }

        final Set<CreativeDto> compatibleCreatives = new HashSet<CreativeDto>();
        final Set<Long> acceptedCampaignIds = new HashSet<Long>();
        final Set<Long> rejectedCampaignIds = new HashSet<Long>();
        final PublicationDto publication = adSpace.getPublication();
        final TransientPublicationAttributes pubAttrs = td.publicationAttributesByPublicationId.get(publication.getId());

        // Start out by iterating through all active creatives for each of the formats
        // supported by the given AdSpace.  As we go, we apply basic publication
        // eligibility rules and either add it to our creativesEligibleForTargeting
        // set or bail.
        for (Long formatId : adSpace.getFormatIds()) {

            List<CreativeDto> creativesWithFormat = td.creativesByFormatId.get(formatId);
            if (creativesWithFormat == null) {
                // dsRejectionListener.ineligible(adSpace, null, "No creatives with Format: " + formatId);
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("adid: " + adSpace.getId() + " format: " + formatId + " creatives: " + creativesWithFormat.size());
            }
            for (CreativeDto creative : creativesWithFormat) {
                checkAdSpaceAndCreative(creative, adSpace, compatibleCreatives, acceptedCampaignIds, rejectedCampaignIds, td, cache);
            }
        }

        // We're tasked with deriving eligibility for all AdSpaces that aren't necessarily
        // intended for use on adserver.  Check the Publication status to determine if
        // this AdSpace is for adserver and EC, or just for EC.
        @Deprecated
        final boolean adSpaceReady = (publication.getStatus() == Publication.Status.ACTIVE || publication.getStatus() == Publication.Status.PENDING);

        // The not-by-priority set of eligible Creative ids for this AdSpace
        @Deprecated
        Set<Long> eligibleCreativeIdsForEC = new HashSet<Long>();

        if (!compatibleCreatives.isEmpty()) {
            Map<Integer, List<CreativeDto>> creativesByPriority = weightByPriority(adSpace, cache, td, compatibleCreatives, publication, pubAttrs, adSpaceReady,
                    eligibleCreativeIdsForEC);
            if (!creativesByPriority.isEmpty()) {
                int totalEligibleCreatives = 0;
                for (Map.Entry<Integer, List<CreativeDto>> entry : creativesByPriority.entrySet()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("AdSpace: " + adSpace.getId() + " has " + entry.getValue().size() + " eligible Creatives with priority " + entry.getKey());
                    }
                    totalEligibleCreatives += entry.getValue().size();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("AdSpace: " + adSpace.getId() + " has " + totalEligibleCreatives + " eligible Creatives");
                }

                Set<AdspaceWeightedCreative> weightedCreativeIds = new TreeSet<AdspaceWeightedCreative>();

                for (Map.Entry<Integer, List<CreativeDto>> oneEntry : creativesByPriority.entrySet()) {
                    List<CreativeDto> creatives = oneEntry.getValue();
                    Long[] creativeIds = new Long[creatives.size()];
                    for (int i = 0; i < creativeIds.length; i++) {
                        creativeIds[i] = creatives.get(i).getId();
                    }
                    AdspaceWeightedCreative weighted = new AdspaceWeightedCreative();
                    weighted.setPriority(oneEntry.getKey());
                    weighted.setCreativeIds(creativeIds);
                    weightedCreativeIds.add(weighted);
                }
                cache.addAdSpaceEligibleCreative(adSpace.getId(), weightedCreativeIds, td.allCountries);
            }
        }

        if (!eligibleCreativeIdsForEC.isEmpty()) {
            // This is for CreativeEligibilityUpdater only
            synchronized (cache.getTransientData().transientCreativeIdsForEC) {
                cache.getTransientData().transientCreativeIdsForEC.addAll(eligibleCreativeIdsForEC);
            }
            synchronized (cache.getTransientData().transientEligibleCreativeIdsByAdSpaceIdForEC) {
                cache.getTransientData().transientEligibleCreativeIdsByAdSpaceIdForEC.put(adSpace.getId(), eligibleCreativeIdsForEC);
            }

        }
    }

    private void checkAdSpaceAndCreative(CreativeDto creative, AdSpaceDto adSpace, final Set<CreativeDto> compatibleCreatives, Set<Long> eligibleCampaignIds,
            Set<Long> ineligibleCampaignIds, AdCacheBuildContext td, AdserverDomainCacheExt cache) {
        final Long creativeId = creative.getId();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Matching adid: " + adSpace.getId() + " crid: " + creativeId);
        }

        PublicationDto publication = adSpace.getPublication();
        Long publicationId = publication.getId();
        Long publisherId = publication.getPublisher().getId();
        DSRejectionListener dsRejectionListener = td.getDsListener();
        final TransientPublicationAttributes pubAttrs = td.publicationAttributesByPublicationId.get(publicationId);

        // Ordinarily we filter out backfill creatives for AdSpaces that don't allow backfill.
        if (creative.getCampaign().getAdvertiser().getCompany().isBackfill() && !adSpace.isBackfillEnabled()) {
            // But before we do, make sure the Publisher or Publication haven't explicitly approved
            // this one...we do allow exceptions like that.  Note that this check is done
            // irrespective of Publication.autoApproval.
            if (!td.getPublisherApprovedCreativeIds(publisherId).contains(creativeId) && !td.getPublicationApprovedCreativeIds(publicationId).contains(creativeId)) {
                // Nope, it wasn't explicitly approved, so it's not eligible
                dsRejectionListener.ineligible(adSpace, creative, "Publisher " + publisherId + " did not approved Creative explicitely");
                return;
            }
            // Joined this next else clause on to be slightly more efficient, since if we got to
            // this point, then we know the creative is explicitly approved, and we don't need to
            // do any more approved/denied checks.
        } else if (pubAttrs.autoApproval) {
            // Make sure the creative hasn't explicitly been denied for the Publication
            if (td.getPublicationDeniedCreativeIds(publicationId).contains(creativeId)) {
                dsRejectionListener.ineligible(adSpace, creative, "Publication " + publicationId + "  denied creative explicitely");
                return;
            }
        }
        // Manual approval pubs do their filtering at the end, since adserver wants
        // one thing, and EC wants another thing...so we delay that elimination step.

        // If the publication is in the creative's removed list, it can't be served
        if (td.isCreativeRemovedPublication(creative.getId(), publicationId)) {
            dsRejectionListener.ineligible(adSpace, creative, "Creative removed Publication " + publicationId + "  explicitly");
            return;
        }

        if (publication.getPublisher().doesRequireRealDestination() && !creative.getDestination().hasRealDestination()) {
            dsRejectionListener.ineligible(adSpace, creative, "Creative does not have required real destination");
            return;
        }

        RtbConfigDto rtbConfig = publication.getPublisher().getRtbConfig();
        if (rtbConfig != null && rtbConfig.isSslRequired() && !creative.isSslCompliant()) {
            dsRejectionListener.ineligible(adSpace, creative, "Creative is not SSL Compliant");
            return;
        }

        // Check to see if the campaign is eligible for the publication
        CampaignDto campaign = creative.getCampaign();
        if (!eligibleCampaignIds.contains(campaign.getId())) {
            if (ineligibleCampaignIds.contains(campaign.getId())) {
                // Already determined that the campaign is NOT eligible
                dsRejectionListener.ineligible(adSpace, creative, "Whole campaign " + campaign.getId() + " already marked ineligible");
                return;
            } else if (!isCampaignEligible(creative, creative.getSegment(), adSpace, cache, td)) {
                // Mark it ineligible so any other creatives for the same campaign will bail quickly
                ineligibleCampaignIds.add(campaign.getId());
                return;
            } else {
                // Mark it eligible so any other creatives for the same campaign can bypass campaign eligibility checks
                eligibleCampaignIds.add(campaign.getId());
            }
        }

        /* Publisher language setting not yet exposed in UI.
        // #631 - added Publication.matchUserLanguage support
        if (!pub.getMatchUserLanguage() && !pub.getLanguages().contains(creative.getLanguage())) {
            // The language isn't one of the pub's approved languages
            continue;
        }
        */

        /*
        // Make sure the creative doesn't have an asset with a ContentType
        // that's not listed by the PublicationType.
        if (approvedContentTypes == null) {
            approvedContentTypes = pub.getPublicationType().getContentTypes();
        }
        boolean contentTypesAreCool = true;
        for (AssetBundle assetBundle : creative.getAssetBundleMap().values()) {
            for (Asset asset : assetBundle.getAssetMap().values()) {
                if (!approvedContentTypes.contains(asset.getContentType())) {
                    // The content type isn't supported by the pub type
                    contentTypesAreCool = false;
                    break;
                }
            }
        }
        if (!contentTypesAreCool) {
            continue;
        }
        */

        // Compare creative/adspace extended types
        Long extendedCreativeTypeId = creative.getExtendedCreativeTypeId();
        if (extendedCreativeTypeId != null) {
            Feature notApprovedFeature = null;
            Set<Feature> featureSet = td.featuresByExtendedCreativeTypeId.get(extendedCreativeTypeId);
            if (featureSet != null) {
                for (Feature feature : featureSet) {
                    if (!td.isFeatureApproved(adSpace.getId(), feature)) {
                        notApprovedFeature = feature;
                        break;
                    }
                }
            }
            if (notApprovedFeature != null) {
                dsRejectionListener.ineligible(adSpace, creative, "Feature " + notApprovedFeature + " of ExtendedCreativeTypeId " + extendedCreativeTypeId
                        + " NOT approved by adspace");
                return;
            }

            Set<Long> publisherWhitelist = td.publisherAllowedExtendedCreativeTypeIds.get(publisherId);
            Set<Long> publicationWhitelist = td.publicationAllowedExtendedCreativeTypeIds.get(publicationId);
            boolean whitelisted = (publisherWhitelist != null && publisherWhitelist.contains(extendedCreativeTypeId))
                    || (publicationWhitelist != null && publicationWhitelist.contains(extendedCreativeTypeId));
            if (!whitelisted) {
                dsRejectionListener.ineligible(adSpace, creative, "ExtendedCreativeTypeId " + extendedCreativeTypeId + " NOT approved by publisher: " + publisherId
                        + " or publication: " + publicationId);
                return;
            }
        }

        // This creative is eligible for targeting checks...add it to our running set
        compatibleCreatives.add(creative);

    }

    private Map<Integer, List<CreativeDto>> weightByPriority(AdSpaceDto adSpace, AdserverDomainCacheExt cache, AdCacheBuildContext td, Set<CreativeDto> compatibleCreatives,
            PublicationDto publication, TransientPublicationAttributes pubAttrs, final boolean adSpaceReady, Set<Long> eligibleCreativeIdsForEC) {
        // Prune the set of creatives based on publication/segment criteria,
        // while applying base targeting weights to those that make it through.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Prioritising " + compatibleCreatives.size() + " compatible Creatives for Adspace " + adSpace.getId());
        }
        DSRejectionListener dsRejectionListener = td.getDsListener();
        Map<Integer, List<CreativeDto>> weightedByPriority = new TreeMap<Integer, List<CreativeDto>>();
        for (CreativeDto creative : compatibleCreatives) {
            boolean creativeApproved;
            if (creative.getCampaign().isHouseAd()) {
                // House ads are implicitly approved (we already filtered by company in the campaign eligibility check above).
                creativeApproved = true;
            } else if (pubAttrs.autoApproval) {
                // Not a house ad, but auto-approval is enabled.
                creativeApproved = true;
            } else {
                // Not a house ad, not auto-approval, so it needs to be manually approved.
                creativeApproved =
                // It either needs to have been approved for the publication...
                td.getPublicationApprovedCreativeIds(publication.getId()).contains(creative.getId()) ||
                // ...or for the publisher
                        td.getPublisherApprovedCreativeIds(publication.getPublisher().getId()).contains(creative.getId());
            }

            // This flag indicates that adserver doesn't care about this Creative, since
            // either adserver doesn't care about the Publication (based on its status),
            // or adserver doesn't care about the Creative due to it not being approved
            // for the publication, but we still need it for ELIGIBLE_CREATIVE if all
            // targeting checks are otherwise satisfied.
            @Deprecated
            boolean creativeEcOnly = !adSpaceReady || !creativeApproved;

            // Apply the segment rules
            SegmentDto segment = creative.getSegment();
            if (segment == null) {
                // This is a Run-of-Network ad.  No Segment, so just don't
                // factor in the segment targeting.  It's still eligible
                // for selection, we just can't come up with real weights
                // based on targeting criteria.  We'll use default weights,
                // considering RON ads the same as "all ages" and a 50/50
                // split on gender.
                if (!creative.getCampaign().isHouseAd()) {
                    eligibleCreativeIdsForEC.add(creative.getId());
                }
                if (!creativeEcOnly) {
                    int effectivePriority;
                    if (creative.getCampaign().isHouseAd()) {
                        // House ads are treated as having top priority (lowest possible value)
                        effectivePriority = Integer.MIN_VALUE;
                    } else {
                        effectivePriority = creative.getPriority();
                    }
                    List<CreativeDto> weighted = weightedByPriority.get(effectivePriority);
                    if (weighted == null) {
                        weighted = new ArrayList<CreativeDto>();
                        weightedByPriority.put(effectivePriority, weighted);
                    }
                    weighted.add(creative);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Only eligible to put in EC");
                }
                dsRejectionListener.ineligible(adSpace, creative, segment, "Only eligible to put in EC");
                continue;
            }

            // Segment.adSpaces
            if (!td.isAdSpaceAllowed(segment.getId(), adSpace.getId())) {
                dsRejectionListener.ineligible(adSpace, creative, segment, "Adspace ID is not allowed by Segment.adspaceIds");
                continue;
            }

            // Segment.channels
            Set<Long> segmentChannelIds = td.channelIdsBySegmentId.get(segment.getId());
            if (segmentChannelIds != null) {
                // Segment.channels must contain Publication.category.channel
                if (!segmentChannelIds.contains(td.channelIdByCategoryId.get(publication.getCategoryId()))) {
                    dsRejectionListener.ineligible(adSpace, creative, segment, "Adspace Category is not allowed by Segment.ChannelIds");
                    continue;
                }
            }

            // Segment.requireTrusted
            SegmentSafetyLevel segmentSafetyLevel = td.safetyLevelBySegmentIdMap.get(segment.getId());
            PublicationSafetyLevel publicationSafetyLevel = td.safetyLevelByPublicationIdMap.get(publication.getId());
            if (segmentSafetyLevel.ordinal() > publicationSafetyLevel.ordinal()) {
                // Safety Levels dont match
                dsRejectionListener.ineligible(adSpace, creative, segment, "Segment safety level is " + segmentSafetyLevel + " and this publication's safety level is "
                        + publicationSafetyLevel);
                continue;
            }

            // Segment.incentivizedAllowed
            if (pubAttrs.incentivized && !td.incentivizedAllowedBySegmentId.get(segment.getId())) {
                // Incentivized publications not allowed
                dsRejectionListener.ineligible(adSpace, creative, segment, "Segment Incentivized publications not allowed");
                continue;
            }

            // Targeted platforms (Segment.models + Segment.vendors + Segment.platforms) must
            // match up with the PublicationType's platforms
            if (!td.isEligibleBasedOnPlatforms(segment, publication.getPublicationTypeId())) {
                // Targeted platforms don't match up
                dsRejectionListener.ineligible(adSpace, creative, segment, "Platforms of PublicationType " + publication.getPublicationTypeId() + " do not match");
                continue;
            }

            if (!td.isEligibleBasedOnInventoryTargeting(creative, adSpace, dsRejectionListener)) {
                // Targeted inventory don't match up
                //Listener Method moved inside the above method
                dsRejectionListener.ineligible(adSpace, creative, segment, "Segment has excluded the adspace/publication inventory");
                continue;
            }

            // Segment.excludedCategories
            if (td.getExpandedSegmentExcludedCategoryIds(segment.getId()).contains(publication.getCategoryId())) {
                dsRejectionListener.ineligible(adSpace, creative, segment, "Segment has excluded the adspace/publication category");
                continue; // not eligible
            } else {
                // FIXME Set<Long> can not contain another Set<Long> 
                Set<Long> publicationStatedCategoryIds = td.getPublicationStatedCategoryIds(publication.getId());
                if (td.getExpandedSegmentExcludedCategoryIds(segment.getId()).contains(publicationStatedCategoryIds)) {
                    dsRejectionListener.ineligible(adSpace, creative, segment, "Segment has excluded the adspace/publication stated category");
                    continue; // not eligible;
                }
            }

            // Segment.includedCategories

            if (!td.getExpandedSegmentIncludedCategoryIds(segment.getId()).isEmpty()
                    && !td.getExpandedSegmentIncludedCategoryIds(segment.getId()).contains(publication.getCategoryId())) {
                //If segment.included Categories Not Empty and it doesn't have pub.category
                dsRejectionListener.ineligible(adSpace, creative, segment, "Segment included categories do not have publication category " + publication.getCategoryId());
                continue;
            }

            // Segment.minAge/maxAge
            if (!AgeRangeTargetingLogic.areAgeRangesEligible(segment.getMinAge(), segment.getMaxAge(), pubAttrs.minAge, pubAttrs.maxAge)) {
                dsRejectionListener.ineligible(adSpace, creative, segment, "Age Ranges are not eligible");
                continue;
            }

            if (!isEligibleBasedOnSegmentTargettedPublisher(creative, adSpace, td)) {
                //dsRejectionListener reporting inside
                continue;
            }

            // Pre-calculate "out of the box" weights for the creative based on
            // how well the publisher matches up with the creative's segment.
            // These will be used in lieu of actual audience member values when
            // it comes time to do targeting.

            // Calculate the genderMix weight
            Double genderDelta = null;
            if (segment.getGenderMix() != null && pubAttrs.genderMix != null) {
                genderDelta = Math.abs(segment.getGenderMix().doubleValue() - pubAttrs.genderMix.doubleValue());
                if (genderDelta == 1.0) {
                    // Gender mixes preclude each other.
                    // Disallow the ad if the gender & genderMix are complete
                    // mismatches.
                    dsRejectionListener.ineligible(adSpace, creative, segment, "Gender mix not eligible");
                    continue;
                }
            }

            // AF-1098 - Temporary hack ticket - exclude CPA/CPI based campaigns from publishers who've any 
            // CPM/CPC based payout threshold set
            if (!creative.getCampaign().isHouseAd()) {
                //Check Bid stuff only for Non House Ads
                CampaignBidDto campaignBid = creative.getCampaign().getCurrentBid();
                if (campaignBid == null) {
                    dsRejectionListener.ineligible(adSpace, creative, segment, "Campaign Bid is null.. scary");
                    continue; // This should never happen, but it has: https://tickets.adfonic.com/browse/AF-629
                }

                // This is phase 2 of 3 of minimum pricing enforcement.  Don't bother
                // checking for non house ads, though, since house ads won't have a
                // current bid set.
                BigDecimal payout = cache.getPayout(publication.getPublisher().getId(), creative.getCampaign().getId());
                if (payout == null) {
                    // wasn't cached yet, Lazily init it and cache it.
                    payout = getPayout(publication.getPublisher(), creative.getCampaign(), campaignBid);
                    cache.cachePayout(publication.getPublisher().getId(), creative.getCampaign().getId(), payout);
                }
                RateCardDto rateCard = adSpace.getPublication().getEffectiveRateCard(campaignBid.getBidType());
                if (rateCard != null) {
                    if (!rateCard.isRateCardLessThenAmount(payout)) {
                        dsRejectionListener.ineligible(adSpace, creative, segment, "Publisher payout for this campaign is less then Ratecard defined by publisher");
                        continue;
                    }
                }
            }

            if (!auditCheck.isEligible(creative, adSpace, td)) {
                dsRejectionListener.ineligible(adSpace, creative, segment, "Publisher Audited creative required!");
                continue;
            }

            // There's no more elimination below, so at this point we can bail on the
            // rest of the WeightedCreative setup if this Creative is for EC only.
            if (!creative.getCampaign().isHouseAd()) {
                eligibleCreativeIdsForEC.add(creative.getId());
            }
            if (creativeEcOnly) {
                dsRejectionListener.ineligible(adSpace, creative, segment, "Creative EC Only,adSpaceEcOnly=" + !adSpaceReady + ",creativeApprovedManuallyOrImplicitly="
                        + creativeApproved);
                continue;
            }

            int effectivePriority;
            if (creative.getCampaign().isHouseAd()) {
                // House ads are treated as having top priority (lowest possible value)
                effectivePriority = Integer.MIN_VALUE;
            } else {
                effectivePriority = creative.getPriority();
            }
            List<CreativeDto> weighted = weightedByPriority.get(effectivePriority);
            if (weighted == null) {
                weighted = new ArrayList<CreativeDto>();
                weightedByPriority.put(effectivePriority, weighted);
            }
            dsRejectionListener.eligible(adSpace, creative, effectivePriority);
            weighted.add(creative);
        }
        return weightedByPriority;
    }

    //Package access to make sure we can test it
    boolean isEligibleBasedOnSegmentTargettedPublisher(CreativeDto creative, AdSpaceDto adspace, AdCacheBuildContext td) {
        if (creative.getCampaign().getCurrentBid() == null) {
            //For house ads currentBid is null
            return true;
        }
        if (creative.isClosedMode()) {
            //If creative is in closed mode, it can go to any inventory so return true.
            return true;
        }

        DSRejectionListener dsRejectionListener = td.getDsListener();

        SegmentDto segment = creative.getSegment();
        Set<Long> segmentPublisherIds = td.getSegmentTargettedPublisherBySegmentId(segment.getId()); //SEGMENT_PUBLISHER table
        //Check if is a targetted publishers id which can be choosen from UI
        if (td.isTargettedPublisherId(adspace.getPublication().getPublisher().getId())) {
            //Publisher ID is one of the UI displayed Publishers (TARGET_PUBLISHER table)
            if ((segmentPublisherIds == null || segmentPublisherIds.isEmpty())) {
                //If campaign belongs to DSP and targetted publisher List is empty and include byyd network is false then this campaign should be served on all publishers
                if (segment.isIncludeAdfonicNetwork()) { //SEGMENT.INCLUDE_ADFONIC_NETWORK
                    //Adfonic network included so this adspace will not be served
                    dsRejectionListener.ineligible(adspace, creative, segment, "Publisher targetable && segmentPublisherIds.isEmpty() && segment.isIncludeAdfonicNetwork()");
                    return false;
                } else {
                    if (BidModelType.DSP_LIC.equals(creative.getCampaign().getCurrentBid().getBidModelType())) {
                        if (adspace.getPublication().getPublisher().getRtbConfig() != null) {
                            //If its DSP_LIC user we can serve on targetted publisher network
                            return true;
                        } else {
                            dsRejectionListener.ineligible(adspace, creative, segment,
                                    "Publisher targetable && segmentPublisherIds.isEmpty() && !segment.isIncludeAdfonicNetwork() && BidModelType == DSP_LIC && Publisher != RTB");
                            return false;
                        }
                    } else {
                        //if its not DSP_LIC, go ahead and server on targetted publisher network
                        return true;
                    }
                }
            } else { // segmentPublisherIds NOT Empty
                Long publisherId = adspace.getPublication().getPublisher().getId();
                if (segmentPublisherIds.contains(publisherId)) {
                    return true;
                } else {
                    dsRejectionListener.ineligible(adspace, creative, segment, "Publisher targetable && !segment.isIncludeAdfonicNetwork() && segmentPublisherIds "
                            + segmentPublisherIds + " !contain " + publisherId);
                    return false;
                }
            }
        } else {
            //Publisher ID is NOT one of the UI displayed Publishers (TARGET_PUBLISHER table)
            if (segment.isIncludeAdfonicNetwork()) {
                //Check if we can show it on Adfonic network
                return true;
            } else {
                if ((segmentPublisherIds == null || segmentPublisherIds.isEmpty())) {
                    if (BidModelType.DSP_LIC.equals(creative.getCampaign().getCurrentBid().getBidModelType())) {
                        //If its DSP_LIC user we must not serve on byyd network
                        dsRejectionListener.ineligible(adspace, creative, segment,
                                "Publisher !targetable && !segment.isIncludeAdfonicNetwork() && segmentPublisherIds.isEmpty() && BidModelType == DSP_LIC ");
                        return false;
                    } else {
                        //if its not DSP_LIC, go ahead and server on byyd network
                        return true;
                    }
                } else {
                    dsRejectionListener.ineligible(adspace, creative, segment, "Publisher !targetable && !segment.isIncludeAdfonicNetwork() && segmentPublisherIds: "
                            + segmentPublisherIds);
                    return false;
                }
            }
        }
    }

    /** Determine if a campaign is eligible for a given publication */
    private boolean isCampaignEligible(CreativeDto creative, SegmentDto segment, AdSpaceDto adSpace, AdserverDomainCacheExt cache, AdCacheBuildContext td) {
        CampaignDto campaign = creative.getCampaign();
        PublicationDto publication = adSpace.getPublication();
        DSRejectionListener dsRejectionListener = td.getDsListener();

        //This should be irrespective of houseAds
        Medium targetedMedium;
        if (segment != null && (targetedMedium = segment.getMedium()) != null && targetedMedium != Medium.UNKNOWN) {
            Medium publicationMedium = td.mediumsByPublicationTypeId.get(publication.getPublicationTypeId());
            if (publicationMedium != Medium.UNKNOWN && targetedMedium != publicationMedium) {
                dsRejectionListener.ineligible(adSpace, creative, "Publication Medium is " + publicationMedium + " and campaign is targeting " + targetedMedium);
                return false;
            }
        }

        // If it's a house ad, make sure the campaign company is the publication company.
        // If it's not a house ad, make sure the campaign company is NOT the publication company.
        Long id = publication.getPublisher().getCompany().getId();
        if (campaign.isHouseAd()) {
            if (!campaign.getAdvertiser().getCompany().getId().equals(id)) {
                dsRejectionListener.ineligible(adSpace, creative, "Creative is a house ad which do not belongs to the same company of adspace");
                return false; // house ads must only be served on the same company
            }
        } else if (campaign.getAdvertiser().getCompany().getId().equals(id)) {
            dsRejectionListener.ineligible(adSpace, creative, "Creative is NOT a house ad and can not be served on same company's adspace");
            return false; // non house ad can't be served on the same company
        }

        // If the publication is in the campaign's removed list, it can't be served
        if (td.isCampaignRemovedPublication(campaign.getId(), publication.getId())) {
            dsRejectionListener.ineligible(adSpace, creative, "Campaign has blocked/removed this publication explicitly");
            return false;
        }

        // Make sure the campaign hasn't ended...this is possible since
        // the domain cache/queue builder will only be looking for the
        // case where endDate has already passed.  It's possible that
        // the campaign ended in between domain cache loads.
        if (campaign.getEndDate() != null && campaign.getEndDate().getTime() <= System.currentTimeMillis()) {
            dsRejectionListener.ineligible(adSpace, creative, "Campaign already ended on " + campaign.getEndDate());
            return false;
        }

        // Was #746 - installTracking is now on campaign.
        // Creatives with installTrackingEnabled set to true
        // should only be allowed in the ad queue for ad spaces where
        // the publication.publicationType.medium == Medium.APPLICATION.
        // Also, make sure the publication hasn't explicitly disabled
        // install tracking...if so, that publication shouldn't serve ads
        // for creatives in campaigns with installTrackingEnabled.

        //JIRA #AF-1390
        //Campaign with AdxEnabled should be served on Sites too
        if (campaign.isInstallTrackingEnabled()) {
            if (publication.isInstallTrackingDisabled()) {
                dsRejectionListener.ineligible(adSpace, creative, "Campaign is tacking enabled but publication is tracking disabled");
                return false;
            }
            //If campaign is not adx enabled then check that it should only be served for Application or devices
            if (!campaign.isInstallTrackingAdXEnabled()
                    && (!Medium.APPLICATION.equals(td.mediumsByPublicationTypeId.get(publication.getPublicationTypeId())) || !TrackingIdentifierType.DEVICE.equals(td
                            .getEffectiveTrackingIdentifierType(publication)))) {
                dsRejectionListener.ineligible(adSpace, creative, "Campaign is not adx install enabled && it can served only on DEVICE or APPS");
                return false;
            }
        }

        // Start with "Phase One" of eliminations based on the
        // publication's TransparentNetwork.  We'll do "Phase Two" when
        // doing targeting, since we need the end user's Country for that.
        TransparentNetworkDto pubTN = publication.getTransparentNetwork();
        Set<Long> campaignTnIds = td.getTransparentNetworkIds(campaign.getId());
        if (pubTN == null) {
            if (!campaignTnIds.isEmpty()) {
                dsRejectionListener.ineligible(adSpace, creative, "Campaign belongs to Transaparent network but Publication do not belong to any Transparent Network");
                return false;
            }
        } else if (!campaignTnIds.isEmpty()) {
            if (!campaignTnIds.contains(pubTN.getId())) {
                dsRejectionListener.ineligible(adSpace, creative, "Campaign belongs to Transaparent network but Publication belongs to some other Transparent Network");
                return false;
            }
        } else if (pubTN.isClosed() && !pubTN.getAdvertiserCompanyIds().contains(campaign.getAdvertiser().getCompany().getId())) {
            dsRejectionListener.ineligible(adSpace, creative, "Publication Transparent Network is closed and it doesnt match to campaign TN");
            return false;
        }

        // Don't bother checking minimum pricing for house ads
        if (!campaign.isHouseAd()) {
            CampaignBidDto campaignBid = campaign.getCurrentBid();
            if (campaignBid == null) {
                dsRejectionListener.ineligible(adSpace, creative, "Campaign BID found null.. scary");
                return false; // This should never happen, but it has: https://tickets.adfonic.com/browse/AF-629
            }

            BigDecimal payout = null; // lazily, possibly not, initialized

            /*
            if (pubTN != null) {
                if (!pubTN.isDefaultRateCard()) {
                    RateCardDto rateCard = pubTN.getRateCard(campaignBid.getBidType());
                    if (rateCard == null) {
                        return false;
                    }

                    // lazily initialized...we need it now
                    payout = getPayout(pub.getPublisher(), campaign, campaignBid);

                    // If the payout doesn't already satisfy the default minimum...
                    if (rateCard.getDefaultMinimum() == null || payout.compareTo(rateCard.getDefaultMinimum()) < 0) {
                        // ...then check every country's mininum bid
                        boolean anyCountrySatisfied = false;
                        for (BigDecimal minimumBid : rateCard.getMinimumBidsByCountryId().values()) {
                            if (payout.compareTo(minimumBid) >= 0) {
                                // Good enough to let it through
                                anyCountrySatisfied = true;
                                break;
                            }
                        }
                        if (!anyCountrySatisfied) {
                            return false;
                        }
                    }
                }
            }
            */

        }

        // Make sure the campaign's category isn't among the publication's or publisher's 
        // excluded categories.
        if (td.isCampaignCategoryExcludedByPublicationOrPublisher(publication, campaign.getCategoryId())) {
            dsRejectionListener.ineligible(adSpace, creative, "Campaign category is excluded by Publication or Publisher");
            return false;
        }

        if (campaign.getCurrentBid() != null && td.isBidTypeDenied(publication, campaign.getCurrentBid().getBidType())) {
            //If Campaign Bid is Denied by this publication/Publisher then ignore this Creative/Campaign
            dsRejectionListener.ineligible(adSpace, creative, "Campaign bid is excluded by Publication or Publisher");
            return false;
        }

        return true; // it's eligible
    }

    private static BigDecimal getPayout(PublisherDto publisher, CampaignDto campaign, CampaignBidDto campaignBid) {
        return Publisher.getPayout(new BigDecimal(publisher.getCurrentRevShare()), new BigDecimal(campaign.getAgencyDiscount()), new BigDecimal(campaignBid.getAmount()),
                new BigDecimal(campaign.getAdvertiser().getCompany().getMarginShareDSP()));
    }

}

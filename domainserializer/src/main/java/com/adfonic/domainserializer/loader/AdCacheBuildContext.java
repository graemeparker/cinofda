package com.adfonic.domainserializer.loader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;

import com.adfonic.domain.BidType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Publication.PublicationSafetyLevel;
import com.adfonic.domain.PublisherAuditedCreative.Status;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PublicationListDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.domain.cache.ext.util.AdfonicStopWatch;
import com.adfonic.domain.cache.listener.DSRejectionListener;
import com.adfonic.domain.cache.listener.DSRejectionListenerImpl;

/**
 * Data needed for cache production and eligibility calculations but not serialized into resulting cache file 
 */
public class AdCacheBuildContext {

    public Long debugCampaignId;

    public Long debugCreativeId;

    public Long debugAdSpaceId;

    public Long debugPublicationId;

    private DSRejectionListener dsListener = new DSRejectionListenerImpl();

    public final Map<Long, List<CreativeDto>> creativesByFormatId = new HashMap<Long, List<CreativeDto>>();

    public final ConcurrentHashMap<Long, SegmentSafetyLevel> safetyLevelBySegmentIdMap = new ConcurrentHashMap<Long, SegmentSafetyLevel>();
    public final ConcurrentHashMap<Long, PublicationSafetyLevel> safetyLevelByPublicationIdMap = new ConcurrentHashMap<Long, PublicationSafetyLevel>();

    public final ConcurrentHashMap<Long, Boolean> incentivizedAllowedBySegmentId = new ConcurrentHashMap<Long, Boolean>();
    public final Map<Long, Set<Long>> adSpaceIdsBySegmentId = new HashMap<Long, Set<Long>>();

    public final Map<Long, Set<Feature>> approvedFeaturesByAdSpaceId = new HashMap<Long, Set<Feature>>();
    public final Map<Long, Set<Feature>> deniedFeaturesByAdSpaceId = new HashMap<Long, Set<Feature>>();

    public final Map<Long, Set<Feature>> featuresByExtendedCreativeTypeId = new HashMap<Long, Set<Feature>>();

    public final Map<Long, Set<Long>> expandedCategoryIdsByCategoryId = new HashMap<Long, Set<Long>>();
    public final Map<Long, Set<Long>> expandedPublicationExcludedCategoryIds = new HashMap<Long, Set<Long>>();
    public final Map<Long, Set<Long>> expandedPublisherExcludedCategoryIds = new HashMap<Long, Set<Long>>();
    public final Map<Long, Set<Long>> expandedSegmentExcludedCategoryIds = new HashMap<Long, Set<Long>>();
    public final Map<Long, Set<Long>> expandedSegmentIncludedCategoryIds = new HashMap<Long, Set<Long>>();

    public final Map<Long, Set<BidType>> publicationDeniedBidTypes = new HashMap<Long, Set<BidType>>();
    public final Map<Long, Set<BidType>> publisherDeniedBidTypes = new HashMap<Long, Set<BidType>>();

    public final Map<Long, Set<Long>> segmentTargettedPublisherIds = new HashMap<Long, Set<Long>>();

    public final Set<Long> allTargettedPublisherIds = new HashSet<Long>();

    public final Map<Long, Set<Long>> publicationStatedCategoryIds = new HashMap<Long, Set<Long>>();

    public final Set<Long> effectivelyPermanentlyStoppedCampaignIds = new HashSet<Long>();
    public final Set<Long> recentlyStoppedAdvertiserIds = new HashSet<Long>();
    public final Set<Long> effectivelyPermanentlyStoppedAdvertiserIds = new HashSet<Long>();
    public final Set<Long> recentlyStoppedCampaignIds = new HashSet<Long>();

    // This contains *all* AdSpaces, including those whose publication status is
    // PAUSED.  Adserver doesn't care about those, but ELIGIBLE_CREATIVE does.
    public final Map<Long, AdSpaceDto> allAdSpacesById = new HashMap<Long, AdSpaceDto>();

    public final Map<Long, Long> channelIdByCategoryId = new HashMap<Long, Long>();
    public final Map<Long, Set<Long>> channelIdsBySegmentId = new HashMap<Long, Set<Long>>();
    public final ConcurrentHashMap<Long, Set<Long>> channelIdsByPublicationId = new ConcurrentHashMap<Long, Set<Long>>();

    public final Map<Long, TransientPublicationAttributes> publicationAttributesByPublicationId = new HashMap<Long, TransientPublicationAttributes>();

    public final Map<Long, Medium> mediumsByPublicationTypeId = new HashMap<Long, Medium>();
    public final Map<Long, TrackingIdentifierType> defaultTrackingIdentifierTypesByPublicationTypeId = new HashMap<Long, TrackingIdentifierType>();

    public final Set<String> creativeRemovedPublications = new HashSet<String>();
    public final Set<String> campaignRemovedPublications = new HashSet<String>();

    public final Map<Long, Set<Long>> transparentNetworkIdsByCampaignId = new HashMap<Long, Set<Long>>();

    public final Map<Long, Set<Long>> publicationApprovedCreativeIds = new HashMap<Long, Set<Long>>();
    public final Map<Long, Set<Long>> publicationDeniedCreativeIds = new HashMap<Long, Set<Long>>();
    public final Map<Long, Set<Long>> publisherApprovedCreativeIds = new HashMap<Long, Set<Long>>();

    // PUBLISHER_ID -> Set of EXTENDED_CREATIVE_TYPE_ID
    public final Map<Long, Set<Long>> publisherAllowedExtendedCreativeTypeIds = new HashMap<Long, Set<Long>>();
    // PUBLICATION_ID -> Set of EXTENDED_CREATIVE_TYPE_ID
    public final Map<Long, Set<Long>> publicationAllowedExtendedCreativeTypeIds = new HashMap<Long, Set<Long>>();

    public final Map<Long, Set<Long>> platformIdsByPublicationTypeId = new HashMap<Long, Set<Long>>();
    public final Map<Long, Set<Long>> platformIdsByVendorId = new HashMap<Long, Set<Long>>();
    public final Map<Long, Set<Long>> platformIdsByModelId = new HashMap<Long, Set<Long>>();

    public final Map<Long, Long> campaignPublicationListId = new HashMap<Long, Long>();
    public final Map<Long, Long> companyWhiteListPublicationListId = new HashMap<Long, Long>();
    public final Map<Long, Long> companyBlackListPublicationListId = new HashMap<Long, Long>();
    public final Map<Long, PublicationListDto> publicationLists = new HashMap<Long, PublicationListDto>();

    public final Map<Long, Map<Long, PublisherAuditedCreativeValue>> creative2PublisherAudited = new HashMap<>();

    public final List<CountryDto> allCountries = new ArrayList<CountryDto>();

    // These ones don't get loaded, they're just a "caches" used at eligibility
    // determination time.  We use them to store a Segment's targeted Platforms,
    // and also to keep track of whether a given Segment and PublicationType are
    // eligible, or whether they're mutually exclusive.
    public final ConcurrentHashMap<Long, Set<Long>> targetedPlatformIdsBySegmentId = new ConcurrentHashMap<Long, Set<Long>>();
    public final ConcurrentHashMap<String, Boolean> eligibilityCache = new ConcurrentHashMap<String, Boolean>();

    private final AdfonicStopWatch stopWatch;

    public AdCacheBuildContext() {
        this.stopWatch = new AdfonicStopWatch();
    }

    public AdCacheBuildContext(AdfonicStopWatch stopWatch) {
        this.stopWatch = stopWatch;
    }

    public DSRejectionListener getDsListener() {
        return dsListener;
    }

    public void setDsListener(DSRejectionListener dsListener) {
        Objects.requireNonNull(dsListener);
        this.dsListener = dsListener;
    }

    public void startWatch(String taskName) {
        stopWatch.start(taskName);

    }

    public void stopWatch(String taskName) {
        stopWatch.stop(taskName);
    }

    private Map<Long, Map<String, String>> publicationAttributesMap = new HashMap<>();

    public void addPublicationAttribute(Long publicationId, String name, String value) {
        Map<String, String> onePublicationMap = publicationAttributesMap.get(publicationId);
        if (onePublicationMap == null) {
            onePublicationMap = new HashMap<>();
            publicationAttributesMap.put(publicationId, onePublicationMap);
        }
        onePublicationMap.put(name, value);
    }

    public boolean getPublicationBooleanAttribute(Long publicationId, String key, boolean defaultMissingValue) {
        Map<String, String> onePublicationMap = publicationAttributesMap.get(publicationId);
        if (onePublicationMap == null) {
            return defaultMissingValue;
        }
        String value = onePublicationMap.get(key);
        if (value == null) {
            return defaultMissingValue;
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return defaultMissingValue;
    }

    public void addCreativeRemovedPublication(long creativeId, long publicationId) {
        creativeRemovedPublications.add(creativeId + "." + publicationId);
    }

    public boolean isCreativeRemovedPublication(long creativeId, long publicationId) {
        return creativeRemovedPublications.contains(creativeId + "." + publicationId);
    }

    public void addCampaignRemovedPublication(long campaignId, long publicationId) {
        campaignRemovedPublications.add(campaignId + "." + publicationId);
    }

    public boolean isCampaignRemovedPublication(long campaignId, long publicationId) {
        return campaignRemovedPublications.contains(campaignId + "." + publicationId);
    }

    /**
     * Function check if given campaignCategoryId is excluded by this publication or publisher of publication.
     *
     * @param publication
     * @param campaignCategoryId
     * @return
     */
    public boolean isCampaignCategoryExcludedByPublicationOrPublisher(PublicationDto publication, Long campaignCategoryId) {
        Set<Long> categoryExcludedByPublication = expandedPublicationExcludedCategoryIds.get(publication.getId());
        if (categoryExcludedByPublication != null) {
            if (categoryExcludedByPublication.contains(campaignCategoryId)) {
                return true;
            }
        }

        Set<Long> categoryExcludedByPublisher = expandedPublisherExcludedCategoryIds.get(publication.getPublisher().getId());
        if (categoryExcludedByPublisher != null) {
            return categoryExcludedByPublisher.contains(campaignCategoryId);
        }
        return false;
    }

    public Set<Long> getPublicationStatedCategoryIds(long publicationId) {
        Set<Long> statedCategoryIds = publicationStatedCategoryIds.get(publicationId);
        return statedCategoryIds == null ? Collections.EMPTY_SET : statedCategoryIds;
    }

    public Set<Long> getExpandedSegmentExcludedCategoryIds(long segmentId) {
        Set<Long> expanded = expandedSegmentExcludedCategoryIds.get(segmentId);
        return expanded == null ? Collections.EMPTY_SET : expanded;
    }

    public Set<Long> getExpandedSegmentIncludedCategoryIds(long segmentId) {
        Set<Long> expanded = expandedSegmentIncludedCategoryIds.get(segmentId);
        return expanded == null ? Collections.EMPTY_SET : expanded;
    }

    public boolean isAdSpaceAllowed(long segmentId, long adSpaceId) {
        Set<Long> adSpaceIds = adSpaceIdsBySegmentId.get(segmentId);
        return adSpaceIds == null || adSpaceIds.contains(adSpaceId);
    }

    public boolean isFeatureApproved(long adSpaceId, Feature feature) {
        Set<Feature> deniedFeatures = deniedFeaturesByAdSpaceId.get(adSpaceId);
        if (deniedFeatures != null && deniedFeatures.contains(feature)) {
            return false;
        }
        Set<Feature> approvedFeatures = approvedFeaturesByAdSpaceId.get(adSpaceId);
        return approvedFeatures == null || approvedFeatures.contains(feature);
    }

    public TrackingIdentifierType getEffectiveTrackingIdentifierType(PublicationDto pub) {
        if (pub.getTrackingIdentifierType() != null) {
            return pub.getTrackingIdentifierType();
        } else {
            return defaultTrackingIdentifierTypesByPublicationTypeId.get(pub.getPublicationTypeId());
        }
    }

    public Set<Long> getTransparentNetworkIds(long campaignId) {
        Set<Long> transparentNetworkIds = transparentNetworkIdsByCampaignId.get(campaignId);
        return transparentNetworkIds == null ? Collections.EMPTY_SET : transparentNetworkIds;
    }

    public Set<Long> getPublicationApprovedCreativeIds(long publicationId) {
        final Set<Long> creativeIds = publicationApprovedCreativeIds.get(publicationId);
        return creativeIds == null ? Collections.EMPTY_SET : creativeIds;
    }

    public Set<Long> getPublicationDeniedCreativeIds(long publicationId) {
        final Set<Long> creativeIds = publicationDeniedCreativeIds.get(publicationId);
        return creativeIds == null ? Collections.EMPTY_SET : creativeIds;
    }

    public Set<Long> getPublisherApprovedCreativeIds(long publisherId) {
        final Set<Long> creativeIds = publisherApprovedCreativeIds.get(publisherId);
        return creativeIds == null ? Collections.EMPTY_SET : creativeIds;
    }

    public boolean isEligibleBasedOnPlatforms(SegmentDto segment, long publicationTypeId) {
        // Check our cached results first
        String key = segment.getId() + "." + publicationTypeId;
        Boolean eligible = eligibilityCache.get(key);
        if (eligible != null) {
            return eligible; // cool, already done
        }

        // Not cached yet...determine eligibility now
        Set<Long> targetedPlatformIds = targetedPlatformIdsBySegmentId.get(segment.getId());
        if (targetedPlatformIds == null) {
            // We haven't yet derived & cached this Segment's targeted Platforms
            targetedPlatformIds = new HashSet<Long>();
            // Segment.platforms
            for (Long platformId : segment.getPlatformIds()) {
                targetedPlatformIds.add(platformId);
            }
            // Segment.models
            for (Long modelId : segment.getModelIds()) {
                Set<Long> modelPlatformIds = platformIdsByModelId.get(modelId);
                if (modelPlatformIds != null) {
                    targetedPlatformIds.addAll(modelPlatformIds);
                }
            }
            // Segment.vendors
            for (Long vendorId : segment.getVendorIds()) {
                Set<Long> vendorPlatformIds = platformIdsByVendorId.get(vendorId);
                if (vendorPlatformIds != null) {
                    targetedPlatformIds.addAll(vendorPlatformIds);
                }
            }
            // Cache it for next time...since we're likely to have to derive
            // eligibility for the same Segment but a different PublicationType
            targetedPlatformIdsBySegmentId.putIfAbsent(segment.getId(), targetedPlatformIds);
        }

        if (targetedPlatformIds.isEmpty()) {
            // No models/vendors/platforms were targeted...no restrictions
            eligible = true;
        } else {
            // Targeted platforms and the publication type's platforms must intersect
            eligible = CollectionUtils.containsAny(targetedPlatformIds, platformIdsByPublicationTypeId.get(publicationTypeId));
        }
        /*
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Segment id=" + segment.getId() + " " + targetedPlatformIds + (eligible ? "" : " NOT") + " eligible for PublicationType id=" + publicationTypeId + " "
                    + platformIdsByPublicationTypeId.get(publicationTypeId));
        }
        */
        // Cache it for quick access next time, since multiple creatives can
        // reference the same Segment
        eligibilityCache.putIfAbsent(key, eligible);
        return eligible;
    }

    public boolean isEligibleBasedOnInventoryTargeting(CreativeDto creative, AdSpaceDto adSpace, DSRejectionListener dsRejectionListener) {
        PublicationDto publication = adSpace.getPublication();
        if (creative.isClosedMode()) {
            //If creative is in closed mode, it can go to any inventory so return true.
            return true;
        }
        if (publication.getApproveDate() == null) {
            //If publication has not been approved that  means we cant to Inventory targetting as its not eligible
            dsRejectionListener.ineligible(adSpace, creative, "Publication " + publication.getId() + " ApproveDate is null");
            return false;
        }

        // Advertiser's Company level Whitelist (COMPANY.COMPANY_PUBLICATION_WHITE_LIST_ID -> PUBLICATION_LIST)
        long publicationId = publication.getId();
        Long whiteListId = companyWhiteListPublicationListId.get(creative.getCampaign().getAdvertiser().getCompany().getId());
        if (whiteListId != null) {
            PublicationListDto whiteList = publicationLists.get(whiteListId);
            if (!whiteList.getPublicationIds().contains(publicationId)) {
                //Publication Id do not exists in Company WhiteList so can not server it
                dsRejectionListener.ineligible(adSpace, creative, "Publication Id do not exists in Advertiser's Company Publication WhiteList " + whiteList.getPublicationIds());
                return false;
            }
            if (publication.getApproveDate() != null && whiteList.getSnapshotDateTime() != null && publication.getApproveDate().after(whiteList.getSnapshotDateTime())) {
                //If Publication was approved after snapshot time of this PublicationList
                dsRejectionListener.ineligible(adSpace, creative, "Publication ApproveDate: " + publication.getApproveDate()
                        + " is after Advertiser's Company Publication WhiteList snapshot timestamp: " + whiteList.getSnapshotDateTime());
                return false;
            }
        }

        // Advertiser's Company level Blacklist (COMPANY.COMPANY_PUBLICATION_BLACK_LIST_ID -> PUBLICATION_LIST)
        Long blackListId = companyBlackListPublicationListId.get(creative.getCampaign().getAdvertiser().getCompany().getId());
        if (blackListId != null) {
            PublicationListDto blackList = publicationLists.get(blackListId);
            if (blackList.getPublicationIds().contains(publicationId)) {
                //Publication Id not exists in Company BlackList so can not server it
                dsRejectionListener.ineligible(adSpace, creative, "Publication Id exists in Advertiser's Company Publication BlackList: " + blackList.getPublicationIds());
                return false;
            }
            if (publication.getApproveDate() != null && blackList.getSnapshotDateTime() != null && publication.getApproveDate().after(blackList.getSnapshotDateTime())) {
                //If Publication was approved after snapshot time of this PublicationList
                dsRejectionListener.ineligible(adSpace, creative, "Publication ApproveDate: " + publication.getApproveDate()
                        + " is after Advertiser's Company Publication BlackList snapshot: " + blackList.getSnapshotDateTime());
                return false;
            }
        }

        // Campaign level Blacklist/Whitelist (CAMPAIGN.CAMPAIGN_PUBLICATION_LIST_ID -> PUBLICATION_LIST)

        Long whiteBlackListId = campaignPublicationListId.get(creative.getCampaign().getId());
        if (whiteBlackListId != null) {
            //If PublicationList exists at Campaign Level
            PublicationListDto publicationList = publicationLists.get(whiteBlackListId);
            if (publicationList.isWhiteList()) {
                if (!publicationList.getPublicationIds().contains(publicationId)) {
                    //Publication Id do not exists in Campaign WhiteList so can not server it
                    dsRejectionListener.ineligible(adSpace, creative, "Publication Id do not exists in Campaign Publication WhiteList: " + publicationList.getPublicationIds());
                    return false;
                }
                if (publication.getApproveDate() != null && publicationList.getSnapshotDateTime() != null
                        && publication.getApproveDate().after(publicationList.getSnapshotDateTime())) {
                    //If Publication was approved after snapshot time of this PublicationList
                    dsRejectionListener.ineligible(adSpace, creative, "Publication ApproveDate: " + publication.getApproveDate()
                            + " is after Campaign Publication WhiteList snapshot: " + publicationList.getSnapshotDateTime());
                    return false;
                }

            } else {
                if (publicationList.getPublicationIds().contains(publicationId)) {
                    //Publication Id not exists in Company BlackList so can not server it
                    dsRejectionListener.ineligible(adSpace, creative, "Publication Id exists in Campaign Publication BlackList: " + publicationList.getPublicationIds());
                    return false;
                }
                if (publication.getApproveDate() != null && publicationList.getSnapshotDateTime() != null
                        && publication.getApproveDate().after(publicationList.getSnapshotDateTime())) {
                    //If Publication was approved after snapshot time of this PublicationList
                    dsRejectionListener.ineligible(adSpace, creative, "Publication ApproveDate: " + publication.getApproveDate()
                            + " is after Campaign Publication BlackList snapshot: " + publicationList.getSnapshotDateTime());
                    return false;
                }
            }
        }

        return true;
    }

    public void addSegmentTargettedPublisher(Long segmentId, Long publisherId) {
        Set<Long> existingPublisherIds = segmentTargettedPublisherIds.get(segmentId);
        if (existingPublisherIds == null) {
            existingPublisherIds = new HashSet<Long>();
            segmentTargettedPublisherIds.put(segmentId, existingPublisherIds);
        }
        existingPublisherIds.add(publisherId);
    }

    public Set<Long> getSegmentTargettedPublisherBySegmentId(Long segmentId) {
        return segmentTargettedPublisherIds.get(segmentId);
    }

    public void addTargettedPublisher(Long publisherId) {
        allTargettedPublisherIds.add(publisherId);
    }

    public boolean isTargettedPublisherId(Long publisherId) {
        return allTargettedPublisherIds.contains(publisherId);
    }

    public boolean isBidTypeDenied(PublicationDto publication, BidType bidType) {
        boolean denied = false;

        //If publisher is RTB enabled then all BidTypes are allowed, even if somehow someone updated DB directly
        //So avoid checking for publishers where Publisher is RTB enabled

        if (!publication.getPublisher().isRtbEnabled() && !BidType.CPM.equals(bidType)) {
            //Check if Publication Level Denied BidType Exists
            Set<BidType> deniedBidTypes = this.publicationDeniedBidTypes.get(publication.getId());
            if (deniedBidTypes == null) {
                //If not then check Publisher Level Denied BidType Exists
                deniedBidTypes = this.publisherDeniedBidTypes.get(publication.getPublisher().getId());
            }
            //If Publication level or Publisher level Denied BidType exists then check if given BidType is Blocked
            if (deniedBidTypes != null) {
                denied = deniedBidTypes.contains(bidType);
            }
        }

        return denied;
    }

    // Publication attributes not required by adserver but required by the loader
    public static final class TransientPublicationAttributes {
        public boolean autoApproval;
        public int minAge;
        public int maxAge;
        public boolean incentivized;
        public BigDecimal genderMix;
    }

    // similar req as above for externally audited creative
    public static final class PublisherAuditedCreativeValue {

        private final Status status;
        private final Date latestFetchTime;
        private final String externalReference;

        public PublisherAuditedCreativeValue(Status status, Date latestFetchTime, String externalReference) {
            this.status = status;
            this.latestFetchTime = latestFetchTime;
            this.externalReference = externalReference;
        }

        public Status getStatus() {
            return status;
        }

        public Date getLatestFetchTime() {
            return latestFetchTime;
        }

        public String getExternalReference() {
            return externalReference;
        }

    }

}

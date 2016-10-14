package com.byyd.middleware.creative.service.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignBid;
import com.adfonic.domain.Category;
import com.adfonic.domain.Channel;
import com.adfonic.domain.Country;
import com.adfonic.domain.Creative;
import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Model;
import com.adfonic.domain.NamedUtils;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Publication;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.Segment;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.Vendor;
import com.adfonic.util.AgeRangeTargetingLogic;
import com.byyd.middleware.creative.service.CreativeEligibilityManager;

@Service("creativeEligibilityManager")
public class CreativeEligibilityManagerJpaImpl implements CreativeEligibilityManager {
    @PersistenceContext(unitName = "adfonic-domain")
    private EntityManager em;

    /**
     * This method checks to see if a Creative is eligible for a given AdSpace.
     * If the Creative is not eligible against the AdSpace, the supplied list
     * will be populated with all reasons why not.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isCreativeEligible(Creative creative, AdSpace adSpace, List<String> reasonsWhyNot) {
        // Avoid all the fetch strategy issues by using our live injected EntityManager.
        // Simply grab live EM-backed instances of Creative and AdSpace...
        Creative dbCreative = em.find(Creative.class, creative.getId());
        AdSpace dbAdSpace = em.find(AdSpace.class, adSpace.getId());

        boolean eligible = true;

        switch (dbAdSpace.getStatus()) {
        case DORMANT:
        case DELETED:
            eligible = false;
            reasonsWhyNot.add("adSpace.status=" + dbAdSpace.getStatus());
            break;
        default:
            break;
        }

        Publication pub = dbAdSpace.getPublication();

        switch (pub.getStatus()) {
        case ACTIVE:
        case PENDING:
        case PAUSED:
            break;
        default:
            eligible = false;
            reasonsWhyNot.add("pub.status=" + pub.getStatus());
            break;
        }

        if (!dbAdSpace.getFormats().contains(dbCreative.getFormat())) {
            eligible = false;
            reasonsWhyNot.add("!adSpace.formats(" + NamedUtils.namedCollectionToString(dbAdSpace.getFormats()) + ").contains(creative.format="
                    + dbCreative.getFormat().getSystemName() + ")");
        }

        // check Extended Creative Type Features
        eligible &= checkExtendedCreativeTypeFeatures(reasonsWhyNot, dbCreative, dbAdSpace);

        // check Campaign bidType
        eligible &= checkCampaignBid(reasonsWhyNot, dbCreative, dbAdSpace);

        // Check segment
        eligible &= checkSegement(reasonsWhyNot, dbCreative, dbAdSpace, pub);

        // Ordinarily we filter out backfill creatives for AdSpaces that don't allow backfill.
        // But before we do, make sure the Company or Publication haven't explicitly approved
        // this one...we do allow exceptions like that.  Note that this check is done
        // irrespective of Publication.autoApproval.
        eligible &= checkBackfillCreatives(reasonsWhyNot, dbCreative, dbAdSpace, pub);

        return eligible && isCreativeEligible(dbCreative, pub, reasonsWhyNot, false);
    }

    private boolean checkExtendedCreativeTypeFeatures(List<String> reasonsWhyNot, Creative dbCreative, AdSpace dbAdSpace) {
        boolean eligible = true;
        if (dbCreative.getExtendedCreativeType() != null) {
            for (Feature feature : dbCreative.getExtendedCreativeType().getFeatures()) {
                if (!dbAdSpace.isFeatureApproved(feature)) {
                    eligible &= false;
                    reasonsWhyNot.add("!adSpace.isFeatureApproved(" + feature + ")");
                }
            }
        }
        return eligible;
    }

    private boolean checkCampaignBid(List<String> reasonsWhyNot, Creative dbCreative, AdSpace dbAdSpace) {
        boolean eligible = true;

        //If not a house ad
        if (dbCreative.getCampaign().getCurrentBid() != null) {
            boolean denied = false;
            //Field only to show who has blocked publication or publisher
            String blockedBy = "Publication";
            if (!dbAdSpace.getPublication().getPublisher().isRtbEnabled() && !BidType.CPM.equals(dbCreative.getCampaign().getCurrentBid().getBidType())) {
                //Check if Publication Level Denied BidType Exists
                Set<BidType> deniedBidTypes = dbAdSpace.getPublication().getBlockedBidTypes();
                if (deniedBidTypes == null || deniedBidTypes.isEmpty()) {
                    //If not then check Publisher Level Denied BidType Exists
                    deniedBidTypes = dbAdSpace.getPublication().getPublisher().getBlockedBidTypes();
                    blockedBy = "Publisher";
                }
                //If Publication level or Publisher level Denied BidType exists then check if given BidType is Blocked
                if (deniedBidTypes != null) {
                    denied = deniedBidTypes.contains(dbCreative.getCampaign().getCurrentBid().getBidType());
                }
            }
            if (denied) {
                eligible = false;
                reasonsWhyNot.add("BidType(" + dbCreative.getCampaign().getCurrentBid().getBidType().getName() + ") is blocked by " + blockedBy);
            }
        }
        return eligible;
    }

    private boolean checkSegement(List<String> reasonsWhyNot, Creative dbCreative, AdSpace dbAdSpace, Publication pub) {
        boolean eligible = true;
        Segment segment = dbCreative.getSegment();
        if (segment != null && !segment.getAdSpaces().isEmpty() && !segment.getAdSpaces().contains(dbAdSpace)) {
            eligible &= false;
            reasonsWhyNot.add("!segment(id=" + segment.getId() + ").adSpaces.contains(adSpace)");
        }
        // Changed logic to reflect that there is only one category per publication
        if ((segment != null && !segment.getChannels().isEmpty()) && (!segment.getChannels().contains(pub.getCategory().getChannel()))) {
            eligible &= false;
            reasonsWhyNot.add("!segment(id=" + segment.getId() + ").channels.contains(pub.category.channel)");
        }
        return eligible;
    }

    private boolean checkBackfillCreatives(List<String> reasonsWhyNot, Creative dbCreative, AdSpace dbAdSpace, Publication pub) {
        boolean eligible = true;
        if ((dbCreative.getCampaign().getAdvertiser().getCompany().isBackfill() && !dbAdSpace.isBackfillEnabled())
                && (!pub.getPublisher().getApprovedCreatives().contains(dbCreative) && !pub.getApprovedCreatives().contains(dbCreative))) {
            // Nope, it wasn't explicitly approved, so it's not eligible
            eligible = false;
            reasonsWhyNot.add("campaign.advertiser.company.backfill==true && !adSpace.backfillEnabled && !pub.publisher(id=" + pub.getPublisher().getId()
                    + ".approvedCreatives.contains(creative) && !pub.approvedCreatives.contains(creative)");
        }
        return eligible;
    }

    /**
     * This method checks to see if a Creative is eligible for a given Publication.
     * If the Creative is not eligible against the Publication, the supplied list
     * will be populated with all reasons why not.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isCreativeEligible(Creative creative, Publication pub, List<String> reasonsWhyNot) {
        // Avoid all the fetch strategy issues by using our live injected EntityManager.
        // Simply grab live EM-backed instances of Creative and Publication...
        Creative dbCreative = em.find(Creative.class, creative.getId());
        Publication dbPub = em.find(Publication.class, pub.getId());

        return isCreativeEligible(dbCreative, dbPub, reasonsWhyNot, true);
    }

    /**
     * This method checks to see if a Creative is eligible for a given Publication.
     * If the Creative is not eligible against the Publication, the supplied list
     * will be populated with all reasons why not.
     */
    private boolean isCreativeEligible(Creative creative, Publication publication, List<String> reasonsWhyNot, boolean checkAdSpaceFormats) {
        boolean eligible = true;

        eligible &= checkAdSpaceFormats(creative, publication, reasonsWhyNot, checkAdSpaceFormats);

        if (publication.isAutoApproval()) {
            // Make sure the creative hasn't explicitly been denied for the Publication
            if (publication.getDeniedCreatives().contains(creative)) {
                eligible = false;
                reasonsWhyNot.add("pub.autoApproval && pub.deniedCreatives.contains(creative)");
            }
        } else if ((!creative.getCampaign().isHouseAd() || !publication.getPublisher().getCompany().equals(creative.getCampaign().getAdvertiser().getCompany()))
                && (!publication.getApprovedCreatives().contains(creative) && !publication.getPublisher().getApprovedCreatives().contains(creative))) {
            // Make sure the creative has manually been approved by the Publication or Company
            eligible = false;
            reasonsWhyNot.add("!pub.autoApproval && !pub.approvedCreatives.contains(creative) && !pub.publisher(id=" + publication.getPublisher().getId()
                    + ").approvedCreatives.contains(creative)");
        }

        if (creative.isPublicationRemoved(publication)) {
            eligible = false;
            reasonsWhyNot.add("creative.removedPublications.containsKey(pub)");
        }

        Campaign campaign = creative.getCampaign();

        // If it's a house ad, make sure the campaign company is the publication company.
        // If it's not a house ad, make sure the campaign company is NOT the publication company.
        if (campaign.isHouseAd()) {
            if (!campaign.getAdvertiser().getCompany().equals(publication.getPublisher().getCompany())) {
                eligible = false;
                reasonsWhyNot.add("houseAd && campaign.advertiser.company != pub.publisher.company");
            }
        } else if (campaign.getAdvertiser().getCompany().equals(publication.getPublisher().getCompany())) {
            eligible = false;
            reasonsWhyNot.add("campaign.advertiser.company == pub.publisher.company && !houseAd");
        }

        Date now = new Date();
        if (campaign.getEndDate() != null && !campaign.getEndDate().after(now)) {
            eligible = false;
            reasonsWhyNot.add("!campaign.endDate(" + campaign.getEndDate() + ").after(now)");
        }

        if (campaign.getPublicationList() != null && !campaign.getPublicationList().getWhiteList()) {
            eligible = false;
            reasonsWhyNot.add("campaign.getPublicationList().getWhiteList() = " + campaign.getPublicationList().getWhiteList() + "(blacklist)");
        }

        //JIRA #AF-1390
        //Campaign with AdxEnabled should be served on Sites too
        eligible &= checkAdxEnabled(publication, reasonsWhyNot, campaign);

        // Check Transparent Network
        eligible &= checkTransparentNetwork(publication, reasonsWhyNot, campaign);

        Segment segment = creative.getSegment();

        // Check Campaign
        eligible &= checkCampaign(publication, reasonsWhyNot, campaign, segment);

        // Check Category Exclusion for Targeting
        eligible &= checkCategoryExclusionForTargeting(publication, creative, segment, reasonsWhyNot);

        // Check Segment
        eligible &= checkSegment(publication, reasonsWhyNot, segment);

        ExtendedCreativeType extendedCreativeType = creative.getExtendedCreativeType();
        if (extendedCreativeType != null) {
            Set<ExtendedCreativeType> publisherWhitelist = publication.getPublisher().getThirdPartyTagVendorWhitelist();
            Set<ExtendedCreativeType> publicationWhitelist = publication.getThirdPartyTagVendorWhitelist();
            boolean whitelisted = (publisherWhitelist != null && publisherWhitelist.contains(extendedCreativeType))
                    || (publicationWhitelist != null && publicationWhitelist.contains(extendedCreativeType));
            if (!whitelisted) {
                eligible = false;
                reasonsWhyNot.add("!(publisher || publication).isWhitelisted(" + extendedCreativeType + ")");
            }
        }

        return eligible;
    }

    private boolean checkAdSpaceFormats(Creative creative, Publication pub, List<String> reasonsWhyNot, boolean checkAdSpaceFormats) {
        boolean eligible = true;
        if (checkAdSpaceFormats) {
            List<String> adSpaceFormatReasons = new ArrayList<String>();
            for (AdSpace adSpace : pub.getAdSpaces()) {
                if (!adSpace.getFormats().contains(creative.getFormat())) {
                    adSpaceFormatReasons.add("!adSpace(id=" + adSpace.getId() + ").formats(" + NamedUtils.namedCollectionToString(adSpace.getFormats())
                            + ").contains(creative.format=" + creative.getFormat().getSystemName() + ")");
                }
            }
            if (!adSpaceFormatReasons.isEmpty()) {
                eligible = false;
                reasonsWhyNot.addAll(adSpaceFormatReasons);
            }
        }
        return eligible;
    }

    private boolean checkAdxEnabled(Publication pub, List<String> reasonsWhyNot, Campaign campaign) {
        boolean eligible = true;
        if (campaign.isInstallTrackingEnabled()) {
            if (pub.isInstallTrackingDisabled()) {
                eligible = false;
                reasonsWhyNot.add("Campaign is installTrackingEnable but Publication is not installTrackingEnable[campaign.installTrackingEnabled && pub.installTrackingDisabled]");
            } else if (!campaign.isInstallTrackingAdXEnabled()) {
                if (pub.getPublicationType().getMedium() != Medium.APPLICATION) {
                    eligible = false;
                    reasonsWhyNot
                            .add("Campaign is installTrackingEnable and not adxEnabled and pub.publicationType is not APPLICATION[campaign.installTrackingEnabled && pub.publicationType("
                                    + pub.getPublicationType().getSystemName() + ").medium(" + pub.getPublicationType().getMedium() + ") != APPLICATION]");
                } else if (pub.getEffectiveTrackingIdentifierType() != TrackingIdentifierType.DEVICE) {
                    eligible = false;
                    reasonsWhyNot
                            .add("Campaing is installTrackingEnable and not adxEnabled and publication effectiveTrackingIdentifierType is not DEVICE[campaign.installTrackingEnabled && pub.effectiveTrackingIdentifierType("
                                    + pub.getEffectiveTrackingIdentifierType() + ") != DEVICE]");
                }
            }

        }
        return eligible;
    }

    private boolean checkTransparentNetwork(Publication pub, List<String> reasonsWhyNot, Campaign campaign) {
        boolean eligible = true;
        TransparentNetwork pubTN = pub.getTransparentNetwork();
        if (pubTN == null) {
            if (!campaign.getTransparentNetworks().isEmpty()) {
                eligible = false;
                reasonsWhyNot.add("pubTN == null && !campaign.transparentNetworks.isEmpty");
            }
        } else {
            if (!campaign.getTransparentNetworks().isEmpty()) {
                if (!campaign.getTransparentNetworks().contains(pubTN)) {
                    eligible = false;
                    reasonsWhyNot.add("!campaign.transparentNetworks.contains(pubTN id=" + pubTN.getId() + ")");
                }
            } else if (pubTN.isClosed() && !pubTN.getAdvertisers().contains(campaign.getAdvertiser().getCompany())) {
                eligible = false;
                reasonsWhyNot.add("pubTN.closed && !pubTN.advertisers.contains(campaign.advertiser.company id=" + campaign.getAdvertiser().getCompany().getId() + ")");
            }
        }
        return eligible;
    }

    private boolean checkCampaign(Publication pub, List<String> reasonsWhyNot, Campaign campaign, Segment segment) {
        boolean eligible = true;
        if (!campaign.isHouseAd()) {
            CampaignBid campaignBid = campaign.getCurrentBid();
            if (campaignBid == null) {
                // This should never happen, but it has: https://tickets.adfonic.com/browse/AF-629
                eligible = false;
                reasonsWhyNot.add("Campaign !houseAd but has no currentBid");
            } else {
                eligible &= checkRateCard(pub, reasonsWhyNot, campaignBid, segment);
            }
        }
        return eligible;
    }

    private boolean checkRateCard(Publication pub, List<String> reasonsWhyNot, CampaignBid campaignBid, Segment segment) {
        boolean eligible = true;
        RateCard pubRateCard = pub.getEffectiveRateCard(campaignBid.getBidType());
        if (pubRateCard != null) {
            BigDecimal payout = pub.getPublisher().getPayout(campaignBid);
            if (pubRateCard.getDefaultMinimum() != null && payout.compareTo(pubRateCard.getDefaultMinimum()) < 0) {
                eligible = false;
                reasonsWhyNot.add("payout < pub.rateCard.defaultMinimum for " + campaignBid.getBidType());
            }

            eligible &= checkCountryRateCard(reasonsWhyNot, campaignBid, segment, pubRateCard, payout);
        }
        return eligible;
    }

    private boolean checkCountryRateCard(List<String> reasonsWhyNot, CampaignBid campaignBid, Segment segment, RateCard pubRateCard, BigDecimal payout) {
        boolean eligible = true;
        if (segment != null && !segment.getCountries().isEmpty()) {
            for (Country country : segment.getCountries()) {
                BigDecimal minimumBid = pubRateCard.getMinimumBid(country);
                if (minimumBid != null && payout.compareTo(minimumBid) < 0) {
                    eligible &= false;
                    reasonsWhyNot.add("!segment.countries.isEmpty && payout < pub.rateCard.minimumBid(" + country.getIsoCode() + ") for " + campaignBid.getBidType());
                }
            }
        }
        return eligible;
    }

    private boolean checkSegment(Publication pub, List<String> reasonsWhyNot, Segment segment) {
        boolean eligible = true;
        if (segment != null) {

            eligible &= checkMedium(pub, reasonsWhyNot, segment);

            if (segment.getSafetyLevel().ordinal() > pub.getSafetyLevel().ordinal()) {
                eligible = false;
                reasonsWhyNot.add("segment.safetyLevel > !pub.safetyLevel");
            }

            if (pub.isIncentivized() && !segment.isIncentivizedAllowed()) {
                eligible = false;
                reasonsWhyNot.add("pub.incentivized && !segment.incentivizedAllowed");
            }

            eligible &= checkAgeAndGenderMix(pub, reasonsWhyNot, segment);

            eligible &= checkTargetedPlatforms(pub, reasonsWhyNot, segment);

            eligible &= checkChannelTargetting(segment, pub, reasonsWhyNot);
        }
        return eligible;
    }

    private boolean checkMedium(Publication pub, List<String> reasonsWhyNot, Segment segment) {
        boolean eligible = true;
        Medium targetedMedium = segment.getMedium();
        if (targetedMedium != null && targetedMedium != Medium.UNKNOWN) {
            Medium publicationMedium = pub.getPublicationType().getMedium();
            if (publicationMedium != Medium.UNKNOWN && publicationMedium != targetedMedium) {
                eligible = false;
                reasonsWhyNot.add("segment.medium!=null && segment.medium!=Medium.UNKNOWN && pub.medium != Medium.UNKNOWN && pub.medium != segment.medium");
            }
        }
        return eligible;
    }

    private boolean checkAgeAndGenderMix(Publication pub, List<String> reasonsWhyNot, Segment segment) {
        boolean eligible = true;
        int segmentMinAge = segment.getMinAge();
        int segmentMaxAge = segment.getMaxAge();
        int pubMinAge = pub.getMinAge();
        int pubMaxAge = pub.getMaxAge();
        if (!AgeRangeTargetingLogic.areAgeRangesEligible(segmentMinAge, segmentMaxAge, pubMinAge, pubMaxAge)) {
            eligible = false;
            reasonsWhyNot.add("segment.ageRange(" + segmentMinAge + "-" + segmentMaxAge + ") !~ pub.ageRange(" + pubMinAge + "-" + pubMaxAge + ")");
        }

        if (segment.getGenderMix() != null && pub.getGenderMix() != null) {
            double genderDelta = Math.abs(segment.getGenderMix().doubleValue() - pub.getGenderMix().doubleValue());
            if (genderDelta == 1.0) {
                eligible = false;
                reasonsWhyNot.add("segment.genderMix(" + segment.getGenderMix() + ") !~ pub.genderMix(" + pub.getGenderMix() + ")");
            }
        }
        return eligible;
    }

    private boolean checkTargetedPlatforms(Publication pub, List<String> reasonsWhyNot, Segment segment) {
        boolean eligible = true;
        // Check targeted platforms against publication type's platforms
        Set<Platform> targetedPlatforms = new HashSet<Platform>();
        targetedPlatforms.addAll(segment.getPlatforms());
        for (Vendor vendor : segment.getVendors()) {
            for (Model model : vendor.getModels()) {
                targetedPlatforms.addAll(model.getPlatforms());
            }
        }
        for (Model model : segment.getModels()) {
            targetedPlatforms.addAll(model.getPlatforms());
        }
        if (!targetedPlatforms.isEmpty() && !CollectionUtils.containsAny(targetedPlatforms, pub.getPublicationType().getPlatforms())) {
            eligible = false;
            reasonsWhyNot.add("!segment.targetedPlatforms(models+vendors+platforms).containsAny(pub.publicationType.platforms)");
        }
        return eligible;
    }

    static boolean checkCategoryExclusionForTargeting(Publication pub, Creative creative, Segment segment, List<String> reasonsWhyNot) {
        // Campaign category
        boolean eligible = checkCreativeCampaignCategory(pub, creative, reasonsWhyNot);

        // Publication category
        eligible &= checkPublicationCategory(pub, segment, reasonsWhyNot);

        return eligible;
    }

    private static boolean checkCreativeCampaignCategory(Publication pub, Creative creative, List<String> reasonsWhyNot) {
        boolean eligible = true;
        Category creativeCampaignCategory = creative.getCampaign().getCategory();
        if (creativeCampaignCategory != null) {
            // Publication excluded categories
            for (Category excludedCategory : pub.getExcludedCategories()) {
                eligible &= checkCreativePublicationExpandedCategory(excludedCategory, creativeCampaignCategory, reasonsWhyNot);
            }

            // Publisher excluded categories
            for (Category publisherExcludedCategory : pub.getPublisher().getExcludedCategories()) {
                eligible &= checkCreativePublisherExpandedCategory(publisherExcludedCategory, creativeCampaignCategory, reasonsWhyNot);
            }

        } else {
            if (CollectionUtils.isNotEmpty(pub.getExcludedCategories())) {
                eligible &= false;
                reasonsWhyNot.add("Publication has excluded categories but creative category is null");
            }
            if (CollectionUtils.isNotEmpty(pub.getPublisher().getExcludedCategories())) {
                eligible &= false;
                reasonsWhyNot.add("Publisher has excluded categories but creative category is null");
            }
        }
        return eligible;
    }

    private static boolean checkCreativePublicationExpandedCategory(Category excludedCategory, Category creativeCampaignCategory, List<String> reasonsWhyNot) {
        boolean eligible = true;
        for (Category expandedCategory : expandCategory(excludedCategory)) {
            if (creativeCampaignCategory.equals(expandedCategory)) {
                eligible &= false;
                if (expandedCategory.equals(excludedCategory)) {
                    reasonsWhyNot.add("pub excludes creative.category id=" + excludedCategory.getId() + " (" + excludedCategory.getName() + ")");
                } else {
                    reasonsWhyNot.add("pub excludes category id=" + excludedCategory.getId() + " (" + excludedCategory.getName() + "), which contains creative.category id="
                            + expandedCategory.getId() + " (" + expandedCategory.getName() + ")");
                }
            }
        }
        return eligible;
    }

    private static boolean checkCreativePublisherExpandedCategory(Category publisherExcludedCategory, Category creativeCampaignCategory, List<String> reasonsWhyNot) {
        boolean eligible = true;
        for (Category expandedCategory : expandCategory(publisherExcludedCategory)) {
            if (creativeCampaignCategory.equals(expandedCategory)) {
                eligible &= false;
                if (expandedCategory.equals(publisherExcludedCategory)) {
                    reasonsWhyNot.add("publisher excludes creative category id=" + publisherExcludedCategory.getId() + " (" + publisherExcludedCategory.getName() + ")");
                } else {
                    reasonsWhyNot.add("publisher excludes category id=" + publisherExcludedCategory.getId() + " (" + publisherExcludedCategory.getName()
                            + "), which contains creative.category id=" + expandedCategory.getId() + " (" + expandedCategory.getName() + ")");
                }
            }
        }
        return eligible;
    }

    private static boolean checkPublicationCategory(Publication pub, Segment segment, List<String> reasonsWhyNot) {
        boolean eligible = true;
        Category pubCategory = pub.getCategory();
        if (segment != null) {
            if (CollectionUtils.isNotEmpty(segment.getExcludedCategories())) {
                if (pubCategory != null) {
                    eligible = checkPublicationSegmentExcludedCategories(pub, segment, reasonsWhyNot, pubCategory);
                } else {
                    eligible = false;
                    reasonsWhyNot.add("!pub.excludedCategories.empty && creative.getCampaign().getCategory() is null");
                }
            }
        } else {
            eligible = false;
            reasonsWhyNot.add("creative segment is empty");
        }
        return eligible;
    }

    private static boolean checkPublicationSegmentExcludedCategories(Publication pub, Segment segment, List<String> reasonsWhyNot, Category pubCategory) {
        boolean eligible = true;
        for (Category excludedCategory : segment.getExcludedCategories()) {
            eligible &= checkPublicationExpandedCategories(pub, reasonsWhyNot, pubCategory, excludedCategory);
        }
        return eligible;
    }

    private static boolean checkPublicationExpandedCategories(Publication pub, List<String> reasonsWhyNot, Category pubCategory, Category excludedCategory) {
        boolean eligible = true;
        for (Category expandedCategory : expandCategory(excludedCategory)) {
            if (expandedCategory.equals(pubCategory)) {
                eligible &= false;
                if (expandedCategory.equals(excludedCategory)) {
                    reasonsWhyNot.add("segment excludes pub.category id=" + excludedCategory.getId() + " (" + excludedCategory.getName() + ")");
                } else {
                    reasonsWhyNot.add("segment excludes category id=" + excludedCategory.getId() + " (" + excludedCategory.getName() + "), which contains pub.category id="
                            + expandedCategory.getId() + " (" + expandedCategory.getName() + ")");
                }
            }

            // Also check Publication.statedCategories
            eligible &= checkPublicationStatedCategories(pub, reasonsWhyNot, excludedCategory, expandedCategory);
        }
        return eligible;
    }

    private static boolean checkPublicationStatedCategories(Publication pub, List<String> reasonsWhyNot, Category excludedCategory, Category expandedCategory) {
        boolean eligible = true;
        for (Category statedCategory : pub.getStatedCategories()) {
            if (expandedCategory.equals(statedCategory)) {
                eligible &= false;
                if (expandedCategory.equals(excludedCategory)) {
                    reasonsWhyNot.add("segment excludes pub.statedCategory id=" + excludedCategory.getId() + " (" + excludedCategory.getName() + ")");
                } else {
                    reasonsWhyNot.add("segment excludes category id=" + excludedCategory.getId() + " (" + excludedCategory.getName() + "), which contains pub.statedCategory id="
                            + expandedCategory.getId() + " (" + expandedCategory.getName() + ")");
                }
            }
        }
        return eligible;
    }

    protected static boolean checkChannelTargetting(Segment segment, Publication pub, List<String> reasonsWhyNot) {
        boolean eligible = true;
        if (segment.isChannelEnabled()) {

            Channel publicationChannel = pub.getCategory().getChannel();

            Set<Channel> segmentChannels = segment.getChannels();
            if (publicationChannel != null) {
                eligible = checkSegmentChannels(reasonsWhyNot, publicationChannel, segmentChannels);
            } else {
                if (CollectionUtils.isNotEmpty(segmentChannels)) {
                    eligible = false;
                    reasonsWhyNot.add("segment is ChannelEnabled but publication Channel is Null");
                }
            }
        }
        return eligible;
    }

    private static boolean checkSegmentChannels(List<String> reasonsWhyNot, Channel publicationChannel, Set<Channel> segmentChannels) {
        boolean eligible = true;
        if (segmentChannels == null || segmentChannels.isEmpty()) {
            eligible = false;
            reasonsWhyNot.add("segment is ChannelEnabled but no channels selected");
        } else {
            if (!segmentChannels.contains(publicationChannel)) {
                eligible = false;
                reasonsWhyNot.add("!segmentChannels.contains(publicationChannel)");
            }
        }
        return eligible;
    }

    private static Set<Category> expandCategory(Category category) {
        Set<Category> expanded = new HashSet<Category>();
        expandCategory(category, expanded);
        return expanded;
    }

    private static void expandCategory(Category category, Set<Category> expanded) {
        expanded.add(category);
        for (Category child : category.getChildren()) {
            expandCategory(child, expanded);
        }
    }
}

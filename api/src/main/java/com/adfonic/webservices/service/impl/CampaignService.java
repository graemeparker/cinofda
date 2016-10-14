package com.adfonic.webservices.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.Campaign.InventoryTargetingType;
import com.adfonic.domain.CampaignBid;
import com.adfonic.domain.CampaignTimePeriod;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Category;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Country;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.DefaultRateCard;
import com.adfonic.domain.DefaultRateCard_;
import com.adfonic.domain.Geotarget;
import com.adfonic.domain.GeotargetType;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.RateCard;
import com.adfonic.domain.RateCard_;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Segment_;
import com.adfonic.domain.TargetPublisher;
import com.adfonic.domain.TargetPublisher_;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.User;
import com.adfonic.util.CurrencyUtils;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.dto.CampaignBidDTO;
import com.adfonic.webservices.dto.mapping.PublicationListObjConverter;
import com.adfonic.webservices.exception.AuthorizationException;
import com.adfonic.webservices.exception.InvalidStateException;
import com.adfonic.webservices.exception.ServiceException;
import com.adfonic.webservices.exception.ValidationException;
import com.adfonic.webservices.service.ICampaignService;
import com.adfonic.webservices.service.ICreativeService;
import com.adfonic.webservices.service.IPublicationListService;
import com.adfonic.webservices.service.IUtilService;
import com.adfonic.webservices.util.DspAccess;
import com.byyd.middleware.account.filter.TargetPublisherFilter;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.campaign.service.BiddingManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

@Service
public class CampaignService implements ICampaignService {
    private static final transient Logger LOG = Logger.getLogger(CampaignService.class.getName());

	
	private static final FetchStrategy CAMPAIGN_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Campaign_.advertiser)
        .addInner(Campaign_.defaultLanguage)
        .addLeft(Campaign_.currentBid)
        .addLeft(Campaign_.segments)
        .addLeft(Campaign_.timePeriods)
        .addLeft(Campaign_.transparentNetworks)
        .addInner(Advertiser_.company)
        .addInner(Company_.accountManager)
        //.addLeft(Segment_.geotargets)
        .addLeft(Segment_.ipAddresses)
        .addLeft(Segment_.browsers)
        .addLeft(Segment_.excludedModels)
        //.addLeft(Creative_.assetBundleMap)
        .addLeft(Creative_.destination)
        //.addLeft(AssetBundle_.assetMap)
        .build();

    private static final FetchStrategy CREATIVE_FETCH_STRATEGY = new FetchStrategyBuilder()
                                                                .addLeft(Creative_.destination)
                                                                .addInner(Creative_.format)
                                                                //.addLeft(Creative_.assetBundleMap)
                                                                .addLeft(Creative_.campaign)
                                                                .build();

    private static final FetchStrategy DEFAULT_RATE_CARD_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(DefaultRateCard_.rateCard)
        .build();

    private static final FetchStrategy RATE_CARD_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addLeft(RateCard_.minimumBidMap)
        .build();

    @Autowired
    private IUtilService utilService;

    @Autowired
    private ICreativeService creativeService;// TODO - invert this dep

    @Autowired
    private CompanyManager companyManager;

    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private TargetingManager targetingManager;
    
    @Autowired
    private BiddingManager biddingManager;
    
    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    private PublisherManager publisherManager;
    
    @Autowired
    private PublicationManager publicationManager;

    @Autowired
    private CommonManager commonManager;

    public Campaign createMinimalCampaign(Advertiser advertiser, Collection<Segment> segments, String name, String defaultLanguage) {
        utilService.validatePresence("name", name);

        Campaign campaign = new Campaign(advertiser);

        campaign.setName(name);
        campaign.setDefaultLanguage(commonManager.getLanguageByIsoCode(defaultLanguage != null ? defaultLanguage : "en"));
        // go for english. TODO - UI prompts for english language translation

        if (CollectionUtils.isNotEmpty(segments)) {
            campaign.getSegments().addAll(segments);
        }

        //MAD-3356 - todo not set on API
        campaign.setReference("");// to match UI; let user override if desired
        campaign.setDisableLanguageMatch(true);// match UI
        // NO need - default;//campaign.setStatus(Campaign.Status.NEW);

        //PPN compatibility - TODO PPN
        campaign.setCategory(commonManager.getCategoryByName(Category.NOT_CATEGORIZED_NAME));

        //campaign.getAdvertiser().getCampaigns().add(campaign);

        verifyUniqueName(advertiser, campaign);
        return (campaign);

    }


    public void submit(Campaign campaign) {
        if (!campaign.isEditable()) {
            throw new InvalidStateException("Campaign already submitted once!");
        }

        List<Creative> creatives = creativeManager.getAllCreativesForCampaign(campaign, CREATIVE_FETCH_STRATEGY);
        if (CollectionUtils.isEmpty(creatives)) {
            throw new ServiceException(ErrorCode.INVALID_STATE, "No creatives found for campaign!");
        }

        validate(campaign);

        campaign.setStatus(Campaign.Status.PENDING);

        if (campaignManager.isPersisted(campaign)) {
            campaignManager.update(campaign);
        } else {
            campaignManager.newCampaign(campaign);
        }

        // TODO - change this also with composite
        for (Creative creative : creatives) {
            creativeService.submit(creative);
        }
    }


    public void authorize(User user, Campaign campaign) {
        
        if (user==null || campaign.getAdvertiser().getCompany().equals(user.getCompany())) {// TODO - Re-check regarding agency
            return;
        }

        throw new AuthorizationException();
    }


    public void delete(Campaign campaign) {
        if (campaign.isEditable()) {
            campaign.transitionStatus(Campaign.Status.DELETED);
            campaign = campaignManager.update(campaign);
        } else {
            throw new ServiceException(ErrorCode.INVALID_STATE, "Will not delete, given the campaign state!");
        }
    }


    public void validate(Campaign campaign) {// TODO - checks like targetting overlap
        verifyUniqueName(campaign.getAdvertiser(), campaign);// not reqd currently; defensive check

        utilService.validatePresence("description", campaign.getDescription());

        if (campaign.getOverallBudget() != null && campaign.getOverallSpend() != null) {
            if (campaign.getOverallBudget().compareTo(campaign.getOverallSpend()) < 0) {
                throw new RuntimeException("overall budget spend problem");
            }
        }

        if(campaignManager.isPersisted(campaign)) {
            validateMinimumBid(campaign);
        }

        validateBudgets(campaign);

        validateFrequencyCap(campaign);

        validateSegment(segment(campaign));

        // ? campaign.setDailyBudgetWeekday(null);
        // ? campaign.setDailyBudgetWeekend(null);

        // TODO - change this with composite
        //for (Creative creative : campaign.getCreatives()) {// Validate member creatives
            //creativeService.validate(creative);
        //}

    }


    private void verifyUniqueName(Advertiser advertiser, Campaign campaign) {
        if (!isCampaignNameUnique(campaign.getName(), advertiser, campaign)) {
            throw new ValidationException("Already a campaign with same name exists for advertiser");
        }
    }


    private boolean isCampaignNameUnique(String name, Advertiser advertiser, Campaign campaign) {
        if (advertiser.getId() == 0) {
            // The advertiser itself hasn't been persisted yet
            return true;
        } else {
            return campaignManager.isCampaignNameUnique(name, advertiser, campaign.getId() == 0 ? null : campaign);
        }
    }

    private void validateBudgets(Campaign campaign) {
        BigDecimal dailyBudget = campaign.getDailyBudget();
        BigDecimal dailyBudgetWeekday = campaign.getDailyBudgetWeekday();
        BigDecimal dailyBudgetWeekend = campaign.getDailyBudgetWeekend();

        validateDailyBudgetOverlaps(dailyBudget, dailyBudgetWeekday, dailyBudgetWeekend);

        validateMinBudget(dailyBudget);
        validateMinBudget(dailyBudgetWeekday);
        validateMinBudget(dailyBudgetWeekend);
        validateMinBudget(campaign.getOverallBudget());
    }

    // TODO - From CampaignBean - for validating min daily budget
    /* VALIDATE_MINIMUM_DAILY_BUDGET block - START */
    public static final BigDecimal MIN_DAILY_BUDGET = new BigDecimal(10.00);
    public void validateMinBudget(BigDecimal newAmount) {
        if (newAmount != null && newAmount.compareTo(MIN_DAILY_BUDGET) < 0) {
            throw new ValidationException("Budget is below the minimum amount of "+ CurrencyUtils.CURRENCY_FORMAT_USD.format(MIN_DAILY_BUDGET));
        }
    }
    /* VALIDATE_MINIMUM_DAILY_BUDGET block - ENDS */


    private void validateDailyBudgetOverlaps(BigDecimal dailyBudget, BigDecimal dailyBudgetWeekday, BigDecimal dailyBudgetWeekend) {
        if ((dailyBudgetWeekday != null || dailyBudgetWeekend != null) && dailyBudget != null) {
            throw new ValidationException("Daily budget has to be set on the same level");
        }
    }


    private void validateSegment(Segment segment) {
        Set<Geotarget> geotargets = segment.getGeotargets();
        if (!geotargets.isEmpty()) {
            if (!segment.getCountries().isEmpty()) {
                throw new ValidationException("Targeting overlap - Countries and Geotargets. Not allowed!");
            }

            Country geoTargetCountry = null;
            GeotargetType geoTargetType = null;
            for (Geotarget geotarget : geotargets) {
                if (geoTargetType == null) {
                    geoTargetCountry = geotarget.getCountry();
                    geoTargetType = geotarget.getGeotargetType();
                    continue;
                }
                
                GeotargetType curType=geotarget.getGeotargetType();
                if (ObjectUtils.notEqual(geoTargetType, curType) || !geoTargetCountry.equals(geotarget.getCountry())) {
                    throw new ValidationException("At the moment, all geotargets should be of the same country and type");
                }
            }
        }

        if (!segment.getPlatforms().isEmpty() &&
                (!segment.getModels().isEmpty() || !segment.getVendors().isEmpty())) {
            throw new ValidationException("Will not simultaneously target Platforms along with Models or Vendors!");
        }

        if (!segment.getIpAddresses().isEmpty() && !segment.getOperators().isEmpty()) {
            throw new ValidationException("Targeting overlap - Operators and IP addresses. Not allowed!");
        }

        // TODO - if ConnectionType=WIFI and operators not empty, throw error. UI does not do this validation - so will break if done

        // TODO - if models and excludedModels both non-empty, throw error. UI does not - it might as well be a feature. so don't
    }


    public void setDailyBudgets(Campaign campaign, BudgetType budgetType, BigDecimal dailyBudget, BigDecimal dailyBudgetWeekday, BigDecimal dailyBudgetWeekend) {
        if (isNonMonetary(budgetType) || isNonMonetary(campaign.getBudgetType())) {
            throw new ValidationException("Non-monetary campaign budget. Write-into not supported!");
        }
        
        if (dailyBudgetWeekday != null || dailyBudgetWeekend != null) {
            if (dailyBudget != null) {
                throw new ValidationException("Daily budget has to be set on the same level");
            }

            if (dailyBudgetWeekend != null) {
                campaign.setDailyBudgetWeekend(dailyBudgetWeekend);
            }

            if (dailyBudgetWeekday != null) {
                campaign.setDailyBudgetWeekday(dailyBudgetWeekday);
            }
        } else if (dailyBudget != null) {
            campaign.setDailyBudgetWeekday(null);
            campaign.setDailyBudgetWeekend(null);
        } else {
            return;// be careful modifying this function; it deals with 6 budget variables' state
        }

        campaign.setDailyBudget(dailyBudget);
    }
    

    private static boolean isNonMonetary(BudgetType budgetType) {
        return budgetType != null && budgetType != BudgetType.MONETARY;
    }

    
    public void setInstallTracking(Campaign campaign, Boolean installTrackingEnabled, String applicationID) {
        if (installTrackingEnabled != null) {
            campaign.setInstallTrackingEnabled(installTrackingEnabled);
            if (!installTrackingEnabled) {
                campaign.setApplicationID(null);
            }
        }

        if (!utilService.hasNoPresence(applicationID)) {
            campaign.setApplicationID(applicationID);
        }

        if (!campaign.isInstallTrackingAdXEnabled() && !campaign.isInstallTrackingEnabled() && !utilService.hasNoPresence(campaign.getApplicationID())) {// defensive check with hasNoPresence
            throw new ValidationException("No point in application id with install tracking disabled!");
        }

        if ((campaign.isInstallTrackingAdXEnabled() || campaign.isInstallTrackingEnabled()) && utilService.hasNoPresence(campaign.getApplicationID())) {//This is the important check
            throw new ValidationException("applicationID mandatory for install tracking!");
        }
    }


    public void setTimePeriods(Campaign campaign, Collection<CampaignTimePeriod> timePeriods) {
        Campaign.Status currentStatus = campaign.getStatus();
        if (currentStatus == Campaign.Status.STOPPED || currentStatus == Campaign.Status.COMPLETED) {
            throw new ValidationException("Cannot update schedule in the current campaign status!");
        }

        for (CampaignTimePeriod existingTimePeriod : campaign.getTimePeriods()) {
            targetingManager.delete(existingTimePeriod);
        }

        campaign.clearTimePeriods();


        for (CampaignTimePeriod timePeriod : timePeriods) {
            try {
                campaign = targetingManager.addTimePeriodToCampaign(campaign, timePeriod);
            } catch (IllegalArgumentException e) {
                throw new ValidationException(e.getMessage() + " " + timePeriod);
            }
        }
    }

    // ------------ JPA bid validation start ------------------------
    // Because of how JPA works, we cannot set a current bid before the campaign is persisted. Therefore, during Campaign creation,
    // there must be a validation method that will validate the current bid based on its intended value before it is set
    // in the campaign. The methods requiring BidType and amount have been copied, with these 2 elements passed through a DTO
    public void validateNewBid(CampaignBidDTO bid, Campaign campaign) {
        if(bid == null) {
            throw new ValidationException("There must be a current-bid with a value no less than the allowed minimum bid.");
        }
        BigDecimal bidAmount = bid.getAmount();
        BidType bidType = bid.getType();
        if(bidAmount == null || bidAmount.compareTo(getBidMin(campaign, bidAmount, bidType)) < 0) {
            throw new ValidationException("There must be a current-bid with a value no less than the allowed minimum bid.");
        }
        // no fractional cents pricing changes allowed in campaign workflow
        if (!campaign.isPriceOverridden() &&
                (((bidAmount.multiply(new BigDecimal(1000)).intValue()) % 10) != 0)
                ) {
            throw new ValidationException("The amount entered is not a valid format.");
        }
    }
    public BigDecimal getBidMin(Campaign campaign, BigDecimal bidAmount, BidType bidType) {
         if (campaign.isTransparent()) {
            // If it's a premium campaign, the minimum is dictated by the networks chosen.
            return maxMinBid(campaign.getTransparentNetworks(), bidAmount, bidType, campaign);
        } else {
            // Otherwise minimums are dictated by geographical targeting
            RateCard rateCard = getDefaultRateCard(bidType);
            BigDecimal minBid = rateCard.getDefaultMinimum();
            for (Country c : segment(campaign).getCountries()) {
                minBid = minBid.max(rateCard.getMinimumBid(c));
            }
            return minBid;
        }
    }
    // find the highest min bid for a set of transparent networks
    private BigDecimal maxMinBid(Set<TransparentNetwork> transparentNetworks, BigDecimal bidAmount, BidType bidType, Campaign campaign) {
        BigDecimal maxMinBid = BigDecimal.ZERO;

        for (TransparentNetwork tn : transparentNetworks) {
            if (getMinBidMap(tn, campaign).containsKey(bidType)) {
                maxMinBid = maxMinBid.max(getMinBidMap(tn, campaign).get(bidType));
            }
        }
        return maxMinBid;
    }
    // ------------ JPA bid validation end ------------------------


    // TODO - The 5 below methods adapted faithfully from CampaignBean - for validating minimum bid
    /* VALIDATE_MINIMUM_BID block - START */
    private void validateMinimumBid(Campaign campaign){
        CampaignBid currentBid = campaign.getCurrentBid();
        BigDecimal bidAmount;
        if (currentBid == null
                || (bidAmount = campaign.getCurrentBid().getAmount()) == null
                || bidAmount.compareTo(getBidMin(campaign)) < 0) {
            throw new ValidationException("There must be a current-bid with a value no less than the allowed minimum bid.");
        }

        // no fractional cents pricing changes allowed in campaign workflow
        if (!campaign.isPriceOverridden() &&
                (((bidAmount.multiply(new BigDecimal(1000)).intValue()) % 10) != 0)
                ) {
            throw new ValidationException("The amount entered is not a valid format.");
        }
    }
    public BigDecimal getBidMin(Campaign campaign) {
        BidType bidType=campaign.getCurrentBid().getBidType();
        if (campaign.isTransparent()) {
            // If it's a premium campaign, the minimum is dictated by the networks chosen.
            return maxMinBid(campaign.getTransparentNetworks(), bidType, campaign);
        } else {
            // Otherwise minimums are dictated by geographical targeting
            RateCard rateCard = getDefaultRateCard(bidType);
            BigDecimal minBid = rateCard.getDefaultMinimum();
            for (Country c : segment(campaign).getCountries()) {
                minBid = minBid.max(rateCard.getMinimumBid(c));
            }
            return minBid;
        }
    }
    // find the highest min bid for a set of transparent networks
    private BigDecimal maxMinBid(Set<TransparentNetwork> transparentNetworks, BidType bidType, Campaign campaign) {
        BigDecimal maxMinBid = BigDecimal.ZERO;

        for (TransparentNetwork tn : transparentNetworks) {
            if (getMinBidMap(tn, campaign).containsKey(bidType)) {
                maxMinBid = maxMinBid.max(getMinBidMap(tn, campaign).get(bidType));
            }
        }
        return maxMinBid;
    }
    private  Map<BidType,BigDecimal> getMinBidMap(TransparentNetwork network, Campaign campaign) {
        Map<BidType,BigDecimal> minBidMap = new HashMap<BidType,BigDecimal>();

        for (BidType bidType : getAvailableBidTypes(campaign)) {
            RateCard rateCard = null;

            if (!network.isDefaultRateCard() && network.getRateCard(bidType) != null) {
                rateCard = network.getRateCard(bidType);
            }
            else {
                rateCard = getDefaultRateCard(bidType);
            }

            BigDecimal minBid = rateCard.getDefaultMinimum();

            for (Country c : segment(campaign).getCountries()) {
                minBid = minBid.max(rateCard.getMinimumBid(c));
            }

            minBidMap.put(bidType, minBid);
        }
        return minBidMap;
    }
    public synchronized List<BidType> getAvailableBidTypes(Campaign campaign) {
        List<BidType> availableBidTypes;
            List<BidType> allowed = Arrays.asList(BidType.values());

            if (!campaign.isTransparent()) {
                availableBidTypes = allowed;
            } else {
                // Transparent networks may not allow all bid types
                // All targeted networks must support the bid type
                for (TransparentNetwork p : campaign.getTransparentNetworks()) {
                    // for default don't remove any of the bid types
                    if (!p.isDefaultRateCard()) {
                        allowed.retainAll(p.getRateCardMap().keySet());
                    }
                }
                availableBidTypes = allowed;
            }

        return availableBidTypes;
    }
    /* VALIDATE_MINIMUM_BID block - ENDS */

    private RateCard getDefaultRateCard(BidType bidType) {
        // Two-step hack to work around NPE if we combined the DefaultRateCard and RateCard fetch strategies
        DefaultRateCard defaultRateCard = publicationManager.getDefaultRateCardByBidType(bidType, DEFAULT_RATE_CARD_FETCH_STRATEGY);
        return publicationManager.getRateCardById(defaultRateCard.getRateCard().getId(), RATE_CARD_FETCH_STRATEGY);
    }

    private static final Set<Integer> frequencyCapDurations = new HashSet<Integer>(Arrays.asList(new Integer[] { 60 * 60, 60 * 60 * 24, 60 * 60 * 24 * 7, 60 * 60 * 24 * 30 }));

    private void validateFrequencyCap(Campaign campaign) {// TODO - functionality shared with UI. 1, 12 etc from UI behavior
        Integer capImpressions = campaign.getCapImpressions();
        Integer capPeriodSeconds = campaign.getCapPeriodSeconds();
        if (capPeriodSeconds != null && !frequencyCapDurations.contains(capPeriodSeconds)) {
            throw new ValidationException("Invalid frequency cap duration!");
        }

        if (capImpressions != null && capImpressions != 0) {
            // MAD-3278
            // if (capImpressions < 1 || capImpressions > 12) {
            //     throw new ValidationException("Frequency cap should be between 1 and 12!");
            // }

            if (capPeriodSeconds == null) {
                throw new ValidationException("Cannot set frequency cap without specifying a duration!");
            }
        }
    }

    public void createNewBid(Campaign campaign, BidType bidType, BigDecimal bidAmount) {
        if (bidType == null || bidAmount == null) {
            throw new ValidationException("Campaign Bid must have a valid type and amount!");
        }

        if (!campaign.isEditable() && campaign.getCurrentBid().getBidType() != bidType) {
            throw new ValidationException("Cannot modify the bid type of a submitted campaign!");
        }

        biddingManager.newCampaignBid(campaign, bidType, bidAmount);
    }

    public Campaign findbyExternalID(String externalID) {
        return findbyExternalID(null, externalID);
    }
    
    public Campaign findbyExternalID(User user, String externalID) {
        Campaign campaign = campaignManager.getCampaignByExternalId(externalID, CAMPAIGN_FETCH_STRATEGY);

        if (campaign == null) {
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Campaign not found");
        }

        try {// TODO - change this to auto translation
            authorize(user, campaign);
        } catch (AuthorizationException e) {// Dont give the info away to user
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Campaign not found");
        }

        if (campaign.getStatus() == Campaign.Status.DELETED) {
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Entity not found");
        }

        return campaign;
    }
    
    public List<Creative> getAllCreativesForCampaign(User user, String externalID){
        Campaign campaign=findbyExternalID(user, externalID);
        return creativeManager.getAllCreativesForCampaign(campaign, CREATIVE_FETCH_STRATEGY);
    }
    
    private static final FetchStrategy creativeFs = new FetchStrategyBuilder()
													    .addLeft(Creative_.destination)
													    .build();

    /*
     * Resubmit creatives that required AdX reprovisioning
     */
    public void resubmitAdXCreatives(Campaign campaign) {
        List<Creative> creatives = creativeManager.getCreativesEligibleForAdXReprovisioning(campaign, creativeFs);
        List<Creative> updatedCreatives = creativeManager.updateCreativeStatusForAdXReprovisioning(creatives);
        for(Creative creative : updatedCreatives) {
            doSubmitCreative(creative);
        }
     }

    private void doSubmitCreative(Creative creative) {
        try {
            creativeManager.submitCreative(creative);
        } catch (Exception e) {
            LOG.log(
                    Level.SEVERE,
                    "Error generating submitting creative item id=" + creative.getId(), e);
        }
    }


    @Override
    public void setInventoryTargeting(Campaign campaign, String pubList, boolean hasSegmentParams) {
        DspAccess dspAccess = utilService.getEffectiveDspAccess(campaign.getAdvertiser().getCompany());
        if (dspAccess == null) {// will not, in current situation
            return;
        }

        campaign.setInventoryTargetingType(inventoryTargetingTypeAfterDataUpdate(campaign, pubList, hasSegmentParams, dspAccess));
    }

    @Autowired
    private IPublicationListService publicationListService;


    // Typical segment copy stuff - merge issues - order of stuff important
    private InventoryTargetingType inventoryTargetingTypeAfterDataUpdate(Campaign campaign, String pubList, boolean hasSegmentParams, DspAccess dspAccess) {
        Segment segment = segment(campaign);
        PublicationList providedBlacklist = null;
        if (pubList != null) {
            PublicationList providedWhitelist = new PublicationListObjConverter(campaign.getAdvertiser(), publicationListService, true).resolveEntity(pubList);
            if (providedWhitelist != null) {
                if (hasSegmentParams) {
                    throw new ValidationException("Invalid combination of inventory targeting parameters!");
                }
                campaign.setPublicationList(providedWhitelist);
                segment.getIncludedCategories().clear();
                segment.getTargettedPublishers().clear();
                return InventoryTargetingType.WHITELIST;
            }

            providedBlacklist = new PublicationListObjConverter(campaign.getAdvertiser(), publicationListService, false).resolveEntity(pubList);

            if (providedBlacklist == null) {
                throw new ValidationException("No list on advertiser by given name!");
            }
        }

        PublicationList campaignPublicationList = campaign.getPublicationList();
        boolean hasIncludedCategories;
        if ((hasIncludedCategories = !segment.getIncludedCategories().isEmpty()) || !segment.getTargettedPublishers().isEmpty()) {
            if (providedBlacklist != null) {
                campaign.setPublicationList(providedBlacklist);
            } else if (campaignPublicationList != null && campaignPublicationList.getWhiteList()) {
                campaign.setPublicationList(null);
            }

            return hasIncludedCategories ? InventoryTargetingType.CATEGORY : InventoryTargetingType.RUN_OF_NETWORK;
        }

        if (providedBlacklist != null) {
            throw new ValidationException("Cannot apply blacklist!");
        }

        if (campaignPublicationList != null && campaignPublicationList.getWhiteList()) {
            return InventoryTargetingType.WHITELIST; // Should be set already in this case
        }

        // Probably first time - set role based defaults
        return inventoryTargetingRoleDefaults(campaign, dspAccess);
    }


    private InventoryTargetingType inventoryTargetingRoleDefaults(Campaign campaign, DspAccess dspAccess) {
        if (dspAccess != null) {
            Set<Publisher> targetPublishers = segment(campaign).getTargettedPublishers();
            for (TargetPublisher targetPublisher : getAllTargetPublishers(dspAccess)) {
                targetPublishers.add(targetPublisher.getPublisher());
            }
            return InventoryTargetingType.RUN_OF_NETWORK;
        }

        Set<Category> includedCategories = segment(campaign).getIncludedCategories();
        for (Category category : getAllIABAndLocalTopLevelCategories()) {
            includedCategories.add(category);
        }
        return InventoryTargetingType.CATEGORY;
    }


    private List<TargetPublisher> getAllTargetPublishers(DspAccess dspAccess) {
        TargetPublisherFilter filter = new TargetPublisherFilter();
        if (dspAccess == DspAccess.RTB) {
            filter.setRtb(true);
        }
        return publisherManager.getAllTargetPublishers(filter, new FetchStrategyBuilder().addLeft(TargetPublisher_.publisher).build());
    }


    private Set<Category> getAllIABAndLocalTopLevelCategories() {
        return filterOutNonIAB(commonManager.getAllTopLevelCategories());
    }


    private static Set<Category> filterOutNonIAB(Collection<Category> categories) {
        Set<Category> iabCatSet = new HashSet<>();
        for (Category category : categories) {
            if (category.getIabId().startsWith("IAB")) {
                iabCatSet.add(category);
            }
        }
        return iabCatSet;
    }


    /*
     * Campaign/Segment domain badnesss - in one place
     */
    private static Segment segment(Campaign campaign) {
        return campaign.getSegments().get(0);
    }
}

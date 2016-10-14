package com.adfonic.data.cache;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.data.cache.ecpm.api.EcpmDataRepository;
import com.adfonic.data.cache.ecpm.datacache.PlatformBidTypePublicationKey;
import com.adfonic.data.cache.ecpm.key.AdSpaceCreativeKey;
import com.adfonic.data.cache.ecpm.key.CampaignCountryKey;
import com.adfonic.data.cache.ecpm.key.PlatformCreativeKey;
import com.adfonic.data.cache.util.Properties;
import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.SerializableCache;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.CampaignCtrInfo;
import com.adfonic.domain.cache.dto.adserver.CampaignCvrInfo;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;
import com.adfonic.domain.cache.service.AdSpaceService;
import com.adfonic.domain.cache.service.CategoryService;
import com.adfonic.domain.cache.service.CreativeService;
import com.adfonic.domain.cache.service.CurrencyService;
import com.adfonic.util.Subnet;

public class AdserverDataCacheImpl implements AdserverDataCache, SerializableCache {

    private static final long serialVersionUID = 1L;

    private static final transient Logger LOG = Logger.getLogger(AdserverDataCacheImpl.class.getName());

    private EcpmDataRepository ecpmDataRepository;

    private Properties properties;

    private CategoryService categoryService;
    private CreativeService creativeService;
    private AdSpaceService adSpaceService;
    private CurrencyService currencyService;

    @Override
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setCreativeService(CreativeService creativeService) {
        this.creativeService = creativeService;
    }

    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public AdserverDataCacheImpl(EcpmDataRepository ecpmDataRepository) {
        this.ecpmDataRepository = ecpmDataRepository;
    }

    public AdSpaceService getAdSpaceService() {
        return adSpaceService;
    }

    public void setAdSpaceService(AdSpaceService adSpaceService) {
        this.adSpaceService = adSpaceService;
    }

    private double getBidPriceForRtb(AdSpaceDto adspace, CreativeDto creative, double rgr) {
        // Look up values to aid in readability of the calculation
        double buyerPremium = adspace.getPublication().getPublisher().getBuyerPremium();
        CampaignDto campaign = creative.getCampaign();
        CompanyDto advertiserCompany = campaign.getAdvertiser().getCompany();
        CampaignBidDto campaignBid = campaign.getCurrentBid();
        BidType bidType = campaignBid.getBidType();
        double currentBid = campaignBid.getAmount();
        boolean isPMP = (campaign.getPrivateMarketPlaceDeal() != null);

        // Convert the bid to units of CPM
        currentBid = currentBid * BidType.CPM.getQuantity() / bidType.getQuantity();

        // Apply the RGR (revenue generation rate)
        currentBid = currentBid * rgr;

        // Apply the buffer for CPC or CPX campaigns
        switch (bidType) {
        case CPC:
            currentBid = currentBid * getDefaultDoubleValue("adfonic_ctr_dsp_buffer", .95);
            break;
        case CPA:
        case CPI:
            currentBid = currentBid * getDefaultDoubleValue("adfonic_cpx_dsp_buffer", .95);
            break;
        }

        // Please note that the order of the following calculations is vital!

        // Apply campaign agency discount
        currentBid = currentBid * (1 - campaign.getAgencyDiscount());

        // Apply trading desk margin
        currentBid = currentBid * (1 - getCampaignTradingDeskMargin(campaign));

        // Apply data fee
        currentBid = currentBid - campaign.getDataFee();

        // Apply rich media adserving fee
        currentBid = currentBid - campaign.getRmAdServingFee();

        // Apply media cost margin (tech fee)
        currentBid = currentBid * (1 - advertiserCompany.getMediaCostMargin());

        // Apply direct cost (unless PMP)
        if (!isPMP) {
            currentBid = currentBid / (1 + advertiserCompany.getDirectCostOrZero());
        }

        // Apply buyer premium (unless PMP)
        if (!isPMP) {
            currentBid = currentBid / (1 + buyerPremium);
        }

        // Ensure we don't bid negative values
        if (currentBid < 0) {
            currentBid = 0.0;
        }
        return currentBid;
    }

    @Override
    public double getCampaignTradingDeskMargin(CampaignDto campaign) {
        double margin = campaign.getTradingDeskMargin();

        if (campaign.isMediaCostOptimisationEnabled()) {
            Double campaignMarginRecommendation = this.ecpmDataRepository.getCampaignMarginRecommendation(campaign.getId());
            if ((campaignMarginRecommendation != null) && (campaignMarginRecommendation.doubleValue() > margin)) {
                margin = campaignMarginRecommendation.doubleValue();
            }
        }

        return margin;
    }

    private ExpectedStatsDto getExpectedStats(AdSpaceDto adSpace, CreativeDto creative, PlatformDto platform, boolean isRtb) {

        //Cap it
        double maxRgr = getDefaultDoubleValue("network_max_expected_rgr", 0.5);

        long creativeId = creative.getId();
        long adSpaceId = adSpace.getId();
        AdSpaceCreativeKey adSpaceCreativeKey = new AdSpaceCreativeKey(adSpaceId, creativeId);
        ExpectedStatsDto expectedStatsDto = ecpmDataRepository.getExpectedStats(adSpaceCreativeKey);

        if (expectedStatsDto == null) {

            Double adspaceCtr = ecpmDataRepository.getAdspaceCtr(adSpaceId);

            if (adspaceCtr == null) {
                adspaceCtr = getDefaultDoubleValue("network_default_ctr", 0.0055);
            }

            BidType bidType = creative.getCampaign().getCurrentBid().getBidType();
            double creativeCvr = calculateCreativeCvr(creative, isRtb);
            if (platform != null) {
                long platformId = platform.getId();
                Double creativeCtrIndex = ecpmDataRepository.getCreativeWeightedCtrIndex(new PlatformCreativeKey(platformId, creativeId));
                if (creativeCtrIndex != null) {
                    //adjust adspace CTR
                    adspaceCtr = adspaceCtr * creativeCtrIndex;
                }

                //SC-108 adjust creative CVR with publicationCvrIndex

                Double publicationCvrIndex = ecpmDataRepository.getPubblicationWeightedCvrIndex(new PlatformBidTypePublicationKey(platformId, bidType, adSpace.getPublication()
                        .getId()));

                if (publicationCvrIndex != null) {

                    creativeCvr = creativeCvr * publicationCvrIndex;
                }
            }

            //calculate RGR

            double defaultRgr;
            if (creative.getCampaign().getCurrentBid().getBidType().equals(BidType.CPM)) {
                defaultRgr = 1.0;
            } else {
                defaultRgr = adspaceCtr;
                if (!creative.getCampaign().getCurrentBid().getBidType().equals(BidType.CPC)) {
                    defaultRgr = creativeCvr * adspaceCtr;
                }
            }
            expectedStatsDto = new ExpectedStatsDto(adspaceCtr, creativeCvr, defaultRgr);

            ecpmDataRepository.addExpectedStats(adSpaceCreativeKey, expectedStatsDto);
        }

        if (!creative.getCampaign().getCurrentBid().getBidType().equals(BidType.CPM)) {
            expectedStatsDto.capMaxRgr(maxRgr);
        }

        return expectedStatsDto;
    }

    private double calculateCreativeCvr(CreativeDto creative, boolean isRtb) {

        Double creativeCvr = ecpmDataRepository.getCreativeCvr(creative.getId());
        if (creativeCvr == null) {
            creativeCvr = ecpmDataRepository.getCampaignCvr(creative.getCampaign().getId());
            if (creativeCvr == null) {
                if (isRtb) {
                    creativeCvr = getDefaultDoubleValue("network_default_cvr_rtb", 0.003);
                } else {
                    creativeCvr = getDefaultDoubleValue("network_default_cvr", 0.02);
                }
            }
        }
        return creativeCvr;

    }

    @Override
    public void computeEcpmInfo(AdSpaceDto adspace, CreativeDto creative, PlatformDto platform, long countryId, BigDecimal bidFloorPrice, EcpmInfo ecpmInfo) {

        if (ecpmInfo == null) {
            throw new RuntimeException("ecpmInfo not initialized!");
        }

        ecpmInfo.reset();

        CampaignDto campaign = creative.getCampaign();
        CampaignBidDto currentBid = campaign.getCurrentBid();
        if (currentBid == null) {
            //ECPM for house ads or the ads where bid is not available then ecpm will be 0
            return;
        }

        PublisherDto publisher = adspace.getPublication().getPublisher();
        boolean isRtb = publisher.getRtbConfig() != null;

        BidType bidType = currentBid.getBidType();
        ExpectedStatsDto expectedStatsDto = getExpectedStats(adspace, creative, platform, isRtb);

        if (creative.getCampaign().getCurrentBid() == null) {
            //ECPM for house ads or the ads where bid is not available then ecpm will be 0
            return;
        }
        Double expectedSettlementPrice = null;
        Double expectedRevenue = null;
        Double expectedProfit = null;
        Double bidPrice = null;
        Double weight = null;
        Double relativeWeight = null;
        Double winningProbability = 1.0;//default value will be calcued in next sprints

        switch (creative.getCampaign().getCurrentBid().getBidType()) {
        case CPM:
            expectedRevenue = creative.getCampaign().getCurrentBid().getAmount();
            break;
        default:
            double matchFactor = calculateMatchFactor(creative.getCampaign().getId(), countryId);
            double expectedOdds = expectedStatsDto.getPriorOdds() * matchFactor;
            double expected_rgr = expectedOdds / (1 + expectedOdds);
            expectedRevenue = expected_rgr * creative.getCampaign().getCurrentBid().getAmount() * 1000;
            break;
        }

        //apply agency discount and publisher revenue share
        expectedRevenue = expectedRevenue * (1 - campaign.getAgencyDiscount());

        if (!isRtb) {
            //NON Rtb expectedRevenue * publisherRevShare
            expectedSettlementPrice = expectedRevenue * publisher.getCurrentRevShare();
            bidPrice = expectedSettlementPrice;
            expectedProfit = expectedRevenue - expectedSettlementPrice;
        } else {
            if (bidFloorPrice == null) {
                expectedSettlementPrice = 0.0;
            } else {
                expectedSettlementPrice = bidFloorPrice.doubleValue();
            }
            double buyerPremium = publisher.getBuyerPremium();
            expectedProfit = expectedRevenue - expectedSettlementPrice * (1 + buyerPremium) * winningProbability;
            bidPrice = getBidPriceForRtb(adspace, creative, expectedStatsDto.getExpectedRgr());
        }

        weight = expectedProfit;
        if (weight <= 0.0) {
            weight = 0.0;
        } else {
            switch (bidType) {
            case CPM:
                double campaignCtrTarget = getDefaultDoubleValue("default_ctr_target", 0.01);
                CampaignCtrInfo campaignCtrInfo = ecpmDataRepository.getCampaignCtrInfo(creative.getCampaign().getId());
                if (campaignCtrInfo != null) {
                    //If RunningCampaignTargetCtr exists use it and discard default ctr target
                    campaignCtrTarget = campaignCtrInfo.getTargetCtr();
                }
                double ctrThreshold = campaign.isMediaCostOptimisationEnabled() ? getDefaultDoubleValue("cpm_ctr_underperformance_threshold_media_cost_opt", 0.7)
                        : getDefaultDoubleValue("cpm_ctr_underperformance_threshold", 0.5);
                if (expectedStatsDto.getExpectedCtr() < campaignCtrTarget * ctrThreshold) {
                    weight = 0.0;
                } else {
                    weight = weight * Math.min(1.0, expectedStatsDto.getExpectedCtr() / campaignCtrTarget);
                    if (campaignCtrInfo != null) {
                        //adjust weight only if Campaign current ctr exists
                        weight = weight * Math.min(1.0, expectedStatsDto.getExpectedCtr() / campaignCtrInfo.getCurrentCtr());
                    }
                }

                break;
            case CPC:
                double cpcCampaignCtrTarget = getDefaultDoubleValue("default_cpc_ctr_target", 0.0001);
                CampaignCtrInfo cpcCampaignCtrInfo = ecpmDataRepository.getCampaignCtrInfo(creative.getCampaign().getId());
                if (cpcCampaignCtrInfo != null) {
                    //If RunningCampaignTargetCtr exists use it and discard default ctr target
                    cpcCampaignCtrTarget = cpcCampaignCtrInfo.getTargetCtr();
                }
                double cpcCtrThreshold = getDefaultDoubleValue("cpc_ctr_underperformance_threshold", 0.5);
                if (expectedStatsDto.getExpectedCtr() < cpcCampaignCtrTarget * cpcCtrThreshold) {
                    weight = 0.0;
                } else {
                    weight = weight * Math.min(1.0, expectedStatsDto.getExpectedCtr() / cpcCampaignCtrTarget);
                    if (cpcCampaignCtrInfo != null) {
                        //adjust weight only if Campaign current ctr exists
                        weight = weight * Math.min(1.0, expectedStatsDto.getExpectedCtr() / cpcCampaignCtrInfo.getCurrentCtr());
                    }
                }

                if (creative.getCampaign().isInstallTrackingEnabled() || creative.getCampaign().isConversionTrackingEnabled()
                        || creative.getCampaign().isInstallTrackingAdXEnabled()) {
                    //Adjust Campaign weights of CPC bid type, for those where campaign is install/conversion trackable
                    //If you chaning this code make sure u ask DBA team to update the ralted proc which insert data in
                    // RUNNING_CAMPAIGN_TARGET_CVR
                    CampaignCvrInfo campaignCvrInfo = ecpmDataRepository.getCampaignCvrInfo(creative.getCampaign().getId());
                    if (campaignCvrInfo != null) {
                        //If RunningCampaignTargetCtr exists use it and discard default ctr target
                        double cvrThreshold = getDefaultDoubleValue("cpc_cvr_underperformance_threshold", 0.5);
                        if (expectedStatsDto.getExpectedCvr() < campaignCvrInfo.getTargetCvr() * cvrThreshold) {
                            weight = 0.0;
                        } else {
                            weight = weight * Math.min(1.0, expectedStatsDto.getExpectedCvr() / campaignCvrInfo.getTargetCvr())
                                    * Math.min(1.0, expectedStatsDto.getExpectedCvr() / campaignCvrInfo.getCurrentCvr());
                        }
                    }

                }

                break;
            default:
                //In future we will change CPC weight
                //and CPX weight
                break;
            }

        }
        // For Non-RTB, apply the campaign Boost factor
        if (!isRtb) {
            double campaignBoostFactor = creative.getCampaign().getBoostFactor();
            weight = weight * campaignBoostFactor;
        }

        //Set Expected Revenue
        ecpmInfo.setExpectedRevenue(expectedRevenue);
        ecpmInfo.setExpectedSettlementPrice(expectedSettlementPrice);
        ecpmInfo.setExpectedProfit(expectedProfit);
        ecpmInfo.setBidPrice(bidPrice);
        ecpmInfo.setWeight(weight);
        ecpmInfo.setWinningProbability(winningProbability);

    }

    private double calculateMatchFactor(long campaignId, long countryId) {
        double matchFactor = 1.0;

        //Currently only country weighting is available so match factor will be calculated based on country only
        Double countryWeight = this.getCampaignCountryWeight(campaignId, countryId);
        if (countryWeight != null) {
            matchFactor = countryWeight * matchFactor;
        }
        //Age

        //gender
        //        LOG.log(Level.FINE, "matchFactor=" + matchFactor);
        return matchFactor;
    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {

            ((SerializableCache) ecpmDataRepository).logCounts(description, logger, level);
        }
    }

    @Override
    public double getPublicationWeightedCvrIndex(PlatformDto platform, CampaignDto campaign, long publicationId) {
        BidType bidType;
        Double publicationWeightedCvrIndex = null;
        if (platform != null) {
            //For house ads CurrentBid will be null
            if (campaign.getCurrentBid() != null) {
                bidType = campaign.getCurrentBid().getBidType();
                publicationWeightedCvrIndex = ecpmDataRepository.getPubblicationWeightedCvrIndex(new PlatformBidTypePublicationKey(platform.getId(), bidType, publicationId));
            }
        }

        if (publicationWeightedCvrIndex == null) {
            publicationWeightedCvrIndex = 1.0;
        }
        return publicationWeightedCvrIndex;
    }

    @Override
    public SystemVariable getSystemVariableByName(String variableName) {
        return ecpmDataRepository.getSystemVariableByName(variableName);
    }

    @Override
    public ExpectedStatsDto getExpectedStats(long adspaceId, long creativeId) {

        return ecpmDataRepository.getExpectedStats(new AdSpaceCreativeKey(adspaceId, creativeId));

    }

    @Override
    public Double getCampaignCvr(long creativeId) {
        return ecpmDataRepository.getCampaignCvr(creativeId);
    }

    @Override
    public Double getCreativeCvr(long creativeId) {
        return ecpmDataRepository.getCreativeCvr(creativeId);
    }

    @Override
    public Double getAdspaceCtr(long adspaceId) {
        return ecpmDataRepository.getAdspaceCtr(adspaceId);
    }

    @Override
    public Double getCampaignCountryWeight(long campaignId, long countryId) {
        Double returnValue = ecpmDataRepository.getCampaignCountryWeight(new CampaignCountryKey(campaignId, countryId));
        if (returnValue == null) {
            //No Country weighting found for this campaign, that means country weighting is 1
            returnValue = 1.0;
        }
        return returnValue;
    }

    @Override
    public double getCreativeWeightedCtrIndex(PlatformDto platform, long creativeId) {
        Double creativeWeightedCtrIndex = null;
        if (platform != null) {
            creativeWeightedCtrIndex = ecpmDataRepository.getCreativeWeightedCtrIndex(new PlatformCreativeKey(platform.getId(), creativeId));
        }
        if (creativeWeightedCtrIndex == null) {
            creativeWeightedCtrIndex = 1.0;
        }
        return creativeWeightedCtrIndex;
    }

    @Override
    public double getDefaultDoubleValue(String fieldName, double defaultValue) {
        SystemVariable systemVariable = getSystemVariableByName(fieldName);
        if (systemVariable == null) {
            LOG.log(Level.WARNING, "Variable " + fieldName + " not defined in table system_variable");
            return defaultValue;
        }
        return systemVariable.getDoubleValue();
    }

    @Override
    public void addExpectedStats(long adspaceId, long creativeId, ExpectedStatsDto expectedStatsDto) {

        AdSpaceCreativeKey adSpaceCreativeKey = new AdSpaceCreativeKey(adspaceId, creativeId);

        this.ecpmDataRepository.addExpectedStats(adSpaceCreativeKey, expectedStatsDto);

    }

    @Override
    public void addCreativeCvr(long creativeId, double value) {

        this.ecpmDataRepository.addCreativeCvr(creativeId, value);
    }

    @Override
    public void addCampaignCvr(long campaignId, double value) {

        this.ecpmDataRepository.addCampaignCvr(campaignId, value);
    }

    @Override
    public void addAdspaceCtr(long adspaceId, double value) {

        this.ecpmDataRepository.addAdspaceCtr(adspaceId, value);
    }

    @Override
    public void addSystemVariable(SystemVariable systemVariable) {

        this.ecpmDataRepository.addSystemVariable(systemVariable);
    }

    @Override
    public void addCampaignCountryWeight(long campaignId, long countryId, Double value) {

        this.ecpmDataRepository.addCampaignCountryWeight(campaignId, countryId, value);
    }

    @Override
    public void addCampaignRunningCtr(long campaignId, Double targetCtr, Double currentCtr) {

        this.ecpmDataRepository.addCampaignRunningCtr(campaignId, targetCtr, currentCtr);
    }

    @Override
    public void addCampaignRunningCvr(long campaignId, Double targetCvr, Double currentCvr) {

        this.ecpmDataRepository.addCampaignRunningCvr(campaignId, targetCvr, currentCvr);
    }

    @Override
    public CampaignCtrInfo getCampaignCtrInfo(long campaignId) {

        return this.ecpmDataRepository.getCampaignCtrInfo(campaignId);
    }

    @Override
    public CampaignCvrInfo getCampaignCvrInfo(long campaignId) {

        return this.ecpmDataRepository.getCampaignCvrInfo(campaignId);
    }

    @Override
    public void addCreativeWeightedCtrIndex(long creativeId, long platformId, double weightedCtrIndex) {

        this.ecpmDataRepository.addCreativeWeightedCtrIndex(creativeId, platformId, weightedCtrIndex);
    }

    @Override
    public void addPublicationWeightedCvrIndex(long publicationId, long platformId, BidType bidType, double weightedCtrIndex) {

        this.ecpmDataRepository.addPublicationWeightedCvrIndex(publicationId, platformId, bidType, weightedCtrIndex);
    }

    @Override
    public Double getCampaignMarginRecommendation(long campaignId) {
        return this.ecpmDataRepository.getCampaignMarginRecommendation(campaignId);
    }

    @Override
    public void addCampaignMarginRecommendation(long campaignId, double margin) {
        this.ecpmDataRepository.addCampaignMarginRecommendation(campaignId, margin);
    }

    // Categories
    @Override
    public void addExpendedCategoryIds(Long categoryId, Set<Long> listOfExpendedCategories) {

        this.categoryService.addExpendedCategoryIds(categoryId, listOfExpendedCategories);
    }

    @Override
    @Deprecated
    public Set<Long> getExpandedCategoryIds(Long categoryId) {

        return this.categoryService.getExpandedCategoryIds(categoryId);
    }

    @Override
    public boolean isExistsInExpandedCategoryIds(Long parentCategoryId, Long lookupCategoryId) {

        return this.categoryService.isExistsInExpandedCategoryIds(parentCategoryId, lookupCategoryId);
    }

    @Override
    @Deprecated
    public Set<Long> getExpandedCreativeCategoryIds(Long creativeId) {

        return this.categoryService.getExpandedCreativeCategoryIds(creativeId);
    }

    @Override
    @Deprecated
    public Set<Long> getExpandedPublicationCategoryIds(Long publicationId) {

        return this.categoryService.getExpandedPublicationCategoryIds(publicationId);
    }

    @Override
    public Set<String> getCachedPluginCategories(Long publicationId, String pluginName) {

        return this.categoryService.getCachedPluginCategories(publicationId, pluginName);
    }

    @Override
    public void cachePluginCategories(Long publicationId, String pluginName, Set<String> pluginCategories) {

        this.categoryService.cachePluginCategories(publicationId, pluginName, pluginCategories);
    }

    @Override
    public void afterDeserialize() {

        this.categoryService.afterDeserialize();
    }

    @Override
    public void beforeSerialization() {

        this.categoryService.beforeSerialization();
    }

    //Creative Service
    @Override
    public void addCreativeToCache(CreativeDto creative) {

        this.creativeService.addCreativeToCache(creative);
    }

    @Override
    public CreativeDto getCreativeByExternalID(String externalID) {

        return this.creativeService.getCreativeByExternalID(externalID);
    }

    @Override
    public CreativeDto getCreativeById(Long id) {

        return this.creativeService.getCreativeById(id);
    }

    @Override
    public CreativeDto[] getAllCreatives() {

        return this.creativeService.getAllCreatives();
    }

    @Override
    public CreativeDto[] getPluginCreatives() {

        return this.creativeService.getPluginCreatives();
    }

    @Override
    public void addAdSpaceEligibleCreative(Long adSpaceId, Set<AdspaceWeightedCreative> list, List<CountryDto> allCountries) {

        this.creativeService.addAdSpaceEligibleCreative(adSpaceId, list, allCountries);
    }

    @Override
    public AdspaceWeightedCreative[] getEligibleCreatives(Long adSpaceId) {

        return this.creativeService.getEligibleCreatives(adSpaceId);
    }

    @Override
    public Set<Long> getEligibleCreativeIdsForCountry(Long countryId) {

        return this.creativeService.getEligibleCreativeIdsForCountry(countryId);
    }

    @Override
    public PluginCreativeInfo getPluginCreativeInfo(CreativeDto creative) {

        return this.creativeService.getPluginCreativeInfo(creative);
    }

    @Override
    public PluginCreativeInfo getPluginCreativeInfo(Long creativeId) {

        return this.creativeService.getPluginCreativeInfo(creativeId);
    }

    @Override
    public void addRecentlyStoppedCreative(CreativeDto creative) {

        this.creativeService.addRecentlyStoppedCreative(creative);
    }

    @Override
    public CreativeDto getRecentlyStoppedCreativeById(Long id) {

        return this.creativeService.getRecentlyStoppedCreativeById(id);
    }

    @Override
    public void stopCampaign(Long campaignID) {

        this.creativeService.stopCampaign(campaignID);
    }

    @Override
    public void stopAdvertiser(Long advertiserId) {

        this.creativeService.stopAdvertiser(advertiserId);
    }

    @Override
    public void addSegmentSubnets(Long segmentId, Set<Subnet> subnets) {

        this.creativeService.addSegmentSubnets(segmentId, subnets);
    }

    @Override
    public Set<Subnet> getSubnetsBySegmentId(Long segmentId) {

        return this.creativeService.getSubnetsBySegmentId(segmentId);
    }

    @Override
    public Map<Long, AdspaceWeightedCreative[]> getAllEligibleCreatives() {

        throw new RuntimeException("avoid infinite loop");
        //		return this.getAllEligibleCreatives();
    }

    @Override
    public BigDecimal convertToBidCurrencyFromUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount) {
        return currencyService.convertToBidCurrencyFromUsd(adSpace, gmtTimeId, amount);
    }

    @Override
    public BigDecimal convertFromBidCurrencyToUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount) {
        return currencyService.convertFromBidCurrencyToUsd(adSpace, gmtTimeId, amount);
    }

    @Override
    public void addCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId, BigDecimal amount) {
        currencyService.addCurrencyConversionRate(fromCurrency, toCurrency, gmtTimeId, amount);
    }

    @Override
    public void clearAllConversionRate() {
        currencyService.clearAllConversionRate();
    }

    @Override
    public BigDecimal getCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId) {
        return currencyService.getCurrencyConversionRate(fromCurrency, toCurrency, gmtTimeId);
    }

    // AdSpace Service
    @Override
    public void addAddSpaceToCache(AdSpaceDto adSpace) {
        this.adSpaceService.addAddSpaceToCache(adSpace);
    }

    @Override
    public AdSpaceDto getAdSpaceByExternalID(String externalID) {
        return this.adSpaceService.getAdSpaceByExternalID(externalID);
    }

    @Override
    public AdSpaceDto getAdSpaceById(Long id) {
        return this.adSpaceService.getAdSpaceById(id);
    }

    @Override
    public AdSpaceDto[] getAllAdSpaces() {
        return this.adSpaceService.getAllAdSpaces();
    }

    @Override
    public void addDormantAdSpaceExternalId(String adSpaceExternalId) {
        this.adSpaceService.addDormantAdSpaceExternalId(adSpaceExternalId);
    }

    @Override
    @Deprecated
    public Set<String> getDormantAdSpaceExternalIds() {
        return this.adSpaceService.getDormantAdSpaceExternalIds();
    }

    @Override
    public boolean isDormantAdSpace(String externalId) {
        return this.adSpaceService.isDormantAdSpace(externalId);
    }

    @Override
    public void addPublicationMayViewPricing(Long publicationId) {
        this.adSpaceService.addPublicationMayViewPricing(publicationId);
    }

    @Override
    public boolean mayPublicationViewPricing(Long publicationId) {
        return this.adSpaceService.mayPublicationViewPricing(publicationId);
    }

    @Override
    public Long getPublisherIdByExternalID(String externalID) {
        return this.adSpaceService.getPublisherIdByExternalID(externalID);
    }

    @Override
    public void addPublisherByExternalId(String publisherExternalId, Long publisherId) {
        this.adSpaceService.addPublisherByExternalId(publisherExternalId, publisherId);
    }

    @Override
    public void addAssociatePublisher(Long id, String associateReference, Long parentId) {
        adSpaceService.addAssociatePublisher(id, associateReference, parentId);

    }

    @Override
    public Long getAssociatePublisherID(Long parentId, String associateReference) {
        return adSpaceService.getAssociatePublisherID(parentId, associateReference);
    }

}

package com.adfonic.domain.cache.ext;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.CampaignCtrInfo;
import com.adfonic.domain.cache.dto.adserver.CampaignCvrInfo;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;
import com.adfonic.domain.cache.service.AdSpaceService;
import com.adfonic.domain.cache.service.AdSpaceServiceImpl;
import com.adfonic.domain.cache.service.CategoryService;
import com.adfonic.domain.cache.service.CategoryServiceImpl;
import com.adfonic.domain.cache.service.CreativeService;
import com.adfonic.domain.cache.service.CreativeServiceImpl;
import com.adfonic.domain.cache.service.MiscCacheService;
import com.adfonic.domain.cache.service.MiscCacheServiceImpl;
import com.adfonic.domain.cache.service.RtbCacheService;
import com.adfonic.domain.cache.service.RtbCacheServiceImpl;
import com.adfonic.domain.cache.service.WeightageServices;
import com.adfonic.domain.cache.service.WeightageServicesImpl;
import com.adfonic.util.Subnet;

public class AdserverDomainCacheImpl implements AdserverDomainCache {

    private static final long serialVersionUID = 5L;

    protected AdSpaceService adSpaceService;
    protected CategoryService categoryService;
    protected CreativeService creativeService;
    protected MiscCacheService miscCacheService;
    protected WeightageServices weightageServices;
    protected RtbCacheService rtbCacheService;

    private Date populationStartedAt;
    private Date elegibilityStartedAt;
    private Date preprocessingStartedAt;
    private Date preprocessingFinishedAt;

    private transient Date deserializationStartedAt;
    private transient Date postprocessingStartedAt;
    private transient Date postprocessingFinishedAt;

    public AdserverDomainCacheImpl(Date populationStartedAt) {
        this();
        this.populationStartedAt = populationStartedAt;
    }

    public AdserverDomainCacheImpl() {
        this.adSpaceService = new AdSpaceServiceImpl();
        this.categoryService = new CategoryServiceImpl();
        this.creativeService = new CreativeServiceImpl();
        this.miscCacheService = new MiscCacheServiceImpl();
        this.weightageServices = new WeightageServicesImpl();
        this.rtbCacheService = new RtbCacheServiceImpl();

    }

    public AdserverDomainCacheImpl(AdserverDomainCacheImpl copy) {
        this.adSpaceService = new AdSpaceServiceImpl((AdSpaceServiceImpl) copy.adSpaceService);
        this.categoryService = new CategoryServiceImpl((CategoryServiceImpl) copy.categoryService);
        this.creativeService = new CreativeServiceImpl((CreativeServiceImpl) copy.creativeService);
        this.miscCacheService = new MiscCacheServiceImpl((MiscCacheServiceImpl) copy.miscCacheService);
        this.weightageServices = new WeightageServicesImpl((WeightageServicesImpl) copy.weightageServices);
        this.rtbCacheService = new RtbCacheServiceImpl((RtbCacheServiceImpl) copy.rtbCacheService);
    }

    @Override
    public void addAddSpaceToCache(AdSpaceDto adSpace) {
        adSpaceService.addAddSpaceToCache(adSpace);
    }

    @Override
    public AdSpaceDto getAdSpaceByExternalID(String externalID) {
        return adSpaceService.getAdSpaceByExternalID(externalID);
    }

    @Override
    public AdSpaceDto getAdSpaceById(Long id) {
        return adSpaceService.getAdSpaceById(id);
    }

    @Override
    public AdSpaceDto[] getAllAdSpaces() {
        return adSpaceService.getAllAdSpaces();
    }

    @Override
    public void addDormantAdSpaceExternalId(String adSpaceExternalId) {
        adSpaceService.addDormantAdSpaceExternalId(adSpaceExternalId);
    }

    @Override
    public Set<String> getDormantAdSpaceExternalIds() {
        return adSpaceService.getDormantAdSpaceExternalIds();
    }

    @Override
    public boolean isDormantAdSpace(String externalId) {
        return adSpaceService.isDormantAdSpace(externalId);
    }

    @Override
    public void afterDeserialize() {
        this.postprocessingStartedAt = new Date();
        adSpaceService.afterDeserialize();
        categoryService.afterDeserialize();
        creativeService.afterDeserialize();
        miscCacheService.afterDeserialize();
        weightageServices.afterDeserialize();
        rtbCacheService.afterDeserialize();
        this.postprocessingFinishedAt = new Date();
    }

    @Override
    public void beforeSerialization() {
        preprocessingStartedAt = new Date();
        adSpaceService.beforeSerialization();
        categoryService.beforeSerialization();
        creativeService.beforeSerialization();
        miscCacheService.beforeSerialization();
        weightageServices.beforeSerialization();
        rtbCacheService.beforeSerialization();
        preprocessingFinishedAt = new Date();
    }

    @Override
    public void addExpendedCategoryIds(Long categoryId, Set<Long> listOfExpendedCategories) {
        categoryService.addExpendedCategoryIds(categoryId, listOfExpendedCategories);
    }

    @Override
    public Set<Long> getExpandedCategoryIds(Long categoryId) {
        return categoryService.getExpandedCategoryIds(categoryId);
    }

    @Override
    public boolean isExistsInExpandedCategoryIds(Long parentCategoryId, Long lookupCategoryId) {
        return categoryService.isExistsInExpandedCategoryIds(parentCategoryId, lookupCategoryId);
    }

    @Override
    public Set<Long> getExpandedCreativeCategoryIds(Long creativeId) {
        return categoryService.getExpandedCreativeCategoryIds(creativeId);
    }

    @Override
    public Set<Long> getExpandedPublicationCategoryIds(Long publicationId) {
        return categoryService.getExpandedPublicationCategoryIds(publicationId);
    }

    @Override
    public Set<String> getCachedPluginCategories(Long publicationId, String pluginName) {
        return categoryService.getCachedPluginCategories(publicationId, pluginName);
    }

    @Override
    public void cachePluginCategories(Long publicationId, String pluginName, Set<String> pluginCategories) {
        categoryService.cachePluginCategories(publicationId, pluginName, pluginCategories);
    }

    @Override
    public void addCreativeToCache(CreativeDto creative) {
        creativeService.addCreativeToCache(creative);
    }

    @Override
    public CreativeDto getCreativeByExternalID(String externalID) {
        return creativeService.getCreativeByExternalID(externalID);
    }

    @Override
    public CreativeDto getCreativeById(Long id) {
        return creativeService.getCreativeById(id);
    }

    @Override
    public CreativeDto[] getAllCreatives() {
        return creativeService.getAllCreatives();
    }

    @Override
    public CreativeDto[] getPluginCreatives() {
        return creativeService.getPluginCreatives();
    }

    @Override
    public void addAdSpaceEligibleCreative(Long adSpaceId, Set<AdspaceWeightedCreative> list, List<CountryDto> allCountries) {
        creativeService.addAdSpaceEligibleCreative(adSpaceId, list, allCountries);
    }

    @Override
    public AdspaceWeightedCreative[] getEligibleCreatives(Long adSpaceId) {
        return creativeService.getEligibleCreatives(adSpaceId);
    }

    @Override
    public PluginCreativeInfo getPluginCreativeInfo(CreativeDto creative) {
        return creativeService.getPluginCreativeInfo(creative);
    }

    @Override
    public void addRecentlyStoppedCreative(CreativeDto creative) {
        creativeService.addRecentlyStoppedCreative(creative);
    }

    @Override
    public CreativeDto getRecentlyStoppedCreativeById(Long id) {
        return creativeService.getRecentlyStoppedCreativeById(id);
    }

    @Override
    public void stopCampaign(Long campaignID) {
        creativeService.stopCampaign(campaignID);
    }

    @Override
    public void stopAdvertiser(Long advertiserId) {
        creativeService.stopAdvertiser(advertiserId);
    }

    @Override
    public void addSegmentSubnets(Long segmentId, Set<Subnet> subnets) {
        creativeService.addSegmentSubnets(segmentId, subnets);
    }

    @Override
    public Set<Subnet> getSubnetsBySegmentId(Long segmentId) {
        return creativeService.getSubnetsBySegmentId(segmentId);
    }

    @Override
    public void addPublicationMayViewPricing(Long publicationId) {
        adSpaceService.addPublicationMayViewPricing(publicationId);
    }

    @Override
    public boolean mayPublicationViewPricing(Long publicationId) {
        return adSpaceService.mayPublicationViewPricing(publicationId);
    }

    @Override
    @Deprecated
    public void logCounts(String description, Logger logger, Level level) {
        adSpaceService.logCounts(description, logger, level);
        categoryService.logCounts(description, logger, level);
        creativeService.logCounts(description, logger, level);
        miscCacheService.logCounts(description, logger, level);
        weightageServices.logCounts(description, logger, level);
        rtbCacheService.logCounts(description, logger, level);
    }

    @Override
    public void cachePayout(long publisherId, long campaignId, BigDecimal payout) {
        miscCacheService.cachePayout(publisherId, campaignId, payout);

    }

    @Override
    public BigDecimal getPayout(long publisherId, long campaignId) {
        return miscCacheService.getPayout(publisherId, campaignId);
    }

    @Override
    public void trimForNonRtbMode() {
        miscCacheService.trimForNonRtbMode();
    }

    @Override
    public PluginCreativeInfo getPluginCreativeInfo(Long creativeId) {
        return creativeService.getPluginCreativeInfo(creativeId);
    }

    @Override
    public void addRtbPublicationAdSpace(AdSpaceDto adspace) {
        rtbCacheService.addRtbPublicationAdSpace(adspace);
    }

    @Override
    public Map<String, AdSpaceDto> getPublisherRtbAdSpacesMap(Long publisherId) {
        return rtbCacheService.getPublisherRtbAdSpacesMap(publisherId);
    }

    @Override
    public Long getPublisherIdByExternalID(String externalID) {
        return adSpaceService.getPublisherIdByExternalID(externalID);
    }

    @Override
    public Map<Long, AdspaceWeightedCreative[]> getAllEligibleCreatives() {
        return creativeService.getAllEligibleCreatives();
    }

    @Override
    public AdSpaceDto getAdSpaceByPublicationRtbId(Long publisherId, String publicationRtbId) {
        return rtbCacheService.getAdSpaceByPublicationRtbId(publisherId, publicationRtbId);
    }

    @Override
    public boolean isRtbEnabled() {
        return rtbCacheService.isRtbEnabled();
    }

    @Override
    public void addPublisherByExternalId(String publisherExternalId, Long publisherId) {
        adSpaceService.addPublisherByExternalId(publisherExternalId, publisherId);
    }

    @Override
    public void addAssociatePublisher(Long id, String associateReference, Long parentId) {
        adSpaceService.addAssociatePublisher(id, associateReference, parentId);
    }

    @Override
    public Long getAssociatePublisherID(Long parentId, String associateReference) {
        return adSpaceService.getAssociatePublisherID(parentId, associateReference);
    }

    @Override
    public void addExpectedStats(long adspaceId, long creativeId, ExpectedStatsDto expectedStatsDto) {
        weightageServices.addExpectedStats(adspaceId, creativeId, expectedStatsDto);
    }

    @Override
    public void addCreativeCvr(long creativeId, double value) {
        weightageServices.addCreativeCvr(creativeId, value);
    }

    @Override
    public void addCampaignCvr(long campaignId, double value) {
        weightageServices.addCampaignCvr(campaignId, value);
    }

    @Override
    public ExpectedStatsDto getExpectedStats(long adspaceId, long creativeId) {
        return weightageServices.getExpectedStats(adspaceId, creativeId);
    }

    @Override
    public Double getCreativeCvr(long creativeId) {
        return weightageServices.getCreativeCvr(creativeId);
    }

    @Override
    public Double getCampaignCvr(long campaignId) {
        return weightageServices.getCampaignCvr(campaignId);
    }

    @Override
    public void addSystemVariable(SystemVariable systemVariable) {
        weightageServices.addSystemVariable(systemVariable);

    }

    @Override
    public SystemVariable getSystemVariableByName(String variableName) {
        return weightageServices.getSystemVariableByName(variableName);
    }

    @Override
    public void addAdspaceCtr(long adspaceId, double value) {
        weightageServices.addAdspaceCtr(adspaceId, value);
    }

    @Override
    public Double getAdspaceCtr(long adspaceId) {
        return weightageServices.getAdspaceCtr(adspaceId);
    }

    @Override
    public void addCampaignCountryWeight(long campaignId, long countryId, Double value) {
        weightageServices.addCampaignCountryWeight(campaignId, countryId, value);
    }

    @Override
    public Double getCampaignCountryWeight(long campaignId, long countryId) {
        return weightageServices.getCampaignCountryWeight(campaignId, countryId);
    }

    @Override
    public Double getSystemVariableDoubleValue(String variableName, Double defaultValue) {
        SystemVariable systemVariable = weightageServices.getSystemVariableByName(variableName);
        if (systemVariable == null) {
            return defaultValue;
        }
        return systemVariable.getDoubleValue();
    }

    @Override
    public void addCreativeWeightedCtrIndex(long creativeId, long platformId, double weightedCtrIndex) {
        weightageServices.addCreativeWeightedCtrIndex(creativeId, platformId, weightedCtrIndex);
    }

    @Override
    public double getCreativeWeightedCtrIndex(CreativeDto creative, PlatformDto platformId) {
        return weightageServices.getCreativeWeightedCtrIndex(creative, platformId);
    }

    @Override
    public void addPublicationWeightedCvrIndex(long publicationId, long platformId, BidType bidType, double weightedCtrIndex) {
        weightageServices.addPublicationWeightedCvrIndex(publicationId, platformId, bidType, weightedCtrIndex);
    }

    @Override
    public double getPublicationWeightedCvrIndex(PublicationDto publication, PlatformDto platformId, CreativeDto creative) {
        return weightageServices.getPublicationWeightedCvrIndex(publication, platformId, creative);
    }

    @Override
    public Set<Long> getEligibleCreativeIdsForCountry(Long countryId) {
        return creativeService.getEligibleCreativeIdsForCountry(countryId);
    }

    @Override
    public double getDefaultDoubleValue(String fieldName, double defaultValue) {
        return weightageServices.getDefaultDoubleValue(fieldName, defaultValue);
    }

    @Override
    public void computeEcpmInfo(AdSpaceDto adspace, CreativeDto creative, PlatformDto platform, long countryId, BigDecimal bidFloorPrice, EcpmInfo ecpmInfo) {
        weightageServices.computeEcpmInfo(adspace, creative, platform, countryId, bidFloorPrice, ecpmInfo);
    }

    @Override
    public void addCampaignRunningCtr(long campaignId, Double targetCtr, Double currentCtr) {
        weightageServices.addCampaignRunningCtr(campaignId, targetCtr, currentCtr);
    }

    @Override
    public void addCampaignRunningCvr(long campaignId, Double targetCvr, Double currentCvr) {
        weightageServices.addCampaignRunningCvr(campaignId, targetCvr, currentCvr);
    }

    @Override
    public CampaignCtrInfo getCampaignCtrInfo(long campaignId) {
        return weightageServices.getCampaignCtrInfo(campaignId);
    }

    @Override
    public CampaignCvrInfo getCampaignCvrInfo(long campaignId) {
        return weightageServices.getCampaignCvrInfo(campaignId);
    }

    @Override
    public Date getPopulationStartedAt() {
        return populationStartedAt;
    }

    @Override
    public Date getElegibilityStartedAt() {
        return elegibilityStartedAt;
    }

    public void setElegibilityStartedAt(Date elegibilityStartedAt) {
        this.elegibilityStartedAt = elegibilityStartedAt;
    }

    @Override
    public Date getPreprocessingStartedAt() {
        return preprocessingStartedAt;
    }

    @Override
    public Date getPreprocessingFinishedAt() {
        return preprocessingFinishedAt;
    }

    @Override
    public Date getDeserializationStartedAt() {
        return deserializationStartedAt;
    }

    public void setDeserializationStartedAt(Date deserializationStartedAt) {
        this.deserializationStartedAt = deserializationStartedAt;
    }

    @Override
    public Date getPostprocessingStartedAt() {
        return postprocessingStartedAt;
    }

    @Override
    public Date getPostprocessingFinishedAt() {
        return postprocessingFinishedAt;
    }

    //Stub methods, MUST not be used
    //Start
    @Override
    public BigDecimal convertToBidCurrencyFromUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount) {
        return null;
    }

    @Override
    public BigDecimal convertFromBidCurrencyToUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount) {
        return null;
    }

    @Override
    public void addCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId, BigDecimal amount) {
    }

    @Override
    public void clearAllConversionRate() {
    }

    @Override
    public BigDecimal getCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId) {
        return null;
    }

    //END

    @Override
    public double getCampaignTradingDeskMargin(CampaignDto campaign) {
        return weightageServices.getCampaignTradingDeskMargin(campaign);
    }

    @Override
    public void addCampaignMarginRecommendation(long campaignId, double margin) {
        weightageServices.addCampaignMarginRecommendation(campaignId, margin);
    }

    @Override
    public Double getCampaignMarginRecommendation(long campaignId) {
        return weightageServices.getCampaignMarginRecommendation(campaignId);
    }

}

package com.adfonic.adserver.delegator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.data.cache.util.Properties;
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
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.util.Subnet;

public class AdserverCacheDelegator implements AdserverDomainCache {

    private static final long serialVersionUID = 1L;

    private static final transient Logger LOG = Logger.getLogger(AdserverCacheDelegator.class.getName());

    private AdserverDomainCache oldDomainCache;
    private AdserverDataCache newDataCache;

    private boolean loadDataCacheEcpmComputation = false;
    private boolean loadDataCacheCategories = false;
    private boolean loadDataCacheCreatives = false;
    private boolean loadDataCacheAdSpaces = false;
    private boolean useDataCacheEcpmComputation = false;
    private boolean useDataCacheCategories = false;
    private boolean useDataCacheCreatives = false;
    private boolean useDataCacheAdSpaces = false;

    public AdserverCacheDelegator(AdserverDomainCache oldDomainCache, AdserverDataCache newDataCache) {

        this.oldDomainCache = oldDomainCache;
        this.newDataCache = newDataCache;

        if (this.newDataCache != null) {
            Properties properties = this.newDataCache.getProperties();
            this.loadDataCacheEcpmComputation = Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.ecpm.compute"));
            this.loadDataCacheCategories = Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.categories"));
            this.loadDataCacheCreatives = Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.creatives"));
            this.loadDataCacheAdSpaces = Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.adspaces"));
            this.useDataCacheEcpmComputation = Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.ecpm.compute"));
            this.useDataCacheCategories = Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.categories"));
            this.useDataCacheCreatives = Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.creatives"));
            this.useDataCacheAdSpaces = Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.adspaces"));
        }
    }

    // *******************
    //     ECPM
    // *******************

    @Override
    public SystemVariable getSystemVariableByName(String variableName) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getSystemVariableByName(variableName);
        else
            return this.oldDomainCache.getSystemVariableByName(variableName);
    }

    @Override
    public void addSystemVariable(SystemVariable systemVariable) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addSystemVariable(systemVariable);
        else
            this.oldDomainCache.addSystemVariable(systemVariable);
    }

    @Override
    public ExpectedStatsDto getExpectedStats(long adspaceId, long creativeId) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getExpectedStats(adspaceId, creativeId);
        else
            return this.oldDomainCache.getExpectedStats(adspaceId, creativeId);
    }

    @Override
    public Double getCampaignCvr(long creativeId) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getCampaignCvr(creativeId);
        else
            return this.oldDomainCache.getCampaignCvr(creativeId);
    }

    @Override
    public Double getCreativeCvr(long creativeId) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getCreativeCvr(creativeId);
        else
            return this.oldDomainCache.getCreativeCvr(creativeId);
    }

    @Override
    public Double getAdspaceCtr(long adspaceId) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getAdspaceCtr(adspaceId);
        else
            return this.oldDomainCache.getAdspaceCtr(adspaceId);
    }

    @Override
    public Double getCampaignCountryWeight(long campaignId, long countryId) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getCampaignCountryWeight(campaignId, countryId);
        else
            return this.oldDomainCache.getCampaignCountryWeight(campaignId, countryId);
    }

    @Override
    public void computeEcpmInfo(AdSpaceDto adspace, CreativeDto creative, PlatformDto platform, long countryId, BigDecimal bidFloorPrice, EcpmInfo ecpmInfo) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.computeEcpmInfo(adspace, creative, platform, countryId, bidFloorPrice, ecpmInfo);
        else
            this.oldDomainCache.computeEcpmInfo(adspace, creative, platform, countryId, bidFloorPrice, ecpmInfo);
    }

    @Override
    public void addExpectedStats(long adspaceId, long creativeId, ExpectedStatsDto expectedStatsDto) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addExpectedStats(adspaceId, creativeId, expectedStatsDto);
        else
            this.oldDomainCache.addExpectedStats(adspaceId, creativeId, expectedStatsDto);
    }

    @Override
    public void addCreativeCvr(long creativeId, double value) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addCreativeCvr(creativeId, value);
        else
            this.oldDomainCache.addCreativeCvr(creativeId, value);
    }

    @Override
    public void addCampaignCvr(long campaignId, double value) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addCampaignCvr(campaignId, value);
        else
            this.oldDomainCache.addCampaignCvr(campaignId, value);
    }

    @Override
    public void addAdspaceCtr(long adspaceId, double value) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addAdspaceCtr(adspaceId, value);
        else
            this.oldDomainCache.addAdspaceCtr(adspaceId, value);
    }

    @Override
    public double getDefaultDoubleValue(String fieldName, double defaultValue) {
        return this.newDataCache.getDefaultDoubleValue(fieldName, defaultValue);
    }

    @Override
    public void addCampaignCountryWeight(long campaignId, long countryId, Double value) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addCampaignCountryWeight(campaignId, countryId, value);
        else
            this.oldDomainCache.addCampaignCountryWeight(campaignId, countryId, value);
    }

    @Override
    public void addCampaignRunningCtr(long campaignId, Double targetCtr, Double currentCtr) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addCampaignRunningCtr(campaignId, targetCtr, currentCtr);
        else
            this.oldDomainCache.addCampaignRunningCtr(campaignId, targetCtr, currentCtr);
    }

    @Override
    public void addCampaignRunningCvr(long campaignId, Double targetCvr, Double currentCvr) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addCampaignRunningCvr(campaignId, targetCvr, currentCvr);
        else
            this.oldDomainCache.addCampaignRunningCvr(campaignId, targetCvr, currentCvr);
    }

    @Override
    public CampaignCtrInfo getCampaignCtrInfo(long campaignId) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getCampaignCtrInfo(campaignId);
        else
            return this.oldDomainCache.getCampaignCtrInfo(campaignId);
    }

    @Override
    public CampaignCvrInfo getCampaignCvrInfo(long campaignId) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getCampaignCvrInfo(campaignId);
        else
            return this.oldDomainCache.getCampaignCvrInfo(campaignId);
    }

    @Override
    public void addPublicationWeightedCvrIndex(long publicationId, long platformId, BidType bidType, double weightedCtrIndex) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addPublicationWeightedCvrIndex(publicationId, platformId, bidType, weightedCtrIndex);
        else
            this.oldDomainCache.addPublicationWeightedCvrIndex(publicationId, platformId, bidType, weightedCtrIndex);
    }

    // *******************
    //     CATEGORIES
    // *******************	

    @Override
    public void addExpendedCategoryIds(Long categoryId, Set<Long> listOfExpendedCategories) {
        if (this.loadDataCacheCategories && this.useDataCacheCategories)
            this.newDataCache.addExpendedCategoryIds(categoryId, listOfExpendedCategories);
        else
            this.oldDomainCache.addExpendedCategoryIds(categoryId, listOfExpendedCategories);
    }

    @Deprecated
    /**
     * use isExistsInExpandedCategoryIds instead
     * @param categoryId
     * @return
     */
    @Override
    public Set<Long> getExpandedCategoryIds(Long categoryId) {
        if (this.loadDataCacheCategories && this.useDataCacheCategories)
            return this.newDataCache.getExpandedCategoryIds(categoryId);
        else
            return this.oldDomainCache.getExpandedCategoryIds(categoryId);
    }

    @Override
    public boolean isExistsInExpandedCategoryIds(Long parentCategoryId, Long lookupCategoryId) {
        if (this.loadDataCacheCategories && this.useDataCacheCategories)
            return this.newDataCache.isExistsInExpandedCategoryIds(parentCategoryId, lookupCategoryId);
        else
            return this.oldDomainCache.isExistsInExpandedCategoryIds(parentCategoryId, lookupCategoryId);
    }

    @Deprecated
    /*
     * Now creative do not have any category so no need for this to be in cache
     */
    @Override
    public Set<Long> getExpandedCreativeCategoryIds(Long creativeId) {
        if (this.loadDataCacheCategories && this.useDataCacheCategories)
            return this.newDataCache.getExpandedCreativeCategoryIds(creativeId);
        else
            return this.oldDomainCache.getExpandedCreativeCategoryIds(creativeId);
    }

    @Deprecated
    /*
     * Now publications do not have list of category so no need for this to be in cache
     * and also we don't expand publication category, even if need arise do the following
     * cat = publication.getCategory();
     * Set<Category> cats = cache.getExpandedCategoryIds();
     */
    @Override
    public Set<Long> getExpandedPublicationCategoryIds(Long publicationId) {
        if (this.loadDataCacheCategories && this.useDataCacheCategories)
            return this.newDataCache.getExpandedPublicationCategoryIds(publicationId);
        else
            return this.oldDomainCache.getExpandedPublicationCategoryIds(publicationId);
    }

    @Override
    public Set<String> getCachedPluginCategories(Long publicationId, String pluginName) {
        if (this.loadDataCacheCategories && this.useDataCacheCategories)
            return this.newDataCache.getCachedPluginCategories(publicationId, pluginName);
        else
            return this.oldDomainCache.getCachedPluginCategories(publicationId, pluginName);
    }

    // *******************
    //     CREATIVES
    // *******************

    @Override
    public void addCreativeToCache(CreativeDto creative) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            this.newDataCache.addCreativeToCache(creative);
        else
            this.oldDomainCache.addCreativeToCache(creative);
    }

    @Override
    public CreativeDto getCreativeByExternalID(String externalID) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            return this.newDataCache.getCreativeByExternalID(externalID);
        else
            return this.oldDomainCache.getCreativeByExternalID(externalID);
    }

    @Override
    public CreativeDto getCreativeById(Long id) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            return this.newDataCache.getCreativeById(id);
        else
            return this.oldDomainCache.getCreativeById(id);
    }

    @Override
    public CreativeDto[] getAllCreatives() {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            return this.newDataCache.getAllCreatives();
        else
            return this.oldDomainCache.getAllCreatives();
    }

    @Override
    public CreativeDto[] getPluginCreatives() {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            return this.newDataCache.getPluginCreatives();
        else
            return this.oldDomainCache.getPluginCreatives();
    }

    @Override
    public void addAdSpaceEligibleCreative(Long adSpaceId, Set<AdspaceWeightedCreative> list, List<CountryDto> allCountries) {

        this.oldDomainCache.addAdSpaceEligibleCreative(adSpaceId, list, allCountries);
    }

    @Override
    public AdspaceWeightedCreative[] getEligibleCreatives(Long adSpaceId) {

        return this.oldDomainCache.getEligibleCreatives(adSpaceId);

    }

    @Override
    public Set<Long> getEligibleCreativeIdsForCountry(Long countryId) {

        return this.oldDomainCache.getEligibleCreativeIdsForCountry(countryId);
    }

    @Override
    public PluginCreativeInfo getPluginCreativeInfo(CreativeDto creative) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            return this.newDataCache.getPluginCreativeInfo(creative);
        else
            return this.oldDomainCache.getPluginCreativeInfo(creative);
    }

    @Override
    public PluginCreativeInfo getPluginCreativeInfo(Long creativeId) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            return this.newDataCache.getPluginCreativeInfo(creativeId);
        else
            return this.oldDomainCache.getPluginCreativeInfo(creativeId);
    }

    @Override
    public void addRecentlyStoppedCreative(CreativeDto creative) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            this.newDataCache.addRecentlyStoppedCreative(creative);
        else
            this.oldDomainCache.addRecentlyStoppedCreative(creative);
    }

    @Override
    public CreativeDto getRecentlyStoppedCreativeById(Long id) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            return this.newDataCache.getRecentlyStoppedCreativeById(id);
        else
            return this.oldDomainCache.getRecentlyStoppedCreativeById(id);
    }

    @Override
    public void stopCampaign(Long campaignID) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            this.newDataCache.stopCampaign(campaignID);
        else
            this.oldDomainCache.stopCampaign(campaignID);
    }

    @Override
    public void stopAdvertiser(Long advertiserId) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            this.newDataCache.stopAdvertiser(advertiserId);
        else
            this.oldDomainCache.stopAdvertiser(advertiserId);
    }

    @Override
    public void addSegmentSubnets(Long segmentId, Set<Subnet> subnets) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            this.newDataCache.addSegmentSubnets(segmentId, subnets);
        else
            this.oldDomainCache.addSegmentSubnets(segmentId, subnets);
    }

    @Override
    public Set<Subnet> getSubnetsBySegmentId(Long segmentId) {
        if (this.loadDataCacheCreatives && this.useDataCacheCreatives)
            return this.newDataCache.getSubnetsBySegmentId(segmentId);
        else
            return this.oldDomainCache.getSubnetsBySegmentId(segmentId);
    }

    @Override
    public Map<Long, AdspaceWeightedCreative[]> getAllEligibleCreatives() {

        return this.oldDomainCache.getAllEligibleCreatives();
    }

    // **********************
    //     REMAINING CALLS
    // **********************

    @Override
    public void addAddSpaceToCache(AdSpaceDto adSpace) {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            this.newDataCache.addAddSpaceToCache(adSpace);
        else
            this.oldDomainCache.addAddSpaceToCache(adSpace);
    }

    @Override
    public AdSpaceDto getAdSpaceByExternalID(String externalID) {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            return this.newDataCache.getAdSpaceByExternalID(externalID);
        else
            return this.oldDomainCache.getAdSpaceByExternalID(externalID);
    }

    @Override
    public AdSpaceDto getAdSpaceById(Long id) {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            return this.newDataCache.getAdSpaceById(id);
        else
            return this.oldDomainCache.getAdSpaceById(id);
    }

    @Override
    public AdSpaceDto[] getAllAdSpaces() {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            return this.newDataCache.getAllAdSpaces();
        else
            return this.oldDomainCache.getAllAdSpaces();
    }

    @Override
    public void addDormantAdSpaceExternalId(String adSpaceExternalId) {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            this.newDataCache.addDormantAdSpaceExternalId(adSpaceExternalId);
        else
            this.oldDomainCache.addDormantAdSpaceExternalId(adSpaceExternalId);
    }

    @Override
    @Deprecated
    public Set<String> getDormantAdSpaceExternalIds() {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            return this.newDataCache.getDormantAdSpaceExternalIds();
        else
            return this.oldDomainCache.getDormantAdSpaceExternalIds();
    }

    @Override
    public boolean isDormantAdSpace(String externalId) {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            return this.newDataCache.isDormantAdSpace(externalId);
        else
            return this.oldDomainCache.isDormantAdSpace(externalId);
    }

    @Override
    public void addPublicationMayViewPricing(Long publicationId) {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            this.newDataCache.addPublicationMayViewPricing(publicationId);
        else
            this.oldDomainCache.addPublicationMayViewPricing(publicationId);
    }

    @Override
    public boolean mayPublicationViewPricing(Long publicationId) {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            return this.newDataCache.mayPublicationViewPricing(publicationId);
        else
            return this.oldDomainCache.mayPublicationViewPricing(publicationId);
    }

    @Override
    public Long getPublisherIdByExternalID(String externalID) {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            return this.newDataCache.getPublisherIdByExternalID(externalID);
        else
            return this.oldDomainCache.getPublisherIdByExternalID(externalID);
    }

    @Override
    public void addPublisherByExternalId(String publisherExternalId, Long publisherId) {
        if (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces)
            this.newDataCache.addPublisherByExternalId(publisherExternalId, publisherId);
        else
            this.oldDomainCache.addPublisherByExternalId(publisherExternalId, publisherId);
    }

    @Override
    public void afterDeserialize() {

        this.oldDomainCache.afterDeserialize();
    }

    @Override
    public void beforeSerialization() {

        this.oldDomainCache.beforeSerialization();
    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {

        this.oldDomainCache.logCounts(description, logger, level);
    }

    @Override
    public void cachePluginCategories(Long publicationId, String pluginName, Set<String> pluginCategories) {

        this.oldDomainCache.cachePluginCategories(publicationId, pluginName, pluginCategories);
    }

    @Override
    public void cachePayout(long publisherId, long campaignId, BigDecimal payout) {

        this.oldDomainCache.cachePayout(publisherId, campaignId, payout);
    }

    @Override
    public BigDecimal getPayout(long publisherId, long campaignId) {

        return this.oldDomainCache.getPayout(publisherId, campaignId);
    }

    @Override
    public void trimForNonRtbMode() {

        this.oldDomainCache.trimForNonRtbMode();
    }

    @Override
    public double getPublicationWeightedCvrIndex(PublicationDto publication, PlatformDto platformId, CreativeDto creative) {

        return this.oldDomainCache.getPublicationWeightedCvrIndex(publication, platformId, creative);
    }

    @Override
    public double getCreativeWeightedCtrIndex(CreativeDto creative, PlatformDto platformId) {

        return this.oldDomainCache.getCreativeWeightedCtrIndex(creative, platformId);
    }

    @Override
    public void addCreativeWeightedCtrIndex(long creativeId, long platformId, double weightedCtrIndex) {

        this.oldDomainCache.addCreativeWeightedCtrIndex(creativeId, platformId, weightedCtrIndex);
    }

    @Override
    public void addRtbPublicationAdSpace(AdSpaceDto adspace) {

        this.oldDomainCache.addRtbPublicationAdSpace(adspace);
    }

    @Override
    public Map<String, AdSpaceDto> getPublisherRtbAdSpacesMap(Long publisherId) {

        return this.oldDomainCache.getPublisherRtbAdSpacesMap(publisherId);
    }

    @Override
    public AdSpaceDto getAdSpaceByPublicationRtbId(Long publisherId, String publicationRtbId) {

        return this.oldDomainCache.getAdSpaceByPublicationRtbId(publisherId, publicationRtbId);
    }

    @Override
    public boolean isRtbEnabled() {

        return this.oldDomainCache.isRtbEnabled();
    }

    @Override
    public Double getSystemVariableDoubleValue(String variableName, Double defaultValue) {

        return this.oldDomainCache.getSystemVariableDoubleValue(variableName, defaultValue);
    }

    // Currency Service
    @Override
    public BigDecimal convertToBidCurrencyFromUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount) {
        return newDataCache.convertToBidCurrencyFromUsd(adSpace, gmtTimeId, amount);
    }

    @Override
    public BigDecimal convertFromBidCurrencyToUsd(AdSpaceDto adSpace, String gmtTimeId, BigDecimal amount) {
        return newDataCache.convertFromBidCurrencyToUsd(adSpace, gmtTimeId, amount);
    }

    @Override
    public void addCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId, BigDecimal amount) {
        newDataCache.addCurrencyConversionRate(fromCurrency, toCurrency, gmtTimeId, amount);
    }

    @Override
    public void clearAllConversionRate() {
        newDataCache.clearAllConversionRate();
    }

    @Override
    public BigDecimal getCurrencyConversionRate(String fromCurrency, String toCurrency, String gmtTimeId) {
        return newDataCache.getCurrencyConversionRate(fromCurrency, toCurrency, gmtTimeId);
    }

    @Override
    public void addAssociatePublisher(Long id, String associateReference, Long parentId) {
        (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces ? newDataCache : oldDomainCache).addAssociatePublisher(id, associateReference, parentId);
    }

    @Override
    public Long getAssociatePublisherID(Long parentId, String associateReference) {
        return (this.loadDataCacheAdSpaces && this.useDataCacheAdSpaces ? newDataCache : oldDomainCache).getAssociatePublisherID(parentId, associateReference);
    }

    @Override
    public Date getPopulationStartedAt() {
        return oldDomainCache.getPopulationStartedAt();
    }

    @Override
    public Date getElegibilityStartedAt() {
        return oldDomainCache.getElegibilityStartedAt();
    }

    @Override
    public Date getPreprocessingStartedAt() {
        return oldDomainCache.getPreprocessingStartedAt();
    }

    @Override
    public Date getPreprocessingFinishedAt() {
        return oldDomainCache.getPreprocessingFinishedAt();
    }

    @Override
    public Date getDeserializationStartedAt() {
        return oldDomainCache.getDeserializationStartedAt();
    }

    @Override
    public Date getPostprocessingStartedAt() {
        return oldDomainCache.getPostprocessingStartedAt();
    }

    @Override
    public Date getPostprocessingFinishedAt() {
        return oldDomainCache.getPostprocessingFinishedAt();
    }

    @Override
    public double getCampaignTradingDeskMargin(CampaignDto campaign) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getCampaignTradingDeskMargin(campaign);
        else
            return this.oldDomainCache.getCampaignTradingDeskMargin(campaign);
    }

    @Override
    public Double getCampaignMarginRecommendation(long campaignId) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            return this.newDataCache.getCampaignMarginRecommendation(campaignId);
        else
            return this.oldDomainCache.getCampaignMarginRecommendation(campaignId);
    }

    @Override
    public void addCampaignMarginRecommendation(long campaignId, double margin) {
        if (this.loadDataCacheEcpmComputation && this.useDataCacheEcpmComputation)
            this.newDataCache.addCampaignMarginRecommendation(campaignId, margin);
        else
            this.oldDomainCache.addCampaignMarginRecommendation(campaignId, margin);
    }

}

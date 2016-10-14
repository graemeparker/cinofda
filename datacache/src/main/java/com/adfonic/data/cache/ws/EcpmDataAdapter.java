package com.adfonic.data.cache.ws;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.CampaignCtrInfo;
import com.adfonic.domain.cache.dto.adserver.CampaignCvrInfo;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.service.WeightageServices;

public class EcpmDataAdapter implements WeightageServices {

    private static final long serialVersionUID = 1L;

    private AdserverDataCache ecpmDataCache;

    public EcpmDataAdapter(AdserverDataCache ecpmDataCache) {

        this.ecpmDataCache = ecpmDataCache;
    }

    @Override
    public SystemVariable getSystemVariableByName(String variableName) {
        return ecpmDataCache.getSystemVariableByName(variableName);
    }

    @Override
    public double getDefaultDoubleValue(String fieldName, double defaultValue) {
        SystemVariable systemVariable = getSystemVariableByName(fieldName);
        if (systemVariable == null) {
            return defaultValue;
        }
        return systemVariable.getDoubleValue();
    }

    @Override
    public ExpectedStatsDto getExpectedStats(long adspaceId, long creativeId) {
        return ecpmDataCache.getExpectedStats(adspaceId, creativeId);
    }

    @Override
    public Double getCampaignCvr(long campaignId) {
        return ecpmDataCache.getCampaignCvr(campaignId);
    }

    @Override
    public Double getCreativeCvr(long creativeId) {
        return ecpmDataCache.getCreativeCvr(creativeId);
    }

    @Override
    public Double getAdspaceCtr(long adspaceId) {
        return ecpmDataCache.getAdspaceCtr(adspaceId);
    }

    @Override
    public Double getCampaignCountryWeight(long campaignId, long countryId) {
        return ecpmDataCache.getCampaignCountryWeight(campaignId, countryId);
    }

    @Override
    public double getPublicationWeightedCvrIndex(PublicationDto publication, PlatformDto platform, CreativeDto creative) {
        return ecpmDataCache.getPublicationWeightedCvrIndex(platform, creative.getCampaign(), publication.getId());
    }

    @Override
    public void computeEcpmInfo(AdSpaceDto adspace, CreativeDto creative, PlatformDto platform, long countryId, BigDecimal bidFloorPrice, EcpmInfo ecpmInfo) {
        ecpmDataCache.computeEcpmInfo(adspace, creative, platform, countryId, bidFloorPrice, ecpmInfo);
    }

    @Override
    public double getCreativeWeightedCtrIndex(CreativeDto creative, PlatformDto platform) {
        return ecpmDataCache.getCreativeWeightedCtrIndex(platform, creative.getId());
    }

    @Override
    public void addPublicationWeightedCvrIndex(long publicationId, long platformId, BidType bidType, double weightedCtrIndex) {
        ecpmDataCache.addPublicationWeightedCvrIndex(publicationId, platformId, bidType, weightedCtrIndex);
    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        ecpmDataCache.logCounts(description, logger, level);
    }

    @Override
    public void addSystemVariable(SystemVariable systemVariable) {
        ecpmDataCache.addSystemVariable(systemVariable);
    }

    @Override
    public void addCreativeWeightedCtrIndex(long creativeId, long platformId, double weightedCtrIndex) {
        ecpmDataCache.addCreativeWeightedCtrIndex(creativeId, platformId, weightedCtrIndex);
    }

    @Override
    public void addExpectedStats(long adspaceId, long creativeId, ExpectedStatsDto expectedStatsDto) {
        ecpmDataCache.addExpectedStats(adspaceId, creativeId, expectedStatsDto);
    }

    @Override
    public void addCreativeCvr(long creativeId, double value) {
        ecpmDataCache.addCreativeCvr(creativeId, value);
    }

    @Override
    public void addCampaignCvr(long campaignId, double value) {
        ecpmDataCache.addCampaignCvr(campaignId, value);
    }

    @Override
    public void addAdspaceCtr(long adspaceId, double value) {
        ecpmDataCache.addAdspaceCtr(adspaceId, value);
    }

    @Override
    public void afterDeserialize() {

    }

    @Override
    public void beforeSerialization() {

    }

    @Override
    public void addCampaignCountryWeight(long campaignId, long countryId, Double value) {
        ecpmDataCache.addCampaignCountryWeight(campaignId, countryId, value);
    }

    @Override
    public void addCampaignRunningCtr(long campaignId, Double targetCtr, Double currentCtr) {
        ecpmDataCache.addCampaignRunningCtr(campaignId, targetCtr, currentCtr);
    }

    @Override
    public void addCampaignRunningCvr(long campaignId, Double targetCvr, Double currentCvr) {
        ecpmDataCache.addCampaignRunningCvr(campaignId, targetCvr, currentCvr);
    }

    @Override
    public CampaignCtrInfo getCampaignCtrInfo(long campaignId) {
        return ecpmDataCache.getCampaignCtrInfo(campaignId);
    }

    @Override
    public CampaignCvrInfo getCampaignCvrInfo(long campaignId) {
        return ecpmDataCache.getCampaignCvrInfo(campaignId);
    }

    @Override
    public double getCampaignTradingDeskMargin(CampaignDto campaign) {
        return this.ecpmDataCache.getCampaignTradingDeskMargin(campaign);
    }

    @Override
    public void addCampaignMarginRecommendation(long campaignId, double margin) {
        this.ecpmDataCache.addCampaignMarginRecommendation(campaignId, margin);
    }

    @Override
    public Double getCampaignMarginRecommendation(long campaignId) {
        return this.ecpmDataCache.getCampaignMarginRecommendation(campaignId);
    }

}

package com.adfonic.data.cache;

import java.math.BigDecimal;

import com.adfonic.data.cache.util.Properties;
import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.CampaignCtrInfo;
import com.adfonic.domain.cache.dto.adserver.CampaignCvrInfo;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.service.AdSpaceService;
import com.adfonic.domain.cache.service.CategoryService;
import com.adfonic.domain.cache.service.CreativeService;
import com.adfonic.domain.cache.service.CurrencyService;

public interface AdserverDataCache extends CategoryService, CreativeService, CurrencyService, AdSpaceService {

    // Ecpm
    SystemVariable getSystemVariableByName(String variableName);

    void addSystemVariable(SystemVariable systemVariable);

    ExpectedStatsDto getExpectedStats(long adspaceId, long creativeId);

    Double getCampaignCvr(long creativeId);

    Double getCreativeCvr(long creativeId);

    Double getAdspaceCtr(long adspaceId);

    Double getCampaignCountryWeight(long campaignId, long countryId);

    double getPublicationWeightedCvrIndex(PlatformDto platform, CampaignDto campaign, long publicationId);

    double getCreativeWeightedCtrIndex(PlatformDto platform, long creativeId);

    void computeEcpmInfo(AdSpaceDto adspace, CreativeDto creative, PlatformDto platform, long countryId, BigDecimal bidFloorPrice, EcpmInfo ecpmInfo);

    void addExpectedStats(long adspaceId, long creativeId, ExpectedStatsDto expectedStatsDto);

    void addCreativeCvr(long creativeId, double value);

    void addCampaignCvr(long campaignId, double value);

    void addAdspaceCtr(long adspaceId, double value);

    double getDefaultDoubleValue(String fieldName, double defaultValue);

    void addCampaignCountryWeight(long campaignId, long countryId, Double value);

    void addCampaignRunningCtr(long campaignId, Double targetCtr, Double currentCtr);

    void addCampaignRunningCvr(long campaignId, Double targetCvr, Double currentCvr);

    CampaignCtrInfo getCampaignCtrInfo(long campaignId);

    CampaignCvrInfo getCampaignCvrInfo(long campaignId);

    void addCreativeWeightedCtrIndex(long creativeId, long platformId, double weightedCtrIndex);

    void addPublicationWeightedCvrIndex(long publicationId, long platformId, BidType bidType, double weightedCtrIndex);

    Properties getProperties();

    Double getCampaignMarginRecommendation(long campaignId);

    void addCampaignMarginRecommendation(long campaignId, double margin);

    double getCampaignTradingDeskMargin(CampaignDto campaign);
}

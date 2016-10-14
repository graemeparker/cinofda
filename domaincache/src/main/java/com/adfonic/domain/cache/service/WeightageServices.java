package com.adfonic.domain.cache.service;

import java.math.BigDecimal;

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

public interface WeightageServices extends BaseCache {

    // Optimization...very commonly found weight combination
    int DEFAULT_GENDER_MIX_WEIGHT = 5;
    int DEFAULT_AGE_RANGE_WEIGHT = 0;
    int DEFAULT_LANGUAGE_WEIGHT = 0;

    void addExpectedStats(long adspaceId, long creativeId, ExpectedStatsDto expectedStatsDto);

    void addCreativeCvr(long creativeId, double value);

    void addCampaignCvr(long campaignId, double value);

    void addAdspaceCtr(long adspaceId, double value);

    ExpectedStatsDto getExpectedStats(long adspaceId, long creativeId);

    Double getCreativeCvr(long creativeId);

    Double getCampaignCvr(long campaignId);

    Double getAdspaceCtr(long adspaceId);

    void addSystemVariable(SystemVariable systemVariable);

    SystemVariable getSystemVariableByName(String variableName);

    double getDefaultDoubleValue(String fieldName, double defaultValue);

    void addCampaignCountryWeight(long campaignId, long countryId, Double value);

    void addCampaignRunningCtr(long campaignId, Double targetCtr, Double currentCtr);

    void addCampaignRunningCvr(long campaignId, Double targetCvr, Double currentCvr);

    CampaignCtrInfo getCampaignCtrInfo(long campaignId);

    CampaignCvrInfo getCampaignCvrInfo(long campaignId);

    Double getCampaignCountryWeight(long campaignId, long countryId);

    void addCreativeWeightedCtrIndex(long creativeId, long platformId, double weightedCtrIndex);

    double getCreativeWeightedCtrIndex(CreativeDto creative, PlatformDto platformId);

    void addPublicationWeightedCvrIndex(long publicationId, long platformId, BidType bidType, double weightedCtrIndex);

    double getPublicationWeightedCvrIndex(PublicationDto publication, PlatformDto platformId, CreativeDto creative);

    /*
     * This function will compute various ecpm information and will put it in the EcpmInfo object being passed.
     * 
     * This is being done in this way as we dont want to create too many EcpmInfo object on adserver and basically
     * for each request we will create only one EcpmInfo object and reuse it.
     * 
     * EcpmInfo is rquired only when we are in targetting loop, and for eligible creative we copy it to mutableWeightedCreative
     * 
     * For rtb, it takes care of publisher buyer premium on the top of normal ecpm calculation
     * @param adspace
     * @param creative
     * @return
     */

    void computeEcpmInfo(AdSpaceDto adspace, CreativeDto creative, PlatformDto platform, long countryId, BigDecimal bidFloorPrice, EcpmInfo ecpmInfo);

    void addCampaignMarginRecommendation(long campaignId, double margin);

    Double getCampaignMarginRecommendation(long campaignId);

    double getCampaignTradingDeskMargin(CampaignDto campaign);
}

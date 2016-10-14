package com.adfonic.data.cache.ecpm.api;


import com.adfonic.data.cache.ecpm.datacache.PlatformBidTypePublicationKey;
import com.adfonic.data.cache.ecpm.key.AdSpaceCreativeKey;
import com.adfonic.data.cache.ecpm.key.CampaignCountryKey;
import com.adfonic.data.cache.ecpm.key.PlatformCreativeKey;
import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.CampaignCtrInfo;
import com.adfonic.domain.cache.dto.adserver.CampaignCvrInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;

public interface EcpmDataRepository {
	
	void clear();
	
    SystemVariable getSystemVariableByName(String variableName);

    Double getCreativeWeightedCtrIndex(PlatformCreativeKey platformCreativeKey);

    void addCreativeCvr(long creativeId, double value);

    void addCampaignCvr(long campaignId, double value);

    void addAdspaceCtr(long adspaceId, double value);

    void addSystemVariable(SystemVariable systemVariable);

    void addCreativeWeightedCtrIndex(PlatformCreativeKey platformCreativeKey, double weightedCtrIndex);

    void addCampaignCountryWeight(CampaignCountryKey campaignCountryKey, Double value);

    Double getCampaignCountryWeight(CampaignCountryKey campaignCountryKey);

    Double getAdspaceCtr(long adSpaceId);

    Double getCampaignCvr(long campaignId);

    Double getCreativeCvr(long creativeId);

    void addExpectedStats(AdSpaceCreativeKey adSpaceCreativeKey, ExpectedStatsDto expectedStatsDto);

    ExpectedStatsDto getExpectedStats(AdSpaceCreativeKey adSpaceCreativeKey);

    void addPublicationWeightedCvrIndex(PlatformBidTypePublicationKey platformBidTypePublicationKey, double weightedCvrIndex);

    Double getPubblicationWeightedCvrIndex(PlatformBidTypePublicationKey platformBidTypePublicationKey);
//
//    Double getCalculatedPriorityOdds(AdSpaceCreativeKey adSpaceCreativeKey);
//
//    void addCalculatedPriorityOdds(AdSpaceCreativeKey adSpaceCreativeKey, double priorityOdd);

    CampaignCtrInfo getCampaignCtrInfo(long campaignId);

    CampaignCvrInfo getCampaignCvrInfo(long campaignId);

    void addCampaignRunningCtr(long campaignId, Double targetCtr,Double currentCtr) ;

    void addCampaignRunningCvr(long campaignId, Double targetCvr,Double currentCvr);
    
    void addCampaignCountryWeight(long campaignId, long countryId, Double value);
    
    void addCreativeWeightedCtrIndex(long creativeId, long platformId, double weightedCtrIndex);
    
    void addPublicationWeightedCvrIndex(long publicationId, long platformId, BidType bidType, double weightedCtrIndex);
    
    Double getCampaignMarginRecommendation(long campaignId);
    
    void addCampaignMarginRecommendation(long campaignId, double margin);

}

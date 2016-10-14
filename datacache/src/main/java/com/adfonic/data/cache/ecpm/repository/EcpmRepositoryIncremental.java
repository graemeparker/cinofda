package com.adfonic.data.cache.ecpm.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.data.cache.ecpm.api.EcpmDataRepository;
import com.adfonic.data.cache.ecpm.datacache.PlatformBidTypePublicationKey;
import com.adfonic.data.cache.ecpm.key.AdSpaceCreativeKey;
import com.adfonic.data.cache.ecpm.key.CampaignCountryKey;
import com.adfonic.data.cache.ecpm.key.PlatformCreativeKey;
import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.SerializableCache;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.CampaignCtrInfo;
import com.adfonic.domain.cache.dto.adserver.CampaignCvrInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;

public class EcpmRepositoryIncremental implements EcpmDataRepository, SerializableCache {

    private static final long serialVersionUID = 1L;

    private Map<String, SystemVariable> systemVariableMapByName = new ConcurrentHashMap<String, SystemVariable>();

    private final Map<PlatformCreativeKey, Double> creativeWeightedCtrMap = new ConcurrentHashMap<PlatformCreativeKey, Double>(1000);

    private final Map<PlatformBidTypePublicationKey, Double> publicationWeightedCvrMap = new ConcurrentHashMap<PlatformBidTypePublicationKey, Double>(100);

    private final Map<CampaignCountryKey, Double> campaignCountryWeightMap = new ConcurrentHashMap<CampaignCountryKey, Double>(100);

    private final Map<Long, Double> creativeCvrMap = new ConcurrentHashMap<Long, Double>();

    private final Map<Long, Double> campaignCvrMap = new ConcurrentHashMap<Long, Double>();

    private final Map<Long, Double> adspaceCtrMap = new ConcurrentHashMap<Long, Double>();

    private final Map<Long, CampaignCtrInfo> campaignTargetCtrMap = new ConcurrentHashMap<Long, CampaignCtrInfo>();

    private final Map<Long, CampaignCvrInfo> campaignTargetCvrMap = new ConcurrentHashMap<Long, CampaignCvrInfo>();// HashMap<Long, CampaignCvrInfo>();

    private final Map<AdSpaceCreativeKey, ExpectedStatsDto> expectedStatsMap = new ConcurrentHashMap<AdSpaceCreativeKey, ExpectedStatsDto>();
    
    private final Map<Long, Double> campaignMarginRecommendationsMap = new ConcurrentHashMap<Long, Double>();



//    private transient Cache<AdSpaceCreativeKey, Double> calculatedPriorityOddsMap = newBuilder().concurrencyLevel(48).initialCapacity(16000).build();


    @Override
    public void addAdspaceCtr(long adspaceId, double value) {

        adspaceCtrMap.put(adspaceId, value);
    }

    @Override
    public void addSystemVariable(SystemVariable systemVariable) {
        //Make it case insenstive
        systemVariableMapByName.put(systemVariable.getName().toUpperCase(), systemVariable);
    }


    @Override
    public void addCreativeCvr(long creativeId, double value) {
        creativeCvrMap.put(creativeId, value);
    }

    @Override
    public void addCampaignCvr(long campaignId, double value) {
        campaignCvrMap.put(campaignId, value);

    }

    @Override
    public void addCreativeWeightedCtrIndex(PlatformCreativeKey platformCreativeKey, double weightedCtrIndex) {
        creativeWeightedCtrMap.put(platformCreativeKey, weightedCtrIndex);
    }


    @Override
    public void addPublicationWeightedCvrIndex(PlatformBidTypePublicationKey platformBidTypePublicationKey, double weightedCvrIndex) {
        publicationWeightedCvrMap.put(platformBidTypePublicationKey, weightedCvrIndex);
    }
    
    @Override
    public void addCampaignMarginRecommendation(long campaignId, double margin){
        campaignMarginRecommendationsMap.put(campaignId, margin);
    }


    @Override
    public SystemVariable getSystemVariableByName(String variableName) {
        return systemVariableMapByName.get(variableName.toUpperCase());
    }

    @Override
    public Double getPubblicationWeightedCvrIndex(PlatformBidTypePublicationKey platformBidTypePublicationKey) {
    	Double publicationWeightedCvr = publicationWeightedCvrMap.get(platformBidTypePublicationKey);
        if (publicationWeightedCvr != null) return publicationWeightedCvr;
        else return new Double(1);
    }

    @Override
    public CampaignCtrInfo getCampaignCtrInfo(long campaignId) {
        return campaignTargetCtrMap.get(campaignId);
    }

    @Override
    public CampaignCvrInfo getCampaignCvrInfo(long campaignId) {
        return campaignTargetCvrMap.get(campaignId);
    }

//    @Override
//    public Double getCalculatedPriorityOdds(AdSpaceCreativeKey adSpaceCreativeKey) {
//        return calculatedPriorityOddsMap.getIfPresent(adSpaceCreativeKey);
//    }
//
//    @Override
//    public void addCalculatedPriorityOdds(AdSpaceCreativeKey adSpaceCreativeKey, double priorityOdd) {
//        calculatedPriorityOddsMap.put(adSpaceCreativeKey, priorityOdd);
//    }

    @Override
    public Double getCreativeWeightedCtrIndex(PlatformCreativeKey platformCreativeKey) {
    	Double creativeWeightedCtr = creativeWeightedCtrMap.get(platformCreativeKey);
        if (creativeWeightedCtr != null) return creativeWeightedCtr;
        else return new Double(1);
    }

    @Override
    public void addCampaignCountryWeight(CampaignCountryKey campaignCountryKey, Double value) {
        campaignCountryWeightMap.put(campaignCountryKey, value);

    }


    @Override
    public Double getCampaignCountryWeight(CampaignCountryKey campaignCountryKey) {
        return campaignCountryWeightMap.get(campaignCountryKey);
    }

    @Override
    public Double getAdspaceCtr(long adSpaceId) {
        return adspaceCtrMap.get(adSpaceId);
    }

    @Override
    public Double getCampaignCvr(long campaignId) {
        return campaignCvrMap.get(campaignId);
    }

    @Override
    public Double getCreativeCvr(long creativeId) {
        return creativeCvrMap.get(creativeId);
    }

    @Override
    public void addCampaignRunningCtr(long campaignId, Double targetCtr,Double currentCtr) {
        campaignTargetCtrMap.put(campaignId, new CampaignCtrInfo(targetCtr, currentCtr));
    }

    @Override
    public void addCampaignRunningCvr(long campaignId, Double targetCvr,Double currentCvr) {
        campaignTargetCvrMap.put(campaignId, new CampaignCvrInfo(targetCvr, currentCvr));
    }

    /**
     * This function will be called by domainserializer after checking property if ECPm required by shard or not
     */
    @Override
    public void addExpectedStats(AdSpaceCreativeKey adSpaceCreativeKey, ExpectedStatsDto expectedStatsDto) {
        expectedStatsMap.put(adSpaceCreativeKey, expectedStatsDto);

    }

    @Override
    public ExpectedStatsDto getExpectedStats(AdSpaceCreativeKey adSpaceCreativeKey) {
        return expectedStatsMap.get(adSpaceCreativeKey);
    }

    @Override
    public Double getCampaignMarginRecommendation(long campaignId) {
        return campaignMarginRecommendationsMap.get(campaignId);
    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {

//            logger.log(level, description + " Total Calculated ECPM Entries(AdSpace X Creative)= " + this.calculatedPriorityOddsMap.size() + "(Transient)");

            logger.log(level, description + "Total expectedStatsMap = " + this.expectedStatsMap.size());

            logger.log(level, description + "Total creativeCvrMap = " + this.creativeCvrMap.size());

            logger.log(level, description + "Total campaignCvrMap = " + this.campaignCvrMap.size());

            logger.log(level, description + "Total adspaceCtrMap = " + this.adspaceCtrMap.size());

            logger.log(level, description + "Total systemVariableMapByName = " + this.systemVariableMapByName.size());

            logger.log(level, description + "Total campaignCountryWeightingMap = " + this.campaignCountryWeightMap.size());

            logger.log(level, description + "Total publicationWeightedCvrMap = " + this.publicationWeightedCvrMap.size());

            logger.log(level, description + "Total creativeWeightedCtrMap = " + this.creativeWeightedCtrMap.size());
            
            logger.log(level, description + "Total campaignMarginRecommendationsMap = " + this.campaignMarginRecommendationsMap.size());

        }
    }

	@Override
	public void clear() {
	    systemVariableMapByName.clear();
	    creativeWeightedCtrMap.clear();
	    publicationWeightedCvrMap.clear();
	    campaignCountryWeightMap.clear();
	    creativeCvrMap.clear();
	    campaignCvrMap.clear();
	    adspaceCtrMap.clear();
	    campaignTargetCtrMap.clear();
	    campaignTargetCvrMap.clear();
	    expectedStatsMap.clear();
	    campaignMarginRecommendationsMap.clear();
	}

	@Override
	public void addCampaignCountryWeight(long campaignId, long countryId,Double value) {
		
		CampaignCountryKey campaignCountryKey = new CampaignCountryKey(campaignId,countryId);
		
		this.campaignCountryWeightMap.put(campaignCountryKey, value);
	}

	@Override
	public void addCreativeWeightedCtrIndex(long creativeId, long platformId, double weightedCtrIndex) {
		
		PlatformCreativeKey platformCreativeKey = new PlatformCreativeKey(platformId, creativeId);
		
		this.creativeWeightedCtrMap.put(platformCreativeKey, weightedCtrIndex);
	}

    @Override
    public void addPublicationWeightedCvrIndex(long publicationId, long platformId, BidType bidType, double weightedCvrIndex){
        PlatformBidTypePublicationKey platformBidTypePublicationKey = new PlatformBidTypePublicationKey(platformId, bidType, publicationId);
    	
    	publicationWeightedCvrMap.put(platformBidTypePublicationKey, weightedCvrIndex);
    }
}

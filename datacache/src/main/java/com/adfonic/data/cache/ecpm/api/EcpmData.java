package com.adfonic.data.cache.ecpm.api;

import com.adfonic.domain.cache.dto.adserver.EcpmInfo;

public class EcpmData {

    private final EcpmInfo ecpmInfo = new EcpmInfo();
    private Double campaignCvr;
    private Double creativeCvr;
    private Double expectedStatsRgr;
    private Double expectedStatsCvr;
    private Double expectedStatsCtr;
    private Double adspaceCtr;
    private Double rtbBidMultiplierCPC;
    private Double rtbBidMultiplierCPM;
    private Double rtbBidMultiplierCPI;
    private Double rtbBidMultiplierCPA;
    private Double defaultCtr;
    private Double defaultCvr;
    private Double defaultRtbCvr;
    private Double networkMaxExpectedRgr;
    private Double buyerPremium;
    private Double countryWeighting;
    private Double creativeWeightedCtrIndex;
    private Double publicationWeightedCvrIndex;
    private Double defaultCtrTarget;
    private Double defaultCvrTarget;
    private Double defaultCpcCtrTarget;
    private Double cpmCtrUnderperformanceThreshold;
    private Double cpcCtrUnderperformanceThreshold;
    private Double cpcCvrUnderperformanceThreshold;
    private Double campaignTargetCtr;
    private Double campaignCurrentCtr;
    private Double campaignTargetCvr;
    private Double campaignCurrentCvr;
    private Double softFloorMultiplier;
    private Double campaignMarginRecommendation;

    private long elapsedTime = 0;

    public Double getCampaignCvr() {
        return campaignCvr;
    }

    public void setCampaignCvr(Double campaignCvr) {
        this.campaignCvr = campaignCvr;
    }

    public Double getCreativeCvr() {
        return creativeCvr;
    }

    public void setCreativeCvr(Double creativeCvr) {
        this.creativeCvr = creativeCvr;
    }

    public Double getExpectedStatsRgr() {
        return expectedStatsRgr;
    }

    public void setExpectedStatsRgr(Double expectedStatsRgr) {
        this.expectedStatsRgr = expectedStatsRgr;
    }

    public Double getExpectedStatsCvr() {
        return expectedStatsCvr;
    }

    public void setExpectedStatsCvr(Double expectedStatsCvr) {
        this.expectedStatsCvr = expectedStatsCvr;
    }

    public Double getExpectedStatsCtr() {
        return expectedStatsCtr;
    }

    public void setExpectedStatsCtr(Double expectedStatsCtr) {
        this.expectedStatsCtr = expectedStatsCtr;
    }

    public Double getAdspaceCtr() {
        return adspaceCtr;
    }

    public void setAdspaceCtr(Double adspaceCtr) {
        this.adspaceCtr = adspaceCtr;
    }

    public Double getRtbBidMultiplierCPC() {
        return rtbBidMultiplierCPC;
    }

    public void setRtbBidMultiplierCPC(Double rtbBidMultiplierCPC) {
        this.rtbBidMultiplierCPC = rtbBidMultiplierCPC;
    }

    public Double getRtbBidMultiplierCPM() {
        return rtbBidMultiplierCPM;
    }

    public void setRtbBidMultiplierCPM(Double rtbBidMultiplierCPM) {
        this.rtbBidMultiplierCPM = rtbBidMultiplierCPM;
    }

    public Double getRtbBidMultiplierCPI() {
        return rtbBidMultiplierCPI;
    }

    public void setRtbBidMultiplierCPI(Double rtbBidMultiplierCPI) {
        this.rtbBidMultiplierCPI = rtbBidMultiplierCPI;
    }

    public Double getRtbBidMultiplierCPA() {
        return rtbBidMultiplierCPA;
    }

    public void setRtbBidMultiplierCPA(Double rtbBidMultiplierCPA) {
        this.rtbBidMultiplierCPA = rtbBidMultiplierCPA;
    }

    public Double getDefaultCtr() {
        return defaultCtr;
    }

    public void setDefaultCtr(Double defaultCtr) {
        this.defaultCtr = defaultCtr;
    }

    public Double getDefaultCvr() {
        return defaultCvr;
    }

    public void setDefaultCvr(Double defaultCvr) {
        this.defaultCvr = defaultCvr;
    }

    public Double getDefaultRtbCvr() {
        return defaultRtbCvr;
    }

    public void setDefaultRtbCvr(Double defaultRtbCvr) {
        this.defaultRtbCvr = defaultRtbCvr;
    }

    public Double getNetworkMaxExpectedRgr() {
        return networkMaxExpectedRgr;
    }

    public void setNetworkMaxExpectedRgr(Double networkMaxExpectedRgr) {
        this.networkMaxExpectedRgr = networkMaxExpectedRgr;
    }

    public Double getBuyerPremium() {
        return buyerPremium;
    }

    public void setBuyerPremium(Double buyerPremium) {
        this.buyerPremium = buyerPremium;
    }

    public Double getCountryWeighting() {
        return countryWeighting;
    }

    public void setCountryWeighting(Double countryWeighting) {
        this.countryWeighting = countryWeighting;
    }

    public Double getCreativeWeightedCtrIndex() {
        return creativeWeightedCtrIndex;
    }

    public void setCreativeWeightedCtrIndex(Double creativeWeightedCtrIndex) {
        this.creativeWeightedCtrIndex = creativeWeightedCtrIndex;
    }

    public Double getPublicationWeightedCvrIndex() {
        return publicationWeightedCvrIndex;
    }

    public void setPublicationWeightedCvrIndex(Double publicationWeightedCvrIndex) {
        this.publicationWeightedCvrIndex = publicationWeightedCvrIndex;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Double getDefaultCtrTarget() {
        return defaultCtrTarget;
    }

    public void setDefaultCtrTarget(Double defaultCtrTarget) {
        this.defaultCtrTarget = defaultCtrTarget;
    }

    public Double getDefaultCpcCtrTarget() {
        return defaultCpcCtrTarget;
    }

    public void setDefaultCpcCtrTarget(Double defaultCpcCtrTarget) {
        this.defaultCpcCtrTarget = defaultCpcCtrTarget;
    }

    public Double getCpmCtrUnderperformanceThreshold() {
        return cpmCtrUnderperformanceThreshold;
    }

    public void setCpmCtrUnderperformanceThreshold(Double cpmCtrUnderperformanceThreshold) {
        this.cpmCtrUnderperformanceThreshold = cpmCtrUnderperformanceThreshold;
    }

    public Double getCpcCtrUnderperformanceThreshold() {
        return cpcCtrUnderperformanceThreshold;
    }

    public void setCpcCtrUnderperformanceThreshold(Double cpcCtrUnderperformanceThreshold) {
        this.cpcCtrUnderperformanceThreshold = cpcCtrUnderperformanceThreshold;
    }

    public EcpmInfo getEcpmInfo() {
        return ecpmInfo;
    }

    public Double getCpcCvrUnderperformanceThreshold() {
        return cpcCvrUnderperformanceThreshold;
    }

    public void setCpcCvrUnderperformanceThreshold(Double cpcCvrUnderperformanceThreshold) {
        this.cpcCvrUnderperformanceThreshold = cpcCvrUnderperformanceThreshold;
    }

    public Double getCampaignTargetCtr() {
        return campaignTargetCtr;
    }

    public void setCampaignTargetCtr(Double campaignTargetCtr) {
        this.campaignTargetCtr = campaignTargetCtr;
    }

    public Double getCampaignCurrentCtr() {
        return campaignCurrentCtr;
    }

    public void setCampaignCurrentCtr(Double campaignCurrentCtr) {
        this.campaignCurrentCtr = campaignCurrentCtr;
    }

    public Double getCampaignTargetCvr() {
        return campaignTargetCvr;
    }

    public void setCampaignTargetCvr(Double campaignTargetCvr) {
        this.campaignTargetCvr = campaignTargetCvr;
    }

    public Double getCampaignCurrentCvr() {
        return campaignCurrentCvr;
    }

    public void setCampaignCurrentCvr(Double campaignCurrentCvr) {
        this.campaignCurrentCvr = campaignCurrentCvr;
    }

    public Double getDefaultCvrTarget() {
        return defaultCvrTarget;
    }

    public void setDefaultCvrTarget(Double defaultCvrTarget) {
        this.defaultCvrTarget = defaultCvrTarget;
    }

    public Double getSoftFloorMultiplier() {
        return softFloorMultiplier;
    }

    public void setSoftFloorMultiplier(Double softFloorMultiplier) {
        this.softFloorMultiplier = softFloorMultiplier;
    }

    public Double getCampaignMarginRecommendation() {
        return campaignMarginRecommendation;
    }

    public void setCampaignMarginRecommendation(Double campaignMarginRecommendation) {
        this.campaignMarginRecommendation = campaignMarginRecommendation;
    }

}

package com.adfonic.domain.cache.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.CampaignCtrInfo;
import com.adfonic.domain.cache.dto.adserver.CampaignCvrInfo;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.TransientDataExt;

public class WeightageServicesImpl implements WeightageServices {

    private static final transient Logger LOG = Logger.getLogger(WeightageServicesImpl.class.getName());
    private static final long serialVersionUID = 5L; // don't forget to increment this when you change this class!

    private final Map<String, SystemVariable> systemVariableMapByName = new HashMap<String, SystemVariable>();

    private final Map<Long, Map<Long, Double>> campaignCountryWeightMap = new HashMap<Long, Map<Long, Double>>(100);

    private final ConcurrentMap<Long, ConcurrentMap<Long, ExpectedStatsDto>> expectedStatsMap = new ConcurrentHashMap<Long, ConcurrentMap<Long, ExpectedStatsDto>>(1000);

    private final Map<Long, Double> creativeCvrMap = new HashMap<Long, Double>();

    private final Map<Long, Double> campaignCvrMap = new HashMap<Long, Double>();

    private final Map<Long, Double> adspaceCtrMap = new HashMap<Long, Double>();

    private final Map<Long, CampaignCtrInfo> campaignTargetCtrMap = new HashMap<Long, CampaignCtrInfo>();

    private final Map<Long, CampaignCvrInfo> campaignTargetCvrMap = new HashMap<Long, CampaignCvrInfo>();

    private final Map<Long, Double> campaignMarginRecommendationsMap = new ConcurrentHashMap<Long, Double>();

    public WeightageServicesImpl() {
    }

    public WeightageServicesImpl(WeightageServicesImpl copy) {
        this.expectedStatsMap.putAll(copy.expectedStatsMap);
        this.campaignCountryWeightMap.putAll(copy.campaignCountryWeightMap);
        this.creativeCvrMap.putAll(copy.creativeCvrMap);
        this.campaignCvrMap.putAll(copy.campaignCvrMap);
        this.adspaceCtrMap.putAll(copy.adspaceCtrMap);
        this.systemVariableMapByName.putAll(copy.systemVariableMapByName);
        this.campaignTargetCtrMap.putAll(copy.campaignTargetCtrMap);
        this.campaignTargetCvrMap.putAll(copy.campaignTargetCvrMap);
        this.campaignMarginRecommendationsMap.putAll(copy.campaignMarginRecommendationsMap);
    }

    private final Map<Long, Map<Long, Double>> creativeWeightedCtrMap = new HashMap<Long, Map<Long, Double>>(1000);

    private final Map<Long, Map<String, Map<Long, Double>>> publicationWeightedCvrMap = new HashMap<Long, Map<String, Map<Long, Double>>>(100);

    @Override
    public void afterDeserialize() {
    }

    @Override
    public void beforeSerialization() {
    }

    private ExpectedStatsDto getExpectedStats(AdSpaceDto adSpace, CreativeDto creative, PlatformDto platform) {
        Map<Long, ExpectedStatsDto> adspaceExpectedStats = expectedStatsMap.get(adSpace.getId());
        if (adspaceExpectedStats != null) {
            ExpectedStatsDto expectedStatsDto = adspaceExpectedStats.get(creative.getId());
            if (expectedStatsDto != null) {
                return capMaxRgr(expectedStatsDto);
            }
        }
        double creativeCvr = 0.0055;
        double adspaceCtr = 0.0055;

        if (adspaceCtrMap.get(adSpace.getId()) != null) {
            adspaceCtr = adspaceCtrMap.get(adSpace.getId());
        } else {
            adspaceCtr = getDefaultDoubleValue("network_default_ctr", 0.0055);
        }
        //adjust adspace CTR
        double creativeCtrIndex = getCreativeWeightedCtrIndex(creative, platform);
        adspaceCtr = adspaceCtr * creativeCtrIndex;

        if (creativeCvrMap.get(creative.getId()) != null) {
            creativeCvr = creativeCvrMap.get(creative.getId());
        } else {
            if (campaignCvrMap.get(creative.getCampaign().getId()) != null) {
                creativeCvr = campaignCvrMap.get(creative.getCampaign().getId());
            } else {
                if (adSpace.getPublication().getPublisher().getRtbConfig() == null) {
                    creativeCvr = getDefaultDoubleValue("network_default_cvr", 0.02);
                } else {
                    creativeCvr = getDefaultDoubleValue("network_default_cvr_rtb", 0.003);
                }
            }
        }
        //SC-108 adjust creative CVR with publicationCvrIndex
        double publicationCvrIndex = getPublicationWeightedCvrIndex(adSpace.getPublication(), platform, creative);
        creativeCvr = creativeCvr * publicationCvrIndex;
        //calculate RGR

        double defaultRgr = adspaceCtr;
        if (!creative.getCampaign().getCurrentBid().getBidType().equals(BidType.CPC)) {
            defaultRgr = creativeCvr * adspaceCtr;
        }
        ExpectedStatsDto expectedStatsDto = new ExpectedStatsDto(adspaceCtr, creativeCvr, defaultRgr);
        addExpectedStats(adSpace.getId(), creative.getId(), expectedStatsDto);

        return capMaxRgr(expectedStatsDto);
    }

    private ExpectedStatsDto capMaxRgr(ExpectedStatsDto expectedStatsDto) {
        //Cap it
        double maxRgr = getDefaultDoubleValue("network_max_expected_rgr", 0.5);
        expectedStatsDto.capMaxRgr(maxRgr);
        return expectedStatsDto;
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

    /**
     * This method will calculate match factor, based on country,age,gender etc
     * @param campaignId
     * @param countryId
     * @return
     */
    private double calculateMatchFactor(long campaignId, long countryId) {
        double matchFactor = 1.0;
        //Currently only country weighting is available so match factor will be calculated based on country only
        Double countryWeight = this.getCampaignCountryWeight(campaignId, countryId);
        if (countryWeight != null) {
            matchFactor = countryWeight * matchFactor;
        }
        //Age

        //gender
        //LOG.log(Level.FINE, "matchFactor="+matchFactor);
        return matchFactor;
    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            logger.log(level, "Total expectedStatsMap = " + this.expectedStatsMap.size());

            logger.log(level, "Total creativeCvrMap = " + this.creativeCvrMap.size());

            logger.log(level, "Total campaignCvrMap = " + this.campaignCvrMap.size());

            logger.log(level, "Total adspaceCtrMap = " + this.adspaceCtrMap.size());

            logger.log(level, "Total systemVariableMapByName = " + this.systemVariableMapByName.size());

            logger.log(level, "Total campaignCountryWeightingMap = " + this.campaignCountryWeightMap.size());

            logger.log(level, "Total publicationWeightedCvrMap = " + this.publicationWeightedCvrMap.size());

            logger.log(level, "Total creativeWeightedCtrMap = " + this.creativeWeightedCtrMap.size());

            logger.log(level, "Total campaignMarginRecommendationsMap = " + this.campaignMarginRecommendationsMap.size());

        }
    }

    /**
     * This function will be called by domainserializer after checking property if ECPm required by shard or not
     */
    @Override
    public void addExpectedStats(long adspaceId, long creativeId, ExpectedStatsDto expectedStatsDto) {
        Long adspaceKey = TransientDataExt.getSingltonId(adspaceId);
        Long creativeKey = TransientDataExt.getSingltonId(creativeId);
        ConcurrentMap<Long, ExpectedStatsDto> creativeMap = expectedStatsMap.get(adspaceKey);
        if (creativeMap == null) {
            creativeMap = new ConcurrentHashMap<Long, ExpectedStatsDto>();
            ConcurrentMap<Long, ExpectedStatsDto> existingCreativeMap = expectedStatsMap.putIfAbsent(adspaceKey, creativeMap);
            if (existingCreativeMap != null) {
                creativeMap = existingCreativeMap;
            }
        }
        creativeMap.putIfAbsent(creativeKey, expectedStatsDto);
    }

    @Override
    public void addCreativeCvr(long creativeId, double value) {
        Long creativeKey = TransientDataExt.getSingltonId(creativeId);
        synchronized (creativeCvrMap) {
            creativeCvrMap.put(creativeKey, value);
        }

    }

    @Override
    public void addCampaignCvr(long campaignId, double value) {
        Long campaignKey = TransientDataExt.getSingltonId(campaignId);
        synchronized (campaignCvrMap) {
            campaignCvrMap.put(campaignKey, value);
        }
    }

    @Override
    public ExpectedStatsDto getExpectedStats(long adspaceId, long creativeId) {
        Map<Long, ExpectedStatsDto> creativeMap = expectedStatsMap.get(adspaceId);
        if (creativeMap != null) {
            return creativeMap.get(creativeId);
        }
        return null;
    }

    @Override
    public Double getCreativeCvr(long creativeId) {
        return creativeCvrMap.get(creativeId);
    }

    @Override
    public Double getCampaignCvr(long campaignId) {
        return campaignCvrMap.get(campaignId);
    }

    @Override
    public void addAdspaceCtr(long adspaceId, double value) {
        Long adspaceKey = TransientDataExt.getSingltonId(adspaceId);
        synchronized (adspaceCtrMap) {
            adspaceCtrMap.put(adspaceKey, value);
        }
    }

    @Override
    public void addSystemVariable(SystemVariable systemVariable) {
        //Make it case insenstive
        systemVariableMapByName.put(systemVariable.getName().toUpperCase(), systemVariable);
    }

    @Override
    public SystemVariable getSystemVariableByName(String variableName) {
        return systemVariableMapByName.get(variableName.toUpperCase());
    }

    @Override
    public Double getAdspaceCtr(long adspaceId) {
        return adspaceCtrMap.get(adspaceId);
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
            Double campaignMarginRecommendation = this.campaignMarginRecommendationsMap.get(campaign.getId());
            if ((campaignMarginRecommendation != null) && (campaignMarginRecommendation.doubleValue() > margin)) {
                margin = campaignMarginRecommendation.doubleValue();
            }
        }

        return margin;
    }

    @Override
    public void addCampaignMarginRecommendation(long campaignId, double margin) {
        this.campaignMarginRecommendationsMap.put(campaignId, margin);
    }

    @Override
    public Double getCampaignMarginRecommendation(long campaignId) {
        return this.campaignMarginRecommendationsMap.get(campaignId);
    }

    /**
     * This method is not threadsafe, as it didn't need to be at the time of writing,
     * so use safely.
     */
    @Override
    public void addCampaignCountryWeight(long campaignId, long countryId, Double value) {
        Map<Long, Double> countryWeightMap = campaignCountryWeightMap.get(campaignId);
        if (countryWeightMap == null) {
            countryWeightMap = new HashMap<Long, Double>();
            campaignCountryWeightMap.put(campaignId, countryWeightMap);
        }
        countryWeightMap.put(countryId, value);
    }

    @Override
    public Double getCampaignCountryWeight(long campaignId, long countryId) {
        Double returnValue = null;
        //LOG.log(Level.FINE, "campaignCountryWeightMap="+campaignCountryWeightMap);
        Map<Long, Double> countryWeightMap = campaignCountryWeightMap.get(campaignId);
        if (countryWeightMap != null) {
            //LOG.log(Level.FINE, "countryWeightMap="+countryWeightMap);
            returnValue = countryWeightMap.get(countryId);
        }
        if (returnValue == null) {
            //No Country weighting found for this campaign, that means country weighting is 1
            returnValue = 1.0;
        }
        return returnValue;
    }

    @Override
    public void addCreativeWeightedCtrIndex(long creativeId, long platformId, double weightedCtrIndex) {
        Map<Long, Double> creativeMap = creativeWeightedCtrMap.get(platformId);
        if (creativeMap == null) {
            creativeMap = new HashMap<Long, Double>();
            creativeWeightedCtrMap.put(platformId, creativeMap);
        }
        creativeMap.put(creativeId, weightedCtrIndex);

    }

    @Override
    public double getCreativeWeightedCtrIndex(CreativeDto creative, PlatformDto platform) {
        Double creativeWeightedCtrIndex = null;
        if (platform != null) {
            Map<Long, Double> creativeMap = creativeWeightedCtrMap.get(platform.getId());
            if (creativeMap != null) {
                creativeWeightedCtrIndex = creativeMap.get(creative.getId());
            }
        }
        if (creativeWeightedCtrIndex == null) {
            creativeWeightedCtrIndex = 1.0;
        }
        return creativeWeightedCtrIndex;
    }

    @Override
    public void addPublicationWeightedCvrIndex(long publicationId, long platformId, BidType bidType, double weightedCvrIndex) {
        Map<String, Map<Long, Double>> bidTypeMap = publicationWeightedCvrMap.get(platformId);
        if (bidTypeMap == null) {
            bidTypeMap = new HashMap<String, Map<Long, Double>>();
            publicationWeightedCvrMap.put(platformId, bidTypeMap);
        }
        Map<Long, Double> publicationMap = bidTypeMap.get(bidType.getName());
        if (publicationMap == null) {
            publicationMap = new HashMap<Long, Double>();
            bidTypeMap.put(bidType.getName(), publicationMap);
        }
        publicationMap.put(publicationId, weightedCvrIndex);
    }

    @Override
    public double getPublicationWeightedCvrIndex(PublicationDto publication, PlatformDto platform, CreativeDto creative) {
        BidType bidType = null;
        Double publicationWeightedCvrIndex = null;
        if (platform != null) {
            //For house ads CurrentBid will be null
            if (creative.getCampaign().getCurrentBid() != null) {
                bidType = creative.getCampaign().getCurrentBid().getBidType();
                Map<String, Map<Long, Double>> bidTypeMap = publicationWeightedCvrMap.get(platform.getId());
                if (bidTypeMap != null) {
                    Map<Long, Double> publicationMap = bidTypeMap.get(bidType.getName());
                    if (publicationMap != null) {
                        publicationWeightedCvrIndex = publicationMap.get(publication.getId());
                    }
                }
            }
        }

        if (publicationWeightedCvrIndex == null) {
            publicationWeightedCvrIndex = 1.0;
        }
        return publicationWeightedCvrIndex;
    }

    @Override
    public void computeEcpmInfo(AdSpaceDto adspace, CreativeDto creative, PlatformDto platform, long countryId, BigDecimal bidFloorPrice, EcpmInfo ecpmInfo) {

        if (ecpmInfo == null) {
            throw new RuntimeException("ecpmInfo not initialized!");
        }

        ecpmInfo.reset();

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

        ExpectedStatsDto expectedStatsDto = getExpectedStats(adspace, creative, platform);
        double advertiserDiscount = creative.getCampaign().getAgencyDiscount();

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
        expectedRevenue = expectedRevenue * (1 - advertiserDiscount);

        if (adspace.getPublication().getPublisher().getRtbConfig() == null) {
            //NON Rtb expectedRevenue * publisherRevShare
            expectedSettlementPrice = expectedRevenue * adspace.getPublication().getPublisher().getCurrentRevShare();
            expectedSettlementPrice = expectedSettlementPrice - creative.getCampaign().getDataFee();
            if (expectedSettlementPrice < 0.0) {
                expectedSettlementPrice = 0.0;
            }
            bidPrice = expectedSettlementPrice;
            expectedProfit = expectedRevenue - expectedSettlementPrice;
        } else {
            if (bidFloorPrice == null) {
                expectedSettlementPrice = 0.0;
            } else {
                expectedSettlementPrice = bidFloorPrice.doubleValue();
            }

            double buyerPremium = adspace.getPublication().getPublisher().getBuyerPremium();
            expectedProfit = expectedRevenue - expectedSettlementPrice * (1 + buyerPremium) * winningProbability;
            bidPrice = getBidPriceForRtb(adspace, creative, expectedStatsDto.getExpectedRgr());
        }

        weight = expectedProfit;
        if (weight <= 0.0) {
            weight = 0.0;
        } else {
            switch (creative.getCampaign().getCurrentBid().getBidType()) {
            case CPM:
                double campaignCtrTarget = getDefaultDoubleValue("default_ctr_target", 0.0001);
                CampaignCtrInfo campaignCtrInfo = this.getCampaignCtrInfo(creative.getCampaign().getId());
                if (campaignCtrInfo != null) {
                    //If RunningCampaignTargetCtr exists use it and discard default ctr target
                    campaignCtrTarget = campaignCtrInfo.getTargetCtr();
                }
                double ctrThreshold = creative.getCampaign().isMediaCostOptimisationEnabled() ? getDefaultDoubleValue("cpm_ctr_underperformance_threshold_media_cost_opt", 0.7)
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
                CampaignCtrInfo cpcCampaignCtrInfo = this.getCampaignCtrInfo(creative.getCampaign().getId());
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
                    CampaignCvrInfo campaignCvrInfo = this.getCampaignCvrInfo(creative.getCampaign().getId());
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
        // For Non-RTB, apply the campaign Boostfactor
        if (adspace.getPublication().getPublisher().getRtbConfig() == null) {
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

    @Override
    public void addCampaignRunningCtr(long campaignId, Double targetCtr, Double currentCtr) {
        campaignTargetCtrMap.put(campaignId, new CampaignCtrInfo(targetCtr, currentCtr));
    }

    @Override
    public void addCampaignRunningCvr(long campaignId, Double targetCvr, Double currentCvr) {
        campaignTargetCvrMap.put(campaignId, new CampaignCvrInfo(targetCvr, currentCvr));
    }

    @Override
    public CampaignCtrInfo getCampaignCtrInfo(long campaignId) {
        return campaignTargetCtrMap.get(campaignId);
    }

    @Override
    public CampaignCvrInfo getCampaignCvrInfo(long campaignId) {
        return campaignTargetCvrMap.get(campaignId);
    }
}

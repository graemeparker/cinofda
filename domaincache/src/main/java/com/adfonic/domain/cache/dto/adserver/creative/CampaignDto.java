package com.adfonic.domain.cache.dto.adserver.creative;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CampaignDto extends BusinessKeyDto {
    private static final long serialVersionUID = 13L;

    private String name;
    private String externalID;
    private AdvertiserDto advertiser;
    private Date startDate;
    private Date endDate;
    private Campaign.Status status;
    private String reference;
    private boolean disableLanguageMatch;
    private double boostFactor;
    private Integer capImpressions;
    private Integer capPeriodSeconds;
    private boolean capPerCampaign;
    private boolean installTrackingEnabled;
    private boolean installTrackingAdXEnabled;
    private boolean conversionTrackingEnabled;
    private String applicationID;
    private CampaignBidDto currentBid;
    private Set<CampaignTimePeriodDto> timePeriods = new HashSet<CampaignTimePeriodDto>();
    private boolean houseAd;
    private int throttle;
    private String advertiserDomain;
    private Long categoryId;
    private Set<Long> deviceIdentifierTypeIds = new HashSet<Long>();
    private double tradingDeskMargin;
    private double dataFee = 0.0;
    private long dataFeeId = 0;
    private double rmAdServingFee;
    private PrivateMarketPlaceDealDto privateMarketPlaceDeal;
    // SC-511
    private boolean hasAudience = false;
    // SC-524
    private boolean isBehavioural = false;
    //AD-367
    private double agencyDiscount;
    //MAD-2915
    private boolean budgetManagerEnabled = false;
    private BigDecimal dailyBudgetImpressions;
    private BigDecimal overallBudgetImpressions;
    private BigDecimal dailyBudgetClicks;
    private BigDecimal overallBudgetClicks;
    private List<BidDeductionDto> bidDeductions = new ArrayList<>();
    private Set<CampaignAudienceDto> locationAudiences = new HashSet<>();
    private Set<CampaignAudienceDto> deviceIdAudiences = new HashSet<>();
    private Set<CampaignAudienceDto> adsquareAudiences = new HashSet<>();
    private Set<CampaignAudienceDto> factualProximityAudiences = new HashSet<>();
    private Set<CampaignAudienceDto> factualAudienceAudiences = new HashSet<>();
    private BigDecimal maxBidThreshold;

    //MAD-2667
    private boolean mediaCostOptimisationEnabled = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(AdvertiserDto advertiser) {
        this.advertiser = advertiser;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Campaign.Status getStatus() {
        return status;
    }

    public void setStatus(Campaign.Status status) {
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean getDisableLanguageMatch() {
        return disableLanguageMatch;
    }

    public void setDisableLanguageMatch(boolean disableLanguageMatch) {
        this.disableLanguageMatch = disableLanguageMatch;
    }

    public double getBoostFactor() {
        return boostFactor;
    }

    public void setBoostFactor(double boostFactor) {
        this.boostFactor = boostFactor;
    }

    public Integer getCapImpressions() {
        return capImpressions;
    }

    public void setCapImpressions(Integer capImpressions) {
        this.capImpressions = capImpressions;
    }

    public Integer getCapPeriodSeconds() {
        return capPeriodSeconds;
    }

    public void setCapPeriodSeconds(Integer capPeriodSeconds) {
        this.capPeriodSeconds = capPeriodSeconds;
    }

    public boolean isCapPerCampaign() {
        return capPerCampaign;
    }

    public void setCapPerCampaign(boolean capPerCampaign) {
        this.capPerCampaign = capPerCampaign;
    }

    public boolean isInstallTrackingEnabled() {
        return installTrackingEnabled;
    }

    public void setInstallTrackingEnabled(boolean installTrackingEnabled) {
        this.installTrackingEnabled = installTrackingEnabled;
    }

    public boolean isInstallTrackingAdXEnabled() {
        return installTrackingAdXEnabled;
    }

    public void setInstallTrackingAdXEnabled(boolean installTrackingAdXEnabled) {
        this.installTrackingAdXEnabled = installTrackingAdXEnabled;
    }

    public boolean isConversionTrackingEnabled() {
        return conversionTrackingEnabled;
    }

    public void setConversionTrackingEnabled(boolean conversionTrackingEnabled) {
        this.conversionTrackingEnabled = conversionTrackingEnabled;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public CampaignBidDto getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(CampaignBidDto currentBid) {
        this.currentBid = currentBid;
    }

    public Set<CampaignTimePeriodDto> getTimePeriods() {
        return timePeriods;
    }

    public boolean isHouseAd() {
        return houseAd;
    }

    public void setHouseAd(boolean houseAd) {
        this.houseAd = houseAd;
    }

    public int getThrottle() {
        return throttle;
    }

    public void setThrottle(int throttle) {
        this.throttle = throttle;
    }

    public String getAdvertiserDomain() {
        return advertiserDomain;
    }

    public void setAdvertiserDomain(String advertiserDomain) {
        this.advertiserDomain = advertiserDomain;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Set<Long> getDeviceIdentifierTypeIds() {
        return deviceIdentifierTypeIds;
    }

    public List<CampaignTimePeriodDto> getSortedTimePeriods() {
        List<CampaignTimePeriodDto> sorted = new ArrayList<CampaignTimePeriodDto>(timePeriods.size()); // initialCapacity for ++performance
        sorted.addAll(timePeriods);
        Collections.sort(sorted);
        return sorted;
    }

    /** Are we active not only status-wise, but also scheduling-wise? */
    public boolean isCurrentlyActive() {
        return Campaign.Status.ACTIVE.equals(status) && (timePeriods.isEmpty() || (getCurrentTimePeriod() != null));
    }

    private static CampaignTimePeriodDto GAP = new CampaignTimePeriodDto();

    private transient CampaignTimePeriodDto currentTimePeriod;
    private transient CampaignTimePeriodDto nextTimePeriod;

    private synchronized CampaignTimePeriodDto determineCurrentTimePeriod() {
        if (currentTimePeriod != null) {
            if (currentTimePeriod == GAP) {
                // We think we're in a gap...make sure we still should be
                if (nextTimePeriod == null || !nextTimePeriod.isCurrent()) {
                    return currentTimePeriod; // Still in the gap
                }
            } else if (currentTimePeriod.isCurrent()) {
                // Already good to go.  We probably got called by a thread that
                // was waiting for the synchronized call while another thread
                // already took care of things.
                return currentTimePeriod;
            }
        }

        // Since currentTimePeriod should never be null once "determined", we
        // always initialize it to GAP.  That way, if we don't find any time
        // period that is current, or future, we'll at least be able to denote
        // such a case.
        currentTimePeriod = GAP;

        // By default we'll assume there's no upcoming time period
        nextTimePeriod = null;

        // Iterate through a sorted list of all of the time periods
        List<CampaignTimePeriodDto> sorted = getSortedTimePeriods();
        Iterator<CampaignTimePeriodDto> iter = sorted.iterator();
        while (iter.hasNext()) {
            CampaignTimePeriodDto timePeriod = iter.next();
            if (timePeriod.isCurrent()) {
                // Found the one that is now current
                currentTimePeriod = timePeriod;
                // Keep track of the one coming up next
                nextTimePeriod = iter.hasNext() ? iter.next() : null;
                break;
            } else if (timePeriod.isFuture()) {
                // We haven't found the current one, but we hit one that's
                // in the future...this means we're currently in a "gap".
                currentTimePeriod = GAP;
                // Keep track of this time period being up next
                nextTimePeriod = timePeriod;
                break;
            }
        }
        return currentTimePeriod;
    }

    public CampaignTimePeriodDto getCurrentTimePeriod() {
        if (currentTimePeriod == null) {
            // This must be the first time this method was called
            synchronized (this) {
                if (currentTimePeriod == null) {
                    return mindTheGap(determineCurrentTimePeriod());
                }
            }
        }

        // Check to see if we're in the middle of a "gap"
        if (currentTimePeriod == GAP) {
            // See if the next time period applies yet
            if (nextTimePeriod != null && nextTimePeriod.isCurrent()) {
                // Yup, time to advance to the next time period
                return mindTheGap(determineCurrentTimePeriod());
            }

            // Nope, we're still in the gap
            return null;
        }
        // Make sure the current time period is still valid
        else if (currentTimePeriod.isCurrent()) {
            return currentTimePeriod;
        } else {
            // Time to advance to the next time period, if available
            return mindTheGap(determineCurrentTimePeriod());
        }
    }

    private static CampaignTimePeriodDto mindTheGap(CampaignTimePeriodDto ctp) {
        return ctp == GAP ? null : ctp;
    }

    public double getTradingDeskMargin() {
        return tradingDeskMargin;
    }

    public void setTradingDeskMargin(double tradingDeskMargin) {
        this.tradingDeskMargin = tradingDeskMargin;
    }

    public double getDataFee() {
        return dataFee;
    }

    public void setDataFee(double dataFee) {
        this.dataFee = dataFee;
    }

    public long getDataFeeId() {
        return dataFeeId;
    }

    public void setDataFeeId(long dataFeeId) {
        this.dataFeeId = dataFeeId;
    }

    public double getRmAdServingFee() {
        return rmAdServingFee;
    }

    public void setRmAdServingFee(double rmAdServingFee) {
        this.rmAdServingFee = rmAdServingFee;
    }

    public PrivateMarketPlaceDealDto getPrivateMarketPlaceDeal() {
        return privateMarketPlaceDeal;
    }

    public void setPrivateMarketPlaceDeal(PrivateMarketPlaceDealDto privateMarketPlaceDeal) {
        this.privateMarketPlaceDeal = privateMarketPlaceDeal;
    }

    // SC-511
    public boolean hasAudience() {
        return hasAudience;
    }

    public void setHasAudience(boolean hasAudience) {
        this.hasAudience = hasAudience;
    }

    // SC-524
    public boolean isBehavioural() {
        return isBehavioural;
    }

    public void setBehavioural(boolean isBehavioural) {
        this.isBehavioural = isBehavioural;
    }

    //AD-367
    public double getAgencyDiscount() {
        return agencyDiscount;
    }

    public void setAgencyDiscount(double agencyDiscount) {
        this.agencyDiscount = agencyDiscount;
    }

    public void addLocationAudience(CampaignAudienceDto audience) {
        this.locationAudiences.add(audience);
    }

    public Set<CampaignAudienceDto> getLocationAudiences() {
        return locationAudiences;
    }

    public void addDeviceIdAudience(CampaignAudienceDto audience) {
        this.deviceIdAudiences.add(audience);
    }

    public Set<CampaignAudienceDto> getDeviceIdAudiences() {
        return deviceIdAudiences;
    }

    public void addAdsquareAudience(CampaignAudienceDto audience) {
        this.adsquareAudiences.add(audience);
    }

    public Set<CampaignAudienceDto> getAdsquareAudiences() {
        return adsquareAudiences;
    }

    public void addFactualAudienceAudience(CampaignAudienceDto audience) {
        this.factualAudienceAudiences.add(audience);
    }

    public Set<CampaignAudienceDto> getFactualAudienceAudiences() {
        return factualAudienceAudiences;
    }

    public void addFactualProximityAudience(CampaignAudienceDto audience) {
        this.factualProximityAudiences.add(audience);
    }

    public Set<CampaignAudienceDto> getFactualProximityAudiences() {
        return factualProximityAudiences;
    }

    public boolean isBudgetManagerEnabled() {
        return budgetManagerEnabled;
    }

    public List<BidDeductionDto> getBidDeductions() {
        return bidDeductions;
    }

    public void setBidDeductions(List<BidDeductionDto> bidDeductions) {
        this.bidDeductions = bidDeductions;
    }

    public void setBudgetManagerEnabled(boolean budgetManagerEnabled) {
        this.budgetManagerEnabled = budgetManagerEnabled;
    }

    public BigDecimal getDailyBudgetImpressions() {
        return dailyBudgetImpressions;
    }

    public void setDailyBudgetImpressions(BigDecimal dailyBudgetImpressions) {
        this.dailyBudgetImpressions = dailyBudgetImpressions;
    }

    public BigDecimal getOverallBudgetImpressions() {
        return overallBudgetImpressions;
    }

    public void setOverallBudgetImpressions(BigDecimal overallBudgetImpressions) {
        this.overallBudgetImpressions = overallBudgetImpressions;
    }

    public BigDecimal getDailyBudgetClicks() {
        return dailyBudgetClicks;
    }

    public void setDailyBudgetClicks(BigDecimal dailyBudgetClicks) {
        this.dailyBudgetClicks = dailyBudgetClicks;
    }

    public BigDecimal getOverallBudgetClicks() {
        return overallBudgetClicks;
    }

    public void setOverallBudgetClicks(BigDecimal overallBudgetClicks) {
        this.overallBudgetClicks = overallBudgetClicks;
    }

    public BudgetType inferBudgetType() {
        if (dailyBudgetImpressions != null || overallBudgetImpressions != null) {
            return BudgetType.IMPRESSIONS;
        }
        if (dailyBudgetClicks != null || overallBudgetClicks != null) {
            return BudgetType.CLICKS;
        }
        return BudgetType.MONETARY;
    }

    public boolean isMediaCostOptimisationEnabled() {
        return mediaCostOptimisationEnabled;
    }

    public void setMediaCostOptimisationEnabled(boolean mediaCostOptimisationEnabled) {
        this.mediaCostOptimisationEnabled = mediaCostOptimisationEnabled;
    }


    public BigDecimal getMaxBidThreshold() {
        return maxBidThreshold;
    }

    public void setMaxBidThreshold(BigDecimal maxBidThreshold) {
        this.maxBidThreshold = maxBidThreshold;
    }

    @Override
    public String toString() {
        return "CampaignDto {" + getId() + ", name=" + name + ", externalID=" + externalID + ", advertiser=" + advertiser + ", startDate=" + startDate + ", endDate=" + endDate
                + ", status=" + status + ", disableLanguageMatch=" + disableLanguageMatch + ", boostFactor=" + boostFactor + ", capImpressions=" + capImpressions
                + ", capPeriodSeconds=" + capPeriodSeconds + ", capPerCampaign=" + capPerCampaign + ", installTrackingEnabled=" + installTrackingEnabled
                + ", installTrackingAdXEnabled=" + installTrackingAdXEnabled + ", conversionTrackingEnabled=" + conversionTrackingEnabled + ", applicationID=" + applicationID
                + ", currentBid=" + currentBid + ", timePeriods=" + timePeriods + ", houseAd=" + houseAd + ", throttle=" + throttle + ", advertiserDomain=" + advertiserDomain
                + ", categoryId=" + categoryId + ", deviceIdentifierTypeIds=" + deviceIdentifierTypeIds + ", tradingDeskMargin=" + tradingDeskMargin + ", dataFee=" + dataFee
                + ", dataFeeId=" + dataFeeId + ", rmAdServingFee=" + rmAdServingFee + ", privateMarketPlaceDeal=" + privateMarketPlaceDeal + ", hasAudience=" + hasAudience
                + ", isBehavioural=" + isBehavioural + ", agencyDiscount=" + agencyDiscount + ", deviceIdAudiences=" + deviceIdAudiences + ", locationAudiences="
                + locationAudiences + ", adsquareAudiences=" + adsquareAudiences + ", mediaCostOptimisationEnabled=" + mediaCostOptimisationEnabled 
                + ", maxBidThreshold=" + maxBidThreshold
                + "}";
    }

}

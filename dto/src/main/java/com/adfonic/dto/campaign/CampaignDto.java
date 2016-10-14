package com.adfonic.dto.campaign;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.audience.CampaignAudienceDto;
import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;
import com.adfonic.dto.campaign.campaignagencydiscount.CampaignAgencyDiscountDto;
import com.adfonic.dto.campaign.campaignbid.BidDeductionDto;
import com.adfonic.dto.campaign.campaignbid.CampaignBidDto;
import com.adfonic.dto.campaign.campaigndatafee.CampaignDataFeeDto;
import com.adfonic.dto.campaign.campaignrichmediaadservingfee.CampaignRichMediaAdServingFeeDto;
import com.adfonic.dto.campaign.campaigntargetctr.CampaignTargetCTRDto;
import com.adfonic.dto.campaign.campaigntargetcvr.CampaignTargetCVRDto;
import com.adfonic.dto.campaign.campaigntradingdeskmargin.CampaignTradingDeskMarginDto;
import com.adfonic.dto.campaign.enums.BiddingStrategyName;
import com.adfonic.dto.campaign.enums.InventoryTargetingType;
import com.adfonic.dto.campaign.privatemarketplace.PrivateMarketplaceDto;
import com.adfonic.dto.campaign.scheduling.CampaignTimePeriodDto;
import com.adfonic.dto.campaign.scheduling.ScheduleDto;
import com.adfonic.dto.campaign.segment.SegmentDto;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.dto.language.LanguageDto;
import com.adfonic.dto.targetpublisher.TargetPublisherDto;

public class CampaignDto extends CampaignBudgetInfoDto {

    private static final long serialVersionUID = 1L;
    
    private static final int DEFAULT_CAP_IMPRESSIONS = 10;
    private static final String BUDTYPE_IMPRESSION   = "IMPRESSION";
    private static final String BUDTYPE_CLICK        = "CLICK";
    private static final String BUDTYPE_MONETARY     = "MONETARY";

    @DTOCascade
    @Source(value = "advertiser")
    private AdvertiserDto advertiser;
    @DTOCascade
    @Source(value = "segments")
    private List<SegmentDto> segments;
    @DTOCascade
    @Source(value = "defaultLanguage")
    private LanguageDto defaultLanguage; // used for new creatives

    @DTOCascade
    @Source(value = "disableLanguageMatch")
    private boolean disableLanguageMatch = true;

    @DTOCascade
    @Source(value = "timePeriods")
    private List<CampaignTimePeriodDto> timePeriods = new ArrayList<CampaignTimePeriodDto>(0);

    @Source(value = "capImpressions")
    private Integer capImpressions = DEFAULT_CAP_IMPRESSIONS;
    @Source(value = "capPeriodSeconds")
    private Integer capPeriodSeconds;
    @Source(value = "capPerCampaign")
    private boolean capPerCampaign;
    
    private SegmentDto currentSegment;

    @Source(value = "currentBid")
    private CampaignBidDto currentBid;
    
    @DTOCascade
    @Source("currentBidDeductions")
    private List<BidDeductionDto> currentBidDeductions = new ArrayList<BidDeductionDto>();

    // Install tracking for iPhone apps
    @Source(value = "installTrackingEnabled")
    private boolean installTrackingEnabled;

    @Source(value = "applicationID")
    private String applicationID;

    @Source(value = "conversionTrackingEnabled")
    private boolean conversionTrackingEnabled;

    @Source(value = "installTrackingAdXEnabled")
    private boolean installTrackingAdXEnabled;

    @Source(value = "priceOverridden")
    private boolean priceOverridden;

    @DTOCascade
    @Source(value = "deviceIdentifierTypes")
    private Set<DeviceIdentifierTypeDto> deviceIdentifierTypes;

    @Source(value = "targetCTR")
    private CampaignTargetCTRDto targetCTR;

    @Source(value = "targetCVR")
    private CampaignTargetCVRDto targetCVR;

    @Source(value = "evenDistributionOverallBudget")
    private boolean evenDistributionOverallBudget;

    @Source(value = "evenDistributionDailyBudget")
    private boolean evenDistributionDailyBudget;

    @Source(value = "currentRichMediaAdServingFee")
    private CampaignRichMediaAdServingFeeDto currentRichMediaAdServingFee;

    @Source(value = "currentTradingDeskMargin")
    private CampaignTradingDeskMarginDto currentTradingDeskMargin;

    @Source(value = "currentTradingDeskMargin")
    private CampaignDataFeeDto currentDataFee;

    @Source(value = "privateMarketPlaceDeal")
    private PrivateMarketplaceDto privateMarketPlaceDeal;

    @Source(value = "inventoryTargetingType")
    private Campaign.InventoryTargetingType inventoryTargetingType;

    private List<TargetPublisherDto> rtb = new ArrayList<TargetPublisherDto>();
    private List<TargetPublisherDto> nonRtb = new ArrayList<TargetPublisherDto>();

    @Source("advertiserDomain")
    private String advertiserDomain;

    @Source("targetCPA")
    private BigDecimal targetCPA;

    @DTOCascade
    @Source("campaignAudiences")
    private List<CampaignAudienceDto> campaignAudiences = new ArrayList<CampaignAudienceDto>();

    @DTOCascade
    @Source("currentAgencyDiscount")
    private CampaignAgencyDiscountDto currentAgencyDiscount;
    
    private ScheduleDto scheduleDto = new ScheduleDto();
    
    @DTOCascade
    @Source("currencyExchangeRate")
    private CurrencyExchangeRateDto currencyExchangeRate;
    
    @Source("exchangeRate")
    private BigDecimal exchangeRate;
    
    @Source("exchangeRateAdminChange")
    private boolean exchangeRateAdminChange;
    
    @Source("biddingStrategies")
    private Set<BiddingStrategyName> biddingStrategies;
    
    @Source("maxBidThreshold")
    private BigDecimal maxBidThreshold;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public LanguageDto getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(LanguageDto defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public boolean isDisableLanguageMatch() {
        return disableLanguageMatch;
    }

    public void setDisableLanguageMatch(boolean disableLanguageMatch) {
        this.disableLanguageMatch = disableLanguageMatch;
    }

    public List<SegmentDto> getSegments() {
        return segments;
    }

    public void setSegments(List<SegmentDto> segments) {
        this.segments = segments;
    }

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(AdvertiserDto advertiser) {
        this.advertiser = advertiser;
    }

    public ScheduleDto getScheduleDto() {
        return scheduleDto;
    }

    public void setScheduleDto(ScheduleDto scheduleDto) {
        this.scheduleDto = scheduleDto;
    }

    public List<CampaignTimePeriodDto> getTimePeriods() {
        Collections.sort(timePeriods);
        return timePeriods;
    }

    public void setTimePeriods(List<CampaignTimePeriodDto> timePeriods) {
        this.timePeriods = timePeriods;
    }

    public SegmentDto getCurrentSegment() {
        if (getSegments() != null && !getSegments().isEmpty()) {
            return getSegments().get(0);
        } else if (currentSegment == null) {
            currentSegment = new SegmentDto();
        }
        return currentSegment;
    }

    public void setCurrentSegment(SegmentDto currentSegment) {
        this.currentSegment = currentSegment;
    }

    @Override
    public BigDecimal getDailyBudget() {
        if ((dailyBudget == null) && (dailyBudgetWeekday != null)) {
            dailyBudget = dailyBudgetWeekday;
        }
        return dailyBudget;
    }

    public CampaignBidDto getCurrentBid() {
        if (currentBid == null) {
            currentBid = new CampaignBidDto();
        }
        return currentBid;
    }

    public void setCurrentBid(CampaignBidDto currentBid) {
        this.currentBid = currentBid;
    }

    public List<BidDeductionDto> getCurrentBidDeductions() {
		return currentBidDeductions;
	}

	public void setCurrentBidDeductions(List<BidDeductionDto> currentBidDeductions) {
		this.currentBidDeductions = currentBidDeductions;
	}

	public boolean getInstallTrackingEnabled() {
        return installTrackingEnabled;
    }

    public void setInstallTrackingEnabled(boolean installTrackingEnabled) {
        this.installTrackingEnabled = installTrackingEnabled;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public boolean getConversionTrackingEnabled() {
        return conversionTrackingEnabled;
    }

    public void setConversionTrackingEnabled(boolean conversionTrackingEnabled) {
        this.conversionTrackingEnabled = conversionTrackingEnabled;
    }

    public Set<DeviceIdentifierTypeDto> getDeviceIdentifierTypes() {
        return deviceIdentifierTypes;
    }

    public void setDeviceIdentifierTypes(Set<DeviceIdentifierTypeDto> deviceIdentifierTypes) {
        this.deviceIdentifierTypes = deviceIdentifierTypes;
    }

    public boolean getInstallTrackingAdXEnabled() {
        return installTrackingAdXEnabled;
    }

    public void setInstallTrackingAdXEnabled(boolean installTrackingAdXEnabled) {
        this.installTrackingAdXEnabled = installTrackingAdXEnabled;
    }

    public List<TargetPublisherDto> getRtb() {
        return rtb;
    }

    public void setRtb(List<TargetPublisherDto> rtb) {
        this.rtb = rtb;
    }

    public List<TargetPublisherDto> getNonRtb() {
        return nonRtb;
    }

    public void setNonRtb(List<TargetPublisherDto> nonRtb) {
        this.nonRtb = nonRtb;
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

    public boolean isPriceOverridden() {
        return priceOverridden;
    }

    public void setPriceOverridden(boolean priceOverridden) {
        this.priceOverridden = priceOverridden;
    }

    public CampaignTargetCTRDto getTargetCTR() {
        return targetCTR;
    }

    public void setTargetCTR(CampaignTargetCTRDto targetCTR) {
        this.targetCTR = targetCTR;
    }

    public CampaignTargetCVRDto getTargetCVR() {
        return targetCVR;
    }

    public void setTargetCVR(CampaignTargetCVRDto targetCVR) {
        this.targetCVR = targetCVR;
    }

    public boolean isEvenDistributionOverallBudget() {
        return evenDistributionOverallBudget;
    }

    public void setEvenDistributionOverallBudget(boolean evenDistributionOverallBudget) {
        this.evenDistributionOverallBudget = evenDistributionOverallBudget;
    }

    public boolean isEvenDistributionDailyBudget() {
        return evenDistributionDailyBudget;
    }

    public void setEvenDistributionDailyBudget(boolean evenDistributionDailyBudget) {
        this.evenDistributionDailyBudget = evenDistributionDailyBudget;
    }

    public CampaignRichMediaAdServingFeeDto getCurrentRichMediaAdServingFee() {
        return currentRichMediaAdServingFee;
    }

    public void setCurrentRichMediaAdServingFee(CampaignRichMediaAdServingFeeDto currentRichMediaAdServingFee) {
        this.currentRichMediaAdServingFee = currentRichMediaAdServingFee;
    }

    public CampaignTradingDeskMarginDto getCurrentTradingDeskMargin() {
        return currentTradingDeskMargin;
    }

    public void setCurrentTradingDeskMargin(CampaignTradingDeskMarginDto currentTradingDeskMargin) {
        this.currentTradingDeskMargin = currentTradingDeskMargin;
    }

    public CampaignDataFeeDto getCurrentDataFee() {
        return currentDataFee;
    }

    public void setCurrentDataFee(CampaignDataFeeDto currentDataFee) {
        this.currentDataFee = currentDataFee;
    }

    public PrivateMarketplaceDto getPrivateMarketPlaceDeal() {
        return privateMarketPlaceDeal;
    }

    public void setPrivateMarketPlaceDeal(PrivateMarketplaceDto privateMarketPlaceDeal) {
        this.privateMarketPlaceDeal = privateMarketPlaceDeal;
    }

    public String getBudType() {
        if (budgetType != null) {
            if (this.budgetType.equals(BudgetType.MONETARY)) {
                return BUDTYPE_MONETARY;
            } else if (this.budgetType.equals(BudgetType.CLICKS)) {
                return BUDTYPE_CLICK;
            } else if (this.budgetType.equals(BudgetType.IMPRESSIONS)) {
                return BUDTYPE_IMPRESSION;
            }
        }
        return BUDTYPE_MONETARY;
    }

    public void setBudType(String budType) {
        if (budType.equals(BUDTYPE_MONETARY)) {
            this.budgetType = BudgetType.MONETARY;
        } else if (budType.equals(BUDTYPE_CLICK)) {
            this.budgetType = BudgetType.CLICKS;
        } else if (budType.equals(BUDTYPE_IMPRESSION)) {
            this.budgetType = BudgetType.IMPRESSIONS;
        } else {
            this.budgetType = BudgetType.MONETARY;
        }
    }

    public String getAdvertiserDomain() {
        return advertiserDomain;
    }

    public void setAdvertiserDomain(String advertiserDomain) {
        this.advertiserDomain = advertiserDomain;
    }

    public BigDecimal getTargetCPA() {
        return targetCPA;
    }

    public void setTargetCPA(BigDecimal targetCPA) {
        this.targetCPA = targetCPA;
    }

    public List<CampaignAudienceDto> getCampaignAudiences() {
        return campaignAudiences;
    }

    public void setCampaignAudiences(List<CampaignAudienceDto> campaignAudiences) {
        this.campaignAudiences = campaignAudiences;
    }

    public CampaignAgencyDiscountDto getCurrentAgencyDiscount() {
        return currentAgencyDiscount;
    }

    public void setCurrentAgencyDiscount(CampaignAgencyDiscountDto currentAgencyDiscount) {
        this.currentAgencyDiscount = currentAgencyDiscount;
    }

    public Campaign.InventoryTargetingType getInventoryTargetingType() {
        return inventoryTargetingType;
    }

    public void setInventoryTargetingType(Campaign.InventoryTargetingType inventoryTargetingType) {
        this.inventoryTargetingType = inventoryTargetingType;
    }

    public InventoryTargetingType getInventoryType() {
        if (this.inventoryTargetingType != null) {
            return InventoryTargetingType.fromString(this.inventoryTargetingType.name());
        } else {
            return null;
        }
    }

    public void setInventoryType(InventoryTargetingType inventoryType) {
        if (inventoryType != null) {
            this.inventoryTargetingType = inventoryType.getInventoryTargetingType();
        }
    }

    public CurrencyExchangeRateDto getCurrencyExchangeRate() {
        return currencyExchangeRate;
    }

    public void setCurrencyExchangeRate(CurrencyExchangeRateDto currencyExchangeRate) {
        this.currencyExchangeRate = currencyExchangeRate;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public boolean isExchangeRateAdminChange() {
        return exchangeRateAdminChange;
    }

    public void setExchangeRateAdminChange(boolean exchangeRateAdminChange) {
        this.exchangeRateAdminChange = exchangeRateAdminChange;
    }
    
    public Set<BiddingStrategyName> getBiddingStrategies() {
        return biddingStrategies;
    }

    public void setBiddingStrategies(Set<BiddingStrategyName> biddingStrategies) {
        this.biddingStrategies = biddingStrategies;
    }
    
    public double getTotalBidDeductions() {
		return currentBidDeductions.stream().mapToDouble(bidDeduction -> bidDeduction.getAmount().doubleValue()).sum();
    }

    public boolean isCapPerCampaign() {
        return capPerCampaign;
    }

    public void setCapPerCampaign(boolean capPerCampaign) {
        this.capPerCampaign = capPerCampaign;
    }

	public BigDecimal getMaxBidThreshold() {
		return maxBidThreshold;
	}

	public void setMaxBidThreshold(BigDecimal maxBidThreshold) {
		this.maxBidThreshold = maxBidThreshold;
	}
    
}

package com.adfonic.domain.cache.dto.datacollector.campaign;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CampaignDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private AdvertiserDto advertiser;
    private boolean installTrackingEnabled;
    private boolean installTrackingAdXEnabled;
    private boolean conversionTrackingEnabled;
    private String applicationID;
    private CampaignBidDto currentBid;
    private List<CampaignBidDto> historicalBids = new ArrayList<CampaignBidDto>();
    private BigDecimal tradingDeskMargin = new BigDecimal(0.0);
    private BigDecimal rmAdServingFee = new BigDecimal(0.0);
    // SC-511
    private boolean hasAudience = false;
    // SC-524
    private boolean isBehavioural = false;
    //AD-367
    private BigDecimal agencyDiscount = new BigDecimal(0.0);
    //MAD-1048
    private CampaignDataFeeDto currentDataFee = null;
    private List<CampaignDataFeeDto> historicalDataFees = new ArrayList<CampaignDataFeeDto>();
    private boolean pmp;

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(AdvertiserDto advertiser) {
        this.advertiser = advertiser;
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

    public List<CampaignBidDto> getHistoricalBids() {
        return historicalBids;
    }

    public CampaignBidDto getBidForDate(Date date) {
        for (CampaignBidDto cb : historicalBids) {
            if (!date.before(cb.getStartDate())) {
                Date cbEnd = cb.getEndDate();
                if ((cbEnd == null) || date.before(cbEnd)) {
                    return cb;
                }
            }
        }
        return null; // no bid for that time
    }

    public BigDecimal getTradingDeskMargin() {
        return tradingDeskMargin;
    }

    public void setTradingDeskMargin(BigDecimal tradingDeskMargin) {
        this.tradingDeskMargin = tradingDeskMargin;
    }

    public BigDecimal getRmAdServingFee() {
        return rmAdServingFee;
    }

    public void setRmAdServingFee(BigDecimal rmAdServingFee) {
        this.rmAdServingFee = rmAdServingFee;
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
    public BigDecimal getAgencyDiscount() {
        return agencyDiscount;
    }

    public void setAgencyDiscount(BigDecimal agencyDiscount) {
        this.agencyDiscount = agencyDiscount;
    }

    //MAD-1048
    public CampaignDataFeeDto getCurrentDataFee() {
        return currentDataFee;
    }

    public void setCurrentDataFee(CampaignDataFeeDto currentDataFee) {
        this.currentDataFee = currentDataFee;
    }

    public List<CampaignDataFeeDto> getHistoricalDataFees() {
        return historicalDataFees;
    }

    public CampaignDataFeeDto getDataFeeForDate(Date date) {
        if (historicalDataFees != null) {
            for (CampaignDataFeeDto cdf : historicalDataFees) {
                if (!date.before(cdf.getStartDate())) {
                    Date cbEnd = cdf.getEndDate();
                    if ((cbEnd == null) || date.before(cbEnd)) {
                        return cdf;
                    }
                }
            }
        }
        return null; // no data fee for that time
    }

    public void setPMP(boolean pmp) {
        this.pmp = pmp;
    }

    public boolean isPMP() {
        return pmp;
    }

    @Override
    public String toString() {
        return "CampaignDto {" + getId() + ", advertiser=" + advertiser + ", installTrackingEnabled=" + installTrackingEnabled + ", installTrackingAdXEnabled="
                + installTrackingAdXEnabled + ", conversionTrackingEnabled=" + conversionTrackingEnabled + ", applicationID=" + applicationID + ", currentBid=" + currentBid
                + ", historicalBids=" + historicalBids + ", tradingDeskMargin=" + tradingDeskMargin + ", rmAdServingFee=" + rmAdServingFee + ", hasAudience=" + hasAudience
                + ", isBehavioural=" + isBehavioural + ", agencyDiscount=" + agencyDiscount + ", currentDataFee=" + currentDataFee + ", historicalDataFees=" + historicalDataFees
                + ", pmp=" + pmp + "}";
    }

}

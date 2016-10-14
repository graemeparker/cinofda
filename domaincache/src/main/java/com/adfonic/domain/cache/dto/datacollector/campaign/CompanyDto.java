package com.adfonic.domain.cache.dto.datacollector.campaign;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.adfonic.domain.cache.dto.BusinessKeyDto;
import com.adfonic.util.TimeZoneUtils;

public class CompanyDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    @Deprecated
    private BigDecimal discount; // needed for pricing info and also RTB price calc
    private boolean postPay;
    private boolean taxableAdvertiser;
    private String defaultTimeZoneID;
    private boolean backfill;
    private BigDecimal mediaCostMargin = new BigDecimal(0.0);
    //AD-367
    private BigDecimal marginShareDSP = new BigDecimal(1.0);


	private CompanyDirectCostDto directCost;
    private List<CompanyDirectCostDto> historicalDirectCost = new ArrayList<CompanyDirectCostDto>();

    public List<CompanyDirectCostDto> getHistoricalDirectCost() {
    	return historicalDirectCost;
    }

    private volatile transient TimeZone defaultTimeZone;

    @Deprecated
    public BigDecimal getDiscount() {
        return discount;
    }

    @Deprecated
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public boolean isPostPay() {
        return postPay;
    }

    public void setPostPay(boolean postPay) {
        this.postPay = postPay;
    }

    public boolean isTaxableAdvertiser() {
        return taxableAdvertiser;
    }

    public void setTaxableAdvertiser(boolean taxableAdvertiser) {
        this.taxableAdvertiser = taxableAdvertiser;
    }

    public String getDefaultTimeZoneID() {
        return defaultTimeZoneID;
    }

    public void setDefaultTimeZoneID(String defaultTimeZoneID) {
        this.defaultTimeZoneID = defaultTimeZoneID;
    }

    public TimeZone getDefaultTimeZone() {
        if (defaultTimeZone == null) {
            defaultTimeZone = TimeZoneUtils.getTimeZoneNonBlocking(defaultTimeZoneID);
        }
        return defaultTimeZone;
    }

    public boolean isBackfill() {
        return backfill;
    }

    public void setBackfill(boolean backfill) {
        this.backfill = backfill;
    }

    public BigDecimal getMediaCostMargin() {
        return mediaCostMargin;
    }

    public void setMediaCostMargin(BigDecimal mediaCostMargin) {
        this.mediaCostMargin = mediaCostMargin;
    }

    //AD-367
    public BigDecimal getMarginShareDSP() {
        return marginShareDSP;
    }

    public void setMarginShareDSP(BigDecimal marginShareDSP) {
        this.marginShareDSP = marginShareDSP;
    }


    public CompanyDirectCostDto getDirectCost() {
        return directCost;
    }

    public void setDirectCost(CompanyDirectCostDto directCost) {
        this.directCost = directCost;
    }


    public CompanyDirectCostDto getDirectCostForDate(Date date) {
        for (CompanyDirectCostDto dc : historicalDirectCost) {
            if (!date.before(dc.getStartDate())) {
                Date cbEnd = dc.getEndDate();
                if ((cbEnd == null) || date.before(cbEnd)) {
                    return dc;
                }
            }
        }
        return null; // no DirectCost for that time
    }
    
    @Override
    public String toString() {
        return "CompanyDto {" + getId() + ", discount=" + discount + ", postPay=" + postPay + ", taxableAdvertiser=" + taxableAdvertiser + ", defaultTimeZoneID="
                + defaultTimeZoneID + ", backfill=" + backfill + ", mediaCostMargin=" + mediaCostMargin + ", marginShareDSP=" + marginShareDSP + ", directCost=" + directCost + "]";
    }

}

package com.adfonic.dto.advertiser;

import java.math.BigDecimal;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.domain.Advertiser;
import com.adfonic.dto.BusinessKeyDTO;
import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;

public class AdvertiserDto extends BusinessKeyDTO implements Comparable<AdvertiserDto> {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "name")
    private String name;

    @Source(value = "dailyBudget")
    private BigDecimal dailyBudget;

    @Source(value = "externalID")
    private String externalID;

    @Source(value = "status")
    private Advertiser.Status status;
    
    @DTOCascade
    @Source("defaultCurrencyExchangeRate")
    private CurrencyExchangeRateDto defaultCurrencyExchangeRate;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(BigDecimal dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public Advertiser.Status getStatus() {
        return status;
    }

    public void setStatus(Advertiser.Status status) {
        this.status = status;
    }

    public AdvertiserStatus getAdvStatus() {
        if (status != null) {
            return AdvertiserStatus.valueOf(this.status.toString());
        } else {
            return null;
        }
    }

    public void setAdvStatus(AdvertiserStatus advStatus) {
        if (advStatus != null) {
            this.status = advStatus.getStatus();
        }
    }
    
    public CurrencyExchangeRateDto getDefaultCurrencyExchangeRate() {
        return defaultCurrencyExchangeRate;
    }

    public void setDefaultCurrencyExchangeRate(CurrencyExchangeRateDto defaultCurrencyExchangeRate) {
        this.defaultCurrencyExchangeRate = defaultCurrencyExchangeRate;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AdvertiserDto [name=");
        builder.append(name);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(AdvertiserDto arg0) {
        if (arg0 == null || arg0.getName() == null) {
            return 1;
        }
        if (name == null) {
            return -1;
        } else {
            return name.toLowerCase().compareTo(arg0.getName().toLowerCase());
        }
    }

}

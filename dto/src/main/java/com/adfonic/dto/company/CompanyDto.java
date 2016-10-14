package com.adfonic.dto.company;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;

import com.adfonic.domain.AccountType;
import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;

public class CompanyDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "externalID")
    private String externalID;
    
    @DTOTransient
    private TimeZone timeZone;

    @Source(value = "isInvoiceDateInGMT")
    private boolean isInvoiceDateInGMT;

    @Source(value = "accountTypeFlags")
    private int accountTypeFlags;

    @DTOTransient
    private List<String> accountTypes;

    @Source(value = "taxCode")
    private String taxCode;

    @Source(value = "defaultTimeZoneId")
    private String defaultTimeZoneId;
    
    @DTOCascade
    @Source("defaultCurrencyExchangeRate")
    private CurrencyExchangeRateDto defaultCurrencyExchangeRate;

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isInvoiceDateInGMT() {
        return isInvoiceDateInGMT;
    }

    public void setInvoiceDateInGMT(boolean isInvoiceDateInGMT) {
        this.isInvoiceDateInGMT = isInvoiceDateInGMT;
    }

    public int getAccountTypeFlags() {
        return accountTypeFlags;
    }

    public void setAccountTypeFlags(int accountTypeFlags) {
        this.accountTypeFlags = accountTypeFlags;
        this.accountTypes = getAccountTypes(accountTypeFlags);
    }

    public List<String> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(List<String> accountTypes) {
        this.accountTypes = accountTypes;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getDefaultTimeZoneId() {
        return defaultTimeZoneId;
    }

    public void setDefaultTimeZoneId(String defaultTimeZone) {
        this.defaultTimeZoneId = defaultTimeZone;
    }
    
    public CurrencyExchangeRateDto getDefaultCurrencyExchangeRate() {
        return defaultCurrencyExchangeRate;
    }

    public void setDefaultCurrencyExchangeRate(CurrencyExchangeRateDto defaultCurrencyExchangeRate) {
        this.defaultCurrencyExchangeRate = defaultCurrencyExchangeRate;
    }

    private List<String> getAccountTypes(int accountTypeFlags) {
        List<String> types = new ArrayList<String>();
        if (AccountType.ADVERTISER.isSet(accountTypeFlags)) {
            types.add(AccountType.ADVERTISER.name());
        }

        if (AccountType.PUBLISHER.isSet(accountTypeFlags)) {
            types.add(AccountType.PUBLISHER.name());
        }

        if (AccountType.AGENCY.isSet(accountTypeFlags)) {
            types.add(AccountType.AGENCY.name());
        }
        return types;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CompanyDto [externalID=");
        builder.append(externalID);
        builder.append(", ");
        builder.append("timeZone=");
        builder.append(timeZone);
        builder.append(", ");
        builder.append("defaultTimeZoneId=");
        builder.append(defaultTimeZoneId);
        builder.append(", ");
        builder.append("isInvoiceDateInGMT=");
        builder.append(isInvoiceDateInGMT);
        builder.append(", ");
        builder.append("accountTypeFlags=");
        builder.append(accountTypeFlags);
        builder.append(", ");
        builder.append("taxCode=");
        builder.append(taxCode);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}

package com.adfonic.dto.country;

import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class CountryDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "name")
    private String name;
    @Source(value = "isoCode")
    private String isoCode;
    @Source(value = "isoAlpha3")
    private String isoAlpha3;
    @Source(value = "dialPrefix")
    private String dialPrefix;
    @DTOTransient
    private com.adfonic.dto.campaign.enums.TaxRegime taxRegime;
    private boolean hidden;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getIsoAlpha3() {
        return isoAlpha3;
    }

    public void setIsoAlpha3(String isoAlpha3) {
        this.isoAlpha3 = isoAlpha3;
    }

    public String getDialPrefix() {
        return dialPrefix;
    }

    public void setDialPrefix(String dialPrefix) {
        this.dialPrefix = dialPrefix;
    }

    public com.adfonic.dto.campaign.enums.TaxRegime getTaxRegime() {
        return taxRegime;
    }

    public void setTaxRegime(com.adfonic.dto.campaign.enums.TaxRegime taxRegime) {
        this.taxRegime = taxRegime;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

}

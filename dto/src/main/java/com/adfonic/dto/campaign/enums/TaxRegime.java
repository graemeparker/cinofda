package com.adfonic.dto.campaign.enums;

import com.adfonic.domain.Country;

public enum TaxRegime {
    UK(Country.TaxRegime.UK), 
    EU(Country.TaxRegime.EU), 
    ROW(Country.TaxRegime.ROW);

    private Country.TaxRegime taxregime;
    
    private TaxRegime(Country.TaxRegime taxregime) {
        this.taxregime = taxregime;
    }

    public Country.TaxRegime getTaxregime() {
        return taxregime;
    }

    public void setTaxregime(Country.TaxRegime taxregime) {
        this.taxregime = taxregime;
    }
}

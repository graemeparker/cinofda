package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Country;

public class CountryConverter extends BaseReferenceEntityConverter<Country> {

    public CountryConverter() {
        super(Country.class, "isoCode");
    }

    @Override
    public Country resolveEntity(String isoCode) {
        return getCommonManager().getCountryByIsoCode(isoCode);
    }
}

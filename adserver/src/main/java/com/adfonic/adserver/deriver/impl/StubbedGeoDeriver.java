package com.adfonic.adserver.deriver.impl;

import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.geo.AustrianProvince;
import com.adfonic.geo.CanadianProvince;
import com.adfonic.geo.ChineseProvince;
import com.adfonic.geo.Coordinates;
import com.adfonic.geo.Dma;
import com.adfonic.geo.DmaManager;
import com.adfonic.geo.SimpleCoordinates;
import com.adfonic.geo.USState;
import com.adfonic.util.TimeZoneUtils;

// To use this deriver, uncomment the @Component and comment out that
// annotation on QuovaGeoDeriverImpl.
//@Component
public class StubbedGeoDeriver extends AbstractGeoDeriver {

    private final DmaManager dmaManager;

    @Autowired
    public StubbedGeoDeriver(DeriverManager deriverManager, DmaManager dmaManager) {
        super(deriverManager);
        this.dmaManager = dmaManager;
    }

    @Override
    protected String deriveSpanishProvinceFromIp(TargetingContext context) {
        return "estremadura";
    }

    @Override
    protected ChineseProvince deriveChineseProvinceFromIp(TargetingContext context) {
        return ChineseProvince.BJ;
    }

    @Override
    protected AustrianProvince deriveAustrianProvinceFromIp(TargetingContext context) {
        return AustrianProvince.WI;
    }

    @Override
    protected CountryDto deriveCountryFromIp(TargetingContext context) {
        return context.getDomainCache().getCountryByIsoCode("US");
    }

    @Override
    protected String derivePostalCodeFromIp(TargetingContext context) {
        return "40324";
    }

    @Override
    protected USState deriveUSStateFromIp(TargetingContext context) {
        return USState.KY;
    }

    @Override
    protected CanadianProvince deriveCanadianProvinceFromIp(TargetingContext context) {
        return null;
    }

    @Override
    protected TimeZone deriveTimeZoneFromIp(TargetingContext context) {
        return TimeZoneUtils.getTimeZoneNonBlocking("America/New_York");
    }

    @Override
    protected Coordinates deriveCoordinatesFromIp(TargetingContext context) {
        return new SimpleCoordinates(38.2344444444444, -84.4346944444444);
    }

    @Override
    protected Dma deriveDmaFromIp(TargetingContext context) {
        return dmaManager.getDmaByName("Lexington");
    }
}

package com.adfonic.data.cache.ecpm;

import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

import java.math.BigDecimal;

public class EcpmInputData {
    private final AdSpaceDto adSpace;
    private final CreativeDto creative;
    private final PlatformDto platform;
    private final long countryId;
    private BigDecimal bidFloorPrice;

    public EcpmInputData(AdSpaceDto adSpace, CreativeDto creative, PlatformDto platform, long countryId, double bidFloorPrice) {
        this.adSpace = adSpace;
        this.creative = creative;
        this.platform = platform;
        this.countryId = countryId;
        this.bidFloorPrice = BigDecimal.valueOf(bidFloorPrice);
    }

    public AdSpaceDto getAdSpace() {
        return adSpace;
    }

    public CreativeDto getCreative() {
        return creative;
    }

    public PlatformDto getPlatform() {
        return platform;
    }

    public long getCountryId() {
        return countryId;
    }

    public BigDecimal getBidFloorPrice() {
        return bidFloorPrice;
    }
}

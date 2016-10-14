package com.adfonic.adserver.controller.dbg.dto;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DbgAdSpaceDto {

    private AdSpaceDto adSpace;

    @JsonProperty("eligibleCreatives")
    private AdspaceWeightedCreative[] creatives;

    public DbgAdSpaceDto() {

    }

    public DbgAdSpaceDto(AdSpaceDto adSpace) {
        this.adSpace = adSpace;
    }

    public AdSpaceDto getAdSpace() {
        return adSpace;
    }

    public void setAdSpace(AdSpaceDto adSpace) {
        this.adSpace = adSpace;
    }

    public AdspaceWeightedCreative[] getCreatives() {
        return creatives;
    }

    public void setCreatives(AdspaceWeightedCreative[] eligibleCreatives) {
        this.creatives = eligibleCreatives;
    }

}

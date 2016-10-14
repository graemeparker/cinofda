package com.adfonic.adserver.controller.dbg.dto;

import java.util.Map;
import java.util.Set;

import com.adfonic.adserver.rtb.util.RtbStats.StatsEntry;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DbgCreativeDto {

    private CreativeDto creative;

    private FormatDto format;

    private ExtendedCreativeTypeDto extendedType;

    @JsonProperty("eligibleAdSpaceIds")
    private Set<Long> eligibleAdSpaceIds;

    @JsonProperty("rtbStats")
    private Map<Long, StatsEntry> rtbStats;

    public DbgCreativeDto() {

    }

    public DbgCreativeDto(CreativeDto creative) {
        this.creative = creative;
    }

    public CreativeDto getCreative() {
        return creative;
    }

    public void setCreative(CreativeDto creative) {
        this.creative = creative;
    }

    public FormatDto getFormat() {
        return format;
    }

    public void setFormat(FormatDto format) {
        this.format = format;
    }

    public ExtendedCreativeTypeDto getExtendedType() {
        return extendedType;
    }

    public void setExtendedType(ExtendedCreativeTypeDto extended) {
        this.extendedType = extended;
    }

    public Set<Long> getEligibleAdSpaceIds() {
        return eligibleAdSpaceIds;
    }

    public void setEligibleAdSpaceIds(Set<Long> eligibleAdSpaceSet) {
        this.eligibleAdSpaceIds = eligibleAdSpaceSet;
    }

    public void setRtbStats(Map<Long, StatsEntry> rtbStats) {
        this.rtbStats = rtbStats;
    }

}

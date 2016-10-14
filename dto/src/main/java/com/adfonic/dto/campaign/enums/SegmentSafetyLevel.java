package com.adfonic.dto.campaign.enums;

import com.adfonic.domain.Segment;

public enum SegmentSafetyLevel {

    OFF("page.campaign.segment.safetylevel.off.short.label", "page.campaign.segment.safetylevel.off.long.label", Segment.SegmentSafetyLevel.OFF), BRONZE(
            "page.campaign.segment.safetylevel.bronze.short.label", "page.campaign.segment.safetylevel.bronze.long.label", Segment.SegmentSafetyLevel.BRONZE), SILVER(
            "page.campaign.segment.safetylevel.silver.short.label", "page.campaign.segment.safetylevel.silver.long.label", Segment.SegmentSafetyLevel.SILVER), GOLD(
            "page.campaign.segment.safetylevel.gold.short.label", "page.campaign.segment.safetylevel.gold.long.label", Segment.SegmentSafetyLevel.GOLD);

    private String safetyLevelShortStr;
    private String safetyLevelLongStr;
    private Segment.SegmentSafetyLevel safetyLevel;

    private SegmentSafetyLevel(String safetyLevelShortStr, String safetyLevelLongStr, Segment.SegmentSafetyLevel safetyLevel) {
        this.safetyLevelShortStr = safetyLevelShortStr;
        this.safetyLevelLongStr = safetyLevelLongStr;
        this.safetyLevel = safetyLevel;
    }

    public String getSafetyLevelShortStr() {
        return safetyLevelShortStr;
    }

    public String getSafetyLevelLongStr() {
        return safetyLevelLongStr;
    }

    public Segment.SegmentSafetyLevel getSafetyLevel() {
        return safetyLevel;
    }
}

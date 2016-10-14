package com.adfonic.dto.audience.enums;

import com.adfonic.domain.CampaignAudience;

public enum AudienceRecencyType {

    NA("page.campaign.targeting.audience.recency.type.na.label", CampaignAudience.RecencyType.NONE),
    NONE("page.campaign.targeting.audience.recency.type.none.label", CampaignAudience.RecencyType.NONE),
    RANGE("page.campaign.targeting.audience.recency.type.range.label", CampaignAudience.RecencyType.RANGE),
    WINDOW("page.campaign.targeting.audience.recency.type.window.label", CampaignAudience.RecencyType.WINDOW);

    private String label;
    private CampaignAudience.RecencyType recencyType;

    private AudienceRecencyType(String label, CampaignAudience.RecencyType recencyType) {
        this.label = label;
        this.recencyType = recencyType;
    }

    public String getLabel() {
        return label;
    }

    public CampaignAudience.RecencyType getRecencyType() {
        return recencyType;
    }

    public static AudienceRecencyType fromString(String recencyTypeStr) {
        if (recencyTypeStr != null) {
            for (AudienceRecencyType v : AudienceRecencyType.values()) {
                if (recencyTypeStr.equalsIgnoreCase(v.label)) {
                    return v;
                }
            }
        }
        return null;
    }
}

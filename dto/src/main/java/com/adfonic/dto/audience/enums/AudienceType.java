package com.adfonic.dto.audience.enums;

import com.adfonic.domain.Audience;

public enum AudienceType {

	// Order matters here MAD-3318 (Alphabetize Audience Types)
    CAMPAIGN_EVENT("page.audience.source.label.campaignevent", Audience.AudienceType.CAMPAIGN_EVENT),
    DMP("page.audience.source.label.thirdpartysegment", Audience.AudienceType.DMP),
    DEVICE("page.audience.source.label.deviceid", Audience.AudienceType.DEVICE),
    LOCATION("page.audience.source.label.location", Audience.AudienceType.LOCATION);
	// SITE_APP("page.audience.source.label.siteappvisitors", Audience.AudienceType.SITE_APP),

    private String label;
    private Audience.AudienceType audienceType;

    private AudienceType(String label, Audience.AudienceType audienceType) {
        this.label = label;
        this.audienceType = audienceType;
    }

    public String getLabel() {
        return label;
    }

    public Audience.AudienceType getAudienceType() {
        return audienceType;
    }

    public static AudienceType fromString(String audienceTypeStr) {
        if (audienceTypeStr != null) {
            for (AudienceType v : AudienceType.values()) {
                if (audienceTypeStr.equalsIgnoreCase(v.label)) {
                    return v;
                }
            }
        }
        return null;
    }
}

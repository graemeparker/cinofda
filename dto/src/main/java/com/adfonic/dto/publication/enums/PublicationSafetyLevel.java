package com.adfonic.dto.publication.enums;

import com.adfonic.domain.Publication;

public enum PublicationSafetyLevel {

    UN_CATEGORISED("page.approval.publication.safety.level.uncategorised", Publication.PublicationSafetyLevel.UN_CATEGORISED),
    OTHER("page.approval.publication.safety.level.other", Publication.PublicationSafetyLevel.OTHER),
    TRUSTED("page.approval.publication.safety.level.trusted", Publication.PublicationSafetyLevel.TRUSTED),
    BRAND_SAFETY("page.approval.publication.safety.level.brandsafety", Publication.PublicationSafetyLevel.BRAND_SAFETY);

    private String label;
    private Publication.PublicationSafetyLevel safetyLevel;

    private PublicationSafetyLevel(String label, Publication.PublicationSafetyLevel safetyLevel) {
        this.label = label;
        this.safetyLevel = safetyLevel;
    }

    public String getLabel() {
        return label;
    }

    public Publication.PublicationSafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public static PublicationSafetyLevel fromString(String safetyLevelStr) {
        if (safetyLevelStr != null) {
            for (PublicationSafetyLevel v : PublicationSafetyLevel.values()) {
                if (safetyLevelStr.equalsIgnoreCase(v.label)) {
                    return v;
                }
            }
        }
        return null;
    }
}

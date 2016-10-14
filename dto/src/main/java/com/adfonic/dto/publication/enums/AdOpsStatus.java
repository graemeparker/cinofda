package com.adfonic.dto.publication.enums;

import com.adfonic.domain.Publication;

public enum AdOpsStatus {

    NONE("page.approval.publication.adops.status.none", null),
    HIGHER_APPROVAL_REQUIRED("page.approval.publication.adops.status.higherapproval", Publication.AdOpsStatus.HIGHER_APPROVAL_REQUIRED),
    MORE_INFO_REQUIRED("page.approval.publication.adops.status.moreinfo", Publication.AdOpsStatus.MORE_INFO_REQUIRED);

    private String label;
    private Publication.AdOpsStatus adOpsStatus;

    private AdOpsStatus(String label, Publication.AdOpsStatus adOpsStatus) {
        this.label = label;
        this.adOpsStatus = adOpsStatus;
    }

    public String getLabel() {
        return label;
    }

    public Publication.AdOpsStatus getStatus() {
        return adOpsStatus;
    }

    public static AdOpsStatus fromString(String adOpsStatusStr) {
        if (adOpsStatusStr != null) {
            for (AdOpsStatus v : AdOpsStatus.values()) {
                if (adOpsStatusStr.equalsIgnoreCase(v.label)) {
                    return v;
                }
            }
        }
        return null;
    }
}

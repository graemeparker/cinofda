package com.adfonic.dto.publication.enums;

import com.adfonic.domain.Publication;

public enum PublicationStatus {

    ALL("page.dashboard.labels.table.filter.status.options.all", null),
    ACTIVE("page.dashboard.labels.table.filter.status.options.active", Publication.Status.ACTIVE),
    NEW("page.dashboard.labels.table.filter.status.options.new", Publication.Status.NEW),
    NEW_REVIEW("page.dashboard.labels.table.filter.status.options.newreview", Publication.Status.NEW_REVIEW),
    PAUSED("page.dashboard.labels.table.filter.status.options.paused", Publication.Status.PAUSED),
    PENDING("page.dashboard.labels.table.filter.status.options.pending", Publication.Status.PENDING),
    STOPPED("page.dashboard.labels.table.filter.status.options.stopped", Publication.Status.STOPPED),
    REJECTED("page.dashboard.labels.table.filter.status.options.rejected", Publication.Status.REJECTED);

    private String publicationStatusStr;
    private Publication.Status status;

    private PublicationStatus(String publicationStatusStr, Publication.Status status) {
        this.publicationStatusStr = publicationStatusStr;
        this.status = status;
    }

    public String getPublicationStatusStr() {
        return publicationStatusStr;
    }

    public Publication.Status getStatus() {
        return status;
    }

    public static PublicationStatus fromString(String text) {
        if (text != null) {
            for (PublicationStatus b : PublicationStatus.values()) {
                if (text.equalsIgnoreCase(b.publicationStatusStr)) {
                    return b;
                }
            }
        }
        return null;
    }
}

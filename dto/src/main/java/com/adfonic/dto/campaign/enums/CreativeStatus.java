package com.adfonic.dto.campaign.enums;

import com.adfonic.domain.Creative;

public enum CreativeStatus {

    ACTIVE("page.campaign.creative.status.active", Creative.Status.ACTIVE), PENDING_PAUSED("page.campaign.creative.status.pendingpaused", Creative.Status.PENDING_PAUSED), REJECTED(
            "page.campaign.creative.status.rejected", Creative.Status.REJECTED), NEW("page.campaign.creative.status.new", Creative.Status.NEW),
    // NEW_PAUSED("page.campaign.creative.status.new",Creative.Status.NEW_PAUSED),
    PAUSED("page.campaign.creative.status.paused", Creative.Status.PAUSED), PENDING("page.campaign.creative.status.pending", Creative.Status.PENDING), STOPPED(
            "page.campaign.creative.status.stopped", Creative.Status.STOPPED);

    private String creativeStatusStr;
    private Creative.Status status;

    private CreativeStatus(String creativeStatusStr, Creative.Status status) {
        this.creativeStatusStr = creativeStatusStr;
        this.status = status;
    }

    public String getCreativeStatusStr() {
        return creativeStatusStr;
    }

    public Creative.Status getStatus() {
        return status;
    }

}

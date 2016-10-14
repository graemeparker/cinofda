package com.adfonic.webservices.util;

import com.adfonic.domain.Campaign;

public enum CampaignCommand {
    submit(Campaign.Status.PENDING), // TODO just mentioning target. Not to be directly transitioned
    pause(Campaign.Status.PAUSED),
    stop(Campaign.Status.STOPPED),
    start(Campaign.Status.ACTIVE);

    public Campaign.Status status;

    private CampaignCommand(Campaign.Status status) {
        this.status = status;
    }
}

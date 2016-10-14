package com.adfonic.webservices.service.impl;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.adfonic.domain.Campaign.Status;
import com.adfonic.webservices.annotations.BlockIfCampaignIn;
import com.adfonic.webservices.service.IRestrictor;

public class CampaignStatusRestrictor implements IRestrictor {

    private Status status;

    public CampaignStatusRestrictor(Status status) {
        this.status = status;
    }

    public boolean isRestricted(Field field) {
        BlockIfCampaignIn campaign = field.getAnnotation(BlockIfCampaignIn.class);
        return (campaign != null ? Arrays.asList(campaign.value()).contains(status) : false);
    }

    @Override
    public boolean equals(Object o) {
        try {
            return (status == ((CampaignStatusRestrictor) o).status);
        } catch (RuntimeException e) {
            return (false);
        }
    }

    @Override
    public int hashCode() {
        return (status.ordinal());
    }
}

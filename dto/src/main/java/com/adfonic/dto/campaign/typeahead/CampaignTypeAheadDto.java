package com.adfonic.dto.campaign.typeahead;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class CampaignTypeAheadDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "status")
    protected com.adfonic.domain.Campaign.Status status;

    public com.adfonic.domain.Campaign.Status getStatus() {
        return status;
    }

    public void setStatus(com.adfonic.domain.Campaign.Status status) {
        this.status = status;
    }

}

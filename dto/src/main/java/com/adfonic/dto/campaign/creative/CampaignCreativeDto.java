package com.adfonic.dto.campaign.creative;

import java.util.ArrayList;
import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;

public class CampaignCreativeDto extends NameIdBusinessDto {
    
    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value = "advertiser")
    private AdvertiserDto advertiser;

    @DTOCascade
    @Source(value = "creatives")
    private List<CreativeDto> creatives = new ArrayList<CreativeDto>(0);

    public List<CreativeDto> getCreatives() {
        return creatives;
    }

    public void setCreatives(List<CreativeDto> creatives) {
        this.creatives = creatives;
    }

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(AdvertiserDto advertiser) {
        this.advertiser = advertiser;
    }

}

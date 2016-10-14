package com.adfonic.dto.campaign.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;

/***
 * Object to get the campaigns from the database according to the parameters.
 * */
public class CampaignSearchDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Collection<CampaignTypeAheadDto> campaigns = new ArrayList<CampaignTypeAheadDto>(0);

    private AdvertiserDto advertiser;

    private String name;

    private String status;

    private Long id;

    public Collection<CampaignTypeAheadDto> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(Collection<CampaignTypeAheadDto> campaigns) {
        this.campaigns = campaigns;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(AdvertiserDto advertiser) {
        this.advertiser = advertiser;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CampaignSearchDto [campaigns=");
        builder.append(campaigns);
        builder.append(", advertiser=");
        builder.append(advertiser);
        builder.append(", name=");
        builder.append(name);
        builder.append(", status=");
        builder.append(status);
        builder.append(", id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }

}

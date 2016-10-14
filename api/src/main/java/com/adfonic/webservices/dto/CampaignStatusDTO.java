package com.adfonic.webservices.dto;

import javax.xml.bind.annotation.XmlRootElement;

import com.adfonic.domain.Campaign;
import com.adfonic.webservices.annotations.BlockIfCampaignIn;

@XmlRootElement(name = "campaign")
public class CampaignStatusDTO {

    @BlockIfCampaignIn
    private Campaign.Status status;

    private String name;
    
    public Campaign.Status getStatus() {
        return status;
    }

    public void setStatus(Campaign.Status status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
}

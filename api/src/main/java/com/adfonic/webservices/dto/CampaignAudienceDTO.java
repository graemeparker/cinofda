package com.adfonic.webservices.dto;

import javax.xml.bind.annotation.XmlAttribute;

public class CampaignAudienceDTO {

    private Boolean include;
    
    private String id;
    
    private Integer recency;

    @XmlAttribute
    public Boolean getInclude() {
        return include;
    }

    public void setInclude(Boolean include) {
        this.include = include;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRecency() {
        return recency;
    }

    public void setRecency(Integer recency) {
        this.recency = recency;
    }
    
}

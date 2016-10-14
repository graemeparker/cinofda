package com.adfonic.webservices.dto;

import javax.xml.bind.annotation.XmlRootElement;

import com.adfonic.domain.Creative;
import com.adfonic.webservices.annotations.BlockIfCreativeIn;

@XmlRootElement(name = "creative")
public class CreativeDTO {

    private String id;// externalID

    private String campaignID;// campaign.externalID

    @BlockIfCreativeIn
    private String name;

    @BlockIfCreativeIn
    private String format;// format.systemName

    @BlockIfCreativeIn
    private DestinationDTO destination;

    @BlockIfCreativeIn
    private Creative.Status status;
    
    private String englishTranslation;

    // creative categories is stuff done internally. should not be set/read by user - removing after confirmation
    //private Set<String> categories;

    @BlockIfCreativeIn
    private Long lastUpdated;

    @BlockIfCreativeIn
    private Long approvedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCampaignID() {
        return campaignID;
    }

    public void setCampaignID(String campaignID) {
        this.campaignID = campaignID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public DestinationDTO getDestination() {
        return destination;
    }

    public void setDestination(DestinationDTO destination) {
        this.destination = destination;
    }

    public String getEnglishTranslation() {
        return englishTranslation;
    }

    public void setEnglishTranslation(String englishTranslation) {
        this.englishTranslation = englishTranslation;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Long approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Creative.Status getStatus() {
        return status;
    }

    public void setStatus(Creative.Status status) {
        this.status = status;
    }

}

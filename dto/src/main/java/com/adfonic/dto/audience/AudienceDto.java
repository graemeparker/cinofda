package com.adfonic.dto.audience;

import java.util.Date;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.domain.Audience.Status;
import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.audience.enums.AudienceType;

public class AudienceDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source("advertiser")
    private AdvertiserDto advertiser;

    @Source("creationTime")
    private Date creationTime;

    @Source("externalID")
    private String externalId;

    @DTOCascade
    @Source("dmpAudience")
    private DMPAudienceDto dmpAudience;

    @DTOCascade
    @Source("firstPartyAudience")
    private FirstPartyAudienceDto firstPartyAudience;

    @Source("status")
    private Status status;

    private AudienceType audienceType;

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = (creationTime == null? null : new Date(creationTime.getTime()));
    }

    public DMPAudienceDto getDmpAudience() {
        return dmpAudience;
    }

    public void setDmpAudience(DMPAudienceDto dmpAudience) {
        this.dmpAudience = dmpAudience;
    }

    public FirstPartyAudienceDto getFirstPartyAudience() {
        return firstPartyAudience;
    }

    public void setFirstPartyAudience(FirstPartyAudienceDto firstPartyAudience) {
        this.firstPartyAudience = firstPartyAudience;
    }

    public AdvertiserDto getAdvertiser() {
        return advertiser;
    }

    public void setAdvertiser(AdvertiserDto advertiser) {
        this.advertiser = advertiser;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public AudienceType getAudienceType() {
        return audienceType;
    }

    public void setAudienceType(AudienceType audienceType) {
        this.audienceType = audienceType;
    }

    public AudienceType resolveAudienceType(AudienceDto dto) {
        if (dto != null) {
            if (dto.getDmpAudience() != null) {
                return AudienceType.DMP;
            } else if (dto.getFirstPartyAudience() != null && dto.getFirstPartyAudience().getType() != null) {
                switch (dto.getFirstPartyAudience().getType()) {
                case UPLOAD:
                    return AudienceType.DEVICE;
                case LOCATION:
                    return AudienceType.LOCATION;
                case COLLECT:
                    // return AudienceType.SITE_APP;
                case CLICK:
                case INSTALL:
                case CONVERSION:
                default:
                    return AudienceType.CAMPAIGN_EVENT;
                }
            }
            return null;
        } else {
            return null;
        }
    }
}

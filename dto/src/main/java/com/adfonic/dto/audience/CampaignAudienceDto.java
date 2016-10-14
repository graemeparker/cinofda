package com.adfonic.dto.audience;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;
import com.adfonic.dto.audience.enums.AudienceRecencyType;

public class CampaignAudienceDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    // Audience is not cascaded, the service will take care of initialising it
    // properly
    @DTOTransient
    private AudienceDto audience;

    @Source("include")
    private boolean include;

    @Source("recencyDateFrom")
    private Date recencyDateFrom;

    @Source("recencyDateTo")
    private Date recencyDateTo;

    @Source("recencyDaysFrom")
    private Integer recencyDaysFrom;

    @Source("recencyDaysTo")
    private Integer recencyDaysTo;

    private AudienceRecencyType audienceRecencyType = AudienceRecencyType.NONE;
    
    private BigDecimal audienceSize = BigDecimal.ZERO;

    public AudienceDto getAudience() {
        return audience;
    }

    public void setAudience(AudienceDto audience) {
        this.audience = audience;
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

    public Date getRecencyDateFrom() {
        return recencyDateFrom;
    }

    public void setRecencyDateFrom(Date recencyDateFrom) {
        this.recencyDateFrom = (recencyDateFrom == null) ? null : new Date(recencyDateFrom.getTime());
    }

    public Date getRecencyDateTo() {
        return recencyDateTo;
    }

    public void setRecencyDateTo(Date recencyDateTo) {
        this.recencyDateTo = (recencyDateTo == null) ? null : new Date(recencyDateTo.getTime());
    }

    public Integer getRecencyDaysFrom() {
        return recencyDaysFrom;
    }

    public void setRecencyDaysFrom(Integer recencyDaysFrom) {
        this.recencyDaysFrom = recencyDaysFrom;
    }

    public Integer getRecencyDaysTo() {
        return recencyDaysTo;
    }

    public void setRecencyDaysTo(Integer recencyDaysTo) {
        this.recencyDaysTo = recencyDaysTo;
    }

    public AudienceRecencyType getAudienceRecencyType() {
        return audienceRecencyType;
    }

    public void setAudienceRecencyType(AudienceRecencyType audienceRecencyType) {
        this.audienceRecencyType = audienceRecencyType;
    }
    
    public BigDecimal getAudienceSize() {
        return audienceSize;
    }

    public void setAudienceSize(BigDecimal audienceSize) {
        this.audienceSize = audienceSize;
    }

}

package com.adfonic.domain.cache.dto.adserver.creative;

import java.util.List;

import org.joda.time.Interval;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class CampaignAudienceDto extends BusinessKeyDto {

    private static final long serialVersionUID = 4L;

    public static enum AudienceType {
        DEVICE_ID, LOCATION, //classic 
        ADSQUARE, // lat & lon + devices 
        FACTUAL, // lat & lon + devices
        ADSQUARE_V2;

        public static final int ADSQUARE_DMP_VENDOR_ID = 3;
        public static final int FACTUAL_DMP_VENDOR_ID = 5;
        public static final int ADSQUARE_V2_DMP_VENDOR_ID = 6;
    }

    private long audienceId;
    private boolean include;
    private Integer numDaysAgoFrom;
    private Integer numDaysAgoTo;
    public Interval recencyInterval;
    private AudienceType type;
    private List<DmpAttributeDto> dmpAttributes;

    public CampaignAudienceDto(long id, long audienceId, AudienceType type, boolean include, List<DmpAttributeDto> attributes, Integer numDaysAgoFrom, Integer numDaysAgoTo,
            Interval recencyInterval) {
        super.setId(id);
        this.audienceId = audienceId;
        this.type = type;
        this.include = include;
        this.dmpAttributes = attributes;
        this.numDaysAgoFrom = numDaysAgoFrom;
        this.numDaysAgoTo = numDaysAgoTo;
        this.recencyInterval = recencyInterval;

    }

    protected CampaignAudienceDto() {
        //marshalling friendly constructor
    }

    public long getAudienceId() {
        return audienceId;
    }

    public boolean isInclude() {
        return include;
    }

    public Integer getNumDaysAgoFrom() {
        return numDaysAgoFrom;
    }

    public Integer getNumDaysAgoTo() {
        return numDaysAgoTo;
    }

    public Interval getRecencyInterval() {
        return recencyInterval;
    }

    public AudienceType getType() {
        return type;
    }

    public List<DmpAttributeDto> getDmpAttributes() {
        return dmpAttributes;
    }

    @Override
    public String toString() {
        return "CampaignAudienceDto {" + getId() + ", audienceId=" + audienceId + ", type=" + type + ", include=" + include + ", numDaysAgoFrom=" + numDaysAgoFrom
                + ", numDaysAgoTo=" + numDaysAgoTo + ", recencyInterval=" + recencyInterval + ", dmpAttributes=" + dmpAttributes + "}";
    }

}

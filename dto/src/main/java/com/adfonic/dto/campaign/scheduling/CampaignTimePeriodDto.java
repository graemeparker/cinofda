package com.adfonic.dto.campaign.scheduling;

import java.util.Date;

import com.adfonic.dto.BusinessKeyDTO;
import com.adfonic.dto.campaign.CampaignDto;

public class CampaignTimePeriodDto extends BusinessKeyDTO implements Comparable<CampaignTimePeriodDto> {

    private static final long serialVersionUID = 1L;
    
    private static final int DEFAULT_END_TIME_OFFSET = 1439;

    private CampaignDto campaign;
    private Date startDate;
    private Date endDate;
    private Integer startTimeOffset = 0;
    // default value to 23:59
    private Integer endTimeOffset = DEFAULT_END_TIME_OFFSET;

    public CampaignDto getCampaign() {
        return campaign;
    }

    public void setCampaign(CampaignDto campaign) {
        this.campaign = campaign;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = (startDate == null ? null : new Date(startDate.getTime()));
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = (endDate == null ? null : new Date(endDate.getTime()));
    }

    public Integer getStartTimeOffset() {
        return startTimeOffset;
    }

    public void setStartTimeOffset(Integer startTimeOffset) {
        this.startTimeOffset = startTimeOffset;
    }

    public Integer getEndTimeOffset() {
        return endTimeOffset;
    }

    public void setEndTimeOffset(Integer endTimeOffset) {
        this.endTimeOffset = endTimeOffset;
    }

    @Override
    public int compareTo(CampaignTimePeriodDto compareObject) {
        if (this.startDate == null || compareObject.getEndDate() == null) {
            return -1;
        }
        if (this.endDate == null || compareObject.getStartDate() == null) {
            return 1;
        }
        if (this.startDate.after(compareObject.getEndDate())) {
            return 1;
        } else if (this.endDate.before(compareObject.getStartDate())) {
            return -1;
        } else {
            return 0;
        }
    }

}

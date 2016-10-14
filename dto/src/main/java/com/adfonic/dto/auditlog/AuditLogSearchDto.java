package com.adfonic.dto.auditlog;

import com.adfonic.dto.AbstractSearchDto;
import com.adfonic.dto.campaign.CampaignDto;

public class AuditLogSearchDto extends AbstractSearchDto{
    private static final Integer DEFAULT_MONTHS_TO_SHOW = 6;

    private CampaignDto campaignDto;
    private Integer monthsToShow = DEFAULT_MONTHS_TO_SHOW;

    public AuditLogSearchDto() {
        super();
    }

    public AuditLogSearchDto(CampaignDto campaignDto, Integer monthsToShow, Integer first, Integer pageSize, String sortField, Boolean ascending) {
        super(first, pageSize, sortField, ascending);
        this.campaignDto = campaignDto;
        if (monthsToShow != null) {
            this.monthsToShow = monthsToShow;
        }
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public Integer getMonthsToShow() {
        return monthsToShow;
    }

    public void setMonthsToShow(Integer monthsToShow) {
        this.monthsToShow = monthsToShow;
    }
}

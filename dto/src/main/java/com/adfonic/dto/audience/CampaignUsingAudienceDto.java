package com.adfonic.dto.audience;

import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class CampaignUsingAudienceDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source("campaignAudiences")
    private List<CampaignAudienceDto> campaignAudiences;

    public List<CampaignAudienceDto> getCampaignAudiences() {
        return campaignAudiences;
    }

    public void setCampaignAudiences(List<CampaignAudienceDto> campaignAudiences) {
        this.campaignAudiences = campaignAudiences;
    }

}

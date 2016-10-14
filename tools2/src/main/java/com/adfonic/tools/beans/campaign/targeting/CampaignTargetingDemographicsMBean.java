package com.adfonic.tools.beans.campaign.targeting;

import java.io.Serializable;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.segment.SegmentDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignTargetingDemographicsMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTargetingDemographicsMBean.class);

    private CampaignDto campaignDto;

    private int genderMix = 50;

    private int minAge;

    private int maxAge;

    @Override
    public void init() {

    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        BigDecimal bd = new BigDecimal(genderMix).divide(new BigDecimal(100));
        dto.getCurrentSegment().setGenderMix(bd);
        dto.getCurrentSegment().setMinAge(this.minAge);
        dto.getCurrentSegment().setMaxAge(this.maxAge);
        LOGGER.debug("prepareDto<--");
        return dto;
    }

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = dto;
        if (campaignDto != null) {
            if (campaignDto.getCurrentSegment().getGenderMix() != null) {
                this.genderMix = campaignDto.getCurrentSegment().getGenderMix().multiply(new BigDecimal(100)).intValue();
            }
            this.minAge = campaignDto.getCurrentSegment().getMinAge();
            this.maxAge = campaignDto.getCurrentSegment().getMaxAge();
        }
        LOGGER.debug("loadCampaignDto<--");
    }

    public String getDemographicsSummary(boolean spaces) {
        String space = "";
        if (spaces) {
            space = " ";
        }
        if (campaignDto != null && campaignDto.getCurrentSegment() != null) {
            SegmentDto segment = campaignDto.getCurrentSegment();
            if (segment.getMinAge() == 0 && segment.getMaxAge() == 75 && segment.getGenderMix().intValue() == 50) {
                return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
            } else {
                String message = segment.getGenderMix().multiply(new BigDecimal(100)).intValue() + "% male "
                        + (100 - segment.getGenderMix().multiply(new BigDecimal(100)).intValue()) + "% female;" + space + "aged between "
                        + segment.getMinAge() + " and " + segment.getMaxAge();
                return message;
            }
        }
        return notSet();
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public int getGenderMix() {
        return genderMix;
    }

    public void setGenderMix(int genderMix) {
        this.genderMix = genderMix;
    }

    public int getGenderMixOpposite() {
        return 100 - genderMix;
    }

    public void setGenderMixOpposite(int genderMixOpposite) {
        if (100 - genderMix != genderMixOpposite) {
            LOGGER.warn("Gender mix in UI doesn't fit");
        }
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }
}

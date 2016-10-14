package com.adfonic.tools.beans.campaign.targeting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Medium;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignTargetingAppSiteMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTargetingAppSiteMBean.class);

    private CampaignDto campaignDto;

    private List<String> mediumTargeting = new ArrayList<String>();

    @Override
    public void init() {

    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        if (CollectionUtils.isEmpty(mediumTargeting)) {
            campaignDto.getCurrentSegment().setMedium(null);
        } else if (mediumTargeting.size() == 2) {
            campaignDto.getCurrentSegment().setMedium(null);
        } else if (mediumTargeting.contains("APPLICATION")) {
            campaignDto.getCurrentSegment().setMedium(Medium.APPLICATION);
        } else if (mediumTargeting.contains("SITE")) {
            campaignDto.getCurrentSegment().setMedium(Medium.SITE);
        }
        LOGGER.debug("prepareDto<--");
        return dto;
    }

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = dto;

        if (campaignDto != null) {
            if (campaignDto.getCurrentSegment().getMedium() == null) {
                // The list is empty, leave this comment to understand all the
                // posibilities
                LOGGER.debug("no medium selected");
                if (mediumTargeting.size() != 2) {
                    mediumTargeting.clear();
                }
            } else if (campaignDto.getCurrentSegment().getMedium().equals(Medium.SITE)) {
                LOGGER.debug("Sites selected");
                mediumTargeting = new ArrayList<String>();
                mediumTargeting.add("SITE");
            } else if (campaignDto.getCurrentSegment().getMedium().equals(Medium.APPLICATION)) {
                LOGGER.debug("Applications selected");
                mediumTargeting = new ArrayList<String>();
                mediumTargeting.add("APPLICATION");
            } else {
                LOGGER.warn("Not recognized medium selected");
            }
        }
        LOGGER.debug("loadCampaignDto<--");
    }

    public String getAppsSummary() {
        if (campaignDto != null && campaignDto.getCurrentSegment() != null) {
            if (campaignDto.getCurrentSegment().getMedium() == null) {
                return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
            } else if (campaignDto.getCurrentSegment().getMedium().equals(Medium.UNKNOWN)) {
                return notSet();
            } else if (campaignDto.getCurrentSegment().getMedium().equals(Medium.SITE)) {
                return FacesUtils.getBundleMessage("page.campaign.targeting.web.label");
            } else if (campaignDto.getCurrentSegment().getMedium().equals(Medium.APPLICATION)) {
                return FacesUtils.getBundleMessage("page.campaign.targeting.app.label");
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

    public List<String> getMediumTargeting() {
        return mediumTargeting;
    }

    public void setMediumTargeting(List<String> mediumTargeting) {
        this.mediumTargeting = mediumTargeting;
    }

}
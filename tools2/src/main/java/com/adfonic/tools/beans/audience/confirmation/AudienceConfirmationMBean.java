package com.adfonic.tools.beans.audience.confirmation;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Audience;
import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.audience.FirstPartyAudienceDto;
import com.adfonic.dto.audience.enums.AudienceType;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class AudienceConfirmationMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(AudienceConfirmationMBean.class);
    private static final String LAUNCH_LABEL_KEY = "page.audience.confirmation.button.launch.label";
    private static final String DONE_LABEL_KEY = "page.audience.confirmation.button.done.label";

    private static final String CAMPAIGN_EVENT_CONFIRMATION_SECTION = "/WEB-INF/jsf/audience/section_confirmation_source_campaign_event.xhtml";
    private static final String DMP_CONFIRMATION_SECTION = "/WEB-INF/jsf/audience/section_confirmation_source.xhtml";
    // private static final String SITE_APP_CONFIRMATION_SECTION =
    // "/WEB-INF/jsf/audience/section_confirmation_source_site_app.xhtml";
    private static final String DEVICE_CONFIRMATION_SECTION = "/WEB-INF/jsf/audience/section_confirmation_source_device.xhtml";
    private static final String LOCATION_CONFIRMATION_SECTION = "/WEB-INF/jsf/audience/section_confirmation_source_location.xhtml";

    @Autowired
    private AudienceService audienceService;

    private AudienceDto audienceDto;

    @Override
    protected void init() throws Exception {
        // noop
    }

    public String launch() {
        LOGGER.debug("launch-->");
        if (isNewAudience()) {
            // for first party set audience collection to active
            if (audienceDto.getFirstPartyAudience() != null) {
                FirstPartyAudienceDto f = audienceDto.getFirstPartyAudience();
                f.setActive(true);
                audienceDto.setFirstPartyAudience(f);
            }

            audienceDto.setStatus(Audience.Status.ACTIVE);
            audienceDto = audienceService.updateAudience(audienceDto);
            audienceDto = audienceService.getAudienceDtoById(audienceDto.getId());

        }
        getAudienceMBean().initAudienceWorkflow();
        updateAudienceBeans(null);
        LOGGER.debug("<--launch");
        return "pretty:audience-builder";
    }

    public boolean isNewAudience() {
        return audienceDto.getStatus().equals(Audience.Status.NEW_REVIEW);
    }

    public String getContinueButtonMessage() {
        if (isNewAudience()) {
            return FacesUtils.getBundleMessage(LAUNCH_LABEL_KEY);
        } else {
            return FacesUtils.getBundleMessage(DONE_LABEL_KEY);
        }
    }

    // for conditional rendering of the proper include in confirmation
    public String getConfirmationSection(AudienceType audienceType) {
        if (audienceType == null) {
            audienceType = AudienceType.DMP;
        }

        switch (audienceType) {
        case CAMPAIGN_EVENT:
            return CAMPAIGN_EVENT_CONFIRMATION_SECTION;
        case DEVICE:
            return DEVICE_CONFIRMATION_SECTION;
        case LOCATION:
            return LOCATION_CONFIRMATION_SECTION;
            // case SITE_APP:
            // return SITE_APP_CONFIRMATION_SECTION;
        case DMP:
        default:
            return DMP_CONFIRMATION_SECTION;
        }
    }

    public AudienceDto getAudienceDto() {
        return audienceDto;
    }

    public void setAudienceDto(AudienceDto audienceDto) {
        this.audienceDto = audienceDto;
    }
}

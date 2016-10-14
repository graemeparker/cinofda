package com.adfonic.tools.beans.audience.setup;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Role;
import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.audience.enums.AudienceType;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.security.SecurityUtils;

@Component
@Scope("view")
public class AudienceSetupMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(AudienceSetupMBean.class);

    @Autowired
    private AudienceService audienceService;

    private AudienceDto audienceDto;
    private String name;
    public AudienceType type;

    @Override
    protected void init() throws Exception {
        // noop
    }

    public String doSave() throws Exception {
        LOGGER.debug("doSave-->");
        if (!isDuplicateAudienceName()) {

            boolean newAudience = (audienceDto == null || audienceDto.getId() == null);
            boolean nameChanged = false;
            if (!newAudience && !audienceDto.getName().equals(name)) {
                nameChanged = true;
                newAudience = true;
            }

            audienceDto = prepareDto(audienceDto);

            if (audienceDto.getId() != null && audienceDto.getId().longValue() > 0) {
                audienceDto = audienceService.updateAudience(audienceDto);
            } else {
                audienceDto = audienceService.createAudience(audienceDto);
            }
            audienceDto = audienceService.getAudienceDtoById(audienceDto.getId());
            updateAudienceBeans(audienceDto);

            if (getAudienceNavigationBean().isSourceDisabled()) {
                getAudienceNavigationBean().setSourceDisabled(false);
            }

            if (newAudience) {
                getAudienceNavigationBean().setEncodedId(URLEncoder.encode(audienceDto.getExternalId(), "UTF-8"));
                if (!nameChanged) {
                    getAudienceNavigationBean().setFromSetup(true);
                }
                return "pretty:audienceSetup";
            }

            if (getAudienceMBean().isNewAudience()) {
                getAudienceNavigationBean().updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_SOURCE);
                getAudienceNavigationBean().setNavigate(Constants.AUDIENCE_SOURCE_VIEW);
            } else {
                getAudienceNavigationBean().updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_CONFIRMATION);
                getAudienceNavigationBean().setNavigate(Constants.AUDIENCE_CONFIRMATION_VIEW);
            }
        } else {
            LOGGER.debug("Duplicated name");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "audience-name", null, "error.audience.setup.duplicate.audience.name");
        }

        LOGGER.debug("<--doSave");
        return null;
    }

    public void cancel(ActionEvent event) {
        LOGGER.debug("cancel-->");
        this.name = audienceDto.getName();
        this.type = audienceDto.resolveAudienceType(audienceDto);
        getAudienceNavigationBean().updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_CONFIRMATION);
        getAudienceNavigationBean().setNavigate(Constants.AUDIENCE_CONFIRMATION_VIEW);
        LOGGER.debug("cancel<--");
    }

    public void loadAudienceDto(AudienceDto dto) {
        LOGGER.debug("loadAudienceDto-->");
        this.audienceDto = dto;
        if (dto != null) {
            LOGGER.debug("dto is not null");
            this.name = dto.getName();
            setType(audienceDto.resolveAudienceType(audienceDto));
            if (this.type == null) {
                setType(getAudienceNavigationBean().getType());
            }
            if (this.type == null) {
                setType(AudienceType.DMP);
            }
        } else {
            this.name = null;
            setType(AudienceType.DMP);
        }
        getAudienceNavigationBean().setType(this.type);
        LOGGER.debug("<--loadAudienceDto");
    }

    public AudienceDto prepareDto(AudienceDto dto) {
        LOGGER.debug("prepareDto-->");
        if (dto == null) {
            dto = new AudienceDto();
        }

        dto.setAdvertiser(getUser().getAdvertiserDto());
        dto.setName(name);
        getAudienceNavigationBean().setType(this.type);
        LOGGER.debug("<--prepareDto");
        return dto;
    }

    public String getAudienceType() {
        if (this.type == null) {
            setType(AudienceType.DMP);
        }
        return FacesUtils.getBundleMessage(this.type.getLabel());
    }

    private boolean isDuplicateAudienceName() {
        if (StringUtils.isNotBlank(name)) {
            AudienceDto search = audienceService.getAudienceByNameForAdvertiser(name, getUser().getAdvertiserDto().getId());
            if (search == null || search.getId() == null) {
                return false;
            } else if (audienceDto == null || audienceDto.getId() == null || audienceDto.getId().longValue() != search.getId().longValue()) {
                // don't say it's a dupe if it's "this" one
                return true;
            }
        }
        return false;
    }
    
    public List<AudienceType> getAvailableTypes() {
        return Arrays.asList(AudienceType.values());
    }

    public AudienceDto getAudienceDto() {
        return audienceDto;
    }

    public void setAudienceDto(AudienceDto audienceDto) {
        this.audienceDto = audienceDto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AudienceType getType() {
        return this.type;
    }

    public void setType(AudienceType type) {
        this.type = type;
    }
}

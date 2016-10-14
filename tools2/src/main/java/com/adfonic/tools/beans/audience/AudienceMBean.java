package com.adfonic.tools.beans.audience;

import java.io.Serializable;
import java.net.URLDecoder;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Audience;
import com.adfonic.domain.FirstPartyAudience;
import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.tools.beans.audience.confirmation.AudienceConfirmationMBean;
import com.adfonic.tools.beans.audience.setup.AudienceSetupMBean;
import com.adfonic.tools.beans.audience.source.AudienceSourceMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = {
        @URLMapping(id = "newAudience", pattern = "/audience", viewId = "/WEB-INF/jsf/audience/audience.jsf"),
        @URLMapping(id = "audienceSetup", pattern = "/audience/#{id : audienceNavigationSessionBean.encodedId}", viewId = "/WEB-INF/jsf/audience/audience.jsf") })
public class AudienceMBean extends GenericAbstractBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AudienceMBean.class);

    private AudienceDto audienceDto;

    @Autowired
    private AudienceService service;

    @Autowired
    private AudienceSetupMBean audienceSetupMBean;

    @Autowired
    private AudienceSourceMBean audienceSourceMBean;

    @Autowired
    private AudienceConfirmationMBean audienceConfirmationMBean;

    @Override
    @URLActions(actions = { @URLAction(mappingId = "audienceSetup") })
    public void init() throws Exception {
        LOGGER.debug("init-->");

        if (StringUtils.isEmpty(getAudienceNavigationBean().getEncodedId())) {
            initAudienceWorkflow();
        } else if (audienceDto == null) {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("progressDialog.hide()");
            UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
            String id = URLDecoder.decode(getAudienceNavigationBean().getEncodedId(), "UTF-8");
            LOGGER.debug(id);
            try {
                audienceDto = service.getAudienceByExternalId(id);
            } catch (Exception e) {
                audienceDto = null;
            }

            if (audienceDto == null || audienceDto.getId() == null
                    || !userDto.getAdvertiserDto().getId().equals(audienceDto.getAdvertiser().getId())) {
                LOGGER.debug("Non-existent or not allowed to load audience");
                initAudienceWorkflow();
                audienceDto = null;
                throw new Exception();
            }

            LOGGER.debug("Navigating to audience id: " + audienceDto.getId());
            if (getAudienceNavigationBean().isFromSetup()) {
                LOGGER.debug("Navigate from setup, audience recently created");
                getAudienceNavigationBean().setFromSetup(false);
                getAudienceNavigationBean().updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_SOURCE);
                getAudienceNavigationBean().setNavigate(Constants.AUDIENCE_SOURCE_VIEW);
            } else {
                LOGGER.debug("navigate to existing");
                getAudienceNavigationBean().setType(null);
                loadAudience(audienceDto, true);
            }
            updateAudienceBeans(audienceDto);
        }

        getNavigationSessionBean().navigate(Constants.AUDIENCE_BUILDER);

        LOGGER.debug("init<--");
    }

    public void loadAudience(AudienceDto audience, boolean navigateToConfirmation) {
        LOGGER.debug("loadAudience-->");
        updateAudienceBeans(audience);
        if (audience.getStatus().equals(Audience.Status.NEW)) {
            getAudienceNavigationBean().restartMenu();
            getAudienceNavigationBean().updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_SETUP);
            getAudienceNavigationBean().setNavigate(Constants.AUDIENCE_SETUP_VIEW);
        } else {
            getAudienceNavigationBean().openAllMenu();
            if (navigateToConfirmation) {
                getAudienceNavigationBean().updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_CONFIRMATION);
                getAudienceNavigationBean().setNavigate(Constants.AUDIENCE_CONFIRMATION_VIEW);
            }
        }
        LOGGER.debug("<--loadAudience");
    }

    @URLActions(actions = { @URLAction(mappingId = "newAudience") })
    public void initAudienceWorkflow() {
        LOGGER.debug("initAudienceWorkflow-->");

        getAudienceNavigationBean().setEncodedId(null);
        getAudienceNavigationBean().updateMenuStyles(Constants.AUDIENCE_MENU_NAVIGATE_TO_SETUP);
        getAudienceNavigationBean().setNavigate(Constants.AUDIENCE_SETUP_VIEW);
        getAudienceNavigationBean().restartMenu();
        getAudienceNavigationBean().initNavigation();

        // style tabs in header
        getNavigationSessionBean().navigate(Constants.ADD_AUDIENCE);

        audienceDto = null;
        LOGGER.debug("<--initAudienceWorkflow");
    }

    public AudienceDto getAudienceDto() {
        // Set initial values
        if (audienceDto == null) {
            audienceDto = new AudienceDto();
            // set any defaults
        }
        return audienceDto;
    }

    public void setAudienceDto(AudienceDto audienceDto) {
        this.audienceDto = audienceDto;
    }

    public String doInitAudience() {
        LOGGER.debug("doInitAudience-->");
        // reset everything
        initAudienceWorkflow();
        LOGGER.debug("<--doInitAudience");
        return "pretty:newAudience";
    }

    public String getContinueButtonMessage() {
        if (isNewAudience()) {
            return FacesUtils.getBundleMessage("page.audience.setup.button.save.label");
        } else {
            return FacesUtils.getBundleMessage("page.audience.button.save.confirm.label");
        }
    }

    public String getContinueSourceButtonMessage() {
        if (isDeviceIdOrLocationAudience()) {
            return FacesUtils.getBundleMessage("page.audience.spurce.button.continue.label");
        } else if (isNewAudience()) {
            return FacesUtils.getBundleMessage("page.audience.setup.button.save.label");
        } else {
            return FacesUtils.getBundleMessage("page.audience.button.save.confirm.label");
        }
    }

    public boolean isDefinedAudience() {
        return audienceDto != null && (audienceDto.getDmpAudience() != null || (audienceDto.getFirstPartyAudience() != null));
    }

    public boolean isNewAudience() {
        return audienceDto == null || audienceDto.getStatus() == null || audienceDto.getStatus().equals(Audience.Status.NEW)
                || audienceDto.getStatus().equals(Audience.Status.NEW_REVIEW);
    }

    public boolean isDeviceIdOrLocationAudience() {
        return audienceDto != null
                && audienceDto.getFirstPartyAudience() != null
                && (audienceDto.getFirstPartyAudience().getType().equals(FirstPartyAudience.Type.UPLOAD) || audienceDto.getFirstPartyAudience().getType()
                        .equals(FirstPartyAudience.Type.LOCATION));
    }

    public AudienceSetupMBean getAudienceSetupMBean() {
        return audienceSetupMBean;
    }

    public void setAudienceSetupMBean(AudienceSetupMBean audienceSetupMBean) {
        this.audienceSetupMBean = audienceSetupMBean;
    }

    public AudienceSourceMBean getAudienceSourceMBean() {
        return audienceSourceMBean;
    }

    public void setAudienceSourceMBean(AudienceSourceMBean audienceSourceMBean) {
        this.audienceSourceMBean = audienceSourceMBean;
    }

    public AudienceConfirmationMBean getAudienceConfirmationMBean() {
        return audienceConfirmationMBean;
    }

    public void setAudienceConfirmationMBean(AudienceConfirmationMBean audienceConfirmationMBean) {
        this.audienceConfirmationMBean = audienceConfirmationMBean;
    }

}

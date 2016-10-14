package com.adfonic.tools.beans.publication;

import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URLDecoder;

import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.tools.beans.publication.app.AppAddSlotsMBean;
import com.adfonic.tools.beans.publication.app.AppSettingsMBean;
import com.adfonic.tools.beans.publication.app.SiteSettingsMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = {
        @URLMapping(id = "newPublication", pattern = "/publication", viewId = "/WEB-INF/jsf/addpublication/addappsite.jsf"),
        @URLMapping(id = "publicationAdd", pattern = "/publication/#{id : publicationNavigationSessionBean.encodedId}", viewId = "/WEB-INF/jsf/addpublication/addappsite.jsf") })
public class PublicationMBean extends GenericAbstractBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 4188436945608530254L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PublicationMBean.class);

    private PublicationDto publicationDto;

    @Autowired
    private PublicationService service;
    @Autowired
    private AppSettingsMBean appSettingsMBean;

    @Autowired
    private AppAddSlotsMBean appAddSlotsMBean;

    @Autowired
    private SiteSettingsMBean siteSettingsMBean;

    @Override
    @URLActions(actions = { @URLAction(mappingId = "publicationAdd"), @URLAction(mappingId = "newPublication") })
    public void init() throws Exception {
        LOGGER.debug("init-->");
        if (!StringUtils.isEmpty(getPublicationNavigationBean().getEncodedId())) {
            getNavigationSessionBean().navigate(Constants.ADD_CAMPAIGN);
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("progressDialog.hide()");
            String id = URLDecoder.decode(getPublicationNavigationBean().getEncodedId(), "UTF-8");
            LOGGER.debug(id);
            PublicationDto pub;
            try {
                pub = service.getPublicationByExternalId(id);
            } catch (Exception e) {
                pub = null;
            }
            if (pub != null && (publicationDto == null || !pub.getId().equals(publicationDto.getId()))) {
                publicationDto = pub;
                getPublicationNavigationBean().navigateTo(Constants.MENU_NAVIGATE_TO_APP_ADSLOT);
                getPublicationNavigationBean().setNewPub(false);
                getPublicationNavigationBean().setPubCreated(true);
                if (publicationDto.getPublicationType().equals(service.getPublicationTypeBySystemName(Constants.MOBILE_SITE))) {
                    getPublicationNavigationBean().setMedium(Constants.MEDIUM_SITE);
                } else {
                    getPublicationNavigationBean().setMedium(Constants.MEDIUM_APPLICATION);
                }
            }
            updatePublicationBeans(publicationDto);
        } else if (publicationDto == null) {
            initPublicationWorkflow();
        }
        getNavigationSessionBean().navigate(Constants.ADD_PUBLICATION);
        LOGGER.debug("init<--");
    }

    public void initPublicationWorkflow() throws Exception {
        getNavigationSessionBean().navigate(Constants.ADD_PUBLICATION);
        publicationDto = null;
        getPublicationNavigationBean().setEncodedId("");
        getPublicationNavigationBean().setNewPub(true);
        getPublicationNavigationBean().setPubCreated(false);
        getPublicationNavigationBean().setNavigate("/WEB-INF/jsf/addpublication/section_new.xhtml");
        updatePublicationBeans(getPublicationDto());
        getAppSettingsMBean().setShowAdvanced(false);
        getSiteSettingsMBean().setShowAdvanced(false);
    }

    public PublicationDto getPublicationDto() {
        // Set initial values
        if (publicationDto == null) {
            publicationDto = new PublicationDto();
            publicationDto.setGenderMix(BigDecimal.valueOf(0.5));
            publicationDto.setAutoApproval(true);
            publicationDto.setBackfillEnabled(true);
            publicationDto.setMinAge(0);
            publicationDto.setMaxAge(75);
        }
        return publicationDto;
    }

    public String doInitPublication() {
        // reset everything from the campaign
        publicationDto = null;
        getPublicationNavigationBean().setEncodedId("");
        getPublicationNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_NEW);
        getNavigationSessionBean().navigate(Constants.ADD_PUBLICATION);
        getAppSettingsMBean().setShowAdvanced(false);
        getSiteSettingsMBean().setShowAdvanced(false);
        return "pretty:newPublication";
    }

    public boolean isNewPublication() {
        return publicationDto == null || publicationDto.getStatus() == null
                || publicationDto.getStatus().equals(PublicationStatus.NEW.getStatus())
                || publicationDto.getStatus().equals(PublicationStatus.NEW_REVIEW.getStatus());
    }

    public void setPublicationDto(PublicationDto publicationDto) {
        this.publicationDto = publicationDto;
    }

    @Override
    public AppSettingsMBean getAppSettingsMBean() {
        return appSettingsMBean;
    }

    public void setAppSettingsMBean(AppSettingsMBean appSettingsMBean) {
        this.appSettingsMBean = appSettingsMBean;
    }

    @Override
    public AppAddSlotsMBean getAppAddSlotsMBean() {
        return appAddSlotsMBean;
    }

    public void setAppAddSlotsMBean(AppAddSlotsMBean appAddSlotsMBean) {
        this.appAddSlotsMBean = appAddSlotsMBean;
    }

    @Override
    public SiteSettingsMBean getSiteSettingsMBean() {
        return siteSettingsMBean;
    }

    public void setSiteSettingsMBean(SiteSettingsMBean siteSettingsMBean) {
        this.siteSettingsMBean = siteSettingsMBean;
    }
}

package com.adfonic.tools.beans.audience;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.audience.CampaignUsingAudienceDto;
import com.adfonic.dto.audience.MyAudienceDto;
import com.adfonic.dto.audience.MyAudienceDto.Status;
import com.adfonic.dto.audience.ThirdPartyAudienceDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.util.AbstractLazyDataModelWrapper;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

@Component
@Scope("view")
@URLMapping(id = "audience-builder", pattern = "/audiences", viewId = "/WEB-INF/jsf/audiencebuilder/audienceBuilder.jsf")
public class AudienceBuilderMBean extends GenericAbstractBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AudienceBuilderMBean.class);

    private static final String CAMPAIGN_SEPARATOR = ", ";
    private static final String DELETE_CAMPAIGN_SUMMARY_PREFIX = "page.audiencebuilder.labels.deletedialog.linkedcampaigns";
    private static final String AUDIENCE_STATUS_LABEL_PREFIX = "page.audiencebuilder.labels.audiencestatus.";
    private static final String AUDIENCE_TYPE_LABEL_PREFIX = "page.audiencebuilder.labels.audiencetype.";

    @Autowired
    private AudienceService service;

    protected LazyDataModel<MyAudienceDto> myAudiencesLazyDataModel;
    protected LazyDataModel<ThirdPartyAudienceDto> thirdPartyAudiencesLazyDataModel;

    protected MyAudienceDto[] myAudiencesSelectedRows;
    protected ThirdPartyAudienceDto[] thirdPartyAudiencesSelectedRows;

    protected List<MyAudienceDto> filteredMyAudiences;
    protected List<ThirdPartyAudienceDto> filteredThirdPartyAudiences;

    private String pauseConfirmationMessage = null;

    @Override
    @PostConstruct
    protected void init() throws Exception {
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Initialization
    // -----------------------------------------------------------------------------------------------------------------

    // invoke on initial view load
    @URLAction(mappingId = "audience-builder", onPostback = false)
    public void load() throws Exception {
        LOGGER.debug("load-->");

        // required for tab to be properly selected
        getNavigationSessionBean().navigate(Constants.AUDIENCE_BUILDER);

        // check feature enabled and role-based access
        if (!getToolsApplicationBean().isAudienceEnabled()) {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/dashboard");
            return;
        }

        doRun();

        LOGGER.debug("<--load");
    }

    public static String getAudienceStatusMessage(String status) {
        return FacesUtils.getBundleMessage(AUDIENCE_STATUS_LABEL_PREFIX + status);
    }

    public static String getAudienceTypeMessage(String type) {
        if (StringUtils.isNotEmpty(type)) {
            return FacesUtils.getBundleMessage(AUDIENCE_TYPE_LABEL_PREFIX + type);
        }
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Table building
    // -----------------------------------------------------------------------------------------------------------------

    public LazyDataModel<MyAudienceDto> getMyAudiencesLazyDataModel() {
        return myAudiencesLazyDataModel;
    }

    public void setMyAudiencesLazyDataModel(LazyDataModel<MyAudienceDto> myAudiencesLazyDataModel) {
        this.myAudiencesLazyDataModel = myAudiencesLazyDataModel;
    }

    public LazyDataModel<ThirdPartyAudienceDto> getThirdPartyAudiencesLazyDataModel() {
        return thirdPartyAudiencesLazyDataModel;
    }

    public void setThirdPartyAudiencesLazyDataModel(LazyDataModel<ThirdPartyAudienceDto> thirdPartyAudiencesLazyDataModel) {
        this.thirdPartyAudiencesLazyDataModel = thirdPartyAudiencesLazyDataModel;
    }

    public MyAudienceDto[] getMyAudiencesSelectedRows() {
        return myAudiencesSelectedRows;
    }

    public void setMyAudiencesSelectedRows(MyAudienceDto[] myAudiencesSelectedRows) {
        this.myAudiencesSelectedRows = myAudiencesSelectedRows;
    }

    public ThirdPartyAudienceDto[] getThirdPartyAudiencesSelectedRows() {
        return thirdPartyAudiencesSelectedRows;
    }

    public void setThirdPartyAudiencesSelectedRows(ThirdPartyAudienceDto[] thirdPartyAudiencesSelectedRows) {
        this.thirdPartyAudiencesSelectedRows = thirdPartyAudiencesSelectedRows;
    }

    public List<MyAudienceDto> getFilteredMyAudiences() {
        return filteredMyAudiences;
    }

    public void setFilteredMyAudiences(List<MyAudienceDto> filteredMyAudiences) {
        this.filteredMyAudiences = filteredMyAudiences;
    }

    public List<ThirdPartyAudienceDto> getFilteredThirdPartyAudiences() {
        return filteredThirdPartyAudiences;
    }

    public void setFilteredThirdPartyAudiences(List<ThirdPartyAudienceDto> filteredThirdPartyAudiences) {
        this.filteredThirdPartyAudiences = filteredThirdPartyAudiences;
    }

    public void doRun() {
        LOGGER.debug("doRun-->");
        this.thirdPartyAudiencesLazyDataModel = buildThirdPartyAudiencesDataModel();
        this.myAudiencesLazyDataModel = buildMyAudiencesDataModel();

        LOGGER.debug("<--doRun");
    }

    protected LazyDataModel<ThirdPartyAudienceDto> buildThirdPartyAudiencesDataModel() {
        return new AbstractLazyDataModelWrapper<ThirdPartyAudienceDto>(service.createThirdPartyAudiencesLazyDataModel(this.getUser()
                .getCompany()));
    }

    protected LazyDataModel<MyAudienceDto> buildMyAudiencesDataModel() {
        return new AbstractLazyDataModelWrapper<MyAudienceDto>(service.createMyAudiencesLazyDataModel(this.getUser().getAdvertiserDto()));
    }

    public int getSelectedRowsCount() {
        if (ArrayUtils.isEmpty(myAudiencesSelectedRows)) {
            return 0;
        } else {
            return myAudiencesSelectedRows.length;
        }

    }

    public String getConfirmDeleteSummary() {
        String confirmDeleteSummary = "";
        LOGGER.debug("<--getConfirmDeleteSummary");
        StringBuilder sb = new StringBuilder();

        if (ArrayUtils.isEmpty(myAudiencesSelectedRows)) {
            LOGGER.debug("No rows selected");
        } else {
            if (ArrayUtils.isNotEmpty(myAudiencesSelectedRows)) {
                List<CampaignUsingAudienceDto> campaigns = service.getAllCampaignsUsingAudiencesAsSingleList(Arrays
                        .asList(myAudiencesSelectedRows));
                int lines = 0;
                for (CampaignUsingAudienceDto c : campaigns) {
                    if (lines > 0) {
                        sb.append(CAMPAIGN_SEPARATOR);
                    }
                    lines++;
                    sb.append(c.getName());
                }
            }
            if (sb.length() > 0) {
                confirmDeleteSummary = FacesUtils.getBundleMessage(DELETE_CAMPAIGN_SUMMARY_PREFIX, sb.toString());
            }
        }
        LOGGER.debug("getConfirmDeleteSummary<--");
        return confirmDeleteSummary;
    }

    public void doActivateCollection() {
        LOGGER.debug("doActivateCollection-->");
        if (ArrayUtils.isEmpty(myAudiencesSelectedRows)) {
            LOGGER.debug("No rows selected");
            return;
        }
        for (MyAudienceDto dto : myAudiencesSelectedRows) {
            LOGGER.debug("processing item id: " + dto.getExternalId());
            service.toggleFirstPartyAudienceCollection(dto, true);
        }
        LOGGER.debug("<--doActivateCollection");
    }

    // warning for changes to an active audience that has already been targeted
    // by campaigns
    public void checkPause(ActionEvent event) {
        LOGGER.debug("checkPause-->");
        if (ArrayUtils.isNotEmpty(myAudiencesSelectedRows)) {
            // only applies to non-new, non-static audiences
            // targeted by one or more campaigns
            List<MyAudienceDto> audiencesToPause = new ArrayList<MyAudienceDto>(0);
            for (MyAudienceDto audience : myAudiencesSelectedRows) {
                if (audience.getStatus() == Status.ACTIVE) {
                    audiencesToPause.add(audience);
                }
            }

            if (CollectionUtils.isNotEmpty(audiencesToPause)) {
                List<CampaignUsingAudienceDto> campaigns = service.getAllCampaignsUsingAudiencesAsSingleList(audiencesToPause);
                if (CollectionUtils.isNotEmpty(campaigns)) {
                    StringBuilder sb = new StringBuilder();
                    int lines = 0;
                    for (CampaignUsingAudienceDto c : campaigns) {
                        if (lines > 0) {
                            sb.append(CAMPAIGN_SEPARATOR);
                        }
                        lines++;
                        sb.append(c.getName());
                    }
                    pauseConfirmationMessage = sb.toString();
                    RequestContext.getCurrentInstance().execute("confirmPause.show()");
                    return;
                }
            }
        }
        doPauseCollection();
        LOGGER.debug("checkPause<--");
    }

    public void doPauseCollection() {
        LOGGER.debug("doPauseCollection-->");
        if (ArrayUtils.isEmpty(myAudiencesSelectedRows)) {
            LOGGER.debug("No rows selected");
            return;
        }
        for (MyAudienceDto dto : myAudiencesSelectedRows) {
            LOGGER.debug("processing item id: " + dto.getExternalId());
            service.toggleFirstPartyAudienceCollection(dto, false);
        }
        LOGGER.debug("doPauseCollection<--");
    }

    public void doDeleteAudiences() {
        LOGGER.debug("doDeleteAudiences-->");
        if (ArrayUtils.isEmpty(myAudiencesSelectedRows)) {
            LOGGER.debug("No rows selected");
            return;
        } else {
            for (MyAudienceDto dto : myAudiencesSelectedRows) {
                LOGGER.debug("processing item id: " + dto.getExternalId());
                service.deleteAudience(dto);
            }
        }
        myAudiencesSelectedRows = null;
        LOGGER.debug("doDeleteAudiences<--");
    }

    public String getPauseConfirmationMessage() {
        return pauseConfirmationMessage;
    }

    public void setPauseConfirmationMessage(String pauseConfirmationMessage) {
        this.pauseConfirmationMessage = pauseConfirmationMessage;
    }
}

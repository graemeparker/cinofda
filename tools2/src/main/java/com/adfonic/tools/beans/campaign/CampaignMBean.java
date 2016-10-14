package com.adfonic.tools.beans.campaign;

import java.io.Serializable;
import java.net.URLDecoder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.creative.CampaignCreativeDto;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.tools.beans.campaign.bid.CampaignBidMBean;
import com.adfonic.tools.beans.campaign.confirmation.CampaignConfirmationMBean;
import com.adfonic.tools.beans.campaign.creative.CampaignCreativeMBean;
import com.adfonic.tools.beans.campaign.history.CampaignHistoryMBean;
import com.adfonic.tools.beans.campaign.inventory.CampaignInventoryTargetingMBean;
import com.adfonic.tools.beans.campaign.scheduling.CampaignSchedulingMBean;
import com.adfonic.tools.beans.campaign.setup.CampaignSetupMBean;
import com.adfonic.tools.beans.campaign.targeting.CampaignTargetingMBean;
import com.adfonic.tools.beans.campaign.tracking.CampaignTrackingMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = {
        @URLMapping(id = "newCampaign", pattern = "/campaign", viewId = "/WEB-INF/jsf/campaign/campaign.jsf"),
        @URLMapping(id = "campaignSetup", pattern = "/campaign/#{id : campaignNavigationSessionBean.encodedId}", viewId = "/WEB-INF/jsf/campaign/campaign.jsf") })
public class CampaignMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 4188436945608530254L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignMBean.class);

    private CampaignDto campaignDto;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignSetupMBean campaignSetupMBean;

    @Autowired
    private CampaignSchedulingMBean campaignSchedulingMBean;

    @Autowired
    private CampaignTargetingMBean campaignTargetingMBean;

    @Autowired
    private CampaignInventoryTargetingMBean campaignInventoryTargetingMBean;

    @Autowired
    private CampaignCreativeMBean campaignCreativeMBean;

    @Autowired
    private CampaignBidMBean campaignBidMBean;

    @Autowired
    private CampaignTrackingMBean campaignTrackingMBean;

    @Autowired
    private CampaignConfirmationMBean campaignConfirmationMBean;

    @Autowired
    private CampaignHistoryMBean campaignHistoryMBean;

    @Override
    @URLActions(actions = { @URLAction(mappingId = "campaignSetup") })
    public void init() throws Exception {

        LOGGER.debug("init-->");
        if (StringUtils.isEmpty(getCNavigationBean().getEncodedId())) {
            initCampaignWorkflow();
        } else if (campaignDto == null) {
            getNavigationSessionBean().navigate(Constants.ADD_CAMPAIGN);
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("progressDialog.hide()");
            CampaignDto searchDto = new CampaignDto();
            UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
            String id = URLDecoder.decode(getCNavigationBean().getEncodedId(), "UTF-8");
            searchDto.setExternalID(id);
            LOGGER.debug(id);
            Exception exception = null;
            try {
                campaignDto = campaignService.getCampaignByExternalId(searchDto);
            } catch (Exception e) {
                exception = e;
            }
            if (campaignDto == null || campaignDto.getId() == null
                    || !userDto.getAdvertiserDto().getId().equals(campaignDto.getAdvertiser().getId())) {
                if (exception!=null){
                    LOGGER.warn("Error retrieving campaign with external id " + id + ":" + exception.getMessage(), exception);
                }else{
                    LOGGER.warn("Try to navigate to a non existing campaign with id " + id);
                }
                initCampaignWorkflow();
                campaignDto = null;
                throw new Exception();
            }
            getCampaignCreativeMBean().setSubmitStatus("CLOSED");
            if (getCNavigationBean().isFromSetup()) {
                LOGGER.debug("Navigate from setup, campaign recently created");
                getCNavigationBean().setFromSetup(false);
                updateCampaignBeans(campaignDto);
                getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_SCHEDULING);
                getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_scheduling.xhtml");
            } else {
                LOGGER.debug("Navigate to an existing campaign");
                CampaignCreativeDto ccDto = campaignService.loadCreatives(campaignDto.getId());
                loadCampaign(campaignDto, ccDto, true);
                getCampaignCreativeMBean().setEditing(false);
                if (getCNavigationBean().isFromCopy()) {
                    getCNavigationBean().setFromCopy(false);
                    getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_SETUP);
                    getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_setup.xhtml");
                }
            }
        }
        LOGGER.debug("init<--");
    }

    @URLActions(actions = { @URLAction(mappingId = "newCampaign") })
    public void initCampaignWorkflow() {
        LOGGER.debug("initCampaignWorkflow-->");
        // reset everything from the campaign
        getCNavigationBean().setEncodedId(null);
        getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_SETUP);
        getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_setup.xhtml");
        getCNavigationBean().restartMenu();
        getCNavigationBean().initNavigation();
        getCampaignCreativeMBean().setEditing(false);
        // Style the tabs from header
        getNavigationSessionBean().navigate(Constants.ADD_CAMPAIGN);
        campaignDto = null;
        getCampaignCreativeMBean().setCampaignDto(new CampaignCreativeDto());
        getCampaignCreativeMBean().setSubmitStatus("CLOSED");
        getCampaignSchedulingMBean().setDisplayHourAndMinutesSelector(false);
        getCNavigationBean().setCampaignBlocked(false);
        LOGGER.debug("initCampaignWorkflow<--");
    }

    public String doInitCampaign() {
        initCampaignWorkflow();
        return "pretty:newCampaign";
    }

    public void loadCampaign(CampaignDto dto, CampaignCreativeDto campCreative, boolean navigateToConfirmation) {
        LOGGER.debug("loadCampaing-->");

        // Targeting
        if (!CollectionUtils.isEmpty(dto.getCurrentSegment().getGeotargets())) {
            campaignTargetingMBean.getCampaignTargetingLocationMBean().setLocationTargetingType("GEO");
        } else {
            campaignTargetingMBean.getCampaignTargetingLocationMBean().setLocationTargetingType("COUNTRY");
        }
        // bidding
        if (dto.getCapImpressions() == null) {
            dto.setCapPeriodSeconds(0);
        }
        if (dto.getCapPeriodSeconds() != null && dto.getCapPeriodSeconds() == 24 * 60 * 60) {
            getCampaignBidMBean().setInterval("day");
        } else if (dto.getCapPeriodSeconds() != null && dto.getCapPeriodSeconds() == 7 * 24 * 60 * 60) {
            getCampaignBidMBean().setInterval("week");
        } else {
            getCampaignBidMBean().setInterval("hour");
        }
        if (!StringUtils.isEmpty(campaignDto.getCurrentSegment().getConnectionType())) {
            getCampaignTargetingBean().getCampaignTargetingConnectionMBean().setConnectionType(
                    campaignDto.getCurrentSegment().getConnectionType());
        }
        getCampaignSchedulingMBean().setDisplayHourAndMinutesSelector(false);
        updateCampaignBeans(dto);
        getCampaignCreativeMBean().setSubmitStatus("CLOSED");
        getCampaignCreativeBean().setCampaignDto(campCreative);
        if (campaignDto.getStatus().equals(CampaignStatus.NEW.getStatus())) {
            getCNavigationBean().restartMenu();
            getCNavigationBean().setNavigationForCampaign(campaignDto.getId());
        } else {
            getCNavigationBean().openAllMenu();
            if (navigateToConfirmation) {
                getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
                getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");

            }
        }
        LOGGER.debug("loadCampaing<--");
    }

    public boolean isNewCampaign() {
        if (campaignDto == null || campaignDto.getId() == null) {
            return true;
        }
        return getCampaignMBean().getCampaignDto().getStatus().equals(com.adfonic.domain.Campaign.Status.NEW)
                || getCampaignMBean().getCampaignDto().getStatus().equals(com.adfonic.domain.Campaign.Status.NEW_REVIEW);
    }

    public boolean isLiveCampaign() {
        if (campaignDto == null || campaignDto.getId() == null) {
            return false;
        }
        return !isNewCampaign() && !getCampaignMBean().getCampaignDto().getStatus().equals(com.adfonic.domain.Campaign.Status.STOPPED)
                && !getCampaignMBean().getCampaignDto().getStatus().equals(com.adfonic.domain.Campaign.Status.COMPLETED);
    }

    public String getContinueButtonMessage() {
        if (isNewCampaign()) {
            return FacesUtils.getBundleMessage("page.campaign.setup.button.save.label");
        } else {
            return FacesUtils.getBundleMessage("page.campaign.button.save.confirm.label");
        }
    }

    public boolean isSavedCampaign() {
        return campaignDto != null && campaignDto.getId() != null;
    }

    public CampaignDto getCampaignDto() {
        if (campaignDto == null) {
            campaignDto = new CampaignDto();
        }
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public CampaignSetupMBean getCampaignSetupMBean() {
        return campaignSetupMBean;
    }

    public void setCampaignSetupMBean(CampaignSetupMBean campaignSetupMBean) {
        this.campaignSetupMBean = campaignSetupMBean;
    }

    public CampaignSchedulingMBean getCampaignSchedulingMBean() {
        return campaignSchedulingMBean;
    }

    public void setCampaignSchedulingMBean(CampaignSchedulingMBean campaignSchedulingMBean) {
        this.campaignSchedulingMBean = campaignSchedulingMBean;
    }

    public CampaignTargetingMBean getCampaignTargetingMBean() {
        return campaignTargetingMBean;
    }

    public void setCampaignTargetingMBean(CampaignTargetingMBean campaignTargetingMBean) {
        this.campaignTargetingMBean = campaignTargetingMBean;
    }

    public CampaignInventoryTargetingMBean getCampaignInventoryTargetingMBean() {
        return campaignInventoryTargetingMBean;
    }

    public void setCampaignInventoryTargetingMBean(CampaignInventoryTargetingMBean campaignInventoryTargetingMBean) {
        this.campaignInventoryTargetingMBean = campaignInventoryTargetingMBean;
    }

    @Override
    public CampaignBidMBean getCampaignBidMBean() {
        return campaignBidMBean;
    }

    public void setCampaignBidMBean(CampaignBidMBean campaignBidMBean) {
        this.campaignBidMBean = campaignBidMBean;
    }

    @Override
    public CampaignTrackingMBean getCampaignTrackingMBean() {
        return campaignTrackingMBean;
    }

    public void setCampaignTrackingMBean(CampaignTrackingMBean campaignTrackingMBean) {
        this.campaignTrackingMBean = campaignTrackingMBean;
    }

    public CampaignCreativeMBean getCampaignCreativeMBean() {
        return campaignCreativeMBean;
    }

    public void setCampaignCreativeMBean(CampaignCreativeMBean campaignCreativeMBean) {
        this.campaignCreativeMBean = campaignCreativeMBean;
    }

    @Override
    public CampaignConfirmationMBean getCampaignConfirmationMBean() {
        return campaignConfirmationMBean;
    }

    public void setCampaignConfirmationMBean(CampaignConfirmationMBean campaignConfirmationMBean) {
        this.campaignConfirmationMBean = campaignConfirmationMBean;
    }

    @Override
    public CampaignHistoryMBean getCampaignHistoryMBean() {
        return campaignHistoryMBean;
    }

    public void setCampaignHistoryMBean(CampaignHistoryMBean campaignHistoryMBean) {
        this.campaignHistoryMBean = campaignHistoryMBean;
    }

}

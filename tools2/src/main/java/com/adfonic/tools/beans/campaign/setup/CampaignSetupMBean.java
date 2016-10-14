package com.adfonic.tools.beans.campaign.setup;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.tools.beans.user.UserSessionBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("view")
public class CampaignSetupMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4188436945608530254L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignSetupMBean.class);

    private CampaignDto campaignDto;

    private List<CampaignTypeAheadDto> campaigns = null;

    private CampaignTypeAheadDto campaignToLoad;

    private String name;

    private String reference;
    
    // MAD-2900 - Add New Field "Opportunity ID"
    private String opportunity;

    @Autowired
    private CampaignService service;

    public String doSave() throws Exception {
        LOGGER.debug("doSave-->");
        // save and continue campaign;
        if (!isDuplicatedName(name)) {
            boolean newCampaign = campaignDto == null || campaignDto.getId() == null;
            boolean nameChanged = false;
            if (!newCampaign && !campaignDto.getName().equals(name)) {
                nameChanged = true;
                newCampaign = true;
            }
            StopWatch stWatch = startWatch();
            LOGGER.debug("CampaignSetupMBean doSave Begin [" + stWatch.getTime() * 0.001 + "] seconds");
            campaignDto = service.saveSetUp(prepareDto(campaignDto));
            stWatch.stop();
            LOGGER.debug("CampaignSetupMBean Time Taken doSave Campaign End [" + stWatch.getTime() * 0.001 + "] seconds");
            LOGGER.info("New campaign created with id " + campaignDto.getId() + " and name " + campaignDto.getName());
            // update campaignDto to controller bean
            updateCampaignBeans(campaignDto);
            getCampaignSchedulerBean().setShowTimeDayControl(false);
            if (getCNavigationBean().isSchedulingDisabled()) {
                getCNavigationBean().setSchedulingDisabled(false);
                getCNavigationBean().saveCampaignNavigation(campaignDto.getId(), Constants.MENU_SCHEDULING);
            }

            if (newCampaign) {
                getCNavigationBean().setEncodedId(URLEncoder.encode(campaignDto.getExternalID(), "UTF-8"));
                if (!nameChanged) {
                    getCNavigationBean().setFromSetup(true);
                }
                return "pretty:campaignSetup";
            }

            // new campaigns go to next step
            if (getCampaignMBean().isNewCampaign()) {
                getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_SCHEDULING);
                getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_scheduling.xhtml");
            }
            // exisiting campaigns redirected to confirmation page
            else {
                getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
                getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
            }
        } else {
            LOGGER.debug("Duplicated name");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "campaign-name", null, "page.error.validation.duplicatednamme");
        }
        LOGGER.debug("doSave<--");
        return null;
    }

    public String copyCampaign() throws Exception {
        LOGGER.debug("copyCampaign-->");
        if (campaignToLoad != null) {
            campaignDto = service.copyCampaign(campaignToLoad.getId());
            getCNavigationBean().setEncodedId(URLEncoder.encode(campaignDto.getExternalID(), "UTF-8"));
            getCNavigationBean().setFromCopy(true);
            return "pretty:campaignSetup";
        }
        LOGGER.debug("copyCampaign<--");
        return null;
    }

    public Collection<CampaignTypeAheadDto> completeCampaigns(String query) {
        return getAdvertiserCampaigns(service, query);
    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        if (dto == null) {
            dto = new CampaignDto();
        }
        dto.setAdvertiser(getUser().getAdvertiserDto());
        dto.setName(name);
        dto.setReference(reference);
        dto.setOpportunity(opportunity);
        LOGGER.debug("prepareDto<--");
        return dto;
    }

    public void loadCampaign(CampaignDto dto) {
        LOGGER.debug("loadCampaign-->");
        this.campaignDto = dto;
        if (dto != null) {
            this.name = dto.getName();
            this.reference = dto.getReference();
            this.opportunity = dto.getOpportunity();
        } else {
            this.name = null;
            this.reference = null;
            this.opportunity = null;
        }
        LOGGER.debug("loadCampaign<--");
    }

    public void cancel(ActionEvent event) {
        LOGGER.debug("cancel-->");
        LOGGER.debug("Setup cancelled, restore name/reference " + campaignDto == null ? "nul campaign"
                : (campaignDto.getName() + "/" + campaignDto.getReference()));
        this.name = campaignDto.getName();
        this.reference = campaignDto.getReference();
        this.opportunity = campaignDto.getOpportunity();
        getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
        getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
        LOGGER.debug("cancel<--");
    }

    @Override
    protected void init() {
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public List<CampaignTypeAheadDto> getCampaigns() {
        if (campaigns == null) {
            LOGGER.debug("getCampaigns-->");
            LOGGER.debug("Search all campaigns to copy for user: " + getUser().getId());
            campaigns = (List<CampaignTypeAheadDto>) getAdvertiserCampaigns(service);
            LOGGER.debug(CollectionUtils.isEmpty(campaigns) ? "No campaigns found" : (campaigns.size() + " campaigns found"));
            LOGGER.debug("getCampaigns<--");
        }
        return campaigns;
    }

    public void setCampaigns(List<CampaignTypeAheadDto> campaigns) {
        this.campaigns = campaigns;
    }

    public CampaignTypeAheadDto getCampaignToLoad() {
        return campaignToLoad;
    }

    public void setCampaignToLoad(CampaignTypeAheadDto campaignToLoad) {
        this.campaignToLoad = campaignToLoad;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getOpportunity() {
		return opportunity;
	}

	public void setOpportunity(String opportunity) {
		this.opportunity = opportunity;
	}

	private boolean isDuplicatedName(String name) {
        if (name != null) {
            CampaignSearchDto dto = new CampaignSearchDto();
            dto.setName(this.name);
            UserSessionBean bean = Utils.findBean(FacesContext.getCurrentInstance(), Constants.USER_SESSION_BEAN);
            UserDTO userDto = (UserDTO) bean.getMap().get(Constants.USERDTO);
            dto.setAdvertiser(userDto.getAdvertiserDto());
            CampaignTypeAheadDto obj = service.getCampaignWithName(dto);
            if (obj == null || obj.getId() == null) {
                return false;
            } else if (campaignDto == null || campaignDto.getId() == null || campaignDto.getId().longValue() != obj.getId().longValue()) {
                return true;
            }
        }
        return false;
    }
}

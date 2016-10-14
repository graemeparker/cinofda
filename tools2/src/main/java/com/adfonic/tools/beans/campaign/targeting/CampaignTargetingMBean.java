package com.adfonic.tools.beans.campaign.targeting;

import static com.adfonic.tools.beans.campaign.targeting.CampaignTargetingLocationMBean.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.geotarget.GeotargetTypeDto;
import com.adfonic.dto.operator.OperatorAutocompleteDto;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.operator.OperatorService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignTargetingMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTargetingMBean.class);

    private CampaignDto campaignDto;

    @Autowired
    private CampaignService service;
    @Autowired
    private OperatorService operatorService;

    // LOCATION
    @Autowired
    private CampaignTargetingLocationMBean campaignTargetingLocationMBean;

    // PLATFORM & DEVICES
    @Autowired
    private CampaignTargetingDeviceMBean campaignTargetingDeviceMBean;

    // CONNECTION
    @Autowired
    private CampaignTargetingConnectionMBean campaignTargetingConnectionMBean;

    // DEMOGRAPHICS
    @Autowired
    private CampaignTargetingDemographicsMBean campaignTargetingDemographicsMBean;

    // AUDIENCE
    @Autowired
    private CampaignTargetingAudienceMBean campaignTargetingAudienceMBean;

    // APP VS SITE
    @Autowired
    private CampaignTargetingAppSiteMBean campaignTargetingAppSiteMBean;

    private String message;

    @Override
    public void init() {
        // do nothing
    }

    /**
     *
     * @param event
     * @throws Exception
     */
    public void doSave(ActionEvent event) {
        LOGGER.debug("Event receive in doSave(): {}", event);
        // location validation
        if (!campaignTargetingLocationMBean.isValid() || !campaignTargetingConnectionMBean.isValid()) {
            return;
        }
        campaignDto = service.saveTargeting(prepareDto(campaignDto), this.campaignTargetingLocationMBean.getLocationListHasChanged());
        updateCampaignBeans(campaignDto);

        // navigate to other page
        campaignTargetingLocationMBean.setChangesNotSave(false);
        campaignTargetingLocationMBean.setRenderingForDialog(false);

        if (getCampaignMBean().isNewCampaign()) {
            if (getCNavigationBean().isInventoryTargetingDisabled()) {
                getCNavigationBean().setInventoryTargetingDisabled(false);
                getCNavigationBean().saveCampaignNavigation(campaignDto.getId(), Constants.MENU_INVENTORY_TARGETING);
            }
            getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_INVENTORY_TARGETING);
            getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_inventory.xhtml");
        } else {
            getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
            getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
        }
    }

    public void continueWithChanges(ActionEvent event) {
        getCampaignCreativeBean().removeNotCommonDestinations();
        getCNavigationBean().setTrackingDisabled(true);
        getCNavigationBean().setBiddingDisabled(true);
        getCNavigationBean().setConfirmationDisabled(true);
        if (campaignDto.getStatus().equals(com.adfonic.domain.Campaign.Status.ACTIVE)) {
            List<Long> ids = new ArrayList<Long>();
            ids.add(campaignDto.getId());
            campaignDto.setStatus(com.adfonic.domain.Campaign.Status.PAUSED);
            service.changeCampaignStatus(ids, CampaignStatus.PAUSED);
            getCNavigationBean().setCampaignBlocked(true);
        }
        if (campaignDto.getStatus().equals(com.adfonic.domain.Campaign.Status.NEW_REVIEW)) {
            List<Long> ids = new ArrayList<Long>();
            ids.add(campaignDto.getId());
            campaignDto.setStatus(com.adfonic.domain.Campaign.Status.NEW);
            service.changeCampaignStatus(ids, CampaignStatus.NEW);
        }

        // MAX-97 - Goal Conversion Tracking now show in tools2
        if (campaignTargetingDeviceMBean.dataChanged()) {
            campaignDto.getDeviceIdentifierTypes().clear();
            campaignDto = service.saveTracking(campaignDto);
        }

        doSave(event);
        getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CREATIVE);
        getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_creative.xhtml");
    }

    public void checkContinue(ActionEvent event) {
        if (!getCNavigationBean().isTrackingDisabled() && campaignTargetingDeviceMBean.dataChanged()) {
            campaignTargetingLocationMBean.setRenderingForDialog(true);
            RequestContext.getCurrentInstance().execute("confirmationChanges.show()");
        } else {
            doSave(event);
        }
    }

    public void cancel(ActionEvent event) {
        LOGGER.debug("cancel-->");
        LOGGER.debug("Event receive in cancel(): {}", event);
        loadCampaignDto(campaignDto);
        getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
        getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
        LOGGER.debug("cancel<--");
    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        dto.setAdvertiser(getUser().getAdvertiserDto());
        if (dto.getSegments() != null && CollectionUtils.isEmpty(dto.getSegments())) {
            LOGGER.debug("Segment list empty, adding a new Segment");
            dto.getSegments().add(dto.getCurrentSegment());
        }

        // Location
        CampaignDto localDto = dto;
        localDto = campaignTargetingLocationMBean.prepareDto(localDto);
        // Devices
        localDto = campaignTargetingDeviceMBean.prepareDto(localDto);
        // Connections
        localDto = campaignTargetingConnectionMBean.prepareDto(localDto);
        // Demographics
        localDto = campaignTargetingDemographicsMBean.prepareDto(localDto);
        // Audience
        localDto = campaignTargetingAudienceMBean.prepareDto(localDto);
        // App vs Web
        localDto = campaignTargetingAppSiteMBean.prepareDto(localDto);

        LOGGER.debug("prepareDto<--");
        return localDto;
    }

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = dto;
        campaignTargetingLocationMBean.loadCampaignDto(dto);
        campaignTargetingDeviceMBean.loadCampaignDto(dto);
        campaignTargetingConnectionMBean.loadCampaignDto(dto);
        campaignTargetingDemographicsMBean.loadCampaignDto(dto);
        campaignTargetingAudienceMBean.loadCampaignDto(dto);
        campaignTargetingAppSiteMBean.loadCampaignDto(dto);
        LOGGER.debug("loadCampaignDto<--");
    }

    /** Event handlers **/
    public void onGeotargetingTypeChangeEvent(ValueChangeEvent event) {
        LOGGER.debug("onGeotargetingTypeChangeEvent-->");
        GeotargetTypeDto newValue = (GeotargetTypeDto) event.getNewValue();

        LOGGER.debug("GeotargetType changed");
        if (newValue != null && !newValue.equals(campaignTargetingLocationMBean.getGeotargetingType())) {
            campaignTargetingLocationMBean.getGeotargetsList().clear();
            campaignTargetingLocationMBean.setGeotargetingType(newValue);
        }
        updateOperators("GEO");
        campaignTargetingLocationMBean.setChangesNotSave(true);
        LOGGER.debug("onGeotargetingTypeChangeEvent<--");
    }

    public void onGeotargetingCountryChangeEvent(ValueChangeEvent event) {
        LOGGER.debug("onGeotargetingCountryChangeEvent-->");
        CountryDto newValue = (CountryDto) event.getNewValue();
        if (newValue != null) {
            LOGGER.debug("Geotargeting country changed, new value: " + newValue.getIsoCode());
            // any change makes the list to be erased
            if (!CollectionUtils.isEmpty(campaignTargetingLocationMBean.getGeotargetsList())) {
                campaignTargetingLocationMBean.getGeotargetsList().clear();
            }

            if (!newValue.equals(campaignTargetingLocationMBean.getGeotargetingCountry())) {
                campaignTargetingLocationMBean.setGeotargetingCountry(newValue);
                LOGGER.debug("New geotargeting country: " + campaignTargetingLocationMBean.getGeotargetingCountry());
                // change country, clear
                campaignTargetingLocationMBean.getGeotargetsList().clear();
                // clear country targeting
                campaignTargetingLocationMBean.getCountryList().clear();
                LOGGER.debug("Cleared countryList: " + campaignTargetingLocationMBean.getCountryList().size());
                // reset the types
                campaignTargetingLocationMBean.setGeotargetingType(campaignTargetingLocationMBean.getGeotargetTypesItems().get(0));
            }
            updateOperators("GEO");
        }
        campaignTargetingLocationMBean.setChangesNotSave(true);
        LOGGER.debug("onGeotargetingCountryChangeEvent<--");
    }

    public void onLocationEvent(ValueChangeEvent event) {
        LOGGER.debug("onLocationEvent-->");
        String newValue = (String) event.getNewValue();
        LOGGER.debug("newValue: " + newValue);
		if ((StringUtils.isNotBlank(newValue))) {
			if (TYPE_GEOTARGET.equals(newValue)) {
				campaignTargetingLocationMBean.setCountryListIsWhitelist(true);
				if (campaignTargetingLocationMBean.getGeotargetingType() == null) {
					campaignTargetingLocationMBean.setGeotargetingType(campaignTargetingLocationMBean.getGeotargetTypesItems().get(0));
				}
			}
			// If the selected location changed from POSTCODE (MAD-3144)
	        if (ObjectUtils.equals(campaignTargetingLocationMBean.getLocationTargetingType(), TYPE_POSTCODE) && ObjectUtils.notEqual(newValue, TYPE_POSTCODE)) {
	        	campaignTargetingLocationMBean.isLocationListHasChanged(true);
	        }
		}
        updateOperators(newValue);
        campaignTargetingLocationMBean.setChangesNotSave(true);
        LOGGER.debug("onLocationEvent<--");
    }

    public void countryChanges(org.primefaces.event.SelectEvent event) {
        LOGGER.debug("Event receive in countryChanges(): {}", event);
        updateOperators(null);
        campaignTargetingLocationMBean.setChangesNotSave(true);
    }

    public void countryChanges(org.primefaces.event.UnselectEvent event) {
        LOGGER.debug("Event receive in countryChanges(): {}", event);
        updateOperators(null);
        campaignTargetingLocationMBean.setChangesNotSave(true);
    }

    /** Getters & Setters **/
    public Collection<OperatorAutocompleteDto> completeMobileOperator(String query) {
        return completeOperator(query,true);
    }
    
    public Collection<OperatorAutocompleteDto> completeIspOperator(String query) {
        return completeOperator(query,false);
    }
    

    public Collection<OperatorAutocompleteDto> completeOperator(String query, boolean mobileOperator) {
        List<CountryDto> lc = new ArrayList<CountryDto>();
        if ("GEO".equals(campaignTargetingLocationMBean.getLocationTargetingType())
                && campaignTargetingLocationMBean.getGeotargetingCountry() != null) {
            lc.add(campaignTargetingLocationMBean.getGeotargetingCountry());
        } else {
            lc = campaignTargetingLocationMBean.getCountryList();
        }
        return operatorService.doQuery(query, lc, mobileOperator);
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public CampaignTargetingLocationMBean getCampaignTargetingLocationMBean() {
        return campaignTargetingLocationMBean;
    }

    public void setCampaignTargetingLocationMBean(CampaignTargetingLocationMBean campaignTargetingLocationMBean) {
        this.campaignTargetingLocationMBean = campaignTargetingLocationMBean;
    }

    public CampaignTargetingDeviceMBean getCampaignTargetingDeviceMBean() {
        return campaignTargetingDeviceMBean;
    }

    public void setCampaignTargetingDeviceMBean(CampaignTargetingDeviceMBean campaignTargetingDeviceMBean) {
        this.campaignTargetingDeviceMBean = campaignTargetingDeviceMBean;
    }

    public CampaignTargetingConnectionMBean getCampaignTargetingConnectionMBean() {
        return campaignTargetingConnectionMBean;
    }

    public void setCampaignTargetingConnectionMBean(CampaignTargetingConnectionMBean campaignTargetingConnectionMBean) {
        this.campaignTargetingConnectionMBean = campaignTargetingConnectionMBean;
    }

    public CampaignTargetingDemographicsMBean getCampaignTargetingDemographicsMBean() {
        return campaignTargetingDemographicsMBean;
    }

    public void setCampaignTargetingDemographicsMBean(CampaignTargetingDemographicsMBean campaignTargetingDemographicsMBean) {
        this.campaignTargetingDemographicsMBean = campaignTargetingDemographicsMBean;
    }

    public CampaignTargetingAppSiteMBean getCampaignTargetingAppSiteMBean() {
        return campaignTargetingAppSiteMBean;
    }

    public void setCampaignTargetingAppSiteMBean(CampaignTargetingAppSiteMBean campaignTargetingAppSiteMBean) {
        this.campaignTargetingAppSiteMBean = campaignTargetingAppSiteMBean;
    }

    public CampaignTargetingAudienceMBean getCampaignTargetingAudienceMBean() {
        return campaignTargetingAudienceMBean;
    }

    public void setCampaignTargetingAudienceMBean(CampaignTargetingAudienceMBean campaignTargetingAudienceMBean) {
        this.campaignTargetingAudienceMBean = campaignTargetingAudienceMBean;
    }

    public String getMessage() {
        if (campaignTargetingLocationMBean.isGeotargetListEmpty()) {
            message = "All";
        } else {
            message = "";
        }
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** PRIVATE METHODS **/
    public void updateOperators(String newValue) {
        LOGGER.debug("updateOperators-->");
        List<OperatorAutocompleteDto> verifiedMobileOperators = new ArrayList<OperatorAutocompleteDto>();
        List<OperatorAutocompleteDto> verifiedIspOperators = new ArrayList<OperatorAutocompleteDto>();
        List<CountryDto> countries = new ArrayList<CountryDto>();

        if (newValue != null && "GEO".equals(newValue)) {
            LOGGER.debug("Geotargeting, removed countries list and added geotarget selected country");
            countries.add(campaignTargetingLocationMBean.getGeotargetingCountry());
            campaignTargetingLocationMBean.getCountryList().clear();
        } else if (newValue != null && "POSTCODE".equals(newValue)) {
            LOGGER.debug("Postcode, removed countries list and added geotarget selected country");
            countries.add(campaignTargetingLocationMBean.getSelectedCountryPostcode());
            campaignTargetingLocationMBean.getCountryList().clear();
        } else {
            if (newValue != null && "COORDINATES".equals(newValue)
                    && !CollectionUtils.isEmpty(campaignTargetingLocationMBean.getSelectedCountryCoordinates())) {
                countries.add(campaignTargetingLocationMBean.getSelectedCountryCoordinates().get(0));
                campaignTargetingLocationMBean.getCountryList().clear();
            }
            // All countries selected
            if (CollectionUtils.isEmpty(campaignTargetingLocationMBean.getCountryList())) {
                LOGGER.debug("All countries selected");
                return;
            } else {
                countries.addAll(campaignTargetingLocationMBean.getCountryList());
            }
            // remove everything of the location state etc..
            campaignTargetingLocationMBean.getGeotargetsList().clear();
        }
        LOGGER.debug("Verify mobile operators with selected countries");
        for (OperatorAutocompleteDto o : campaignTargetingConnectionMBean.getMobileOperatorsList()) {
            for (CountryDto c : countries) {
                if ((operatorService.getOperatorById(o.getId())).getCountry().equals(c)) {
                    LOGGER.debug(o.getName() + " verified for " + c.getName());
                    verifiedMobileOperators.add(o);
                    break;
                }
            }
        }
        LOGGER.debug("Verify ISP operators with selected countries");
        for (OperatorAutocompleteDto o : campaignTargetingConnectionMBean.getIspOperatorsList()) {
            for (CountryDto c : countries) {
                if ((operatorService.getOperatorById(o.getId())).getCountry().equals(c)) {
                    LOGGER.debug(o.getName() + " verified for " + c.getName());
                    verifiedIspOperators.add(o);
                    break;
                }
            }
        }

        campaignTargetingConnectionMBean.getMobileOperatorsList().clear();
        campaignTargetingConnectionMBean.getMobileOperatorsList().addAll(verifiedMobileOperators);
        campaignTargetingConnectionMBean.getIspOperatorsList().clear();
        campaignTargetingConnectionMBean.getIspOperatorsList().addAll(verifiedIspOperators);

        LOGGER.debug("updateOperators-->");
    }
}

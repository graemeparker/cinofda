package com.adfonic.tools.beans.campaign.tracking;

import static com.adfonic.domain.DeviceIdentifierType.SYSTEM_NAME_ANDROID;
import static com.adfonic.domain.DeviceIdentifierType.SYSTEM_NAME_HIFA;
import static com.adfonic.domain.DeviceIdentifierType.SYSTEM_NAME_IFA;
import static com.adfonic.presentation.FacesUtils.addFacesMessage;
import static com.adfonic.tools.beans.util.Constants.APP_TRACKING;
import static com.adfonic.tools.beans.util.Constants.GOAL_CONVERSION;
import static com.adfonic.tools.beans.util.Constants.MENU_BIDDING;
import static com.adfonic.tools.beans.util.Constants.MENU_NAVIGATE_TO_BIDDING;
import static com.adfonic.tools.beans.util.Constants.MENU_NAVIGATE_TO_CONFIRMATION;
import static com.adfonic.tools.beans.util.Constants.NO_CONVERSION;
import static com.adfonic.tools.beans.util.Constants.TRACKING_INSTALL_ALLTRAFFIC_NODEVICEIDS;
import static com.adfonic.tools.beans.util.Constants.TRACKING_INSTALL_ONLYTRAFFIC_DEVICEIDS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.deviceidentifier.DeviceIdentifierService;
import com.adfonic.presentation.devicetarget.DeviceTargetService;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignTrackingMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTrackingMBean.class);

    @Autowired
    private CampaignService service;
    @Autowired
    private DeviceTargetService deviceTargetService;
    @Autowired
    private DeviceIdentifierService deviceIdentifierService;

    private CampaignDto campaignDto;

    private Boolean applicationDestination;

    private String convTrackingType;

    private String convTrackingTypeMenuText;

    private String trackingInstallOptions;

    private String trackingInstallOptionsMenuText;

    private List<DeviceIdentifierTypeDto> selectedDeviceIdentifierTypes = null;

    private Collection<DeviceIdentifierTypeDto> deviceIdentifierTypesList = null;

    private String applicationID;

    public void doSave(ActionEvent event) throws Exception {
        LOGGER.debug("doSave-->");
        // save and continue campaign;
        CampaignDto camp = prepareDto(campaignDto);
        if (camp != null) {
            camp = service.saveTracking(camp);
            // update campaignDto to controller bean
            if (getCNavigationBean().isBiddingDisabled()) {
                camp.setCapImpressions(6);
                camp.setCapPeriodSeconds(60 * 60 * 24);
            }
            updateCampaignBeans(camp);
            if (getCNavigationBean().isBiddingDisabled()) {
                getCNavigationBean().setBiddingDisabled(false);
                getCNavigationBean().saveCampaignNavigation(campaignDto.getId(), MENU_BIDDING);
            }

            if (getCampaignMBean().isNewCampaign() || getCNavigationBean().isCampaignBlocked()) {
                getCNavigationBean().updateMenuStyles(MENU_NAVIGATE_TO_BIDDING);
                getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_bidding.xhtml");
            } else {
                getCNavigationBean().updateMenuStyles(MENU_NAVIGATE_TO_CONFIRMATION);
                getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
            }
        }
        LOGGER.debug("doSave<--");
    }

    public CampaignDto prepareDto(CampaignDto campaignDto) {
        LOGGER.debug("prepareDto-->");
        if (!validateFields()) {
            LOGGER.debug("Not valid fields to save");
            return null;
        }
        campaignDto.setAdvertiser(getUser().getAdvertiserDto());

        // Tracking install option radios
        if (TRACKING_INSTALL_ALLTRAFFIC_NODEVICEIDS.equals(trackingInstallOptions)) {
            LOGGER.debug("All traffic including where Device Identifiers not available");
            campaignDto.getDeviceIdentifierTypes().clear();
        } else {
            LOGGER.debug("Only traffic where Device Identifiers are available");
            campaignDto.getDeviceIdentifierTypes().clear();
            campaignDto.getDeviceIdentifierTypes().addAll(selectedDeviceIdentifierTypes);
        }

        // Tracking Type radios
        if (GOAL_CONVERSION.equals(convTrackingType)) {
            LOGGER.debug("Goal converion");
            campaignDto.setConversionTrackingEnabled(true);
            campaignDto.setInstallTrackingEnabled(false);
        } else if (APP_TRACKING.equals(convTrackingType)) {
            LOGGER.debug("App tracking");
            campaignDto.setConversionTrackingEnabled(false);
            campaignDto.setInstallTrackingEnabled(true);
        } else {
            LOGGER.debug("No conversion");
            campaignDto.setConversionTrackingEnabled(false);
            campaignDto.setInstallTrackingEnabled(false);
        }

        campaignDto.setApplicationID(this.applicationID);
        LOGGER.debug("prepareDto<--");
        return campaignDto;
    }

    private boolean validateFields() {
        LOGGER.debug("validateFields-->");
        boolean result = true;

        // Tracking Install Options validation
        if (TRACKING_INSTALL_ONLYTRAFFIC_DEVICEIDS.equals(trackingInstallOptions)
                && CollectionUtils.isEmpty(getSelectedDeviceIdentifierTypes())) {
            LOGGER.debug("'Only traffic where Device Identifiers are available' but no specific deviceids options were selected");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "appleOrAndroidDeviceTypesChkId", null,
                    "page.campaign.conversiontracking.tracking.devicetypeschecks.required");
            result = false;
        }

        // App tracking validation
        if (APP_TRACKING.equals(convTrackingType)) {
            LOGGER.debug("App tracking");
            if (StringUtils.isEmpty(applicationID)) {
                String message = getAndroidOnly() ? "page.campaign.conversiontracking.tracking.googlepackage.required"
                        : "page.campaign.conversiontracking.tracking.appid.required";
                addFacesMessage(FacesMessage.SEVERITY_ERROR, "uniq-id", null, message);
                result = false;
            }
        }
        LOGGER.debug("validateFields<--");
        return result;
    }

    public void loadCampaignDto(CampaignDto campaignDto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = campaignDto;
        if (campaignDto != null) {

            // Loading Tracking install options
            if (!CollectionUtils.isEmpty(campaignDto.getDeviceIdentifierTypes())) {
                LOGGER.debug("Only traffic where Device Identifiers are available");
                getSelectedDeviceIdentifierTypes().clear();
                getSelectedDeviceIdentifierTypes().addAll(campaignDto.getDeviceIdentifierTypes());
                trackingInstallOptions = TRACKING_INSTALL_ONLYTRAFFIC_DEVICEIDS;
            } else {
                LOGGER.debug("All traffic including where Device Identifiers not available");
                trackingInstallOptions = TRACKING_INSTALL_ALLTRAFFIC_NODEVICEIDS;
            }

            // Loading Tracking Type
            if (campaignDto.getConversionTrackingEnabled()) {
                LOGGER.debug("Goal conversion");
                convTrackingType = GOAL_CONVERSION;
            } else if (campaignDto.getInstallTrackingEnabled() && (getAppleOnly() || getAndroidOnly())) {
                LOGGER.debug("App tracking");
                convTrackingType = APP_TRACKING;
            } else {
                LOGGER.debug("No conversion");
                convTrackingType = NO_CONVERSION;
            }

            this.applicationID = campaignDto.getApplicationID();
        }
        LOGGER.debug("loadCampaignDto-->");
    }

    public void cancel(ActionEvent event) {
        LOGGER.debug("cancel-->");
        loadCampaignDto(campaignDto);
        getCNavigationBean().updateMenuStyles(MENU_NAVIGATE_TO_CONFIRMATION);
        getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
        LOGGER.debug("cancel<--");
    }

    public String getAdvertiserId() {
        return getUser().getAdvertiserDto().getExternalID();
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

    public Boolean getApplicationDestination() {
        applicationDestination = service.isApplicationDestination(campaignDto);
        return applicationDestination;
    }

    public void setApplicationDestination(Boolean applicationDestination) {
        this.applicationDestination = applicationDestination;
    }

    public Boolean getAppleOnly() {
        return deviceTargetService.isIOSOnly(campaignDto);
    }

    public Boolean getAndroidOnly() {
        return deviceTargetService.isAndroidOnly(campaignDto);
    }

    public Boolean getIosAndAndroidOnly() {
        return deviceTargetService.isIOSAndroidOnly(campaignDto);
    }

    public Boolean getAndroidTarget() {
        return deviceTargetService.isAndroidTarget(campaignDto);
    }

    public Boolean getAppleTarget() {
        return deviceTargetService.isAppleTarget(campaignDto);
    }

    public String getConvTrackingType() {
        return convTrackingType;
    }

    public String getTrackingInstallOptions() {
        return trackingInstallOptions;
    }

    public void setConvTrackingType(String convTrackingType) {
        this.convTrackingType = convTrackingType;
    }

    public void setTrackingInstallOptions(String trackingInstallOptions) {
        this.trackingInstallOptions = trackingInstallOptions;
    }

    public List<DeviceIdentifierTypeDto> getSelectedDeviceIdentifierTypes() {
        if (selectedDeviceIdentifierTypes == null) {
            selectedDeviceIdentifierTypes = new ArrayList<DeviceIdentifierTypeDto>(0);
        }
        return selectedDeviceIdentifierTypes;
    }

    public void setSelectedDeviceIdentifierTypes(List<DeviceIdentifierTypeDto> selectedDeviceIdentifierTypesUpdated) {
        this.selectedDeviceIdentifierTypes = selectedDeviceIdentifierTypesUpdated;
    }

    public DeviceIdentifierService getDeviceIdentifierService() {
        return deviceIdentifierService;
    }

    public void setDeviceIdentifierService(DeviceIdentifierService deviceIdentifierService) {
        this.deviceIdentifierService = deviceIdentifierService;
    }

    public Collection<DeviceIdentifierTypeDto> getDeviceIdentifierTypesList() {
        if (deviceIdentifierTypesList == null) {
            deviceIdentifierTypesList = new LinkedList<DeviceIdentifierTypeDto>();
            boolean isAppleTarget = getAppleTarget();
            boolean isAndroidTarget = getAndroidTarget();
            String systemName;

            Iterator<DeviceIdentifierTypeDto> dvIt = deviceIdentifierService.getDeviceIdentifierTypes().iterator();
            while (dvIt.hasNext()) {
                DeviceIdentifierTypeDto dto = dvIt.next();
                systemName = dto.getSystemName();

                if (isAppleTarget && !SYSTEM_NAME_ANDROID.equals(systemName)) {
                    deviceIdentifierTypesList.add(dto);
                } else if (isAndroidTarget && !SYSTEM_NAME_IFA.equals(systemName) && !SYSTEM_NAME_HIFA.equals(systemName)) {
                    deviceIdentifierTypesList.add(dto);
                }
            }
        }
        return deviceIdentifierTypesList;
    }

    public void setDeviceIdentifierTypesList(Collection<DeviceIdentifierTypeDto> deviceIdentifierTypesList) {
        this.deviceIdentifierTypesList = deviceIdentifierTypesList;
    }

    public String getTrackingInstallOptionsMenuText() {
        if (!StringUtils.isEmpty(trackingInstallOptions)) {
            if (TRACKING_INSTALL_ONLYTRAFFIC_DEVICEIDS.equals(trackingInstallOptions)) {
                trackingInstallOptionsMenuText = FacesUtils
                        .getBundleMessage("page.campaign.conversiontracking.tracking.install.onlytraffic.deviceids.label");
            } else if (TRACKING_INSTALL_ALLTRAFFIC_NODEVICEIDS.equals(trackingInstallOptions)) {
                trackingInstallOptionsMenuText = FacesUtils
                        .getBundleMessage("page.campaign.conversiontracking.tracking.install.alltraffic.nodeviceids.label");
            } else {
                trackingInstallOptionsMenuText = notSet();
            }
        }
        return trackingInstallOptionsMenuText;
    }

    public void setTrackingInstallOptionsMenuText(String trackingInstallOptionsMenuText) {
        this.trackingInstallOptionsMenuText = trackingInstallOptionsMenuText;
    }

    public String getConvTrackingTypeMenuText() {
        if (!StringUtils.isEmpty(convTrackingType)) {
            if (GOAL_CONVERSION.equals(convTrackingType)) {
                convTrackingTypeMenuText = FacesUtils.getBundleMessage("page.campaign.conversiontracking.tracking.options.conversion.label");
            } else if (APP_TRACKING.equals(convTrackingType)) {
                convTrackingTypeMenuText = FacesUtils.getBundleMessage("page.campaign.conversiontracking.tracking.options.install.label");
            } else {
                // NO_CONVERSION
                convTrackingTypeMenuText = FacesUtils.getBundleMessage("page.campaign.conversiontracking.tracking.options.noconversion.label");
            }
        }
        return convTrackingTypeMenuText;
    }

    public void doChangeInstallTrackingOptions(ValueChangeEvent ev) {
        ev.getNewValue();
    }

    public void setConvTrackingTypeMenuText(String convTrackingTypeMenuText) {
        this.convTrackingTypeMenuText = convTrackingTypeMenuText;
    }

    public String getSelectedDeviceIdentifierTypesToConfirm() {
        String selectedDeviceIdentifierTypesToConfirm = "";
        if (!CollectionUtils.isEmpty(getSelectedDeviceIdentifierTypes())) {
            int k = 0;
            Collection<DeviceIdentifierTypeDto> list = getSelectedDeviceIdentifierTypes();
            for (DeviceIdentifierTypeDto dto : list) {
                String name = dto.getName();
                if (name.equals("DPID") && getAppleOnly()) {
                    name = "iOS UDID";
                }
                if (k < list.size() - 1) {
                    selectedDeviceIdentifierTypesToConfirm = selectedDeviceIdentifierTypesToConfirm + name + ", ";
                } else {
                    selectedDeviceIdentifierTypesToConfirm = selectedDeviceIdentifierTypesToConfirm + name;
                }
                k++;
            }
        }

        return selectedDeviceIdentifierTypesToConfirm;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

}

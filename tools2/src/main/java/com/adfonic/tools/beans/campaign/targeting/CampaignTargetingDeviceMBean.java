package com.adfonic.tools.beans.campaign.targeting;

import static com.adfonic.domain.DeviceGroup.DEVICE_GROUP_MOBILE_SYSTEM_NAME;
import static com.adfonic.domain.DeviceGroup.DEVICE_GROUP_TABLET_SYSTEM_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.segment.SegmentDto;
import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.model.ModelDto;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.model.ModelService;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.tools.beans.commons.DeviceModelsMBean;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("view")
public class CampaignTargetingDeviceMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTargetingDeviceMBean.class);
    private static final String DEVICE_GROUP_ALL = "ALL";

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private ModelService modelService;

    private CampaignDto campaignDto;

    private List<PlatformDto> allPlatForms = new ArrayList<PlatformDto>(0);

    private List<PlatformDto> platformsList = new ArrayList<PlatformDto>(0);

    private int include;

    @Autowired
    private DeviceModelsMBean deviceModelsMBean;

    // since device grouping choices don't match the domain the form uses
    // deviceGroupMode
    private String deviceGroupMode;
    private DeviceGroupDto deviceGroupDto;

    @Override
    public void init() {
    };

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = dto;
        if (campaignDto != null) {
            this.platformsList = new ArrayList<PlatformDto>(campaignDto.getCurrentSegment().getPlatforms());
            this.include = 1;
            if (!CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getExcludedModels())) {
                setSelectedDeviceModels(new ArrayList<ModelDto>(campaignDto.getCurrentSegment().getExcludedModels()));
                include = 1;
            } else if (!CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getModels())) {
                setSelectedDeviceModels(new ArrayList<ModelDto>(campaignDto.getCurrentSegment().getModels()));
                include = 0;
            }

            if (CollectionUtils.isNotEmpty(campaignDto.getCurrentSegment().getDeviceGroups())) {
                deviceGroupDto = campaignDto.getCurrentSegment().getDeviceGroups().iterator().next();
                if (deviceGroupDto.getSystemName().equals(DEVICE_GROUP_MOBILE_SYSTEM_NAME)) {
                    deviceGroupMode = DEVICE_GROUP_MOBILE_SYSTEM_NAME;
                } else {
                    deviceGroupMode = DEVICE_GROUP_TABLET_SYSTEM_NAME;
                }
            } else {
                deviceGroupDto = null;
                deviceGroupMode = DEVICE_GROUP_ALL;
            }
        }

        // Initialise device MBean
        getDeviceModelsMBean().setPlatformsList(platformsList);
        getDeviceModelsMBean().setDeviceGroupDto(deviceGroupDto);

        LOGGER.debug("loadCampaignDto<--");
    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        campaignDto = dto;

        SegmentDto segment = campaignDto.getCurrentSegment();
        // Platforms
        Utils.fillSetWithList(getPlatformsList(), segment.getPlatforms());
        // device group
        segment.getDeviceGroups().clear();
        if (deviceGroupDto != null) {
            segment.getDeviceGroups().add(deviceGroupDto);
        }
        LOGGER.debug("Include models: " + include);
        // Included and excluded devices
        segment.getModels().clear();
        segment.getExcludedModels().clear();
        // include models
        if (include == 0) {
            segment.getModels().addAll(getSelectedDeviceModels());
        }
        // exclude models
        else if (include == 1) {
            segment.getExcludedModels().addAll(getSelectedDeviceModels());
        }
        LOGGER.debug("prepareDto<--");
        return dto;
    }

    public void onDeviceGroupChangedEvent(ValueChangeEvent event) {
        LOGGER.debug("onDeviceGroupSelectedEvent-->");
        // remove any models if they don't match the device group targeted
        List<ModelDto> verifiedModels = new ArrayList<ModelDto>();
        String value = (String) event.getNewValue();
        if (StringUtils.isNotBlank(value)) {
            this.deviceGroupDto = getDeviceGroupDtoFromMode(value);
            if (deviceGroupDto == null) {
                LOGGER.debug("all device groups, no models to remove");
                verifiedModels.addAll(getSelectedDeviceModels());
            } else {
                LOGGER.debug("New device group is: " + deviceGroupDto.getSystemName());
                for (ModelDto m : getSelectedDeviceModels()) {
                    if (m.getDeviceGroup() != null) {
                        if (m.getDeviceGroup().equals(deviceGroupDto)) {
                            verifiedModels.add(m);
                        }
                    }
                }
            }
        }
        getSelectedDeviceModels().clear();
        getSelectedDeviceModels().addAll(verifiedModels);
        LOGGER.debug("onDeviceGroupSelectedEvent<--");
    }

    @SuppressWarnings("unchecked")
    public void onPlatformSelectedEvent(ValueChangeEvent event) {
        LOGGER.debug("onPlatformSelectedEvent-->");
        List<PlatformDto> value = (List<PlatformDto>) event.getNewValue();
        List<ModelDto> verifiedModels = new ArrayList<ModelDto>();

        if (CollectionUtils.isEmpty(value)) {
            LOGGER.debug("No new models (remove)");
            verifiedModels.addAll(getSelectedDeviceModels());
        } else {
            for (ModelDto m : getSelectedDeviceModels()) {
                for (PlatformDto p : m.getPlatforms()) {
                    if (value.contains(p)) {
                        verifiedModels.add(m);
                        break;
                    }
                }
            }
        }
        getSelectedDeviceModels().clear();
        getSelectedDeviceModels().addAll(verifiedModels);
        LOGGER.debug("onPlatformSelectedEvent<--");
    }

    public String getDevicesSummary(boolean spaces) {
        if (campaignDto != null) {
            String space = "";
            if (spaces) {
                space = " ";
            }
            String message = "";
            if (campaignDto.getCurrentSegment() != null && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getModels())) {
                if (include == 1) {
                    message = "Excluded:" + space;
                }
                for (ModelDto m : campaignDto.getCurrentSegment().getModels()) {
                    if (spaces) {
                        message += m.getVendor().getName() + " ";
                    }
                    message += m.getName() + "," + space;
                }
                message = message.substring(0, message.length() - (1 + space.length()));
                return message;
            }
            if (campaignDto.getCurrentSegment() != null && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getExcludedModels())) {
                if (include == 1) {
                    message = "Excluded:" + space;
                }
                for (ModelDto m : campaignDto.getCurrentSegment().getExcludedModels()) {
                    if (spaces) {
                        message += m.getVendor().getName() + " ";
                    }
                    message += m.getName() + "," + space;
                }
                message = message.substring(0, message.length() - (1 + space.length()));
                return message;
            }

            return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
        }
        return notSet();
    }

    public String getDeviceGroupsSummary(boolean spaces) {
        if (campaignDto != null) {
            if (campaignDto.getCurrentSegment() != null && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getDeviceGroups())) {
                DeviceGroupDto deviceGroupDto = campaignDto.getCurrentSegment().getDeviceGroups().iterator().next();
                if (deviceGroupDto.getSystemName().equals(DEVICE_GROUP_MOBILE_SYSTEM_NAME)) {
                    deviceGroupMode = DEVICE_GROUP_MOBILE_SYSTEM_NAME;
                    return FacesUtils.getBundleMessage("page.campaign.targeting.platformdevice.devicegroup.mobile");
                } else {
                    return FacesUtils.getBundleMessage("page.campaign.targeting.platformdevice.devicegroup.tablet");
                }
            }
            return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
        }
        return notSet();
    }

    public String getPlatformsSummary(boolean spaces) {
        if (campaignDto != null) {
            String space = "";
            if (spaces) {
                space = " ";
            }
            String message = "";
            if (campaignDto.getCurrentSegment() != null && !CollectionUtils.isEmpty(platformsList)) {

                for (PlatformDto p : platformsList) {
                    message += p.getName() + "," + space;
                }
                message = message.substring(0, message.length() - (1 + space.length()));
                return message;
            }
            return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
        }
        return notSet();
    }

    public boolean dataChanged() {
        // Changes are relevant only if platform was ios or android only and has
        // changed
        List<PlatformDto> segmentPlatforms = new ArrayList<PlatformDto>(campaignDto.getCurrentSegment().getPlatforms());
        if (campaignDto.getCurrentSegment().getPlatforms().size() == 1
                && (segmentPlatforms.get(0).getSystemName().equals("ios") || segmentPlatforms.get(0).getSystemName().equals("android"))) {
            if (platformsList.size() != 1) {
                return true;
            }
            if ((segmentPlatforms.get(0).getSystemName().equals("ios") && !getPlatformsList().get(0).getSystemName().equals("ios"))
                    || (segmentPlatforms.get(0).getSystemName().equals("android") && !getPlatformsList().get(0).getSystemName()
                            .equals("android"))) {
                return true;
            }
        }
        return false;
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public List<PlatformDto> getAllPlatForms() {
        if (CollectionUtils.isEmpty(allPlatForms)) {
            allPlatForms = publicationService.getAllOrderedPlatforms(false);
        }
        return allPlatForms;
    }

    public void setAllPlatForms(List<PlatformDto> allPlatForms) {
        this.allPlatForms = allPlatForms;
    }

    public List<PlatformDto> getPlatformsList() {
        if (platformsList == null) {
            platformsList = new ArrayList<PlatformDto>();
        }
        return platformsList;
    }

    public void setPlatformsList(List<PlatformDto> platformsList) {
        this.platformsList = platformsList;
    }

    public int getInclude() {
        return include;
    }

    public void setInclude(int include) {
        this.include = include;
    }

    public String getDeviceGroupMode() {
        return this.deviceGroupMode;
    }

    public void setDeviceGroupMode(String deviceGroupMode) {
        this.deviceGroupMode = deviceGroupMode;
    }

    // Delegated getters / setters

    public List<ModelDto> getSelectedDeviceModels() {
        return getDeviceModelsMBean().getSelectedDeviceModels();
    }

    public void setSelectedDeviceModels(List<ModelDto> modelsList) {
        getDeviceModelsMBean().setSelectedDeviceModels(modelsList);
    }

    public DeviceModelsMBean getDeviceModelsMBean() {
        return deviceModelsMBean;
    }

    // PRIVATE METHODS

    private DeviceGroupDto getDeviceGroupDtoFromMode(String deviceGroupMode) {
        if (StringUtils.isNotBlank(deviceGroupMode)) {
            switch (deviceGroupMode) {
            case DEVICE_GROUP_MOBILE_SYSTEM_NAME:
                return getToolsApplicationBean().getMobileDeviceGroup();
            case DEVICE_GROUP_TABLET_SYSTEM_NAME:
                return getToolsApplicationBean().getTabletDeviceGroup();
            default:
                return null;
            }
        }
        return null;
    }
}

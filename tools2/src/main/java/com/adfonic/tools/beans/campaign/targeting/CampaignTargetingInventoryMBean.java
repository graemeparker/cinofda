package com.adfonic.tools.beans.campaign.targeting;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.targetpublisher.TargetPublisherDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.targetpublisher.TargetPublisherService;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignTargetingInventoryMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTargetingInventoryMBean.class);

    private static final long NONRTB_NETWORK_ADFONIC_ID = Long.MAX_VALUE;
    private static TargetPublisherDto adfonicNetwork = getAdfonicNetwork();

    @Autowired
    private TargetPublisherService targetPublisherService;

    private CampaignDto campaignDto;

    private List<TargetPublisherDto> nonRtbList = null;
    private List<TargetPublisherDto> rtbList = null;
    private List<TargetPublisherDto> rtbListForPmp = null;

    private List<TargetPublisherDto> rtb = null;
    private List<TargetPublisherDto> nonRtb = null;

    private boolean includeAdfonicNetwork;

    @Override
    public void init() {

    }

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");

        this.campaignDto = dto;

        if (campaignDto != null) {
            List<TargetPublisherDto> dtoRtb = campaignDto.getRtb();
            List<TargetPublisherDto> dtoNonRtb = campaignDto.getNonRtb();
            boolean emptyRtb = CollectionUtils.isEmpty(dtoRtb);
            boolean emptyNonRtb = CollectionUtils.isEmpty(dtoNonRtb);

            if (emptyRtb && emptyNonRtb) {
                this.rtb = new ArrayList<TargetPublisherDto>(this.getRtbList());
                this.nonRtb = new ArrayList<TargetPublisherDto>(this.getNonRtbList());
            } else {
                // Loading rtb
                if (!emptyRtb) {
                    this.rtb = new ArrayList<TargetPublisherDto>(dtoRtb);
                }

                // Loading non rtb
                if (!emptyNonRtb) {
                    this.nonRtb = new ArrayList<TargetPublisherDto>();
                    this.includeAdfonicNetwork = campaignDto.getCurrentSegment().isIncludeAdfonicNetwork();
                    if (includeAdfonicNetwork) {
                        this.nonRtb.add(CampaignTargetingInventoryMBean.adfonicNetwork);
                    }
                    this.nonRtb.addAll(dtoNonRtb);
                }
            }

        }
        LOGGER.debug("loadCampaignDto<--");
    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");

        campaignDto = dto;

        // Users without tech fee only get RTB inventory
        boolean hasTechFee = getUserSessionBean().hasTechFee();
        if (hasTechFee) {
            if (nonRtb != null) {
                this.nonRtb.clear();
                dto.setNonRtb(nonRtb);
            }
            dto.getCurrentSegment().setIncludeAdfonicNetwork(false);
        }
        // non rtb only for non tech fee
        else {
            // IncludeAdfonicNetwork flag
            if (CollectionUtils.isNotEmpty(this.nonRtb)) {
                boolean hasAdfonicNetwork = this.nonRtb.contains(CampaignTargetingInventoryMBean.adfonicNetwork);
                dto.getCurrentSegment().setIncludeAdfonicNetwork(hasAdfonicNetwork);

                if (hasAdfonicNetwork) {
                    this.nonRtb.remove(CampaignTargetingInventoryMBean.adfonicNetwork);
                }
            } else {
                dto.getCurrentSegment().setIncludeAdfonicNetwork(false);
            }
        }

        // RTB
        // something is selected OR rtbis empty but we have nonRtb..which is
        // fine, will clear rtb from dto
        dto.getRtb().clear();
        dto.getRtb().addAll(this.rtb);

        // Non RTB
        if (!hasTechFee) {
            // check for non rtb and rtb and add everything. we need all the
            // lists to be empty to do an add all.
            dto.getNonRtb().clear();
            dto.getNonRtb().addAll(this.nonRtb);
        }

        LOGGER.debug("prepareDto<--");
        return dto;
    }

    public boolean isValid() {
        boolean isValid = true;

        // Check if there are" any network selected
        if (CollectionUtils.isEmpty(this.rtb) && CollectionUtils.isEmpty(this.nonRtb)) {
            boolean hasTechFee = getUserSessionBean().hasTechFee();
            LOGGER.debug("Invalid networks selection");
            String componentId = "nonRtbLabel";
            if (hasTechFee) {
                componentId = "rtbLabel";
            }
            addFacesMessage(FacesMessage.SEVERITY_ERROR, componentId, null, "page.campaign.targeting.inventory.error.nonetworkselected");
            isValid = false;
        }
        return isValid;
    }

    public void cleanBean() {
        this.rtb = null;
        this.nonRtb = null;
    }

    public String getInventorySummary(boolean spaces) {
        String space = "";
        if (spaces) {
            space = " ";
        }

        StringBuffer finalMessage = new StringBuffer("");

        if (campaignDto != null) {

            // Catching information from domain
            List<TargetPublisherDto> selectedRtb = campaignDto.getRtb();
            List<TargetPublisherDto> selectedNonRtb = campaignDto.getNonRtb();
            boolean emptyRtb = CollectionUtils.isEmpty(selectedRtb);
            boolean emptyNonRtb = CollectionUtils.isEmpty(selectedNonRtb);

            StringBuffer rtbMessage = new StringBuffer("");
            StringBuffer nonRtbMessage = new StringBuffer("");

            // RTB Summary
            if (emptyRtb && emptyNonRtb) {
                finalMessage.append(notSet());
            } else {
                if (!emptyRtb) {
                    if (getRtbList().size() == selectedRtb.size()) {
                        rtbMessage.append(FacesUtils.getBundleMessage("page.campaign.targeting.menu.allrtb.label"));
                    } else {
                        TargetPublisherDto s = null;
                        for (int cnt = 0; cnt < selectedRtb.size(); cnt++) {
                            s = selectedRtb.get(cnt);
                            rtbMessage.append(s.getName().trim());
                            if (cnt != selectedRtb.size() - 1) {
                                rtbMessage.append(",").append(space);
                            }
                        }
                    }
                } else {
                    rtbMessage.append(FacesUtils.getBundleMessage("page.campaign.targeting.menu.nonesetrtb.label"));
                }

                // NonRTB Summary, users without tech fee only get RTB inventory
                if (!emptyNonRtb && !getUserSessionBean().hasTechFee()) {
                    boolean includeAdfonicNetwork = campaignDto.getCurrentSegment().isIncludeAdfonicNetwork();
                    if (((getNonRtbList().size() - 1) == selectedNonRtb.size()) && includeAdfonicNetwork) {
                        nonRtbMessage.append(FacesUtils.getBundleMessage("page.campaign.targeting.menu.allnonrtb.label"));
                    } else {
                        TargetPublisherDto s = null;
                        for (int cnt = 0; cnt < selectedNonRtb.size(); cnt++) {
                            s = selectedNonRtb.get(cnt);
                            nonRtbMessage.append(s.getName().trim());
                            if (cnt != selectedNonRtb.size() - 1) {
                                nonRtbMessage.append(",").append(space);
                            }
                        }

                        if (includeAdfonicNetwork) {
                            nonRtbMessage.append(",").append(space)
                                    .append(FacesUtils.getBundleMessage("page.campaign.targeting.inventory.nonrtb.adfonic.label"));
                        }
                    }
                } else {
                    nonRtbMessage.append(FacesUtils.getBundleMessage("page.campaign.targeting.menu.nonesetnonrtb.label"));
                }

                finalMessage.append(rtbMessage);
                if (!getUserSessionBean().hasTechFee()) {
                    finalMessage.append(";").append(space).append(nonRtbMessage);
                }
            }

        } else {
            finalMessage.append(notSet());
        }

        return finalMessage.toString();
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public List<TargetPublisherDto> getNonRtbList() {
        if (this.nonRtbList == null) {
            List<TargetPublisherDto> targetPublisherNonRtbList = (List<TargetPublisherDto>) targetPublisherService.getAllTargetPublishers(false, false);
            this.nonRtbList = new ArrayList<TargetPublisherDto>(targetPublisherNonRtbList.size() + 1);
            this.nonRtbList.add(CampaignTargetingInventoryMBean.adfonicNetwork);
            this.nonRtbList.addAll(targetPublisherNonRtbList);
        }
        return nonRtbList;
    }

    public void setNonRtbList(List<TargetPublisherDto> nonRtbList) {
        this.nonRtbList = nonRtbList;
    }

    public List<TargetPublisherDto> getRtbList() {
        if (this.rtbList == null) {
            this.rtbList = (List<TargetPublisherDto>) targetPublisherService.getAllTargetPublishers(true, false);
        }
        return this.rtbList;
    }

    public List<TargetPublisherDto> getRtbListForPmp() {
        if (this.rtbListForPmp == null) {
            this.rtbListForPmp = (List<TargetPublisherDto>) targetPublisherService.getAllTargetPublishersForPmp(true, true, false);
        }
        return this.rtbListForPmp;
    }

    public void setRtbList(List<TargetPublisherDto> rtbList) {
        this.rtbList = rtbList;
    }

    public List<TargetPublisherDto> getRtb() {
        return this.rtb;
    }

    public void setRtb(List<TargetPublisherDto> rtb) {
        this.rtb = rtb;
    }

    public List<TargetPublisherDto> getNonRtb() {
        return nonRtb;
    }

    public void setNonRtb(List<TargetPublisherDto> nonRtb) {
        this.nonRtb = nonRtb;
    }

    public static TargetPublisherDto getAdfonicNetwork() {
        if (adfonicNetwork == null) {
            adfonicNetwork = new TargetPublisherDto();
            adfonicNetwork.setId(NONRTB_NETWORK_ADFONIC_ID);
            adfonicNetwork.setName(FacesUtils.getBundleMessage("page.campaign.targeting.inventory.nonrtb.adfonic.label"));
        }
        return adfonicNetwork;
    }

    public boolean isIncludeAdfonicNetwork() {
        return includeAdfonicNetwork;
    }

    public void setIncludeAdfonicNetwork(boolean includeAdfonicNetwork) {
        this.includeAdfonicNetwork = includeAdfonicNetwork;
    }
    
    public boolean isSelectAllRtbCheckbox() {
        // #{fn:length(campaignMBean.campaignInventoryTargetingMBean.campaignTargetingInventoryMBean.rtb) eq fn:length(campaignMBean.campaignInventoryTargetingMBean.campaignTargetingInventoryMBean.rtbList)}
        int rtbSize = (getRtb()==null ? 0 : getRtb().size());
        return (getRtbList().size() == rtbSize);
    }

    public void setSelectAllRtbCheckbox(boolean selectAllRtbCheckbox) {
    }

    public boolean isSelectAllNonrtbCheckbox() {
        // #{fn:length(campaignMBean.campaignInventoryTargetingMBean.campaignTargetingInventoryMBean.nonRtb) eq fn:length(campaignMBean.campaignInventoryTargetingMBean.campaignTargetingInventoryMBean.nonRtbList)}
        int nonRtbSize = (getNonRtb()==null ? 0 : getNonRtb().size());
        return (getNonRtbList().size() == nonRtbSize);
    }

    public void setSelectAllNonrtbCheckbox(boolean selectAllNonrtbCheckbox) {
    }
}

package com.adfonic.tools.beans.campaign.inventory;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.category.CategoryDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.channel.ChannelService;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignInventoryCategoriesMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignInventoryCategoriesMBean.class);

    @Autowired
    private ChannelService channelService;

    private CampaignDto campaignDto;

    private List<CategoryDto> categories = null;

    private List<CategoryDto> targetedCategories = new ArrayList<CategoryDto>(0);

    @Override
    public void init() {
    };

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = dto;
        if (getCampaignDto() != null && !CollectionUtils.isEmpty(getCampaignDto().getCurrentSegment().getIncludedCategories())) {
            if (getCampaignDto().getCurrentSegment().getIncludedCategories().size() < getCategories().size()) {
                targetedCategories.addAll(getCampaignDto().getCurrentSegment().getIncludedCategories());
            } else if (getCampaignDto().getCurrentSegment().getIncludedCategories().size() >= getCategories().size()) {
                targetedCategories.addAll(getCategories());
            }
        } else if (CollectionUtils.isEmpty(targetedCategories) && !CollectionUtils.isEmpty(getCategories())) {
            targetedCategories.addAll(getCategories());
        }
        LOGGER.debug("loadCampaignDto<--");
    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        campaignDto = dto;

        if (CollectionUtils.isEmpty(targetedCategories)) {
            dto.getCurrentSegment().getIncludedCategories().clear();
            dto.getCurrentSegment().getIncludedCategories().addAll(getCategories());
        } else {
            dto.getCurrentSegment().getIncludedCategories().clear();
            dto.getCurrentSegment().getIncludedCategories().addAll(targetedCategories);
        }

        LOGGER.debug("prepareDto<--");
        return dto;
    }

    public boolean allCategories() {
        if (targetedCategories.size() == getCategories().size() || CollectionUtils.isEmpty(targetedCategories)) {
            return true;
        }
        return false;
    }

    public String getCategoriesSummary(boolean spaces) {
        List<CategoryDto> categories = new ArrayList<CategoryDto>();
        if (campaignDto != null) {
            categories.addAll(campaignDto.getCurrentSegment().getIncludedCategories());
        }
        if (!CollectionUtils.isEmpty(categories)) {
            String space = "";
            if (spaces) {
                space = " ";
            }
            if (allCategories()) {
                return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
            }
            String message = "";

            for (CategoryDto c : categories) {
                message += c.getName() + "," + space;
            }
            message = message.substring(0, message.length() - (1 + space.length()));
            return message;
        }
        return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
    }

    public void cleanBean() {
        this.targetedCategories.clear();
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    /***
     * Get all channel Dto
     * **/
    public Collection<CategoryDto> getCategories() {
        // This needs to exclude the NOT_CATEGORIED_CHANNEL
        if (categories == null) {
            categories = channelService.getCategories();
        }
        return categories;
    }

    public List<CategoryDto> getTargetedChannels() {
        if (targetedCategories == null) {
            targetedCategories = new ArrayList<CategoryDto>();
        }
        return targetedCategories;
    }

    public void setTargetedChannels(List<CategoryDto> targetedCategories) {
        this.targetedCategories = targetedCategories;
    }

    public boolean includeUncategorized() {
        return ((CollectionUtils.isEmpty(targetedCategories)) || (!CollectionUtils.isEmpty(targetedCategories)
                && !CollectionUtils.isEmpty(categories) && targetedCategories.size() == categories.size()));
    }

    public boolean isValid() {
        boolean isValid = true;

        // Check if there are" any network selected
        if (CollectionUtils.isEmpty(targetedCategories)) {
            LOGGER.debug("Empty categories");
            String componentId = "iabCategorySelection";
            addFacesMessage(FacesMessage.SEVERITY_ERROR, componentId, null, "page.campaign.targeting.inventory.error.nocategorieselected");
            isValid = false;
        }
        return isValid;
    }
}

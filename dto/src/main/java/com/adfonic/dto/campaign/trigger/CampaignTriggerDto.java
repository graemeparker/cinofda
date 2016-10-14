package com.adfonic.dto.campaign.trigger;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;
import com.adfonic.dto.campaign.enums.PluginType;

public class CampaignTriggerDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @Source(value = "pluginVendor")
    private PluginVendorDto pluginVendor;

    @Source(value = "pluginType")
    private PluginType pluginType;

    public PluginVendorDto getPluginVendor() {
        return pluginVendor;
    }

    public void setPluginVendor(PluginVendorDto pluginVendor) {
        this.pluginVendor = pluginVendor;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }
}

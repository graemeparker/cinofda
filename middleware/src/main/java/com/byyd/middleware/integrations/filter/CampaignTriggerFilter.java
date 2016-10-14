package com.byyd.middleware.integrations.filter;

import java.util.Collection;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTrigger.PluginType;
import com.adfonic.domain.PluginVendor;

public class CampaignTriggerFilter {

    private Campaign campaign;
    private PluginVendor pluginVendor;
    private Collection<PluginType> pluginTypes;
    private Boolean deleted;
    
    public Campaign getCampaign() {
        return campaign;
    }
    
    public CampaignTriggerFilter setCampaign(Campaign campaign) {
        this.campaign = campaign;
        return this;
    }
    
    public PluginVendor getPluginVendor() {
        return pluginVendor;
    }
    
    public CampaignTriggerFilter setPluginVendor(PluginVendor pluginVendor) {
        this.pluginVendor = pluginVendor;
        return this;
    }
    
    public Collection<PluginType> getPluginTypes() {
        return pluginTypes;
    }
    
    public CampaignTriggerFilter setPluginTypes(Collection<PluginType> pluginTypes) {
        this.pluginTypes = pluginTypes;
        return this;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public CampaignTriggerFilter setDeleted(Boolean deleted) {
        this.deleted = deleted;
        return this;
    }
}

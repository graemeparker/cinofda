package com.byyd.middleware.integrations.service;

import java.util.List;
import java.util.Set;

import com.adfonic.domain.AdserverPlugin;
import com.adfonic.domain.AdserverShard;
import com.adfonic.domain.AdserverStatus;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTrigger;
import com.adfonic.domain.PluginVendor;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;
import com.byyd.middleware.integrations.filter.CampaignTriggerFilter;

public interface IntegrationsManager extends BaseManager {
    
    //------------------------------------------------------------------------------------------
    // AdserverStatus
    //------------------------------------------------------------------------------------------
    List<AdserverStatus> getAllStatuses(FetchStrategy ... fetchStrategy);
    AdserverStatus update(AdserverStatus adserverStatus);
    void updateAllStatuses(List<AdserverStatus> statuses);

    //------------------------------------------------------------------------------------------
    // AdserverShard
    //------------------------------------------------------------------------------------------
    List<AdserverShard> getAllShards(FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------
    // AdserverPlugin
    //------------------------------------------------------------------------------
    AdserverPlugin newAdserverPlugin(String name, String systemName, boolean enabled, long expectedResponseTimeMillis, FetchStrategy... fetchStrategy);
    AdserverPlugin getAdserverPluginById(String id, FetchStrategy... fetchStrategy);
    AdserverPlugin getAdserverPluginById(Long id, FetchStrategy... fetchStrategy);
    AdserverPlugin update(AdserverPlugin adserverPlugin);
    void delete(AdserverPlugin adserverPlugin);
    void deleteAdserverPlugins(List<AdserverPlugin> list);

    Long countAllAdserverPlugins();
    List<AdserverPlugin> getAllAdserverPlugins(FetchStrategy ... fetchStrategy);
    List<AdserverPlugin> getAllAdserverPlugins(Sorting sort, FetchStrategy ... fetchStrategy);
    List<AdserverPlugin> getAllAdserverPlugins(Pagination page, FetchStrategy ... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // CampaignTriggers
    //------------------------------------------------------------------------------------------
    
    public CampaignTrigger getCampaignTriggerById(String id, FetchStrategy... fetchStrategy);
    public CampaignTrigger getCampaignTriggerById(Long id, FetchStrategy... fetchStrategy);
    public CampaignTrigger create(CampaignTrigger campaignTrigger);
    public CampaignTrigger update(CampaignTrigger campaignTrigger);
    public void delete(CampaignTrigger campaignTrigger);
    public void deleteCampaignTrigger(List<CampaignTrigger> list);
    public Long countCampaignTriggers(CampaignTriggerFilter filter);
    public List<CampaignTrigger> getCampaignTriggers(CampaignTriggerFilter filter, FetchStrategy... fetchStrategy);
    public List<CampaignTrigger> getCampaignTriggers(CampaignTriggerFilter filter, Sorting sort, FetchStrategy... fetchStrategy);
    public List<CampaignTrigger> getCampaignTriggers(CampaignTriggerFilter filter, Pagination page, FetchStrategy... fetchStrategy);
    public Campaign updateCampaignTriggers(Campaign campaign, Set<CampaignTrigger> newCampaignTriggers);
    
    //------------------------------------------------------------------------------
    // PluginVendor
    //------------------------------------------------------------------------------

    public PluginVendor getPluginVendorById(String id, FetchStrategy... fetchStrategy);
    public PluginVendor getPluginVendorById(Long id, FetchStrategy... fetchStrategy);
    public PluginVendor getPluginVendorByEmail(String email, FetchStrategy... fetchStrategy);
    public PluginVendor create(PluginVendor pluginVendor);
    public PluginVendor update(PluginVendor pluginVendor);
    public void delete(PluginVendor pluginVendor);
    public List<PluginVendor> getAllPluginVendors();
}

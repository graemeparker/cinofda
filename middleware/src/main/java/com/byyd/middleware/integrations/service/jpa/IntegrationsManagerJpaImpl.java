package com.byyd.middleware.integrations.service.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdserverPlugin;
import com.adfonic.domain.AdserverShard;
import com.adfonic.domain.AdserverStatus;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTrigger;
import com.adfonic.domain.PluginVendor;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.integrations.dao.AdserverPluginDao;
import com.byyd.middleware.integrations.dao.AdservserShardDao;
import com.byyd.middleware.integrations.dao.AdservserStatusDao;
import com.byyd.middleware.integrations.dao.CampaignTriggerDao;
import com.byyd.middleware.integrations.dao.PluginVendorDao;
import com.byyd.middleware.integrations.filter.CampaignTriggerFilter;
import com.byyd.middleware.integrations.service.IntegrationsManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("integrationsManager")
public class IntegrationsManagerJpaImpl extends BaseJpaManagerImpl implements IntegrationsManager {
    
    @Autowired(required=false)
    private AdservserStatusDao adservserStatusDao;
    
    @Autowired(required=false)
    private AdservserShardDao adservserShardDao;
    
    @Autowired(required = false)
    private AdserverPluginDao adserverPluginDao;
    
    @Autowired(required=false)
    private CampaignTriggerDao campaignTriggerDao;
    
    @Autowired(required=false)
    private PluginVendorDao pluginVendorDao;
    
    @Override
    @Transactional(readOnly=true)
    public List<AdserverStatus> getAllStatuses(FetchStrategy ... fetchStrategy) {
        return adservserStatusDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public AdserverStatus update(AdserverStatus adserverStatus) {
        return adservserStatusDao.update(adserverStatus);
    }

    @Override
    @Transactional(readOnly=false)
    public void updateAllStatuses(List<AdserverStatus> statuses) {
        if(statuses == null ||statuses.isEmpty()) {
            return;
        }
        for(AdserverStatus adserverStatus : statuses) {
            update(adserverStatus);
        }
    }

    //------------------------------------------------------------------------------------------
    // AdserverShard
    //------------------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly=true)
    public List<AdserverShard> getAllShards(FetchStrategy ... fetchStrategy) {
        return adservserShardDao.getAll(fetchStrategy);
    }

    // ------------------------------------------------------------------------------
    // AdserverPlugin
    // ------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public AdserverPlugin newAdserverPlugin(String name, String systemName, boolean enabled, long expectedResponseTimeMillis, FetchStrategy... fetchStrategy) {
        AdserverPlugin plugin = new AdserverPlugin();
        plugin.setName(name);
        plugin.setSystemName(systemName);
        plugin.setEnabled(enabled);
        plugin.setExpectedResponseTimeMillis(expectedResponseTimeMillis);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(plugin);
        } else {
            plugin = create(plugin);
            return getAdserverPluginById(plugin.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdserverPlugin getAdserverPluginById(String id, FetchStrategy... fetchStrategy) {
        return this.getAdserverPluginById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public AdserverPlugin getAdserverPluginById(Long id, FetchStrategy... fetchStrategy) {
        return adserverPluginDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public AdserverPlugin create(AdserverPlugin adserverPlugin) {
        return adserverPluginDao.create(adserverPlugin);
    }

    @Override
    @Transactional(readOnly = false)
    public AdserverPlugin update(AdserverPlugin adserverPlugin) {
        return adserverPluginDao.update(adserverPlugin);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(AdserverPlugin adserverPlugin) {
        adserverPluginDao.delete(adserverPlugin);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAdserverPlugins(List<AdserverPlugin> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (AdserverPlugin entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllAdserverPlugins() {
        return adserverPluginDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdserverPlugin> getAllAdserverPlugins(FetchStrategy... fetchStrategy) {
        return adserverPluginDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdserverPlugin> getAllAdserverPlugins(Sorting sort, FetchStrategy... fetchStrategy) {
        return adserverPluginDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdserverPlugin> getAllAdserverPlugins(Pagination page, FetchStrategy... fetchStrategy) {
        return adserverPluginDao.getAll(page, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------
    // CampaignTrigger
    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public CampaignTrigger getCampaignTriggerById(String id, FetchStrategy... fetchStrategy) {
        return this.getCampaignTriggerById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public CampaignTrigger getCampaignTriggerById(Long id, FetchStrategy... fetchStrategy) {
        return campaignTriggerDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTrigger create(CampaignTrigger campaignTrigger) {
        return campaignTriggerDao.create(campaignTrigger);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTrigger update(CampaignTrigger campaignTrigger) {
        return campaignTriggerDao.update(campaignTrigger);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(CampaignTrigger campaignTrigger) {
        campaignTriggerDao.delete(campaignTrigger);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteCampaignTrigger(List<CampaignTrigger> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (CampaignTrigger entry : list) {
                delete(entry);
            }
        }   
    }
    
    @Override
    @Transactional(readOnly=true)
    public Long countCampaignTriggers(CampaignTriggerFilter filter) {
        return campaignTriggerDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CampaignTrigger> getCampaignTriggers(CampaignTriggerFilter filter, FetchStrategy... fetchStrategy) {
        return campaignTriggerDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CampaignTrigger> getCampaignTriggers(CampaignTriggerFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return campaignTriggerDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CampaignTrigger> getCampaignTriggers(CampaignTriggerFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return campaignTriggerDao.getAll(filter, page, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=false)
    public Campaign updateCampaignTriggers(Campaign campaign, Set<CampaignTrigger> newCampaignTriggers){
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        Campaign dbCampaign = campaignManager.getCampaignById(campaign.getId());
        
        // Catching old campaign triggers
        Set<CampaignTrigger> oldCampaignTriggers = new HashSet<>(dbCampaign.getCampaignTriggers());
        
        // Clearing all current campaign triggers from Campaign
        dbCampaign.getCampaignTriggers().clear();
        
        for (CampaignTrigger newCampaignTrigger : newCampaignTriggers){
            newCampaignTrigger.setCampaign(dbCampaign);
            
            CampaignTrigger oldCampaignTrigger = searchCampaignTrigger(oldCampaignTriggers, newCampaignTrigger);
            
            if (oldCampaignTrigger != null){ // Exist (update)
                dbCampaign.getCampaignTriggers().add(oldCampaignTrigger);
            }else{
                newCampaignTrigger = create(newCampaignTrigger);
                dbCampaign.getCampaignTriggers().add(newCampaignTrigger);
            }
        }
        
        return campaignManager.update(dbCampaign);
    }
    
    private CampaignTrigger searchCampaignTrigger(Set<CampaignTrigger> campaignTriggers, CampaignTrigger ct){
        CampaignTrigger result = null;
        for (CampaignTrigger element : campaignTriggers){
            if (element.equals(ct)){
                result = element;
                break;
            }
        }
        return result;
    }
    
    //------------------------------------------------------------------------------
    // PluginVendor
    //------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public PluginVendor getPluginVendorById(String id, FetchStrategy... fetchStrategy) {
        return this.getPluginVendorById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public PluginVendor getPluginVendorById(Long id, FetchStrategy... fetchStrategy) {
        return pluginVendorDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public PluginVendor getPluginVendorByEmail(String email, FetchStrategy... fetchStrategy) {
        return pluginVendorDao.getByEmail(email, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=false)
    public PluginVendor create(PluginVendor pluginVendor) {
        return pluginVendorDao.create(pluginVendor);
    }

    @Override
    @Transactional(readOnly=false)
    public PluginVendor update(PluginVendor pluginVendor) {
        return pluginVendorDao.update(pluginVendor);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(PluginVendor pluginVendor) {
        pluginVendorDao.delete(pluginVendor);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<PluginVendor> getAllPluginVendors(){
        Sorting sorting = new Sorting("name");
        return pluginVendorDao.getAll(sorting);
    }
}

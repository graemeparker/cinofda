package com.adfonic.presentation.pluginvendor.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.PluginVendor;
import com.adfonic.dto.campaign.trigger.PluginVendorDto;
import com.adfonic.presentation.pluginvendor.PluginVendorService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.integrations.service.IntegrationsManager;

@Service("pluginVendorService")
public class PluginVendorServiceImpl extends GenericServiceImpl implements PluginVendorService{
    
    @Autowired
    IntegrationsManager integrationsManager;
    
    @Override
    @Transactional(readOnly = true)
    public PluginVendorDto getPluginVendorById(final Long id){
        PluginVendorDto pluginVendorDto = null;
        PluginVendor pluginVendor = integrationsManager.getPluginVendorById(id);
        if(pluginVendor!=null){
            pluginVendorDto = getDtoObject(PluginVendorDto.class, pluginVendor);
        }
        return pluginVendorDto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public PluginVendorDto getPluginVendorById(final String id){
        return getPluginVendorById(Long.getLong(id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PluginVendorDto> getPluginVendors(){
        List<PluginVendor> pluginVendors = integrationsManager.getAllPluginVendors();
        return getDtoList(PluginVendorDto.class, pluginVendors);
    }
}

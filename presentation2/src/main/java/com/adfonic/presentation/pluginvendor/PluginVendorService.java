package com.adfonic.presentation.pluginvendor;

import java.util.List;

import com.adfonic.dto.campaign.trigger.PluginVendorDto;

public interface PluginVendorService {
    public PluginVendorDto getPluginVendorById(final Long id);
    public PluginVendorDto getPluginVendorById(final String value);
    public List<PluginVendorDto> getPluginVendors();
}

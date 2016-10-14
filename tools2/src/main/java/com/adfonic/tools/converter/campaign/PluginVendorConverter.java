package com.adfonic.tools.converter.campaign;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.campaign.trigger.PluginVendorDto;
import com.adfonic.presentation.pluginvendor.PluginVendorService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.campaign.PluginVendorConverter", forClass = com.adfonic.dto.campaign.trigger.PluginVendorDto.class)
public class PluginVendorConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object obj = null;
        if (value != null) {
            PluginVendorService service = getSpringService(context, PluginVendorService.class);
            obj = service.getPluginVendorById(Long.valueOf(value));
        }
        return obj;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String ret = null;
        if (value != null) {
            PluginVendorService service = getSpringService(context, PluginVendorService.class);
            PluginVendorDto dto = service.getPluginVendorById(((PluginVendorDto) value).getId());
            ret = Long.toString(dto.getId());
        }
        return ret;
    }
}

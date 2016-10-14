package com.adfonic.tools.converter.dmpvendor;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.audience.DMPVendorDto;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "dmpVendorConverter", forClass = com.adfonic.dto.audience.DMPVendorDto.class)
public class DMPVendorConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            AudienceService service = getSpringService(context, com.adfonic.presentation.audience.service.AudienceService.class);
            try {
                DMPVendorDto obj = service.getDMPVendorById(Long.valueOf(value), false);
                return obj;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            DMPVendorDto dto = (DMPVendorDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }
}

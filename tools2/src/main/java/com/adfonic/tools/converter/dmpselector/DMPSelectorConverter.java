package com.adfonic.tools.converter.dmpselector;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.audience.DMPSelectorDto;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "dmpSelectorConverter", forClass = com.adfonic.dto.audience.DMPSelectorDto.class)
public class DMPSelectorConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            AudienceService service = getSpringService(context, com.adfonic.presentation.audience.service.AudienceService.class);
            try {
                DMPSelectorDto obj = service.getDMPSelectorById(Long.valueOf(value));
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
            DMPSelectorDto dto = (DMPSelectorDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }
}

package com.adfonic.tools.converter.platform;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.platform.PlatformConverter", forClass = com.adfonic.dto.publication.platform.PlatformDto.class)
public class PlatformConverter extends GenericConverter {

   @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            if (Long.valueOf(value) != -1l) {
                PublicationService service = getSpringService(context, com.adfonic.presentation.publication.service.PublicationService.class);
                return service.getPlatformById(Long.valueOf(value));
            } else {
                PlatformDto dto = new PlatformDto();
                dto.setId(Long.valueOf(-1));
                dto.setName("All");
                return dto;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        PlatformDto dto = (PlatformDto) value;
        if (dto != null) {
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }

}

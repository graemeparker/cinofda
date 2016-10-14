package com.adfonic.tools.converter.creative.creativeattribute;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.campaign.creative.CreativeAttributeDto;
import com.adfonic.presentation.campaign.creative.CreativeService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.creative.creativeattribute.CreativeAttributeConverter", forClass = com.adfonic.dto.campaign.creative.CreativeAttributeDto.class)
public class CreativeAttributeConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            CreativeService service = getSpringService(context, com.adfonic.presentation.campaign.creative.CreativeService.class);
            try {
                CreativeAttributeDto creativeDto = service.getCreativeAttributeById(Long.valueOf(value));
                return creativeDto;
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
            CreativeAttributeDto dto = (CreativeAttributeDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }
}

package com.adfonic.tools.converter.format;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.format.FormatDto;
import com.adfonic.presentation.format.FormatService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.format.FormatConverter", forClass = com.adfonic.dto.format.FormatDto.class)
public class FormatConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            FormatService service = getSpringService(context, com.adfonic.presentation.format.FormatService.class);
            FormatDto dto = service.getFormatById(Long.valueOf(value));
            return dto;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            FormatDto dto = (FormatDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }

}

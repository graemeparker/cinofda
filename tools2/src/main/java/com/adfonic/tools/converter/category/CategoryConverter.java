package com.adfonic.tools.converter.category;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.category.CategoryDto;
import com.adfonic.presentation.campaign.channel.ChannelService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.channel.CategoryConverter", forClass = com.adfonic.dto.category.CategoryDto.class)
public class CategoryConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            ChannelService service = getSpringService(context, com.adfonic.presentation.campaign.channel.ChannelService.class);
            CategoryDto dto = service.getCategoryById(Long.valueOf(value));
            return dto;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            CategoryDto dto = (CategoryDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }

}

package com.adfonic.tools.converter.channel;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.channel.ChannelDto;
import com.adfonic.presentation.campaign.channel.ChannelService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.channel.ChannelConverter", forClass = com.adfonic.dto.channel.ChannelDto.class)
public class ChannelConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            ChannelService service = getSpringService(context, com.adfonic.presentation.campaign.channel.ChannelService.class);
            ChannelDto dto = service.getChannelById(Long.valueOf(value));
            return dto;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            ChannelDto dto = (ChannelDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }

}

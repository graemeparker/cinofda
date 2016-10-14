package com.adfonic.tools.converter.targetpublisher;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.targetpublisher.TargetPublisherDto;
import com.adfonic.presentation.targetpublisher.TargetPublisherService;
import com.adfonic.tools.beans.campaign.targeting.CampaignTargetingInventoryMBean;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.targetpublisher.TargetPublisherConverter", forClass = com.adfonic.dto.targetpublisher.TargetPublisherDto.class)
public class TargetPublisherConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        TargetPublisherDto dto = null;
        if (value != null) {
            long lValue = Long.valueOf(value);

            if (lValue != CampaignTargetingInventoryMBean.getAdfonicNetwork().getId()) {
                TargetPublisherService service = getSpringService(context,
                        com.adfonic.presentation.targetpublisher.TargetPublisherService.class);
                dto = service.getTargetPublisherById(Long.valueOf(value));
            } else {
                dto = CampaignTargetingInventoryMBean.getAdfonicNetwork();
            }
        }
        return dto;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            TargetPublisherDto dto = (TargetPublisherDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }
}

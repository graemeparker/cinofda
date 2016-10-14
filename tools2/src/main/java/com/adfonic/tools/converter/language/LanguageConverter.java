package com.adfonic.tools.converter.language;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.language.LanguageDto;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.language.LanguageConverter", forClass = com.adfonic.dto.language.LanguageDto.class)
public class LanguageConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            if (value.equals("------------")) {
                return new LanguageDto();
            }
            CampaignService service = getSpringService(context, com.adfonic.presentation.campaign.CampaignService.class);
            return service.getLanguageById(Long.valueOf(value));
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        LanguageDto dto = (LanguageDto) value;
        if (dto != null) {
            if (dto.getId() == null) {
                return "------------";
            }
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }

}

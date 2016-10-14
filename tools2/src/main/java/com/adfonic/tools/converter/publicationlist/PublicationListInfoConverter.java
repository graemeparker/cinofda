package com.adfonic.tools.converter.publicationlist;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.publicationlist.service.PublicationListService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.publicationlist.PublicationListInfoConverter", forClass = com.adfonic.dto.campaign.publicationlist.PublicationListInfoDto.class)
public class PublicationListInfoConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            if (value.equals(FacesUtils.getBundleMessage("page.campaign.inventory.select.emptylist.label"))) {
                return null;
            }
            PublicationListService service = getSpringService(context,
                    com.adfonic.presentation.publicationlist.service.PublicationListService.class);
            return service.getPublicationListInfoById(Long.valueOf(value));
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null && ((NameIdBusinessDto) value).getId() != null) {
            NameIdBusinessDto dto = (NameIdBusinessDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }

}

package com.adfonic.tools.converter.publication;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.publication.search.PublicationSearchDto;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.publication.PublicationConverter", forClass = com.adfonic.dto.publication.enums.PublicationStatus.class)
public class PublicationConverter extends GenericConverter {

@Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            PublicationService service = getSpringService(context, com.adfonic.presentation.publication.service.PublicationService.class);
            PublicationSearchDto dto = new PublicationSearchDto();
            dto.setId(Long.valueOf(value));
            // dto.setName(value);
            // UserSessionBean bean = Utils.findBean(context,
            // Constants.USER_SESSION_BEAN);
            // UserDTO userDto = (UserDTO) bean.getMap().get(Constants.USERDTO);
            // dto.setPublisher(userDto.getPublisherDto());
            return service.getPublicationById(dto);
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            NameIdBusinessDto dto = (NameIdBusinessDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }

}

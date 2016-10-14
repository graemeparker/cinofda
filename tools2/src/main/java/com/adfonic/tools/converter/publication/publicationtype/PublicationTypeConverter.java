package com.adfonic.tools.converter.publication.publicationtype;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.publication.publicationtype.PublicationTypeConverter", forClass = com.adfonic.dto.publication.publicationtype.PublicationtypeDto.class)
public class PublicationTypeConverter extends GenericConverter {

 @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            if (Long.valueOf(value) != -1l) {
                PublicationService service = getSpringService(context, com.adfonic.presentation.publication.service.PublicationService.class);
                PublicationtypeDto dto = service.getPublicationTypeById(Long.valueOf(value));
                return dto;
            } else {
                PublicationtypeDto dto = new PublicationtypeDto();
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
        if (value != null) {
            PublicationtypeDto dto = (PublicationtypeDto) value;
            if (dto.getId() != null) {
                return Long.toString(dto.getId());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}

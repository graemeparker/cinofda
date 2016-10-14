package com.adfonic.tools.converter.operator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.operator.OperatorAutocompleteDto;
import com.adfonic.presentation.operator.OperatorService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.operator.OperatorConverter", forClass = com.adfonic.dto.operator.OperatorAutocompleteDto.class)
public class OperatorConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            OperatorService service = getSpringService(context, com.adfonic.presentation.operator.OperatorService.class);
            OperatorAutocompleteDto obj = service.getOperatorAutocompleteById(Long.valueOf(value));
            return obj;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        OperatorAutocompleteDto dto = (OperatorAutocompleteDto) value;
        if (dto != null) {
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }

}

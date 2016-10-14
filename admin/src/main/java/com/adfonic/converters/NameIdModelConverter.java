package com.adfonic.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;

import com.adfonic.presentation.NameIdModel;

@FacesConverter(value="nameIdModelConverter")
public class NameIdModelConverter extends BaseConverter {

    private static final String SEPARATOR = "#";

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        NameIdModel nameIdModel = new NameIdModel();
        nameIdModel.setId(0L);
        nameIdModel.setName(value);
        return nameIdModel;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null && value instanceof NameIdModel) {
            NameIdModel dto = (NameIdModel) value;
            return dto.getId() + SEPARATOR + dto.getName();
        }
        return (value == null) ? StringUtils.EMPTY : value.toString();
    }

}

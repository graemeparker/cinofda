package com.adfonic.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * @author jon
 */
@FacesConverter(value="stringTrimConverter")
public class StringTrimConverter implements Converter {

    public StringTrimConverter() {}

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            return ((String)value).trim();
        }
        else {
            return "";
        }
    }
}

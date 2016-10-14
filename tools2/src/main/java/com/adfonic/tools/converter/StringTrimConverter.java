package com.adfonic.tools.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;

/**
 * @author jon
 */
@FacesConverter(value = "stringTrimConverter")
public class StringTrimConverter implements Converter {

    public StringTrimConverter() {
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            return ((String) value).trim();
        } else {
            return StringUtils.EMPTY;
        }
    }
}

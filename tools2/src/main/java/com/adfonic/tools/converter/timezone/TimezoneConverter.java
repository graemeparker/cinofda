package com.adfonic.tools.converter.timezone;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.tools.converter.GenericConverter;
import com.adfonic.util.AdfonicTimeZone;

@FacesConverter("com.adfonic.tools.converter.timezone.TimezoneConverter")
public class TimezoneConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String description) {
        AdfonicTimeZone result = null;

        for (AdfonicTimeZone timezone : AdfonicTimeZone.values()) {
            if (timezone.getDescription().equals(description)) {
                result = timezone;
                break;
            }
        }
        return result;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String result = null;
        if (value instanceof AdfonicTimeZone) {
            result = ((AdfonicTimeZone) value).getDescription();
        }
        return result;
    }

}

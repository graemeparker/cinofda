package com.adfonic.tools.converter.campaign;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.campaign.enums.SegmentSafetyLevel;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter("com.adfonic.tools.converter.campaign.SegmentSafetyLevelConverter")
public class SegmentSafetyLevelConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return SegmentSafetyLevel.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String result = null;
        if (value instanceof String) {
            SegmentSafetyLevel safetyLevel = SegmentSafetyLevel.valueOf((String) value);
            if (safetyLevel != null) {
                result = safetyLevel.name();
            }
        }
        return result;
    }

}

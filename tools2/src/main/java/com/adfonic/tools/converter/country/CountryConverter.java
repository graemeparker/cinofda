package com.adfonic.tools.converter.country;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.country.CountryDto;
import com.adfonic.presentation.location.LocationService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "com.adfonic.tools.converter.country.CountryConverter", forClass = com.adfonic.dto.country.CountryDto.class)
public class CountryConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            LocationService service = getSpringService(context, com.adfonic.presentation.location.LocationService.class);
            Object obj = service.getCountryById(Long.valueOf(value));
            return obj;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            CountryDto dto = (CountryDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }
}

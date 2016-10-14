package com.adfonic.adserver.web.jsf.managed.beans;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.CountryDto;

public class CountryConvertor implements Converter {

    DomainCacheManager domainCacheManager;

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return null;
        } else {
            try {

                long countryId = Long.parseLong(submittedValue);
                CountryDto country = domainCacheManager.getCache().getCountryById(countryId);
                return country;
            } catch (NumberFormatException exception) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid player"));
            }
        }

    }

    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object country) {
        if (country == null || "".equals(country)) {
            return "";
        }
        return String.valueOf(((CountryDto) country).getId());
    }

    public DomainCacheManager getDomainCacheManager() {
        return domainCacheManager;
    }

    public void setDomainCacheManager(DomainCacheManager domainCacheManager) {
        this.domainCacheManager = domainCacheManager;
    }

}

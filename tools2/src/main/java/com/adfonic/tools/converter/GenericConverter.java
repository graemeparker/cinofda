package com.adfonic.tools.converter;

import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.springframework.web.jsf.FacesContextUtils;

public abstract class GenericConverter implements Converter {

    protected <T> T getSpringService(FacesContext context, Class<T> type) {
        // should not be using qualifiers for services, hence no double
        // implementations to get the beans.
        return FacesContextUtils.getWebApplicationContext(context).getBean(type);
    }
}

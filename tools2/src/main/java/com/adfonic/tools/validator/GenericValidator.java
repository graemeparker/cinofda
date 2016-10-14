package com.adfonic.tools.validator;

import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;

import org.springframework.web.jsf.FacesContextUtils;

public abstract class GenericValidator implements Validator {

    protected <T> T getSpringService(FacesContext context, Class<T> type) {
        // should not be using qualifiers for services, hence no double
        // implementations to get the beans.
        return FacesContextUtils.getWebApplicationContext(context).getBean(type);
    }
}

package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.validator.ValidationResult;
import com.adfonic.presentation.validator.ValidationUtils;

public class URLValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {

        ValidationResult validation = ValidationUtils.validateUrl((String) value);
        if (validation.isFailed()) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, validation.getMessageKey(), "");

            // context.addMessage(componet.getClientId(),fm);
            throw new ValidatorException(fm);
        }
    }
}

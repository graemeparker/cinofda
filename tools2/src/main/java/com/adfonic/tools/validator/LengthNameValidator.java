package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.presentation.FacesUtils;

public class LengthNameValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {
        int maxLength = 35;
        if (componet.getId().equals("ad-text")) {
            maxLength = 35;
        } else if (componet.getId().equals("ad-title")) {
            maxLength = 25;
        } else if (componet.getId().equals("ad-description")) {
            maxLength = 100;
        }

        if (!isValid((String) value, context, maxLength)) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, null, "page.error.validation.invalidname");

            // context.addMessage(componet.getClientId(),fm);
            throw new ValidatorException(fm);
        }
    }

    private boolean isValid(String name, FacesContext context, int maxLength) {

        if (name != null) {
            if (name.length() <= maxLength) {
                return true;
            }
        }
        return false;
    }

}

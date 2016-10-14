package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.presentation.FacesUtils;

public class CampaignNameValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {

        if (!isValid((String) value, context)) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, null, "page.error.validation.invalidname");

            // context.addMessage(componet.getClientId(),fm);
            throw new ValidatorException(fm);
        }
    }

    private boolean isValid(String name, FacesContext context) {
        if (name.contains("<") || name.contains(">") || name.contains("|") || name.contains("[") || name.contains("]")
                || name.contains("{") || name.contains("}") || name.contains("\\") || name.contains("/") || name.contains("%")
                || name.contains("^") || name.contains("\"")) {
            return false;
        }
        return true;
    }

}

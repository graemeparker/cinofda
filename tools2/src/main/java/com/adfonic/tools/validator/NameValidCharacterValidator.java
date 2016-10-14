package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;

import com.adfonic.presentation.FacesUtils;

public class NameValidCharacterValidator extends GenericValidator {

    private static final char[] invalidChars = { '<', '>', '|', '[', ']', '{', '}', '\\', '/', '%', '^', '\"' };

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {
        if (!isValid((String) value)) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, null, "page.error.validation.invalidname");
            throw new ValidatorException(fm);
        }
    }

    private boolean isValid(String name) {
        return StringUtils.containsNone(name, invalidChars);
    }
}

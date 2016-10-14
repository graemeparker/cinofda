package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.util.ValidationUtils;

public class EmailValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {
        boolean validEmail = true;
        String email = (String) value;

        validEmail = ValidationUtils.isValidEmailAddress(email);

        if (!validEmail) {
            throw new ValidatorException(new FacesMessage());
        }
    }
}

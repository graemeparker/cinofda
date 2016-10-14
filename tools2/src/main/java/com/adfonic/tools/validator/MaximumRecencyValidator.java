package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.presentation.FacesUtils;

public class MaximumRecencyValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {

        if (!isValid((Long) value)) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, null, "page.campaign.targeting.recency.validation.message");

            throw new ValidatorException(fm);
        }
    }

    private boolean isValid(long numDays) {
        return numDays <= 180;
    }

}

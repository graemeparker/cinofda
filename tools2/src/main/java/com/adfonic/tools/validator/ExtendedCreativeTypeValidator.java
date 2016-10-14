package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.dto.campaign.creative.ExtendedCreativeTypeDto;
import com.adfonic.presentation.FacesUtils;

public class ExtendedCreativeTypeValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {
        if (((ExtendedCreativeTypeDto) value).getId() == null) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.campaign.creative.error.novendor");

            throw new ValidatorException(fm);
        }
    }

}

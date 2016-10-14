package com.adfonic.tools.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.dto.language.LanguageDto;
import com.adfonic.presentation.FacesUtils;

public class LanguageValidator extends GenericValidator {

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {
        if (((LanguageDto) value).getId() == null) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "page.campaign.creative.language.required");

            throw new ValidatorException(fm);
        }
    }

}

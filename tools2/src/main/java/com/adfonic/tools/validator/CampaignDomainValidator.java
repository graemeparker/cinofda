package com.adfonic.tools.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;

import com.adfonic.presentation.FacesUtils;

@FacesValidator("CampaignDomainValidator")
public class CampaignDomainValidator extends GenericValidator {

    private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile("^([a-z0-9_-]+\\.)*[a-z0-9_-]+\\.[a-z0-9_]{2,}$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public void validate(FacesContext context, UIComponent componet, Object value) throws ValidatorException {
        String domainName = (String) value;
        if (StringUtils.isEmpty(domainName)) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, null,
                    "page.campaign.validation.campaigndomain.required");
            throw new ValidatorException(fm);
        }

        if (!isValid(domainName)) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, null,
                    "page.error.validation.invalidcampaigndomain");
            throw new ValidatorException(fm);
        }
    }

    private boolean isValid(String domainName) {
        Matcher matcher = DOMAIN_NAME_PATTERN.matcher(domainName);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

}
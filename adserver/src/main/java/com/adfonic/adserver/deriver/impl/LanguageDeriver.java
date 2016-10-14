package com.adfonic.adserver.deriver.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.util.AcceptedLanguages;

/** Derive a Language domain object from the request */
@Component
public class LanguageDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(LanguageDeriver.class.getName());

    @Autowired
    public LanguageDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.ACCEPTED_LANGUAGES);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.ACCEPTED_LANGUAGES.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        // First see if the publisher hard-coded the user's language
        String langSpec = context.getAttribute(Parameters.LANGUAGE);
        if (langSpec == null || langSpec.equals("")) {
            // Not hard-coded...let's see if the publisher proxied (/ad)
            // or passively supplied (/js) the Accept-Language header
            langSpec = context.getHeader("Accept-Language");
            if (langSpec == null || langSpec.equals("")) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("No " + Parameters.LANGUAGE + " parameter or Accept-Language header");
                }
                return null;
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("langSpec=" + langSpec);
        }

        return AcceptedLanguages.parse(langSpec);
    }
}

package com.adfonic.adserver.deriver.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.Gender;

/** Derive the viewer's Gender from the request */
@Component
public class GenderDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(GenderDeriver.class.getName());

    @Autowired
    public GenderDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.GENDER);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.GENDER.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        String genderStr = context.getAttribute(Parameters.GENDER);
        if (genderStr == null) {
            return null;
        }

        switch (genderStr.charAt(0)) {
        case 'm':
        case 'M':
            return Gender.MALE;
        case 'f':
        case 'F':
            return Gender.FEMALE;
        case 'u':
        case 'U': // unknown
            return null;
        default:
            if (LOG.isLoggable(Level.FINE)) { // some exchange creative how to pass unknown gender
                LOG.fine("Invalid value for " + Parameters.GENDER + ": " + genderStr);
            }
            return null;
        }
    }
}

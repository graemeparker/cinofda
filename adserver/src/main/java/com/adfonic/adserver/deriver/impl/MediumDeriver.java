package com.adfonic.adserver.deriver.impl;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.Medium;

/** Derive a medium override from the request */
@Component
public class MediumDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(MediumDeriver.class.getName());

    @Autowired
    public MediumDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.MEDIUM);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.MEDIUM.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        String mediumName = context.getAttribute(Parameters.MEDIUM);
        if (mediumName == null) {
            // fall back on t.type temporarily
            mediumName = context.getAttribute(Parameters.MEDIUM_DEPRECATED);
            if (mediumName == null) {
                return null;
            }
        }

        mediumName = mediumName.toUpperCase(); // i.e. "site" -> "SITE"
        try {
            return Medium.valueOf(mediumName);
        } catch (Exception e) {
            // Special case because I guess we're being passed "app" sometimes
            if ("APP".equals(mediumName)) {
                return Medium.APPLICATION;
            }
        }

        LOG.warning("Unrecognized medium: " + mediumName);
        return null;
    }
}

package com.adfonic.adserver.deriver.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.Feature;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

/** Derive the set of accepted Features based on the request */
@Component
public class AcceptedFeaturesDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(AcceptedFeaturesDeriver.class.getName());

    @Autowired
    public AcceptedFeaturesDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.ACCEPTED_FEATURES);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.ACCEPTED_FEATURES.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        IntegrationTypeDto integrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE);
        if (integrationType == null) {
            return Collections.EMPTY_SET;
        }

        // Start out with the assumption that the IntegrationType's supported
        // features is the set we'll be returning
        Set<Feature> acceptedFeatures = integrationType.getSupportedFeatures();

        // Remove any explicitly excluded features if t.exclude was specified
        String param = context.getAttribute(Parameters.EXCLUDED_FEATURES);
        if (StringUtils.isNotBlank(param)) {
            // Copy the acceptedFeatures set since we're about to modify it, and we
            // don't want to modify the live set on IntegrationType.
            acceptedFeatures = new HashSet<Feature>(acceptedFeatures);
            for (String feature : StringUtils.split(param, ",")) {
                try {
                    acceptedFeatures.remove(Feature.valueOf(feature.trim()));
                } catch (Exception e) {
                    LOG.warning("Invalid Feature listed in " + Parameters.EXCLUDED_FEATURES + ": " + feature);
                }
            }
        }

        return acceptedFeatures;
    }
}

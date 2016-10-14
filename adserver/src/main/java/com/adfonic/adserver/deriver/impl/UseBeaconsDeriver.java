package com.adfonic.adserver.deriver.impl;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.BeaconUtils;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

/**
 * Derive whether or not beacons should be used.  This doesn't technically
 * need to be a deriver, but it's done this way for efficiency.  That way
 * anybody can simply ask the context for the USE_BEACONS Boolean, as many
 * times as needed, in as many spots needed, and it will only be derived
 * at most once per request.  And it encapsulates the logic.
 */
@Component
public class UseBeaconsDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(UseBeaconsDeriver.class.getName());

    @Autowired
    public UseBeaconsDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.USE_BEACONS);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.USE_BEACONS.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        IntegrationTypeDto integrationType = context.getAttribute(TargetingContext.INTEGRATION_TYPE);
        return BeaconUtils.shouldUseBeacons(integrationType);
    }
}

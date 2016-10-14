package com.adfonic.adserver.deriver.impl;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.util.ConstraintsHelper;

/** Derive a Platform domain object from the request */
@Component
public class PlatformDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(PlatformDeriver.class.getName());

    @Autowired
    public PlatformDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.PLATFORM);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.PLATFORM.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        Map<String, String> props = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
        if (props == null) {
            LOG.warning("Unable to derive device properties");
            return null;
        }
        return ConstraintsHelper.findMatch(context.getDomainCache().getPlatforms(), new ConstraintsHelper.MapPropertySource(props));
    }
}

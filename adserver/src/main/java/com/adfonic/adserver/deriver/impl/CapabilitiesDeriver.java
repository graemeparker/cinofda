package com.adfonic.adserver.deriver.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.dto.adserver.CapabilityDto;
import com.adfonic.util.ConstraintsHelper;

/** Derive a list of CapabilityDto domain objects from the request */
@Component
public class CapabilitiesDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(CapabilitiesDeriver.class.getName());

    @Autowired
    public CapabilitiesDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.CAPABILITIES, TargetingContext.CAPABILITY_IDS);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (TargetingContext.CAPABILITIES.equals(attribute)) {
            return getCapabilities(context);
        } else if (TargetingContext.CAPABILITY_IDS.equals(attribute)) {
            return getCapabilityIds(context);
        } else {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }
    }

    public List<CapabilityDto> getCapabilities(TargetingContext context) {
        // First add all the Capabilities of the end user's device
        Map<String, String> props = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
        if (props == null) {
            LOG.warning("Unable to derive device properties");
            return null;
        }

        return ConstraintsHelper.findAllMatches(context.getDomainCache().getCapabilities(), new ConstraintsHelper.MapPropertySource(props));
    }

    public Set<Long> getCapabilityIds(TargetingContext context) {
        List<CapabilityDto> capabilities = context.getAttribute(TargetingContext.CAPABILITIES);
        if (capabilities == null) {
            return null;
        }
        Set<Long> ids = new HashSet<Long>();
        for (CapabilityDto capability : capabilities) {
            ids.add(capability.getId());
        }
        return ids;
    }
}

package com.adfonic.adserver.deriver.impl;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;

/** Derive a MobileIpAddressRange domain object from the request */
@Component
public class MobileIpAddressRangeDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(MobileIpAddressRangeDeriver.class.getName());

    @Autowired
    public MobileIpAddressRangeDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.MOBILE_IP_ADDRESS_RANGE);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.MOBILE_IP_ADDRESS_RANGE.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        String ip = context.getAttribute(Parameters.IP);
        return context.getDomainCache().getMobileIpAddressRange(ip);
    }
}

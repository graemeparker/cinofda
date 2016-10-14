package com.adfonic.adserver.deriver.impl;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.util.Subnet;

/** Derive the numeric IP address value */
@Component
public class IpAddressValueDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(IpAddressValueDeriver.class.getName());

    @Autowired
    public IpAddressValueDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.IP_ADDRESS_VALUE);
    }

    @Override
    public Long getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.IP_ADDRESS_VALUE.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        String ip = context.getAttribute(Parameters.IP);
        if (ip != null) {
            return Subnet.getIpAddressValue(ip);
        } else {
            return 0l;
        }

    }
}

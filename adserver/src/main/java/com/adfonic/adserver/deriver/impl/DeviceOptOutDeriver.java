package com.adfonic.adserver.deriver.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.dmp.cache.OptOutType;
import com.adfonic.retargeting.redis.DeviceData;
import com.adfonic.util.stats.CounterManager;

@Component
public class DeviceOptOutDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(DeviceOptOutDeriver.class.getName());

    @Autowired
    CounterManager counterManager;

    @Autowired
    public DeviceOptOutDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.DEVICE_OPT_OUT);
    }

    /**
     * This method derives optout type, where the keys are DeviceIdentifierType ids
     */
    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (TargetingContext.DEVICE_OPT_OUT.equals(attribute)) {
            return deriveOptOutForDevice(context);
        } else {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }
    }

    // opt outs for the device
    private Set<OptOutType> deriveOptOutForDevice(TargetingContext context) {
        try {
            Map<Long, String> deviceIdentifiers = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Getting optouts for " + deviceIdentifiers);
            }

            if (deviceIdentifiers == null || deviceIdentifiers.isEmpty()) {
                return Collections.emptySet();
            }

            Set<DeviceData> deviceDataSet = context.getAttribute(TargetingContext.DEVICE_DATA);
            if (deviceDataSet == null || deviceDataSet.isEmpty()) {
                return Collections.emptySet();
            }

            Set<OptOutType> optOuts = new HashSet<>();
            for (DeviceData dd : deviceDataSet) {

                OptOutType optOut = dd.getOptOutType();
                if (optOut != null && OptOutType.noOptout != optOut) {
                    optOuts.add(optOut);
                }
            }

            return optOuts;

        } catch (Exception e) {
            counterManager.incrementCounter("DeviceOptOutDeriverErrors");
            LOG.log(Level.WARNING, "Failed to get optouts data", e);
            return Collections.emptySet();
        } finally {
            counterManager.incrementCounter("DeviceOptOutDeriverTotalCall");
        }
    }
}

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
import com.adfonic.retargeting.redis.DeviceData;
import com.adfonic.util.stats.CounterManager;

@Component
public class DeviceAudiencesDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(DeviceAudiencesDeriver.class.getName());

    @Autowired
    CounterManager counterManager;

    @Autowired
    public DeviceAudiencesDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.DEVICE_AUDIENCES);
    }

    /**
     * This method derives audience ids, where the keys are DeviceIdentifierType ids
     */
    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (TargetingContext.DEVICE_AUDIENCES.equals(attribute)) {
            return deriveAudienceTargettedCampaignForDeviceData(context);
        } else {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }
    }

    // return AudienceIds
    private Set<Long> deriveAudienceTargettedCampaignForDeviceData(TargetingContext context) {
        long startTime = System.currentTimeMillis();
        try {
            Map<Long, String> deviceIdentifiers = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Getting eligible audiences for " + deviceIdentifiers);
            }

            if (deviceIdentifiers == null || deviceIdentifiers.isEmpty()) {
                return Collections.emptySet();
            }

            Set<DeviceData> ddSet = context.getAttribute(TargetingContext.DEVICE_DATA);
            if (ddSet == null || ddSet.isEmpty()) {
                return Collections.emptySet();
            }

            Set<Long> targetedAudienceIds = new HashSet<>();
            for (DeviceData dd : ddSet) {
                if (dd == null)
                    continue;
                if (dd.getAudienceIds() == null)
                    continue;
                targetedAudienceIds.addAll(dd.getAudienceIds());
            }
            return targetedAudienceIds;

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get Audience targetted campaignIds data", e);
            return Collections.emptySet();
        } finally {
            long endTime = System.currentTimeMillis();
            counterManager.incrementCounter("DeviceAudiencesDeriverTotalTime", endTime - startTime);
            counterManager.incrementCounter("DeviceAudiencesDeriverTotalCall");
        }
    }
}

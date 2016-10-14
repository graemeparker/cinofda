package com.adfonic.adserver.deriver.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.exceptions.JedisConnectionException;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.controller.rtb.BiddingSwitchController;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.dmp.cache.DeviceDataCacheReader;
import com.adfonic.dmp.cache.KeyManager;
import com.adfonic.retargeting.redis.DeviceData;
import com.adfonic.retargeting.redis.DeviceDataRedisReader;
import com.adfonic.util.stats.CounterManager;

@Component
public class DeviceDataDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(DeviceDataDeriver.class.getName());

    private final DeviceDataCacheReader cacheReader;

    @Autowired
    CounterManager counterManager;

    @Autowired
    public DeviceDataDeriver(DeriverManager deriverManager, DeviceDataRedisReader cacheReader) {
        super(deriverManager, TargetingContext.DEVICE_DATA);

        this.cacheReader = cacheReader;
    }

    /**
     * This method derives audience ids, where the keys are DeviceIdentifierType ids
     */
    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (TargetingContext.DEVICE_DATA.equals(attribute)) {
            if (BiddingSwitchController.BIDDING_ENABLED) {
                return deriveAudienceTargettedCampaignForDeviceData(context);
            } else {
                return null;
            }
        } else {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }
    }

    private Set<DeviceData> deriveAudienceTargettedCampaignForDeviceData(TargetingContext context) {
        try {
            Map<Long, String> deviceIdentifiers = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Getting device data for " + deviceIdentifiers);
            }

            if (MapUtils.isEmpty(deviceIdentifiers)) {
                return Collections.<DeviceData> emptySet();
            }

            Set<DeviceData> ddSet = new HashSet<>();

            for (Map.Entry<Long, String> entry : deviceIdentifiers.entrySet()) {
                long deviceIdentifierTypeId = entry.getKey();
                String deviceId = entry.getValue();
                String deviceIdKey = KeyManager.getKey(deviceId, deviceIdentifierTypeId);
                counterManager.incrementCounter(AsCounter.DeviceRedisCall);
                DeviceData deviceData = cacheReader.getData(deviceIdKey);
                if (deviceData != null) {
                    ddSet.add(deviceData);
                }
            }

            return ddSet;
        } catch (JedisConnectionException jcx) {
            // Redis connection exception - log short warning
            LOG.log(Level.WARNING, "Device Redis connection problem: " + jcx);
            counterManager.incrementCounter(AsCounter.DeviceRedisError);
        } catch (Exception e) {
            // Not a Redis connection problem so rather log whole exception stacktrace
            LOG.log(Level.WARNING, "Failed to get Audience targetted campaignIds data", e);
            counterManager.incrementCounter(AsCounter.DeviceRedisError);
        }
        return Collections.<DeviceData> emptySet();
    }
}

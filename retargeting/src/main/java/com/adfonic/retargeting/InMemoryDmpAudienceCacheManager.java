package com.adfonic.retargeting;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.time.Instant;

import com.adfonic.dmp.cache.OptOutType;
import com.adfonic.retargeting.redis.DeviceData;

public class InMemoryDmpAudienceCacheManager extends AbstractDmpAudienceCacheManager {

    private ConcurrentMap<String, DeviceData> map = new ConcurrentHashMap<>();

    @Override
    protected void delteFromCache(String deviceIdKey) throws CacheException {
        map.remove(deviceIdKey);
    }

    @Override
    protected Set<Long> extractTargetedCampaignIds(Map<Long, String> deviceIdentifiers, List<String> keys) {
        Set<Long> ret = new HashSet<>();
        List<String> devIdKs = keys;
        for (String devIdK : devIdKs) {
            DeviceData deviceData = map.get(devIdK);
            if (deviceData == null) {
                continue;
            }
            Set<Long> vals = deviceData.getAudienceIds();
            if (vals != null) {
                ret.addAll(vals);
            }
        }

        return ret;
    }

    @Override
    public void setDeviceAudienceIds(String deviceId, Set<Long> audienceIds) {
        DeviceData deviceData = map.get(deviceId);
        if (deviceData == null) {
            deviceData = new DeviceData();
            deviceData.setAudienceIds(audienceIds);
        }
        map.put(deviceId, deviceData);
    }

    @Override
    public void flush() {
        // not implemented
    }

    @Override
    public DeviceData getData(String deviceId) {
        return map.get(deviceId);
    }

    @Override
    public void setOptOut(String deviceId, OptOutType optOutType) {
        DeviceData deviceData = map.get(deviceId);
        if (deviceData == null) {
            deviceData = new DeviceData();
            deviceData.setOptOutType(optOutType);
        }
        map.put(deviceId, deviceData);

    }

    @Override
    public void delOptOut(String deviceIdKey) {
        DeviceData deviceData = map.get(deviceIdKey);
        if (deviceData == null) {
            deviceData = new DeviceData();
            deviceData.setOptOutType(OptOutType.noOptout);
        }
        map.put(deviceIdKey, deviceData);
    }

    @Override
    public void deleteDeviceId(String deviceIdKey) {
        map.remove(deviceIdKey);
    }

    @Override
    public void setDeviceData(String deviceIdKey, DeviceData old, DeviceData dd) {
        // not implemented
    }

    @Override
    public void setDeviceAudienceRecency(String deviceIdKey, Long audienceId, Instant instant) {
        // not implemented
    }
}

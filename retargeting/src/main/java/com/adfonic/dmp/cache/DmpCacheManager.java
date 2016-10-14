package com.adfonic.dmp.cache;

import java.util.Set;

import org.joda.time.Instant;

import com.adfonic.retargeting.redis.DeviceData;

public interface DmpCacheManager extends DeviceDataCacheReader {

    void setDeviceData(String deviceIdKey, DeviceData old, DeviceData dd);

    void setDeviceAudienceIds(String deviceId, Set<Long> audienceIds);

    void setDeviceAudienceRecency(String deviceIdKey, Long audienceId, Instant instant);

    void setOptOut(String deviceId, OptOutType optOutType);

    void delOptOut(String deviceIdKey);

    void flush();

    void deleteDeviceId(String deviceIdKey);

}

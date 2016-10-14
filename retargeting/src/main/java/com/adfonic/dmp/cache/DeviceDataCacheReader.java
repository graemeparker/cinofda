package com.adfonic.dmp.cache;

import com.adfonic.retargeting.redis.DeviceData;

public interface DeviceDataCacheReader {

    DeviceData getData(String deviceId);
}

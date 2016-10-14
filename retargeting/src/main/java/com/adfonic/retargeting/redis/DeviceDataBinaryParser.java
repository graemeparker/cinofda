package com.adfonic.retargeting.redis;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceDataBinaryParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDataBinaryParser.class);
    private final InstantSerializer instantSerializer = new InstantSerializer();
    private final LongSetSerializer longSetSerializer = new LongSetSerializer();
    private final OptOutSerializer optOutSerializer = new OptOutSerializer();

    public DeviceData parse(Map<byte[], byte[]> bytes) {
        DeviceData deviceData = new DeviceData();
        if (bytes == null) {
            return deviceData;
        }

        Set<Entry<byte[], byte[]>> entrySet = bytes.entrySet();
        for (Entry<byte[], byte[]> entry : entrySet) {
            byte[] keyB = entry.getKey();
            String key = new String(keyB);

            switch (key) {
            case AbstractRedis.AUDIENCE:
                byte[] audienceBytes = entry.getValue();
                deviceData.setAudienceIds(longSetSerializer.parseSet(audienceBytes));
                break;

            case AbstractRedis.OPTOUT:
                byte[] optoutBytes = entry.getValue();
                deviceData.setOptOutType(optOutSerializer.parseOptOut(optoutBytes));
                break;

            default:
                if (key.startsWith(AbstractRedis.AUDIENCE_RECENCY)) {
                    String id = key.substring(1);
                    Long audienceId = Long.parseLong(id);
                    Instant instant = instantSerializer.parseInstant(entry.getValue());
                    deviceData.getRecencyByAudience().put(audienceId, instant);
                    break;
                } else {
                    LOGGER.warn("unexpected key {}", key);
                }
            }
        }
        return deviceData;
    }

}

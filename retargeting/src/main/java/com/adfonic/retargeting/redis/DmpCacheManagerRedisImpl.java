package com.adfonic.retargeting.redis;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.adfonic.dmp.cache.DmpCacheManager;
import com.adfonic.dmp.cache.OptOutType;
import com.newrelic.api.agent.Trace;

public class DmpCacheManagerRedisImpl extends AbstractRedis implements DmpCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmpCacheManagerRedisImpl.class);

    private final AtomicLong deleteCounter = new AtomicLong();
    private final AtomicLong saveCounter = new AtomicLong();
    private final InstantSerializer instantSerializer = new InstantSerializer();
    private final LongSetSerializer longSetSerializer = new LongSetSerializer();
    private final OptOutSerializer optOutSerializer = new OptOutSerializer();

    private DeviceDataBinaryParser ddParser;
    protected final Pipeline pipe;
    protected final Jedis jedis;

    public DmpCacheManagerRedisImpl(Jedis jedis) {
        this(jedis, new DeviceDataBinaryParser());
    }

    public DmpCacheManagerRedisImpl(Jedis jedis, DeviceDataBinaryParser ddParser) {
        this.jedis = jedis;
        this.pipe = jedis.pipelined();
        this.ddParser = ddParser;
    }
    
    public AtomicLong getDeleteCounter() {
        return deleteCounter;
    }

    public AtomicLong getSaveCounter() {
        return saveCounter;
    }

    Set<Long> readFieldAsSet(String deviceIdKey, String field) {
        byte[] keyBytes = sha.get().digest(deviceIdKey.getBytes());

        byte[] bytes = jedis.hget(keyBytes, field.getBytes());
        if (bytes == null) {
            LOGGER.debug("no value for key: {}", deviceIdKey);
            return Collections.<Long> emptySet();
        }
        return longSetSerializer.parseSet(bytes);
    }

    public void saveSet(String deviceIdKey, String field, Set<Long> ids) {

        byte[] array = longSetSerializer.longSetToByteArray(ids);
        byte[] keyBytes = sha.get().digest(deviceIdKey.getBytes());

        saveCounter.incrementAndGet();
        pipe.hset(keyBytes, field.getBytes(), array);
    }

    @Trace(dispatcher = true)
    @Override
    public void setDeviceAudienceIds(String deviceIdKey, Set<Long> audienceIds) {
        LOGGER.debug("Saving {} audienceIds: {}", deviceIdKey, audienceIds);

        saveSet(deviceIdKey, AUDIENCE, audienceIds);
    }

    @Override
    public void deleteDeviceId(String deviceIdKey) {
        deleteCounter.incrementAndGet();

        LOGGER.debug("deleting from redis key: {}", deviceIdKey);

        byte[] keyBytes = sha.get().digest(deviceIdKey.getBytes());
        pipe.del(keyBytes);
    }

    public void deleteField(String deviceIdKey, String field) {
        LOGGER.debug("deleting from redis key: {} field: {}", field);

        byte[] keyBytes = sha.get().digest(deviceIdKey.getBytes());
        pipe.hdel(keyBytes, field.getBytes());
    }

    @Override
    public void setDeviceAudienceRecency(String deviceIdKey, Long audienceId, Instant instant) {
        byte[] keyBytes = sha.get().digest(deviceIdKey.getBytes());

        String field = AUDIENCE_RECENCY + audienceId;
        if (instant == null) {
            pipe.hdel(keyBytes, field.getBytes());
            return;
        }

        byte[] array = instantSerializer.instantToByteArray(instant);

        pipe.hset(keyBytes, field.getBytes(), array);
    }

    @Override
    public void delOptOut(String deviceIdKey) {
        deleteField(deviceIdKey, OPTOUT);
    }

    @Trace(dispatcher = true)
    @Override
    public void setOptOut(String deviceId, OptOutType optOutType) {

        LOGGER.debug("setting optout key: {}  optOutType: {}", deviceId, optOutType);
        byte[] keyBytes = sha.get().digest(deviceId.getBytes());

        byte[] bArr = optOutSerializer.toByteArray(optOutType);
        pipe.hset(keyBytes, OPTOUT.getBytes(), bArr);
    }

    public DeviceData getData(byte[] keyBytes) {

        Map<byte[], byte[]> bytes = jedis.hgetAll(keyBytes);
        if (bytes == null) {
            LOGGER.debug("no value for key: {}", keyBytes);
            return null;
        }

        return ddParser.parse(bytes);
    }

    @Trace(dispatcher = true)
    @Override
    public DeviceData getData(String deviceIdKey) {
        byte[] keyBytes = sha.get().digest(deviceIdKey.getBytes());

        return getData(keyBytes);
    }

    @Trace(dispatcher = true)
    @Override
    public void setDeviceData(String deviceIdKey, DeviceData oldDD, DeviceData dd) {
        byte[] keyBytes = sha.get().digest(deviceIdKey.getBytes());

        if (oldDD.getOptOutType() != dd.getOptOutType()) {
            OptOutType optOutType = dd.getOptOutType() == null ? OptOutType.noOptout : dd.getOptOutType();
            pipe.hset(keyBytes, OPTOUT.getBytes(), optOutType.toString().getBytes());
        }

        if (dd.getAudienceIds() != null && !dd.getAudienceIds().equals(oldDD.getAudienceIds())) {
            byte[] audiencesB = longSetSerializer.longSetToByteArray(dd.getAudienceIds());
            pipe.hset(keyBytes, AUDIENCE.getBytes(), audiencesB);
        }

        if (dd.getRecencyByAudience() != null) {
            setRegencyByAudience(oldDD, dd, keyBytes);
        }
    }

    private void setRegencyByAudience(DeviceData oldDD, DeviceData dd, byte[] keyBytes) {
        for (Entry<Long, Instant> e : dd.getRecencyByAudience().entrySet()) {
            Long audienceId = e.getKey();
            Instant i = e.getValue();
            if (i != null) {
                Instant oldI = oldDD.getRecencyByAudience().get(audienceId);
                if (!i.equals(oldI)) {
                    byte[] recAud = instantSerializer.instantToByteArray(i);
                    String field = AUDIENCE_RECENCY + audienceId;
                    pipe.hset(keyBytes, field.getBytes(), recAud);
                }
            }
        }
    }

    @Override
    public void flush() {
        pipe.sync();
        jedis.close();
    }

}

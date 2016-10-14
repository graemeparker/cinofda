package com.adfonic.adserver.rtb.util;

import java.util.Objects;

import redis.clients.jedis.Jedis;

import com.adfonic.adserver.spring.config.AdserverStatusSpringConfig.AdServerResource;
import com.adfonic.retargeting.redis.ThreadLocalClientFactory;
import com.adfonic.util.status.BaseResourceCheck;
import com.adfonic.util.status.ResourceId;

/**
 * 
 * @author mvanek
 *
 */
public class SimpleRedisCheck extends BaseResourceCheck<AdServerResource> {

    private final ThreadLocalClientFactory factory;

    private final String testKey;

    public SimpleRedisCheck(ThreadLocalClientFactory factory, String testKey) {
        Objects.requireNonNull(factory);
        this.factory = factory;
        Objects.requireNonNull(testKey);
        this.testKey = testKey;
    }

    public SimpleRedisCheck(ThreadLocalClientFactory factory) {
        this(factory, "whatever");
    }

    @Override
    public String doCheck(ResourceId<AdServerResource> resource) throws Exception {
        Jedis jedis = factory.getJedis();
        try {
            return jedis.get(testKey);
        } catch (Exception x) {
            factory.invalidateConnection(x);
            throw x;
        }
    }

}

package com.adfonic.retargeting.redis;

import redis.clients.jedis.Jedis;

import com.adfonic.dmp.cache.DmpCacheManager;
import com.adfonic.dmp.cache.DmpCacheManagerFactory;

public class DmpCacheManagerRedisFactory implements DmpCacheManagerFactory {

    private final ClientFactory clientFactory;

    public DmpCacheManagerRedisFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public DmpCacheManager get() {

        Jedis jedis = clientFactory.getJedis();
        return new DmpCacheManagerRedisImpl(jedis);
    }

}

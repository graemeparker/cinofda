package com.adfonic.retargeting.redis;

import redis.clients.jedis.Jedis;

public interface ClientFactory {
    Jedis getJedis();
}

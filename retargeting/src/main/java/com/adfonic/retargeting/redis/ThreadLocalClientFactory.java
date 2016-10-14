package com.adfonic.retargeting.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class ThreadLocalClientFactory extends SimpleClientFactory implements ClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalClientFactory.class);

    protected ThreadLocal<Jedis> tlClient = new ThreadLocal<Jedis>() {
        @Override
        protected Jedis initialValue() {
            return getJedisFromSuper();
        }
    };

    public ThreadLocalClientFactory(String serverPortList, int timeout) {
        super(serverPortList, timeout);
    }

    public ThreadLocalClientFactory(String serverPortList) {
        super(serverPortList);
    }

    @Override
    public Jedis getJedis() {
        return tlClient.get();
    }

    protected Jedis getJedisFromSuper() {
        return super.getJedis();
    }

    public void invalidateConnection(Exception re) {
        if (re instanceof JedisException || re instanceof java.io.IOException) {
            try {
                LOGGER.warn("invalidating caused by " + re.getClass());
                Jedis jedis = tlClient.get();
                if (jedis != null)
                    jedis.close();
            } catch (Exception e) {
                LOGGER.error("error closing connection", e);
            } finally {
                tlClient.remove();
            }
        }
    }

}

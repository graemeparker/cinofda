package com.adfonic.retargeting.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class SimpleClientFactory implements ClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClientFactory.class);

    public static final int DEFAULT_TIMEOUT = 2000; // jedis default

    protected String[] hostAndPort;

    protected int timeout = DEFAULT_TIMEOUT;

    public SimpleClientFactory(String serverPortList, int timeout) {
        this(serverPortList);
        this.timeout = timeout;
    }

    public SimpleClientFactory(String serverPortList) {
        LOGGER.info("Configured server/port list {}", serverPortList);

        try {
            this.hostAndPort = serverPortList.split("[:,]");
        } catch (Exception e) {
            throw new RuntimeException("unable to create " + getClass(), e);
        }
    }

    @Override
    public Jedis getJedis() {
        LOGGER.debug("retrieving Jedis client");
        int numberOfHosts = hostAndPort.length / 2;
        for (int i = 0; i < numberOfHosts; i++) {
            try {
                int n = i % numberOfHosts;
                String host = hostAndPort[n * 2];
                int port = Integer.parseInt(hostAndPort[n * 2 + 1]);
                Jedis jedis = getNewJedis(host, port, timeout);
                jedis.connect();
                return jedis;
            } catch (Exception re) {
                LOGGER.warn("Unable to connect to server: " + hostAndPort[i * 2] + ":" + hostAndPort[i * 2 + 1] + ", failing over to next server. " + re);
            }
        }

        throw new RuntimeException("No active servers found to connect!");
    }

    protected Jedis getNewJedis(String host, int port, int timeout) {
        Jedis jedis;
        jedis = new Jedis(host, port, timeout);
        return jedis;
    }

}

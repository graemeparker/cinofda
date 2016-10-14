package com.adfonic.retargeting.redis;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

@RunWith(MockitoJUnitRunner.class)
public class ThreadLocalClientFactoryTest {

    ThreadLocalClientFactory testObj = new MyClass("host1:6379,host2:6379");

    @Mock
    private Jedis jedis1;
    @Mock
    private Jedis jedis2;

    @Test
    public void testGetJedis() {
        Jedis result = testObj.getJedis();

        Assert.assertEquals(jedis1, testObj.getJedis());

        // without call to invalidate it wont try to connect again , it returns locally cached one
        Mockito.doThrow(new JedisConnectionException("simulated connect timed out")).when(jedis1).connect();

        // call again will return cached jedis
        Jedis j1 = testObj.getJedis();
        Assert.assertSame(result, j1);
    }

    @Test
    public void testInvalidateConnection() {
        Jedis j1 = testObj.getJedis();
        Assert.assertEquals(jedis1, j1);

        testObj.invalidateConnection(new JedisConnectionException("simulated connect timed out"));
        Mockito.doThrow(new JedisConnectionException("simulated connect timed out")).when(jedis1).connect();

        Jedis j2 = testObj.getJedis();
        Assert.assertEquals(jedis2, j2);
    }

    class MyClass extends ThreadLocalClientFactory {

        public MyClass(String serverPortList) {
            super(serverPortList);
        }

        @Override
        protected Jedis getNewJedis(String host, int port, int timeout) {
            switch (host) {
            case "host1":
                return jedis1;
            case "host2":
                return jedis2;

            default:
                break;
            }
            return null;
        }
    }
}

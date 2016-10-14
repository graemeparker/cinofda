package com.adfonic.retargeting.redis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

@RunWith(MockitoJUnitRunner.class)
public class SimpleClientFactoryTest {

    private SimpleClientFactory testObj = Mockito.spy(new SimpleClientFactory("host1:6379,host2:6379,host3:6379"));

    @Mock
    private Jedis jedis1;
    @Mock
    private Jedis jedis2;
    @Mock
    private Jedis jedis3;

    @Before
    public void before() {
        Mockito.when(testObj.getNewJedis("host1", 6379, SimpleClientFactory.DEFAULT_TIMEOUT)).thenReturn(jedis1);
        Mockito.when(testObj.getNewJedis("host2", 6379, SimpleClientFactory.DEFAULT_TIMEOUT)).thenReturn(jedis2);
        Mockito.when(testObj.getNewJedis("host3", 6379, SimpleClientFactory.DEFAULT_TIMEOUT)).thenReturn(jedis3);
    }

    @Test
    public void testGetJedis1happyPath() {

        Assert.assertEquals(jedis1, testObj.getJedis());

        // subsequent calls always return from jedis1
        Assert.assertEquals(jedis1, testObj.getJedis());
        Assert.assertEquals(jedis1, testObj.getJedis());
    }

    @Test
    public void ifJedis1FailsReturnJedis2() {
        Mockito.doThrow(new JedisConnectionException("simulated connect timed out")).when(jedis1).connect();
        Assert.assertEquals(jedis2, testObj.getJedis());
    }

    @Test
    public void ifJedis1and2FailsReturnJedis3() {
        Mockito.doThrow(new JedisConnectionException("simulated connect timed out")).when(jedis1).connect();
        Mockito.doThrow(new JedisConnectionException("Connection refused")).when(jedis2).connect();
        Assert.assertEquals(jedis3, testObj.getJedis());
    }

    @Test(expected = RuntimeException.class)
    public void whenAllRedisFailThrowException() {
        Mockito.doThrow(new JedisConnectionException("simulated connect timed out")).when(jedis1).connect();
        Mockito.doThrow(new JedisConnectionException("simulated connect timed out")).when(jedis2).connect();
        Mockito.doThrow(new JedisConnectionException("simulated connect timed out")).when(jedis3).connect();

        Jedis result = testObj.getJedis();
        Assert.fail("exception expected" + result);
    }

    @Test
    public void failHost1AndLaterFixIt() {

        // test fail over to jedis2
        Mockito.doThrow(new JedisConnectionException("simulated connect timed out")).when(jedis1).connect();
        Jedis result1 = testObj.getJedis();
        Assert.assertEquals(jedis2, result1);

        // fix jedis1 and connect again
        Mockito.doNothing().when(jedis1).connect();

        Jedis result2 = testObj.getJedis();
        Assert.assertEquals(jedis1, result2);
    }

    @Test
    public void hostFailOver() {

        // test fail over to jedis3
        Mockito.doThrow(new JedisConnectionException("total disaster")).when(jedis1).connect();
        Mockito.doThrow(new JedisConnectionException("total disaster")).when(jedis2).connect();
        Jedis result1 = testObj.getJedis();
        Assert.assertEquals(jedis3, result1);

        // fix jedis1 and connect again
        Mockito.doNothing().when(jedis1).connect();

        Jedis result2 = testObj.getJedis();
        Assert.assertEquals(jedis1, result2);
    }

}

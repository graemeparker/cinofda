package com.adfonic.retargeting.redis;

import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class GeoAudienceRedisReaderTest {

    ThreadLocalClientFactory factory = new ThreadLocalClientFactory("localhost:6379");
    GeoAudienceRedisReader testObj = new GeoAudienceRedisReader(factory, 4, 7);

    @Test
    public void encodeLatLon() {

        String result = testObj.encodeLatLon(51.2510, -0.7440);
        Assert.assertEquals("gcp7x1f", result);
    }

    @Test
    public void produceKeys3() {
        String[] keys = testObj.produceKeys("abc");

        // retaining too short key is probably more sensible than discarding it
        String[] expected = new String[] { "abc" };
        Assert.assertArrayEquals(expected, keys);
    }

    @Test
    public void produceKeys5() {
        String[] keys = testObj.produceKeys("abcd");
        String[] expected = new String[] { "abcd" };
        Assert.assertArrayEquals(expected, keys);
    }

    @Test
    public void produceKeys7() {
        String[] keys = testObj.produceKeys("djn5w7m");
        String[] expected = new String[] { "djn5", "djn5w", "djn5w7", "djn5w7m" };
        Assert.assertArrayEquals(expected, keys);
    }

    @Ignore
    @Test
    public void getAudiences() {
        Set<Long> result = testObj.getAudiences(51.2510, -0.7440);
        fail("require local redis populated with data");
    }

    @Ignore("performance test")
    @Test
    public void testGetUnionOfAudiences() {

        for (int x = 0; x < 1_000_000; x++) {

            String[] keys = testObj.produceKeys("djn5w7m");
            Set<Long> resultA = testObj.getAudiencesUnion(keys);
        }

        fail("this is performance test");
    }

}

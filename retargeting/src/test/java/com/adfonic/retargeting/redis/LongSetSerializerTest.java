package com.adfonic.retargeting.redis;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class LongSetSerializerTest {

    private LongSetSerializer testObj = new LongSetSerializer();

    @Test
    public void testEmptySet() {

        byte[] bArr = testObj.longSetToByteArray(Collections.<Long> emptySet());
        Assert.assertEquals(0, bArr.length);

        Set<Long> result = testObj.parseSet(bArr);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void test() {
        Set<Long> set = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        byte[] bArr = testObj.longSetToByteArray(set);
        Assert.assertEquals(12, bArr.length);

        Set<Long> result = testObj.parseSet(bArr);
        Assert.assertEquals(set, result);
    }

    @Test
    public void serializeNull() {
        byte[] bArr = testObj.longSetToByteArray(null);
        Assert.assertTrue(bArr.length == 0);
    }

    @Test
    public void deSerializeNull() {
        Set<Long> set = testObj.parseSet(null);
        Assert.assertTrue(set.isEmpty());
    }

}

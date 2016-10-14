package com.adfonic.retargeting.redis;

import junit.framework.Assert;

import org.junit.Test;

import com.adfonic.dmp.cache.OptOutType;

public class OptOutSerializerTest {

    private OptOutSerializer testObj = new OptOutSerializer();

    @Test
    public void test() {

        byte[] globalArr = testObj.toByteArray(OptOutType.global);
        byte[] noOptoutArr = testObj.toByteArray(OptOutType.noOptout);
        byte[] weveArr = testObj.toByteArray(OptOutType.weve);

        OptOutType g = testObj.parseOptOut(globalArr);
        OptOutType n = testObj.parseOptOut(noOptoutArr);
        OptOutType w = testObj.parseOptOut(weveArr);

        Assert.assertEquals(OptOutType.global, g);
        Assert.assertEquals(OptOutType.noOptout, n);
        Assert.assertEquals(OptOutType.weve, w);
    }

    @Test(expected = NullPointerException.class)
    public void testSerializeNull() {
        testObj.toByteArray(null);
    }

    @Test
    public void testDeserializeNull() {
        OptOutType result = testObj.parseOptOut(null);
        Assert.assertEquals(OptOutType.noOptout, result);
    }
}

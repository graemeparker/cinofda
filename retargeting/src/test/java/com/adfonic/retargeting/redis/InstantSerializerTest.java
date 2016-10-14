package com.adfonic.retargeting.redis;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Test;

public class InstantSerializerTest {

    private InstantSerializer testObj = new InstantSerializer();
    private Instant instant = new DateTime(2014, 11, 18, 17, 59, DateTimeZone.UTC).toInstant();

    @Test
    public void test() {

        byte[] bArr = testObj.instantToByteArray(instant);
        Assert.assertNotNull(bArr);

        Instant result = testObj.parseInstant(bArr);

        Assert.assertEquals(instant, result);
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {

        testObj.instantToByteArray(null);

    }
}

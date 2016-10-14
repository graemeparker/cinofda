package com.adfonic.retargeting.redis;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Test;

import com.adfonic.dmp.cache.OptOutType;

public class DeviceDataBinaryParserTest {

    final private InstantSerializer instantSerializer = new InstantSerializer();
    private DeviceDataBinaryParser testObj = new DeviceDataBinaryParser();
    private Instant dec13 = new DateTime(2014, 12, 13, 13, 59).toInstant();
    private Instant dec14 = new DateTime(2014, 12, 14, 17, 59).toInstant();

    @Test
    public void testParseEmpty() {

        Map<byte[], byte[]> bytes = new HashMap<>();

        DeviceData result = testObj.parse(bytes);
        Assert.assertTrue(result.getRecencyByAudience().isEmpty());
    }

    @Test
    public void testParseNull() {
        DeviceData result = testObj.parse(null);
        Assert.assertTrue(result.getRecencyByAudience().isEmpty());
    }

    @Test
    public void testParse() {

        Map<byte[], byte[]> bytes = new HashMap<>();

        byte[] s10RecBin = instantSerializer.instantToByteArray(dec13);
        bytes.put("A13".getBytes(), s10RecBin);
        byte[] s55RecBin = instantSerializer.instantToByteArray(dec14);
        ;
        bytes.put("A44".getBytes(), s55RecBin);

        DeviceData result = testObj.parse(bytes);
        Assert.assertEquals(2, result.getRecencyByAudience().size());
        Assert.assertEquals(dec13, result.getRecencyByAudience().get(13L));
        Assert.assertEquals(dec14, result.getRecencyByAudience().get(44L));

        Assert.assertTrue(result.getAudienceIds().isEmpty());
        Assert.assertEquals(OptOutType.noOptout, result.getOptOutType());

    }

    @Test
    public void testParse2() {

        Map<byte[], byte[]> bytes = new HashMap<>();

        byte[] s10RecBin = instantSerializer.instantToByteArray(dec13);
        bytes.put("A13".getBytes(), s10RecBin);
        byte[] s55RecBin = instantSerializer.instantToByteArray(dec14);
        ;
        bytes.put("A44".getBytes(), s55RecBin);

        bytes.put("o".getBytes(), OptOutType.weve.toString().getBytes());

        DeviceData result = testObj.parse(bytes);
        Assert.assertEquals(2, result.getRecencyByAudience().size());
        Assert.assertEquals(dec13, result.getRecencyByAudience().get(13L));
        Assert.assertEquals(dec14, result.getRecencyByAudience().get(44L));

        Assert.assertTrue(result.getAudienceIds().isEmpty());
        Assert.assertEquals(OptOutType.weve, result.getOptOutType());

    }

}

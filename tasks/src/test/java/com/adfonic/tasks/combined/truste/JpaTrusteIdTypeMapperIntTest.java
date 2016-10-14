package com.adfonic.tasks.combined.truste;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore("rely on database")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/device-identifier-validator-test-context.xml" })
public class JpaTrusteIdTypeMapperIntTest {

    // private TrusteIdTypeMapper testObj = new HardcodedTrusteIdTypeMapper();

    @Autowired
    private JpaTrusteIdTypeMapper testObj;

    @Test
    public void testMapAdfonicIdType() {

        Assert.assertEquals(4, testObj.mapAdfonicIdType("AnID"));
        Assert.assertEquals(6, testObj.mapAdfonicIdType("IFA"));
        Assert.assertEquals(5, testObj.mapAdfonicIdType("UDID"));
        Assert.assertEquals(7, testObj.mapAdfonicIdType("IFA-SHA1"));
        Assert.assertEquals(1, testObj.mapAdfonicIdType("AnID-SHA1"));
        Assert.assertEquals(3, testObj.mapAdfonicIdType("UDID-SHA1"));

        Assert.assertEquals(0, testObj.mapAdfonicIdType("IFA-SHA2"));
        Assert.assertEquals(0, testObj.mapAdfonicIdType("MAC"));
        Assert.assertEquals(0, testObj.mapAdfonicIdType("IMEI"));
        Assert.assertEquals(0, testObj.mapAdfonicIdType("MEID-ESN"));
        Assert.assertEquals(0, testObj.mapAdfonicIdType("MAC-SHA3"));
        Assert.assertEquals(0, testObj.mapAdfonicIdType("UDID-MD5"));
        Assert.assertEquals(0, testObj.mapAdfonicIdType("IMEI-SHA1"));
        Assert.assertEquals(0, testObj.mapAdfonicIdType("MEID-ESN-SHA1"));

    }

    @Test
    public void testMapAdfonicIdTypeLowerCase() {

        Assert.assertEquals(4, testObj.mapAdfonicIdType("anid"));
        Assert.assertEquals(6, testObj.mapAdfonicIdType("ifa"));
        Assert.assertEquals(5, testObj.mapAdfonicIdType("udid"));
        Assert.assertEquals(7, testObj.mapAdfonicIdType("ifa-sha1"));
        Assert.assertEquals(1, testObj.mapAdfonicIdType("anid-sha1"));
        Assert.assertEquals(3, testObj.mapAdfonicIdType("udid-sha1"));

    }

}

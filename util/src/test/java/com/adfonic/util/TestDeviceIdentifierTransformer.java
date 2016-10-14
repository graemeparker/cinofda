package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestDeviceIdentifierTransformer extends AbstractAdfonicTest {
    @Test
    public void testTransform01_all_general() {
        String deviceIdentifier = randomAlphaNumericString(10);
        for (DeviceIdentifierTransformer transformer : DeviceIdentifierTransformer.values()) {
            assertNotNull(transformer.transform(deviceIdentifier));
        }
    }

    @Test
    public void testTransform02_AS_IS_40() {
        String deviceIdentifier = randomHexString(40);
        assertEquals(deviceIdentifier, DeviceIdentifierTransformer.AS_IS.transform(deviceIdentifier));
    }

    @Test
    public void testTransform03_AS_IS_16() {
        String deviceIdentifier = randomHexString(16);
        assertEquals(deviceIdentifier, DeviceIdentifierTransformer.AS_IS.transform(deviceIdentifier));
    }

    @Test
    public void testTransform04_SHA1_40() {
        String deviceIdentifier = randomHexString(40);
        assertEquals(DigestUtils.shaHex(deviceIdentifier), DeviceIdentifierTransformer.SHA1.transform(deviceIdentifier));
    }

    @Test
    public void testTransform04_SHA1_16() {
        String deviceIdentifier = randomHexString(16);
        assertEquals(DigestUtils.shaHex(deviceIdentifier), DeviceIdentifierTransformer.SHA1.transform(deviceIdentifier));
    }
}

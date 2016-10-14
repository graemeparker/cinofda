package com.adfonic.util;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestDeviceIdentifierUtils extends AbstractAdfonicTest {
    @Test
    public void test01_normalizeIfa_already_hyphenated_properly() {
        String ifa = UUID.randomUUID().toString();
        assertEquals(ifa, DeviceIdentifierUtils.normalizeIfa(ifa));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test02_normalizeIfa_not_hyphenated_but_invalid_length_too_short() {
        String ifa = randomHexString(31);
        DeviceIdentifierUtils.normalizeIfa(ifa);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test03_normalizeIfa_not_hyphenated_but_invalid_length_too_long() {
        String ifa = randomHexString(33);
        DeviceIdentifierUtils.normalizeIfa(ifa);
    }

    @Test
    public void test04_normalizeIfa_non_hyphenated() {
        String uuid = UUID.randomUUID().toString();
        String ifa = uuid.replaceAll("-", "");
        assertEquals(uuid, DeviceIdentifierUtils.normalizeIfa(ifa));
    }
}

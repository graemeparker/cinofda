package com.adfonic.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestBase64 extends AbstractAdfonicTest {
    @Test
    public void encodeString() {
        assertEquals("YWJjZHNsZGZramRhcw==", Base64.encodeString("abcdsldfkjdas"));
    }

    @Test
    public void encodeString2() {
        assertEquals("cGxlYXN1cmUu", Base64.encodeString("pleasure."));
    }

    @Test
    public void compareToCommonsCodec() {
        String value = "abcdsldfkjdas";
        String ours = Base64.encodeString(value);
        String theirs = org.apache.commons.codec.binary.Base64.encodeBase64String(value.getBytes()).trim();
        assertEquals(theirs, ours);
    }

    @Test
    public void symmetry() {
        for (int length = 1; length <= 30; ++length) {
            String value = randomAlphaNumericString(length);
            assertEquals("At length=" + length, value, Base64.decodeString(Base64.encodeString(value)));
        }
    }
}
package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestIpAddressUtils {
    private static final class TestCase {
        private long value;
        private byte[] bytes;
        private String ip;

        private TestCase(long value, byte[] bytes, String ip) {
            this.value = value;
            this.bytes = bytes;
            this.ip = ip;
        }
    }

    private static final TestCase[] TEST_CASES = new TestCase[] { new TestCase(2066563929L, new byte[] { (byte) 123, (byte) 45, (byte) 67, (byte) 89 }, "123.45.67.89"),
            new TestCase(3232235521L, new byte[] { (byte) 192, (byte) 168, (byte) 0, (byte) 1 }, "192.168.0.1"),
            new TestCase(169090815L, new byte[] { (byte) 10, (byte) 20, (byte) 30, (byte) 255 }, "10.20.30.255"),
            new TestCase(123456789L, new byte[] { (byte) 7, (byte) 91, (byte) 205, (byte) 21 }, "7.91.205.21"),
            new TestCase(16909060L, new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4 }, "1.2.3.4"), };

    @Test(expected = java.net.UnknownHostException.class)
    public void testIpAddressToLong_invalid() throws Exception {
        IpAddressUtils.ipAddressToLong("no chance this will resolve, no way");
        fail("should have thrown");
    }

    @Test
    public void testIpAddressToLong_valid() throws Exception {
        for (TestCase testCase : TEST_CASES) {
            assertEquals(testCase.value, IpAddressUtils.ipAddressToLong(testCase.ip));
        }
    }

    @Test
    public void testBytesToLong() throws Exception {
        for (TestCase testCase : TEST_CASES) {
            assertEquals(testCase.value, IpAddressUtils.bytesToLong(testCase.bytes));
        }
    }

    @Test
    public void testLongToIpAddress() throws Exception {
        for (TestCase testCase : TEST_CASES) {
            assertEquals(testCase.ip, IpAddressUtils.longToIpAddress(testCase.value));
        }
    }
}

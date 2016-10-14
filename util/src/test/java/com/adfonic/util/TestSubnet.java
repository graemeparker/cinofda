package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import org.junit.Test;

public class TestSubnet {
    private static final transient Logger LOG = Logger.getLogger(TestSubnet.class.getName());

    static final class TestCase {
        String cidr;
        boolean expectedToParse;
        boolean isIp;
        boolean isPrivate;

        TestCase(String ip) {
            this.cidr = ip;
            this.expectedToParse = true;
            this.isIp = true;
        }

        TestCase(String cidr, boolean expectedToParse, boolean isIp) {
            this(cidr, expectedToParse, isIp, false);
        }

        TestCase(String cidr, boolean expectedToParse, boolean isIp, boolean isPrivate) {
            this.cidr = cidr;
            this.expectedToParse = expectedToParse;
            this.isIp = isIp;
            this.isPrivate = isPrivate;
        }
    }

    static final TestCase[] testCases = new TestCase[] { new TestCase("", false, false), new TestCase("abc", false, false), new TestCase("123", false, false),
            new TestCase("123.45", false, false), new TestCase("123.45.67", false, false), new TestCase("123.45.67.89"), new TestCase("123.45.67.89/", false, false),
            new TestCase("123.45.67.89/0", false, false), new TestCase("123.45.67.89/32", true, false), new TestCase("123.45.67.89/99", false, false), new TestCase("127.0.0.1"),
            new TestCase("192.168.0.0/16", true, false), new TestCase("192.168.0.0/0", false, false), new TestCase("192.168.0.0/32", true, false),
            new TestCase("192.168.0.0/31", true, false), new TestCase("255.255.255.255"), new TestCase("255.255.255.255/1", true, false),
            new TestCase("255.255.255.255/32", true, false), new TestCase("987.65.43.21", false, false), new TestCase("987.65.43.21/32", false, false),
            new TestCase("87.965.43.21", false, false), new TestCase("87.965.43.21/32", false, false), new TestCase("87.65.943.21", false, false),
            new TestCase("87.65.943.21/32", false, false), new TestCase("87.65.43.921", false, false), new TestCase("87.65.43.921/32", false, false),
            new TestCase("256.0.0.0", false, false), new TestCase("0.0.0.0"), new TestCase("1.1.1.1"), new TestCase("92.40.253.0/24", true, false),
            new TestCase("92.40.254.0/24", true, false), new TestCase("94.197.127.0/24", true, false), new TestCase("94.196.232.112/32", true, false),
            new TestCase("82.132.139.0/24", true, false), new TestCase("82.132.210.0/24", true, false), new TestCase("82.132.211.0/24", true, false),
            new TestCase("82.132.242.0/24", true, false), new TestCase("82.132.243.0/24", true, false), new TestCase("82.132.248.0/24", true, false),
            new TestCase("82.132.249.0/24", true, false), new TestCase("178.96.0.0/16", true, false), new TestCase("178.111.255.255/32", true, false),
            new TestCase("10.56.65.247", true, true, true), new TestCase("10.0.0.0", true, true, true), new TestCase("10.255.255.255", true, true, true),
            new TestCase("172.16.0.0", true, true, true), new TestCase("172.16.255.255", true, true, true), new TestCase("192.168.0.0", true, true, true),
            new TestCase("192.168.0.255", true, true, true), };

    @Test
    public void test() throws Exception {
        Subnet subnet;

        /*
         * org.apache.commons.lang.time.StopWatch stopWatch = new
         * org.apache.commons.lang.time.StopWatch(); stopWatch.start(); for (int
         * k = 0; k < 1000000; ++k) { for (TestCase testCase : testCases) { if
         * (testCase.expectedToParse) { subnet = new Subnet(testCase.cidr); }
         * Subnet.isIpAddress(testCase.cidr); } } stopWatch.stop();
         * LOG.info("Elapsed: " + stopWatch);
         */

        for (TestCase testCase : testCases) {
            /*
             * subnet = new Subnet(testCase.cidr); System.out.println(
             * "-- ==========================================================");
             * System.out.println("-- " + subnet.getCidr()); System.out.println(
             * "SELECT ID INTO @countryId FROM COUNTRY WHERE ISO_CODE='';");
             * System.out.println(
             * "SELECT ID INTO @operatorId FROM OPERATOR WHERE COUNTRY_ID=@countryId AND NAME='';"
             * ); System.out.println(
             * "INSERT INTO MOBILE_IP_ADDRESS_RANGE (START_POINT, END_POINT, CARRIER, COUNTRY_ID, OPERATOR_ID, SOURCE, PRIORITY) VALUES ("
             * + subnet.getLow() + "," + subnet.getHigh() +
             * ",'carrier',@countryId,@operatorId,'ADFONIC',1);");
             */

            // LOG.info(testCase.cidr + ", expectedToParse=" +
            // testCase.expectedToParse + ", isIp=" + testCase.isIp);
            try {
                subnet = new Subnet(testCase.cidr);
                assertTrue("Subnet should not have parsed: " + testCase.cidr, testCase.expectedToParse);
                LOG.info(testCase.cidr + " ==> " + subnet.toString());
            } catch (Exception e) {
                assertFalse("Subnet should have parsed: " + testCase.cidr, testCase.expectedToParse);
            }

            boolean isIp = Subnet.isIpAddress(testCase.cidr);
            assertEquals("Subnet.isIpAddress(" + testCase.cidr + ")", isIp, testCase.isIp);

            if (testCase.isPrivate) {
                assertEquals("Subnet.isOnPrivateNetwork(" + testCase.cidr + ")", Subnet.isOnPrivateNetwork(testCase.cidr), testCase.isPrivate);
            }
        }

        subnet = new Subnet("192.168.0.0/16");
        assertTrue("contains check 1", subnet.contains("192.168.0.1"));
        assertTrue("contains check 2", subnet.contains("192.168.255.255"));
        assertTrue("contains check 3", subnet.contains("192.168.0.255"));
        assertTrue("contains check 4", subnet.contains("192.168.255.0"));
        assertFalse("contains check 5", subnet.contains("192.169.0.1"));
        assertFalse("contains check 6", subnet.contains("193.168.0.1"));
        assertFalse("contains check 7", subnet.contains("191.168.0.1"));
        assertFalse("contains check 8", subnet.contains("192.167.255.255"));
        assertFalse("contains check 9", subnet.contains("192.169.0.0"));
    }
}

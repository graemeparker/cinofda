package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class TestLoadBalancingUtils {
    private static final int NUM_ADSERVERS = 35;
    private static final String[] SERVER_LIST = StringUtils.split("ch1gds01:8080,ch1gds02:8080", ',');

    @Test
    public void test() {
        int[] clientCount = new int[SERVER_LIST.length];
        for (int k = 0; k < clientCount.length; ++k) {
            clientCount[k] = 0;
        }

        System.out.println("Assignments:");
        for (int a = 0; a < NUM_ADSERVERS; ++a) {
            if (a > 0 && a % SERVER_LIST.length == 0) {
                System.out.println("-----------");
            }
            int primary = LoadBalancingUtils.getPrimaryServerIndex(a, SERVER_LIST.length);
            int failover = LoadBalancingUtils.getFailoverServerIndex(a, SERVER_LIST.length);
            assertFalse(primary == failover);

            ++clientCount[primary];
            System.out.println("ch1adserver" + make2Digit(a + 1) + ".adfonic.com => primary=" + SERVER_LIST[primary] + ", failover=" + SERVER_LIST[failover]);
        }
        System.out.println();

        System.out.println("********************************************************");
        System.out.println("*** When all GDS servers are up...");
        for (int k = 0; k < clientCount.length; ++k) {
            System.out.println(SERVER_LIST[k] + " has " + clientCount[k] + " clients");
        }

        for (int failedGds = 0; failedGds < SERVER_LIST.length; ++failedGds) {
            for (int k = 0; k < clientCount.length; ++k) {
                clientCount[k] = 0;
            }

            for (int a = 0; a < NUM_ADSERVERS; ++a) {
                int primary = LoadBalancingUtils.getPrimaryServerIndex(a, SERVER_LIST.length);
                int failover = LoadBalancingUtils.getFailoverServerIndex(a, SERVER_LIST.length);
                if (primary == failedGds) {
                    ++clientCount[failover];
                } else {
                    ++clientCount[primary];
                }
            }

            assertEquals(clientCount[failedGds], 0);

            System.out.println("********************************************************");
            System.out.println("*** When " + SERVER_LIST[failedGds] + " is down...");
            for (int k = 0; k < clientCount.length; ++k) {
                System.out.println(SERVER_LIST[k] + " has " + clientCount[k] + " clients");
            }
        }
    }

    private static final String make2Digit(int val) {
        return val < 10 ? ("0" + val) : String.valueOf(val);
    }
}

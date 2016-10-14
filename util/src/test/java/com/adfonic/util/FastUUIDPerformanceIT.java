package com.adfonic.util;

import java.util.UUID;

import org.junit.Test;
import org.springframework.util.StopWatch;

public class FastUUIDPerformanceIT {
    private static final int PERFORMANCE_TEST_ITERATIONS = 10000000;

    @Test
    public void testPerformanceOfFastUUIDRandomUUID() {
        StopWatch stopWatch = new StopWatch("FastUUID.randomUUID(), " + PERFORMANCE_TEST_ITERATIONS + " iterations");
        stopWatch.start();
        for (int k = 0; k < PERFORMANCE_TEST_ITERATIONS; ++k) {
            FastUUID.randomUUID().toString();
        }
        stopWatch.stop();
        System.out.println(stopWatch.toString());
    }

    @Test
    public void testPerformanceOfUUIDRandomUUID() {
        StopWatch stopWatch = new StopWatch("UUID.randomUUID(), " + PERFORMANCE_TEST_ITERATIONS + " iterations");
        stopWatch.start();
        for (int k = 0; k < PERFORMANCE_TEST_ITERATIONS; ++k) {
            UUID.randomUUID().toString();
        }
        stopWatch.stop();
        System.out.println(stopWatch.toString());
    }
}

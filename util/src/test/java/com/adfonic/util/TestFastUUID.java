package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

public class TestFastUUID {
    // Originally I used a much higher number (and increased the test heap) to
    // make
    // sure FastUUID.randomUUID() was producing unique results. Like 100 million
    // iterations. From a unit test standpoint, we can get away with far fewer
    // iterations. At this point it's mostly about code coverage, and making
    // sure
    // nobody royally botches the ability to produce unique results.
    private static final int UNIQUENESS_TEST_ITERATIONS = 10000;

    @Test
    public void testRandomUUID() {
        UUID uuid = FastUUID.randomUUID();
        assertNotNull(uuid);
        assertEquals(36, uuid.toString().length());
    }

    @Test
    public void testUniqueness() {
        Set<String> alreadySeen = new HashSet<String>(UNIQUENESS_TEST_ITERATIONS);
        for (int k = 0; k < UNIQUENESS_TEST_ITERATIONS; ++k) {
            String value = FastUUID.randomUUID().toString();
            assertFalse("Uniqueness failed on the " + (alreadySeen.size() + 1) + "th attempt", alreadySeen.contains(value));
            alreadySeen.add(value);
        }
    }
}

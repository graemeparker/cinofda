package com.adfonic.util.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TestCounterManager {
    @Test
    public void testWithStringName() {
        String[] names = new String[] { "c1", "whatever", "com.adfonic.something.Important", "FOO.BAR.BAZ" };
        CounterManager c = new CounterManager();
        assertTrue(c.getCounterNames().isEmpty());
        for (String name : names) {
            assertEquals(0L, c.getCount(name));
        }
        Set<String> counterNames = c.getCounterNames();
        assertEquals(names.length, counterNames.size());
        for (String name : names) {
            assertTrue(counterNames.contains(name));
        }
        for (int k = 1; k <= 100; ++k) {
            for (String name : names) {
                c.incrementCounter(name);
                assertEquals(k, c.getCount(name));
            }
        }
        for (String name : names) {
            c.resetCounter(name);
            assertEquals(0L, c.getCount(name));
        }
    }

    private enum CounterType {
        IP_INVALID, TARGETING_CONTEXT_EXCEPTION, ADSPACE_DORMANT, ADSPACE_INVALID, USERAGENT_MISSING, USERAGENT_UNWELCOME, UPLIFT, MOFO, PARTY, PLAN
    }

    @Test
    public void testWithEnum() {
        CounterManager c = new CounterManager();
        assertTrue(c.getCounterNames().isEmpty());
        for (CounterType counterType : CounterType.values()) {
            assertEquals(0L, c.getCount(counterType));
        }
        Set<String> counterNames = c.getCounterNames();
        assertEquals(CounterType.values().length, counterNames.size());
        for (CounterType counterType : CounterType.values()) {
            assertTrue(counterNames.contains(counterType.name()));
        }
        for (int k = 1; k <= 100; ++k) {
            for (CounterType counterType : CounterType.values()) {
                c.incrementCounter(counterType);
                assertEquals(k, c.getCount(counterType));
            }
        }
        for (CounterType counterType : CounterType.values()) {
            c.resetCounter(counterType);
            assertEquals(0L, c.getCount(counterType));
        }
    }

    @Test
    public void testWithEnumPublishersNotPresent() {
        Set<Long> publishers = new HashSet<>();
        publishers.add(1L);
        publishers.add(2L);
        publishers.add(3L);

        CounterManager c = new CounterManager(publishers);
        assertTrue(c.getCounterNames().isEmpty());

        for (CounterType counterType : CounterType.values()) {
            assertEquals(0L, c.getCount(counterType));
        }
        Set<String> counterNames = c.getCounterNames();
        assertEquals(CounterType.values().length, counterNames.size());
        for (CounterType counterType : CounterType.values()) {
            assertTrue(counterNames.contains(counterType.name()));
        }

        c.incrementCounter(this.getClass(), 5L, CounterType.UPLIFT);
        assertEquals(0L, c.getCount(this.getClass().getName() + "." + CounterType.UPLIFT.name() + "." + String.valueOf(5L)));
        assertEquals(1L, c.getCount(this.getClass(), CounterType.UPLIFT));
    }

    @Test
    public void testWithEnumPublishersPresent() {
        Set<Long> publishers = new HashSet<>();
        publishers.add(1L);
        publishers.add(2L);
        publishers.add(3L);

        CounterManager c = new CounterManager(publishers);
        assertTrue(c.getCounterNames().isEmpty());

        for (CounterType counterType : CounterType.values()) {
            assertEquals(0L, c.getCount(counterType));
        }
        Set<String> counterNames = c.getCounterNames();
        assertEquals(CounterType.values().length, counterNames.size());
        for (CounterType counterType : CounterType.values()) {
            assertTrue(counterNames.contains(counterType.name()));
        }

        c.incrementCounter(this.getClass(), 3L, CounterType.UPLIFT);
        assertEquals(1L, c.getCount(this.getClass().getName() + "." + CounterType.UPLIFT.name() + "." + String.valueOf(3L)));
        assertEquals(1L, c.getCount(this.getClass(), CounterType.UPLIFT));
    }

    @Test
    public void testWithAssociatedClassAndEnum() {
        CounterManager c = new CounterManager();
        assertTrue(c.getCounterNames().isEmpty());
        for (CounterType counterType : CounterType.values()) {
            assertEquals(0L, c.getCount(TestCounterManager.class, counterType));
        }
        Set<String> counterNames = c.getCounterNames();
        assertEquals(CounterType.values().length, counterNames.size());
        for (CounterType counterType : CounterType.values()) {
            assertTrue(counterNames.contains(TestCounterManager.class.getName() + "." + counterType.name()));
        }
        for (int k = 1; k <= 100; ++k) {
            for (CounterType counterType : CounterType.values()) {
                c.incrementCounter(TestCounterManager.class, counterType);
                assertEquals(k, c.getCount(TestCounterManager.class, counterType));
            }
        }
        for (CounterType counterType : CounterType.values()) {
            c.resetCounter(TestCounterManager.class, counterType);
            assertEquals(0L, c.getCount(TestCounterManager.class, counterType));
        }
    }

    @Test
    public void testSetCount() {
        CounterManager c = new CounterManager();
        assertTrue(c.getCounterNames().isEmpty());
        long count = 10l;
        c.setCount("TestCount", count);
        assertEquals(count, c.getCount("TestCount"));
    }
}
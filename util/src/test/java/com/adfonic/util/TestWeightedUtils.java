package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestWeightedUtils {
    private static final class WeightedImpl implements Weighted {
        private final int weight;

        private WeightedImpl(int weight) {
            this.weight = weight;
        }

        @Override
        public double getWeight() {
            return weight;
        }
    }

    @Test
    public void testGetRandomWeighted01_null() {
        assertNull(WeightedUtils.getRandomWeighted(null, 1.0));
    }

    @Test
    public void testGetRandomWeighted02_empty() {
        List<WeightedImpl> list = new ArrayList<WeightedImpl>();
        assertNull(WeightedUtils.getRandomWeighted(list, 1.0));
    }

    @Test
    public void testGetRandomWeighted03_single_entry() {
        WeightedImpl impl = new WeightedImpl(1);
        List<WeightedImpl> list = new ArrayList<WeightedImpl>();
        list.add(impl);
        assertEquals(impl, WeightedUtils.getRandomWeighted(list, 1.0));
    }

    @Test
    public void testGetRandomWeighted04_zero_total_weight() {
        // For code coverage
        List<WeightedImpl> list = new ArrayList<WeightedImpl>();
        for (int k = 0; k < 10; ++k) {
            list.add(new WeightedImpl(0));
        }
        WeightedUtils.getRandomWeighted(list, 1.0);
    }

    @Test
    public void testGetRandomWeighted05_negative_total_weight() {
        // For code coverage
        List<WeightedImpl> list = new ArrayList<WeightedImpl>();
        for (int k = 0; k < 10; ++k) {
            list.add(new WeightedImpl(-1));
        }
        WeightedUtils.getRandomWeighted(list, 1.0);
    }

    @Test
    public void testGetRandomWeighted06_valid() {
        List<WeightedImpl> list = new ArrayList<WeightedImpl>();
        list.add(new WeightedImpl(20));
        list.add(new WeightedImpl(15));
        list.add(new WeightedImpl(14));
        list.add(new WeightedImpl(10));
        list.add(new WeightedImpl(9));
        list.add(new WeightedImpl(7));
        list.add(new WeightedImpl(7));
        list.add(new WeightedImpl(3));
        list.add(new WeightedImpl(3));
        list.add(new WeightedImpl(1));
        for (int k = 0; k < 10000; ++k) {
            WeightedUtils.getRandomWeighted(list, 1.0);
        }
    }
}

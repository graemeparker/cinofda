package com.adfonic.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestRange {
    enum DayOfWeek {
        Sun, Mon, Tue, Wed, Thu, Fri, Sat
    };

    @Test
    public void testPoint() {
        new Range<Integer>(1);
    }

    @Test
    public void testCopyConstructor() {
        Range<Integer> range1 = new Range<Integer>(1, 10);
        new Range<Integer>(range1);
    }

    @Test
    public void testIsPoint() {
        assertFalse(new Range<Integer>(1, 123).isPoint());
        assertTrue(new Range<Integer>(123, 123).isPoint());
    }

    @Test
    public void testContains() {
        Range<Integer> range = new Range<Integer>(1, 10);
        for (int k = 1; k <= 10; ++k) {
            assertTrue(range.contains(k));
        }
        assertFalse(range.contains(-1));
        assertFalse(range.contains(-10));
        assertFalse(range.contains(0));
        assertFalse(range.contains(123));
    }

    @Test
    public void testEquals() {
        Range<Integer> r1a = new Range<Integer>(1000, 2000);
        Range<Integer> r1b = new Range<Integer>(1000, 2000);
        Range<Integer> r2 = new Range<Integer>(2000, 3000);

        Range<Integer> r1i = new Range<Integer>(1000, 2000, true);

        assertFalse(r1a.equals(null));
        assertTrue(r1a.equals(r1a));
        assertTrue(r1a.equals(r1b));
        assertTrue(r1b.equals(r1a));
        assertFalse(r1a.equals("not a range"));
        assertFalse(r1a.equals(r2));
        assertFalse(r2.equals(r1a));
        assertFalse(r1a.equals(r1i));
        assertFalse(r1i.equals(r1a));
    }

    @Test
    public void test1() {
        List<Range<Integer>> list = new ArrayList<Range<Integer>>();
        Range.combine(list, new Range<Integer>(1, 2));
        Range.combine(list, new Range<Integer>(2, 3));
        Range.combine(list, new Range<Integer>(7, 8));
        Range.combine(list, new Range<Integer>(4, 5));
        Range.combine(list, new Range<Integer>(6, 7));
        for (Range<Integer> r : list) {
            System.out.println(r);
        }

        list = new ArrayList<Range<Integer>>();
        Range.combine(list, 0);
        Range.combine(list, 1);
        Range.combine(list, 2);
        Range.combine(list, 4);
        Range.combine(list, 5);
        for (Range<Integer> r : list) {
            System.out.println(r);
        }

        List<Range<DayOfWeek>> list2 = new ArrayList<Range<DayOfWeek>>();
        Range.combine(list2, DayOfWeek.Sun);
        Range.combine(list2, DayOfWeek.Thu);
        Range.combine(list2, DayOfWeek.Wed);
        Range.combine(list2, DayOfWeek.Sat);
        Range.combine(list2, DayOfWeek.Mon);
        Range.combine(list2, DayOfWeek.Fri);

        for (Range<DayOfWeek> r : list2) {
            System.out.println(r);
        }

        list.clear();
        Range.combine(list, new Range<Integer>(1, 2));
        Range.combine(list, new Range<Integer>(2, 4));
        Range.combine(list, new Range<Integer>(3, 5));
        Range.combine(list, new Range<Integer>(6, 7));
        for (Range<Integer> r : list) {
            System.out.println(r);
        }
    }
}

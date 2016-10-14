package com.adfonic.util;

import static org.junit.Assert.assertEquals;

import java.util.Enumeration;
import java.util.Vector;

import org.junit.Test;

public class TestEnumerationUtils {
    @Test
    public void testMakeIteratable01() {
        Vector<Integer> vector = new Vector<Integer>();
        for (int k = 0; k < 100; ++k) {
            vector.add(k);
        }
        Enumeration<Integer> e = vector.elements();
        Iterable<Integer> iterable = EnumerationUtils.makeIterable(e);
        int counter = 0;
        for (Integer value : iterable) {
            assertEquals((Object) counter, value);
            ++counter;
        }
    }

    @Test
    public void testMakeIteratable02() {
        Vector<Integer> vector = new Vector<Integer>();
        for (int k = 0; k < 100; ++k) {
            vector.add(k);
        }
        Enumeration<Integer> e = vector.elements();
        Iterable<Integer> iterable = EnumerationUtils.makeIterable(e);
        int counter = 0;
        for (Integer value : iterable) {
            assertEquals((Object) counter, value);
            ++counter;
        }
    }
}

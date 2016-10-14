package com.adfonic.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestAdXUtils {
    @Test
    public void testIsValidAdxApplicationID() {
        assertFalse(AdXUtils.isValidAdXApplicationID(null));
        assertFalse(AdXUtils.isValidAdXApplicationID(""));
        assertFalse(AdXUtils.isValidAdXApplicationID(" "));
        assertFalse(AdXUtils.isValidAdXApplicationID("a.b.c "));
        assertFalse(AdXUtils.isValidAdXApplicationID("a.b.c.1"));

        assertTrue(AdXUtils.isValidAdXApplicationID("A.b.c"));
        assertTrue(AdXUtils.isValidAdXApplicationID("."));
        assertTrue(AdXUtils.isValidAdXApplicationID("a"));
        assertTrue(AdXUtils.isValidAdXApplicationID("a.b.c.d.e.f.g"));
    }
}

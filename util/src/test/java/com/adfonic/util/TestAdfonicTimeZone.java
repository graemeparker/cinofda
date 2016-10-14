package com.adfonic.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.TimeZone;

import org.junit.Test;

public class TestAdfonicTimeZone {
    @Test
    public void testGetAdfonicTimeZoneById() {
        assertNotNull(AdfonicTimeZone.getAdfonicTimeZoneById("America/Los_Angeles"));
        assertNotNull(AdfonicTimeZone.getAdfonicTimeZoneById("Asia/Tehran"));
        assertNotNull(AdfonicTimeZone.getAdfonicTimeZoneById("Europe/London"));
        assertNull(AdfonicTimeZone.getAdfonicTimeZoneById(""));
        assertNull(AdfonicTimeZone.getAdfonicTimeZoneById(null));
        assertNull(AdfonicTimeZone.getAdfonicTimeZoneById("junk that won't match anything"));
    }

    @Test
    public void testGetDescription() {
        // For code coverage
        AdfonicTimeZone.getAdfonicTimeZoneById("America/Los_Angeles").getDescription();
        AdfonicTimeZone.getAdfonicTimeZoneById("America/Los_Angeles").getDescription();
    }

    @Test
    public void testGetAdfonicTimeZoneDescription() {
        // For code coverage
        AdfonicTimeZone.getAdfonicTimeZoneDescription(null);
        AdfonicTimeZone.getAdfonicTimeZoneDescription(TimeZone.getTimeZone("Asia/Tehran"));
    }
}

package com.adfonic.geo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestGeoUtils {
    @Test
    public void test() throws Exception {
        assertTrue(GeoUtils.validateCoordinates(new SimpleCoordinates(0, 0)));
        assertTrue(GeoUtils.validateCoordinates(new SimpleCoordinates(-90.0, -180.0)));
        assertTrue(GeoUtils.validateCoordinates(new SimpleCoordinates(-90.0, 180.0)));
        assertTrue(GeoUtils.validateCoordinates(new SimpleCoordinates(90.0, -180.0)));
        assertTrue(GeoUtils.validateCoordinates(new SimpleCoordinates(90.0, 180.0)));
        assertFalse(GeoUtils.validateCoordinates(new SimpleCoordinates(90.00001, 0)));
        assertFalse(GeoUtils.validateCoordinates(new SimpleCoordinates(-90.00001, 0)));
        assertFalse(GeoUtils.validateCoordinates(new SimpleCoordinates(0, 180.00001)));
        assertFalse(GeoUtils.validateCoordinates(new SimpleCoordinates(0, -180.00001)));
        assertFalse(GeoUtils.validateCoordinates(new SimpleCoordinates(1234, 1234)));
        assertFalse(GeoUtils.validateCoordinates(new SimpleCoordinates(-1.0, -1.0)));
    }
}
package com.adfonic.geo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSimpleCoordinates {
    @Test
    public void test() throws Exception {
        SimpleCoordinates coords = new SimpleCoordinates();
        assertEquals(0.0, coords.getLatitude(), 0.000001);
        assertEquals(0.0, coords.getLongitude(), 0.000001);

        coords = new SimpleCoordinates(45, 45);
        assertEquals(45.0, coords.getLatitude(), 0.000001);
        assertEquals(45.0, coords.getLongitude(), 0.000001);
    }

    @Test(expected=SimpleCoordinates.InvalidCoordinatesException.class)
    public void stringConstructorThrowsWhenEmpty() {
        new SimpleCoordinates("");
    }

    @Test(expected=SimpleCoordinates.InvalidCoordinatesException.class)
    public void stringConstructorThrowsWithNotEnoughTokens() {
        new SimpleCoordinates("123.45");
    }

    @Test(expected=SimpleCoordinates.InvalidCoordinatesException.class)
    public void stringConstructorThrowsWithTooManyTokens() {
        new SimpleCoordinates("123,456,789");
    }

    @Test
    public void stringConstructorWorks() {
        double lat = 33.4444;
        double lon = -100.12345;
        SimpleCoordinates sc = new SimpleCoordinates(String.valueOf(lat) + "," + String.valueOf(lon));
        assertEquals(lat, sc.getLatitude(), 0.0000001);
        assertEquals(lon, sc.getLongitude(), 0.0000001);
    }

    @Test
    public void stringConstructorTrims() {
        double lat = 33.4444;
        double lon = -100.12345;
        SimpleCoordinates sc = new SimpleCoordinates("   " + String.valueOf(lat) + "  ,  " + String.valueOf(lon) + "     ");
        assertEquals(lat, sc.getLatitude(), 0.0000001);
        assertEquals(lon, sc.getLongitude(), 0.0000001);
    }
}
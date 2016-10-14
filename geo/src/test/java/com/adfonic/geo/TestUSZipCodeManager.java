package com.adfonic.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestUSZipCodeManager extends AbstractAdfonicTest {
    @Test
    public void testGetNumberOfHeaderLinesToSkip() {
        USZipCodeManager mgr = new USZipCodeManager(null, 0);
        assertEquals(2, mgr.getNumberOfHeaderLinesToSkip());
    }

    @Test
    public void testProcessCsvLine01_normal() {
        String city = randomAlphaNumericString(10);
        String state = randomAlphaNumericString(10);
        String zip = randomAlphaNumericString(10);
        String ac = randomAlphaNumericString(10);
        String fips = randomAlphaNumericString(10);
        String county = randomAlphaNumericString(10);
        String tz = randomAlphaNumericString(10);
        String dst = randomAlphaNumericString(10);
        double latitude = 35.12345;
        double longitude = -123.45678;
        String type = randomAlphaNumericString(10);

        String[] line = new String[] { city, state, zip, ac, fips, county, tz, dst, String.valueOf(latitude), String.valueOf(longitude), type, };

        Map<String, USZipCode> map = new HashMap<String, USZipCode>();

        USZipCodeManager mgr = new USZipCodeManager(null, 0);
        mgr.processCsvLine(line, map);

        assertEquals(1, map.size());
        USZipCode uzc = map.get(zip);
        assertNotNull(uzc);
        assertEquals(city, uzc.getCity());
        assertEquals(state, uzc.getState());
        assertEquals(zip, uzc.getZip());
        assertEquals(county, uzc.getCounty());
        assertEquals(latitude, uzc.getLatitude(), 0.0);
        assertEquals(longitude, uzc.getLongitude(), 0.0);
    }

    @Test
    public void testProcessCsvLine02_single_token() {
        String[] line = new String[] { "^Z", };

        Map<String, USZipCode> map = new HashMap<String, USZipCode>();

        USZipCodeManager mgr = new USZipCodeManager(null, 0);
        mgr.processCsvLine(line, map);

        assertTrue(map.isEmpty());
    }
}

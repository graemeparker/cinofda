package com.adfonic.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.adfonic.geo.postalcode.GeneralPostalCode;
import com.adfonic.test.AbstractAdfonicTest;

public class TestSpanishPostalCodeManager extends AbstractAdfonicTest {
    @Test
    public void testGetNumberOfHeaderLinesToSkip() {
        SpanishPostalCodeManager mgr = new SpanishPostalCodeManager(null, 0);
        assertEquals(1, mgr.getNumberOfHeaderLinesToSkip());
    }

    @Test
    public void testProcessCsvLine() {
        String city = randomAlphaNumericString(10);
        String postalCode = randomAlphaNumericString(10);
        String prov = randomAlphaNumericString(10);
        double latitude = 35.12345;
        double longitude = -123.45678;

        String[] line = new String[] { postalCode, city, prov, String.valueOf(latitude), String.valueOf(longitude) };

        Map<String, GeneralPostalCode> map = new HashMap<String, GeneralPostalCode>();

        SpanishPostalCodeManager mgr = new SpanishPostalCodeManager(null, 0);
        mgr.processCsvLine(line, map);

        assertEquals(1, map.size());
        GeneralPostalCode cpc = map.get(postalCode.toLowerCase());
        assertNotNull(cpc);
        assertEquals("ES", cpc.getCountryCode());
        assertEquals(postalCode, cpc.getPostalCode());
        assertEquals(prov, cpc.getProvince());
        assertEquals(latitude, cpc.getLatitude(), 0.0);
        assertEquals(longitude, cpc.getLongitude(), 0.0);
    }
}

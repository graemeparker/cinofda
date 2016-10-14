package com.adfonic.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestCanadianPostalCodeManager extends AbstractAdfonicTest {
    @Test
    public void testGetNumberOfHeaderLinesToSkip() {
        CanadianPostalCodeManager mgr = new CanadianPostalCodeManager(null, 0);
        assertEquals(2, mgr.getNumberOfHeaderLinesToSkip());
    }

    @Test
    public void testProcessCsvLine() {
        String city = randomAlphaNumericString(10);
        CanadianProvince canadianProvince = CanadianProvince.BC;
        String postalCode = randomAlphaNumericString(10);
        String pCode = randomAlphaNumericString(10);
        String pref = randomAlphaNumericString(10);
        String timeZone = randomAlphaNumericString(10);
        String dst = "Y";
        double latitude = 35.12345;
        double longitude = -123.45678;

        String[] line = new String[] { city, canadianProvince.name(), postalCode, pCode, pref, timeZone, dst, String.valueOf(latitude), String.valueOf(longitude) };

        Map<String, CanadianPostalCode> map = new HashMap<String, CanadianPostalCode>();

        CanadianPostalCodeManager mgr = new CanadianPostalCodeManager(null, 0);
        mgr.processCsvLine(line, map);

        assertEquals(1, map.size());
        CanadianPostalCode cpc = map.get(postalCode.toLowerCase());
        assertNotNull(cpc);
        assertEquals(postalCode, cpc.getPostalCode());
        assertEquals(canadianProvince, cpc.getCanadianProvince());
        assertEquals(latitude, cpc.getLatitude(), 0.0);
        assertEquals(longitude, cpc.getLongitude(), 0.0);
    }
}

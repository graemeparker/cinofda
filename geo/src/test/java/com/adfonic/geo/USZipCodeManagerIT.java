package com.adfonic.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

public class USZipCodeManagerIT {

    private static File coordinatesFile = null;

    @BeforeClass
    public static void init() {
        URL url = TestPostalCodeIdManagerIT.class.getClassLoader().getResource("ziplist5-geo.csv");
        coordinatesFile = new File(url.getPath());
    }

    @Test
    public void test() throws java.io.IOException {
        USZipCodeManager mgr = new USZipCodeManager(coordinatesFile, 999);
        mgr.initialize();

        USZipCode zip;

        zip = mgr.getNearest(38.2344444444444, -84.4346944444444);
        assertNotNull(zip);
        assertEquals(zip.toString(), "Georgetown", zip.getCity());
        assertEquals(zip.toString(), "Scott", zip.getCounty());
        assertEquals(zip.toString(), "KY", zip.getState());
        assertEquals(zip.toString(), "40324", zip.getZip());

        zip = mgr.getNearest(33.9747793055556, -117.636482833333);
        assertNotNull(zip);
        assertEquals(zip.toString(), "Chino", zip.getCity());
        assertEquals(zip.toString(), "San Bernardino", zip.getCounty());
        assertEquals(zip.toString(), "CA", zip.getState());
        assertEquals(zip.toString(), "91710", zip.getZip());
    }
}

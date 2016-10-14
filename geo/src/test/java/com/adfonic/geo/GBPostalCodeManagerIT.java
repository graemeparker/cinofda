package com.adfonic.geo;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

public class GBPostalCodeManagerIT {

    private static File coordinatesFile = null;

    @BeforeClass
    public static void init() {
        URL url = TestPostalCodeIdManagerIT.class.getClassLoader().getResource("gb_postal_codes.csv");
        coordinatesFile = new File(url.getPath());
    }

    @Test
    public void test() throws java.io.IOException {
        GBPostalCodeManager mgr = new GBPostalCodeManager(coordinatesFile, 999);
        mgr.initialize();

        PostalCode postalCode;

        // "AB30","Aberdeen","Aberdeen",56.8470000000,-2.4770000000
        postalCode = mgr.getNearest(56.848, -2.478);
        assertEquals(postalCode.toString(), "AB30", postalCode.getPostalCode());
        assertEquals(postalCode.toString(), "Aberdeen", postalCode.getName());

        // "YO26","York","York",53.9750000000,-1.1720000000
        postalCode = mgr.getNearest(53.976, -1.173);
        assertEquals(postalCode.toString(), "YO26", postalCode.getPostalCode());
        assertEquals(postalCode.toString(), "York", postalCode.getName());
    }
}

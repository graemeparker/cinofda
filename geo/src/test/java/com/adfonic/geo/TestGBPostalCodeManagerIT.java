package com.adfonic.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestGBPostalCodeManagerIT {

    private static File coordinatesFile = null;

    @BeforeClass
    public static void init() {
        URL url = TestPostalCodeIdManagerIT.class.getClassLoader().getResource("gb_postal_codes.csv");
        coordinatesFile = new File(url.getPath());
    }

    @Test
    public void test() throws java.io.IOException {
        GBPostalCodeManager gbPostalCodeManager = new GBPostalCodeManager(coordinatesFile, 120);
        gbPostalCodeManager.initialize();

        PostalCode pc1 = gbPostalCodeManager.getNearest(51.4775, -0.461389);
        //        System.out.println(pc.toString());

        PostalCode pc2 = gbPostalCodeManager.getNearest(51.148056, -0.190278);
        // System.out.println(pc.toString());
        assertNotNull(pc1);
        assertEquals("TW6", pc1.getPostalCode());
        assertEquals("Hounslow", pc1.getCity());

        assertNotNull(pc2);
        assertEquals("RH6", pc2.getPostalCode());
        assertEquals("Redhill", pc2.getCity());
    }

    @Test
    public void testCroydon() throws java.io.IOException {
        GBPostalCodeManager gbPostalCodeManager = new GBPostalCodeManager(coordinatesFile, 120);
        gbPostalCodeManager.initialize();

        PostalCode pc1 = gbPostalCodeManager.getNearest(51.3720000000, -0.0740000000);
        //        System.out.println(pc.toString());

        assertNotNull(pc1);
        assertEquals("CR0", pc1.getPostalCode());
        assertEquals("Croydon", pc1.getCity());

    }
}

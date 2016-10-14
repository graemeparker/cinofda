package com.adfonic.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

public class CanadianPostalCodeManagerIT {

    private static File coordinatesFile = null;

    @BeforeClass
    public static void init() {
        URL url = TestPostalCodeIdManagerIT.class.getClassLoader().getResource("ziplist5-geo-ca.csv");
        coordinatesFile = new File(url.getPath());
    }

    @Test
    public void test() throws java.io.IOException {
        long start = System.nanoTime();

        CanadianPostalCodeManager canadianPostalCodeManager = new CanadianPostalCodeManager(coordinatesFile, 120);
        canadianPostalCodeManager.initialize();

        long initialised = System.nanoTime() - start;
        System.out.println("initialized " + (System.nanoTime() - start) / 1000);

        CanadianPostalCode pc1 = canadianPostalCodeManager.getNearest(48.42, -54.0);
        CanadianPostalCode pc2 = canadianPostalCodeManager.getNearest(49.8693, -119.4049);
        System.out.println("elapsed " + (System.nanoTime() - start - initialised) / 1000);

        assertNotNull(pc1);
        assertEquals("A0C 1L0", pc1.getPostalCode());
        assertEquals(CanadianProvince.NL, pc1.getCanadianProvince());

        assertNotNull(pc2);
        assertEquals("V1P 1", pc2.getPostalCode().substring(0, 5));
        assertEquals(CanadianProvince.BC, pc2.getCanadianProvince());

    }
}

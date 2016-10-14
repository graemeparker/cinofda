package com.adfonic.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestPostalCodeIdManagerIT {
    private static File coordinatesFile = null;

    @BeforeClass
    public static void init() {
        URL url = TestPostalCodeIdManagerIT.class.getClassLoader().getResource("postal-codes.csv");
        coordinatesFile = new File(url.getPath());
    }

    @Test
    public void test() throws java.io.IOException {
        PostalCodeIdManager postalCodeIdManager = new PostalCodeIdManager(coordinatesFile, 120);
        postalCodeIdManager.initialize();

        Long postalCodeId;

        postalCodeId = postalCodeIdManager.getPostalCodeId("US", "40324");
        assertNotNull(postalCodeId);
        assertEquals(Long.valueOf(17404), postalCodeId);

        postalCodeId = postalCodeIdManager.getPostalCodeId("GB", "AB23");
        assertNotNull(postalCodeId);
        assertEquals(Long.valueOf(42067), postalCodeId);

        postalCodeId = postalCodeIdManager.getPostalCodeId("CA", "A0A 1H0");
        assertNotNull(postalCodeId);
        assertEquals(Long.valueOf(44884), postalCodeId);
    }
}

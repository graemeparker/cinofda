package com.adfonic.geo;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestDmaManagerIT {

    private static File coordinatesFile = null;

    @BeforeClass
    public static void init() {
        URL url = TestPostalCodeIdManagerIT.class.getClassLoader().getResource("zip-to-dma.csv");
        coordinatesFile = new File(url.getPath());
    }

    @Test
    public void test() throws java.io.IOException {
        DmaManager dmaManager = new DmaManager(coordinatesFile, 120);
        dmaManager.initialize();

        Dma dma;

        dma = dmaManager.get("40324");
        assertEquals(dma.getName(), "Lexington");

        dma = dmaManager.get("91709");
        assertEquals(dma.getName(), "Los Angeles");

        dma = dmaManager.getDmaById("807");
        assertEquals(dma.getName(), "San Francisco-Oak-San Jose");

        dma = dmaManager.getDmaById("506");
        assertEquals(dma.getName(), "Boston (Manchester)");

        dma = dmaManager.getDmaByName("Lexington");
        assertEquals(dma.getCode(), "541");

        dma = dmaManager.getDmaByName("lexINGton");
        assertEquals(dma.getCode(), "541");

        dma = dmaManager.getDmaByName("SAN FRANCISCO-OAK-SAN JOSE");
        assertEquals(dma.getCode(), "807");
    }
}

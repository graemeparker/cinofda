package com.adfonic.geo;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.StopWatch;

import com.adfonic.test.AbstractAdfonicTest;

public class SpeedComparisonIT extends AbstractAdfonicTest {
    private static final transient Logger LOG = Logger.getLogger(SpeedComparisonIT.class.getName());

    private static File coordinatesFile = null;

    @BeforeClass
    public static void init() {
        URL url = TestPostalCodeIdManagerIT.class.getClassLoader().getResource("ziplist5-geo-ca.csv");
        coordinatesFile = new File(url.getPath());
    }

    @Test
    public void testCanada() throws java.io.IOException {
        CanadianPostalCodeManager mgr = new CanadianPostalCodeManager(coordinatesFile, 9999);
        mgr.initialize();

        LOG.info("Initialization complete, running speed test");
        int iterations = 500;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Canada with " + iterations + " iterations");
        for (int k = 0; k < iterations; ++k) {
            mgr.getNearest(47.0, -53.0);
        }
        stopWatch.stop();
        LOG.info(stopWatch.toString());
    }

    @Test
    public void getUS() throws java.io.IOException {
        USZipCodeManager mgr = new USZipCodeManager(new File("/usr/local/adfonic/data/ziplist5-geo.csv"), 999);
        mgr.initialize();

        LOG.info("Initialization complete, running speed test");
        int iterations = 5000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("US with " + iterations + " iterations");
        for (int k = 0; k < iterations; ++k) {
            mgr.getNearest(38.2344444444444, -84.4346944444444);
        }
        stopWatch.stop();
        LOG.info(stopWatch.toString());
    }

    @Test
    public void testGB() throws java.io.IOException {
        GBPostalCodeManager mgr = new GBPostalCodeManager(new File("/usr/local/adfonic/data/gb_postal_codes.csv"), 999);
        mgr.initialize();

        LOG.info("Initialization complete, running speed test");
        int iterations = 50000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("GB with " + iterations + " iterations");
        for (int k = 0; k < iterations; ++k) {
            mgr.getNearest(53.976, -1.173);
        }
        stopWatch.stop();
        LOG.info(stopWatch.toString());
    }
}

package com.adfonic.geo;

import java.io.File;
import java.net.URL;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.StopWatch;

import com.adfonic.geo.utils.GeoDataManagerGrid;
import com.adfonic.geo.utils.GeoDataManagerKdTree;

public class AlgorithmnComparisonTestIT {

    Random rnd = new Random();

    private static File coordinatesFile = null;

    @BeforeClass
    public static void init() {
        URL url = TestPostalCodeIdManagerIT.class.getClassLoader().getResource("coord-tests.csv");
        coordinatesFile = new File(url.getPath());
    }

    @Test
    public void speedGridAlgo() throws Exception {
        GeoDataManagerGrid mgr = new GeoDataManagerGrid(coordinatesFile, 9999);
        mgr.initialize();

        //warming up
        for (int i = 0; i < 20000; i++) {
            mgr.getNearest(rnd.nextDouble() * 30 + 20, rnd.nextDouble() * 50 + 75); //ranges for china: Lat    18-50   Long  75-130
        }

        ChinesePostalCode[] chinesePostalCodes = new ChinesePostalCode[10];

        System.out.println("Initialization complete, running speed test");
        int iterations = 5000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("China Old Algo with " + iterations + " iterations");
        for (int k = 0; k < iterations; ++k) {
            chinesePostalCodes[0] = mgr.getNearest(35.0, 102.0);
            chinesePostalCodes[1] = mgr.getNearest(44.0, 87.0);
            chinesePostalCodes[2] = mgr.getNearest(18.0, 120.0);
            chinesePostalCodes[3] = mgr.getNearest(25.0, 102.0);
            chinesePostalCodes[4] = mgr.getNearest(44.0, 122.0);
            chinesePostalCodes[5] = mgr.getNearest(36.0, 82.0);
            chinesePostalCodes[6] = mgr.getNearest(40.0, 79.0);
            chinesePostalCodes[7] = mgr.getNearest(36.0, 101.0);
            chinesePostalCodes[8] = mgr.getNearest(40.0, 116.0);
            chinesePostalCodes[9] = mgr.getNearest(44.0, 80.5);
        }
        stopWatch.stop();
        System.out.println("Old finished in " + stopWatch.toString());

        for (ChinesePostalCode chinesePostalCode : chinesePostalCodes) {
            System.out.println("nearest " + chinesePostalCode);

        }

    }

    @Test
    public void speedKdTree() throws Exception {
        GeoDataManagerKdTree geoDataManager = new GeoDataManagerKdTree(coordinatesFile, 0);
        geoDataManager.initialize();

        for (int i = 0; i < 20000; i++) {
            geoDataManager.getNearest(rnd.nextDouble() * 30 + 20, rnd.nextDouble() * 50 + 75); //ranges for china: Lat    18-50   Long  75-130
        }
        ChinesePostalCode[] chinesePostalCodes = new ChinesePostalCode[10];

        System.out.println("Initialization complete, running speed test");
        int iterations = 5000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("China Kd-Tree Algo with " + iterations + " iterations");
        for (int k = 0; k < iterations; ++k) {
            chinesePostalCodes[0] = geoDataManager.getNearest(35.0, 102.0);
            chinesePostalCodes[1] = geoDataManager.getNearest(44.0, 87.0);
            chinesePostalCodes[2] = geoDataManager.getNearest(18.0, 120.0);
            chinesePostalCodes[3] = geoDataManager.getNearest(25.0, 102.0);
            chinesePostalCodes[4] = geoDataManager.getNearest(44.0, 122.0);
            chinesePostalCodes[5] = geoDataManager.getNearest(36.0, 82.0);
            chinesePostalCodes[6] = geoDataManager.getNearest(40.0, 79.0);
            chinesePostalCodes[7] = geoDataManager.getNearest(36.0, 101.0);
            chinesePostalCodes[8] = geoDataManager.getNearest(40.0, 116.0);
            chinesePostalCodes[9] = geoDataManager.getNearest(44.0, 80.5);
        }
        stopWatch.stop();
        System.out.println(stopWatch.toString());

        for (ChinesePostalCode chinesePostalCode : chinesePostalCodes) {
            System.out.println("Kd-Tree finished in " + chinesePostalCode);

        }

        //347 559
    }
}

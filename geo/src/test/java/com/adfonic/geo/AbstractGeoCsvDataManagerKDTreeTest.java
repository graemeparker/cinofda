package com.adfonic.geo;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.adfonic.geo.utils.GeoDataManagerKdTree;

public class AbstractGeoCsvDataManagerKDTreeTest {

    private static File coordinatesFile = null;
    private GeoDataManagerKdTree geoDataManager;

    @BeforeClass
    public static void init() {
        URL url = TestPostalCodeIdManagerIT.class.getClassLoader().getResource("coord-tests.csv");
        coordinatesFile = new File(url.getPath());
    }

    @Before
    public void setUp() throws Exception {
        geoDataManager = new GeoDataManagerKdTree(coordinatesFile, 0);
        geoDataManager.initialize();
    }

    @Test
    public void oggiprocessCsvCoordinates() throws Exception {
        Coordinates nearest = geoDataManager.getNearest(33, 120);
        assertThat(nearest.getLatitude(), is(33.347378));
        assertThat(nearest.getLongitude(), is(120.163561));

    }

}

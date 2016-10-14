package com.adfonic.geo;

import java.io.File;
import java.util.Map;

import com.adfonic.geo.kdtree.KdTree;

public abstract class AbstractGeoCsvDataManagerKDTree<T extends Coordinates> extends AbstractCsvDataManager<T> {

    private KdTree<T> kdTree;

    protected AbstractGeoCsvDataManagerKDTree(boolean forceLowerCaseLookups, File dataFile, int checkForUpdatesPeriodSec) {
        super(forceLowerCaseLookups, dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected void afterDataReloaded(Map<String, T> map) {
        kdTree = new KdTree<T>(map.values());
    }

    /**
     * Get the nearest to a given set of coordinates
     */
    public T getNearest(Coordinates coordinates) {
        return getNearest(coordinates.getLatitude(), coordinates.getLongitude());
    }

    /**
     * Get the nearest to a given set of coordinates
     */
    public T getNearest(double latitude, double longitude) {
        return kdTree.getNearest(latitude, longitude);
    }
}

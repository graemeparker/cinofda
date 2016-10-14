package com.adfonic.geo;

import java.io.File;
import java.util.Map;

/** Abstract base class for CSV-based data.  This class handles the management
    of the data file, watching for file updates, atomic access to the cache,
    CSV reading, etc.  Subclasses simply need to implement a method that takes
    a CSV line and maps it to an object that extends Coordinates.  This class
    provides "get nearest" functionality.
*/
public abstract class AbstractGeoCsvDataManager<T extends Coordinates> extends AbstractCsvDataManager<T> {

    private static final double DEFAULT_OPTI_RADIUS_MILES = 50;

    private double optimizationRadiusMiles = DEFAULT_OPTI_RADIUS_MILES;

    protected AbstractGeoCsvDataManager(File dataFile, int checkForUpdatesPeriodSec) {
        super(dataFile, checkForUpdatesPeriodSec);
    }

    protected AbstractGeoCsvDataManager(boolean forceLowerCaseLookups, File dataFile, int checkForUpdatesPeriodSec) {
        super(forceLowerCaseLookups, dataFile, checkForUpdatesPeriodSec);
    }

    public double getOptimizationRadiusMiles() {
        return optimizationRadiusMiles;
    }

    public void setOptimizationRadiusMiles(double optimizationRadiusMiles) {
        this.optimizationRadiusMiles = optimizationRadiusMiles;
    }

    /** Get the nearest to a given set of coordinates */
    public T getNearest(Coordinates coordinates) {
        return getNearest(coordinates.getLatitude(), coordinates.getLongitude());
    }

    /** Get the nearest to a given set of coordinates */
    public T getNearest(double latitude, double longitude) {
        // We don't want to iterate through every single value and do
        // expensive math on each one.  Instead, we'll narrow the list
        // down as much as possible first, by using the semblance of
        // a "bounding radius" -- which we'll simplify by using a bounding
        // box instead.  Basically, picture a lat/lon square centered on
        // the supplied coordinates.  Each side is "2 x radius" in length,
        // which somewhat approximates a circle around the supplied coords.
        // Yeah, yeah, a lat/lon square isn't really a square, but for our
        // needs this will do.  And no, this won't work well near the north
        // or south poles, but screw it...this will do the trick for our
        // needs, predominantly in UK and US for now.

        // Compute a bounding box by calculating two of the corners (se & nw)
        // with heading-distance projections.
        Coordinates se = GeoUtils.relativeCoordinates(latitude, longitude, GeoUtils.INITIAL_TRUE_HEADING_SOUTHEAST, // southeast
                optimizationRadiusMiles);
        Coordinates nw = GeoUtils.relativeCoordinates(latitude, longitude, GeoUtils.INITIAL_TRUE_HEADING_NORTHEAST, // northwest
                optimizationRadiusMiles);
        double minLat = se.getLatitude(); // southern-most
        double maxLat = nw.getLatitude(); // northern-most
        // We need to add 180 in order to convert a +/- 180 system into a
        // 0 to 360 system...simplifies things below...
        double minLon360 = nw.getLongitude() + GeoUtils.LON_180;
        double maxLon360 = se.getLongitude() + GeoUtils.LON_180;

        Map<String, T> map = getCache();
        T closest = null;
        double closestDistance = GeoUtils.VERY_LONG_DISTANCE;
        double lon360;
        for (T obj : map.values()) {
            // Before we bother checking the distance, let's make sure it
            // falls within our optimization bounding box...
            if (obj.getLatitude() < minLat || obj.getLatitude() > maxLat || (lon360 = (obj.getLongitude() + GeoUtils.LON_180)) < minLon360 || lon360 > maxLon360) {
                continue; // It's not in the box...
            }
            double dist = GeoUtils.distanceBetween(obj.getLatitude(), obj.getLongitude(), latitude, longitude);
            if (dist < closestDistance) {
                closest = obj;
                closestDistance = dist;
            }
        }
        return closest;
    }
}

package com.adfonic.geo;

import java.io.File;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base class for CSV-based data.  This class handles the management
 * of the data file, watching for file updates, atomic access to the cache,
 * CSV reading, etc.  Subclasses simply need to implement a method that takes
 * a CSV line and maps it to an object that extends Coordinates.  This class
 * provides "get nearest" functionality -- with grid-based optimization.
 */
public abstract class AbstractGeoCsvDataManager2<T extends Coordinates> extends AbstractCsvDataManager<T> {

    private static final ThreadLocal<DecimalFormat> TL_GRID_KEY_FORMATTER = new ThreadLocal<DecimalFormat>() {
        @Override
        public DecimalFormat initialValue() {
            return new DecimalFormat("0.0");
        }
    };

    // Optimization, storing a "coordinate grid" where each area in the
    // grid knows which elements fall inside of it.
    // This is used to optimize getNearest().
    private final AtomicReference<Map<String, T[]>> gridMapRef = new AtomicReference<Map<String, T[]>>();

    // Number of degrees in each increment (box) in the grid
    static final double GRID_INCREMENT = 0.5;

    protected AbstractGeoCsvDataManager2(File dataFile, int checkForUpdatesPeriodSec) {
        super(dataFile, checkForUpdatesPeriodSec);
    }

    protected AbstractGeoCsvDataManager2(boolean forceLowerCaseLookups, File dataFile, int checkForUpdatesPeriodSec) {
        super(forceLowerCaseLookups, dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected void afterDataReloaded(Map<String, T> map) {
        // Build an optimized grid-based map of elements in order to
        // reduce the # of values we'll need to scan when handling
        // a call to getNearest().
        gridMapRef.set(buildGridMap(map));
    }

    /**
     * Build a grid-based map of arrays of values.  Each key in the
     * map represents a half-degree square, and the value is an array
     * of elements that fall in that half-degree square area.
     */
    @SuppressWarnings("unchecked")
    static <T extends Coordinates> Map<String, T[]> buildGridMap(Map<String, T> map) {
        Map<String, List<T>> gridMapWithLists = new HashMap<>();
        for (T t : map.values()) {
            String gridKey = getGridKey(t.getLatitude(), t.getLongitude());
            List<T> list = gridMapWithLists.get(gridKey);
            if (list == null) {
                list = new ArrayList<T>();
                gridMapWithLists.put(gridKey, list);
            }
            list.add(t);
        }

        // Convert List to array, so we can avoid constructing and GC'ing
        // an iterator every time we need to use one of the lists.
        Map<String, T[]> gridMapWithArrays = new HashMap<>();
        for (Map.Entry<String, List<T>> entry : gridMapWithLists.entrySet()) {
            // I wish it was as simple as just calling list.toArray(),
            // and you can't do: new T[list.size()].
            // Generic array creation...we have to avoid it.
            // We cheat and grab the list, and use its first element in
            // order to grab the class and create a typesafe array.
            List<T> list = entry.getValue();
            T[] array = (T[]) Array.newInstance(list.get(0).getClass(), list.size());
            gridMapWithArrays.put(entry.getKey(), list.toArray(array));
        }
        return gridMapWithArrays;
    }

    /**
     * Generate a "grid key" used to denote into which half-degree
     * square area the given value falls.  Coordinates are rounded
     * down to the nearest half degree.  For example, if the value
     * has coordinates 37.45802,-117.06192, then the key produced
     * would be "37.0x-117.5".
     */
    static String getGridKey(double latitude, double longitude) {
        double keyLat = roundDownToNearest(latitude, GRID_INCREMENT);
        double keyLon = roundDownToNearest(longitude, GRID_INCREMENT);
        if (keyLat >= GeoUtils.LAT_90) {
            keyLat = GeoUtils.LAT_90 - GRID_INCREMENT;
        } else if (keyLat < -GeoUtils.LAT_90) {
            keyLat = -GeoUtils.LAT_90;
        }
        if (keyLon >= GeoUtils.LON_180) {
            keyLon = -GeoUtils.LON_180;
        }
        DecimalFormat fmt = TL_GRID_KEY_FORMATTER.get();
        return fmt.format(keyLat) + "x" + fmt.format(keyLon);
    }

    /**
     * Round down to the nearest multiple.
     * For example, if multiple = 0.5 then:
     * 9.278 rounds down to 9.0
     * 9.775 rounds down to 9.5
     * -10.001 rounds down to -10.5
     */
    static double roundDownToNearest(double value, double multiple) {
        double mod = value % multiple;
        if (Double.doubleToRawLongBits(mod) == 0) {
            return value;
        } else if (value > 0.0) {
            return value - mod;
        } else {
            return value - mod - multiple;
        }
    }

    /** Get the nearest to a given set of coordinates */
    public T getNearest(Coordinates coordinates) {
        return getNearest(coordinates.getLatitude(), coordinates.getLongitude());
    }

    /**
     * Get the set of either 6 or 9 grid keys surrounding the given
     * coordinates.
     */
    static String[] getNearestGridKeys(double latitude, double longitude, double gridSize) {
        double lonWest = coerceLongitudeIntoRange(longitude - gridSize);
        double lonHere = longitude;
        double lonEast = coerceLongitudeIntoRange(longitude + gridSize);
        boolean nearNorthPole = latitude > (GeoUtils.LAT_90 - gridSize);
        boolean nearSouthPole = latitude < (-GeoUtils.LAT_90 + gridSize);
        String[] gridKeys;
        if (nearNorthPole || nearSouthPole) {
            gridKeys = new String[6];
        } else {
            gridKeys = new String[9];
        }
        int idx = 0;
        gridKeys[idx++] = getGridKey(latitude, lonWest);
        gridKeys[idx++] = getGridKey(latitude, lonHere);
        gridKeys[idx++] = getGridKey(latitude, lonEast);
        if (!nearNorthPole) {
            gridKeys[idx++] = getGridKey(latitude + gridSize, lonWest);
            gridKeys[idx++] = getGridKey(latitude + gridSize, lonHere);
            gridKeys[idx++] = getGridKey(latitude + gridSize, lonEast);
        }
        if (!nearSouthPole) {
            gridKeys[idx++] = getGridKey(latitude - gridSize, lonWest);
            gridKeys[idx++] = getGridKey(latitude - gridSize, lonHere);
            gridKeys[idx++] = getGridKey(latitude - gridSize, lonEast);
        }
        return gridKeys;
    }

    /**
     * Coerce a given longitude into the range -180.0 (exclusive) to
     * 180.0 (inclusive).  This allows us to add and subtract values
     * from a given longitude value without regard to range, and this
     * will coerce the result into valid range.
     */
    static double coerceLongitudeIntoRange(double longitude) {
        if (longitude > GeoUtils.LON_180) {
            return longitude - GeoUtils.LON_360;
        } else if (longitude <= -GeoUtils.LON_180) {
            return longitude + GeoUtils.LON_360;
        } else {
            return longitude;
        }
    }

    /** Get the nearest to a given set of coordinates */
    public T getNearest(double latitude, double longitude) {
        // Optimized reduction of the number of values for which we have
        // to calculate distance.  First narrow the set down using the
        // nearest grid entries.
        T closest = null;
        double closestDistance = GeoUtils.VERY_LONG_DISTANCE;
        String[] gridKeys = getNearestGridKeys(latitude, longitude, GRID_INCREMENT);
        Map<String, T[]> gridMap = gridMapRef.get();
        for (int k = 0; k < gridKeys.length; ++k) {
            String gridKey = gridKeys[k];
            T[] array = gridMap.get(gridKey);
            if (array == null) {
                continue; // no data points in that grid area
            }
            for (int a = 0; a < array.length; ++a) {
                T t = array[a];
                double dist = GeoUtils.distanceBetween(t.getLatitude(), t.getLongitude(), latitude, longitude);
                if (dist < closestDistance) {
                    closest = t;
                    closestDistance = dist;
                }
            }
        }
        return closest;
    }
}

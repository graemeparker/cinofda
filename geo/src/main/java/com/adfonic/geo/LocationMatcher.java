package com.adfonic.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A utility class that optimizes finding a matching location (coordinates plus
 * radius) for a given set of coordinates from a possibly long list of location
 * possibilities.
 *
 * Optimal usage is to "compile" a LocationMatcher once (using its constructor)
 * for a given set of locations, then repeatedly call the thread-safe
 * getFirstMatch() method, which approximates O(log n) performance. The match
 * methods are threadsafe.
 *
 * This class does not currently accurately handle location targets that cross
 * the antimeridian (180th meridian) and is therefore unsuitable for powering
 * extensive Fijian advertising campaigns.
 */
public class LocationMatcher {
    private Map<Double, MaxTargetPair> minLats, minLongs;
    private Set<Double> maxLats, maxLongs;

    private static class MaxTargetPair {
        double max;
        CoordinatesWithRadius target;

        public MaxTargetPair(double max, CoordinatesWithRadius target) {
            this.max = max;
            this.target = target;
        }
    }

    public LocationMatcher(Collection<CoordinatesWithRadius> locationTargets) {
        this.minLats = new TreeMap<Double, MaxTargetPair>();
        this.maxLats = new TreeSet<Double>();
        this.minLongs = new TreeMap<Double, MaxTargetPair>();
        this.maxLongs = new TreeSet<Double>();

        for (CoordinatesWithRadius target : locationTargets) {
            double radiusMiles = target.getRadius();
            double latitude = target.getLatitude();
            double longitude = target.getLongitude();

            double latDelta = GeoUtils.milesToLatitudeDegrees(radiusMiles);
            double longDelta = GeoUtils.milesAtLatitudeToLongitudeDegrees(radiusMiles, latitude);

            minLats.put(latitude - latDelta, new MaxTargetPair(latitude + latDelta, target));
            minLongs.put(longitude - longDelta, new MaxTargetPair(longitude + longDelta, target));

            maxLats.add(latitude + latDelta);
            maxLongs.add(longitude + longDelta);
        }

        Double maxVal;

        maxVal = null;
        for (Double minVal : minLats.keySet()) {
            MaxTargetPair pair = minLats.get(minVal);
            if (maxVal == null || maxVal < pair.max) {
                maxVal = pair.max;
            }
            pair.max = maxVal;
        }

        maxVal = null;
        for (Double minVal : minLongs.keySet()) {
            MaxTargetPair pair = minLongs.get(minVal);
            if (maxVal == null || maxVal < pair.max) {
                maxVal = pair.max;
            }
            pair.max = maxVal;
        }
    }

    private Collection<MaxTargetPair> getCandidateMatches(double coordLat, double coordLong) {
        NavigableMap<Double, MaxTargetPair> nmap, pmap;
        Double value;

        nmap = ((TreeMap<Double, MaxTargetPair>) minLats).headMap(coordLat, true);
        if (nmap.isEmpty()) {
            return null;
        }

        value = ((TreeSet<Double>) maxLats).higher(coordLat);
        Double minVal = null;
        boolean broke = false;
        Map.Entry<Double, MaxTargetPair> entry = null;
        Iterator<Map.Entry<Double, MaxTargetPair>> minIterator = nmap.descendingMap().entrySet().iterator();
        if (value != null) {
            while (minIterator.hasNext()) {
                entry = minIterator.next();
                minVal = entry.getKey();
                if (entry.getValue().max <= value) {
                    broke = true;
                    break;
                }
            }
            if ((broke) && (minVal < nmap.lastKey())) {
                nmap = nmap.tailMap(minVal, true);
            }
        }

        pmap = ((TreeMap<Double, MaxTargetPair>) minLongs).headMap(coordLong, true);
        if (pmap.isEmpty()){
            return null;
        }

        value = ((TreeSet<Double>) maxLongs).higher(coordLong);
        if (value != null) {
            minVal = null;
            broke = false;
            minIterator = pmap.descendingMap().entrySet().iterator();
            while (minIterator.hasNext()) {
                entry = minIterator.next();
                minVal = entry.getKey();
                if (entry.getValue().max <= value) {
                    broke = true;
                    break;
                }
            }
            if ((broke) && (minVal < pmap.lastKey())) {
                pmap = pmap.tailMap(minVal, true);
            }
        }

        return (nmap.size() < pmap.size()) ? nmap.values() : pmap.values();
    }

    /** Returns the first location that matches, or null if none do. */
    public CoordinatesWithRadius getFirstMatch(Coordinates coordinates) {
        double coordLat = coordinates.getLatitude();
        double coordLong = coordinates.getLongitude();
        Collection<MaxTargetPair> toCheck = getCandidateMatches(coordLat, coordLong);
        if (toCheck == null){
            return null;
        }

        CoordinatesWithRadius locationTarget;
        for (MaxTargetPair pair : toCheck) {
            locationTarget = pair.target;
            if (GeoUtils.distanceBetween(coordLat, coordLong, locationTarget.getLatitude(), locationTarget.getLongitude()) <= locationTarget
                    .getRadius()) {
                return locationTarget;
            }
        }
        return null;
    }

    /** Returns a (possibly empty) collection of all locations that match. */
    public Collection<CoordinatesWithRadius> getAllMatches(Coordinates coordinates) {
        double coordLat = coordinates.getLatitude();
        double coordLong = coordinates.getLongitude();
        Collection<MaxTargetPair> toCheck = getCandidateMatches(coordLat, coordLong);
        if (toCheck == null){
            return Collections.emptySet();
        }

        List<CoordinatesWithRadius> results = new ArrayList<>();
        CoordinatesWithRadius locationTarget;
        for (MaxTargetPair pair : toCheck) {
            locationTarget = pair.target;
            if (GeoUtils.distanceBetween(coordLat, coordLong, locationTarget.getLatitude(), locationTarget.getLongitude()) <= locationTarget
                    .getRadius()) {
                results.add(locationTarget);
            }
        }
        return results;
    }
}

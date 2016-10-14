package com.adfonic.geo;

public class GeoUtils {
    
    public static final double LON_360 = 360.0;
    public static final double LON_180 = 180.0;
    public static final double LAT_90  = 90.0;
    public static final int VERY_LONG_DISTANCE = 99999999;
    public static final int INITIAL_TRUE_HEADING_NORTHEAST = 315;
    public static final int INITIAL_TRUE_HEADING_SOUTHEAST = 135;
    
    // Source: http://en.wikipedia.org/wiki/Earth_radius#Mean_radius
    public static final double EARTH_MEAN_RADIUS_MILES = 3958.761;
    private static double DEGREES_TO_RADIANS = Math.PI / LON_180;
    private static double RADIANS_TO_DEGREES = LON_180 / Math.PI;
    
    private GeoUtils(){
        // private constructor, util class
    }

    /** Calculate distance in statute miles between two lat/lon coordinates
        @param coord1 the first coordinate
        @param coord2 the second coordinate
    @return number of statute miles between the two points
    */
    public static double distanceBetween(Coordinates coord1,
                                         Coordinates coord2) {
        if (coord1 == coord2) {
            return 0;
        } else if (coord1 == null || coord2 == null) {
            return -1;
        }
        return distanceBetween(coord1.getLatitude(),
                               coord1.getLongitude(),
                               coord2.getLatitude(),
                               coord2.getLongitude());
    }

    /** Calculate distance in statute miles between two lat/lon coordinates
        @param lat1 the first coordinate latitude
        @param lon1 the first coordinate longitude
        @param lat2 the second coordinate latitude
        @param lon2 the second coordinate longitude
    @return number of statute miles between the two points
    */
    public static double distanceBetween(double lat1,
                                         double lon1,
                                         double lat2,
                                         double lon2){
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }

        return EARTH_MEAN_RADIUS_MILES *
            getd(Math.toRadians(lat1),
                 Math.toRadians(lon1),
                 Math.toRadians(lat2),
                 Math.toRadians(lon2));
    }
    
    /**
     * Test if point 1, defined by (lat1, lon1), is within the target location and radius
     * defined by (lat2, lon2, distanceMiles). Because a mile is a variable number of degrees
     * in longitude depending on latitude, it's important that this function is called with
     * the right argument ordering.
     */
    public static boolean fastWithinDistance(double lat1,
                                         double lon1,
                                         double lat2,
                                         double lon2, double distanceMiles) {
        double dLon = lon1 - lon2;
        double dLat = lat1 - lat2;
        double dLonM = dLon/milesAtLatitudeToLongitudeDegrees(1, lat2);
        double dLatM = dLat/milesToLatitudeDegrees(1);
        
        return dLonM*dLonM + dLatM*dLatM <= distanceMiles*distanceMiles;
    }

    private static double getd(double rlat1, double rlon1,
                               double rlat2, double rlon2) {
        double dlon = rlon2 - rlon1;
        double dlat = rlat2 - rlat1;
        double slat2 = Math.sin(dlat / 2.0);
        double slon2 = Math.sin(dlon / 2.0);
        double a = (slat2 * slat2) + Math.cos(rlat1) * Math.cos(rlat2) * slon2 * slon2;
        return 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static Coordinates relativeCoordinates(Coordinates origin,
                                                  double initialTrueHeading,
                                                  double distanceMiles) {
        return relativeCoordinates(origin.getLatitude(),
                                   origin.getLongitude(),
                                   initialTrueHeading,
                                   distanceMiles);
    }
    
    public static Coordinates relativeCoordinates(double originLat,
                                                  double originLon,
                                                  double initialTrueHeading,
                                                  double distanceMiles) {
        double lat1 = Math.toRadians(originLat);
        double lon1 = Math.toRadians(originLon);
        double d = distanceMiles / EARTH_MEAN_RADIUS_MILES;
        double tc = Math.toRadians(initialTrueHeading);
        double lat = Math.asin(Math.sin(lat1)*Math.cos(d)+Math.cos(lat1)*Math.sin(d)*Math.cos(tc));
        double dlon = Math.atan2(Math.sin(tc)*Math.sin(d)*Math.cos(lat1), Math.cos(d)-Math.sin(lat1)*Math.sin(lat));
        double lon = Math.IEEEremainder(lon1 + dlon + Math.PI, 2*Math.PI) - Math.PI;
        return new SimpleCoordinates(toLatitudeDegrees(lat),
                                     toLongitudeDegrees(lon));
    }
    
    public static double toLatitudeDegrees(double radians) {
        double deg = Math.toDegrees(radians);
        while (deg < -LAT_90) {
            deg = -LON_180 - deg; // i.e. -120 -> -60
        }
        while (deg > LAT_90) {
            deg = LON_180 - deg; // i.e. 120 -> 60
        }
        return deg;
    }

    public static double toLongitudeDegrees(double radians) {
        double deg = Math.toDegrees(radians);
        while (deg < -LON_180) {
            deg += LON_360;
        }
        while (deg >= LON_360) {
            deg -= LON_360;
        }
        return deg;
    }

    public static boolean validateCoordinates(Coordinates coordinates) {
        return validateCoordinates(coordinates.getLatitude(),
                                   coordinates.getLongitude());
    }

    public static boolean validateCoordinates(double lat, double lon) {
        // SC-191 - we were getting lots of requests from Smaato where they
        // passed us u.latitude=-1.0 and u.longitude=-1.0.
        if (lat == -1.0 && lon == -1.0) {
            return false;
        } else {
            return lat >= -LAT_90 && lat <= LAT_90 && lon >= -LON_180 && lon <= LON_180;
        }
    }

    public static double milesToLatitudeDegrees(double miles) {
        return (miles / EARTH_MEAN_RADIUS_MILES) * RADIANS_TO_DEGREES;
    }

    public static double milesAtLatitudeToLongitudeDegrees(double miles, double latitude) {
        double r = EARTH_MEAN_RADIUS_MILES * Math.cos(latitude * DEGREES_TO_RADIANS);
        return (miles/r) * RADIANS_TO_DEGREES;
    }

}

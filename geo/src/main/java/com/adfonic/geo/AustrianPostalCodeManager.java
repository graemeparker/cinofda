package com.adfonic.geo;

import java.io.File;
import java.util.Map;

public class AustrianPostalCodeManager extends AbstractGeoCsvDataManagerKDTree<AustrianPostalCode> {

    private static final short INDEX_POSTALCODE = 0;
    private static final short INDEX_PROVINCE = 1;
    private static final short INDEX_LATITUDE = 3;
    private static final short INDEX_LONGITUD = 4;

    /**
     * Manager for accessing Austrian postal code data.
     * This class loads postal code data from a configured
     * CSV file expected to have the following format:
     * at_postal_codes.csv
     * <p/>
     * "postal_code","province","city","Lat","Long"
     * "1010","WI","Wien",48.2077,16.3705
     * "1020","WI","Wien",48.2167,16.4000
     * "1140","NO","Purkersdorf",48.2077,16.1754
     */

    public AustrianPostalCodeManager(File dataFile, int checkForUpdatesPeriodSec) {
        super(true, dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected int getNumberOfHeaderLinesToSkip() {
        return 1;
    }

    @Override
    protected void processCsvLine(String[] line, Map<String, AustrianPostalCode> map) {
        map.put(line[INDEX_POSTALCODE].toLowerCase(),
                new AustrianPostalCodeImmutable(line[INDEX_POSTALCODE], AustrianProvince.valueOf(line[INDEX_PROVINCE]), Double.parseDouble(line[INDEX_LATITUDE]), Double
                        .parseDouble(line[INDEX_LONGITUD])));
    }

    static final class AustrianPostalCodeImmutable implements AustrianPostalCode {
        private final String postalCode;
        private final AustrianProvince austrianProvince;
        private final double latitude;
        private final double longitude;

        AustrianPostalCodeImmutable(String postalCode, AustrianProvince austrianProvince, double latitude, double longitude) {
            this.postalCode = postalCode;
            this.austrianProvince = austrianProvince;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String getPostalCode() {
            return postalCode;
        }

        @Override
        public AustrianProvince getAustrianProvince() {
            return austrianProvince;
        }

        @Override
        public double getLatitude() {
            return latitude;
        }

        @Override
        public double getLongitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return "AustrianPostalCode[austrianProvince=" + austrianProvince + ",postalCode=" + postalCode + ",latitude=" + latitude + ",longitude=" + longitude + "]";
        }
    }
}

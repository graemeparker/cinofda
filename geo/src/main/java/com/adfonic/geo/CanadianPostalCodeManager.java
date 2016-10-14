package com.adfonic.geo;

import java.io.File;
import java.util.Map;

/**
 * Manager for accessing Canadian postal code data. This class loads data
 * from a configured CSV file expected to have the following format:
 *
 * "City","Prov","P Code","Pref","A/C","T/Z","DST?","Lat","Long"
 * " Copyright (c) 2011 CD Light, LLC",," 0611",,,,,,
 * "Aquaforte","NL","A0A 1A0","P","709","NST","Y","47.0004","-52.9669"
 * "Avondale","NL","A0A 1B0","P","709","NST","Y","47.4166","-53.1976"
 * "Bay Bulls","NL","A0A 1C0","P","709","NST","Y","47.3164","-52.8166"
 * "Bay de Verde","NL","A0A 1E0","P","709","NST","Y","48.0830","-52.9006"
 * "Bay Roberts","NL","A0A 1G0","P","709","NST","Y","47.5997","-53.2673"
 * ...
 *
 * This is the format of the Ziplist data file ziplist5-geo-ca.csv.
 */
public class CanadianPostalCodeManager extends AbstractGeoCsvDataManagerKDTree<CanadianPostalCode> {

    private static final short INDEX_POSTALCODE = 2;
    private static final short INDEX_PROVINCE = 1;
    private static final short INDEX_LATITUDE = 7;
    private static final short INDEX_LONGITUD = 8;

    public CanadianPostalCodeManager(File dataFile, int checkForUpdatesPeriodSec) {
        super(true, dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected int getNumberOfHeaderLinesToSkip() {
        return 2;
    }

    @Override
    protected void processCsvLine(String[] line, Map<String, CanadianPostalCode> map) {
        map.put(line[INDEX_POSTALCODE].toLowerCase(),
                new CanadianPostalCodeImmutable(line[INDEX_POSTALCODE], CanadianProvince.valueOf(line[INDEX_PROVINCE]), Double.parseDouble(line[INDEX_LATITUDE]), Double
                        .parseDouble(line[INDEX_LONGITUD])));
    }

    static final class CanadianPostalCodeImmutable implements CanadianPostalCode {
        private final String postalCode;
        private final CanadianProvince canadianProvince;
        private final double latitude;
        private final double longitude;

        CanadianPostalCodeImmutable(String postalCode, CanadianProvince canadianProvince, double latitude, double longitude) {
            this.postalCode = postalCode;
            this.canadianProvince = canadianProvince;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String getPostalCode() {
            return postalCode;
        }

        @Override
        public CanadianProvince getCanadianProvince() {
            return canadianProvince;
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
            return "CanadianPostalCode[canadianProvince=" + canadianProvince + ",postalCode=" + postalCode + ",latitude=" + latitude + ",longitude=" + longitude + "]";
        }
    }
}

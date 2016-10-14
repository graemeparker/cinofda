package com.adfonic.geo;

import java.io.File;
import java.util.Map;

/**
 * Manager for accessing US zip code data. This class loads zip code data from a
 * configured CSV file expected to have the following format:
 * 
 * "City","ST","ZIP","A/C","FIPS","County","T/Z","DST?","Lat","Long","Type"
 * " Copyright (c) 2010 CD Light",," 1010",,," All Rights Reserved",,,,,
 * "Holtsville"
 * ,"NY","00501","631","36103","Suffolk","EST","Y","40.8151","-73.0455","U"
 * "Holtsville"
 * ,"NY","00544","631","36103","Suffolk","EST","Y","40.8132","-73.0476","U"
 * "Adjuntas"
 * ,"PR","00601","787","72001","Adjuntas","EST+1","N","18.1642","-66.7227", ...
 *
 * This is the format of the Ziplist data file ziplist5-geo.csv.
 */
public class USZipCodeManager extends AbstractGeoCsvDataManagerKDTree<USZipCode> {
    private static final short INDEX_CITY = 0;
    private static final short INDEX_STATE = 1;
    private static final short INDEX_ZIP = 2;
    private static final short INDEX_COUNTY = 5;
    private static final short INDEX_LATITUDE = 8;
    private static final short INDEX_LONGITUD = 9;

    public USZipCodeManager(File dataFile, int checkForUpdatesPeriodSec) {
        super(false, dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected int getNumberOfHeaderLinesToSkip() {
        return 2;
    }

    @Override
    protected void processCsvLine(String[] line, Map<String, USZipCode> map) {
        if (line.length <= 1) {
            return;
        }
        USZipCode usZipCode = new USZipCodeImmutable(line[INDEX_CITY], line[INDEX_STATE], line[INDEX_ZIP], line[INDEX_COUNTY], Double.parseDouble(line[INDEX_LATITUDE]),
                Double.parseDouble(line[INDEX_LONGITUD]));
        map.put(usZipCode.getZip(), usZipCode);
    }

    static final class USZipCodeImmutable implements USZipCode {
        private String city;
        private String state;
        private String zip;
        private String county;
        private double latitude;
        private double longitude;

        USZipCodeImmutable(String city, String state, String zip, String county, double latitude, double longitude) {
            this.city = city;
            this.state = state;
            this.zip = zip;
            this.county = county;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String getCity() {
            return city;
        }

        @Override
        public String getState() {
            return state;
        }

        @Override
        public String getZip() {
            return zip;
        }

        @Override
        public String getCounty() {
            return county;
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
            return "USZipCode[city=" + city + ",state=" + state + ",zip=" + zip + ",county=" + county + ",latitude=" + latitude + ",longitude=" + longitude + "]";
        }
    }
}

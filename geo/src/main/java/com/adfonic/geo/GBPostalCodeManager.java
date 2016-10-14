package com.adfonic.geo;

import java.io.File;
import java.util.Map;

/** Manager for accessing postal code data. This class loads postal code
    data from a configured CSV file expected to have the following format:
    
    "postal_code","name","city","latitude","longitude"
    "AB10","Aberdeen","Aberdeen",57.1350000000,-2.1170000000
    "AB11","Aberdeen","Aberdeen",57.1380000000,-2.0920000000
    "AB12","Aberdeen","Aberdeen",57.1010000000,-2.1110000000
    ...
*/
public class GBPostalCodeManager extends AbstractGeoCsvDataManagerKDTree<PostalCode> {
    private static final short INDEX_POSTALCODE = 0;
    private static final short INDEX_NAME = 1;
    private static final short INDEX_CITY = 2;
    private static final short INDEX_LATITUDE = 3;
    private static final short INDEX_LONGITUD = 4;

    public GBPostalCodeManager(File dataFile, int checkForUpdatesPeriodSec) {
        super(false, dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected int getNumberOfHeaderLinesToSkip() {
        return 1;
    }

    @Override
    protected void processCsvLine(String[] line, Map<String, PostalCode> map) {
        PostalCode postalCode = new PostalCodeImmutable(line[INDEX_POSTALCODE], line[INDEX_NAME], line[INDEX_CITY], Double.parseDouble(line[INDEX_LATITUDE]),
                Double.parseDouble(line[INDEX_LONGITUD]));
        map.put(postalCode.getPostalCode(), postalCode);
    }

    static final class PostalCodeImmutable implements PostalCode {
        private String postalCode;
        private String name;
        private String city;
        private double latitude;
        private double longitude;

        PostalCodeImmutable(String postalCode, String name, String city, double latitude, double longitude) {
            this.postalCode = postalCode;
            this.name = name;
            this.city = city;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String getPostalCode() {
            return postalCode;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCity() {
            return city;
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
            return "PostalCode[postalCode=" + postalCode + ",name=" + name + ",city=" + city + ",latitude=" + latitude + ",longitude=" + longitude + "]";
        }
    }
}

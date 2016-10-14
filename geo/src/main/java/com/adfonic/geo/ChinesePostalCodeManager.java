package com.adfonic.geo;

import java.io.File;
import java.util.Map;

/**
 * Manager for accessing Chinese postal code data.
 * This class loads postal code data from a configured
 * CSV file expected to have the following format:
 * cn_postal_codes.csv

 "postal_code","province","city","Lat","Long"
 "100000","BJ","Beijing",39.92889,116.38833
 "210000","JS","Nanjing",32.0616667,118.7777778
 */
public class ChinesePostalCodeManager extends AbstractGeoCsvDataManagerKDTree<ChinesePostalCode> {

    private static final short INDEX_POSTALCODE = 0;
    private static final short INDEX_PROVINCE = 1;
    private static final short INDEX_LATITUDE = 3;
    private static final short INDEX_LONGITUD = 4;

    public ChinesePostalCodeManager(File dataFile, int checkForUpdatesPeriodSec) {
        super(true, dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected int getNumberOfHeaderLinesToSkip() {
        return 1;
    }

    @Override
    protected void processCsvLine(String[] line, Map<String, ChinesePostalCode> map) {
        map.put(line[INDEX_POSTALCODE].toLowerCase(),
                new ChinesePostalCodeImmutable(line[INDEX_POSTALCODE], ChineseProvince.valueOf(line[INDEX_PROVINCE]), Double.parseDouble(line[INDEX_LATITUDE]), Double
                        .parseDouble(line[INDEX_LONGITUD])));
    }

    public static final class ChinesePostalCodeImmutable implements ChinesePostalCode {
        private final String postalCode;
        private final ChineseProvince chineseProvince;
        private final double latitude;
        private final double longitude;

        public ChinesePostalCodeImmutable(String postalCode, ChineseProvince chineseProvince, double latitude, double longitude) {
            this.postalCode = postalCode;
            this.chineseProvince = chineseProvince;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String getPostalCode() {
            return postalCode;
        }

        @Override
        public ChineseProvince getChineseProvince() {
            return chineseProvince;
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
            return "ChinesePostalCode[chineseProvince=" + chineseProvince + ",postalCode=" + postalCode + ",latitude=" + latitude + ",longitude=" + longitude + "]";
        }
    }
}

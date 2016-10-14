package com.adfonic.geo;

import java.io.File;
import java.util.Map;

import com.adfonic.geo.postalcode.GeneralPostalCode;
import com.adfonic.geo.postalcode.PostalCodeImmutable;

/**
 * Manager for accessing Spanish postal code data.
 * This class loads postal code data from a configured
 * CSV file expected to have the following format:
 * es_postal_codes.csv

 "postal_code","city","province","Lat","Long"
 "01001","Vitoria-Gasteiz","Pais Vasco",42.8500000000,-2.6667000000
 "01002","Vitoria-Gasteiz","Pais Vasco",42.8500000000,-2.6667000000
 "01003","Vitoria-Gasteiz","Pais Vasco",42.8500000000,-2.6667000000
 */
public class SpanishPostalCodeManager extends AbstractGeoCsvDataManagerKDTree<GeneralPostalCode> {

    private static final short INDEX_POSTALCODE = 0;
    private static final short INDEX_PROVINCE = 2;
    private static final short INDEX_LATITUDE = 3;
    private static final short INDEX_LONGITUD = 4;

    public SpanishPostalCodeManager(File dataFile, int checkForUpdatesPeriodSec) {
        super(true, dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected int getNumberOfHeaderLinesToSkip() {
        return 1;
    }

    @Override
    protected void processCsvLine(String[] line, Map<String, GeneralPostalCode> map) {
        map.put(line[INDEX_POSTALCODE].toLowerCase(),
                new PostalCodeImmutable("ES", line[INDEX_POSTALCODE], line[INDEX_PROVINCE], Double.parseDouble(line[INDEX_LATITUDE]), Double.parseDouble(line[INDEX_LONGITUD])));
    }

}
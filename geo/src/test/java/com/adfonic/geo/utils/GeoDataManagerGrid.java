package com.adfonic.geo.utils;

import java.io.File;
import java.util.Map;

import com.adfonic.geo.AbstractGeoCsvDataManagerKDTree;
import com.adfonic.geo.ChinesePostalCode;
import com.adfonic.geo.ChinesePostalCodeManager;
import com.adfonic.geo.ChineseProvince;

public class GeoDataManagerGrid extends AbstractGeoCsvDataManagerKDTree<ChinesePostalCode> {

    public GeoDataManagerGrid(File dataFile, int checkForUpdatesPeriodSec) {
        super(true, dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected int getNumberOfHeaderLinesToSkip() {
        return 1;
    }

    @Override
    protected void processCsvLine(String[] line, Map<String, ChinesePostalCode> map) {
        map.put(line[0].toLowerCase(),
                new ChinesePostalCodeManager.ChinesePostalCodeImmutable(line[0], ChineseProvince.valueOf(line[1]), Double.parseDouble(line[3]), Double.parseDouble(line[4])));

    }

}

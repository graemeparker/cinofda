package com.adfonic.geo;

public interface USZipCode extends Coordinates {
    String getCity();
    String getState();
    String getZip();
    String getCounty();
}
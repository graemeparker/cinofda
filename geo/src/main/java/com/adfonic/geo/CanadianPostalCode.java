package com.adfonic.geo;

public interface CanadianPostalCode extends Coordinates {
    String getPostalCode();
    CanadianProvince getCanadianProvince();
}
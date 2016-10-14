package com.adfonic.geo;

public interface AustrianPostalCode extends Coordinates {
    String getPostalCode();
    AustrianProvince getAustrianProvince();
}

package com.adfonic.geo;

public interface ChinesePostalCode extends Coordinates {
    String getPostalCode();
    ChineseProvince getChineseProvince();
}

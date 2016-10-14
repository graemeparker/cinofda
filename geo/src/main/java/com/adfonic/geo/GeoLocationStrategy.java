package com.adfonic.geo;

public interface GeoLocationStrategy<T extends Coordinates>{
    T getNearest(double latitude, double longitude);
}

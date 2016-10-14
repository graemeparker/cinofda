package com.adfonic.adserver;

/**
 * Source of the lat/lon location data on an AdEvent, indicating whether the
 * data was supplied explicitly, or derived (i.e. from the IP address)
 */
public enum LocationSource {
    EXPLICIT, // coordinates were supplied explicitly by the publisher
    DERIVED // coordinates were derived, i.e. via Quova/Neustar
}

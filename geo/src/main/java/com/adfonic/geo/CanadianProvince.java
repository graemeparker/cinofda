package com.adfonic.geo;

/**
 * Canadian Provinces.
 * Source: http://en.wikipedia.org/wiki/Provinces_and_territories_of_Canada
 * Google geocoding was used to derive the coordinates:
 * curl 'http://maps.google.com/maps/geo?q=Alberta,Canada'
 * curl 'http://maps.google.com/maps/geo?q=British+Columbia,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Manitoba,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=New+Brunswick,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Newfoundland+and+Labrador,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Nova+Scotia,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Northwest+Territories,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Nunavut,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Ontario,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Prince+Edward+Island,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Quebec,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Saskatchewan,+Canada'
 * curl 'http://maps.google.com/maps/geo?q=Yukon,+Canada'
 */
public enum CanadianProvince implements Coordinates {
    AB("Alberta", "Edmonton", 53.9332706, -116.5765035),
    BC("British Columbia", "Victoria", 53.7266683, -127.6476206),
    MB("Manitoba", "Winnipeg", 53.7608608, -98.8138763),
    NB("New Brunswick", "Fredericton", 46.5653163, -66.4619164),
    NL("Newfoundland and Labrador", "St. John's", 53.1355091, -57.6604364),
    NS("Nova Scotia", "Halifax", 44.5534208, -63.4115454),
    NT("Northwest Territories", "Yellowknife", 64.8255441, -124.8457334),
    NU("Nunavut", "Iqaluit", 70.2997711, -83.1075769),
    ON("Ontario", "Toronto", 51.2537750, -85.3232139),
    PE("Prince Edward Island", "Charlottetown", 46.5107120, -63.4168136),
    QC("Quebec", "Quebec City", 52.9399159, -73.5491361),
    SK("Saskatchewan", "Regina", 52.9399159, -106.4508639),
    YT("Yukon", "Whitehorse", 64.2823274, -135.0000000);
    
    private final String name;
    private final String capital;
    private final double latitude;
    private final double longitude;
        
    private CanadianProvince(String name, String capital, double latitude, double longitude) {
        this.name = name;
        this.capital = capital;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCapital() {
        return capital;
    }
    
    @Override
    public double getLatitude() {
        return latitude;
    }
    
    @Override
    public double getLongitude() {
        return longitude;
    }
}

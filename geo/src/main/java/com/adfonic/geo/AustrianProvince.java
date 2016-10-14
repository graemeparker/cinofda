package com.adfonic.geo;

//HASC codes from this page: http://www.statoids.com/uat.html
public enum AustrianProvince implements Coordinates {
    BU("Burgenland"),
    KA("Kärnten"),
    NO("Niederösterreich"),
    OO("Oberösterreich"),
    SZ("Salzburg"),
    ST("Steiermark"),
    TR("Tirol"),
    VO("Vorarlberg"),
    WI("Wien");

    private final String name;
    private final String capital;
    private final double latitude;
    private final double longitude;

    private AustrianProvince(String name) {
        this.name = name;
        this.capital = "capital";
        this.latitude = 0;
        this.longitude = 0;
    }
    
    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getCapital() {
        return capital;
    }
}

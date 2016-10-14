package com.adfonic.geo.postalcode;

public final class PostalCodeImmutable implements GeneralPostalCode {
    private final String countryCode;
    private final String postalCode;
    private final String province;
    private final double latitude;
    private final double longitude;

    public PostalCodeImmutable(String countryCode, String postalCode, String province, double latitude, double longitude) {
        this.countryCode = countryCode;
        this.postalCode = postalCode;
        this.province = province;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public String getProvince() {
        return province;
    }

    @Override
    public String getCountryCode() {
        return countryCode;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "PostalCodeImmutable{" +
               "countryCode='" + countryCode + '\'' +
               ", postalCode='" + postalCode + '\'' +
               ", province='" + province + '\'' +
               ", latitude=" + latitude +
               ", longitude=" + longitude +
               '}';
    }
}

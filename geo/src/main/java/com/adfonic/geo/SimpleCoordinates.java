package com.adfonic.geo;

public class SimpleCoordinates implements Coordinates {
    private double latitude;
    private double longitude;

    public SimpleCoordinates() {
        
    }
    
    public SimpleCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SimpleCoordinates(String commaSeparatedLatLon) {
        String[] latLon = commaSeparatedLatLon.split(",");
        if (latLon.length != 2) {
            throw new InvalidCoordinatesException("Expected format: lat,lon actual=" + commaSeparatedLatLon);
        }
        this.latitude = Double.parseDouble(latLon[0].trim());
        this.longitude = Double.parseDouble(latLon[1].trim());
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
        return "[" + latitude + "," + longitude + "]";
    }

    public static final class InvalidCoordinatesException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        private InvalidCoordinatesException(String message) {
            super(message);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        SimpleCoordinates that = (SimpleCoordinates) o;

        if (Double.compare(that.latitude, latitude) != 0){
            return false;
        }
        
        if (Double.compare(that.longitude, longitude) != 0){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = latitude != +0.0d ? Double.doubleToLongBits(latitude) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = longitude != +0.0d ? Double.doubleToLongBits(longitude) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}

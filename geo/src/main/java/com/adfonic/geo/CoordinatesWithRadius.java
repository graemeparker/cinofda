package com.adfonic.geo;

public interface CoordinatesWithRadius extends Coordinates {
    double getRadius();

    boolean isPossiblyInReach(double latitude, double longitude);
}

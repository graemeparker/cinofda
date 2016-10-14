package com.adfonic.domain;

public enum Gender {
    FEMALE(0.0),
    MALE(1.0);

    private double mixValue;
    
    private Gender(double mixValue) {
        this.mixValue = mixValue;
    }

    public double getMixValue() {
        return mixValue;
    }
}

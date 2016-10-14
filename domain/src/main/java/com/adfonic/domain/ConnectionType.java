package com.adfonic.domain;

public enum ConnectionType {
    OPERATOR(1),
    WIFI(2),
    BOTH(3);

    private int bitValue;
    ConnectionType(int bitValue) { this.bitValue = bitValue; }
    public int bitValue() { return bitValue; }
    public boolean isSet(int flags) { return (flags & bitValue) != 0; }
}


package com.adfonic.domain;

public enum AccountType {
    /* Direct advertisers */
    ADVERTISER(1),
    /* Agencies */
    AGENCY(2),
    /* Direct publishers */
    PUBLISHER(4),
    ;

    private final int bitValue;
    private AccountType(int bitValue) { this.bitValue = bitValue; }
    public int bitValue() { return bitValue; }
    public boolean isSet(int flags) { return (flags & bitValue) != 0; }
}


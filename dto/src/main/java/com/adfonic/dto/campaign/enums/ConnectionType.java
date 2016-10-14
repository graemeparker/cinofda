package com.adfonic.dto.campaign.enums;

public enum ConnectionType {
    OPERATOR("OPERATOR", 1, com.adfonic.domain.ConnectionType.OPERATOR), 
    WIFI("WIFI", 2, com.adfonic.domain.ConnectionType.WIFI), 
    BOTH("BOTH", 3, com.adfonic.domain.ConnectionType.BOTH);

    private int bitValue;
    private com.adfonic.domain.ConnectionType connectionType;
    private String name;

    ConnectionType(String name, int bitValue, com.adfonic.domain.ConnectionType connectionType) {
        this.bitValue = bitValue;
        this.name = name;
        this.connectionType = connectionType;

    }

    public int bitValue() {
        return bitValue;
    }

    public boolean isSet(int flags) {
        return (flags & bitValue) != 0;
    }

    public com.adfonic.domain.ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(com.adfonic.domain.ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

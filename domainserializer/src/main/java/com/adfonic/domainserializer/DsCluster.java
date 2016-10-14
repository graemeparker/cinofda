package com.adfonic.domainserializer;

import java.util.Properties;

public final class DsCluster {
    private final String name;

    public DsCluster(String name, Properties properties) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "RtbCluster {name=" + name + "}";
    }
}

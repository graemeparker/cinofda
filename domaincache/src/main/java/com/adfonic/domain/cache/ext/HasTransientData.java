package com.adfonic.domain.cache.ext;

public interface HasTransientData {

    TransientDataExt getTransientData();

    void addTransientAdserverPluginExpectedResponseTimesByPluginName(String name, Long responseTime);

    Long getTransientAdserverPluginExpectedResponseTimesByPluginName(String name);

    void clearTransientData();
}

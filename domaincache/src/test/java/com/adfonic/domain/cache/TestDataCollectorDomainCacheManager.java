package com.adfonic.domain.cache;

import java.io.File;

import org.junit.Test;

public class TestDataCollectorDomainCacheManager {
    @Test
    public void testConstructor() {
        new DataCollectorDomainCacheManager(new File("."), "DataCollectorDomainCache", false);
    }
}
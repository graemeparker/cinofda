package com.adfonic.domain.cache;

import java.io.File;

import org.junit.Test;

public class TestDomainCacheManager {
    @Test
    public void testConstructor() {
        new DomainCacheManager(new File("."), "DomainCache", false);
    }
}
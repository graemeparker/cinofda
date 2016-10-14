package com.adfonic.domain.cache;

import java.io.File;

import org.junit.Test;

public class TestAdserverDomainCacheManager {
    @Test
    public void testConstructor() {
        new AdserverDomainCacheManager(new File("."), "AdserverDomainCache", false);
    }
}
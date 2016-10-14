package com.adfonic.cache;

import static org.junit.Assert.assertEquals;
import java.util.Date;
import org.junit.Test;

public class TestAbstractCacheManager {
    @Test
    public void testCalculateTtlSeconds() {
        Date date = new Date();
        assertEquals(0, AbstractCacheManager.calculateTtlSeconds(date));
        
        date.setTime(System.currentTimeMillis() - 500);
        assertEquals(0, AbstractCacheManager.calculateTtlSeconds(date));
        
        date.setTime(System.currentTimeMillis() + 1000);
        assertEquals(1, AbstractCacheManager.calculateTtlSeconds(date));
        
        date.setTime(System.currentTimeMillis() + 1200);
        assertEquals(2, AbstractCacheManager.calculateTtlSeconds(date));
        
        date.setTime(System.currentTimeMillis() + 1800);
        assertEquals(2, AbstractCacheManager.calculateTtlSeconds(date));
        
        date.setTime(System.currentTimeMillis() + 2000);
        assertEquals(2, AbstractCacheManager.calculateTtlSeconds(date));
        
        date.setTime(System.currentTimeMillis() + 2200);
        assertEquals(3, AbstractCacheManager.calculateTtlSeconds(date));
    }
}
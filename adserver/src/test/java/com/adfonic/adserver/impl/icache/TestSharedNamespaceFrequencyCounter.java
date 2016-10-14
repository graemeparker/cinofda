package com.adfonic.adserver.impl.icache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.FrequencyCounter;
import com.adfonic.cache.CacheManager;
import com.adfonic.util.stats.CounterManager;

public class TestSharedNamespaceFrequencyCounter extends BaseAdserverTest {

    private SharedNamespaceFrequencyCounter sharedNamespaceFrequencyCounter;
    private CacheManager cacheManager;
    private CounterManager counterManager;

    @Before
    public void initTest() {
        cacheManager = mock(CacheManager.class, "cacheManager");
        counterManager = new CounterManager();

        sharedNamespaceFrequencyCounter = new SharedNamespaceFrequencyCounter(cacheManager, counterManager);
    }

    /**
     * Test where setting a value for 10 seconds in cache
     */
    @Test
    public void testSharedNamespaceFrequencyCounter1() {
        final String key = "SomeKey";
        final String value = "SomeValue";
        final int expireTimestamp = 10000;
        final Calendar cal = Calendar.getInstance();
        //Cache should expire after 10	 seconds
        cal.add(Calendar.MILLISECOND, expireTimestamp);
        expect(new Expectations() {
            {
                oneOf(cacheManager).set(key, value, new Date(cal.getTimeInMillis()));
                oneOf(cacheManager).get(key, String.class);
                will(returnValue(value));
            }
        });
        sharedNamespaceFrequencyCounter.setValue(key, value, cal.getTimeInMillis());
        String valueFound = sharedNamespaceFrequencyCounter.getValue(key);
        assertEquals(value, valueFound);

    }

    /**
     * Test where setting a value in cache whose expiration time already gone
     */
    @Test
    public void testSharedNamespaceFrequencyCounter2() {

        final String key = "SomeKey";
        final String value = "SomeValue";
        final int expireTimestamp = 1000;
        final Calendar cal = Calendar.getInstance();
        //Cache should expire after 1 seconds
        cal.add(Calendar.MILLISECOND, -expireTimestamp);
        expect(new Expectations() {
            {
                oneOf(cacheManager).set(key, value, new Date(cal.getTimeInMillis()));
                oneOf(cacheManager).get(key, String.class);
                will(returnValue(null));
            }
        });
        sharedNamespaceFrequencyCounter.setValue(key, value, cal.getTimeInMillis());
        String valueFound = sharedNamespaceFrequencyCounter.getValue(key);
        System.out.println("Value Found = " + valueFound);
        assertNull(valueFound);

    }

    /**
     * Test where setting a value for 1 seconds in cache and then wait for 2 seconds and then try to find
     * valye in cache using same key
     */
    @Test
    public void testSharedNamespaceFrequencyCounter3() {
        final String key = "SomeKey";
        final String value = "SomeValue";
        final int expireTimestamp = 1000;
        final Calendar cal = Calendar.getInstance();
        //Cache should expire after 1 seconds
        cal.add(Calendar.MILLISECOND, expireTimestamp);
        expect(new Expectations() {
            {
                oneOf(cacheManager).set(key, value, new Date(cal.getTimeInMillis()));
                oneOf(cacheManager).get(key, String.class);
                will(returnValue(null));
            }
        });
        sharedNamespaceFrequencyCounter.setValue(key, value, cal.getTimeInMillis());
        SleepForMilliSeconds(expireTimestamp + 1000);
        String valueFound = sharedNamespaceFrequencyCounter.getValue(key);
        assertNull(valueFound);
    }

    @Test
    public void testSharedNamespaceFrequencyCounter4() {
        String key = "SomeKey";
        long creativeId = 1000;
        String generatedKey = sharedNamespaceFrequencyCounter.makeKey(key, creativeId, FrequencyCounter.FrequencyEntity.CREATIVE);
        assertEquals("f." + key + "." + creativeId, generatedKey);
    }
    
    @Test
    public void testSharedNamespaceFrequencyCounter5() {
        String key = "SomeKey";
        long creativeId = 1000;
        String generatedKey = sharedNamespaceFrequencyCounter.makeKey(key, creativeId, FrequencyCounter.FrequencyEntity.CAMPAIGN);
        assertEquals("f." + key + ".c." + creativeId, generatedKey);

    }
}

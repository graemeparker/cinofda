package com.adfonic.adserver.impl.icache;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.cache.CacheManager;
import com.adfonic.util.stats.CounterManager;

public class TestNamespaceAwareFrequencyCounter extends BaseAdserverTest {
    private static final String FREQUENCY_COUNTER_CACHE_NAME = "FrequencyCounter";
    private NamespaceAwareFrequencyCounter namespaceAwareFrequencyCounter;
    private CacheManager cacheManager;
    private CounterManager counterManager;

    @Before
    public void initTest() {
        cacheManager = mock(CacheManager.class, "cacheManager");
        counterManager = new CounterManager();

        namespaceAwareFrequencyCounter = new NamespaceAwareFrequencyCounter(cacheManager, counterManager);
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
        cal.add(Calendar.MILLISECOND, expireTimestamp);
        expect(new Expectations() {
            {
                oneOf(cacheManager).set(key, value, FREQUENCY_COUNTER_CACHE_NAME, new Date(cal.getTimeInMillis()));
                oneOf(cacheManager).get(key, FREQUENCY_COUNTER_CACHE_NAME, String.class);
                will(returnValue(value));

            }
        });
        namespaceAwareFrequencyCounter.setValue(key, value, cal.getTimeInMillis());
        String valueFound = namespaceAwareFrequencyCounter.getValue(key);
        assertEquals(value, valueFound);

    }
}

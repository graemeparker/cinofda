package com.adfonic.adserver.impl.icache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.FrequencyCounter;
import com.adfonic.cache.CacheManager;
import com.adfonic.util.stats.CounterManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:adfonic-adserver-test-context.xml")
@Ignore
public class TestSharedNamespaceFrequencyCounterIT extends BaseAdserverTest {

    private SharedNamespaceFrequencyCounter sharedNamespaceFrequencyCounter;
    @Autowired
    private CacheManager cacheManager;
    private CounterManager counterManager;

    @Before
    public void initTest() {
        counterManager = new CounterManager();
        sharedNamespaceFrequencyCounter = new SharedNamespaceFrequencyCounter(cacheManager, counterManager);
    }

    @Test
    public void testSharedNamespaceFrequencyCounter1() {
        final String key = "SomeKey";
        final String value = "SomeValue";
        final int expireTimestamp = 10000;
        final Calendar cal = Calendar.getInstance();
        //Cache should expire after 10	 seconds
        cal.add(Calendar.MILLISECOND, expireTimestamp);
        sharedNamespaceFrequencyCounter.setValue(key, value, cal.getTimeInMillis());
        String valueFound = sharedNamespaceFrequencyCounter.getValue(key);
        assertEquals(value, valueFound);
    }

    @Test
    public void testSharedNamespaceFrequencyCounter2() throws InterruptedException {

        final String key = "SomeKey";
        final String value = "SomeValue";
        final int expireTimestamp = 1000;
        final Calendar cal = Calendar.getInstance();
        //Cache should expire after 1 seconds
        cal.add(Calendar.MILLISECOND, expireTimestamp);
        sharedNamespaceFrequencyCounter.setValue(key, value, cal.getTimeInMillis());
        Thread.sleep(1500L); //sleeping for 1.5 seconds so that the value expires
        String valueFound = sharedNamespaceFrequencyCounter.getValue(key);
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
        String uniqueIdentifier = "uniqueIdentifier";
        long creativeId = 123;
        int windowSeconds = 5;
        int countFound = sharedNamespaceFrequencyCounter.getFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        assertEquals(0, countFound);
        sharedNamespaceFrequencyCounter.incrementFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        countFound = sharedNamespaceFrequencyCounter.getFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        assertEquals(1, countFound);
        sharedNamespaceFrequencyCounter.incrementFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        countFound = sharedNamespaceFrequencyCounter.getFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        assertEquals(2, countFound);
        sharedNamespaceFrequencyCounter.incrementFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        countFound = sharedNamespaceFrequencyCounter.getFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        assertEquals(3, countFound);
        sharedNamespaceFrequencyCounter.incrementFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        countFound = sharedNamespaceFrequencyCounter.getFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        sharedNamespaceFrequencyCounter.incrementFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        assertEquals(4, countFound);
        SleepForSeconds(windowSeconds + 1);
        countFound = sharedNamespaceFrequencyCounter.getFrequencyCount(uniqueIdentifier, creativeId, windowSeconds, FrequencyCounter.FrequencyEntity.CREATIVE);
        assertEquals(0, countFound);
    }
}

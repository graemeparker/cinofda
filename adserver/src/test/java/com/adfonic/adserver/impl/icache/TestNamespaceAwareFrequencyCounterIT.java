package com.adfonic.adserver.impl.icache;

import static org.junit.Assert.assertEquals;

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
public class TestNamespaceAwareFrequencyCounterIT extends BaseAdserverTest {
    private NamespaceAwareFrequencyCounter namespaceAwareFrequencyCounter;
    @Autowired
    private CacheManager cacheManager;
    private CounterManager counterManager;

    @Before
    public void initTest() {
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
        namespaceAwareFrequencyCounter.setValue(key, value, cal.getTimeInMillis());
        String valueFound = namespaceAwareFrequencyCounter.getValue(key);
        assertEquals(value, valueFound);

    }

    @Test
    public void testSharedNamespaceFrequencyCounter02_incrementCounter() {
        final int expireTimestamp = 10000;
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, expireTimestamp);
        String uniqueIdentifier = randomAlphaNumericString(10);
        final Long creativeId = randomLong();
        namespaceAwareFrequencyCounter.incrementFrequencyCount(uniqueIdentifier, creativeId, expireTimestamp, FrequencyCounter.FrequencyEntity.CREATIVE);
        int count = namespaceAwareFrequencyCounter.getFrequencyCount(uniqueIdentifier, creativeId, expireTimestamp, FrequencyCounter.FrequencyEntity.CREATIVE);
        assertEquals(1, count);
    }

    @Test
    public void testSharedNamespaceFrequencyCounter03_decrementCounter() {
        final int expireTimestamp = 10000;
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, expireTimestamp);
        String uniqueIdentifier = randomAlphaNumericString(10);
        final Long creativeId = randomLong();
        namespaceAwareFrequencyCounter.incrementFrequencyCount(uniqueIdentifier, creativeId, expireTimestamp, FrequencyCounter.FrequencyEntity.CREATIVE);
        int count = namespaceAwareFrequencyCounter.getFrequencyCount(uniqueIdentifier, creativeId, expireTimestamp, FrequencyCounter.FrequencyEntity.CREATIVE);
        namespaceAwareFrequencyCounter.decrementFrequencyCount(uniqueIdentifier, creativeId, expireTimestamp, FrequencyCounter.FrequencyEntity.CREATIVE);
        count = namespaceAwareFrequencyCounter.getFrequencyCount(uniqueIdentifier, creativeId, expireTimestamp, FrequencyCounter.FrequencyEntity.CREATIVE);
        assertEquals(0, count);
    }
}

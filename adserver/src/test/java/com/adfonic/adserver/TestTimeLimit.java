package com.adfonic.adserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class TestTimeLimit extends BaseAdserverTest {

    private TimeLimit timeLimit;

    @Before
    public void initTest() {

    }

    @Test
    public void testTimeLimit1() {
        long duration = 1000;
        long start = System.currentTimeMillis();
        timeLimit = new TimeLimit(start, duration);
        long expectedExpireTime = start + duration;
        assertEquals(duration, timeLimit.getDuration());
        assertTrue(duration >= timeLimit.getTimeLeft());
        assertFalse(timeLimit.hasExpired());
        assertTrue(expectedExpireTime >= timeLimit.getExpireTime());
        SleepForMilliSeconds(duration + 1000);
        assertTrue(timeLimit.hasExpired());
    }
}

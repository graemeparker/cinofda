package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class TestNonBlockingCalendarPool {

    @Test
    public void testTimeZone() {
        TimeZone timeZone1 = TimeZone.getTimeZone("Europe/London");
        Calendar cal1 = NonBlockingCalendarPool.acquireCalendar(timeZone1);
        assertEquals(timeZone1, cal1.getTimeZone());
        NonBlockingCalendarPool.releaseCalendar(cal1);

        TimeZone timeZone2 = TimeZone.getTimeZone("America/New_York");
        Calendar cal2 = NonBlockingCalendarPool.acquireCalendar(timeZone2);
        assertEquals(timeZone2, cal2.getTimeZone());
    }

    @Test
    public void testWithTime() throws InterruptedException {
        TimeZone timeZone = TimeZone.getTimeZone("EST5EDT");

        long time = System.currentTimeMillis();
        Thread.sleep(500);

        Calendar cal;

        cal = NonBlockingCalendarPool.acquireCalendar();
        assertTrue(cal.getTime().getTime() - time >= 500);
        NonBlockingCalendarPool.releaseCalendar(cal);

        Thread.sleep(500);

        cal = NonBlockingCalendarPool.acquireCalendar(timeZone);
        assertTrue(cal.getTime().getTime() - time >= 1000);
        NonBlockingCalendarPool.releaseCalendar(cal);

        cal = NonBlockingCalendarPool.acquireCalendar(time);
        assertEquals(time, cal.getTime().getTime());
        NonBlockingCalendarPool.releaseCalendar(cal);

        cal = NonBlockingCalendarPool.acquireCalendar(time, timeZone);
        assertEquals(time, cal.getTime().getTime());
        NonBlockingCalendarPool.releaseCalendar(cal);
    }

    @Test
    public void testWithDate() throws InterruptedException {
        TimeZone timeZone = TimeZone.getTimeZone("EST5EDT");

        Date date = new Date();
        Thread.sleep(500);

        Calendar cal;

        cal = NonBlockingCalendarPool.acquireCalendar(date);
        assertEquals(date, cal.getTime());
        NonBlockingCalendarPool.releaseCalendar(cal);

        cal = NonBlockingCalendarPool.acquireCalendar(date, timeZone);
        assertEquals(date, cal.getTime());
        NonBlockingCalendarPool.releaseCalendar(cal);
    }
}

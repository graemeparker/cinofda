package com.adfonic.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Non-blocking pool of reusable Calendar instances
 */
public final class NonBlockingCalendarPool {
    private static final ConcurrentLinkedQueue<Calendar> POOL = new ConcurrentLinkedQueue<Calendar>();

    private NonBlockingCalendarPool() {
    }

    /**
     * Acquire an instance of Calendar. Callers should use the releaseCalendar
     * method when finished with the instance, in order to return it to this
     * pool.
     */
    public static Calendar acquireCalendar() {
        return acquireCalendar(TimeZoneUtils.getDefaultTimeZone());
    }

    /**
     * Acquire an instance of Calendar. Callers should use the releaseCalendar
     * method when finished with the instance, in order to return it to this
     * pool.
     * 
     * @param timeZone
     *            the TimeZone to set on the Calendar
     */
    public static Calendar acquireCalendar(TimeZone timeZone) {
        Calendar calendar = POOL.poll();
        if (calendar == null) {
            // The pool is empty, so construct a new instance
            return Calendar.getInstance(timeZone);
        }

        // Reuse the instance snarfed from the pool, but ensure that the
        // TimeZone and time are set on it.
        if (!timeZone.equals(calendar.getTimeZone())) {
            calendar.setTimeZone(timeZone);
        }
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar;
    }

    /**
     * Acquire an instance of Calendar. Callers should use the releaseCalendar
     * method when finished with the instance, in order to return it to this
     * pool.
     * 
     * @param timeInMillis
     *            the new time in UTC milliseconds from the epoch
     */
    public static Calendar acquireCalendar(long timeInMillis) {
        return acquireCalendar(timeInMillis, TimeZoneUtils.getDefaultTimeZone());
    }

    /**
     * Acquire an instance of Calendar. Callers should use the releaseCalendar
     * method when finished with the instance, in order to return it to this
     * pool.
     * 
     * @param timeInMillis
     *            the new time in UTC milliseconds from the epoch
     * @param timeZone
     *            the TimeZone to set on the Calendar
     */
    public static Calendar acquireCalendar(long timeInMillis, TimeZone timeZone) {
        Calendar calendar = POOL.poll();
        if (calendar == null) {
            // The pool is empty, so construct a new instance
            calendar = Calendar.getInstance(timeZone);
        } else {
            // Reuse the instance snarfed from the pool, but ensure that the
            // TimeZone and time are set on it.
            if (!timeZone.equals(calendar.getTimeZone())) {
                calendar.setTimeZone(timeZone);
            }
        }
        calendar.setTimeInMillis(timeInMillis);
        return calendar;
    }

    /**
     * Acquire an instance of Calendar. Callers should use the releaseCalendar
     * method when finished with the instance, in order to return it to this
     * pool.
     * 
     * @param date
     *            the given date
     */
    public static Calendar acquireCalendar(Date date) {
        return acquireCalendar(date, TimeZoneUtils.getDefaultTimeZone());
    }

    /**
     * Acquire an instance of Calendar. Callers should use the releaseCalendar
     * method when finished with the instance, in order to return it to this
     * pool.
     * 
     * @param date
     *            the given date
     * @param timeZone
     *            the TimeZone to set on the Calendar
     */
    public static Calendar acquireCalendar(Date date, TimeZone timeZone) {
        Calendar calendar = POOL.poll();
        if (calendar == null) {
            // The pool is empty, so construct a new instance
            calendar = Calendar.getInstance(timeZone);
        } else {
            // Reuse the instance snarfed from the pool, but ensure that the
            // TimeZone and time are set on it. Note that we are deliberately
            // NOT checking the TimeZone that was already set on the instance.
            // When you call Calendar.getTimeZone, it actually clones it before
            // returning it. And since we're about to call setTime() below,
            // which "resets" the calendar's fields anyway, there's nothing to
            // be saved performance-wise by NOT setting the TimeZone. So just
            // blindly set it, even if it's the same...most efficient (ugh).
            calendar.setTimeZone(timeZone);
        }
        calendar.setTime(date);
        return calendar;
    }

    /**
     * Release an instance of Calendar back to this pool.
     */
    public static void releaseCalendar(Calendar calendar) {
        if (calendar!=null){
            POOL.add(calendar);
        }
    }
}

package com.adfonic.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Time-based counter that keeps track of a count of events with a sliding time
 * window. An "event" equates to any time one of the increment methods gets
 * called. The window duration is configurable and may be changed on the fly
 * without adversely affecting any other threads.
 * 
 * @author Dan Checkoway
 */
public class SlidingWindowCounter {
    private final AtomicLong windowDurationMillisec = new AtomicLong(-1);
    private final List<Long> eventTimestamps = new LinkedList<Long>();

    /**
     * Default constructor. NOTE: if you don't call setWindowDurationMillisec
     * after constructing this object, it will be configured without a window
     * duration, which means it is simply a plain vanilla atomic counter. So
     * call the setter.
     */
    public SlidingWindowCounter() {
    }

    public SlidingWindowCounter(long windowDurationMillisec) {
        this(windowDurationMillisec, TimeUnit.MILLISECONDS);
    }

    public SlidingWindowCounter(long windowDuration, TimeUnit timeUnit) {
        if (windowDuration <= 0) {
            throw new IllegalArgumentException("Window duration cannot be <= 0");
        }
        windowDurationMillisec.set(TimeUnit.MILLISECONDS.convert(windowDuration, timeUnit));
    }

    public long getWindowDurationMillisec() {
        return windowDurationMillisec.get();
    }

    public long getWindowDuration(TimeUnit timeUnit) {
        return timeUnit.convert(windowDurationMillisec.get(), TimeUnit.MILLISECONDS);
    }

    public void setWindowDurationMillisec(long windowDurationMillisec) {
        setWindowDurationMillisec(windowDurationMillisec, TimeUnit.MILLISECONDS);
    }

    public void setWindowDurationMillisec(long windowDuration, TimeUnit timeUnit) {
        if (windowDuration <= 0) {
            throw new IllegalArgumentException("Window duration cannot be <= 0");
        }
        this.windowDurationMillisec.set(TimeUnit.MILLISECONDS.convert(windowDuration, timeUnit));
    }

    /**
     * Get the current count during the previous window duration amount of time
     */
    public long get() {
        synchronized (eventTimestamps) {
            return pruneAndCount();
        }
    }

    /** Increment the count for the current time */
    public void increment() {
        increment(System.currentTimeMillis());
    }

    /**
     * Increment the count at a specific time. This is useful if you're
     * post-processing an event and want to track the actual time the event
     * occurred as opposed to current time. NOTE: the event may not actually
     * affect the sliding count if the timestamp is far enough in the past so
     * that it's prior to the window duration.
     */
    public void increment(long timestamp) {
        synchronized (eventTimestamps) {
            eventTimestamps.add(timestamp);
        }
    }

    /**
     * Increment the count for the current time and return the current count
     * during the previous window duration amount of time
     */
    public long incrementAndGet() {
        return incrementAndGet(System.currentTimeMillis());
    }

    /**
     * Increment the count at a specific time and return the current count
     * during the previous window duration amount of time. This is useful if
     * you're post-processing an event and want to track the actual time the
     * event occurred as opposed to current time. NOTE: the event may not
     * actually affect the sliding count if the timestamp is far enough in the
     * past so that it's prior to the window duration.
     */
    public long incrementAndGet(long timestamp) {
        synchronized (eventTimestamps) {
            eventTimestamps.add(timestamp);
            return pruneAndCount();
        }
    }

    /**
     * Increment the count for the current time and return the count during the
     * previous window duration amount of time NOT including this event
     */
    public long getAndIncrement() {
        return getAndIncrement(System.currentTimeMillis());
    }

    /**
     * Increment the count at a specific time and return the count during the
     * previous window duration amount of time NOT including this event.
     */
    public long getAndIncrement(long timestamp) {
        synchronized (eventTimestamps) {
            long count = pruneAndCount();
            eventTimestamps.add(timestamp);
            return count;
        }
    }

    /**
     * Remove any event timestamps that occurred prior to the current time minus
     * the window duration, and return the number of event timestamps that
     * occurred during the window
     */
    private long pruneAndCount() {
        // eventTimestamps is already synchronized...just prune it
        long currentDuration = windowDurationMillisec.get();
        if (currentDuration > 0) {
            long threshold = System.currentTimeMillis() - currentDuration;
            while (!eventTimestamps.isEmpty() && ((LinkedList<Long>) eventTimestamps).getFirst() < threshold) {
                ((LinkedList<Long>) eventTimestamps).removeFirst();
            }
        }
        return eventTimestamps.size();
    }
}

package com.adfonic.adserver.bidmanager;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedBidEntry<T> implements Delayed {

    private final T element;
    private final long expiryTimeInMilli;

    public DelayedBidEntry(T element, long numberOfMilliSecondsToLive) {
        this.element = element;
        this.expiryTimeInMilli = System.currentTimeMillis() + numberOfMilliSecondsToLive;
    }

    public T getElement() {
        return element;
    }

    @Override
    public int compareTo(Delayed delayed) {
        if (delayed == this) {
            return 0;
        }
        return (int) (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));

    }

    @Override
    public long getDelay(TimeUnit timeUnit) {
        return timeUnit.convert((expiryTimeInMilli - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
    }

}

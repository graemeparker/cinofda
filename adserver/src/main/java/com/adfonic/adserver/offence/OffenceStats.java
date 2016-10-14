package com.adfonic.adserver.offence;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.adfonic.util.stats.CircularBuffer;

/**
 * 
 * @author mvanek
 *
 */
public class OffenceStats<O, R> {

    private final O offence;

    private final AtomicInteger count = new AtomicInteger(0);

    private final CircularBuffer<R> snapshot;

    private final Date firstOccuredAt;

    private Date lastOccuredAt;

    public OffenceStats(O offence, int snapshotSize, Class<R> offenceType) {
        this.offence = offence;
        this.snapshot = new CircularBuffer<R>(snapshotSize, offenceType);
        this.firstOccuredAt = new Date();
    }

    public void record(R recordable) {
        count.incrementAndGet();
        snapshot.add(recordable);
        lastOccuredAt = new Date();
    }

    public int getCount() {
        return count.intValue();
    }

    public O getOffence() {
        return offence;
    }

    public R[] getSnapshot() {
        return snapshot.snapshot();
    }

    public Date getLastOccuredAt() {
        return lastOccuredAt;
    }

    public Date getFirstOccuredAt() {
        return firstOccuredAt;
    }

    @Override
    public String toString() {
        return "OffenceStats {" + offence + ", " + count + "}";
    }
}

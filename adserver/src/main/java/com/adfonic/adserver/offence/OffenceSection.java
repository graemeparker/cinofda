package com.adfonic.adserver.offence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.adfonic.adserver.offence.OffenceRegistry.BidExceptionStats;
import com.adfonic.adserver.offence.OffenceRegistry.BidFailureStats;
import com.adfonic.util.stats.CircularBuffer;

/**
 * @author mvanek
 * 
 * Just a grouping of offences under same sectionId, most likely publisherId in RTB
 *
 */
public class OffenceSection {

    private final String sectionId;

    // TODO proper encapsualtion here...
    protected final Map<Class<? extends Exception>, CircularBuffer<BidExceptionStats>> exceptions = new ConcurrentHashMap<Class<? extends Exception>, CircularBuffer<BidExceptionStats>>();

    protected final Map<String, CircularBuffer<BidFailureStats>> oneliners = new ConcurrentHashMap<String, CircularBuffer<BidFailureStats>>();

    public OffenceSection(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void clear() {
        exceptions.clear();
    }

    /**
     * List of Exception Instance Stats 
     */
    public List<BidExceptionStats[]> values() {
        List<BidExceptionStats[]> retval = new ArrayList<OffenceRegistry.BidExceptionStats[]>();
        Collection<CircularBuffer<BidExceptionStats>> values = exceptions.values();
        for (CircularBuffer<BidExceptionStats> buffer : values) {
            retval.add(buffer.snapshot());
        }
        return retval;
    }

    public BidExceptionStats[] getStats(Class<? extends Exception> key) {
        CircularBuffer<BidExceptionStats> buffer = exceptions.get(key);
        if (buffer != null) {
            return buffer.snapshot();
        } else {
            return null;
        }
    }

    public BidFailureStats[] getMessageStats(String offenceType) {
        CircularBuffer<BidFailureStats> buffer = oneliners.get(offenceType);
        if (buffer != null) {
            return buffer.snapshot();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "OffenceSection{" + sectionId + ", " + exceptions + "}";
    }

}

package com.adfonic.adserver.offence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.adfonic.adserver.AdServerFeatureFlag;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.util.stats.CircularBuffer;

/**
 * 
 * @author mvanek
 *
 */
public class OffenceRegistry {

    private ConcurrentHashMap<String, OffenceSection> sections = new ConcurrentHashMap<String, OffenceSection>();

    private final int samplesPerOffence;

    private final int instancesPerType;

    private static final ExceptionEquator<Exception> defaultComparator = new StackTraceEquator();

    private Map<Class<? extends Exception>, ExceptionEquator<? extends Exception>> xEquators = new HashMap<Class<? extends Exception>, ExceptionEquator<? extends Exception>>();

    /**
     * @param instancesPerType - Number of NullPointerExceptions occurences in different locations (stack trace) we will track
     * @param samplesPerOffence - Number of instances of the same Exception and Request for every of above
     */
    public OffenceRegistry(int instancesPerType, int samplesPerOffence) {
        if (instancesPerType < 1) {
            throw new IllegalArgumentException("instancesPerType should be positive");
        }
        if (samplesPerOffence < 1) {
            throw new IllegalArgumentException("samplesPerOffence should be positive");
        }
        this.instancesPerType = instancesPerType;
        this.samplesPerOffence = samplesPerOffence;
        this.xEquators.put(NoBidException.class, new NoBidExceptionEquator());
    }

    public Collection<OffenceSection> sections() {
        return values();
    }

    public Collection<OffenceSection> values() {
        return Collections.unmodifiableCollection(sections.values());
    }

    public OffenceSection getSection(String sectionId) {
        return sections.get(sectionId);
    }

    public void setEquator(Class<? extends Exception> xClass, ExceptionEquator<? extends Exception> comparator) {
        if (xClass == null) {
            throw new IllegalArgumentException("Null class");
        }
        if (comparator == null) {
            throw new IllegalArgumentException("Null comparator");
        }
        xEquators.put(xClass, comparator);
    }

    public void clear() {
        sections.clear();
    }

    public <T extends Enum<T>> void record(T type, String message) {

    }

    public void record(Exception exception, RtbExecutionContext<?, ?> executionContext) {
        if (!AdServerFeatureFlag.OFFENCE_REGISTRY.isEnabled()) {
            return;
        }
        String sectionId = executionContext.getPublisherExternalId();
        OffenceSection section = sections.get(sectionId);
        if (section == null) {
            section = new OffenceSection(sectionId);
            sections.put(sectionId, section);
        }
        /*
        ByydRequest byydRequest = executionContext.getByydRequest();
        if (byydRequest != null) {
            byydRequest.setContext(null); //Free quite heavy targeting context - BidRequest may stay in offence registry long time...
        }
        executionContext.getHttpContext().getHttpRequest().removeAttribute(RtbExecutionContext.RTB_CONTEXT);//break cycle
        */

        doRecord(section, exception, executionContext);
    }

    private void doRecord(OffenceSection section, Exception exception, RtbExecutionContext<?, ?> request) {
        Class<? extends Exception> xClass = exception.getClass();
        CircularBuffer<BidExceptionStats> exceptionStatBuffer = section.exceptions.get(xClass);
        if (exceptionStatBuffer == null) {
            exceptionStatBuffer = new CircularBuffer<BidExceptionStats>(instancesPerType, BidExceptionStats.class);
            section.exceptions.put(xClass, exceptionStatBuffer);
        }

        ExceptionEquator<Exception> comparator = (ExceptionEquator<Exception>) xEquators.get(xClass);
        if (comparator == null) {
            comparator = defaultComparator;
        }
        BidExceptionStats stats = null;
        BidExceptionStats[] snapshot = exceptionStatBuffer.snapshot();
        for (BidExceptionStats item : snapshot) {
            if (comparator.isEqual(exception, item.getOffence())) {
                stats = item;
                break;
            }
        }
        if (stats == null) {
            stats = new BidExceptionStats(exception, samplesPerOffence);
            exceptionStatBuffer.add(stats);
        }
        stats.record(new TroubledBidRequest(exception, request, section.getSectionId()));
    }

    /**
     * Compare exception class and stack trace but ignore exception message
     */
    static class StackTraceEquator implements ExceptionEquator<Exception> {

        @Override
        public boolean isEqual(Exception x, Exception y) {
            /*
            if (!x.getClass().equals(y.getClass())) {
                return false;
            }
            */
            StackTraceElement[] xStackTrace = x.getStackTrace();
            StackTraceElement[] yStackTrace = y.getStackTrace();
            for (int i = 0; i < xStackTrace.length; i++) {
                if (!xStackTrace[i].equals(yStackTrace[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * For NoBidException, there is no stacktrace only noBidReson
     */
    static class NoBidExceptionEquator implements ExceptionEquator<NoBidException> {

        @Override
        public boolean isEqual(NoBidException x, NoBidException y) {
            return x.getNoBidReason() == y.getNoBidReason() && x.getOffenceName().equals(y.getOffenceName());
        }
    }

    public static class BidExceptionStats extends OffenceStats<Exception, TroubledBidRequest> {

        public BidExceptionStats(Exception offence, int snapshotSize) {
            super(offence, snapshotSize, TroubledBidRequest.class);
        }

    }

    public static class BidFailureStats extends OffenceStats<String, TroubledBidRequest> {

        public BidFailureStats(String offence, int snapshotSize) {
            super(offence, snapshotSize, TroubledBidRequest.class);
        }

    }

    public interface ExceptionEquator<T extends Exception> {

        public boolean isEqual(T x, T y);
    }

}

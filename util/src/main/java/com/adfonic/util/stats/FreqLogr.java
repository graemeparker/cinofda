package com.adfonic.util.stats;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mvanek
 * 
 * On very busy places, we do not want to log every exception instance...
 * 
 * TODO merge with OffenceRegistry somehow...
 */
public class FreqLogr {

    public static final String FLUSH_SYSPRO = "FreqLogr.flush.period";

    private static final Logger logger = LoggerFactory.getLogger(FreqLogr.class);

    private static Map<Class<? extends Exception>, ConcurrentLinkedQueue<ExceptionData>> xstorage = new ConcurrentHashMap<>();

    private static long flushPeriodMillis = Long.parseLong(System.getProperty(FLUSH_SYSPRO, "5000"));
    private static long lastFlushMillis = System.currentTimeMillis();

    public static void report(Exception exception) {
        report(exception, exception.getMessage());
    }

    /**
     *  XXX reporting can be asynchronous...
     */
    public static void report(Exception exception, String message) {
        Class<? extends Exception> xclass = exception.getClass();
        ConcurrentLinkedQueue<ExceptionData> xinstances = xstorage.get(xclass);
        if (xinstances == null) {
            xinstances = new ConcurrentLinkedQueue<ExceptionData>();
            xstorage.put(xclass, xinstances);
        }
        ExceptionData xinstance = find(exception, xinstances);
        if (xinstance == null) {
            xinstance = new ExceptionData(exception);
            xinstances.add(xinstance);
        }
        xinstance.record(message);
        // XXX flushing can by asynchronous...
        long currentMillis = System.currentTimeMillis();
        if (currentMillis > lastFlushMillis + flushPeriodMillis) {
            flush(xstorage);
            xstorage.clear();
            lastFlushMillis = currentMillis;
            flushPeriodMillis = Long.parseLong(System.getProperty(FLUSH_SYSPRO, "5000")); // flush period can change
        }
    }

    public static void flush(Map<Class<? extends Exception>, ConcurrentLinkedQueue<ExceptionData>> storage) {
        Collection<ConcurrentLinkedQueue<ExceptionData>> xtypes = storage.values();
        for (ConcurrentLinkedQueue<ExceptionData> xinstances : xtypes) {
            for (ExceptionData xinstance : xinstances) {
                String[] xmessages = xinstance.getMessages().snapshot();
                logger.error(xinstance.getException().getClass().getName() + " occured " + xinstance.getCount() + " times", xinstance.exception);
                logger.error("Last " + xmessages.length + " messages");
                for (String xmessage : xmessages) {
                    logger.error(xmessage);
                }
            }
        }
    }

    /**
     * For testing and monitoring. Do not mess up with it!
     */
    public static Map<Class<? extends Exception>, ConcurrentLinkedQueue<ExceptionData>> getXstorage() {
        return xstorage;
    }

    private static ExceptionData find(Exception x, ConcurrentLinkedQueue<ExceptionData> instances) {
        for (ExceptionData instance : instances) {
            if (isSameStackTrace(x, instance.exception)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Check if reflection invacation is faster than cloning of StackTrace
     * 
     * XXX x.getStackTrace() makes clone of stack but x.getOurStackTrace() is private and x.getStackTraceDepth() is package 
     */
    private static boolean isSameStackTrace(Exception x, Exception y) {
        StackTraceElement[] xsta = x.getStackTrace(); // 
        StackTraceElement[] ysta = y.getStackTrace();
        if (xsta.length != ysta.length) {
            return false;
        } else {
            for (int i = 0; i < xsta.length; ++i) {
                if (!xsta[i].equals(ysta[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
    public static void report(String message) {
        
    }
    */

    public static class ExceptionData {

        private final Exception exception;
        private final CircularBuffer<String> messages = new CircularBuffer<>(10, String.class);
        private final AtomicInteger count = new AtomicInteger();
        private long lastTimestamp = System.currentTimeMillis();

        public ExceptionData(Exception exception) {
            this.exception = exception;
        }

        public void record(String message) {
            count.incrementAndGet();
            lastTimestamp = System.currentTimeMillis();
            messages.add(message);
        }

        public Exception getException() {
            return exception;
        }

        public CircularBuffer<String> getMessages() {
            return messages;
        }

        public AtomicInteger getCount() {
            return count;
        }

        public long getLastTimestamp() {
            return lastTimestamp;
        }

        @Override
        public String toString() {
            return "ExceptionData {exception=" + exception + ", count=" + count + ", lastTimestamp=" + lastTimestamp + ", messages=" + messages + "}";
        }

    }

}

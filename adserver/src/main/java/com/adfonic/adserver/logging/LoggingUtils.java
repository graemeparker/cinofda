package com.adfonic.adserver.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

/**
 * Standard Logger to replace logging within the adserver to a more standard formatting
 * with certain required values (like publisher / advertiser id, adspace / createive id etc.) logged in the log messages.
 * @see https://tickets.adfonic.com/browse/AD-283
 */
@Deprecated
public class LoggingUtils {

    private static final FastDateFormat TIME_FORMAT = FastDateFormat.getInstance("yyyyMMddhhmmss");

    private static final String STACK_TRACE = "Stacktrace available";

    @SuppressWarnings("rawtypes")
    public static void log(Logger LOG, Level level, Impression impression, TargetingContext context, Class clazz, String method, String message) {
        log(LOG, level, null, impression, context, clazz, method, message, null);
    }

    @SuppressWarnings("rawtypes")
    public static void log(Logger LOG, Level level, Impression impression, TargetingContext context, Class clazz, String method, String message, Throwable thrown) {
        log(LOG, level, null, impression, context, clazz, method, message, thrown);
    }

    /**
     * Prepare the message to be logged in the following format
     *  <LOG LEVEL> <TID> <GMT_TIME_ID> <PUB_ID> <ADSPACE_ID> <ADV_ID> <CREATIVE_ID> <IMPRESSION_ID> <MODULE> <Message> <Stack Trace> (optional)
     * @param LOG
     * @param level
     * @param eventTime
     * @param impression
     * @param context
     * @param module
     * @param method
     * @param message
     * @param thrown
     */
    @SuppressWarnings("rawtypes")
    public static void log(Logger LOG, Level level, Date eventTime, Impression impression, TargetingContext context, Class clazz, String method, String message, Throwable thrown) {
        List<String> values = new ArrayList<String>();
        if (eventTime != null)
            values.add(TIME_FORMAT.format(eventTime));
        else
            values.add(TIME_FORMAT.format(new Date()));
        addCommonValues(values, impression, context);
        values.add(clazz.getName());
        values.add(method);
        values.add(message);
        values.add(BooleanUtils.toString(thrown != null, STACK_TRACE, StringUtils.EMPTY));
        writeToLogFile(LOG, level, values, thrown);
    }

    private static void writeToLogFile(Logger LOG, Level level, List<String> values, Throwable thrown) {
        LOG.log(level, StringUtils.join(values, '\t'), thrown);
    }

    /**
     * Add values that are common for every request/result
     */
    private static void addCommonValues(List<String> values, Impression impression, TargetingContext context) {
        String publicationId = null;
        String adSpaceId = null;
        String advertiserId = null;
        String creativeId = null;
        String impressionId = null;

        if (context != null) {
            if (context.getAdSpace() != null) {
                adSpaceId = String.valueOf(context.getAdSpace().getId());
                if (context.getAdSpace().getPublication() != null) {
                    publicationId = context.getAdSpace().getPublication().getExternalID();

                }
            }
            if (impression != null) {
                CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
                if (creative != null) {
                    AdvertiserDto advertiser = creative.getCampaign().getAdvertiser();
                    if (advertiser != null) {
                        advertiserId = advertiser.getExternalID();
                    }
                    creativeId = creative.getExternalID();
                }
            }
        }
        if (impression != null) {
            impressionId = impression.getExternalID();
        }

        values.add(StringUtils.defaultString(publicationId));
        values.add(StringUtils.defaultString(adSpaceId));
        values.add(StringUtils.defaultString(advertiserId));
        values.add(StringUtils.defaultString(creativeId));
        values.add(StringUtils.defaultString(impressionId));
    }

    private static AtomicLong lastDump = new AtomicLong();
    private static AtomicLong occurance = new AtomicLong();

    public static void logUnexpectedError(Logger logger, Throwable t, String message) {
        occurance.incrementAndGet();
        if (logger.isLoggable(Level.INFO)) {
            // logger.log(Level.INFO, message == null? "Unexpected in rtb":
            // message, t);
            LoggingUtils.log(logger, Level.INFO, null, null, LoggingUtils.class, "logUnexpectedError", message == null ? "Unexpected in rtb" : message, t);
            // logger.warning(ExceptionUtils.getStackTrace(t));//what we get
            // here is unexpected anyway
        } else {
            long curSec = System.currentTimeMillis() / 1000 / 5;
            long lastSec = lastDump.get();
            if (lastDump.get() != curSec) {
                if (lastDump.compareAndSet(lastSec, curSec)) {
                    logger.warning("Details of unexpected Log: " + message + " / " + t.getMessage() + "/" + t.getClass() + " occured: " + occurance.get());
                    if (t != null && t.getStackTrace() != null) {
                        for (StackTraceElement ste : t.getStackTrace()) {
                            logger.warning("  stacktrace: " + ste.getClassName() + "." + ste.getMethodName() + ":" + ste.getLineNumber());
                        }
                    }
                    occurance.set(0);
                }
            }
        }
    }
}

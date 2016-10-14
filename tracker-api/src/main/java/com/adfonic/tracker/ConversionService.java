package com.adfonic.tracker;

import java.util.List;

import com.adfonic.adserver.Click;

public interface ConversionService {
    /**
     * This method tracks at most one conversion per click.  It indicates to
     * the caller whether or not it was unique.
     * @return true if this was the first conversion for the given click, or
     * false if this was a duplicate
     */
    boolean trackConversion(Click click);

    /**
     * Schedule a retry of a conversion tracking request.  This should be called
     * when the tracker fails to find the respective click.
     */
    void scheduleConversionRetry(String clickExternalId);

    /**
     * Schedule a retry of a conversion tracking request.  This should be called
     * when the tracker fails to find the respective click.
     */
    void scheduleRetry(PendingConversion pendingConversion);

    /**
     * Deletes a scheduled retry of a conversion tracking request from the db.
     * This should be called once the retry was successfully processed.
     */
    void deleteScheduledConversionRetry(PendingConversion pendingConversion);

    /**
     * Get a list of pending conversions to retry.  This is intended to be used by
     * the app that will periodically reattempt to track conversions that have been
     * scheduled for retry.
     */
    List<PendingConversion> getPendingConversionsToRetry(int maxRows);
}

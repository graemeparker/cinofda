package com.adfonic.adserver;

import java.util.Date;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

public final class AdserverUtils {
    private AdserverUtils() {}
    
    /**
     * Derive the appropriate click TTL based on whether we'll need to
     * give it a longer lifespan for install or conversion tracking.
     * @param impression the original impression
     * @param applicationID the application ID of the campaign being clicked on
     * @param installTrackingEnabled whether or not the campaign needs install tracking
     * @param installTrackingAdXEnabled whether or not the campaign needs AdX install tracking
     * @param conversionTrackingEnabled whether or not the campaign needs conversion tracking
     * @param defaultTtlSeconds the default TTL
     * @param installTrackingTtlSeconds the TTL used when install tracking
     * @param conversionTrackingTtlSeconds the TTL used when conversion tracking
     * @return the TTL in seconds that should be used with the Click being stored
     */
    public static int getClickTtlSeconds(Impression impression,
                                         String applicationID,
                                         Boolean installTrackingEnabled,
                                         Boolean installTrackingAdXEnabled,
                                         Boolean conversionTrackingEnabled,
                                         int defaultTtlSeconds,
                                         int installTrackingTtlSeconds,
                                         int conversionTrackingTtlSeconds)
    {
        // Let's plan to use the default TTL...but we may extend it based on
        // whether or not the click needs to stick around longer for install
        // or conversion tracking.
        int ttlSeconds = defaultTtlSeconds;

        // Extend the expire time for install tracking, if required...
        // - and either install tracking or AdX install tracking is enabled
        // - the campaign has a non-null non-blank applicationID
        // - and at least one device identifier was supplied
        if ((Boolean.TRUE.equals(installTrackingEnabled) || Boolean.TRUE.equals(installTrackingAdXEnabled))
            && StringUtils.isNotBlank(applicationID)
            && MapUtils.isNotEmpty(impression.getDeviceIdentifiers())) {
            // Yup, it's install trackable, so extend the TTL as applicable
            ttlSeconds = Math.max(ttlSeconds, installTrackingTtlSeconds);
        }

        // Extend the expire time for conversion tracking, if required
        if (Boolean.TRUE.equals(conversionTrackingEnabled)) {
            ttlSeconds = Math.max(ttlSeconds, conversionTrackingTtlSeconds);
        }

        return ttlSeconds;
    }
    
    /**
     * Derive the appropriate click expire time based on whether we'll need
     * to give it a longer lifespan for install or conversion tracking.
     * @param impression the original impression
     * @param campaign the campaign of the creative being clicked on
     * @param defaultTtlSeconds the default TTL
     * @param installTrackingTtlSeconds the TTL used when install tracking
     * @param conversionTrackingTtlSeconds the TTL used when conversion tracking
     * @return the expireTime that can be set on the Click being stored
     */
    public static Date getClickExpireTime(Impression impression,
                                          String applicationID,
                                          Boolean installTrackingEnabled,
                                          Boolean installTrackingAdXEnabled,
                                          Boolean conversionTrackingEnabled,
                                          int defaultTtlSeconds,
                                          int installTrackingTtlSeconds,
                                          int conversionTrackingTtlSeconds)
    {
        int ttlSeconds = getClickTtlSeconds(impression, applicationID, installTrackingEnabled, installTrackingAdXEnabled, conversionTrackingEnabled, defaultTtlSeconds, installTrackingTtlSeconds, conversionTrackingTtlSeconds);
        // Simply add the TTL to the impression's creation time
        return DateUtils.addSeconds(impression.getCreationTime(), ttlSeconds);
    }
}

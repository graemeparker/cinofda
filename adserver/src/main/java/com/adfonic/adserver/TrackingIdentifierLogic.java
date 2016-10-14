package com.adfonic.adserver;

import javax.servlet.http.HttpServletResponse;

public interface TrackingIdentifierLogic {
    /**
     * 1. Make sure TrackingIdentifierType is set on the TargetingContext.  This value
     * is based on what's set (or defaulted) on the Publication.
     *
     * 2. Make sure that if we can glean a tracking identifier from the request, that
     * it's stored in secure form in the TargetingContext.SECURE_TRACKING_ID attribute.
     *
     * @param context the targeting context in progress
     * @param response must be provided non-null when cookiesAllowed is true, otherwise optional
     * @param cookiesAllowed whether or not cookies are allowed (when true, response must be non-null)
     * @throws InvalidTrackingIdentifierException if the established tracking identifier is too long
     */
    void establishTrackingIdentifier(TargetingContext context, HttpServletResponse response, boolean cookiesAllowed) throws InvalidTrackingIdentifierException;
}
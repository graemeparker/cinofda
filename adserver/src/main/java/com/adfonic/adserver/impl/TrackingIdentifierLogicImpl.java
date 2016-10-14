package com.adfonic.adserver.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.InvalidTrackingIdentifierException;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TrackingIdentifierLogic;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

@Component
public class TrackingIdentifierLogicImpl implements TrackingIdentifierLogic {

    private static final transient Logger LOG = Logger.getLogger(TrackingIdentifierLogicImpl.class.getName());

    // Package protected so unit tests can access it
    static final String TRACKING_IDENTIFIER_COOKIE = "adfonic-id";

    /** {@inheritDoc} */
    @Override
    public void establishTrackingIdentifier(TargetingContext context, HttpServletResponse response, boolean cookiesAllowed) throws InvalidTrackingIdentifierException {
        String rawTrackingIdentifier = null;
        try {
            AdSpaceDto adSpace = context.getAdSpace();
            if (adSpace == null) {
                LOG.warning("No AdSpace set on TargetingContext, can't determine TrackingIdentifierType");
                return;
            }

            // First let's see what type of tracking identifier we're expecting.
            // The publication will dictate what type to expect.
            TrackingIdentifierType tiType = adSpace.getPublication().getTrackingIdentifierType();

            // Store the tracking identifier type in the context for use by the
            // targeting engine, frequency capping, etc.
            context.setAttribute(TargetingContext.TRACKING_IDENTIFIER_TYPE, tiType);

            // See if we were supplied a raw tracking identifier
            switch (tiType) {
            case PUBLISHER_GENERATED:
            case DEVICE:
                rawTrackingIdentifier = context.getAttribute(Parameters.TRACKING_ID);
                break;
            case COOKIE:
                // Sanity check...
                if (cookiesAllowed) {
                    // Look for it as a cookie
                    rawTrackingIdentifier = context.getCookie(TRACKING_IDENTIFIER_COOKIE);
                    if (StringUtils.isEmpty(rawTrackingIdentifier)) {
                        // We didn't get the cookie, so generate one now and set it on the response
                        rawTrackingIdentifier = UUID.randomUUID().toString();
                        // Set the cookie so we'll see it next time
                        Cookie cookie = new Cookie(TRACKING_IDENTIFIER_COOKIE, rawTrackingIdentifier);
                        cookie.setPath("/");
                        cookie.setMaxAge(3600 * 24 * 365 * 2); // 2 years
                        response.addCookie(cookie);

                        // Make sure we also set the raw tracking id on the context, so that
                        // anything downstream can use it as if the publisher passed r.id
                        context.setAttribute(Parameters.TRACKING_ID, rawTrackingIdentifier);
                    }
                } else {
                    LOG.warning("TrackingIdentifierType=COOKIE but !cookiesAllowed");
                }
                break;
            case NONE:
                break; // Don't even bother looking
            default:
                LOG.warning("Unsupported TrackingIdentifierType: " + tiType);
                break;
            }
        } finally {
            // Make sure we set up TargetingContext.SECURE_TRACKING_ID
            if (StringUtils.isNotBlank(rawTrackingIdentifier)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Raw tracking identifier found, setting TargetingContext.SECURE_TRACKING_ID");
                }
                context.setAttribute(TargetingContext.SECURE_TRACKING_ID, DigestUtils.shaHex(rawTrackingIdentifier));
            }
        }
    }
}

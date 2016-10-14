package com.adfonic.ddr;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DDR service base implementation, complete with logic for doing device
 * detection not only using User-Agent, but also checking alternate headers
 * for browsers such as Opera Mini, Google Wireless Transcoder, etc.
 */
public abstract class AbstractDdrService implements DdrService {
    private static final transient Logger LOG = Logger.getLogger(AbstractDdrService.class.getName());

    /**
     * Subclasses must implement this.
     */
    protected abstract Map<String,String> doGetDdrProperties(String userAgent);
    
    /** @{inheritDoc} */
    @Override
    public Map<String,String> getDdrProperties(String userAgent) {
        Map<String,String> props = doGetDdrProperties(userAgent);

        // Make sure the properties are actually "valid" in terms of how the
        // callers expect to use 'em.  Otherwise we need to return null.
        if (props == null || props.isEmpty()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("No device properties found for User-Agent: " + userAgent);
            }
            return null;
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(props.toString());
            }
            return props;
        }
    }

    /** @{inheritDoc} */
    @Override
    public Map<String, String> getDdrProperties(HttpHeaderAware context) {
        Map<String, String> props;

        props = checkForOperaMini(context);
        if (props == null) {
            props = checkForGoogleWirelessTranscoder(context);
        }
        if (props == null) {
            props = checkForDeviceUserAgent(context);
        }
        if (props == null) {
            String userAgent = getUserAgentFromContext(context);
            if (userAgent == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Cannot get device properties, no User-Agent");
                }
                props = null;
            } else {
                props = getDdrProperties(userAgent);
                if (props == null && LOG.isLoggable(Level.INFO)) {
                    LOG.info("No device properties found for for User-Agent: " + userAgent);
                }
            }
        }

        return props;
    }
    
    /**
     * Special handling for Opera Mini
     * @see http://dev.opera.com/articles/view/opera-mini-request-headers/
     */
    Map<String,String> checkForOperaMini(HttpHeaderAware context) {
        String operaMiniUA = context.getHeader("X-OperaMini-Phone-UA");
        if (operaMiniUA == null) {
            return null;
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Trying X-OperaMini-Phone-UA: " + operaMiniUA);
        }
        Map<String,String> props = getDdrProperties(operaMiniUA);
        if (props != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("X-OperaMini-UA identified successfully");
            }
            setUserAgentInContext(operaMiniUA, context);
        } else if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("X-OperaMini-UA was NOT identified: " + operaMiniUA);
        }
        return props;
    }

    /**
     * Special handling for Google Wireless Transcoder, which sends us
     * an "X-Original-User-Agent" header.
     */
    Map<String,String> checkForGoogleWirelessTranscoder(HttpHeaderAware context) {
        String originalUserAgent = context.getHeader("X-Original-User-Agent");
        if (originalUserAgent == null) {
            return null;
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Trying X-Original-User-Agent: " + originalUserAgent);
        }
        Map<String,String> props = getDdrProperties(originalUserAgent);
        if (props != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("X-Original-User-Agent identified successfully");
            }
            setUserAgentInContext(originalUserAgent, context);
        } else if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("X-Original-User-Agent was NOT identified: " + originalUserAgent);
        }
        return props;
    }

    /**
     * Look for an X-Device-User-Agent header
     */
    Map<String,String> checkForDeviceUserAgent(HttpHeaderAware context) {
        String deviceUserAgent = context.getHeader("X-Device-User-Agent");
        if (deviceUserAgent == null) {
            return null;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Trying X-Device-User-Agent: " + deviceUserAgent);
        }
        Map<String,String> props = getDdrProperties(deviceUserAgent);
        if (props != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("X-Device-User-Agent identified successfully");
            }
            setUserAgentInContext(deviceUserAgent, context);
        } else if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("X-Device-User-Agent was NOT identified: " + deviceUserAgent);
        }
        return props;
    }

    /**
     * Get the most relevant value of User-Agent from the given context
     */
    static String getUserAgentFromContext(HttpHeaderAware context) {
        if (context instanceof UserAgentAware) {
            return ((UserAgentAware)context).getEffectiveUserAgent();
        } else {
            return context.getHeader("User-Agent");
        }
    }

    /**
     * Update the context's effective User-Agent
     */
    static void setUserAgentInContext(String effectiveUserAgent, HttpHeaderAware context) {
        if (context instanceof UserAgentAware) {
            // Swap this value into the context for the effective User-Agent
            ((UserAgentAware)context).setUserAgent(effectiveUserAgent);
        }
    }
}

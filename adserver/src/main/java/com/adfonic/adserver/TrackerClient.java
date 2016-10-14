package com.adfonic.adserver;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Queue;

import org.springframework.jms.core.JmsTemplate;

import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.jms.JmsUtils;
import com.adfonic.tracking.TrackingMessage;
import com.adfonic.tracking.action.ConversionMessage;
import com.adfonic.tracking.action.InstallMessage;

public class TrackerClient {

    private static final transient Logger LOG = Logger.getLogger(TrackerClient.class.getName());

    /** Parameter name for the Base64-encoded Impression */
    public static final String IMPRESSION_BASE64 = "i64";
    /** Parameter name for the IP address */
    public static final String IP_ADDRESS = "ip";
    /** Parameter name for the User-Agent */
    public static final String USER_AGENT = "ua";

    // Response map used whenever this client is configured as blocked
    @SuppressWarnings("serial")
    static Map<String, String> BLOCKED_RESPONSE_MAP = new LinkedHashMap<String, String>() {
        {
            put("success", "0");
            put("error", "Proxied tracker calls blocked per configuration");
        }
    };

    // Response map used when the message was queued for processing
    @SuppressWarnings("serial")
    static Map<String, String> RESPONSE_QUEUED_MAP = new LinkedHashMap<String, String>() {
        {
            put("success", "0.5");
            put("error", "Queued for processing. Status unknowable");
        }
    };

    private Map<String, String> queueAction(TrackingMessage trackerMessage) {
        jmsUtils.sendObject(centralJmsTemplate, actionTrackingQueue, trackerMessage);
        //LOG.info("Deprecated tracking call on adserver; Queueing " + trackerMessage.getTrackerPath());
        LoggingUtils.log(LOG, Level.INFO, null, null, this.getClass(), "queueAction", "Deprecated tracking call on adserver; Queueing " + trackerMessage.getTrackerPath());
        return RESPONSE_QUEUED_MAP;
    }

    public String getBaseUrlLookalike() {
        return "http://localhost/tracker";
    }

    private final boolean blocked;

    private JmsUtils jmsUtils;
    private Queue actionTrackingQueue;
    private JmsTemplate centralJmsTemplate;

    public TrackerClient(boolean blocked, Queue actionTrackingQueue, JmsUtils jmsUtils, JmsTemplate centralJmsTemplate) {
        this.blocked = blocked;
        this.jmsUtils = jmsUtils;
        this.actionTrackingQueue = actionTrackingQueue;
        this.centralJmsTemplate = centralJmsTemplate;
        if (blocked) {
            LOG.warning("Tracker proxy calls will be BLOCKED");
        }
    }

    /**
     * Track an install
     * @param appId the application id
     * @param udid the device UDID
     * @return the JSON response map from the tracker service
     */
    @SuppressWarnings("rawtypes")
    public Map trackInstall(String appId, String udid) throws java.io.IOException {
        if (blocked) {
            if (LOG.isLoggable(Level.FINE)) {
                //LOG.fine("Tracker proxy calls blocked for testing, not tracking install of appId=" + appId + ", udid=" + udid);
                LoggingUtils.log(LOG, Level.INFO, null, null, this.getClass(), "trackInstall", "Tracker proxy calls blocked for testing, not tracking install of appId=" + appId
                        + ", udid=" + udid);
            }
            return BLOCKED_RESPONSE_MAP;
        }

        InstallMessage installMessage = new InstallMessage(appId, udid);
        try {
            // Just for backward compatible error validation
            new URI(getBaseUrlLookalike() + installMessage.getTrackerPath());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Illegal appId and/or udid supplied", e);
        }

        return queueAction(installMessage);
    }

    /**
     * Track a conversion
     * @param clickExternalID the external ID of the click
     * @return the JSON response map from the tracker service
     */
    @SuppressWarnings("rawtypes")
    public Map trackConversion(String clickExternalID) throws java.io.IOException {
        if (blocked) {
            if (LOG.isLoggable(Level.FINE)) {
                //LOG.fine("Tracker proxy calls blocked for testing, not tracking conversion of " + clickExternalID);
                LoggingUtils.log(LOG, Level.INFO, null, null, this.getClass(), "trackConversion", "Tracker proxy calls blocked for testing, not tracking conversion of "
                        + clickExternalID);
            }
            return BLOCKED_RESPONSE_MAP;
        }

        ConversionMessage conversionMessage = new ConversionMessage(clickExternalID);
        try {
            // Just for backward compatible error validation
            new URI(getBaseUrlLookalike() + conversionMessage.getTrackerPath());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Invalid clickExternalID: " + clickExternalID, e);
        }

        return queueAction(conversionMessage);
    }

}

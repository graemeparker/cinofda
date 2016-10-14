package com.adfonic.adserver;

import java.util.Date;
import java.util.TimeZone;

import com.adfonic.domain.AdAction;
import com.adfonic.util.HostUtils;

/**
 * Factory for working with AdEvent objects and encapsulating their serialization and deserialization.
 */
public class AdEventFactory {

    private final KryoManager kryoManager;

    public AdEventFactory(KryoManager kryoManager) {
        this.kryoManager = kryoManager;
    }

    /**
     * Return a new instance of AdEvent with adAction, host, and eventTime set.
     * This is the proper way to construct an AdEvent for logging purposes.
     * @param adAction the AdAction value for this event
     */
    public AdEvent newInstance(AdAction adAction) {
        return newInstance(adAction, new Date(), null);
    }

    /**
     * Return a new instance of AdEvent with adAction, host, and eventTime set.
     * This is the proper way to construct an AdEvent for logging purposes.
     * @param adAction the AdAction value for this event
     * @param eventTime the time at which the event occurred
     * @param userTimeZone the TimeZone of the end user, if known, otherwise pass null
     */
    public AdEvent newInstance(AdAction adAction, Date eventTime, TimeZone userTimeZone) {
        return new AdEvent(adAction, HostUtils.getHostName(), eventTime, userTimeZone);
    }

    /**
     * Serialize an AdEvent to a raw byte array
     */
    public byte[] serialize(AdEvent adEvent) {
        return kryoManager.writeObject(adEvent);
    }

    /**
     * Deserialize an AdEvent from a raw byte array
     */
    public AdEvent deserialize(byte[] serialized) {
        return kryoManager.readObject(serialized, AdEvent.class);
    }

    /**
     * Clone an AdEvent (SC-1, i.e. used by datacollector when splitting
     * up an AD_SERVED_AND_IMPRESSION event into two events)
     */
    public AdEvent cloneAdEvent(AdEvent copyMe) {
        return new AdEvent(copyMe);
    }
    
    /**
     * Return a new instance of AdEvent with adAction, host, eventTime and usertimeid set.
     * Used by datacollector to create AdEvent from serialized JSON AdEvent 
     * @param adAction the AdAction value for this event
     * @param hostName the hostname where the event ocurred
     * @param eventTime the time at which the event occurred
     * @param userTimeId the timeId of the event
     */
    public AdEvent newInstance(AdAction adAction, String hostName, Date eventTime, Integer userTimeId) {
        AdEvent adEvent = new AdEvent();
        adEvent.setAdAction(adAction);
        adEvent.setHost(hostName);
        adEvent.populateEventTime(eventTime,userTimeId);
        return adEvent;
    }
}

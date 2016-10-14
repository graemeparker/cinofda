package com.adfonic.tracker;

import java.util.Date;
import com.adfonic.adserver.Click;
import com.adfonic.adserver.Impression;

public interface ClickService {
    /**
     * Track a click record.  This method persists the click record, as long
     * as it hasn't been recorded already.
     * @param impression the original impression
     * @param applicationIdForInstallTracking optional application ID of the campaign
     * (should be null if install tracking is not required or desired).  When supplied,
     * and when the impression's tracking identifier has been supplied, provisions
     * for install tracking will be made
     * @param creationTime the time at which the click occurred
     * @param expireTime the time at which point the click should expire
     * @param ipAddress the IP address from which the click originated
     * @param userAgentHeader the User-Agent header from which the click originated
     * @return true if this was the first click and the event should be logged, or false otherwise
     */
    boolean trackClick(Impression impression, String applicationIdForInstallTracking, Date creationTime, Date expireTime, String ipAddress, String userAgentHeader);

    /** Look up a click record by its external ID */
    Click getClickByExternalID(String externalID);

    /**
     * Look up an install trackable click by application ID and device identifier
     * @param appId the campaign's applicationID
     * @param deviceIdentifierTypeId the id of the device identifier type
     * @param deviceIdentifier the device identifier
     * @return the Click if found, otherwise null
     */
    Click getClickByAppIdAndDeviceIdentifier(String appId, long deviceIdentifierTypeId, String deviceIdentifier);

    /**
     * Look up a click by pending authenticated install
     */
    Click getClick(PendingAuthenticatedInstall pendingAuthenticatedInstall);
    
    /**
     * Look up a click by pending conversion
     */
    Click getClick(PendingConversion pendingConversion);
    
    /**
     * Look up a click by pending install
     */
    Click getClick(PendingInstall pendingInstall);
    
    /**
     * Look up a click by pending video view
     */
    Click getClick(PendingVideoView pendingVideoView);

    /**
     * Load and populate the deviceIdentifiers field on the given Click object
     */
    void loadDeviceIdentifiers(Click click);
}

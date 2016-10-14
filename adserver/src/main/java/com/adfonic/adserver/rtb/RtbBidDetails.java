package com.adfonic.adserver.rtb;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.adfonic.adserver.BidDetails;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;

public class RtbBidDetails implements BidDetails {

    static final String IP_ADDRESS = "ip";
    static final String DISPLAY_TYPE_SYSTEM_NAME = "dt";
    static final String PLATFORM_ID = "p";
    static final String IMPRESSION = "imp";
    static final String PROXIED_DESTINATION = "pd";

    private final String ipAddress;
    private final String displayTypeSystemName;
    private final Long platformId;
    private final Impression impression;
    private TargetingContext bidTimeTargetingContext;
    private ProxiedDestination proxiedDestination;

    /** Construct at bid time */
    public RtbBidDetails(TargetingContext context, Impression impression, DisplayTypeDto displayType, ProxiedDestination proxiedDestination) {
        this.bidTimeTargetingContext = context;
        this.ipAddress = context.getAttribute(Parameters.IP);
        this.displayTypeSystemName = displayType.getSystemName();
        this.impression = impression;
        this.proxiedDestination = proxiedDestination;

        // We need to cache the PlatformDto id so that we can set PlatformDto in the context
        // again at win notice time.
        PlatformDto platform = context.getAttribute(TargetingContext.PLATFORM);
        this.platformId = platform == null ? null : platform.getId();
    }

    /** Reconstruct at cache get time */
    RtbBidDetails(String ipAddress, String displayTypeSystemName, Long platformId, Impression impression, ProxiedDestination proxiedDestination) {
        this.ipAddress = ipAddress;
        this.displayTypeSystemName = displayTypeSystemName;
        this.platformId = platformId;
        this.impression = impression;
        this.proxiedDestination = proxiedDestination;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    public String getDisplayTypeSystemName() {
        return displayTypeSystemName;
    }

    public Long getPlatformId() {
        return platformId;
    }

    @Override
    public Impression getImpression() {
        return impression;
    }

    @Override
    public TargetingContext getBidTimeTargetingContext() {
        return bidTimeTargetingContext;
    }

    /**
     * Convert this object to a serializable Map.  We do this in order to
     * avoid deserialization errors in case we ever update this class.
     * @return HashMap since that's Serializable itself
     */
    public HashMap<String, Serializable> toMap(KryoManager kryoManager) {
        HashMap<String, Serializable> map = new HashMap<String, Serializable>();
        map.put(IP_ADDRESS, ipAddress);
        map.put(DISPLAY_TYPE_SYSTEM_NAME, displayTypeSystemName);
        if (platformId != null) {
            map.put(PLATFORM_ID, platformId);
        }

        map.put(IMPRESSION, kryoManager.writeObject(impression));

        if (proxiedDestination != null) {
            map.put(PROXIED_DESTINATION, proxiedDestination);
        }

        return map;
    }

    /**
     * Reconstruct a BidDetails object from a serialized map, i.e. if we're
     * loading it from cache.  We do it this way to avoid deserialization errors.
     */
    public static RtbBidDetails fromMap(final Map<String, Serializable> map, KryoManager kryoManager) {
        return new RtbBidDetails((String) map.get(IP_ADDRESS), (String) map.get(DISPLAY_TYPE_SYSTEM_NAME), (Long) map.get(PLATFORM_ID), kryoManager.readObject(
                (byte[]) map.get(IMPRESSION), Impression.class), (ProxiedDestination) map.get(PROXIED_DESTINATION));
    }

    public ProxiedDestination getProxiedDestination() {
        return proxiedDestination;
    }

}

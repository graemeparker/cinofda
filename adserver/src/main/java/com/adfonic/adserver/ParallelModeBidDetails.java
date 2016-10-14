package com.adfonic.adserver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ParallelModeBidDetails implements BidDetails {

    private final String ipAddress;
    private final Impression impression;
    private TargetingContext bidTimeTargetingContext;

    /** Construct at bid time */
    public ParallelModeBidDetails(TargetingContext context, Impression impression) {
        this.bidTimeTargetingContext = context;
        this.ipAddress = context.getAttribute(Parameters.IP);
        this.impression = impression;
    }

    /** Reconstruct at cache get time */
    ParallelModeBidDetails(String ipAddress, Impression impression) {
        this.ipAddress = ipAddress;
        this.impression = impression;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
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
        map.put("ip", ipAddress);
        map.put("imp", kryoManager.writeObject(impression));
        return map;
    }

    /**
     * Reconstruct a BidDetails object from a serialized map, i.e. if we're
     * loading it from cache.  We do it this way to avoid deserialization errors.
     */
    public static ParallelModeBidDetails fromMap(final Map<String, Serializable> map, KryoManager kryoManager) {
        return new ParallelModeBidDetails((String) map.get("ip"), kryoManager.readObject((byte[]) map.get("imp"), Impression.class));
    }
}

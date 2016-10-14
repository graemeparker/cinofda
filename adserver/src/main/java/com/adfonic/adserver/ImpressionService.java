package com.adfonic.adserver;

public interface ImpressionService {

    Impression getImpression(String externalID);

    boolean removeImpression(String externalID);

    void saveImpression(Impression impression);

    Impression getStaticImpression(long adSpaceId, String staticImpressionId);

    void saveStaticImpression(long adSpaceId, String staticImpressionId, Impression impression);

    /**
     * Track a beacon having been loaded for a given impression.  This method
     * persists the beacon record, as long as it hasn't been recorded already.
     * @param impression the original impression
     * @return true if this was the first beacon request and the event should be logged, or false otherwise
     */
    boolean trackBeacon(Impression impression);
}
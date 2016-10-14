package com.adfonic.adserver.impl.icache;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.impl.AbstractImpressionService;
import com.adfonic.cache.CacheManager;

public class NamespaceAwareImpressionService extends AbstractImpressionService {

    private static final String IMPRESSION_CACHE_NAME = "Impression";
    private static final String STATIC_IMPRESSION_CACHE_NAME = "StaticImpression";
    private static final String BEACON_CACHE_NAME = "Beacon";

    private final CacheManager cacheManager;
    private final KryoManager kryoManager;

    public NamespaceAwareImpressionService(CacheManager cacheManager, KryoManager kryoManager) {
        this.cacheManager = cacheManager;
        this.kryoManager = kryoManager;
    }

    @Override
    protected Impression doGetImpression(String externalID) {
        byte[] value = cacheManager.get(externalID, IMPRESSION_CACHE_NAME, byte[].class);
        return value == null ? null : kryoManager.readObject(value, Impression.class);
    }

    @Override
    protected void doSaveImpression(Impression impression, String externalID) {
        cacheManager.set(externalID, kryoManager.writeObject(impression), IMPRESSION_CACHE_NAME, getImpressionTtlSeconds());
    }

    private static String makeStaticImpressionKey(long adSpaceId, String staticImpressionId) {
        return adSpaceId + "-" + staticImpressionId;
    }

    @Override
    public Impression getStaticImpression(long adSpaceId, String staticImpressionId) {
        // generate a compound key comprising AdSpace.id + staticImpressionId
        String compoundKey = makeStaticImpressionKey(adSpaceId, staticImpressionId);
        // Grab the Impression.externalID from the static impression cache
        String impressionExternalID = cacheManager.get(compoundKey, STATIC_IMPRESSION_CACHE_NAME, String.class);
        if (impressionExternalID == null) {
            return null;
        } else {
            // Grab the Impression from the impression cache
            // NOTE: Don't be tempted to call doGetImpression here.  We need to let the
            // superclass method handle re-populating the Impression.
            return getImpression(impressionExternalID);
        }
    }

    @Override
    public void saveStaticImpression(long adSpaceId, String staticImpressionId, Impression impression) {
        // generate a compound key comprising AdSpace.id + staticImpressionId
        String compoundKey = makeStaticImpressionKey(adSpaceId, staticImpressionId);
        // Save the Impression.externalID mapping in the static impression cache
        cacheManager.set(compoundKey, impression.getExternalID(), STATIC_IMPRESSION_CACHE_NAME, getImpressionTtlSeconds());
    }

    /** {@inheritDoc} */
    @Override
    public boolean trackBeacon(Impression impression) {
        String key = impression.getExternalID();
        // This is totally NOT atomic, but we're doing away with memcached soon anyway...
        Boolean tracked = cacheManager.get(key, BEACON_CACHE_NAME, Boolean.class);
        if (tracked != null) {
            return false;
        }
        cacheManager.set(key, Boolean.TRUE, BEACON_CACHE_NAME, getImpressionTtlSeconds());
        return true;
    }

    @Override
    protected boolean doRemoveImpression(String externalID) {
        return cacheManager.remove(externalID, IMPRESSION_CACHE_NAME);
    }
}

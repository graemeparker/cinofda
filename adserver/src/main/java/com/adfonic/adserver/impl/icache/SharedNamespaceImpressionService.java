package com.adfonic.adserver.impl.icache;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.impl.AbstractImpressionService;
import com.adfonic.cache.CacheManager;

/**
 * ImpressionService implementation that can work properly on a shared namespace,
 * i.e. memcached or an in-memory cache like ehcache.
 */
public class SharedNamespaceImpressionService extends AbstractImpressionService {

    private static final String IMPRESSION_KEY_PREFIX = "i.";
    private static final String BEACON_KEY_PREFIX = "B.";
    private static final String STATIC_IMPRESSION_KEY_PREFIX = "si.";

    private final CacheManager cacheManager;
    private final KryoManager kryoManager;

    public SharedNamespaceImpressionService(CacheManager cacheManager, KryoManager kryoManager) {
        this.cacheManager = cacheManager;
        this.kryoManager = kryoManager;
    }

    @Override
    protected Impression doGetImpression(String externalID) {
        byte[] value = cacheManager.get(makeImpressionKey(externalID), byte[].class);
        return value == null ? null : kryoManager.readObject(value, Impression.class);
    }

    @Override
    protected void doSaveImpression(Impression impression, String externalID) {
        cacheManager.set(makeImpressionKey(externalID), kryoManager.writeObject(impression), getImpressionTtlSeconds());
    }

    @Override
    protected boolean doRemoveImpression(String externalID) {
        boolean result = cacheManager.remove(makeImpressionKey(externalID));
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Impression getStaticImpression(long adSpaceId, String staticImpressionId) {
        // generate a compound key comprising AdSpace.id + staticImpressionId
        String compoundKey = makeStaticImpressionKey(adSpaceId, staticImpressionId);
        // Grab the Impression.externalID from the static impression cache
        String impressionExternalID = cacheManager.get(compoundKey, String.class);
        if (impressionExternalID == null) {
            return null;
        } else {
            // Grab the Impression from the impression cache
            // NOTE: Don't be tempted to call doGetImpression here.  We need to let the
            // superclass method handle re-populating the Impression.
            return getImpression(impressionExternalID);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void saveStaticImpression(long adSpaceId, String staticImpressionId, Impression impression) {
        // generate a compound key comprising AdSpace.id + staticImpressionId
        String compoundKey = makeStaticImpressionKey(adSpaceId, staticImpressionId);
        // Save the Impression.externalID mapping in the static impression cache
        cacheManager.set(compoundKey, impression.getExternalID(), getImpressionTtlSeconds());
    }

    /** {@inheritDoc} */
    @Override
    public boolean trackBeacon(Impression impression) {
        String key = makeBeaconKey(impression);
        // This is totally NOT atomic, but we're doing away with memcached soon anyway...
        // TODO: CAS (compare and swap)
        Boolean tracked = cacheManager.get(key, Boolean.class);
        if (tracked != null) {
            return false;
        }
        cacheManager.set(key, Boolean.TRUE, getImpressionTtlSeconds());
        return true;
    }

    private static String makeImpressionKey(String externalID) {
        return IMPRESSION_KEY_PREFIX + externalID;
    }

    private static String makeBeaconKey(Impression impression) {
        return BEACON_KEY_PREFIX + impression.getExternalID();
    }

    private static String makeStaticImpressionKey(long adSpaceId, String staticImpressionId) {
        return STATIC_IMPRESSION_KEY_PREFIX + adSpaceId + "-" + staticImpressionId;
    }
}

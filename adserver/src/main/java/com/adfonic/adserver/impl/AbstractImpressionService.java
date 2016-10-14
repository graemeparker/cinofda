package com.adfonic.adserver.impl;

import org.springframework.beans.factory.annotation.Value;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;

/** Abstract base class for ImpressionService implementations */
public abstract class AbstractImpressionService implements ImpressionService {

    @Value("${cache.impression.ttlSeconds}")
    private int impressionTtlSeconds;

    /** {@inheritDoc} */
    @Override
    public Impression getImpression(String externalID) {
        Impression impression = doGetImpression(externalID);
        if (impression != null) {
            // Restore the externalID, since it was nulled out on the way into
            // the cache.  See saveImpression() below for an explanation...
            impression.setExternalID(externalID);
        }
        return impression;
    }

    @Override
    public boolean removeImpression(String externalID) {
        if (externalID != null) {
            return doRemoveImpression(externalID);
        }
        return false;
    }

    protected int getImpressionTtlSeconds() {
        return impressionTtlSeconds;
    }

    /** {@inheritDoc} */
    @Override
    public void saveImpression(Impression impression) {
        // Major optimization from the mind of Tiemen...there's no good reason to
        // store the externalID in cache, since it's the cache key.  In other words,
        // we can null the field out now, and then when getImpression() is called,
        // we can "restore" the externalID on the way out.
        String externalID = impression.getExternalID();
        impression.setExternalID(null);

        doSaveImpression(impression, externalID);

        // We've stored it in cache without the externalID, and it'll get restored
        // at getImpression() time...but since the code calling this method may
        // depend on the externalID *now*, we need to restore it.  This is
        // absolutely key, since the ad gets rendered after this call.
        impression.setExternalID(externalID);
    }

    protected abstract Impression doGetImpression(String externalID);

    protected abstract void doSaveImpression(Impression impression, String externalID);

    protected abstract boolean doRemoveImpression(String externalID);
}

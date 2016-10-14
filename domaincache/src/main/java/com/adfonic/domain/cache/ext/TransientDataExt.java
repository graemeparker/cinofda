package com.adfonic.domain.cache.ext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TransientDataExt {

    // This is another optimization for CreativeEligibilityUpdater
    @Deprecated
    public final transient Set<Long> transientCreativeIdsForEC = new HashSet<Long>();

    // This is something we populate while loading, but it doesn't get
    // serialized.  Adserver doesn't need it.  But CreativeEligibilityUpdater
    // does need it to do faster eligibility maintenance checks.  That puppy
    // doesn't want to have to deal with merging the by-priority maps.
    @Deprecated
    public final transient Map<Long, Set<Long>> transientEligibleCreativeIdsByAdSpaceIdForEC = new HashMap<Long, Set<Long>>();

    /*
     * We pass lots of Ids(Long) in cache. In memory when we say Long(10) and again Long(10), that means they are different
     * Objects at different location taking 16bytes in Heap +  a reference.
     * If we reuse Long(10) everytime we countr a long field with value 10 then we can save lot of space.
     * 
     */
    private static final transient Map<Long, Long> transientMapForIds = new HashMap<Long, Long>();

    private static final transient Map<Integer, Integer> transientMapForECPMValue = new HashMap<Integer, Integer>();

    public static synchronized Long getSingltonId(Long id) {
        Long sigletonId = transientMapForIds.get(id);
        if (sigletonId == null) {
            sigletonId = id;
            transientMapForIds.put(id, sigletonId);
        }
        return sigletonId;
    }

    public static synchronized Integer getSingletonInteger(int key) {
        Integer integerValue = transientMapForECPMValue.get(key);
        if (integerValue == null) {
            integerValue = key;
            transientMapForECPMValue.put(key, integerValue);
        }
        return integerValue;
    }
}

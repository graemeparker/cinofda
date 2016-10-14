package com.adfonic.retargeting.redis;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spatial4j.core.io.GeohashUtils;

public class GeoAudienceRedisReader implements GeoAudienceReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoAudienceRedisReader.class);
    private final int minKeyLen;
    private final int maxKeyLen;

    private final ThreadLocalClientFactory factory;

    public GeoAudienceRedisReader(ThreadLocalClientFactory factory, int minKeyLen, int maxKeyLen) {
        this.factory = factory;
        this.minKeyLen = minKeyLen;
        this.maxKeyLen = maxKeyLen;
    }

    public Set<Long> getAudiencesUnion(String... geoHashes) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Query audiences for: {}", geoHashes);
        }

        // when redis restarts it time out with java.net.SocketTimeoutException, which is java.io.IOException
        try {

            Set<String> smembers = factory.getJedis().sunion(geoHashes);

            if (smembers == null) {
                LOGGER.debug("no value for key: {}", geoHashes);
                return null;
            }
            return toLongSet(smembers);
        } catch (Exception re) {
            LOGGER.error("invalidating connection and rethrowing: ");
            factory.invalidateConnection(re);
            throw re;
        }
    }

    //    public Set<Long> getAudiences(String geoHash) {
    //        
    //        // when redis restarts it time out with java.net.SocketTimeoutException, which is java.io.IOException
    //        try {
    //            Set<String> smembers = factory.getJedis().smembers(geoHash);
    //            
    //            if (smembers == null) {
    //                LOGGER.debug("no value for key: {}", geoHash);
    //                return null;
    //            }
    //            return toLongSet(smembers);
    //        } catch (Exception re) {
    //            LOGGER.error("invalidating connection and rethrowing: ");
    //            factory.invalidateConnection(re);
    //            throw re;
    //        }
    //    }

    private Set<Long> toLongSet(Set<String> set) {

        Set<Long> audiences = new HashSet<>();
        for (String s : set) {
            long audienceId = Long.parseLong(s);
            audiences.add(audienceId);
        }
        return audiences;
    }

    public String encodeLatLon(double lat, double lng) {
        String gHash = GeohashUtils.encodeLatLon(lat, lng, maxKeyLen);
        return gHash;
    }

    public String[] produceKeys(String string) {

        int l = string.length() - minKeyLen;
        if (l <= 0) {
            return new String[] { string };
        }

        int j = 0;
        String[] a = new String[l + 1];
        for (int i = minKeyLen; i <= string.length(); i++) {
            a[j] = string.substring(0, i);
            j++;
        }
        return a;
    }

    @Override
    public Set<Long> getAudiences(double lat, double lng) {
        String gHash = encodeLatLon(lat, lng);
        String[] keys = produceKeys(gHash);
        Set<Long> audiences = getAudiencesUnion(keys);
        return audiences;
    }

}

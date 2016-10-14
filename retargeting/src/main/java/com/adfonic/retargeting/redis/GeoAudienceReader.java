package com.adfonic.retargeting.redis;

import java.util.Set;

public interface GeoAudienceReader {

    Set<Long> getAudiences(double lat, double lng);

}

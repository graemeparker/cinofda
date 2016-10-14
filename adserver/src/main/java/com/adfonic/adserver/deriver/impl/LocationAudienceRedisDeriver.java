package com.adfonic.adserver.deriver.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.exceptions.JedisConnectionException;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.geo.Coordinates;
import com.adfonic.retargeting.redis.GeoAudienceReader;
import com.adfonic.util.stats.CounterManager;

@Component
public class LocationAudienceRedisDeriver extends BaseDeriver {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final GeoAudienceReader locationReader;

    private final CounterManager counterManager;

    @Autowired
    public LocationAudienceRedisDeriver(DeriverManager deriverManager, GeoAudienceReader locationReader, CounterManager counterManager) {
        super(deriverManager, TargetingContext.LOCATION_AUDIENCES);
        this.locationReader = locationReader;
        this.counterManager = counterManager;
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.LOCATION_AUDIENCES.equals(attribute)) {
            return null;
        }

        Coordinates coordinates = context.getAttribute(TargetingContext.COORDINATES);
        if (coordinates != null) {
            try {
                counterManager.incrementCounter(AsCounter.GeoRedisCall);
                return locationReader.getAudiences(coordinates.getLatitude(), coordinates.getLongitude());
            } catch (JedisConnectionException jcx) {
                // Redis connection exception - log only short warning
                logger.log(Level.SEVERE, "Geo Redis connection problem: " + jcx);
                counterManager.incrementCounter(AsCounter.GeoRedisError);
            } catch (Exception e) {
                // Not a Redis connection problem so rather log whole exception stacktrace
                logger.log(Level.WARNING, "Location Audience failure", e);
                counterManager.incrementCounter(AsCounter.GeoRedisError);
            }
        }
        return null;
    }

    //
}

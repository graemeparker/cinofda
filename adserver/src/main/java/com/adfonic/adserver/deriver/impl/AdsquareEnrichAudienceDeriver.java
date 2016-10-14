package com.adfonic.adserver.deriver.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.geo.Coordinates;
import com.adfonic.util.stats.CounterManager;
import com.adfonic.util.stats.FreqLogr;
import com.byyd.adsquare.v2.AdsqrEnrichQueryRequest;
import com.byyd.adsquare.v2.AdsqrEnrichQueryResponse;
import com.byyd.adsquare.v2.AmpSupplySidePlatform;
import com.byyd.adsquare.v2.EnrichmentApiClient;

@Component
public class AdsquareEnrichAudienceDeriver extends BaseDeriver {

    private static final AdsquareEnrichAudiences EMPTY = new AdsquareEnrichAudiences(null, null);

    private final EnrichmentApiClient enrichClient;

    private final CounterManager counterManager;

    private List<AmpSupplySidePlatform> ssps;

    @Autowired
    public AdsquareEnrichAudienceDeriver(DeriverManager deriverManager, EnrichmentApiClient enrichClient, CounterManager counterManager) {
        super(deriverManager, TargetingContext.ADSQUARE_ENRICH_AUDIENCES);
        this.enrichClient = enrichClient;
        this.counterManager = counterManager;
    }

    @Override
    public AdsquareEnrichAudiences getAttribute(String attribute, TargetingContext context) {

        String deviceIdRaw = null;
        String deviceIdSha1 = null;
        String deviceIdMd5 = null;
        Map<Long, String> deviceIdentifiers = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
        if (deviceIdentifiers != null && !deviceIdentifiers.isEmpty()) {
            Map<String, Long> bySystemName = context.getDomainCache().getDeviceIdentifierTypeIdsBySystemName();
            deviceIdRaw = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_IFA));
            if (deviceIdRaw == null) {
                deviceIdRaw = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ADID));
            }
            // Use Hashes only when RAW is not avaliable 
            if (deviceIdRaw == null) {
                deviceIdSha1 = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_HIFA));
                if (deviceIdSha1 == null) {
                    deviceIdSha1 = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ADID_SHA1));
                    if (deviceIdSha1 == null) {
                        deviceIdSha1 = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_DPID));
                    }
                }
                deviceIdMd5 = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5));
                if (deviceIdMd5 == null) {
                    deviceIdMd5 = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ADID_MD5));
                }
            }
        }

        Coordinates coordinates = context.getAttribute(TargetingContext.COORDINATES);
        Double latitude = null;
        Double longitude = null;
        if (coordinates != null) {
            latitude = coordinates.getLatitude();
            longitude = coordinates.getLongitude();
        }

        //if (latitude != null || deviceIdRaw != null) {
        if (latitude != null) { // XXX: only lat/lon so far...
            counterManager.incrementCounter(AsCounter.AdsquareEnrichCall);
            try {
                /*
                String deviceType = null;
                Integer sspId = AdsquareWorker.getSspIdForExchange(context.getAdSpace());
                XXX: Enrich API cannot handle device Id queries yet. It allways returns 0 audiences when device id is sent in
                AdsqrEnrichQueryRequest request = new AdsqrEnrichQueryRequest(latitude, longitude, deviceIdRaw, deviceIdSha1, deviceIdMd5, deviceType, sspId);
                 */
                AdsqrEnrichQueryRequest request = new AdsqrEnrichQueryRequest(latitude, longitude, null, null, null, null, null);
                // Pull other fields out of bid request 
                ByydRequest byydRequest = context.getAttribute(TargetingContext.BYYD_REQUEST);
                request.setIpAddress(byydRequest.getDevice().getIp());

                return new AdsquareEnrichAudiences(request, enrichClient.query(request));
            } catch (Exception x) {
                counterManager.incrementCounter(AsCounter.AdsquareEnrichError);
                FreqLogr.report(x);
                return null; // null is for errors
            }
        }
        return EMPTY; // empty is for no request/response
    }

    /**
     * Caller has to know exact request we used to be able to build tracking API request later
     */
    public static class AdsquareEnrichAudiences {

        private final AdsqrEnrichQueryRequest queryRequest; //request

        private final List<Integer> audiences; // response

        public AdsquareEnrichAudiences(AdsqrEnrichQueryRequest request, AdsqrEnrichQueryResponse response) {
            this.queryRequest = request;
            this.audiences = response != null ? response.getAudiences() : Collections.EMPTY_LIST;
        }

        public AdsqrEnrichQueryRequest getQueryRequest() {
            return queryRequest;
        }

        public List<Integer> getAudiences() {
            return audiences;
        }

        @Override
        public String toString() {
            return "AdsquareEnrichment {request=" + queryRequest + ", audiences=" + audiences + "}";
        }

    }
}

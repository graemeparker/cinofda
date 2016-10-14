package com.adfonic.adserver.deriver.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.geo.Coordinates;
import com.adfonic.util.stats.CounterManager;
import com.adfonic.util.stats.FreqLogr;
import com.byyd.factual.FactualOnPremHttpClient;
import com.byyd.factual.MatchResponse;

@Component
public class FactualAudienceDeriver extends BaseDeriver {

    private final FactualOnPremHttpClient client;

    private final CounterManager counterManager;

    private static final List<MatchResponse> EMPTY = Collections.emptyList();

    @Autowired
    public FactualAudienceDeriver(DeriverManager deriverManager, FactualOnPremHttpClient client, CounterManager counterManager) {
        super(deriverManager, TargetingContext.FACTUAL_AUDIENCE_MATCHES, TargetingContext.FACTUAL_PROXIMITY_MATCHES);
        this.client = client;
        this.counterManager = counterManager;
    }

    @Override
    public List<MatchResponse> getAttribute(String attribute, TargetingContext context) {

        if (TargetingContext.FACTUAL_AUDIENCE_MATCHES.equals(attribute)) {
            return fetchAudienceAudience(context);
        } else {
            return fetchProximityAudience(context);
        }
    }

    private List<MatchResponse> fetchAudienceAudience(TargetingContext context) {

        String userId = null;
        Map<Long, String> deviceIdentifiers = context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
        if (deviceIdentifiers != null && !deviceIdentifiers.isEmpty()) {
            Map<String, Long> bySystemName = context.getDomainCache().getDeviceIdentifierTypeIdsBySystemName();
            Long publisherId = context.getAdSpace().getPublication().getPublisher().getId();
            RtbExchange exchange = RtbExchange.getByPublisherId(publisherId);
            /**
             * Factual supports only few exchanges ~ "Data Providers"
             * On the top of that, only particular id from bid request must be used for any of them
             * http://developer.factual.com/geopulse-audience-creating/ 
             */
            if (exchange == RtbExchange.Mopub) {
                // MoPub - device.dpidsha1 - which is just sha1 of adid/idfa
                userId = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_HIFA));
                if (userId == null) {
                    userId = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ADID_SHA1));
                    if (userId == null) {
                        // quite unlikely but usefull for testing when we know only hash that belongs to audience
                        userId = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_DPID));
                    }
                }
            } else if (exchange == RtbExchange.Nexage) {
                // Nexage - user.id
                ByydRequest byydRequest = context.getAttribute(TargetingContext.BYYD_REQUEST);
                ByydUser byydUser = byydRequest.getUser();
                if (byydUser != null) {
                    userId = byydUser.getUid();
                }
            } else if (exchange == RtbExchange.Smaato) {
                // Smaato - ext.udi.idfa, .ext.udi.googleadid, .ext.udi.atuid, .ext.udi.wpid
                userId = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_IFA));
                if (userId == null) {
                    userId = deviceIdentifiers.get(bySystemName.get(DeviceIdentifierType.SYSTEM_NAME_ADID));
                }
            }
        }

        if (userId != null) {
            counterManager.incrementCounter(AsCounter.FactualAudienceCall);
            try {
                return client.audience(userId);
            } catch (Exception x) {
                counterManager.incrementCounter(AsCounter.FactualAudienceError);
                FreqLogr.report(x);
                return null; // null is for error
            }
        }
        return EMPTY; // empty is for no request/response

    }

    private List<MatchResponse> fetchProximityAudience(TargetingContext context) {
        Coordinates coordinates = context.getAttribute(TargetingContext.COORDINATES);
        if (coordinates != null) {
            try {
                counterManager.incrementCounter(AsCounter.FactualProximityCall);
                return client.proximity(coordinates.getLatitude(), coordinates.getLongitude());
            } catch (Exception x) {
                counterManager.incrementCounter(AsCounter.FactualProximityError);
                FreqLogr.report(x);
                return null; // null is for error
            }
        }
        return EMPTY; // empty is for no request/response
    }
}

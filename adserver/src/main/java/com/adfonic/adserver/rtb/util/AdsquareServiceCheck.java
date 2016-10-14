package com.adfonic.adserver.rtb.util;

import com.adfonic.adserver.spring.config.AdserverStatusSpringConfig.AdServerResource;
import com.adfonic.util.status.BaseResourceCheck;
import com.adfonic.util.status.ResourceId;
import com.byyd.adsquare.v2.EnrichmentApiClient;

/**
 * 
 * @author mvanek
 *
 */
public class AdsquareServiceCheck extends BaseResourceCheck<AdServerResource> {

    private final EnrichmentApiClient enrichClient;

    public AdsquareServiceCheck(EnrichmentApiClient enrichClient) {
        this.enrichClient = enrichClient;
    }

    @Override
    public String doCheck(ResourceId<AdServerResource> resource) throws Exception {
        enrichClient.query(52.516, 13.377);
        return "query executed succesfully";

    }

}

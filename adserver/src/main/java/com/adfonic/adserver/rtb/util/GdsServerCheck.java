package com.adfonic.adserver.rtb.util;

import com.adfonic.adserver.spring.config.AdserverStatusSpringConfig.AdServerResource;
import com.adfonic.quova.QuovaClient;
import com.adfonic.util.status.BaseResourceCheck;
import com.adfonic.util.status.ResourceId;
import com.quova.data._1.Ipinfo;

/**
 * 
 * @author mvanek
 *
 */
public class GdsServerCheck extends BaseResourceCheck<AdServerResource> {

    private final QuovaClient quovaClient;

    private final String testIp;

    public GdsServerCheck(QuovaClient quovaClient, String testIp) {
        this.quovaClient = quovaClient;
        this.testIp = testIp;
    }

    @Override
    public String doCheck(ResourceId<AdServerResource> resource) throws Exception {
        Ipinfo ipinfo = this.quovaClient.getIpinfo(testIp);
        if (ipinfo != null) {
            return ipinfo.getIpAddress() + " found in GDS";
        } else {
            //null is legitimate return value from QuovaClient
            return testIp + " not in GDS";
        }

    }

}

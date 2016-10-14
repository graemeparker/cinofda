package com.adfonic.tasks.xaudit.impl;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import com.adfonic.adserver.Constant;
import com.adfonic.adserver.vhost.VhostManager;

/**
 * VhostManager is honestly disgusting pile of hedgehog's vomits
 * 
 * On AdServer, RTB bid HttpServletRequest's URL is used to build Byyd beacon URL 
 * that points back to same AdServer's Shard (geograficaly colocated with RTB exchange)
 * For example AdX we have 3 domains: adx-east-rtb.byyd.net, adx-west-rtb.byyd.net (USA) and adx-emea-rtb.byyd.net (Europe)
 * 
 * While auditing, we have no incoming http request so URLs must be configured statically, 
 * There is no low latency requirement as for RTB, so it is OK
 * 
 * Both click types are pointing back to Adserver's audit url, but they do not have to be secure (https)
 * 
 * @author mvanek
 *
 */
public class AuditVhostManager extends VhostManager {

    private final String adserverAuditBeaconUrl;

    private final String adserverAuditClickThroughUrl;

    private final String adserverAuditClickRedirectUrl;

    public AuditVhostManager(String adserverAuditBaseUrl) {
        if (!adserverAuditBaseUrl.startsWith("http:") && !adserverAuditBaseUrl.startsWith("https:")) {
            throw new IllegalArgumentException("Url must start with http: or https:");
        }
        this.adserverAuditBeaconUrl = adserverAuditBaseUrl + Constant.BEACON_URI_PATH;
        this.adserverAuditClickThroughUrl = adserverAuditBaseUrl + Constant.CLICK_THROUGH_PATH;
        this.adserverAuditClickRedirectUrl = adserverAuditBaseUrl + Constant.CLICK_REDIRECT_PATH;
    }

    @Override
    public StringBuilder getBeaconBaseUrl(HttpServletRequest request) {
        return new StringBuilder(adserverAuditBeaconUrl);
    }

    @Override
    public StringBuilder getBeaconBaseUrl(HttpServletRequest request, boolean httpsRequired) {
        if (httpsRequired) {
            if (adserverAuditBeaconUrl.startsWith("https://")) {
                // in case beacons are https by default... 
                return new StringBuilder(adserverAuditBeaconUrl);
            } else {
                return new StringBuilder("https://" + adserverAuditBeaconUrl.substring(7));
            }
        } else {
            return new StringBuilder(adserverAuditBeaconUrl);
        }
    }

    @Override
    public StringBuilder getClickRedirectBaseUrl(HttpServletRequest request) {
        return new StringBuilder(adserverAuditClickRedirectUrl);
    }

    @Override
    public StringBuilder getClickBaseUrl(HttpServletRequest request) {
        return new StringBuilder(adserverAuditClickThroughUrl);
    }

    // Not used....
    @Override
    public String getAssetBaseUrl(HttpServletRequest request) {
        throw new UnsupportedOperationException("Do not call me please");
    }

    @Override
    public void initialize() throws JAXBException {
        // ignore
    }

    @Override
    public void destroy() {
        // ignore
    }

}

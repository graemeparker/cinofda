package com.adfonic.adserver.deriver.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.quova.data._1.Ipinfo;
import com.quova.data._1.NetworkType;

/** Derive a OperatorDto domain object from the request */
@Component
public class OperatorDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(OperatorDeriver.class.getName());

    @Autowired
    public OperatorDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.OPERATOR);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.OPERATOR.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        // First see if we were passed MCC+MNC, which is an easy shortcut
        // to resolving the operator...but only bother if nettype != wifi.
        String nettype = context.getAttribute(Parameters.NETWORK_TYPE);
        // Note that this treats null/missing nettype as non-wifi
        if (!"wifi".equalsIgnoreCase(nettype)) {
            String mccmnc = context.getAttribute(Parameters.MCC_MNC);
            if (mccmnc != null && mccmnc.length() > 3) {
                OperatorDto operator = context.getDomainCache().getOperatorByMccMnc(mccmnc);
                if (operator != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Resolved OperatorDto id=" + operator.getId() + " (" + operator.getName() + "/" + operator.getCountryIsoCode() + ") by MCC+MNC: " + mccmnc);
                    }
                    return operator;
                } else {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("Unrecognized MCC+MNC: " + mccmnc);
                    }
                }
            }
        }

        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country == null) {
            // No country...not much we can do
            return null;
        }

        // Bugzilla 1187:
        // See if we can identify the operator using Quova's carrier name, if it
        // was set up as an operator alias for the given country.
        Ipinfo ipinfo = context.getAttribute(TargetingContext.QUOVA_IP_INFO);
        if (ipinfo != null) {
            NetworkType network = ipinfo.getNetwork();
            // Make sure Quova thinks the IP address is coming from a "mobile gateway"
            if ((network != null) && 
                (StringUtils.isNotBlank(network.getCarrier()))) {
                OperatorDto operator = context.getDomainCache().getOperatorByCountryAndQuovaAlias(country, network.getCarrier());
                if (operator != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Resolved OperatorDto id=" + operator.getId() + " (" + operator.getName() + "/" + operator.getCountryIsoCode() + ") by Quova alias: "
                                + network.getCarrier());
                    }
                    return operator;
                } else if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("OperatorDto not found for CountryDto id=" + country.getId() + " (" + country.getIsoCode() + "), Quova carrier=" + network.getCarrier());
                }
            }
        }

        // Fall back on resolving operator by IP + country
        String ip = context.getAttribute(Parameters.IP);
        return context.getDomainCache().getOperator(ip, country);
    }
}

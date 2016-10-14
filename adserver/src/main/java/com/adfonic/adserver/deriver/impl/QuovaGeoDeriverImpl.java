package com.adfonic.adserver.deriver.impl;

import java.math.BigDecimal;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.geo.AustrianProvince;
import com.adfonic.geo.CanadianProvince;
import com.adfonic.geo.ChineseProvince;
import com.adfonic.geo.Coordinates;
import com.adfonic.geo.Dma;
import com.adfonic.geo.GeoUtils;
import com.adfonic.geo.SimpleCoordinates;
import com.adfonic.geo.USState;
import com.adfonic.quova.QuovaClient;
import com.adfonic.util.TimeZoneUtils;
import com.adfonic.util.stats.CounterManager;
import com.adfonic.util.stats.FreqLogr;
import com.newrelic.api.agent.Trace;
import com.quova.data._1.Ipinfo;

/**
 * This implementation uses Quova GeoDirectory API to talk to the Quova
 * GeoDirectory server.
 */
@Component
public class QuovaGeoDeriverImpl extends AbstractGeoDeriver {

    private static final transient Logger LOG = Logger.getLogger(QuovaGeoDeriverImpl.class.getName());

    private final QuovaClient quovaClient;

    @Autowired
    private CounterManager counterManager;

    @Autowired
    public QuovaGeoDeriverImpl(DeriverManager deriverManager, QuovaClient quovaClient) {
        super(deriverManager, TargetingContext.QUOVA_IP_INFO);
        this.quovaClient = quovaClient;
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (TargetingContext.QUOVA_IP_INFO.equals(attribute)) {
            return deriveQuovaIpinfo(context);
        } else {
            return super.getAttribute(attribute, context);
        }
    }

    @Override
    protected ChineseProvince deriveChineseProvinceFromIp(TargetingContext context) {
        String quovaProv = generalDeriveProvinceFromIp(context, "cn");

        if (quovaProv == null)
            return null;

        try {
            //        return ChineseProvince.forName(quovaProv);
            return ChineseProvince.valueOf(quovaProv);
        } catch (IllegalArgumentException e) {
            LOG.warning("Unrecognized Province from Quova! impossible to map to ChineseProvince  province:" + quovaProv);
            return null;
        }
    }

    @Override
    protected AustrianProvince deriveAustrianProvinceFromIp(TargetingContext context) {
        String quovaProv = generalDeriveProvinceFromIp(context, "at");
        if (quovaProv == null)
            return null;

        try {
            //        return AustrianProvince.forName(quovaProv);
            return AustrianProvince.valueOf(quovaProv);
        } catch (IllegalArgumentException e) {
            LOG.warning("Unrecognized Province from Quova! impossible to map to AustrianProvince  province:" + quovaProv);
            return null;
        }

    }

    private String generalDeriveProvinceFromIp(TargetingContext context, String countryCode) {
        Ipinfo ipInfo = context.getAttribute(TargetingContext.QUOVA_IP_INFO);
        if (ipInfo == null) {
            return null;
        } else if (ipInfo.getLocation() == null) {
            return null;
        } else if (ipInfo.getLocation().getStateData() == null) {
            return null;
        } else if (ipInfo.getLocation().getCountryData() == null || !countryCode.equals(ipInfo.getLocation().getCountryData().getCountryCode())) {
            return null;
        }

        // See if Quova gave us the "state"
        String state = ipInfo.getLocation().getStateData().getStateCode();
        if (StringUtils.isNotBlank(state)) {
            try {
                // Quova values are all lowercase...need to uppercase
                return state.toUpperCase();
            } catch (Exception e) {
                LOG.warning("Unrecognized Province from Quova! country:" + countryCode + "  province:" + state);
            }
        }

        return null;
    }

    @Override
    protected String deriveSpanishProvinceFromIp(TargetingContext context) {
        return generalDeriveProvinceFromIp(context, "es");

    }

    @Trace(metricName = "Quava")
    Ipinfo deriveQuovaIpinfo(TargetingContext context) {
        String ip = context.getAttribute(Parameters.IP);
        if (ip == null) {
            return null;
        }
        counterManager.incrementCounter(AsCounter.QuovaCalls);
        try {
            Ipinfo ipinfo = quovaClient.getIpinfo(ip);
            return ipinfo;
        } catch (Exception e) {
            counterManager.incrementCounter(AsCounter.QuovaError);
            FreqLogr.report(e, "Quova Error");
            return null;
        }
    }

    @Override
    protected CountryDto deriveCountryFromIp(TargetingContext context) {
        Ipinfo ipInfo = context.getAttribute(TargetingContext.QUOVA_IP_INFO);
        if (ipInfo == null) {
            return null;
        } else if (ipInfo.getLocation() == null) {
            return null;
        } else if (ipInfo.getLocation().getCountryData() == null) {
            return null;
        }

        String countryCode = ipInfo.getLocation().getCountryData().getCountryCode();
        if (StringUtils.isNotBlank(countryCode)) {
            // Quova values are all lowercase...need to uppercase
            CountryDto country = context.getDomainCache().getCountryByIsoCode(countryCode.toUpperCase());
            if (country != null) {
                return country;
            }
            LOG.warning("Unrecognized country ISO code: " + countryCode);
        }

        return null;
    }

    @Override
    protected TimeZone deriveTimeZoneFromIp(TargetingContext context) {
        Ipinfo ipInfo = context.getAttribute(TargetingContext.QUOVA_IP_INFO);
        if (ipInfo == null) {
            return null;
        } else if (ipInfo.getLocation() == null) {
            return null;
        } else if (ipInfo.getLocation().getCityData() == null) {
            return null;
        } else if (ipInfo.getLocation().getCityData().getTimeZone() == null) {
            return null;
        }

        // Quova provides us an hours offset value (i.e. -5.0).  Just in case
        // they change what format they're using, let's just use our freeform
        // input parser to resolve the time zone.
        BigDecimal tz = ipInfo.getLocation().getCityData().getTimeZone().getValue();
        if (tz == null) {
            return null;
        } else {
            return TimeZoneUtils.getTimeZoneByOffset(tz.doubleValue());
        }
    }

    @Override
    protected Coordinates deriveCoordinatesFromIp(TargetingContext context) {
        // The superclass has already tried determining the Coordinates via
        // parameters and any other non-implementation-specific methods.
        // It's up to us here to use Quova directly to resolve Coordinates.
        Ipinfo ipInfo = context.getAttribute(TargetingContext.QUOVA_IP_INFO);
        if (ipInfo == null) {
            return null;
        } else if (ipInfo.getLocation() == null) {
            return null;
        } else if (ipInfo.getLocation().getLatitude() == null || ipInfo.getLocation().getLongitude() == null) {
            return null;
        }

        BigDecimal bdLat = ipInfo.getLocation().getLatitude().getValue();
        BigDecimal bdLon = ipInfo.getLocation().getLongitude().getValue();
        if (bdLat != null && bdLon != null) {
            double lat = bdLat.doubleValue();
            double lon = bdLon.doubleValue();
            if (GeoUtils.validateCoordinates(lat, lon)) {
                return new SimpleCoordinates(lat, lon);
            } else {
                LOG.warning("Invalid latitude/longitude from Quova: " + bdLat + "/" + bdLon);
            }
        }

        return null;
    }

    @Override
    protected String derivePostalCodeFromIp(TargetingContext context) {
        Ipinfo ipInfo = context.getAttribute(TargetingContext.QUOVA_IP_INFO);
        if (ipInfo != null && ipInfo.getLocation() != null && ipInfo.getLocation().getCityData() != null) {
            return ipInfo.getLocation().getCityData().getPostalCode();
        } else {
            return null;
        }
    }

    @Override
    protected USState deriveUSStateFromIp(TargetingContext context) {
        Ipinfo ipInfo = context.getAttribute(TargetingContext.QUOVA_IP_INFO);
        if (ipInfo == null) {
            return null;
        } else if (ipInfo.getLocation() == null) {
            return null;
        } else if (ipInfo.getLocation().getStateData() == null) {
            return null;
        } else if (ipInfo.getLocation().getCountryData() == null || !"us".equals(ipInfo.getLocation().getCountryData().getCountryCode())) {
            return null;
        }

        String state = ipInfo.getLocation().getStateData().getStateCode();
        if (StringUtils.isBlank(state)) {
            return null;
        }

        try {
            // Quova values are all lowercase...need to uppercase
            return USState.valueOf(state.toUpperCase());
        } catch (Exception e) {
            LOG.warning("Unrecognized USState: " + state);
            return null;
        }
    }

    @Override
    protected CanadianProvince deriveCanadianProvinceFromIp(TargetingContext context) {
        Ipinfo ipInfo = context.getAttribute(TargetingContext.QUOVA_IP_INFO);
        if (ipInfo == null) {
            return null;
        } else if (ipInfo.getLocation() == null) {
            return null;
        } else if (ipInfo.getLocation().getStateData() == null) {
            return null;
        } else if (ipInfo.getLocation().getCountryData() == null || !"ca".equals(ipInfo.getLocation().getCountryData().getCountryCode())) {
            return null;
        }

        // See if Quova gave us the Canadian "state"
        String state = ipInfo.getLocation().getStateData().getStateCode();
        if (StringUtils.isNotBlank(state)) {
            try {
                // Quova values are all lowercase...need to uppercase
                return CanadianProvince.valueOf(state.toUpperCase());
            } catch (Exception e) {
                LOG.warning("Unrecognized CanadianProvince from Quova: " + state);
            }
        }

        return null;
    }

    @Override
    protected Dma deriveDmaFromIp(TargetingContext context) {
        Ipinfo ipInfo = context.getAttribute(TargetingContext.QUOVA_IP_INFO);
        if (ipInfo == null) {
            return null;
        } else if (ipInfo.getLocation() == null) {
            return null;
        } else if (ipInfo.getLocation().getDma() == null) {
            return null;
        } else if (ipInfo.getLocation().getCountryData() != null && !"us".equals(ipInfo.getLocation().getCountryData().getCountryCode())) {
            return null; // SC-30
        }

        Integer dmaId = ipInfo.getLocation().getDma().getValue();
        if (dmaId == null) {
            return null;
        }

        return getDmaManager().getDmaById(dmaId.toString());
    }
}

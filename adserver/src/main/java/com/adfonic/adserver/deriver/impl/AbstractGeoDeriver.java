package com.adfonic.adserver.deriver.impl;

import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.adserver.LocationSource;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.MobileIpAddressRangeDto;
import com.adfonic.geo.AustrianPostalCode;
import com.adfonic.geo.AustrianPostalCodeManager;
import com.adfonic.geo.AustrianProvince;
import com.adfonic.geo.CanadianPostalCode;
import com.adfonic.geo.CanadianPostalCodeManager;
import com.adfonic.geo.CanadianProvince;
import com.adfonic.geo.ChinesePostalCode;
import com.adfonic.geo.ChinesePostalCodeManager;
import com.adfonic.geo.ChineseProvince;
import com.adfonic.geo.Coordinates;
import com.adfonic.geo.Dma;
import com.adfonic.geo.DmaManager;
import com.adfonic.geo.GBPostalCodeManager;
import com.adfonic.geo.GeoUtils;
import com.adfonic.geo.PostalCode;
import com.adfonic.geo.SimpleCoordinates;
import com.adfonic.geo.SpanishPostalCodeManager;
import com.adfonic.geo.USState;
import com.adfonic.geo.USZipCode;
import com.adfonic.geo.USZipCodeManager;
import com.adfonic.geo.postalcode.GeneralPostalCode;
import com.adfonic.util.TimeZoneUtils;

/**
 * Geo deriver with "pluggable" abstract methods for deriving various
 * attributes using IP-based data (i.e. Quova).
 */
public abstract class AbstractGeoDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(AbstractGeoDeriver.class.getName());

    // This is the key to a Boolean attribute in the context that indicates
    // whether or not the IP-based data attributes can be trusted as reliable
    // for this request.
    static final String IP_BASED_DATA_RELIABLE = "\\ipdr";

    private static final String FROM_PARAMETER = "param";
    private static final String FROM_IP = "ip";

    public static final String COORDINATES_FROM_PARAMETERS = TargetingContext.COORDINATES + FROM_PARAMETER;
    static final String POSTAL_CODE_FROM_IP = TargetingContext.POSTAL_CODE + FROM_IP;
    static final String US_STATE_FROM_PARAMETER = TargetingContext.US_STATE + FROM_PARAMETER;
    static final String US_STATE_FROM_IP = TargetingContext.US_STATE + FROM_IP;
    static final String COUNTRY_FROM_PARAMETER = TargetingContext.COUNTRY + FROM_PARAMETER;
    static final String COUNTRY_FROM_IP = TargetingContext.COUNTRY + FROM_IP;
    static final String DMA_FROM_PARAMETER = TargetingContext.DMA + FROM_PARAMETER;
    static final String DMA_FROM_IP = TargetingContext.DMA + FROM_IP;

    @SuppressWarnings("unchecked")
    private static <T> T[] join(T[] array1, T... array2) {
        return (T[]) ArrayUtils.addAll(array1, array2);
    }

    @Autowired
    private GBPostalCodeManager gbPostalCodeManager;

    @Autowired
    private USZipCodeManager usZipCodeManager;

    @Autowired
    private CanadianPostalCodeManager canadianPostalCodeManager;

    @Autowired
    private ChinesePostalCodeManager chinesePostalCodeManager;

    @Autowired
    private AustrianPostalCodeManager austrianPostalCodeManager;

    @Autowired
    private SpanishPostalCodeManager spanishPostalCodeManager;

    @Autowired
    private DmaManager dmaManager;
    @Value("${geotargeting.GB.postalCode.radius.mi}")
    private double gbPostalCodeRadius;

    protected AbstractGeoDeriver(DeriverManager deriverManager, final String... additionalAttributes) {
        super(deriverManager, join(additionalAttributes, TargetingContext.COUNTRY,
                TargetingContext.HAS_COORDINATES,
                TargetingContext.COORDINATES,// 
                TargetingContext.POSTAL_CODE, TargetingContext.UK_POSTAL_CODE, TargetingContext.US_ZIP_CODE,
                TargetingContext.US_STATE,
                TargetingContext.CANADIAN_POSTAL_CODE,//
                TargetingContext.CANADIAN_PROVINCE, TargetingContext.CHINESE_PROVINCE, TargetingContext.CHINESE_POSTAL_CODE, TargetingContext.AUSTRIAN_PROVINCE,
                TargetingContext.AUSTRIAN_POSTAL_CODE, TargetingContext.SPANISH_PROVINCE, TargetingContext.SPANISH_POSTAL_CODE, TargetingContext.DMA, TargetingContext.TIME_ZONE,
                // And the local attributes
                IP_BASED_DATA_RELIABLE, COORDINATES_FROM_PARAMETERS, POSTAL_CODE_FROM_IP, US_STATE_FROM_PARAMETER, US_STATE_FROM_IP, COUNTRY_FROM_PARAMETER, COUNTRY_FROM_IP,
                DMA_FROM_PARAMETER, DMA_FROM_IP));
    }

    protected final DmaManager getDmaManager() {
        return dmaManager;
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (TargetingContext.COUNTRY.equals(attribute)) {
            return deriveCountry(context);
        } else if (IP_BASED_DATA_RELIABLE.equals(attribute)) {
            return deriveIpBasedDataReliable(context);
        } else if (TargetingContext.HAS_COORDINATES.equals(attribute)) {
            return deriveGeolocatable(context);
        } else if (TargetingContext.COORDINATES.equals(attribute)) {
            return deriveCoordinates(context);
        } else if (TargetingContext.POSTAL_CODE.equals(attribute)) {
            return derivePostalCode(context);
        } else if (TargetingContext.UK_POSTAL_CODE.equals(attribute)) {
            return deriveUKPostalCode(context);
        } else if (TargetingContext.US_ZIP_CODE.equals(attribute)) {
            return deriveUSZipCode(context);
        } else if (TargetingContext.US_STATE.equals(attribute)) {
            return deriveUSState(context);
        } else if (TargetingContext.CANADIAN_POSTAL_CODE.equals(attribute)) {
            return deriveCanadianPostalCode(context);
        } else if (TargetingContext.CANADIAN_PROVINCE.equals(attribute)) {
            return deriveCanadianProvince(context);
        } else if (TargetingContext.CHINESE_PROVINCE.equals(attribute)) {
            return deriveChineseProvince(context);
        } else if (TargetingContext.CHINESE_POSTAL_CODE.equals(attribute)) {
            return deriveChinesePostalCode(context);
        } else if (TargetingContext.AUSTRIAN_PROVINCE.equals(attribute)) {
            return deriveAustrianProvince(context);
        } else if (TargetingContext.AUSTRIAN_POSTAL_CODE.equals(attribute)) {
            return deriveAustrianPostalCode(context);
        } else if (TargetingContext.SPANISH_PROVINCE.equals(attribute)) {
            return deriveSpanishProvince(context);
        } else if (TargetingContext.SPANISH_POSTAL_CODE.equals(attribute)) {
            return deriveSpanishPostalCode(context);
        } else if (TargetingContext.DMA.equals(attribute)) {
            return deriveDma(context);
        } else if (TargetingContext.TIME_ZONE.equals(attribute)) {
            return deriveTimeZone(context);
        } else if (COORDINATES_FROM_PARAMETERS.equals(attribute)) {
            return deriveCoordinatesFromParameters(context);
        } else if (POSTAL_CODE_FROM_IP.equals(attribute)) {
            return derivePostalCodeFromIp(context);
        } else if (US_STATE_FROM_PARAMETER.equals(attribute)) {
            return deriveUSStateFromParameter(context);
        } else if (US_STATE_FROM_IP.equals(attribute)) {
            return deriveUSStateFromIp(context);
        } else if (COUNTRY_FROM_PARAMETER.equals(attribute)) {
            return deriveCountryFromParameter(context);
        } else if (COUNTRY_FROM_IP.equals(attribute)) {
            return deriveCountryFromIp(context);
        } else if (DMA_FROM_PARAMETER.equals(attribute)) {
            return deriveDmaFromParameter(context);
        } else if (DMA_FROM_IP.equals(attribute)) {
            return deriveDmaFromIp(context);
        } else {
            throw new IllegalArgumentException("Cannot derive attribute: " + attribute);
        }
    }

    private ChineseProvince deriveChineseProvince(TargetingContext context) {
        ChineseProvince chineseProvince = deriveChineseProvinceFromParameter(context);

        if (chineseProvince == null && BooleanUtils.isTrue(context.getAttribute(IP_BASED_DATA_RELIABLE, Boolean.class))) {
            // SC-30 - Only derive it for Canada
            CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
            if (country != null && !"CN".equals(country.getIsoCode())) {
                return null;
            }

            // We can use the IP-based data
            chineseProvince = deriveChineseProvinceFromIp(context);
        }

        if (chineseProvince == null) {
            ChinesePostalCode postalCode = context.getAttribute(TargetingContext.CHINESE_POSTAL_CODE);
            if (postalCode != null) {
                chineseProvince = postalCode.getChineseProvince();
            }
        }

        return chineseProvince;
    }

    private String deriveSpanishProvince(TargetingContext context) {
        String province = deriveSpanishProvinceFromParameter(context);

        if (province == null && BooleanUtils.isTrue(context.getAttribute(IP_BASED_DATA_RELIABLE, Boolean.class))) {
            CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
            if (country != null && !"ES".equals(country.getIsoCode())) {
                return null;
            }

            // We can use the IP-based data
            province = deriveSpanishProvinceFromIp(context);
        }

        if (province == null) {
            GeneralPostalCode postalCode = context.getAttribute(TargetingContext.SPANISH_POSTAL_CODE);
            if (postalCode != null) {
                province = postalCode.getProvince();
            }
        }

        return province;
    }

    protected abstract String deriveSpanishProvinceFromIp(TargetingContext context);

    static String deriveSpanishProvinceFromParameter(TargetingContext context) {
        String param = context.getAttribute(Parameters.STATE);
        if (StringUtils.isNotBlank(param)) {
            try {
                return param;
            } catch (Exception e) {

                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid value for Spanish Province " + Parameters.STATE + ": " + param);
                }
            }
        }
        return null;
    }

    protected GeneralPostalCode deriveSpanishPostalCode(TargetingContext context) {
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country != null && !"ES".equals(country.getIsoCode())) {
            return null;
        }

        // Derive postalCode, which is smart enough to determine the most
        // appropriate derivation method, either IP-based or derived using
        // explicit coordinates and what not.
        String postalCode = context.getAttribute(TargetingContext.POSTAL_CODE);
        if (StringUtils.isNotEmpty(postalCode)) {
            return spanishPostalCodeManager.get(postalCode);
        } else {
            return null;
        }
    }

    private AustrianProvince deriveAustrianProvince(TargetingContext context) {
        AustrianProvince austrianProvince = deriveAustrianProvinceFromParameter(context);

        if (austrianProvince == null && BooleanUtils.isTrue(context.getAttribute(IP_BASED_DATA_RELIABLE, Boolean.class))) {
            CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
            if (country != null && !"AT".equals(country.getIsoCode())) {
                return null;
            }

            // We can use the IP-based data
            austrianProvince = deriveAustrianProvinceFromIp(context);
        }

        if (austrianProvince == null) {
            AustrianPostalCode postalCode = context.getAttribute(TargetingContext.AUSTRIAN_POSTAL_CODE);
            if (postalCode != null) {
                austrianProvince = postalCode.getAustrianProvince();
            }
        }

        return austrianProvince;
    }

    protected abstract ChineseProvince deriveChineseProvinceFromIp(TargetingContext context);

    protected abstract AustrianProvince deriveAustrianProvinceFromIp(TargetingContext context);

    /**
     * Determine whether or not IP-based data can be used for this request.
     * Normally, IP-based data (i.e. from Quova) is the fastest, simplest
     * way to derive most geo attributes.  But if the request has supplied
     * certain attributes that would be in conflict with *some* IP-based
     * data, we need to avoid using *any* IP-based data.
     * <p/>
     * Examples would be:
     * 1. Explicit coordinates were supplied (this isn't an obvious conflict
     * per se, but it renders the IP-based data *potentially* in conflict)
     * 2. o.postalCode supplied and doesn't match the IP-based postalCode
     * 3. o.state supplied and doesn't match the IP-based state
     * 4. o.country supplied and doesn't match the IP-based country
     * 5. o.dma supplied and doesn't match the IP-based DMA
     * <p/>
     * Any of the above conditions are enough not to trust the geoip-based
     * data as accurate.  Just one attribute in conflict means we need to
     * disregard the entire set of attributes.
     */
    static Boolean deriveIpBasedDataReliable(TargetingContext context) {
        // 1. Explicit coordinates supplied
        if (context.getAttribute(COORDINATES_FROM_PARAMETERS) != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("IP-based data not reliable due to: coordinates override");
            }
            return false;
        }

        // 2. o.postalCode supplied and doesn't match the IP-based postalCode
        String postalCode = context.getAttribute(Parameters.POSTAL_CODE);
        if (StringUtils.isNotBlank(postalCode) && !postalCode.equals(context.getAttribute(POSTAL_CODE_FROM_IP))) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("IP-based data not reliable due to: postal code override");
            }
            return false;
        }

        // 3. o.state supplied and doesn't match the IP-based state
        USState state = context.getAttribute(US_STATE_FROM_PARAMETER);
        if (state != null && !state.equals(context.getAttribute(US_STATE_FROM_IP))) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("IP-based data not reliable due to: US state override");
            }
            return false;
        }

        // 4. o.country supplied and doesn't match the IP-based country
        CountryDto country = context.getAttribute(COUNTRY_FROM_PARAMETER);
        if (country != null && !country.equals(context.getAttribute(COUNTRY_FROM_IP))) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("IP-based data not reliable due to: country override");
            }
            return false;
        }

        // 5. o.dma supplied and doesn't match the IP-based DMA
        Dma dma = context.getAttribute(DMA_FROM_PARAMETER);
        if (dma != null && !dma.equals(context.getAttribute(DMA_FROM_IP))) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("IP-based data not reliable due to: DMA override");
            }
            return false;
        }

        // All good...the IP-based data can be considered reliable
        return true;
    }

    CountryDto deriveCountry(TargetingContext context) {
        CountryDto country = context.getAttribute(COUNTRY_FROM_PARAMETER);
        if (country != null) {
            return country;
        }

        // Always fall back on IP-based logic for country derivation.
        // We don't even bother checking IP_BASED_DATA_RELIABLE in this case,
        // since even if there's a conflict, we don't have a geolocation service
        // capable of translating coordinates into country.

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Country not specified, deriving using IP address");
        }
        return deriveCountryFromIp(context);
    }

    /**
     * Derive whether or not the traffic is geolocatable
     */
    static Boolean deriveGeolocatable(TargetingContext context) {
        // For now we rely on the Coordinates deriver logic (below) as a way of
        // enforcing locatability rules.  If the coordinates deriver didn't bail
        // and was able to locate the end user, then they're geolocatable.
        // One example of a reason why coordinate derivation would return null is
        // if the user is on a mobile network instead of wifi.
        return context.getAttribute(TargetingContext.COORDINATES) != null;
    }

    /**
     * Derive coordinates, applying business rules for locatability
     */
    Coordinates deriveCoordinates(TargetingContext context) {
        Coordinates coordinates = context.getAttribute(COORDINATES_FROM_PARAMETERS);
        if (coordinates != null) {
            context.setAttribute(TargetingContext.LOCATION_SOURCE, LocationSource.EXPLICIT);
            return coordinates;
        }

        // A specific location wasn't passed to us via parameters.  Before we can
        // fall back on an IP-based lookup, we need to make sure the IP address
        // is non-operator (wifi).
        MobileIpAddressRangeDto mobileIpAddressRange = context.getAttribute(TargetingContext.MOBILE_IP_ADDRESS_RANGE);
        if (mobileIpAddressRange != null && mobileIpAddressRange.getOperatorId() != null) {
            // It's coming from an operator...can't derive coordinates for this request
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Can't derive coordinates for mobile request");
            }
            return null;
        }

        // It's non-operator (wifi), so we're ok to derive from IP
        coordinates = deriveCoordinatesFromIp(context);
        if (coordinates != null) {
            context.setAttribute(TargetingContext.LOCATION_SOURCE, LocationSource.DERIVED);
        }
        return coordinates;
    }

    String derivePostalCode(TargetingContext context) {
        // First see if the publisher passed it explicitly
        String postalCode = context.getAttribute(Parameters.POSTAL_CODE);
        if (StringUtils.isNotBlank(postalCode)) {
            return postalCode;
        }

        if (BooleanUtils.isTrue(context.getAttribute(IP_BASED_DATA_RELIABLE, Boolean.class))) {
            // We can use the IP-based data
            return context.getAttribute(POSTAL_CODE_FROM_IP);
        }

        // Attempt to derive the from coordinates, the details of which
        // depend largely on which country we're dealing with.
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country == null) {
            // This should never happen, but you never know
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Can't derive postal code using coordinates when country is unknown");
            }
            return null;
        }

        Coordinates coordinates = context.getAttribute(TargetingContext.COORDINATES);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Using explicit coordinates (" + coordinates + "), deriving postal code for country=" + country.getIsoCode());
        }
        if ("US".equals(country.getIsoCode())) {
            // Find the nearest US zip code
            USZipCode usZipCode = usZipCodeManager.getNearest(coordinates);
            if (usZipCode != null) {
                // Save it in case anybody it subsequently needs to be derived
                context.setAttribute(TargetingContext.US_ZIP_CODE, usZipCode);
                return usZipCode.getZip();
            }
        } else if ("GB".equals(country.getIsoCode())) {
            // Find the nearest GB postal code
            PostalCode ukPostalCode = gbPostalCodeManager.getNearest(coordinates);
            // Make sure it's within the configured maximum radius
            if (ukPostalCode != null && GeoUtils.distanceBetween(coordinates, ukPostalCode) <= gbPostalCodeRadius) {
                // Save it in case anybody it subsequently needs to be derived
                context.setAttribute(TargetingContext.UK_POSTAL_CODE, ukPostalCode);
                return ukPostalCode.getPostalCode().toLowerCase();
            }
        } else if ("CA".equals(country.getIsoCode())) {
            CanadianPostalCode caPostalCode = canadianPostalCodeManager.getNearest(coordinates);
            if (caPostalCode != null) {
                // Save it in case anybody it subsequently needs to be derived
                context.setAttribute(TargetingContext.CANADIAN_POSTAL_CODE, caPostalCode);
                return caPostalCode.getPostalCode().toLowerCase();
            }
        } else if ("CN".equals(country.getIsoCode())) {
            ChinesePostalCode cnPostalCode = chinesePostalCodeManager.getNearest(coordinates);
            if (cnPostalCode != null) {
                // Save it in case anybody it subsequently needs to be derived
                context.setAttribute(TargetingContext.CHINESE_POSTAL_CODE, cnPostalCode);
                return cnPostalCode.getPostalCode().toLowerCase();
            }

        } else if ("AT".equals(country.getIsoCode())) {
            AustrianPostalCode atPostalCode = austrianPostalCodeManager.getNearest(coordinates);
            if (atPostalCode != null) {
                // Save it in case anybody it subsequently needs to be derived
                context.setAttribute(TargetingContext.AUSTRIAN_POSTAL_CODE, atPostalCode);
                return atPostalCode.getPostalCode().toLowerCase();
            }

        } else if ("ES".equals(country.getIsoCode())) {
            GeneralPostalCode esPostalCode = spanishPostalCodeManager.getNearest(coordinates);
            if (esPostalCode != null) {
                // Save it in case anybody it subsequently needs to be derived
                context.setAttribute(TargetingContext.SPANISH_POSTAL_CODE, esPostalCode);
                return esPostalCode.getPostalCode();
            }
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("No way to derive postal code using explicit coordinates for country=" + country.getIsoCode());
            }
        }

        return null;
    }

    PostalCode deriveUKPostalCode(TargetingContext context) {
        // SC-30 - Only derive it for the UK
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country != null && !"GB".equals(country.getIsoCode())) {
            return null;
        }

        // Derive postalCode, which is smart enough to determine the most
        // appropriate derivation method, either IP-based or derived using
        // explicit coordinates and what not.
        String postalCode = context.getAttribute(TargetingContext.POSTAL_CODE);
        if (StringUtils.isNotEmpty(postalCode)) {
            // Quova values are all lowercase...need to uppercase
            return gbPostalCodeManager.get(postalCode.toUpperCase());
        } else {
            return null;
        }
    }

    USZipCode deriveUSZipCode(TargetingContext context) {
        // SC-30 - Only derive it for the US
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country != null && !"US".equals(country.getIsoCode())) {
            return null;
        }

        // Derive postalCode, which is smart enough to determine the most
        // appropriate derivation method, either IP-based or derived using
        // explicit coordinates and what not.
        String postalCode = context.getAttribute(TargetingContext.POSTAL_CODE);
        if (StringUtils.isNotEmpty(postalCode)) {
            return usZipCodeManager.get(postalCode);
        } else {
            return null;
        }
    }

    CanadianPostalCode deriveCanadianPostalCode(TargetingContext context) {
        // SC-30 - Only derive it for Canada
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country != null && !"CA".equals(country.getIsoCode())) {
            return null;
        }

        // Derive postalCode, which is smart enough to determine the most
        // appropriate derivation method, either IP-based or derived using
        // explicit coordinates and what not.
        String postalCode = context.getAttribute(TargetingContext.POSTAL_CODE);
        if (StringUtils.isNotEmpty(postalCode)) {
            return canadianPostalCodeManager.get(postalCode);
        } else {
            return null;
        }
    }

    protected ChinesePostalCode deriveChinesePostalCode(TargetingContext context) {
        // SC-30 - Only derive it for Canada
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country != null && !"CN".equals(country.getIsoCode())) {
            return null;
        }

        // Derive postalCode, which is smart enough to determine the most
        // appropriate derivation method, either IP-based or derived using
        // explicit coordinates and what not.
        String postalCode = context.getAttribute(TargetingContext.POSTAL_CODE);
        if (StringUtils.isNotEmpty(postalCode)) {
            ChinesePostalCode res = chinesePostalCodeManager.get(postalCode);
            if (res != null) {
                return res;
            }
            Coordinates coordinates = context.getAttribute(TargetingContext.COORDINATES);

            if (coordinates == null) {
                return null;
            }
            return chinesePostalCodeManager.getNearest(coordinates);

        } else {
            return null;
        }
    }

    protected AustrianPostalCode deriveAustrianPostalCode(TargetingContext context) {
        // SC-30 - Only derive it for Canada
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country != null && !"AT".equals(country.getIsoCode())) {
            return null;
        }

        // Derive postalCode, which is smart enough to determine the most
        // appropriate derivation method, either IP-based or derived using
        // explicit coordinates and what not.
        String postalCode = context.getAttribute(TargetingContext.POSTAL_CODE);
        if (StringUtils.isNotBlank(postalCode)) {
            AustrianPostalCode res = austrianPostalCodeManager.get(postalCode);
            if (res != null) {
                return res;
            }
            Coordinates coordinates = context.getAttribute(TargetingContext.COORDINATES);

            if (coordinates == null) {
                return null;
            }
            return austrianPostalCodeManager.getNearest(coordinates);
        } else {
            return null;

        }
    }

    USState deriveUSState(TargetingContext context) {
        // First check to see if there's a parameter-based override
        USState state = context.getAttribute(US_STATE_FROM_PARAMETER);

        if (state == null) {
            // SC-30 - Only derive it for the US
            CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
            if (country != null && !"US".equals(country.getIsoCode())) {
                return null;
            }

            if (BooleanUtils.isTrue(context.getAttribute(IP_BASED_DATA_RELIABLE, Boolean.class))) {
                // We can use the IP-based data
                state = deriveUSStateFromIp(context);
            }

            if (state == null) {
                // Fall back on deriving the USZipCode, however possible,
                // and using that the derive the state
                USZipCode usZipCode = context.getAttribute(TargetingContext.US_ZIP_CODE);
                if (usZipCode != null) {
                    try {
                        state = USState.valueOf(usZipCode.getState());
                    } catch (Exception e) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Unrecognized USState: " + usZipCode.getState());
                        }
                    }
                }
            }
        }

        return state;
    }

    CanadianProvince deriveCanadianProvince(TargetingContext context) {
        CanadianProvince canadianProvince = deriveCanadianProvinceFromParameter(context);

        if (canadianProvince == null && BooleanUtils.isTrue(context.getAttribute(IP_BASED_DATA_RELIABLE, Boolean.class))) {
            // SC-30 - Only derive it for Canada
            CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
            if (country != null && !"CA".equals(country.getIsoCode())) {
                return null;
            }

            // We can use the IP-based data
            canadianProvince = deriveCanadianProvinceFromIp(context);
        }

        if (canadianProvince == null) {
            CanadianPostalCode caPostalCode = context.getAttribute(TargetingContext.CANADIAN_POSTAL_CODE);
            if (caPostalCode != null) {
                canadianProvince = caPostalCode.getCanadianProvince();
            }
        }

        return canadianProvince;
    }

    Dma deriveDma(TargetingContext context) {
        Dma dma = context.getAttribute(DMA_FROM_PARAMETER);

        if (dma == null && BooleanUtils.isTrue(context.getAttribute(IP_BASED_DATA_RELIABLE, Boolean.class))) {
            // SC-30 - Only derive it for the US
            CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
            if (country != null && !"US".equals(country.getIsoCode())) {
                return null;
            }

            // We can use the IP-based data
            dma = deriveDmaFromIp(context);
        }

        if (dma == null) {
            // Try to derive DMA from US zip code
            USZipCode usZipCode = context.getAttribute(TargetingContext.US_ZIP_CODE);
            if (usZipCode != null) {
                dma = dmaManager.getDmaByZipCode(usZipCode.getZip());
            }
        }

        return dma;
    }

    TimeZone deriveTimeZone(TargetingContext context) {
        // The way we derive TimeZone is:
        // 1. Were we passed explicit u.timezone parameter?  If so, use that.
        // 2. Fall back on IP-based lookup

        String tzParam = context.getAttribute(Parameters.TIME_ZONE);
        if (tzParam != null) {
            // It can be either a TimeZone ID or an offset.
            TimeZone timeZone = TimeZoneUtils.getTimeZoneByFreeformInput(tzParam);
            if (timeZone != null) {
                return timeZone;
            }
        }

        // Fall through to the IP-based logic
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("TimeZone not specified, deriving using IP address");
        }
        return deriveTimeZoneFromIp(context);
    }

    //========================================================================
    // Methods that derive things only using parameters
    //========================================================================

    static CountryDto deriveCountryFromParameter(TargetingContext context) {
        String countryCode = context.getAttribute(Parameters.COUNTRY_CODE);
        CountryDto country = null;
        if (StringUtils.isNotBlank(countryCode)) {
            // We may have been passed ISO 3166-1 alpha-2 or alpha-3...check length
            if (countryCode.length() == 2) {
                country = context.getDomainCache().getCountryByIsoCode(countryCode);
            } else if (countryCode.length() == 3) {
                country = context.getDomainCache().getCountryByIsoAlpha3(countryCode);
            } else {
                // AF-1129 - RTB exchanges are passing us country names.  They shouldn't
                // be, but we can work around this.  And everybody (non-RTB publishers
                // as well) can benefit if they're doing silly things, too.
                country = context.getDomainCache().getCountryByName(countryCode);
            }
            if (country == null && LOG.isLoggable(Level.INFO)) {
                LOG.info("Invalid value for " + Parameters.COUNTRY_CODE + ": " + countryCode);
            }
        }
        return country;
    }

    static Coordinates deriveCoordinatesFromParameters(TargetingContext context) {
        // First see if we were passed a device location
        String latStr = context.getAttribute(Parameters.DEVICE_LATITUDE);
        String lonStr = context.getAttribute(Parameters.DEVICE_LONGITUDE);
        if (StringUtils.isEmpty(latStr) && StringUtils.isEmpty(lonStr)) {
            // Fall back on the "user" location (i.e. their home)
            latStr = context.getAttribute(Parameters.USER_LATITUDE);
            lonStr = context.getAttribute(Parameters.USER_LONGITUDE);
        }
        if (StringUtils.isNotEmpty(latStr) && StringUtils.isNotEmpty(lonStr)) {
            try {
                // MAD-1710: Can't parse latitude/longitude
                latStr = latStr.replace(",", ".");
                lonStr = lonStr.replace(",", ".");

                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                if (GeoUtils.validateCoordinates(lat, lon)) {
                    return new SimpleCoordinates(lat, lon);
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Invalid latitude/longitude: " + lat + "/" + lon);
                    }
                }
            } catch (NumberFormatException e) {
                LOG.warning("Can't parse latitude/longitude: " + latStr + "/" + lonStr);
            }
        }
        return null;
    }

    static USState deriveUSStateFromParameter(TargetingContext context) {
        String param = context.getAttribute(Parameters.STATE);
        if (StringUtils.isNotBlank(param)) {
            try {
                return USState.valueOf(param.toUpperCase());
            } catch (Exception e) {
                // SC-284 - fall back on trying to resolve it by name (i.e. OpenX RTB)
                USState state = USState.byName(param);
                if (state != null) {
                    return state;
                } else {
                    // We get here in this scenario: Nexage RTB supplies us
                    // user.state=<non-US state> but they don't supply user.country,
                    // and the device IP resolves to a location in a non-US country.
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("Invalid value for " + Parameters.STATE + ": " + param);
                    }
                }
            }
        }
        return null;
    }

    static CanadianProvince deriveCanadianProvinceFromParameter(TargetingContext context) {
        String param = context.getAttribute(Parameters.STATE);
        if (StringUtils.isNotBlank(param)) {
            try {
                return CanadianProvince.valueOf(param.toUpperCase());
            } catch (Exception e) {
                // We get here in this scenario: Nexage RTB supplies us user.state=<US state>
                // but they don't supply user.country, and the device IP resolves to a location
                // location in Canada.
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid value for " + Parameters.STATE + ": " + param);
                }
            }
        }
        return null;
    }

    static ChineseProvince deriveChineseProvinceFromParameter(TargetingContext context) {
        String param = context.getAttribute(Parameters.STATE);
        if (StringUtils.isNotBlank(param)) {
            try {
                return ChineseProvince.valueOf(param.toUpperCase());
            } catch (Exception e) {

                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid value for ChineseProvince " + Parameters.STATE + ": " + param);
                }
            }
        }
        return null;
    }

    static AustrianProvince deriveAustrianProvinceFromParameter(TargetingContext context) {
        String param = context.getAttribute(Parameters.STATE);
        if (StringUtils.isNotBlank(param)) {
            try {
                return AustrianProvince.valueOf(param.toUpperCase());
            } catch (Exception e) {

                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Invalid value for AustrianProvince " + Parameters.STATE + ": " + param);
                }
            }
        }
        return null;
    }

    Dma deriveDmaFromParameter(TargetingContext context) {
        Dma dma = null;
        String param = context.getAttribute(Parameters.DMA);
        if (StringUtils.isNotBlank(param)) {
            // First try by id, falling back on by name
            dma = dmaManager.getDmaById(param);
            if (dma == null) {
                dma = dmaManager.getDmaByName(param);
            }
            if (dma == null) {
                LOG.warning("Invalid value for " + Parameters.DMA + ": " + param);
            }
        }
        return dma;
    }

    //========================================================================
    // Methods that derive things only using IP-based data
    //========================================================================

    // Subclasses need to implement these IP-based lookups.  By the time this
    // has been called, we can rely on the fact that the IP address is the most
    // appropriate source for deriving these attributes.
    protected abstract CountryDto deriveCountryFromIp(TargetingContext context);

    protected abstract TimeZone deriveTimeZoneFromIp(TargetingContext context);

    protected abstract Coordinates deriveCoordinatesFromIp(TargetingContext context);

    protected abstract String derivePostalCodeFromIp(TargetingContext context);

    protected abstract USState deriveUSStateFromIp(TargetingContext context);

    protected abstract CanadianProvince deriveCanadianProvinceFromIp(TargetingContext context);

    protected abstract Dma deriveDmaFromIp(TargetingContext context);
}

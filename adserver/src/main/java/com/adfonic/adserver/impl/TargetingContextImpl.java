package com.adfonic.adserver.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.LocationSource;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.Deriver;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.domain.Gender;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.geo.Coordinates;
import com.adfonic.geo.PostalCodeIdManager;
import com.adfonic.util.HostUtils;
import com.adfonic.util.Range;
import com.adfonic.util.Subnet;

public class TargetingContextImpl implements TargetingContext {
    // Made this static so it doesn't get created every time we need a new
    // instance of this object (bazillions of times per second).
    private static final transient Logger LOG = Logger.getLogger(TargetingContextImpl.class.getName());

    /**
     * When running in dev machine where ipv6 is enabled cause a ip address problem as it supports only ipv4
     * So to avoid that exaception when running in dev mode, a developer can set JVM property
     * com.adfonic.adserver.TargetingContext.ipv6LoopbackWorkaround=true in Catalina.bat/.sh in CATALINA_OPT variable
     * as follows
     * CATALINA_OPTS="$CATALINA_OPTS -Dcom.adfonic.adserver.TargetingContext.ipv6LoopbackWorkaround=true"
     * When it is set to true then ip "0:0:0:0:0:0:0:1%0" will be replaced by 127.0.0.1
     * We switched to using a pattern so that "0:0:0:0:0:0:0:1" will be replaced as well.
     * NOTE: this is package to allow unit tests to manipulate it.
     */
    static boolean ipv6LoopbackDevWorkaround = BooleanUtils.toBoolean(System.getProperty("com.adfonic.adserver.TargetingContext.ipv6LoopbackWorkaround", "false"));

    private static final Pattern LOCAL_IPV6_ADDRESS_PATTERN = Pattern.compile("^0:0:0:0:0:0:0:.+$");
    private static final String LOCAL_IPV4_ADDRESS = "127.0.0.1";

    // This attribute isn't used directly by anything other than the
    // getEffectiveUserAgent and setUserAgent methods.
    static final String EFFECTIVE_USER_AGENT = "\\ua";

    // We've seen quite a few cases lately (2010-09-04) where publishers are
    // sending us requests with bogus repeated h.user-agent values like:
    // SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1, SonyEricssonF305/R1FA Browser/OpenWave/1.0 Profile/MIDP-2.1 Configuration/CLDC-1.1    
    // This pattern detects the same value repeated at least 2x with comma-space
    // separating each occurrence.
    private static final Pattern REPEATED_USER_AGENT_PATTERN = Pattern.compile("^((.+), ){2,}");

    /*
    // Handle the case when some dumbass publisher passes us some wacky crap
    // for r.ip, such as:
    // r.ip=text%2Fhtml%253%255%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%254%257%25A%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80H%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%250%250%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%C3%BFC2%C3%BF80%1D%251%250%25E+68.171.233.24
    private static final Pattern wackyIpPattern = Pattern.compile("^text/.+\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$");
    */

    // i.e. 8.38.114.222/78.38.114.222
    // i.e. aildm.zahid.com/212.119.69.187
    private static final Pattern CRAP_SLASH_IP_PATTERN = Pattern.compile("^[^/]+/(" + Subnet.IP_ADDRESS_REGEX + ")$");

    // i.e. fe80::204:61ff:254.157.241.86
    // i.e. fe80:0:0:0:0204:61ff:254.157.241.86
    private static final Pattern IPV6_DOTTED_QUAD_PATTERN = Pattern.compile("^.*:(" + Subnet.IP_ADDRESS_REGEX + ")$");

    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private final DomainCache domainCache;
    private final AdserverDomainCache adserverDomainCache;
    private final DeriverManager deriverMgr;
    private final PostalCodeIdManager postalCodeIdManager;

    private Long exchangePublisherId;
    private PublisherDto effectivePublisher;
    private AdSpaceDto adSpace;

    // Just in case somebody tries to serialize this (never say never),
    // we make this reference transient
    private transient HttpServletRequest request;

    private boolean sslRequired;

    private ByydRequest byydRequest;

    private ByydImp byydImp;

    private ByydBid byydBid;

    private ByydResponse byydResponse;

    public TargetingContextImpl(DomainCache domainCache, AdserverDomainCache adserverDomainCache, DeriverManager deriverMgr, PostalCodeIdManager postalCodeIdManager) {
        this.domainCache = domainCache;
        this.adserverDomainCache = adserverDomainCache;
        this.deriverMgr = deriverMgr;
        this.postalCodeIdManager = postalCodeIdManager;
    }

    @Override
    public void setExchangePublisherId(Long id) {
        this.exchangePublisherId = id;
    }

    @Override
    public Long getExchangePublisherId() {
        return exchangePublisherId;
    }

    @Override
    public void setEffectivePublisher(PublisherDto publisher) {
        this.effectivePublisher = publisher;
    }

    @Override
    public PublisherDto getEffectivePublisher() {
        return effectivePublisher;
    }

    /** @{inheritDoc} */
    @Override
    public AdSpaceDto getAdSpace() {
        return adSpace;
    }

    /** @{inheritDoc} */
    @Override
    public void setAdSpace(AdSpaceDto adSpace) {
        this.adSpace = adSpace;
    }

    /** @{inheritDoc} */
    @Override
    public DomainCache getDomainCache() {
        return domainCache;
    }

    /** @{inheritDoc} */
    @Override
    public AdserverDomainCache getAdserverDomainCache() {
        return adserverDomainCache;
    }

    /** @{inheritDoc} */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /** @{inheritDoc} */
    @Override
    public <T> T getAttribute(String attribute, Class<T> clazz) {
        return getAttribute(attribute);
    }

    /** @{inheritDoc} */
    @Override
    public String getHeader(String header) {
        header = header.toLowerCase();
        return (String) attributes.get(Parameters.HTTP_HEADER_PREFIX + header);
    }

    /** @{inheritDoc} */
    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            if (!entry.getKey().startsWith(Parameters.HTTP_HEADER_PREFIX)) {
                continue;
            }
            headers.put(entry.getKey().substring(Parameters.HTTP_HEADER_PREFIX.length()), (String) entry.getValue());
        }
        return headers;
    }

    /** @{inheritDoc} */
    @Override
    public String getCookie(String name) {
        return (String) attributes.get(Parameters.COOKIE_PREFIX + name);
    }

    /** @{inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attribute) {
        Object value = attributes.get(attribute);
        if (value != null) {
            return (T) value;
        }
        Deriver deriver = deriverMgr.getDeriver(attribute);
        if (deriver != null) {
            // Before we bother trying to derive the attribute, let's see if
            // the deriver allows multiple attempts on the given attribute.
            if (!deriver.canDeriveMoreThanOnce(attribute)) {
                // Multiple attempts aren't allowed.  Let's make sure that
                // (a) we haven't already attemped to derive this attribute,
                // and (b) we won't attempt it again on susbsequent calls.
                String flagKey = "_a." + attribute;
                if (attributes.containsKey(flagKey)) {
                    // It has already been flagged, which means we already
                    // attempted to derive this attribute.  Don't bother.
                    return null;
                }
                // Flag it to prevent a reattempt next time
                attributes.put(flagKey, true);
            }

            if ((value = deriver.getAttribute(attribute, this)) != null) {
                // Cache it for later
                attributes.put(attribute, value);
                return (T) value;
            }
        }
        return null;
    }

    /** @{inheritDoc} */
    @Override
    public void setAttribute(String attribute, Object value) {
        if (value == null) {
            attributes.remove(attribute);
            // Also remove the flag indicating that we've already derived this attribute
            attributes.remove("_a." + attribute);
        } else {
            attributes.put(attribute, value);
        }
    }

    /** Populate attributes with values snagged from an HTTP request.
        This method grabs all headers, parameters, and cookies and puts them
        in the attributes map.  It also determines the remote IP address.
        @param request the HTTP servlet request
        @param useHttpHeaders whether or not the actual HTTP request
        headers should be used, or whether we should only look for
        parameter-based headers (i.e. server-to-server passed headers)
    */
    /*package*/void populateAttributes(HttpServletRequest request, boolean useHttpHeaders) throws InvalidIpAddressException {
        String name, value;

        // Hold a reference to the HttpServletRequest so we can provide
        // access to it later, i.e. for things like backup logging
        this.request = request;

        if (useHttpHeaders) {
            // Populate the HTTP header values
            for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
                name = e.nextElement();
                value = request.getHeader(name);
                //LOG.finest("Header -> " + name + ": " + value);
                attributes.put(Parameters.HTTP_HEADER_PREFIX + name.toLowerCase(), value);
            }
        }

        // Populate the request parameters
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            name = e.nextElement();
            value = request.getParameter(name);
            if ("".equals(value)) {
                continue; // #754 - treat empty string as non-existent
            }
            //LOG.finest("Parameter -> " + name + ": " + value);

            // Bugzilla 1942 -- when publishers pass us stuff like h.User-Agent, we need
            // to transform that into lowercase, i.e. h.user-agent.
            if (name.startsWith(Parameters.HTTP_HEADER_PREFIX)) {
                attributes.put(name.toLowerCase(), value);
            } else {
                attributes.put(name, value);
            }
        }

        // Populate the cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                name = cookie.getName();
                value = cookie.getValue();
                //LOG.finest("Cookie -> " + name + ": " + value);
                attributes.put(Parameters.COOKIE_PREFIX + name, value);
            }
        }

        // Derive the most applicable IP address of the end user
        deriveIpAddress(request);

        if (!useHttpHeaders) {
            // If useHttpHeaders=false but the
            // calling app hasn't proxied through the given User-Agent, fall
            // back on the User-Agent of the calling app.  This is a fix for
            // java apps that may not have access to a real User-Agent.
            String userAgent = (String) attributes.get(Parameters.HTTP_HEADER_PREFIX + "user-agent");
            if (userAgent == null) {
                // Fall back on the User-Agent on the request
                userAgent = request.getHeader("User-Agent");
                if (userAgent != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Falling back on request User-Agent: " + userAgent);
                    }
                    attributes.put(Parameters.HTTP_HEADER_PREFIX + "user-agent", userAgent);
                }
            }
        }

        // Make sure there's an effective User-Agent attribute available
        String userAgent = getEffectiveUserAgent();
        if (userAgent == null) {
            // Use the supplied User-Agent header
            setUserAgent(getHeader("User-Agent"));
        }
    }

    void deriveIpAddress(HttpServletRequest request) throws InvalidIpAddressException {
        // Let's make sure the "IP" attribute is set in the targeting context.
        // The caller may have passed in "r.ip" (or we fall back on the actual
        // remote address if not, i.e. for /js or /ct requests).  But it's not
        // quite as simple as just taking that IP address at face value.  If
        // there was an "X-Forwarded-For" header, then the request was proxied.
        // In that case, we need to walk back up the IP forward list to find
        // the IP "closest" to the actual user.  But of course it's not quite
        // that simple, either.  Sometimes the "closest" IP to the user is on
        // a private network (i.e. 192.168.* or 172.16.* or 10.*), and that
        // doesn't do us any good since we can't derive the country or service
        // provider from that.  So what we're doing here is composing a list
        // of IP addresses, closest (X-Forwarded-For) to furthest (the provided
        // IP), and we'll walk the list until we find a non-private IP address.
        //
        // i.e.
        // r.ip = 206.88.141.92
        // h.x-forwarded-for: 192.168.1.7, 172.16.24.2, 206.88.1.12
        //
        // The list will end up containing:
        // 192.168.1.7, 172.16.24.2, 206.88.1.12, 206.88.141.92
        // We'll pop & discard the first two, since they're private, and
        // we'll end up using 206.88.1.12 as the actual IP address.

        // Use as low initialCapacity for the IPs list as possible to make this slightly
        // GC-friendlier.  The default initialCapacity of ArrayList if not specified
        // is 10, and that's most likely way more than we'll need.  On average, we're
        // seeing one and only one IP address on the x-forwarded-for header.  So let's
        // optimize this.
        List<String> ips;

        // Start with the X-Forwarded-For header
        String xForwardedFor = (String) attributes.get(Parameters.HTTP_HEADER_PREFIX + "x-forwarded-for");
        if (StringUtils.isNotBlank(xForwardedFor)) {
            String[] xForwardedIps = StringUtils.split(xForwardedFor, ',');
            ips = new ArrayList<String>(xForwardedIps.length + 1);
            for (String ip : xForwardedIps) {
                ips.add(ip.trim());
            }
        } else {
            ips = new ArrayList<String>(1);
        }

        // End with the actual provided or detected client IP
        String ip = (String) attributes.get(Parameters.IP);
        if (ip != null) {
            // Bugzilla 1309 - somebody was passing r.ip=87.218.62.107%0A,
            // so trim any whitespace.
            ip = ip.trim();
            /*
            // TODO: remove this once Nexage cuts the crap
            // Handle the wacky case noted in the wackyIpPattern above
            Matcher matcher = wackyIpPattern.matcher(ip);
            if (matcher.matches()) {
            LOG.warning("Handling wacky IP: " + ip);
            ip = matcher.group(1);
            }
            */
        }

        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();
        } else if (ip.indexOf(',') != -1) {
            throw new InvalidIpAddressException("Provided IP address is invalid: " + ip);
        }
        ips.add(ip);

        // While we're at this point, we need to store this "provided IP"
        // value in the context as the sorta original value.  This attribute
        // always needs to be set.
        setAttribute(PROVIDED_IP, ip);

        // Ok we have the list now
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("IP list: " + ips);
        }

        // Walk the list until we find a non-private IP address
        ip = null;
        for (int k = 0; k < ips.size(); ++k) {
            String ipCandidate = ips.get(k);

            // #730 - wrap a try/catch around this in case junk ends up in
            // here somehow.  We're filtering out the "unknown" crap above,
            // but in case anything else slips through...
            try {
                // Bugzilla 1797 - reject ad requests with IPv6 addresses
                if (ipCandidate != null && ipCandidate.indexOf(':') != -1) {
                    //if ipv6 coming from local dev machine change it to ipv4
                    if (ipv6LoopbackDevWorkaround && LOCAL_IPV6_ADDRESS_PATTERN.matcher(ipCandidate).matches()) {
                        ipCandidate = LOCAL_IPV4_ADDRESS;
                        ips.set(k, ipCandidate);

                    } else {
                        // Before we just bail on it, let's see if it's dotted quad notation,
                        // i.e. an IPv4 to IPv6 transition address.  If it is, we can at least
                        // snarf the IPv4 address from it and proceed.
                        Matcher matcher = IPV6_DOTTED_QUAD_PATTERN.matcher(ipCandidate);
                        if (matcher.find()) {
                            if (LOG.isLoggable(Level.INFO)) {
                                LOG.info("Using IPv4 dotted quad IP (" + matcher.group(1) + ") from IPv6 address: " + ipCandidate);
                            }
                            ipCandidate = matcher.group(1);
                        } else {

                            throw new InvalidIpAddressException("Provided IP address is invalid: " + ipCandidate);
                        }
                    }
                }

                // Check to make sure it's not on a private network
                if (!Subnet.isOnPrivateNetwork(ipCandidate)) {
                    ip = ipCandidate;
                    break;
                }
            } catch (Exception e) {
                // Remove it from the candidate list
                ips.remove(k);

                // Before we bail on this IP candidate, see if it matches "crap/ip".
                Matcher matcher = CRAP_SLASH_IP_PATTERN.matcher(ipCandidate);
                if (matcher.matches()) {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("Detected crap/ip pattern: " + ipCandidate);
                    }
                    // Replace it with just the IP, and try it again
                    ips.add(k, matcher.group(1));
                } else {
                    // Nope, it's not crap/ip, it's some other crap...
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.log(Level.INFO, "Issue with candidate IP (" + ipCandidate + ")", e);
                    }
                    // TODO: track a stats counter for this condition
                }

                // Decrement the index since we just removed element "k", and we're
                // about to hit k++ in the for loop...
                --k;
            }
        }

        boolean isPrivateNetwork = false;
        if (ip == null) {
            // They all must have been on private networks, or otherwise invalid
            if (ips.isEmpty()) {
                // Looks like all the candidates were invalid.  This would be the
                // case if we were only passed IPv6 address(es), for example.
                throw new InvalidIpAddressException("Provided IP address is invalid: " + getAttribute(PROVIDED_IP, String.class));
            } else {
                // Use the first one from the list, but flag that it's private network
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Can't determine non-private IP among: " + ips);
                }
                ip = ips.get(0);
                isPrivateNetwork = true;
            }
        }

        // One last check to make sure the chosen IP is really an IP address.
        // We've seen cases where publishers pass us crap like this:
        // r.ip=StreamUtils.java+-%3DERROR%3D-
        // r.ip=86.194.5.66%2F186.194.5.66
        if (!Subnet.isIpAddress(ip)) {
            throw new InvalidIpAddressException("Provided IP address is invalid: " + ip);
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Using IP: " + ip);
        }
        setAttribute(Parameters.IP, ip);

        // If the only IP address we were able to derive is on a private
        // network, set a respective flag in the context to save us the
        // check later.
        setAttribute(TargetingContext.IS_PRIVATE_NETWORK, isPrivateNetwork);
    }

    /** @{inheritDoc} */
    @Override
    public void setIpAddress(String ip) throws InvalidIpAddressException {
        setAttribute(PROVIDED_IP, ip);

        if (!Subnet.isIpAddress(ip)) {
            throw new InvalidIpAddressException("Provided IP address is invalid: " + ip);
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Using IP: " + ip);
        }

        boolean isPrivateNetwork = false;
        try {
            isPrivateNetwork = Subnet.isOnPrivateNetwork(ip);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Issue with IP (" + ip + ")", e);
        }

        setAttribute(Parameters.IP, ip);

        // If the only IP address we were able to derive is on a private
        // network, set a respective flag in the context to save us the
        // check later.
        setAttribute(TargetingContext.IS_PRIVATE_NETWORK, isPrivateNetwork);
    }

    /** @{inheritDoc} */
    @Override
    public String getEffectiveUserAgent() {
        return getAttribute(EFFECTIVE_USER_AGENT, String.class);
    }

    /** @{inheritDoc} */
    @Override
    public void setUserAgent(String userAgent) {
        if (userAgent == null) {
            // This would result from an ad request with no proxied User-Agent
            // (i.e. h.user-agent) and no actual User-Agent header either.
            // While an ad request will probably fail in this case, it's actually
            // perfectly acceptable in other cases, i.e. server-to-server calls
            // for things like install or conversion tracking.
            LOG.fine("No User-Agent to set");
            return;
        }

        // If the userAgent value is really long (i.e. >512) then we should
        // check to make sure it's not one of those funky repeated User-Agent
        // values, with the same value repeated over and over.
        if (userAgent.length() > 512) {
            Matcher matcher = REPEATED_USER_AGENT_PATTERN.matcher(userAgent);
            if (matcher.find()) {
                LOG.warning("Repeated User-Agent from " + getAttribute(Parameters.IP) + ": " + userAgent);
                // Grab just the section that repeats (not including comma-space)
                userAgent = matcher.group(2);
            }
        }

        setAttribute(EFFECTIVE_USER_AGENT, userAgent);
    }

    /** @{inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void populateImpression(Impression impression, SelectedCreative selectedCreative) {
        if (selectedCreative != null) {
            // Set the Creative id on the Impression
            impression.setCreativeId(selectedCreative.getCreative().getId());

            if (selectedCreative.getProxiedDestination() != null) {
                // Make sure the proxied destination URL gets set on the Impression so
                // we can use it properly at click time
                impression.setPdDestinationUrl(selectedCreative.getProxiedDestination().getDestinationUrl());
            }
            //Populate data fee if proceeds
            if (selectedCreative.getCreative().getCampaign().getDataFeeId() != 0) {
                impression.setCampaignDataFeeId(selectedCreative.getCreative().getCampaign().getDataFeeId());
            }
        }

        // Mark whether or not this request was made in test mode
        impression.setTestMode(isFlagTrue(Parameters.TEST_MODE));

        // Make sure the tracking identifier gets populated, if provided or derived.
        // AF-1084 - make sure Impression.trackingIdentifier is secure, so that it
        // will be stored in secure form in AD_EVENT_LOG and tracker db.
        impression.setTrackingIdentifier(getAttribute(TargetingContext.SECURE_TRACKING_ID, String.class));

        // Make sure any device identifier(s) get populated
        Map<Long, String> suppliedDeviceIdentifiers = getAttribute(DEVICE_IDENTIFIERS, Map.class);
        impression.setDeviceIdentifiers(suppliedDeviceIdentifiers);

        ModelDto model = getAttribute(MODEL, ModelDto.class);
        if (model != null) {
            impression.setModelId(model.getId());
        }

        CountryDto country = getAttribute(COUNTRY, CountryDto.class);
        if (country != null) {
            impression.setCountryId(country.getId());

            // Make sure we set Impression.postalCodeId whenever possible, but
            // we can only do that if we know the country, which is why this
            // section of code is here inside the country check.
            // AF-1305 - only log postal code if we're confident about its accuracy
            if (BooleanUtils.isTrue(getAttribute(HAS_COORDINATES, Boolean.class))) {
                String postalCode = getAttribute(POSTAL_CODE, String.class);
                if (postalCode != null) {
                    impression.setPostalCodeId(postalCodeIdManager.getPostalCodeId(country.getIsoCode(), postalCode));
                }
            }
        }

        OperatorDto operator = getAttribute(OPERATOR, OperatorDto.class);
        if (operator != null && operator.getCountryIsoCode().equals(country.getIsoCode())) {
            impression.setOperatorId(operator.getId());
        }

        GeotargetDto geotarget = getAttribute(GEOTARGET, GeotargetDto.class);
        if (geotarget != null) {
            impression.setGeotargetId(geotarget.getId());
        }

        IntegrationTypeDto integrationType = getAttribute(INTEGRATION_TYPE, IntegrationTypeDto.class);
        if (integrationType != null) {
            impression.setIntegrationTypeId(integrationType.getId());
        }

        impression.setAgeRange(getAttribute(AGE_RANGE, Range.class));

        impression.setGender(getAttribute(GENDER, Gender.class));

        // New fields as of SC-134
        impression.setHost(HostUtils.getHostName());

        TimeZone timeZone = getAttribute(TIME_ZONE, TimeZone.class);
        if (timeZone != null) {
            impression.setUserTimeZoneId(timeZone.getID());
        }

        // TODO: note the "strategy"  ...once we start distinguishing
        //impression.setStrategy("...");

        impression.setDateOfBirth(getAttribute(DATE_OF_BIRTH, Date.class));

        Coordinates coordinates = getAttribute(COORDINATES, Coordinates.class);
        if (coordinates != null) {
            impression.setLatitude(coordinates.getLatitude());
            impression.setLongitude(coordinates.getLongitude());
        }

        LocationSource locationSource = getAttribute(LOCATION_SOURCE, LocationSource.class);
        if (locationSource != null) {
            impression.setLocationSource(locationSource.name());
        }
        impression.setSslRequired(sslRequired);

        Integer videoProtocol = getAttribute(VIDEO_PROTOCOL);
        impression.setVideoProtocol(videoProtocol);
    }

    /** @{inheritDoc} */
    @Override
    public void populateAdEvent(AdEvent event, Impression impression, CreativeDto creative) {
        // Use the TargetingContext's derived IP address and User-Agent
        String ipAddress = getAttribute(Parameters.IP, String.class);
        String userAgentHeader = getEffectiveUserAgent();
        event.populate(impression, ipAddress, userAgentHeader, creative == null ? null : creative.getCampaign().getId(), adSpace.getPublication().getId());
    }

    /** @{inheritDoc} */
    @Override
    public HttpServletRequest getHttpServletRequest() {
        return request;
    }

    @Override
    public boolean isSslRequired() {
        return sslRequired;
    }

    @Override
    public void setSslRequired(boolean sslRequired) {
        this.sslRequired = sslRequired;
    }

    @Override
    public boolean isFlagTrue(String attribute) {
        Object value = getAttribute(attribute);
        if (value != null && value == Boolean.TRUE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setFlagTrue(String attribute) {
        setAttribute(attribute, Boolean.TRUE);
    }

    @Override
    public void setFlagFalse(String attribute) {
        setAttribute(attribute, Boolean.FALSE);
    }

}

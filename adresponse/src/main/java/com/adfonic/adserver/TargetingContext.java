package com.adfonic.adserver;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.adfonic.ddr.UserAgentAware;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.util.HttpRequestContext;

public interface TargetingContext extends HttpRequestContext, UserAgentAware {
    // Derived attribute names
    //
    // NOTE: these now have a leading \ in order to fix this issue:
    // https://tickets.adfonic.com/browse/BZ-1844
    //
    // java.lang.ClassCastException: java.lang.String cannot be cast to
    // com.adfonic.domain.Model at com.adfonic.adserver.controller.BeaconController.handleBeacon(BeaconController.java:160)
    //
    // Turns out a publisher is hitting our beacon service with a funky m=
    // parameter:
    // 223.189.176.232 - - [18/Aug/2011:10:37:16 +0000] GET
    // /bc/07789eec-ad7a-4ce3-bb42-df7df7796286/71413088-aed4-4a52-a32d-3919bf79c778.gif?m=0.facebook.com/zout=1
    // HTTP/1.1 200 60 5
    // "LG-GD510/V100 Teleca/WAP2.0 Profile/MIDP-2.1 Configuration/CLDC-1.1 UNTRUSTED/1.0"
    //
    // So we're taking the one-byte hit (probably 20-30 extra bytes per request)
    // as an added measure here. Even better would be just to isolate
    // Parameter.*
    // from TargetingContext.* in separate maps, which we should probably do at
    // some point. For now, this is trivial and it solves the immediate issue.
    //
    String COUNTRY = "\\c";
    String MODEL = "\\m";
    String DEVICE_PROPERTIES = "\\dp";
    String DEVICE_IS_ROBOT_CHECKER_OR_SPAM = "\\dr";
    String MOBILE_IP_ADDRESS_RANGE = "\\mip";
    String OPERATOR = "\\o";
    String PLATFORM = "\\p";
    String CAPABILITIES = "\\cap";
    String CAPABILITY_IDS = "\\capIds";
    String AGE_RANGE = "\\ar";
    String PROVIDED_CREATIVE_ID = "\\pcid";
    String DATE_OF_BIRTH = "\\dob";
    String GENDER = "\\g";
    String ACCEPTED_LANGUAGES = "\\al";
    String UNFILLED_REASON = "\\ur";
    String MEDIUM = "\\med";
    String COORDINATES = "\\ll";
    String LOCATION_SOURCE = "\\ls";
    String HAS_COORDINATES = "\\gl"; // COORDINATES != null
    String GEOTARGET = "\\gt";
    String LOCATION_AUDIENCES = "\\la"; // Redis stored audience ids
    String ADSQUARE_ENRICH_AUDIENCES = "\\aea"; // response from Adsquare Enrichment API
    String ADSQUARE_ENRICH_CREATIVES = "\\aec";
    String FACTUAL_PROXIMITY_MATCHES = "\\fpm"; // Factual "Proximity" audiences from Outpost API server
    String FACTUAL_AUDIENCE_MATCHES = "\\fam"; // Factual "Audience" audiences from Outpost API server
    String FACTUAL_CREATIVES = "\\fac"; // Map<Long,String> - creativeId -> pixelUrl we need to add to Ad markup for Factual audience targetted bids
    String DYNAMIC_IMP_TRACKERS = "\\dit"; // List<String> - additional impression trackers (Factual)
    String US_STATE = "\\st";
    String US_ZIP_CODE = "\\z";
    String UK_POSTAL_CODE = "\\ukpc";
    String POSTAL_CODE = "\\pc";
    String CANADIAN_POSTAL_CODE = "\\cpc";
    String CANADIAN_PROVINCE = "\\cp";
    String CHINESE_POSTAL_CODE = "\\cnpc";
    String CHINESE_PROVINCE = "\\cnp";
    String AUSTRIAN_PROVINCE = "\\aup";
    String AUSTRIAN_POSTAL_CODE = "\\aupc";
    String SPANISH_POSTAL_CODE = "\\espc";
    String SPANISH_PROVINCE = "\\esp";
    String DMA = "\\dma";
    String TIME_ZONE = "\\tz";
    String QUOVA_IP_INFO = "\\qi";
    String INTEGRATION_TYPE = "\\it";
    String USE_BEACONS = "\\ub";
    String CONTENT_FORM_RESTRICTION_SET = "\\cf";

    String BLOCKED_EXT_CRT_TYP_SET = "\\bects";

    // This attribute is the r.ip param, or getRemoteAddr if r.ip wasn't passed
    String PROVIDED_IP = "\\pip";

    // This is the numeric value corresponding to the derived IP address
    String IP_ADDRESS_VALUE = "\\ipv";

    // This flag will be set to Boolean.TRUE at TargetingContext creation
    // time if the derived IP address is on a private network (i.e. if we
    // were unable to derive a non-private IP)
    String IS_PRIVATE_NETWORK = "\\priv";

    // This attribute is the established (defaulted, if not specified via
    // parameters) tracking identifier type
    String TRACKING_IDENTIFIER_TYPE = "\\tt";

    // This is the secure form of Parameters.TRACKING_ID
    String SECURE_TRACKING_ID = "\\sti";

    // The value of this attribute is a Map<Long,String> that holds device
    // identifiers by DeviceIdentifierType id, as established from the request
    String DEVICE_IDENTIFIERS = "\\di";

    // Boolean flag telling that tracking is disabled - dnt/lmt/coppa
    String TRACKING_DISABLED = "\\trd";

    // The value of this attribute is a set of audienceIds
    String DEVICE_AUDIENCES = "\\das";

    // The value of this attribute is
    String DEVICE_OPT_OUT = "\\doo";
    String DEVICE_DATA = "\\ddata";

    // This allows us to restrict targeting by blocking certain categories.
    // Introduced for RTB "bcat" support.
    String BLOCKED_CATEGORY_IDS = "\\bcat";

    // This allows us to restrict targeting by blocking certain domains.
    // Introduced for RTB "badv" blocked advertiser domain support.
    String BLOCKED_ADVERTISER_DOMAINS = "\\badv";

    // To restrict targeting so that at least one of the assets matches one in
    // the list
    // Introduced with RTB v2 support
    String MIME_TYPE_WHITELIST = "\\mimes";

    // To restrict targeting to those with the specified minimum ecpm
    String ECPM_FLOOR = "\\ecpmflr";

    // This allows us to restrict targeting by blocking certain destination
    // types.
    // Introduced for AdX RTB "DestinationUrlType: ClickToApp" support
    String BLOCKED_DESTINATION_TYPES = "\\bdtyp";

    // This allows us to restrict targeting by blocking certain bid types.
    // Introduced for AdX RTB "DestinationUrlType: ClickToApp" support
    String BLOCKED_BID_TYPES = "\\bbtyp";

    // This allows us to restrict targeting by blocking on creative attributes.
    String BLOCKED_CREATIVE_ATTRIBUTES = "\\battrs";

    // Introduced to support OpenX RTB "pub_blocked_ad_languages", this is
    // a Set<String> containing Langage.isoCodes blocked by the publisher
    String BLOCKED_LANGUAGE_ISO_CODES = "\\blang";

    // Introduced to support OpenX RTB DHTML blocking, when this Boolean is
    // true, all extended creatives will be blocked
    String BLOCK_EXTENDED_CREATIVES = "\\bext";

    // Flag to check if the request is native
    String IS_NATIVE = "\\rtbisnative";

    // PMP input from RTB
    String RTB_PMP = "\\pmdi";

    // Map<Long, ByydDeal> = store if creative is biding on some deal (or not for non private deals) 
    String PMP_CREATIVES_DEALS = "\\pmpc";

    // OpenRTB 2.3 native request
    String NATIVE_REQUEST = "\\nrq";
    String NATIVE_RESPONSE = "\\nrs";

    String RENDERED_TRANSFORM = "\\rndrdtfrm";

    // This allows us to block plugins - even offline ones
    String BLOCK_PLUGINS = "\\noplgn";

    // This is the derived Dimension representing t.width & t.height if supplied
    String TEMPLATE_SIZE = "\\wh";

    // Whether or not markup is available, i.e. t.markup=1 or t.markup=true or
    // not supplied
    String MARKUP_AVAILABLE = "\\ma";

    // This is the Set<Feature> that the given integration type accepts
    String ACCEPTED_FEATURES = "\\af";

    // This is the SelectedCreative that was targeted when an ad gets served
    String SELECTED_CREATIVE = "\\sc";

    // as noun. dummy object to flag true - since there is no type checking
    // around
    Integer SET_FLAG = Integer.valueOf(1);

    String DEAL_FLOOR_EXISTS = "\\dfe";

    String EXCHANGE_NAME = "\\exch";

    String VIDEO_DURATION = "\\vidur";

    String BYYD_REQUEST = "\\bdreq"; //ByydRequest written allways once

    String BYYD_IMP = "\\bdimp"; //ByydImp written allways once

    String BYYD_RESPONSE = "\\bdrsp";//ByydResponse written maximally once (on bid)

    String DEBUG_CONTEXT = "\\dbcx";

    // For rendering - is it rendered for external creative audit purposes
    String CREATIVE_AUDIT = "\\xca";

    // From rendering - Mopub needs impression trackers not in markup but in special bid response field
    String IMP_TRACK_LIST = "\\itl";

    // VAST 2/3/InLine/Wrapper
    String VIDEO_PROTOCOL = "\\vp";

    /**
     * Publisher with externalId passed to REST endpoint
     */
    void setExchangePublisherId(Long id);

    /**
     * Publisher with externalId passed to REST endpoint
     */
    Long getExchangePublisherId();

    /**
     * May be exchange or associative publisher 
     */
    void setEffectivePublisher(PublisherDto publisher);

    /**
     * May be exchange or associative publisher (Millenum, Orange) 
     */
    PublisherDto getEffectivePublisher();

    /** Provide access to the AdSpace to which this context applies */
    AdSpaceDto getAdSpace();

    /** Provide a setter in case the AdSpace wasn't known at construction time */
    void setAdSpace(AdSpaceDto adSpace);

    /** Provide access to the domain cache to users of this object */
    DomainCache getDomainCache();

    /** Provide access to the adserver domain cache to users of this object */
    AdserverDomainCache getAdserverDomainCache();

    /** Get all attributes that have been set or derived already */
    Map<String, Object> getAttributes();

    /** Get or derive an attribute of a known type */
    <T> T getAttribute(String attribute, Class<T> clazz);

    /** Get an HTTP header value */
    @Override
    String getHeader(String header);

    /** Get all HTTP headers */
    Map<String, String> getHeaders();

    /** Get a cookie value */
    String getCookie(String name);

    /** Get or derive a specific attribute */
    <T> T getAttribute(String attribute);

    /** Return if attribute exist and it is Boolean.TRUE */
    boolean isFlagTrue(String attribute);

    /** Sets attribute value to Boolean.TRUE */
    void setFlagTrue(String attribute);

    /** Sets attribute value to Boolean.FALSE */
    void setFlagFalse(String attribute);

    /** Set an attribute */
    void setAttribute(String attribute, Object value);

    /**
     * Direct method for setting a specific IP address associated with the
     * request. This is used by the RTB controller.
     */
    void setIpAddress(String ip) throws InvalidIpAddressException;

    /**
     * Direct method for setting a specific User-Agent associated with the
     * request.
     */
    @Override
    void setUserAgent(String userAgent);

    /**
     * Direct method for getting the effective User-Agent associated with the
     * request. The "effective User-Agent" may vary from the value that was set,
     * based on certain length enforcement and pattern checking rules.
     */
    @Override
    String getEffectiveUserAgent();

    /**
     * Populate an Impression object using attributes derived on this context
     * 
     * @param impression
     *            the Impression object to populate
     * @param selectedCreative
     *            the SelectedCreative that was targeted, or null for an
     *            unfilled request
     */
    void populateImpression(Impression impression, SelectedCreative selectedCreative);

    /**
     * Populate an AdEvent object from an Impression object
     * 
     * @param event
     *            the AdEvent object to populate
     * @param impression
     *            the Impression to use
     * @param creative
     *            the Creative that was targeted
     */
    void populateAdEvent(AdEvent event, Impression impression, CreativeDto creative);

    /**
     * Access to the HttpServletRequest, if set
     */
    HttpServletRequest getHttpServletRequest();

    boolean isSslRequired();

    void setSslRequired(boolean sslRequired);

}

package com.adfonic.adserver;

public interface Parameters {
    String SERVICE_PROPERTY_PREFIX = "s.";
    String HTTP_HEADER_PREFIX = "h.";
    String COOKIE_PREFIX = "c.";
    String TEMPLATE_PARAM_PREFIX = "t.";
    String USER_DATA_PREFIX = "u.";
    String DEVICE_PREFIX = "d.";
    String REQUEST_PARAM_PREFIX = "r.";
    String PUBLICATION_PROPERTY_PREFIX = "p.";
    String OVERRIDE_PROPERTY_PREFIX = "o.";

    String STATIC_IMPRESSION_ID = REQUEST_PARAM_PREFIX + "impid";
    String STATIC_FALLBACK_URL = REQUEST_PARAM_PREFIX + "fallback";
    String STATIC_PASSBACK_URL = REQUEST_PARAM_PREFIX + "passback";

    // Service properties
    String TEST_MODE = SERVICE_PROPERTY_PREFIX + "test";
    String CREATIVE_ID = SERVICE_PROPERTY_PREFIX + "creative_id";
    String ADOPS_KEY = SERVICE_PROPERTY_PREFIX + "key";

    // Template properties
    String COLOR_SCHEME = TEMPLATE_PARAM_PREFIX + "colorScheme";
    String FORMAT = TEMPLATE_PARAM_PREFIX + "format";
    String MARKUP = TEMPLATE_PARAM_PREFIX + "markup";
    String CONSTRAINTS = TEMPLATE_PARAM_PREFIX + "constraints";
    String PRETTY = TEMPLATE_PARAM_PREFIX + "pretty";
    String BEACONS_MODE = TEMPLATE_PARAM_PREFIX + "beacons";
    String TEMPLATE_WIDTH = TEMPLATE_PARAM_PREFIX + "width";
    String TEMPLATE_HEIGHT = TEMPLATE_PARAM_PREFIX + "height";
    String EXCLUDED_FEATURES = TEMPLATE_PARAM_PREFIX + "exclude";

    // User data
    String GENDER = USER_DATA_PREFIX + "gender";
    String DATE_OF_BIRTH = USER_DATA_PREFIX + "dob";
    String AGE = USER_DATA_PREFIX + "age";
    String AGE_LOW = USER_DATA_PREFIX + "ageLow";
    String AGE_HIGH = USER_DATA_PREFIX + "ageHigh";
    String LANGUAGE = USER_DATA_PREFIX + "lang";
    String USER_LATITUDE = USER_DATA_PREFIX + "latitude";
    String USER_LONGITUDE = USER_DATA_PREFIX + "longitude";
    String TIME_ZONE = USER_DATA_PREFIX + "timezone";
    String DO_NOT_TRACK = USER_DATA_PREFIX + "dnt";

    // Device data
    String DEVICE_OS_VERSION = DEVICE_PREFIX + "osVersion";
    String DEVICE_LATITUDE = DEVICE_PREFIX + "latitude";
    String DEVICE_LONGITUDE = DEVICE_PREFIX + "longitude";
    String DEVICE_LOCATION_ACCURACY = DEVICE_PREFIX + "locationAcc";
    String DEVICE_LOCATION_AGE = DEVICE_PREFIX + "locationAge";
    String DEVICE_SCREEN_SCALE = DEVICE_PREFIX + "screenScale";

    // Request properties
    String TRACKING_ID = REQUEST_PARAM_PREFIX + "id";
    String IP = REQUEST_PARAM_PREFIX + "ip";
    String HARDWARE = REQUEST_PARAM_PREFIX + "hw";
    String INTEGRATION_TYPE = REQUEST_PARAM_PREFIX + "client";
    String MCC_MNC = REQUEST_PARAM_PREFIX + "mccmnc";
    String NETWORK_TYPE = REQUEST_PARAM_PREFIX + "nettype";
    String PARALLEL = REQUEST_PARAM_PREFIX + "parallel";
    String NATIVE = REQUEST_PARAM_PREFIX + "native";

    // "Override" properties...these are generally considered to be undocumented
    // and for internal/testing use only, but there may be special cases where
    // actual users might take advantage of them.
    String COUNTRY_CODE = OVERRIDE_PROPERTY_PREFIX + "country";
    String STATE = OVERRIDE_PROPERTY_PREFIX + "state";
    String POSTAL_CODE = OVERRIDE_PROPERTY_PREFIX + "postalCode";
    String DMA = OVERRIDE_PROPERTY_PREFIX + "dma";
    String MEDIUM_DEPRECATED = "t.type";
    String MEDIUM = OVERRIDE_PROPERTY_PREFIX + "medium";
    // Smaato insists on o.format...and the customer is always right
    String FORMATS = OVERRIDE_PROPERTY_PREFIX + "format";
}

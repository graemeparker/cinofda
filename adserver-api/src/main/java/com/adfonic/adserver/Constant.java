package com.adfonic.adserver;

public class Constant {

    public static final Integer ZERO = Integer.valueOf(0);
    public static final Integer ONE = Integer.valueOf(1);

    public static final Long UNKNOWN_COUNTRY_ID = Long.valueOf(0);

    public static final String USD = "USD";
    public static final String EUR = "EUR";

    public static final String APPL_JSON = "application/json";
    public static final String APPL_JSON_UTF8 = APPL_JSON + "; charset=utf-8";
    public static final String APPL_XML = "application/xml";
    public static final String APPL_XML_UTF8 = APPL_XML + "; charset=utf-8";
    public static final String TEXT_HTML = "text/html";

    public static final String WIN_URL_PATH = "/rtb/win";
    public static final String BEACON_URI_PATH = "/bc";
    public static final String VAST_URI_PATH = "/creative/vast";
    public static final String CLICK_THROUGH_PATH = "/ct";
    public static final String CLICK_REDIRECT_PATH = "/cr";
    public static final String SP_URL_PARAM = "sp";
    public static final String CLICK_REDIRECT_URL_PARAM = "redir";

    public static final String AS_CONFIG_FILENAME = "adfonic-adserver.properties";

    public static final String XAUDIT_IMPRESSION_EXTERNAL_ID = "00000000-0000-0000-0000-000000000000";
    public static final String XAUDIT_ADSPACE_EXTERNAL_ID = "00000000-0000-0000-0000-000000000000"; //
    public static final String XAUDIT_PUBLICATION_EXTERNAL_ID = "00000000-0000-0000-0000-000000000000"; //
}

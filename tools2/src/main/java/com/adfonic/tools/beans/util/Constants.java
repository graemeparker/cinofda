package com.adfonic.tools.beans.util;

import com.adfonic.presentation.reporting.budget.impl.BudgetBreakdown;
import com.adfonic.presentation.reporting.campaign.impl.AdvertiserCampaignReportServiceImpl.CampaignDetailedReportOption;
import com.adfonic.presentation.reporting.device.impl.AdvertiserDeviceReportServiceImpl.LocationBreakdown;
import com.adfonic.presentation.reporting.device.impl.AdvertiserDeviceReportServiceImpl.LocationBreakdown.DeviceGrouping;

public class Constants {

    // Time periods, hours and days.
    private static final Integer[] HOURS = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
            22, 23 };
    private static final Integer[] WEEKDAY_INDICES = new Integer[] { 1, 2, 3, 4, 5 };

    // LOGINBEAN
    // spring-security url
    public static final String SPRING_SECURITY_CHECK_URL = "/j_spring_security_check";
    public static final String ACCESS_DENIED_URL = "/accessdenied";

    // the role that the spring security context adds when logged in as an admin
    public static final String LOGGED_IN_AS_ADMIN_ROLE = "Admin";

    public static final String KEY_MESSAGE_COMMON_ERROR = "page.error.common.error";

    // DASHBOARD pretty config keys
    public static final String P_DASHBOARD_ADVERTISER = "pretty:dashboard-advertiser";
    public static final String P_DASHBOARD_PUBLISHER = "pretty:dashboard-publisher";
    public static final String P_DASHBOARD_AGENCY = "pretty:dashboard-agency";

    // OPTIMISATION pretty config keys
    public static final String P_OPTIMISATION_ADVERTISER = "pretty:optimisation-advertiser";

    // REPORTING pretty config keys
    public static final String P_REPORTING_ADVERTISER = "pretty:reporting-advertiser";

    // AUDIENCE pretty config keys
    public static final String P_AUDIENCE_BUILDER = "pretty:audience-builder";

    // TRANSACTIONS pretty config keys
    public static final String P_TRANSACTIONS_REDIRECT = "pretty:transactions-redirect";
    public static final String P_TRANSACTIONS_ADVERTISER = "pretty:transactions-advertiser";
    public static final String P_TRANSACTIONS_PUBLISHER = "pretty:transactions-publisher";

    // ACCOUNT_DETAILS pretty config keys
    public static final String P_ACCOUNT_DETAILS = "pretty:accountDetails";

    public static final String LOGINFORM = "loginForm";
    public static final String KEY_MESSAGE_USER_NOT_FOUND = "page.login.validation.user.nodatafound";

    // KEYS INTO THE USER_SESSION_BEAN
    public static final String USERDTO = "userDto";
    public static final String USER_STATUS_VERIFIED = "VERIFIED";
    public static final String ADFONIC_USER = "adfonicUser";

    // MANAGED_BEANS
    public static final String USER_SESSION_BEAN = "userSessionBean";
    public static final String LANGUAGE_SESSION_BEAN = "languageSessionBean";
    public static final String DASHBOAR_BEAN = "dashBoardMBean";
    public static final String AGENCY_DASHBOAR_BEAN = "agencyConsoleDashboardMBean";
    public static final String PUBLISHER_DASHBOAR_BEAN = "publisherDashBoardMBean";
    public static final String DASHBOARD_HEADER_FIGURESBEAN = "headerFiguresMBean";
    public static final String DATATABLE_EXPANDER_JS_BEAN = "chartExpanderJs";
    public static final String CAMPAIGN_MBEAN = "campaignMBean";
    public static final String CAMPAIGN_SCHEDULER_BEAN = "campaignSchedulingMBean";
    public static final String CAMPAIGN_SETUP_BEAN = "campaignSetupMBean";
    public static final String CAMPAIGN_NAVIGATION_BEAN = "campaignNavigationSessionBean";
    public static final String CAMPAIGN_TARGETING_BEAN = "campaignTargetingMBean";
    public static final String CAMPAIGN_INVENTORY_TARGETING_BEAN = "campaignInventoryTargetingMBean";
    public static final String CAMPAIGN_TARGETING_LOCATION_BEAN = "campaignLocationTargetingMBean";
    public static final String CAMPAIGN_CREATIVE_BEAN = "campaignCreativeMBean";
    public static final String CAMPAIGN_BID_BEAN = "campaignBidMBean";
    public static final String CAMPAIGN_TRACKING_BEAN = "campaignTrackingMBean";
    public static final String CAMPAIGN_CONFIRMATION_BEAN = "campaignConfirmationMBean";
    public static final String CAMPAIGN_HISTORY_BEAN = "campaignHistoryMBean";
    public static final String PUBLICATION_MBEAN = "publicationMBean";
    public static final String PUBLICATION_NAVIGATION_BEAN = "publicationNavigationSessionBean";
    public static final String APPSETTINGS_MBEAN = "appSettingsMBean";
    public static final String SITESETTINGS_MBEAN = "siteSettingsMBean";
    public static final String APP_ADD_SLOTS_MBEAN = "appAddSlotsMBean";
    public static final String ACCOUNT_SESSION_BEAN = "accountSessionBean";
    public static final String AUDIENCE_MBEAN = "audienceMBean";
    public static final String AUDIENCE_SETUP_BEAN = "audienceSetupMBean";
    public static final String AUDIENCE_SOURCE_BEAN = "audienceSourceMBean";
    public static final String AUDIENCE_CONFIRMATION_BEAN = "audienceConfirmationMBean";
    public static final String AUDIENCE_SESSION_BEAN = "audienceSessionBean";
    public static final String AUDIENCE_NAVIGATION_BEAN = "audienceNavigationSessionBean";
    public static final String NAVIGATION_SESSION_BEAN = "navigationSessionBean";
    public static final String TOOLS_APPLICATION_BEAN = "toolsApplicationBean";

    public static final String CHART_BEAN = "chartMBean";
    public static final String REPORTING_BEAN = "reportingMBean";
    public static final String AGENCY_REPORTING_BEAN = "agencyReportingMBean";
    public static final String PUBLISHER_REPORTING_BEAN = "publisherReportingMBean";
    public static final String DATE_PICKER_BEAN = "datePickerMBean";

    /** User cookie. */
    public static final String LOGOUT_SESSION_KEY = "LOGOUT_SESSION_KEY";
    public static final String COOKIE_SELECTED_CAMPAIGNS = "c_selected_campaigns";
    public static final String COOKIE_SELECTED_PUBLICATIONS = "c_selected_publications";
    public static final String COOKIE_SELECTED_ADVERTISERS = "c_selected_advertisers";
    public static final String COOKIE_SELECTED_DATE = "c_selected_date";
    public static final String COOKIE_DATE_SELECTION = "c_date_selection";
    public static final String COOKIE_SELECTED_PRESET_DATE = "c_selected_preset_date";
    public static final String COOKIE_INDIVIDUAL_LINES = "c_individual_lines";
    public static final String COOKIE_DATE_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String CHARTS_DATE_FORMAT = "dd-MMM-yy";
    public static final String COOKIE_DONT_SHOW_DIALOG = "c_dont_show_dialog";
    public static final String COOKIE_CAMP_NAVIGATION = "campaignsNavigation";

    public static final String COOKIE_CAMPAIGN_STATUS = "c_status";
    public static final String COOKIE_CAMPAIGN_BIDTYPE = "c_bidtype";
    public static final String COOKIE_PUBLICATION_STATUS = "p_status";
    public static final String COOKIE_PLATFORM = "p_platform";
    public static final String COOKIE_APPROVAL = "p_approval";
    public static final String COOKIE_BACKFILL = "p_backfill";
    public static final String COOKIE_ADVERTISER_STATUS = "a_status";

    public static final String DEFAULT_AUTH_PAGE = "defaultPage";
    /** Date picker presetd */
    public static final String TODAY = "1";
    public static final String YESTERDAY = "2";
    public static final String LAST_7_DAYS = "3";
    public static final String THIS_MONTH = "4";
    public static final String LAST_MONTH = "5";

    /** Chart filter */
    public static final String HOUR = "HOUR";
    public static final String DAY = "DAY";
    public static final String WEEK = "WEEK";
    public static final String MONTH = "MONTH";
    public static final String ONE_DAY = "'1 day'";
    public static final String ONE_HOUR = "'1 hour'";
    public static final String THREE_HOUR = "'3 hour'";
    public static final String SIX_HOUR = "'6 hour'";
    public static final String TWELVE_HOUR = "'12 hour'";

    public static final String FILTER_BIDTYPE = "bidType";
    public static final String FILTER_STATUS = "status";

    public static final String FILTER_PLATFORM = "platform";
    public static final String FILTER_APPROVAL = "approval";
    public static final String FILTER_BACKFILL = "backfill";

    /** Chart intervals */
    public static final int LESS_THAN_TEN = 2;
    public static final int LESS_THAN_FIFTY = 5;
    public static final int LESS_THAN_HUNDRED = 10;

    /****/
    public static final String DASHBOARD_TABLE_CFG_XAXIS_JS = "META-INF/resources/scripts/cfg.axes.xaxis.js";
    public static final String DASHBOARD_TABLE_CFG_YAXIS_JS = "META-INF/resources/scripts/cfg.axes.yaxis.js";
    public static final String DASHBOARD_TABLE_CFG_GRID_JS = "META-INF/resources/scripts/cfg.grid.js";
    public static final String DASHBOARD_TABLE_CFG_HIGHLIGHTER_JS = "META-INF/resources/scripts/cfg.highlighter.js";
    public static final String DASHBOARD_TABLE_CFG_SERIES_JS = "META-INF/resources/scripts/cfg.series";

    /**
     * dashboard.xhtml dataTable column Id's to sortBy
     * **/
    public static final String DASHBOARD_DATATABLE_CAMPAIGNNAMEID = "campaignName";
    public static final String DASHBOARD_DATATABLE_BIDTTYPEID = "bidType";
    public static final String DASHBOARD_DATATABLE_BIDPRICEID = "bidPrice";
    public static final String DASHBOARD_DATATABLE_LIFETIMEBUDGETID = "totalBudget";
    public static final String DASHBOARD_DATATABLE_LIFETIMESPENDID = "totalSpend";
    public static final String DASHBOARD_DATATABLE_DAILYCAPID = "dailyCap";
    public static final String DASHBOARD_DATATABLE_SPENDYESTERDAYID = "spendYesterday";
    public static final String DASHBOARD_DATATABLE_CTRID = "ctr";
    public static final String DASHBOARD_DATATABLE_CVRID = "cvr";
    public static final String DASHBOARD_DATATABLE_SPENDID = "spend";
    public static final String DASHBOARD_DATATABLE_ECPAID = "cpa";
    public static final String DASHBOARD_DATATABLE_ECPMID = "cpm";

    /**
     * publisher dashboard dataTable column Id's to sortBy
     * **/
    public static final String PUB_DASHBOARD_DATATABLE_PUBLICATIONNAMEID = "publicationName";
    public static final String PUB_DASHBOARD_DATATABLE_IMPRESSIONSID = "impressions";
    public static final String PUB_DASHBOARD_DATATABLE_PLATFORMID = "platform";
    public static final String PUB_DASHBOARD_DATATABLE_APPROVALID = "approval";
    public static final String PUB_DASHBOARD_DATATABLE_BACKFILLID = "backfill";
    public static final String PUB_DASHBOARD_DATATABLE_REQUESTSID = "requests";
    public static final String PUB_DASHBOARD_DATATABLE_FILLRATEID = "fillRate";
    public static final String PUB_DASHBOARD_DATATABLE_REVENUEID = "revenue";
    public static final String PUB_DASHBOARD_DATATABLE_ECPMID = "ecpm";
    public static final String PUB_DASHBOARD_DATATABLE_CLICKSID = "clicks";
    public static final String PUB_DASHBOARD_DATATABLE_CTRID = "ctr";

    /**
     * agency console dashboard dataTable column Id's to sortBy
     * **/
    public static final String AC_DASHBOARD_DATATABLE_ADVERTISERNAMEID = "advertiserName";
    public static final String AC_DASHBOARD_DATATABLE_IMPRESSIONSID = "impressions";
    public static final String AC_DASHBOARD_DATATABLE_SPENDID = "spend";
    public static final String AC_DASHBOARD_DATATABLE_SPENDYESTERDAYID = "spendYesterday";
    public static final String AC_DASHBOARD_DATATABLE_BALANCEID = "balance";

    public static final String DEFAULT_NAVIGATION = "/WEB-INF/jsf/campaign/section_setup.xhtml";

    public static final String PUBLICATION_DEFAULT_NAVIGATION = "/WEB-INF/jsf/addpublication/section_new.xhtml";

    /** Recency types */
    // public static final String RECENCY_NONE = "None";
    // public static final String RECENCY_RANGE = "Range";
    // public static final String RECENCY_WINDOW = "Window";

    /** Report types */
    public static final String REPORT_SNAPSHOT = "snapshot";
    public static final String REPORT_CAMPAIGNS = "campaigns";
    public static final String REPORT_DEVICES = "devices";
    public static final String REPORT_BUDGETS = "budgets";
    public static final String REPORT_CREATIVES = "creatives";
    public static final String REPORT_LOCATIONS = "locations";
    public static final String REPORT_CONNECTIONS = "connections";

    /**
     * Navigation options
     * */
    public static final String NAVIGATE_TO = "navigateTo";
    public static final String NAVIGATE_FROM_CONFIRMATION = "navigateFromConfirmation";
    public static final String MENU_NAVIGATE_TO_SETUP = "setup";
    public static final String MENU_NAVIGATE_TO_SCHEDULING = "scheduling";
    public static final String MENU_NAVIGATE_TO_TARGETING = "targeting";
    public static final String MENU_NAVIGATE_TO_INVENTORY_TARGETING = "inventoryTargeting";
    public static final String MENU_NAVIGATE_TO_CREATIVE = "creative";
    public static final String MENU_NAVIGATE_TO_BIDDING = "bid";
    public static final String MENU_NAVIGATE_TO_TRACKING = "tracking";
    public static final String MENU_NAVIGATE_TO_CONFIRMATION = "confirmation";
    public static final String MENU_NAVIGATE_TO_HISTORY = "history";

    /**
     * Navigation status
     * */
    public static final int MENU_SETUP = 1;
    public static final int MENU_SCHEDULING = 2;
    public static final int MENU_TARGETING = 3;
    public static final int MENU_INVENTORY_TARGETING = 4;
    public static final int MENU_CREATIVE = 5;
    public static final int MENU_TRACKING = 6;
    public static final int MENU_BIDDING = 7;
    public static final int MENU_CONFIRMATION = 8;

    /**
     * Navigation options for publications
     * */
    public static final String MENU_NAVIGATE_TO_NEW = "new";
    public static final String MENU_NAVIGATE_TO_APP_SETTINGS = "appSettings";
    public static final String MENU_NAVIGATE_TO_SETTINGS = "settings";
    public static final String MENU_NAVIGATE_TO_SITE_SETTINGS = "siteSettings";
    public static final String MENU_NAVIGATE_TO_APP_ADSLOT = "appAddslot";

    /**
     * Navigation options for audiences
     * */
    public static final String AUDIENCE_MENU_NAVIGATE_TO_SETUP = "setup";
    public static final String AUDIENCE_MENU_NAVIGATE_TO_SOURCE = "source";
    public static final String AUDIENCE_MENU_NAVIGATE_TO_CONFIRMATION = "confirmation";
    public static final String AUDIENCE_SETUP_VIEW = "/WEB-INF/jsf/audience/section_setup.xhtml";
    public static final String AUDIENCE_SOURCE_VIEW = "/WEB-INF/jsf/audience/section_source.xhtml";
    public static final String AUDIENCE_CONFIRMATION_VIEW = "/WEB-INF/jsf/audience/section_confirmation.xhtml";
    public static final String AUDIENCE_DEFAULT_NAVIGATION = AUDIENCE_SETUP_VIEW;

    /**
     * Navigation options for reporting
     * */
    public static final String REPORTING_MENU_NAVIGATE_TO_CAMPAIGNS = REPORT_CAMPAIGNS;
    public static final String REPORTING_MENU_NAVIGATE_TO_DEVICES = REPORT_DEVICES;
    public static final String REPORTING_MENU_NAVIGATE_TO_BUDGETS = REPORT_BUDGETS;
    public static final String REPORTING_MENU_NAVIGATE_TO_CREATIVES = REPORT_CREATIVES;
    public static final String REPORTING_MENU_NAVIGATE_TO_LOCATIONS = REPORT_LOCATIONS;
    public static final String REPORTING_MENU_NAVIGATE_TO_CONNECTIONS = REPORT_CONNECTIONS;

    public static final String REPORTING_SNAPSHOT_VIEW = "/WEB-INF/jsf/reporting/advertiser/section_snapshot.xhtml";
    public static final String REPORTING_CAMPAIGNS_VIEW = "/WEB-INF/jsf/reporting/advertiser/section_campaigns.xhtml";
    public static final String REPORTING_DEVICES_VIEW = "/WEB-INF/jsf/reporting/advertiser/section_devices.xhtml";
    public static final String REPORTING_BUDGETS_VIEW = "/WEB-INF/jsf/reporting/advertiser/section_budgets.xhtml";
    public static final String REPORTING_CREATIVES_VIEW = "/WEB-INF/jsf/reporting/advertiser/section_creatives.xhtml";
    public static final String REPORTING_LOCATIONS_VIEW = "/WEB-INF/jsf/reporting/advertiser/section_locations.xhtml";
    public static final String REPORTING_CONNECTIONS_VIEW = "/WEB-INF/jsf/reporting/advertiser/section_connections.xhtml";

    /**
     * Navigation classes for reporting
     * */
    public static final String CLASS = "Class";
    public static final String REPORTING_CAMPAIGNS_CLASS = REPORTING_MENU_NAVIGATE_TO_CAMPAIGNS + CLASS;
    public static final String REPORTING_DEVICES_CLASS = REPORTING_MENU_NAVIGATE_TO_DEVICES + CLASS;
    public static final String REPORTING_BUDGETS_CLASS = REPORTING_MENU_NAVIGATE_TO_BUDGETS + CLASS;
    public static final String REPORTING_CREATIVES_CLASS = REPORTING_MENU_NAVIGATE_TO_CREATIVES + CLASS;
    public static final String REPORTING_LOCATIONS_CLASS = REPORTING_MENU_NAVIGATE_TO_LOCATIONS + CLASS;
    public static final String REPORTING_CONNECTIONS_CLASS = REPORTING_MENU_NAVIGATE_TO_CONNECTIONS + CLASS;

    /**
     * General Navigation options
     * */
    public static final String DASHBOARD = "dashboard";
    public static final String ADD_CAMPAIGN = "addcampaign";
    public static final String ADD_PUBLICATION = "addpublication";
    public static final String ADD_AUDIENCE = "addaudience";
    public static final String OPTIMISATION = "optimisation";
    public static final String AUDIENCE_BUILDER = "audiencebuilder";
    public static final String REPORTING = "reporting";
    public static final String TRANSACTIONS = "transactions";
    public static final String ACCOUNT_DETAILS = "accountdetails";

    /**
     * Device images
     * */
    public static final String DEFAULT_IMAGE = "default_image.gif";
    public static final String IPHONE_IMAGE = "iphone_line.jpg";
    public static final String IPAD_IMAGE = "ipad.png";
    public static final String ANDROID_IMAGE = "android_320x480.jpg";
    public static final String RIM_IMAGE = "RIM-Blackberry-bold.png";
    public static final String ELSE_IMAGE = "nokia-lumia-900.jpg";

    /**
     * Add publication
     * */
    public static final String MEDIUM_APPLICATION = "APPLICATION";
    public static final String MEDIUM_SITE = "SITE";
    public static final String ADSPACE_TEST_ID = "22222222-2222-2222-2222-222222222222";
    public static final String ADSPACE_ID = "adSpaceId";
    public static final String MOBILE_SITE = "MOBILE_SITE";

    /**
     * Creatives
     * */
    public static final String CREATIVE_INDEX = "creativeIndex";
    public static final String TEMPLATE_INDEX = "templateIndex";
    public static final String BEACON_INDEX = "beaconIndex";
    public static final String SSL_OVERRIDE = "sslOverride";
    public static final int COMPLETE_STATE = 0;
    public static final int INCOMPLETE_STATE = 1;
    public static final int ERROR_STATE = 2;
    public static final int MAX_BEACONS = 10;

    /** OPERA BROWSER */
    public static final String BROWSER_OPERA = "EXCLUDEOPERA";

    // CONVERSION TRACKING OPTIONS
    public static final String GOAL_CONVERSION = "goalconversion";
    public static final String NO_CONVERSION = "noconversion";
    public static final String APP_TRACKING = "apptracking";
    public static final String TRACKING_INSTALL_ONLYTRAFFIC_DEVICEIDS = "TRACKING_INSTALL_ONLYTRAFFIC_DEVICEIDS";
    public static final String TRACKING_INSTALL_ALLTRAFFIC_NODEVICEIDS = "TRACKING_INSTALL_ALLTRAFFIC_NODEVICEIDS";

    // INVENTORY TARGETING OPTIONS
    public static final String EXCHANGE_INVENTORY = "EXCHANGE_INVENTORY";
    public static final String APP_SITE_LIST = "WHITELIST";
    public static final String IAB_CATEGORY = "CATEGORY";
    public static final String PRIVATE_MARKET_PLACE = "PRIVATE_MARKET_PLACE";

    // REPORTING VIEW OPTIONS FOR CAMPAIGNS
    public static final String REPORTING_VIEW_OPTION_TOTAL = CampaignDetailedReportOption.TOTAL.name();
    public static final String REPORTING_VIEW_OPTION_SUMMARY = CampaignDetailedReportOption.SUMMARY.name();
    public static final String REPORTING_VIEW_OPTION_DAILY = CampaignDetailedReportOption.DAILY.name();
    public static final String REPORTING_VIEW_OPTION_HOURLY = CampaignDetailedReportOption.HOURLY.name();

    // REPORTING VIEW OPTIONS FOR DEVICES
    public static final String REPORTING_VIEW_OPTION_VENDOR = DeviceGrouping.VENDOR.name();
    public static final String REPORTING_VIEW_OPTION_PLATFORM = DeviceGrouping.PLATFORM.name();
    public static final String REPORTING_VIEW_OPTION_ALL = DeviceGrouping.ALL.name();

    // REPORTING LOCATION BREAKDOWN OPTIONS FOR DEVICES
    public static final String LOCATION_BREAKDOWN_OPTION_SUMMARY = LocationBreakdown.SUMMARY_BY_ALL.name();
    public static final String LOCATION_BREAKDOWN_OPTION_REGION = LocationBreakdown.REGION_BY_ALL.name();
    public static final String LOCATION_BREAKDOWN_OPTION_COUNTRY = LocationBreakdown.COUNTRY_BY_ALL.name();

    public static final String BUDGET_BREAKDOWN_OPTION_DAILY = BudgetBreakdown.DAILY.name();
    public static final String BUDGET_BREAKDOWN_OPTION_OVERALL = BudgetBreakdown.OVERALL.name();

    public static final String CALL = "CALL";
    public static final String AUDIO = "AUDIO";
    public static final String VIDEO = "VIDEO";
    public static final String ITUNES_STORE = "ITUNES_STORE";
    public static final String URL = "URL";
    public static final String IPHONE_APP_STORE = "IPHONE_APP_STORE";
    public static final String ANDROID = "ANDROID";
    public static final String UNKNOWN = "UNKNOWN";

    // BIDDING & BUDGET
    public static final String BID_DEDUCTION_INDEX = "bidDeductionIndex";
    public static final double MIN_BID = 0.001;
    public static final double MIN_CPI_BID = 2;
    public static final double MIN_CPA_BID = 5;
    public static final double MIN_DAILY_BUDGET = 10;

    // AUDIENCES
    public static final String EXCHANGE_INDEX = "exchangeIndex";
    public static final String AUDIENCE_COLLECTION_JAVASCRIPT_TAG = "<script type=\"text/javascript\" src=\"http://as.adfonic.net/adtruth/prefs.js\"></script>\n<script>var adtruth_data = fortyone.collect();</script>\n<script>\n\tdocument.write(\\\'<img width=1 height=1 src=\"http://ac.byyd.net/:ADVERTISER_EXTERNAL_ID/:AUDIENCE_EXTERNAL_ID.gif?d.adtruth_data=\\\' + adtruth_data + \\\'\"/>\\\');\n</script>";
    
    // AUDIENCE UPLOAD OPTIONS
    public static final String FILE_UPLOAD = "FILE_UPLOAD";
    public static final String S3_UPLOAD   = "S3_UPLOAD";
    
    // TARGETING
    public static final int METERS_IN_MILE = 1609;

    private Constants() {
        // Utility Class
    }

    public static Integer[] getHours() {
        return HOURS.clone();
    }

    public static Integer[] getWeeklyIndices() {
        return WEEKDAY_INDICES.clone();
    }
}

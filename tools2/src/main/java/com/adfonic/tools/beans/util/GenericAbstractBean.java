package com.adfonic.tools.beans.util;

import static com.adfonic.tools.beans.util.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.joda.time.DateTime;
import org.primefaces.model.chart.CartesianChartModel;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.dashboard.AgencyConsoleDashboardDto;
import com.adfonic.dto.dashboard.BaseDashboardDto;
import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.dashboard.PublisherDashboardDto;
import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.publication.typeahead.PublicationTypeAheadDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.account.AccountService;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.util.DateUtils;
import com.adfonic.tools.beans.application.ToolsApplicationBean;
import com.adfonic.tools.beans.audience.AudienceMBean;
import com.adfonic.tools.beans.audience.confirmation.AudienceConfirmationMBean;
import com.adfonic.tools.beans.audience.navigation.AudienceNavigationSessionBean;
import com.adfonic.tools.beans.audience.setup.AudienceSetupMBean;
import com.adfonic.tools.beans.audience.source.AudienceSourceMBean;
import com.adfonic.tools.beans.campaign.CampaignMBean;
import com.adfonic.tools.beans.campaign.bid.CampaignBidMBean;
import com.adfonic.tools.beans.campaign.confirmation.CampaignConfirmationMBean;
import com.adfonic.tools.beans.campaign.creative.CampaignCreativeMBean;
import com.adfonic.tools.beans.campaign.history.CampaignHistoryMBean;
import com.adfonic.tools.beans.campaign.inventory.CampaignInventoryTargetingMBean;
import com.adfonic.tools.beans.campaign.navigation.CampaignNavigationSessionBean;
import com.adfonic.tools.beans.campaign.scheduling.CampaignSchedulingMBean;
import com.adfonic.tools.beans.campaign.setup.CampaignSetupMBean;
import com.adfonic.tools.beans.campaign.targeting.CampaignTargetingMBean;
import com.adfonic.tools.beans.campaign.tracking.CampaignTrackingMBean;
import com.adfonic.tools.beans.dashboard.ChartMBean;
import com.adfonic.tools.beans.dashboard.DashBoardMBean;
import com.adfonic.tools.beans.dashboard.DatePickerMBean;
import com.adfonic.tools.beans.dashboard.agencyconsole.AgencyConsoleDashboardMBean;
import com.adfonic.tools.beans.dashboard.header.HeaderFiguresMBean;
import com.adfonic.tools.beans.dashboard.publisher.PublisherDashBoardMBean;
import com.adfonic.tools.beans.dashboard.reporting.AgencyReportingMBean;
import com.adfonic.tools.beans.dashboard.reporting.PublisherReportingMBean;
import com.adfonic.tools.beans.dashboard.reporting.ReportingMBean;
import com.adfonic.tools.beans.i18n.LanguageSessionBean;
import com.adfonic.tools.beans.js.ChartExpanderJs;
import com.adfonic.tools.beans.publication.PublicationMBean;
import com.adfonic.tools.beans.publication.app.AppAddSlotsMBean;
import com.adfonic.tools.beans.publication.app.AppSettingsMBean;
import com.adfonic.tools.beans.publication.app.SiteSettingsMBean;
import com.adfonic.tools.beans.publication.navigation.PublicationNavigationSessionBean;
import com.adfonic.tools.beans.session.AccountSessionBean;
import com.adfonic.tools.beans.session.NavigationSessionBean;
import com.adfonic.tools.beans.user.UserSessionBean;
import com.adfonic.tools.security.SecurityUtils;
import com.adfonic.util.TimeZoneUtils;

/**
 * The Class GenericBean will have common methods for the Managed Beans to use.
 */
public abstract class GenericAbstractBean {

    protected abstract void init() throws Exception;

    /**
     * Returns the UserDto from the UserSessionBean
     *
     * @return UserDTO bean with the user info.
     *
     **/
    protected UserDTO getUser() {
        FacesContext fc = FacesContext.getCurrentInstance();
        UserSessionBean bean = Utils.findBean(fc, USER_SESSION_BEAN);
        return ((UserDTO) bean.getMap().get(USERDTO));
    }

    /**
     * Return the Company Time Zone based on the user session bean
     */
    public TimeZone getCompanyTimeZone() {
        return TimeZoneUtils.getTimeZoneNonBlocking(getUser().getCompany().getDefaultTimeZoneId());
    }

    /**
     * Returns the AdfonicUserDto from the UserSessionBean
     *
     * @return AdfonicUserDto bean with the AdfonicUser info.
     *
     **/
    protected AdfonicUser getAdfonicUser() {
        FacesContext fc = FacesContext.getCurrentInstance();
        UserSessionBean bean = Utils.findBean(fc, USER_SESSION_BEAN);
        return ((AdfonicUser) bean.getMap().get(ADFONIC_USER));
    }

    /**
     * Copied from NavigationMBean, placed here for beans extending this class
     * to be able to determine this
     *
     * @return
     */
    public boolean isAdminUserLoggedIn() {
        if (SecurityUtils.getAdfonicUserFromSecurityContextHolder() == null) {
            return false;
        }
        return true;
    }

    // TODO DELETE METHOD WHEN IT EXTENDS FROM BASEDASHBOARDDTO
    protected void addCookiesToResponse(DashboardDto searchDto) {
        addCampaignCookie(searchDto.getCampaigns());
        addStringCookie(searchDto.getDatePickerPresetValue(), COOKIE_SELECTED_DATE);
    }

    protected void addCookiesToResponse(BaseDashboardDto searchDto) {
        if (searchDto instanceof PublisherDashboardDto) {
            addPublicationCookie(((PublisherDashboardDto) searchDto).getPublications());
        } else if (searchDto instanceof AgencyConsoleDashboardDto) {
            addAdvertisersCookie(((AgencyConsoleDashboardDto) searchDto).getAdvertisers());
        }
        // TODO FILL WITH CAMPAINGS WHEN IT EXTENDS FROM BASEDASHBOARDDTO
        // else{
        // addCampaignCookie(searchDto.getCampaigns());
        // }
        addStringCookie(searchDto.getDatePickerPresetValue(), COOKIE_SELECTED_DATE);
    }

    protected void addCampaignCookie(List<NameIdBusinessDto> campaigns) {
        if (!CollectionUtils.isEmpty(campaigns)) {
            String cookieValue = Utils.getCampaignCookieProperties(campaigns);
            Map<String, Object> props = new HashMap<String, Object>(0);
            props.put("maxAge", Integer.MAX_VALUE);
            props.put("path", "/");
            FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(COOKIE_SELECTED_CAMPAIGNS, cookieValue, props);
        } else {
            expireCookie(COOKIE_SELECTED_CAMPAIGNS);
        }
    }

    protected void addPublicationCookie(List<PublicationTypeAheadDto> publications) {
        if (!CollectionUtils.isEmpty(publications)) {
            String cookieValue = Utils.getPublicationCookieProperties(publications);
            Map<String, Object> props = new HashMap<String, Object>(0);
            props.put("maxAge", Integer.MAX_VALUE);
            props.put("path", "/");
            FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(COOKIE_SELECTED_PUBLICATIONS, cookieValue, props);
        } else {
            expireCookie(COOKIE_SELECTED_PUBLICATIONS);
        }
    }

    protected void addAdvertisersCookie(List<AdvertiserDto> advertisers) {
        if (!CollectionUtils.isEmpty(advertisers)) {
            String cookieValue = Utils.getAdvertiserCookieProperties(advertisers);
            Map<String, Object> props = new HashMap<String, Object>(0);
            props.put("maxAge", Integer.MAX_VALUE);
            props.put("path", "/");
            FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(COOKIE_SELECTED_ADVERTISERS, cookieValue, props);
        } else {
            expireCookie(COOKIE_SELECTED_ADVERTISERS);
        }
    }

    protected void addDateCookie(Date from, Date to) {
        String value = from.getTime() + "#" + to.getTime();
        Map<String, Object> props = new HashMap<String, Object>(0);
        props.put("maxAge", Integer.MAX_VALUE);
        props.put("path", "/");
        FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(COOKIE_SELECTED_DATE, value, props);
    }

    protected void addDontShowCookie(Boolean dontShowDialog) {
        if (dontShowDialog) {
            Map<String, Object> props = new HashMap<String, Object>(0);
            props.put("maxAge", Integer.MAX_VALUE);
            props.put("path", "/");
            FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(COOKIE_DONT_SHOW_DIALOG, "true", props);
        } else {
            expireCookie(COOKIE_DONT_SHOW_DIALOG);
        }

    }

    protected void addStringCookie(String filter, String name) {
        if (!StringUtils.isEmpty(filter)) {
            Map<String, Object> props = new HashMap<String, Object>(0);
            props.put("maxAge", Integer.MAX_VALUE);
            props.put("path", "/");
            FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(name, filter, props);
        } else {
            expireCookie(name);
        }
    }

    protected void expireCookie(String cookieName) {
        Map<String, Object> props = new HashMap<String, Object>(0);
        props.put("maxAge", 0);
        props.put("path", "/");
        FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(cookieName, "", props);

    }

    @SuppressWarnings("unchecked")
    protected <T> T getCookieValueFromRequest(Class<T> valueType, String cookieName) {
        Map<String, Object> cookiesMap = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();
        if (!CollectionUtils.isEmpty(cookiesMap)) {
            if (cookiesMap.get(cookieName) != null) {
                Cookie cookie = (Cookie) cookiesMap.get(cookieName);
                if (valueType.isArray() && valueType.getName().indexOf("String") != -1) {
                    return (T) cookie.getValue().split("#");
                } else if (valueType.isArray() && valueType.getName().indexOf("Date") != -1) {
                    String[] dates = cookie.getValue().split("#");
                    return (T) new Date[] { DateUtils.getDateFormatFromString(dates[0], COOKIE_DATE_FORMAT),
                            DateUtils.getDateFormatFromString(dates[1], COOKIE_DATE_FORMAT) };
                } else if (valueType.isAssignableFrom(java.lang.String.class)) {
                    return (T) cookie.getValue();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    protected Map<String, Object> getCookiesFromRequest() {
        Map<String, Object> cookiesMap = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();
        Map<String, Object> result = new HashMap<String, Object>(0);
        if (!CollectionUtils.isEmpty(cookiesMap)) {

            if (cookiesMap.get(COOKIE_SELECTED_CAMPAIGNS) != null) {
                Cookie cookie = (Cookie) cookiesMap.get(COOKIE_SELECTED_CAMPAIGNS);
                String[] cookiesValues = cookie.getValue().split("#");
                // format will be campaignId#campaignId#...
                result.put(COOKIE_SELECTED_CAMPAIGNS, cookiesValues);
            }

            if (cookiesMap.get(COOKIE_SELECTED_DATE) != null) {
                Cookie cookie = (Cookie) cookiesMap.get(COOKIE_SELECTED_DATE);
                // format will be dateFrom#dateTo#...
                String[] cookiesValues = cookie.getValue().split("#");
                Date dateFrom = DateUtils.getDateFormatFromString(cookiesValues[0], COOKIE_DATE_FORMAT);
                Date dateTo = DateUtils.getDateFormatFromString(cookiesValues[1], COOKIE_DATE_FORMAT);
                Date[] dateRes = new Date[] { dateFrom, dateTo };

                result.put(COOKIE_SELECTED_DATE, dateRes);
            }

        }
        return result;
    }

    /**
     * Evaluate expression which is assumed to be located in a file in the
     * classpath
     *
     * @param templatePath
     *            - reference to template file (such as
     *            /templates/some_template.html)
     * @param values
     *            - values that will be put into context for resolution
     * @return
     */
    public static String evaluateTemplate(FacesContext fc, InputStream templateStream, Map<String, Object> values) throws IOException {
        String expression = Streams.asString(templateStream);
        return evaluateTemplate(fc, expression, values);
    }

    public static String evaluateTemplate(FacesContext fc, String expression, Map<String, Object> values) {
        Application app = fc.getApplication();
        ELResolver resolver = app.getELResolver();
        ELContext context = fc.getELContext();
        if (values != null) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                resolver.setValue(context, null, entry.getKey(), entry.getValue());
            }
        }
        String text = app.evaluateExpressionGet(fc, expression, String.class);
        return text;
    }

    public String templateToString(String templateResource, Map<String, Object> values) throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        return evaluateTemplate(fc, getClass().getResourceAsStream(templateResource), values);
    }

    /**
     * Gets the scheme, hostname, port, and any URL prefix associated with the
     * running application, e.g. http://www.adfonic.com or
     * http://localhost:8080/adfonic-tools
     *
     * The output of this method will NOT have a trailing slash.
     */
    public static String getURLRoot() {
        return getURLRoot(true);
    }

    /**
     * Gets the scheme, hostname, port, and an optional context path associated
     * with the running application, e.g. http://www.adfonic.com or
     * http://localhost:8080/adfonic-tools
     *
     * The output of this method will NOT have a trailing slash.
     *
     * @param includeContextPath
     *            whether or not to include the context path for the running
     *            application (i.e. /adfonic-tools)
     */
    public static String getURLRoot(boolean includeContextPath) {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
        ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
        return getURLRoot(request, sc, includeContextPath);
    }

    public static String getURLRoot(HttpServletRequest request, ServletContext sc) {
        return getURLRoot(request, sc, true);
    }

    public static String getURLRoot(HttpServletRequest request, ServletContext sc, boolean includeContextPath) {
        StringBuilder out = new StringBuilder();
        String scheme = request.getScheme();
        out.append(request.getScheme()).append("://").append(request.getServerName());
        int port = request.getServerPort();
        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
            out.append(':').append(port);
        }
        if (includeContextPath) {
            out.append(sc.getContextPath());
        }
        return out.toString();
    }

    // Getting JSF instance beans

    protected LanguageSessionBean getLanguageSessionBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), LANGUAGE_SESSION_BEAN);
    }

    protected DashBoardMBean getDashboardMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), DASHBOAR_BEAN);
    }

    protected PublisherDashBoardMBean getPublisherDashboardMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), PUBLISHER_DASHBOAR_BEAN);
    }

    protected AgencyConsoleDashboardMBean getAgencyDashboardMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), AGENCY_DASHBOAR_BEAN);
    }

    protected HeaderFiguresMBean getHeaderFiguresMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), DASHBOARD_HEADER_FIGURESBEAN);
    }

    protected ChartMBean getChartMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CHART_BEAN);
    }

    protected ReportingMBean getReportingMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), REPORTING_BEAN);
    }

    protected DatePickerMBean getDatePickerMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), DATE_PICKER_BEAN);
    }

    protected PublisherReportingMBean getPublisherReportingMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), PUBLISHER_REPORTING_BEAN);
    }

    protected AgencyReportingMBean getAgencyReportingMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), AGENCY_REPORTING_BEAN);
    }

    protected ChartExpanderJs getChartExpanderJs() {
        return Utils.findBean(FacesContext.getCurrentInstance(), DATATABLE_EXPANDER_JS_BEAN);
    }

    protected CampaignMBean getCampaignMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_MBEAN);
    }

    protected CampaignSchedulingMBean getCampaignSchedulerBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_SCHEDULER_BEAN);
    }

    protected CampaignSetupMBean getCampaignSetupBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_SETUP_BEAN);
    }

    protected CampaignNavigationSessionBean getCNavigationBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_NAVIGATION_BEAN);
    }

    protected CampaignTargetingMBean getCampaignTargetingBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_TARGETING_BEAN);
    }

    protected CampaignInventoryTargetingMBean getCampaignInventoryTargetingBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_INVENTORY_TARGETING_BEAN);
    }

    protected CampaignCreativeMBean getCampaignCreativeBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_CREATIVE_BEAN);
    }

    protected CampaignBidMBean getCampaignBidMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_BID_BEAN);
    }

    protected CampaignTrackingMBean getCampaignTrackingMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_TRACKING_BEAN);
    }

    protected CampaignConfirmationMBean getCampaignConfirmationMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_CONFIRMATION_BEAN);
    }

    protected CampaignHistoryMBean getCampaignHistoryMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), CAMPAIGN_HISTORY_BEAN);
    }

    protected PublicationMBean getPublicationMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), PUBLICATION_MBEAN);
    }

    protected PublicationNavigationSessionBean getPublicationNavigationBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), PUBLICATION_NAVIGATION_BEAN);
    }

    protected AppSettingsMBean getAppSettingsMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), APPSETTINGS_MBEAN);
    }

    protected SiteSettingsMBean getSiteSettingsMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), SITESETTINGS_MBEAN);
    }

    protected AppAddSlotsMBean getAppAddSlotsMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), APP_ADD_SLOTS_MBEAN);
    }

    protected AccountSessionBean getAccountSessionBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), ACCOUNT_SESSION_BEAN);
    }

    protected AudienceMBean getAudienceMBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), AUDIENCE_MBEAN);
    }

    protected AudienceNavigationSessionBean getAudienceNavigationBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), AUDIENCE_NAVIGATION_BEAN);
    }

    protected AudienceSetupMBean getAudienceSetupBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), AUDIENCE_SETUP_BEAN);
    }

    protected AudienceSourceMBean getAudienceSourceBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), AUDIENCE_SOURCE_BEAN);
    }

    protected AudienceConfirmationMBean getAudienceConfirmationBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), AUDIENCE_CONFIRMATION_BEAN);
    }

    protected NavigationSessionBean getNavigationSessionBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), NAVIGATION_SESSION_BEAN);
    }

    protected UserSessionBean getUserSessionBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), USER_SESSION_BEAN);
    }

    protected ToolsApplicationBean getToolsApplicationBean() {
        return Utils.findBean(FacesContext.getCurrentInstance(), TOOLS_APPLICATION_BEAN);
    }

    protected void refreshHeaderAndChartsValues() {
        getChartMBean().setDashboardDto(getDashboardMBean().getDashboardDto());
        getChartMBean().setCacheDashboardDto(null);
        getChartMBean().setChartMap(new HashMap<String, CartesianChartModel>(0));

        getHeaderFiguresMBean().setDashboardDto(getDashboardMBean().getDashboardDto());
        getHeaderFiguresMBean().setCacheDashboardDto(null);
    }

    protected void refreshPublisherHeaderAndChartsValues() {
        getChartMBean().setPublisherDashboardDto(getPublisherDashboardMBean().getDashboardDto());
        getChartMBean().setPublisherCacheDashboardDto(null);
        getChartMBean().setChartMap(new HashMap<String, CartesianChartModel>(0));

        getHeaderFiguresMBean().setBaseDashboardDto(getPublisherDashboardMBean().getDashboardDto());
        getHeaderFiguresMBean().setCacheDashboardDto(null);
    }

    protected void refreshAgencyChartsValues() {
        getChartMBean().setAgencyDashboardDto(getAgencyDashboardMBean().getDashboardDto());
        getChartMBean().setCacheAgencyDashboardDto(null);
        getChartMBean().setChartMap(new HashMap<String, CartesianChartModel>(0));
    }

    protected void refreshDates(Date from, Date to, String dateSelection) {
        DateTime dtTo = new DateTime(to);
        DateTime d23 = dtTo.withHourOfDay(23).withMinuteOfHour(59);
        DateTime dtFrom = new DateTime(from);
        DateTime d0 = dtFrom.withHourOfDay(0).withMinuteOfHour(0);
        if (getDashboardMBean() != null && getDashboardMBean().getDashboardDto() != null) {
            getDashboardMBean().getDashboardDto().setFrom(d0.toDate());
            getDashboardMBean().getDashboardDto().setTo(d23.toDate());
            getDashboardMBean().getDashboardDto().setDatePickerPresetValue(dateSelection);
            refreshHeaderAndChartsValues();
            refreshTableValues();
        }
        if (getPublisherDashboardMBean() != null && getPublisherDashboardMBean().getDashboardDto() != null) {
            getPublisherDashboardMBean().getDashboardDto().setFrom(d0.toDate());
            getPublisherDashboardMBean().getDashboardDto().setTo(d23.toDate());
            getPublisherDashboardMBean().getDashboardDto().setDatePickerPresetValue(dateSelection);
            refreshPublisherHeaderAndChartsValues();
            refreshPublisherTableValues();
        }
        if (getAgencyDashboardMBean() != null && getAgencyDashboardMBean().getDashboardDto() != null) {
            getAgencyDashboardMBean().getDashboardDto().setFrom(d0.toDate());
            getAgencyDashboardMBean().getDashboardDto().setTo(d23.toDate());
            getAgencyDashboardMBean().getDashboardDto().setDatePickerPresetValue(dateSelection);
            refreshAgencyChartsValues();
            refreshAgencyTableValues();
        }
    }

    protected void refreshTableValues() {
        getReportingMBean().setDashboardDto(getDashboardMBean().getDashboardDto());
        getReportingMBean().setCacheDashboardDto(null);
    }

    protected void refreshPublisherTableValues() {
        getPublisherReportingMBean().setDashboardDto(getPublisherDashboardMBean().getDashboardDto());
        getPublisherReportingMBean().setCacheDashboardDto(null);
    }

    protected void refreshAgencyTableValues() {
        getAgencyReportingMBean().setDashboardDto(getAgencyDashboardMBean().getDashboardDto());
        getAgencyReportingMBean().setCacheDashboardDto(null);
    }

    protected void updateCampaignBeans(CampaignDto dto) {
        getCampaignMBean().setCampaignDto(dto);
        getCampaignSetupBean().loadCampaign(dto);
        getCampaignSchedulerBean().loadCampaignDto(dto);
        getCampaignTargetingBean().loadCampaignDto(dto);
        getCampaignInventoryTargetingBean().loadCampaign(dto);
        getCampaignCreativeBean().loadCampaignDto(dto);
        getCampaignBidMBean().loadCampaignDto(dto);
        getCampaignTrackingMBean().loadCampaignDto(dto);
        getCampaignConfirmationMBean().setCampaignDto(dto);
        getCampaignConfirmationMBean().setCampaignCreativeDto(getCampaignCreativeBean().getCampaignDto());
        getCampaignHistoryMBean().setCampaignDto(dto);
    }

    protected void updatePublicationBeans(PublicationDto dto) {
        getPublicationMBean().setPublicationDto(dto);
        getAppSettingsMBean().loadDto(dto);
        getSiteSettingsMBean().loadDto(dto);
        getAppAddSlotsMBean().setPublicationDto(dto);
    }

    protected void updateAudienceBeans(AudienceDto dto) {
        getAudienceMBean().setAudienceDto(dto);
        getAudienceSetupBean().loadAudienceDto(dto);
        getAudienceSourceBean().loadAudienceDto(dto);
        getAudienceConfirmationBean().setAudienceDto(dto);
    }

    protected StopWatch startWatch() {
        StopWatch stWatch = new StopWatch();
        stWatch.start();
        return stWatch;

    }

    // Inventory targeting methods

    /**
     * Checks weather the given text is equal to 'IAB Category' string
     * representation
     */
    protected boolean isIABCategory(String value) {
        return IAB_CATEGORY.equals(value) ? true : false;
    }

    /**
     * Checks weather the given text is equal to 'Exchange Inventory' string
     * representation
     */
    protected boolean isExchangeInventory(String value) {
        return EXCHANGE_INVENTORY.equals(value) ? true : false;
    }

    /**
     * Checks weather the given text is equal to 'App/Site List' string
     * representation
     */
    protected boolean isAppSiteList(String value) {
        return APP_SITE_LIST.equals(value) ? true : false;
    }

    /**
     * Checks weather the given text is equal to 'Private Marketplace' string
     * representation
     */
    protected boolean isPrivateMarketPlace(String value) {
        return PRIVATE_MARKET_PLACE.equals(value) ? true : false;
    }

    /**
     * TODO: We should check whether this is equivalent with
     * {@link #getCompanyTimeZone()}
     */
    protected TimeZone getCompanyTimeZone(CompanyService companyService) {
        return companyService.getCompanyForAdvertiser(getUser().getAdvertiserDto()).getTimeZone();
    }

    // Common campaign methods

    protected Collection<CampaignTypeAheadDto> getAdvertiserCampaigns(CampaignService campaignService, String query) {
        CampaignSearchDto dto = new CampaignSearchDto();
        if (query != null) {
            dto.setName(query);
        }
        dto.setAdvertiser(getUser().getAdvertiserDto());
        return campaignService.getCampaigns(dto).getCampaigns();
    }

    protected Collection<CampaignTypeAheadDto> getAdvertiserCampaigns(CampaignService campaignService) {
        return getAdvertiserCampaigns(campaignService, null);
    }
    
    // Common account methods
    
    protected Double getAccountDailyBudget(AccountService accountService) {
        return accountService.getAccountDailyBudget(getUser().getAdvertiserDto());
    }

    // User generic methods

    protected Locale getUserLocale() {
        return getLanguageSessionBean().getLocale();
    }

    // Common generic methods

    /**
     * TODO: We should check this should be better or not:
     * Calendar.getInstance(getCompanyTimeZone()).getTime()
     */
    protected Date returnNow() {
        return new Date();
    }

    protected String getDisplayValueNotSetOrUndefined(String param) {
        return (param != null && !param.isEmpty()) ? param : notSet();
    }

    protected String notSet() {
        return FacesUtils.getBundleMessage("page.campaign.menu.noneset.label");
    }

    protected UIComponent getUIComponent(String componentName) {
        return FacesContext.getCurrentInstance().getViewRoot().findComponent(componentName);
    }
}

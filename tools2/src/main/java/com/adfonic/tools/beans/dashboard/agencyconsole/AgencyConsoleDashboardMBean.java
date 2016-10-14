package com.adfonic.tools.beans.dashboard.agencyconsole;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.campaign.bidding.CurrencyExchangeRateDto;
import com.adfonic.dto.dashboard.AgencyConsoleDashboardDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.currencyexchangerate.CurrencyExchangeRateService;
import com.adfonic.tools.beans.dashboard.ChartMBean;
import com.adfonic.tools.beans.dashboard.reporting.AgencyReportingMBean;
import com.adfonic.tools.beans.js.ChartExpanderJs;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "dashboard-agency", pattern = "/dashboard/agencyconsole", viewId = "/WEB-INF/jsf/dashboard/agencyconsole/dashboard.jsf") })
public class AgencyConsoleDashboardMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private AgencyConsoleDashboardDto dashboardDto = new AgencyConsoleDashboardDto();

    @Autowired
    @Qualifier("companyService")
    private CompanyService cService;
    
    @Autowired
    private CurrencyExchangeRateService currencyExchangeRateService;

    @Autowired
    private AgencyReportingMBean reportingBean;

    @Autowired
    private ChartExpanderJs expanderJs;

    @Autowired
    private ChartMBean chartBean;

    private AdvertiserDto newAdvertiser = new AdvertiserDto();
    
    // MAD-3303 - Default currency per client
    private Long defaultCurrencyId = 0L;
    private Map<Long, CurrencyExchangeRateDto> currenciesMap;

    /***
     * Method invoke everytime the user gets into the session to check if
     * there's any previous cookie with the latest searches. If there are
     * cookies with the searches, we should provide those searches when loading
     * page
     * */
    @Override
    @URLActions(actions = { @URLAction(mappingId = "dashboard-agency") })
    public void init() {
        getNavigationSessionBean().navigate(Constants.DASHBOARD);
        doCookiesCheck();
        prepareDashBoardDto(dashboardDto);

        if (reportingBean == null) {
            reportingBean = Utils.findBean(FacesContext.getCurrentInstance(), "agencyReportingMBean");
        }
        if (chartBean == null) {
            chartBean = Utils.findBean(FacesContext.getCurrentInstance(), "chartBean");
        }
        reportingBean.setDashboardDto(dashboardDto);
        chartBean.setAgencyDashboardDto(dashboardDto);
        getAllCurrenciesAsOrderedMap();
    }
    
    private void getAllCurrenciesAsOrderedMap() {
        List<CurrencyExchangeRateDto> currencyExchangeRateDtos = this.currencyExchangeRateService.getAllCurrencyExchangeRate();
        if (currencyExchangeRateDtos!=null){
            this.currenciesMap = new LinkedHashMap<Long, CurrencyExchangeRateDto>(currencyExchangeRateDtos.size());
            for(CurrencyExchangeRateDto currencyExchangeRateDto : currencyExchangeRateDtos){
                this.currenciesMap.put(currencyExchangeRateDto.getId(), currencyExchangeRateDto);
            }
        } 
    }

    public Collection<AdvertiserDto> complete(String query) {
        UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        return cService.doQuery(query, userDto.getCompany().getId());
    }

    /***
     * Method invoke to filter the dashboard with the publications selected and
     * date. Will also add the cookies if campaign and or date are not the
     * default ones.
     * */
    public void advertiserFilter(ActionEvent event) {
        if (event.getComponent().getId().equals("applyAdvertiserButtonId")) {
            removeTableFilters();
        }
        addCookiesToResponse(dashboardDto);
        if (!CollectionUtils.isEmpty(dashboardDto.getAdvertisers())) {
            dashboardDto.setAdvertisers(Utils.removeDuplicated(dashboardDto.getAdvertisers()));
            List<Long> ids = new ArrayList<Long>();
            for (AdvertiserDto n : dashboardDto.getAdvertisers()) {
                ids.add(n.getId());
            }
            dashboardDto.setAdvertisersIdFiltered(ids);
        }
        refreshAgencyChartsValues();
        refreshAgencyTableValues();
    }

    /***
     * Gets the cookies from the request and adds to the DashboardSearchDto the
     * init params needed.
     * */
    private void doCookiesCheck() {
        // Only use the cookie thing with the first in from the user
        String[] advertiserIds = getCookieValueFromRequest(String[].class, Constants.COOKIE_SELECTED_ADVERTISERS);
        if (advertiserIds != null) {
            if (dashboardDto.getAdvertisers() == null) {
                dashboardDto.setAdvertisers(new ArrayList<AdvertiserDto>());
            }
            dashboardDto.getAdvertisers().clear();
            dashboardDto.getAdvertisers().addAll(cService.getAdvertisersById(advertiserIds));

        }
        String selection = getCookieValueFromRequest(String.class, Constants.COOKIE_DATE_SELECTION);
        Date[] dates;
        if (selection != null) {
            dates = Utils.getDateRange(selection);
            getDatePickerMBean().setPreviousSelection(selection);
        } else {
            dates = Utils.getDateRange(getDatePickerMBean().getPreviousSelection());
        }
        dashboardDto.setFrom(dates[0]);
        dashboardDto.setTo(dates[1]);
        getDatePickerMBean().setFrom(dates[0]);
        getDatePickerMBean().setTo(dates[1]);
        dashboardDto.setDatePickerPresetValue(getDatePickerMBean().getPreviousSelection());
        String datePickerPreset = getCookieValueFromRequest(String.class, Constants.COOKIE_SELECTED_PRESET_DATE);
        if (!StringUtils.isEmpty(datePickerPreset)) {
            dashboardDto.setDatePickerPresetValue(datePickerPreset);
        }
        String status = getCookieValueFromRequest(String.class, Constants.COOKIE_ADVERTISER_STATUS);
        if (!StringUtils.isEmpty(status)) {
            if (status.equals("All")) {
                dashboardDto.setStatusFiltered(false);
                dashboardDto.setStatusFilter(AdvertiserStatus.ALL);
            } else {
                dashboardDto.setStatusFiltered(true);
                dashboardDto.setStatusFilter(AdvertiserStatus.fromString(status));
            }
        } else {
            dashboardDto.setStatusFiltered(true);
            dashboardDto.setStatusFilter(AdvertiserStatus.ACTIVE);
        }
    }

    public void createAdvertiser(ActionEvent event) throws Exception {
        UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        List<AdvertiserDto> lAdvertisers = cService.getAdvertisersForCompany(userDto.getCompany());
        for (AdvertiserDto a : lAdvertisers) {
            if (a.getName().equals(newAdvertiser.getName())) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, "ad-name", null, "page.dashboard.labels.advertiser.name.duplicated.message");
                return;
            }
        }
        AdvertiserDto advertiser = cService.newAdvertiserDto(userDto.getCompany().getId(), newAdvertiser);
        // MAD-3303 - Default currency per client
        if (this.defaultCurrencyId!=0L){
            advertiser = cService.setAdvertiserDefaultCurrency(advertiser.getId(), this.defaultCurrencyId);
        }
        userDto.getAdvertiserListDto().add(advertiser);
        newAdvertiser = new AdvertiserDto();
        if (event.getComponent().getId().equals("gotoButton")) {
            userDto.setAdvertiserDto(advertiser);
        } else {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("newAdvertiserDialog.hide();");
        }
        refreshAgencyTableValues();
    }

    public String createAdvertiserNavigate() throws Exception {
        UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        for (AdvertiserDto a : userDto.getAdvertiserListDto()) {
            if (a.getName().equals(newAdvertiser.getName())) {
                return null;
            }
        }
        return "pretty:dashboard-advertiser";
    }

    public boolean isChartTimeFilterAvailable(String type) {
        Date from = dashboardDto.getFrom();
        Date to = dashboardDto.getTo();
        Map<String, Boolean> res = Utils.getChartPeriodAvailable(new Date[] { from, to });
        return res.containsKey(type);
    }

    private void prepareDashBoardDto(AgencyConsoleDashboardDto dashboardDto) {
        Date[] dates = new Date[] { dashboardDto.getFrom(), dashboardDto.getTo() };
        dates = Utils.getNormalizedDate(dates);

        dashboardDto.setFrom(dates[0]);
        dashboardDto.setTo(dates[1]);
    }

    public void navigateToAdvertiser(ActionEvent event) {
        long advId = Long.valueOf((Long) event.getComponent().getAttributes().get("advertiserId"));
        AdvertiserDto dto = cService.getAdvertiserById(advId);
        UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        userDto.setAdvertiserDto(dto);
    }

    public Date getToday() {
        return returnNow();
    }

    public Date getMinDate() {
        DateTime dt = new DateTime();
        dt = dt.minusDays(15);
        return dt.toDate();
    }

    public List<AdvertiserStatus> getStatusList() {
        List<AdvertiserStatus> statusList = new ArrayList<AdvertiserStatus>();
        statusList.add(AdvertiserStatus.ACTIVE);
        statusList.add(AdvertiserStatus.INACTIVE);

        return statusList;
    }

    public AgencyReportingMBean getReportingBean() {
        return reportingBean;
    }

    public void setReportingBean(AgencyReportingMBean reportingBean) {
        this.reportingBean = reportingBean;
    }

    public AgencyConsoleDashboardDto getDashboardDto() {
        return dashboardDto;
    }

    public void setDashboardDto(AgencyConsoleDashboardDto dashboardDto) {
        this.dashboardDto = dashboardDto;
    }

    public ChartExpanderJs getExpanderJs() {
        return expanderJs;
    }

    public void setExpanderJs(ChartExpanderJs expanderJs) {
        this.expanderJs = expanderJs;
    }

    public ChartMBean getChartBean() {
        return chartBean;
    }

    public void setChartBean(ChartMBean chartBean) {
        this.chartBean = chartBean;
    }

    private void removeTableFilters() {
        dashboardDto.setStatusFiltered(false);
        dashboardDto.setStatusFilter(AdvertiserStatus.ALL);

    }

    public AdvertiserDto getNewAdvertiser() {
        return newAdvertiser;
    }

    public void setNewAdvertiser(AdvertiserDto newAdvertiser) {
        this.newAdvertiser = newAdvertiser;
    }
    
    public Long getDefaultCurrencyId() {
        return defaultCurrencyId;
    }

    public void setDefaultCurrencyId(Long defaultCurrencyId) {
        this.defaultCurrencyId = defaultCurrencyId;
    }
    
    public List<Long> getCurrencyIds(){
        return new ArrayList<Long>(this.currenciesMap.keySet());
    }
    
    public CurrencyExchangeRateDto getCurrency(Long id) {
        return this.currenciesMap.get(id);
    }
}

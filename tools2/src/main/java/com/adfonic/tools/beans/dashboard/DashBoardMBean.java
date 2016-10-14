package com.adfonic.tools.beans.dashboard;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.tools.beans.dashboard.header.HeaderFiguresMBean;
import com.adfonic.tools.beans.dashboard.reporting.ReportingMBean;
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
@URLMappings(mappings = { @URLMapping(id = "dashboard-advertiser", pattern = "/dashboard/advertiser", viewId = "/WEB-INF/jsf/dashboard/dashboard.jsf") })
public class DashBoardMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private DashboardDto dashboardDto = new DashboardDto();

    @Autowired
    @Qualifier("campaignService")
    private CampaignService cService;

    @Autowired
    private ReportingMBean reportingBean;

    @Autowired
    private ChartExpanderJs expanderJs;

    @Autowired
    private ChartMBean chartBean;

    @Autowired
    private HeaderFiguresMBean headerFigures;

    private Date previousFrom;

    private Date previousTo;

    private String previousSelection;

    /***
     * Method invoke everytime the user gets into the session to check if
     * there's any previous cookie with the latest searches. If there are
     * cookies with the searches, we should provide those searches when loading
     * page
     * */
    @Override
    @URLActions(actions = { @URLAction(mappingId = "dashboard-advertiser") })
    public void init() {
        getNavigationSessionBean().navigate(Constants.DASHBOARD);
        doCookiesCheck();
        RequestContext.getCurrentInstance().execute("progressDialog.hide()");
        prepareDashBoardDto(dashboardDto);
        previousFrom = dashboardDto.getFrom();
        previousTo = dashboardDto.getTo();
        if (reportingBean == null) {
            reportingBean = Utils.findBean(FacesContext.getCurrentInstance(), "reportingMBean");
        }
        if (headerFigures == null) {
            headerFigures = Utils.findBean(FacesContext.getCurrentInstance(), "headerFiguresMBean");

        }
        if (chartBean == null) {
            chartBean = Utils.findBean(FacesContext.getCurrentInstance(), "chartBean");
        }
        reportingBean.setDashboardDto(dashboardDto);
        headerFigures.setDashboardDto(dashboardDto);
        chartBean.setDashboardDto(dashboardDto);
    }

    public Collection<CampaignTypeAheadDto> complete(String query) {
        // fire event to add the cookie for next time
        return getAdvertiserCampaigns(cService, query);
    }

    public void campaignChanges(org.primefaces.event.SelectEvent event) {
        NameIdBusinessDto campaign = (NameIdBusinessDto) event.getObject();
        if (!dashboardDto.getCampaigns().contains(campaign)) {
            dashboardDto.getCampaigns().add(campaign);
        }
    }

    public void campaignChanges(org.primefaces.event.UnselectEvent event) {
        NameIdBusinessDto campaign = (NameIdBusinessDto) event.getObject();
        if (dashboardDto.getCampaigns() != null && dashboardDto.getCampaigns().contains(campaign)) {
            dashboardDto.getCampaigns().remove(campaign);
        }
    }

    /***
     * Method invoke to filter the dashboard with the campaigns selected and
     * date. Will also add the cookies if campaign and or date are not the
     * default ones.
     * */
    public void campaignFilter(ActionEvent event) {
        if (event.getComponent().getId().equals("applyCampaignButtonId")) {
            removeTableFilters();
        }
        addCookiesToResponse(dashboardDto);
        if (!CollectionUtils.isEmpty(dashboardDto.getCampaigns())) {
            dashboardDto.setCampaigns(Utils.removeDuplicated(dashboardDto.getCampaigns()));
            List<Long> ids = new ArrayList<Long>();
            for (NameIdBusinessDto n : dashboardDto.getCampaigns()) {
                ids.add(n.getId());
            }
            dashboardDto.setCampaignsIdFiltered(ids);
        }
        refreshHeaderAndChartsValues();
        refreshTableValues();
    }

    public String loadCampaign(String campaignname) throws UnsupportedEncodingException {
        getCNavigationBean().setEncodedId(URLEncoder.encode(campaignname, "UTF-8"));
        return "pretty:campaignSetup";
    }

    /***
     * Gets the cookies from the request and adds to the DashboardSearchDto the
     * init params needed.
     * */
    private void doCookiesCheck() {
        // Only use the cookie thing with the first in from the user
        String[] campaignIds = getCookieValueFromRequest(String[].class, Constants.COOKIE_SELECTED_CAMPAIGNS);
        if (campaignIds != null) {
            UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
            if (dashboardDto.getCampaigns() == null) {
                dashboardDto.setCampaigns(new ArrayList<NameIdBusinessDto>());
            }
            dashboardDto.getCampaigns().clear();
            List<CampaignDto> lCampaigns = cService.getCampaignsById(campaignIds, userDto.getAdvertiserDto().getId());
            if (!CollectionUtils.isEmpty(lCampaigns)) {
                dashboardDto.getCampaigns().addAll(lCampaigns);
            } else {
                addCampaignCookie(null);
            }

        }
        String oldsel = getCookieValueFromRequest(String.class, Constants.COOKIE_SELECTED_PRESET_DATE);
        if (!StringUtils.isEmpty(oldsel)) {

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
        String status = getCookieValueFromRequest(String.class, Constants.COOKIE_CAMPAIGN_STATUS);
        if (!StringUtils.isEmpty(status)) {
            if (status.equals("All")) {
                dashboardDto.setStatusFiltered(false);
                dashboardDto.setCampaignStatusFilter(CampaignStatus.ALL);
            } else {
                dashboardDto.setCampaignStatusFilter(CampaignStatus.fromString(status));
                dashboardDto.setStatusFiltered(true);
            }
        } else {
            dashboardDto.setStatusFiltered(true);
            dashboardDto.setCampaignStatusFilter(CampaignStatus.ACTIVE);
        }
        String bidType = getCookieValueFromRequest(String.class, Constants.COOKIE_CAMPAIGN_BIDTYPE);
        if (!StringUtils.isEmpty(bidType)) {
            dashboardDto.setBidTypeFiltered(true);
            dashboardDto.setBidTypeFilter(BidType.value(bidType));
        }
    }

    public boolean isChartTimeFilterAvailable(String type) {
        Date from = dashboardDto.getFrom();
        Date to = dashboardDto.getTo();
        Map<String, Boolean> res = Utils.getChartPeriodAvailable(new Date[] { from, to });
        return res.containsKey(type);
    }

    private void prepareDashBoardDto(DashboardDto dashboardDto) {
        dashboardDto.setAdvertiser(getUser().getAdvertiserDto());
        Date[] dates = new Date[] { dashboardDto.getFrom(), dashboardDto.getTo() };
        dates = Utils.getNormalizedDate(dates);

        dashboardDto.setFrom(dates[0]);
        dashboardDto.setTo(dates[1]);
    }

    public boolean isMoreThan5() {
        if (dashboardDto != null) {
            if (!CollectionUtils.isEmpty(dashboardDto.getCampaigns()) && dashboardDto.getCampaigns().size() > 5) {
                return true;
            }
        }

        return false;
    }

    public String getExternalIdFromId(long id) {
        CampaignDto dto = cService.getCampaignByIdWithExternal(id);
        if (dto == null) {
            return "";
        }
        return dto.getExternalID();
    }

    public Date getToday() {
        return returnNow();
    }

    public Date getMinDate() {
        DateTime dt = new DateTime();
        dt = dt.minusDays(14);
        return dt.toDate();
    }

    public ReportingMBean getReportingBean() {
        return reportingBean;
    }

    public void setReportingBean(ReportingMBean reportingBean) {
        this.reportingBean = reportingBean;
    }

    public DashboardDto getDashboardDto() {
        return dashboardDto;
    }

    public void setDashboardDto(DashboardDto dashboardDto) {
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

    public HeaderFiguresMBean getHeaderFigures() {
        return headerFigures;
    }

    public void setHeaderFigures(HeaderFiguresMBean headerFigures) {
        this.headerFigures = headerFigures;
    }

    public Date getPreviousFrom() {
        return previousFrom;
    }

    public void setPreviousFrom(Date previousFrom) {
        this.previousFrom = previousFrom;
    }

    public Date getPreviousTo() {
        return previousTo;
    }

    public void setPreviousTo(Date previousTo) {
        this.previousTo = previousTo;
    }

    public String getPreviousSelection() {
        return previousSelection;
    }

    public void setPreviousSelection(String previousSelection) {
        this.previousSelection = previousSelection;
    }

    private void removeTableFilters() {
        dashboardDto.setStatusFiltered(false);
        dashboardDto.setCampaignStatusFilter(CampaignStatus.ALL);
        addStringCookie("All", Constants.COOKIE_CAMPAIGN_STATUS);
        dashboardDto.setBidTypeFiltered(false);
        dashboardDto.setBidTypeFilter(BidType.ALL);
        addStringCookie(null, Constants.COOKIE_CAMPAIGN_BIDTYPE);
    }
}

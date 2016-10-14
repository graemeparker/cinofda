package com.adfonic.tools.beans.dashboard.publisher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.dto.dashboard.PublisherDashboardDto;
import com.adfonic.dto.dashboard.statistic.PublisherStatisticsDto;
import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.publication.enums.Approval;
import com.adfonic.dto.publication.enums.Backfill;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.dto.publication.search.PublicationSearchDto;
import com.adfonic.dto.publication.typeahead.PublicationTypeAheadDto;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.tools.beans.dashboard.ChartMBean;
import com.adfonic.tools.beans.dashboard.header.HeaderFiguresMBean;
import com.adfonic.tools.beans.dashboard.reporting.PublisherReportingMBean;
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
@URLMappings(mappings = { @URLMapping(id = "dashboard-publisher", pattern = "/dashboard/publisher", viewId = "/WEB-INF/jsf/publisher/dashboard/dashboard.jsf") })
public class PublisherDashBoardMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private PublisherDashboardDto dashboardDto = new PublisherDashboardDto();

    @Autowired
    @Qualifier("publicationService")
    private PublicationService pService;

    @Autowired
    private PublisherReportingMBean reportingBean;

    @Autowired
    private ChartExpanderJs expanderJs;

    @Autowired
    private ChartMBean chartBean;

    @Autowired
    private HeaderFiguresMBean headerFigures;

    /***
     * Method invoke everytime the user gets into the session to check if
     * there's any previous cookie with the latest searches. If there are
     * cookies with the searches, we should provide those searches when loading
     * page
     * */
    @Override
    @URLActions(actions = { @URLAction(mappingId = "dashboard-publisher") })
    public void init() {
        getNavigationSessionBean().navigate(Constants.DASHBOARD);
        doCookiesCheck();
        prepareDashBoardDto(dashboardDto);
        // previousFrom = dashboardDto.getFrom();
        // previousTo = dashboardDto.getTo();

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
        headerFigures.setBaseDashboardDto(dashboardDto);
        chartBean.setPublisherDashboardDto(dashboardDto);
    }

    public Collection<PublicationTypeAheadDto> complete(String query) {
        PublicationSearchDto dto = new PublicationSearchDto();
        dto.setName(query);
        dto.setPublisher(getUser().getPublisherDto());
        // fire event to add the cookie for next time
        return pService.getPublications(dto).getPublications();
    }

    /***
     * Method invoke to filter the dashboard with the publications selected and
     * date. Will also add the cookies if campaign and or date are not the
     * default ones.
     * */
    public void publicationFilter(ActionEvent event) {
        if (event.getComponent().getId().equals("applyPublicationButtonId")) {
            removeTableFilters();
        }
        addCookiesToResponse(dashboardDto);
        if (!CollectionUtils.isEmpty(dashboardDto.getPublications())) {
            dashboardDto.setPublications(Utils.removeDuplicated(dashboardDto.getPublications()));
            List<Long> ids = new ArrayList<Long>();
            for (PublicationTypeAheadDto n : dashboardDto.getPublications()) {
                ids.add(n.getId());
            }
            dashboardDto.setPublicationsIdFiltered(ids);
        }
        refreshPublisherHeaderAndChartsValues();
        refreshPublisherTableValues();
    }

    public String getExternalIdFromId(long id) {
        PublicationDto dto = pService.getPublicationById(id);
        if (dto == null) {
            return "";
        }
        return dto.getExternalID();
    }

    /***
     * Gets the cookies from the request and adds to the DashboardSearchDto the
     * init params needed.
     * */
    private void doCookiesCheck() {
        // Only use the cookie thing with the first in from the user
        String[] publicationIds = getCookieValueFromRequest(String[].class, Constants.COOKIE_SELECTED_PUBLICATIONS);
        if (publicationIds != null) {
            if (dashboardDto.getPublications() == null) {
                dashboardDto.setPublications(new ArrayList<PublicationTypeAheadDto>());
            }
            dashboardDto.getPublications().clear();
            dashboardDto.getPublications().addAll(pService.getPublicationsById(publicationIds));

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
        String datePickerPreset = getCookieValueFromRequest(String.class, Constants.COOKIE_SELECTED_PRESET_DATE);
        if (!StringUtils.isEmpty(datePickerPreset)) {
            dashboardDto.setDatePickerPresetValue(datePickerPreset);
        }
        String status = getCookieValueFromRequest(String.class, Constants.COOKIE_PUBLICATION_STATUS);
        if (!StringUtils.isEmpty(status)) {
            if (status.equals("All")) {
                dashboardDto.setStatusFiltered(false);
                dashboardDto.setPublicationStatusFilter(PublicationStatus.ALL);
            } else {
                dashboardDto.setStatusFiltered(true);
                dashboardDto.setPublicationStatusFilter(PublicationStatus.fromString(status));
            }
        } else {
            dashboardDto.setStatusFiltered(true);
            dashboardDto.setPublicationStatusFilter(PublicationStatus.ACTIVE);
        }
        String platform = getCookieValueFromRequest(String.class, Constants.COOKIE_PLATFORM);
        if (!StringUtils.isEmpty(platform)) {
            dashboardDto.setPlatformFiltered(true);
            dashboardDto.setPlatformFilter(pService.getPublicationTypeById(Long.valueOf(platform)));
        }
        String approval = getCookieValueFromRequest(String.class, Constants.COOKIE_APPROVAL);
        if (!StringUtils.isEmpty(approval)) {
            dashboardDto.setApprovalFiltered(true);
            dashboardDto.setApprovalFilter(Approval.value(approval));
        }
        String backfill = getCookieValueFromRequest(String.class, Constants.COOKIE_BACKFILL);
        if (!StringUtils.isEmpty(backfill)) {
            dashboardDto.setBackfillFiltered(true);
            dashboardDto.setBackfillFilter(Backfill.value(backfill));
        }
    }

    public boolean isChartTimeFilterAvailable(String type) {
        Date from = dashboardDto.getFrom();
        Date to = dashboardDto.getTo();
        Map<String, Boolean> res = Utils.getChartPeriodAvailable(new Date[] { from, to });
        return res.containsKey(type);
    }

    public void individualLinesChange(AjaxBehaviorEvent event) {
        if (dashboardDto.isIndividualLines()) {
            // If publication filter is not filled, it's filled with first five
            if (dashboardDto.getPublicationsFiltered().isEmpty()) {
                List<Long> ids = new ArrayList<Long>();
                List<PublicationTypeAheadDto> pubs = new ArrayList<PublicationTypeAheadDto>();
                for (PublisherStatisticsDto s : reportingBean.getStatistics(5)) {
                    ids.add(s.getPublicationId());
                    PublicationTypeAheadDto pub = new PublicationTypeAheadDto();
                    pub.setId(s.getPublicationId());
                    pub.setName(s.getPublicationName());
                    pubs.add(pub);
                }
                dashboardDto.setPublicationsIdFiltered(ids);
                dashboardDto.setPublications(pubs);
            }
            addCookiesToResponse(dashboardDto);
            refreshPublisherHeaderAndChartsValues();
            refreshPublisherTableValues();
        } else {
            addCookiesToResponse(dashboardDto);
            refreshPublisherHeaderAndChartsValues();
        }
    }

    private void prepareDashBoardDto(PublisherDashboardDto dashboardDto) {
        dashboardDto.setPublisherDto(getUser().getPublisherDto());
        Date[] dates = new Date[] { dashboardDto.getFrom(), dashboardDto.getTo() };
        dates = Utils.getNormalizedDate(dates);

        // Date NO_DATA_DATE = null;
        //
        // try {
        // NO_DATA_DATE =
        // org.apache.commons.lang.time.DateUtils.parseDate("05/10/2012", new
        // String[] {"dd/MM/yy"});
        // } catch (ParseException e) {}
        //
        // if (NO_DATA_DATE != null) {
        // if(dates[0].after(NO_DATA_DATE)) {
        // dates[0] = Utils.getNormalizedDate(new Date[] {NO_DATA_DATE,
        // NO_DATA_DATE})[0];
        // }
        // try {
        // NO_DATA_DATE =
        // org.apache.commons.lang.time.DateUtils.parseDate("12/10/2012", new
        // String[] {"dd/MM/yy"});
        // } catch (ParseException e) {}
        // if(dates[1].after(NO_DATA_DATE)) {
        // dates[1] = Utils.getNormalizedDate(new Date[] {NO_DATA_DATE,
        // NO_DATA_DATE})[1];
        // }
        // }

        dashboardDto.setFrom(dates[0]);
        dashboardDto.setTo(dates[1]);
    }

    public boolean isMoreThan5() {
        if (dashboardDto != null) {
            if (!CollectionUtils.isEmpty(dashboardDto.getPublications()) && dashboardDto.getPublications().size() > 5) {
                return true;
            }
        }
        return false;
    }

    public Date getToday() {
        return returnNow();
    }

    public Date getMinDate() {
        DateTime dt = new DateTime();
        dt = dt.minusDays(15);
        return dt.toDate();
    }

    public List<PublicationtypeDto> getPlatformMap() {
        return pService.getPublicationType(true);
    }

    public void setPlatformMap(List<PlatformDto> l) {
        return;
    }

    public PublisherReportingMBean getReportingBean() {
        return reportingBean;
    }

    public void setReportingBean(PublisherReportingMBean reportingBean) {
        this.reportingBean = reportingBean;
    }

    public PublisherDashboardDto getDashboardDto() {
        return dashboardDto;
    }

    public void setDashboardDto(PublisherDashboardDto dashboardDto) {
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

    private void removeTableFilters() {
        dashboardDto.setStatusFiltered(false);
        dashboardDto.setPublicationStatusFilter(PublicationStatus.ALL);
        dashboardDto.setPlatformFiltered(false);
        dashboardDto.setPlatformFilter(getPlatformMap().get(0));
        dashboardDto.setApprovalFiltered(false);
        dashboardDto.setApprovalFilter(Approval.ALL);
        dashboardDto.setBackfillFiltered(false);
        dashboardDto.setBackfillFilter(Backfill.ALL);

    }
}

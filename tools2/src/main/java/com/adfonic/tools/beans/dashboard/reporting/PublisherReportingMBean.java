package com.adfonic.tools.beans.dashboard.reporting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherSortBy;
import com.adfonic.dto.dashboard.PublisherDashboardDto;
import com.adfonic.dto.dashboard.statistic.PublisherStatisticsDto;
import com.adfonic.dto.publication.enums.Approval;
import com.adfonic.dto.publication.enums.Backfill;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.presentation.dashboard.DashboardService;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.tools.beans.dashboard.publisher.PublisherDashBoardMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("view")
public class PublisherReportingMBean extends GenericAbstractBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingMBean.class);

    @Autowired
    private DashboardService dService;
    @Autowired
    private PublicationService pService;
    private String publicationStatusFilter;
    private PublisherDashboardDto dashboardDto;
    private PublisherDashboardDto cacheDashboardDto;
    // statisticsList just to cache the response in case search bean is the
    // same.
    private List<PublisherStatisticsDto> statisticsList = new ArrayList<PublisherStatisticsDto>(0);

    private LazyDataModel<PublisherStatisticsDto> lazyModel;

    private PublisherStatisticsDto[] selected;

    // dummy string column just to display the sorting
    private String column;

    @Override
    protected void init() {
    }

    public LazyDataModel<PublisherStatisticsDto> getLazyModel() {
        if (lazyModel == null) {
            lazyModel = new LazyDataModel<PublisherStatisticsDto>() {
                private static final long serialVersionUID = -1021263345029971044L;
                private List<PublisherStatisticsDto> data;

                @Override
                public List<PublisherStatisticsDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                        Map<String, String> filters) {
                    if (dashboardDto.isRecentlyFiltered()) {
                        LOGGER.debug("Table has just been filtered so pagination is set to page 1");
                        dashboardDto.setStart(1L);
                        dashboardDto.setRecentlyFiltered(false);
                        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("dataTableFormId:dataTable"))
                                .setFirst(0);
                    } else {
                        LOGGER.debug("Normal load");
                        dashboardDto.setStart(Long.valueOf((first / pageSize) + 1));
                    }
                    dashboardDto.setNumberOfRecords(Long.valueOf(pageSize));
                    // filtering thingy
                    boolean ascending = false;
                    if (sortOrder != null) {
                        LOGGER.debug("Order descending by default");
                        ascending = sortOrder.equals(SortOrder.DESCENDING) ? true : false;
                    }
                    if (sortField == null) {
                        LOGGER.debug("Sort by requests by default");
                        sortField = "requests";
                    }
                    sort(sortField, ascending);
                    data = getReportingList();
                    this.setRowCount(dashboardDto.getNumTotalRecords().intValue());

                    return data;
                }

                @Override
                public String getRowKey(PublisherStatisticsDto t) {
                    return Double.toString(t.getPublicationId());
                }

                @Override
                public PublisherStatisticsDto getRowData(String rowKey) {
                    if (!CollectionUtils.isEmpty(data)) {
                        for (PublisherStatisticsDto dto : data) {
                            if (Double.toString(dto.getPublicationId()).equals(rowKey)) {
                                return dto;
                            }
                        }
                    }
                    return null;
                }
            };
        }

        return lazyModel;
    }

    public List<PublisherStatisticsDto> getReportingList() {
        if (!dashboardDto.equals(cacheDashboardDto)) {
            // need to know if I come from a filtered action or a normal one.
            // if filtered action, need to grab the campaign ids at the
            // statisticsList
            PublisherDashboardDto dto = (PublisherDashboardDto) dService.getReportingTable(dashboardDto);
            if (dto != null) {
                statisticsList = dto.getReportingTable();
                Long total = dto.getNumTotalRecords();
                // either if its filtered or not, we need to update the
                // HeaderFigures
                // scenario: we filter ->we add the dashboardDto filtered to the
                // headeFigure
                // then we select all in the table -> need to update the
                // headerFigures to set it
                // not filtered so -> we end in updating always the header
                // figures
                if (dashboardDto.isFiltered()) {
                    // only update campaignsFiltered in this case
                    List<Long> publicationIds = Utils.getPublicationListId(statisticsList);
                    getPublisherDashboardMBean().getDashboardDto().setPublicationsIdFiltered(publicationIds);
                } else {
                    getPublisherDashboardMBean().getDashboardDto().setPublicationsIdFiltered(null);
                }
                dashboardDto = getPublisherDashboardMBean().getDashboardDto();
                dashboardDto.setNumTotalRecords(total);
                // UPDATE dahsbordDto as this bean can change parameters used to
                // search.
                getHeaderFiguresMBean().setBaseDashboardDto(dashboardDto);
                // force a search. Not always will do the search. it depends if
                // the pri:ajax combo has reference to figures
                getHeaderFiguresMBean().setBaseCacheDashboardDto(null);
                cacheDashboardDto = dashboardDto;
            }
        }
        return statisticsList;
    }

    public List<PublisherStatisticsDto> getStatistics(int numRows) {
        List<PublisherStatisticsDto> result = new ArrayList<PublisherStatisticsDto>();
        List<PublisherStatisticsDto> reportingList = getReportingList();

        if (numRows > reportingList.size()) {
            numRows = reportingList.size();
        }

        for (int i = 0; i < numRows; i++) {
            result.add(reportingList.get(i));
        }

        return result;
    }

    public void processFilter(ValueChangeEvent e) {
        // after this actions is completed, well need to update the
        // headerFigures dashboardDto object because a refresh is done
        // afterwards
        if (Constants.FILTER_PLATFORM.equals(e.getComponent().getAttributes().get("filterBy"))) {
            if (e.getNewValue() == null) {
                if (e.getOldValue() != null) {
                    getPublisherDashboardMBean().getDashboardDto().setPlatformFilter((PublicationtypeDto) e.getOldValue());
                    setDashboardDto(getPublisherDashboardMBean().getDashboardDto());

                    // cacheDashboardDto = null;
                }
            } else {
                getPublisherDashboardMBean().getDashboardDto().setPlatformFilter((PublicationtypeDto) e.getNewValue());
                getPublisherDashboardMBean().getDashboardDto().setPlatformFiltered(
                        (((PublicationtypeDto) e.getNewValue()).getId() == -1 ? false : true));
                setDashboardDto(getPublisherDashboardMBean().getDashboardDto());
                cacheDashboardDto = null;
                if (getPublisherDashboardMBean().getDashboardDto().isPlatformFiltered()) {
                    addStringCookie(((PublicationtypeDto) e.getNewValue()).getId().toString(), Constants.COOKIE_PLATFORM);
                } else {
                    addStringCookie(null, Constants.COOKIE_PLATFORM);
                }
                refreshPublisherHeaderAndChartsValues();
            }
        } else if (Constants.FILTER_STATUS.equals(e.getComponent().getAttributes().get("filterBy"))) {
            if (e.getNewValue() == null) {

                if (e.getOldValue() != null) {
                    getPublisherDashboardMBean().getDashboardDto().setPublicationStatusFilter((PublicationStatus) e.getOldValue());
                    setDashboardDto(getPublisherDashboardMBean().getDashboardDto());
                    // cacheDashboardDto = null;
                }

            } else {
                getPublisherDashboardMBean().getDashboardDto().setPublicationStatusFilter((PublicationStatus) e.getNewValue());
                getPublisherDashboardMBean().getDashboardDto().setStatusFiltered(
                        ((PublicationStatus) e.getNewValue()) == PublicationStatus.ALL ? false : true);
                setDashboardDto(getPublisherDashboardMBean().getDashboardDto());
                cacheDashboardDto = null;
                String value = ((PublicationStatus) e.getNewValue()).getPublicationStatusStr();
                if (value.equals("page.dashboard.labels.table.filter.status.options.all")) {
                    value = "All";
                }
                addStringCookie(value, Constants.COOKIE_PUBLICATION_STATUS);

                refreshPublisherHeaderAndChartsValues();
            }
        } else if (Constants.FILTER_APPROVAL.equals(e.getComponent().getAttributes().get("filterBy"))) {
            if (e.getNewValue() == null) {

                if (e.getOldValue() != null) {
                    getPublisherDashboardMBean().getDashboardDto().setApprovalFilter((Approval) e.getOldValue());
                    setDashboardDto(getPublisherDashboardMBean().getDashboardDto());
                    // cacheDashboardDto = null;
                }

            } else {
                getPublisherDashboardMBean().getDashboardDto().setApprovalFilter((Approval) e.getNewValue());
                getPublisherDashboardMBean().getDashboardDto().setApprovalFiltered(
                        ((Approval) e.getNewValue()) == Approval.ALL ? false : true);
                setDashboardDto(getPublisherDashboardMBean().getDashboardDto());
                cacheDashboardDto = null;
                if (getPublisherDashboardMBean().getDashboardDto().isApprovalFiltered()) {
                    addStringCookie(((Approval) e.getNewValue()).getId(), Constants.COOKIE_APPROVAL);
                } else {
                    addStringCookie(null, Constants.COOKIE_APPROVAL);
                }
                refreshPublisherHeaderAndChartsValues();
            }
        } else if (Constants.FILTER_BACKFILL.equals(e.getComponent().getAttributes().get("filterBy"))) {
            if (e.getNewValue() == null) {

                if (e.getOldValue() != null) {
                    getPublisherDashboardMBean().getDashboardDto().setBackfillFilter((Backfill) e.getOldValue());
                    setDashboardDto(getPublisherDashboardMBean().getDashboardDto());
                    // cacheDashboardDto = null;
                }

            } else {
                getPublisherDashboardMBean().getDashboardDto().setBackfillFilter((Backfill) e.getNewValue());
                getPublisherDashboardMBean().getDashboardDto().setBackfillFiltered(
                        ((Backfill) e.getNewValue()) == Backfill.ALL ? false : true);
                setDashboardDto(getPublisherDashboardMBean().getDashboardDto());
                cacheDashboardDto = null;
                if (getPublisherDashboardMBean().getDashboardDto().isBackfillFiltered()) {
                    addStringCookie(((Backfill) e.getNewValue()).getId(), Constants.COOKIE_BACKFILL);
                } else {
                    addStringCookie(null, Constants.COOKIE_BACKFILL);
                }
                refreshPublisherHeaderAndChartsValues();
            }
        }

        selected = null;
    }

    public void filter(AjaxBehaviorEvent event) {
        this.dashboardDto.setRecentlyFiltered(true);
    }

    private void sort(String columnName, boolean ascending) {
        if (Constants.PUB_DASHBOARD_DATATABLE_PUBLICATIONNAMEID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.PUBLICATION_NAME, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);
        } else if (Constants.PUB_DASHBOARD_DATATABLE_IMPRESSIONSID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.IMPRESSIONS, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.PUB_DASHBOARD_DATATABLE_PLATFORMID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.PLATFORM, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.PUB_DASHBOARD_DATATABLE_APPROVALID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.APPROVAL, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.PUB_DASHBOARD_DATATABLE_BACKFILLID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.BACKFILL, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.PUB_DASHBOARD_DATATABLE_REQUESTSID.equals(columnName)) {

            updateDashboardBeanDto(PublisherSortBy.REQUESTS, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.PUB_DASHBOARD_DATATABLE_FILLRATEID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.FILL_RATE, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.PUB_DASHBOARD_DATATABLE_REVENUEID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.REVENUE, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.PUB_DASHBOARD_DATATABLE_ECPMID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.ECPM, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.PUB_DASHBOARD_DATATABLE_CLICKSID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.CLICKS, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.PUB_DASHBOARD_DATATABLE_CTRID.equals(columnName)) {
            updateDashboardBeanDto(PublisherSortBy.CTR, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        }
        // ((DataTable) event.getSource()).getSortOrder();
    }

    /**
     * http://cagataycivici.wordpress.com/2011/06/10/datatable-hooks/
     * http://forum.primefaces.org/viewtopic.php?f=3&t=14151 Sorting ajax event
     * to get fresh data from database. Sorting is done via procedure not
     * dataTable sorting built-in feature. <!-- pri:ajax event="sort"
     * listener="#{dashBoardMBean.reportingBean.onSort}" update="dataTable"/ -->
     * 
     * public void onSort(SortEvent event) { String columnName =
     * event.getSortColumn().getClientId(); columnName =
     * columnName.replace("dataTable:", ""); boolean ascending =
     * event.isAscending();
     * 
     * if (Constants.DASHBOARD_DATATABLE_CAMPAIGNNAMEID.equals(columnName)) {
     * updateDashboardBeanDto(SortBy.CAMPAIGN_NAME, ascending ?
     * OrderBy.ASCENDING : OrderBy.DESCENDING); } else if
     * (Constants.DASHBOARD_DATATABLE_IMPRESSIONSID .equals(columnName)) {
     * updateDashboardBeanDto(SortBy.IMPRESSIONS, ascending ? OrderBy.ASCENDING
     * : OrderBy.DESCENDING);
     * 
     * } else if (Constants.DASHBOARD_DATATABLE_CTRID.equals(columnName)) {
     * updateDashboardBeanDto(SortBy.CTR, ascending ? OrderBy.ASCENDING :
     * OrderBy.DESCENDING);
     * 
     * } else if (Constants.DASHBOARD_DATATABLE_CLICKSID.equals(columnName)) {
     * updateDashboardBeanDto(SortBy.CLICKS, ascending ? OrderBy.ASCENDING :
     * OrderBy.DESCENDING);
     * 
     * } else if (Constants.DASHBOARD_DATATABLE_BIDPRICEID.equals(columnName)) {
     * updateDashboardBeanDto(SortBy.BID_PRICE, ascending ? OrderBy.ASCENDING :
     * OrderBy.DESCENDING);
     * 
     * } else if (Constants.DASHBOARD_DATATABLE_SPENDID.equals(columnName)) {
     * 
     * updateDashboardBeanDto(SortBy.SPEND, ascending ? OrderBy.ASCENDING :
     * OrderBy.DESCENDING);
     * 
     * } else if (Constants.DASHBOARD_DATATABLE_CONVERSIONID
     * .equals(columnName)) { updateDashboardBeanDto(SortBy.CONVERSIONS,
     * ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);
     * 
     * } else if (Constants.DASHBOARD_DATATABLE_CPAID.equals(columnName)) {
     * updateDashboardBeanDto(SortBy.CPA, ascending ? OrderBy.ASCENDING :
     * OrderBy.DESCENDING);
     * 
     * } else if (Constants.DASHBOARD_DATATABLE_BUDGETSPENTID
     * .equals(columnName)) { updateDashboardBeanDto(SortBy.BUDGET_SPENT,
     * ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);
     * 
     * } else if (Constants.DASHBOARD_DATATABLE_TOTALBUDGETID
     * .equals(columnName)) { updateDashboardBeanDto(SortBy.TOTAL_BUDGENT_SPENT,
     * ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);
     * 
     * } // ((DataTable) event.getSource()).getSortOrder(); statisticsList =
     * getReportingList(); }
     */
    private void updateDashboardBeanDto(PublisherSortBy sort, OrderBy orderby) {
        PublisherDashBoardMBean dash = getPublisherDashboardMBean();
        dash.getDashboardDto().setSortBy(sort);
        dash.getDashboardDto().setOrderBy(orderby);
        // it happens that when sorting the table, if the
        PublicationtypeDto all = new PublicationtypeDto();
        all.setId((long) -1);
        all.setName("All");
        if (dash.getDashboardDto().getPlatformFilter() == null) {
            dash.getDashboardDto().setPlatformFilter(all);
        }
        if (dash.getDashboardDto().getPublicationStatusFilter() == null) {
            dash.getDashboardDto().setPublicationStatusFilter(PublicationStatus.ALL);
        }
        if (dash.getDashboardDto().getApprovalFilter() == null) {
            dash.getDashboardDto().setApprovalFilter(Approval.ALL);
        }
        if (dash.getDashboardDto().getBackfillFilter() == null) {
            dash.getDashboardDto().setBackfillFilter(Backfill.ALL);
        }
        setDashboardDto(dash.getDashboardDto());
        cacheDashboardDto = null;
    }

    public void doChangeStatus(ActionEvent event) {
        String id = event.getComponent().getId();
        if (!"newPublicationButtonId".equals(id)) {
            doChangeState((String) event.getComponent().getAttributes().get("stat"));
            // reset selected checks
            // selected.clear();
        }
    }

    private void doChangeState(String status) {
        if (!ArrayUtils.isEmpty(selected)) {
            List<Long> list = new ArrayList<Long>(0);
            for (PublisherStatisticsDto st : Arrays.asList(selected)) {
                list.add(st.getPublicationId());
            }

            if (status.equals("pause")) {
                pService.changePublicationStatus(list, PublicationStatus.PAUSED);
            } else if (status.equals("activate")) {
                pService.changePublicationStatus(list, PublicationStatus.ACTIVE);
            }

        }
        selected = null;
    }

    public String convertStatus(String status) {
        if (StringUtils.isEmpty(status)) {
            return "NEW";
        }
        return PublicationStatus.valueOf(status).getPublicationStatusStr();
    }

    public String getPublicationStatusFilter() {
        return publicationStatusFilter;
    }

    public void setPublicationStatusFilter(String publicationStatusFilter) {
        this.publicationStatusFilter = publicationStatusFilter;
    }

    public PublisherStatisticsDto[] getSelected() {
        return selected;
    }

    public void setSelected(PublisherStatisticsDto[] selected) {
        this.selected = selected;
    }

    public PublisherDashboardDto getDashboardDto() {
        if (dashboardDto == null) {
            dashboardDto = new PublisherDashboardDto();
        }
        return dashboardDto;
    }

    public void setDashboardDto(PublisherDashboardDto dashboardDto) {
        this.dashboardDto = dashboardDto;
    }

    public List<PublisherStatisticsDto> getStatisticsList() {
        return statisticsList;
    }

    public void setStatisticsList(List<PublisherStatisticsDto> statisticsList) {
        this.statisticsList = statisticsList;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setLazyModel(LazyDataModel<PublisherStatisticsDto> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public PublisherDashboardDto getCacheDashboardDto() {
        return cacheDashboardDto;
    }

    public void setCacheDashboardDto(PublisherDashboardDto cacheDashboardDto) {
        this.cacheDashboardDto = cacheDashboardDto;
    }
}

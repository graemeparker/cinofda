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

import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.dashboard.AgencyConsoleDashboardDto;
import com.adfonic.dto.dashboard.DashboardParameters.AgencyConsoleSortBy;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.statistic.AgencyConsoleStatisticsDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.dashboard.DashboardService;
import com.adfonic.tools.beans.dashboard.agencyconsole.AgencyConsoleDashboardMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("view")
public class AgencyReportingMBean extends GenericAbstractBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AgencyReportingMBean.class);

    @Autowired
    private DashboardService dService;
    @Autowired
    private CompanyService cService;
    private String advertiserStatusFilter;
    private AgencyConsoleDashboardDto dashboardDto;
    private AgencyConsoleDashboardDto cacheDashboardDto;
    // statisticsList just to cache the response in case search bean is the
    // same.
    private List<AgencyConsoleStatisticsDto> statisticsList = new ArrayList<AgencyConsoleStatisticsDto>(0);

    private List<AdvertiserStatus> statusList = null;

    private LazyDataModel<AgencyConsoleStatisticsDto> lazyModel;

    private AgencyConsoleStatisticsDto[] selected;

    // dummy string column just to display the sorting
    private String column;

    /***
     * Variable to keep track if the getReportingList has to save the filtered
     * campaignIds or not.
     *
     * */
    private boolean isFiltered = false;

    @Override
    protected void init() {
    }

    public LazyDataModel<AgencyConsoleStatisticsDto> getLazyModel() {
        if (lazyModel == null) {
            lazyModel = new LazyDataModel<AgencyConsoleStatisticsDto>() {
                private static final long serialVersionUID = -1021263345029971044L;

                @Override
                public List<AgencyConsoleStatisticsDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
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
                        LOGGER.debug("Sort by impressions by default");
                        sortField = "spend";
                    }
                    sort(sortField, ascending);
                    List<AgencyConsoleStatisticsDto> result = getReportingList();
                    if (dashboardDto.getNumTotalRecords()!=null){
                        this.setRowCount(dashboardDto.getNumTotalRecords().intValue());
                    }else{
                        this.setRowCount(0);
                    }
                    selected = null;

                    return result;
                }
            };
        }

        return lazyModel;
    }

    public List<AgencyConsoleStatisticsDto> getReportingList() {
        if (!dashboardDto.equals(cacheDashboardDto)) {
            // need to know if I come from a filtered action or a normal one.
            // if filtered action, need to grab the campaign ids at the
            // statisticsList
            dashboardDto.getAdvertisersRequested().clear();
            if (CollectionUtils.isEmpty(dashboardDto.getAdvertisers())) {
                UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
                dashboardDto.getAdvertisersRequested().addAll(userDto.getAdvertiserListDto());
            } else {
                dashboardDto.getAdvertisersRequested().addAll(dashboardDto.getAdvertisers());
            }
            AgencyConsoleDashboardDto dto = (AgencyConsoleDashboardDto) dService.getReportingTable(dashboardDto);
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
                if (isFiltered()) {
                    // only update campaignsFiltered in this case
                    List<Long> campaignsIds = Utils.getAdvertisersListId(statisticsList);
                    getDashboardMBean().getDashboardDto().setCampaignsIdFiltered(campaignsIds);
                } else {
                    getDashboardMBean().getDashboardDto().setCampaignsIdFiltered(null);
                }
                dashboardDto = getAgencyDashboardMBean().getDashboardDto();
                dashboardDto.setNumTotalRecords(total);

                cacheDashboardDto = dashboardDto;
            }
        }
        return statisticsList;
    }

    public List<AgencyConsoleStatisticsDto> getStatistics(int numRows) {
        List<AgencyConsoleStatisticsDto> result = new ArrayList<AgencyConsoleStatisticsDto>();
        List<AgencyConsoleStatisticsDto> reportingList = getReportingList();

        if (numRows > reportingList.size()) {
            numRows = reportingList.size();
        }

        for (int i = 0; i < numRows; i++) {
            result.add(reportingList.get(i));
        }

        return result;
    }

    public void filter(AjaxBehaviorEvent event) {
        this.dashboardDto.setRecentlyFiltered(true);
    }

    public void processFilter(ValueChangeEvent e) {
        if (Constants.FILTER_STATUS.equals(e.getComponent().getAttributes().get("filterBy"))) {
            if (e.getNewValue() == null) {
                if (e.getOldValue() != null) {
                    getAgencyDashboardMBean().getDashboardDto().setStatusFilter((AdvertiserStatus) e.getOldValue());
                    setDashboardDto(getAgencyDashboardMBean().getDashboardDto());
                    // cacheDashboardDto = null;
                }

            } else {
                isFiltered = ((AdvertiserStatus) e.getNewValue()) == AdvertiserStatus.ALL ? false : true;
                getAgencyDashboardMBean().getDashboardDto().setStatusFilter((AdvertiserStatus) e.getNewValue());
                getAgencyDashboardMBean().getDashboardDto().setStatusFiltered(
                        ((AdvertiserStatus) e.getNewValue()) == AdvertiserStatus.ALL ? false : true);
                setDashboardDto(getAgencyDashboardMBean().getDashboardDto());
                cacheDashboardDto = null;
                String value = ((AdvertiserStatus) e.getNewValue()).getAdvertiserStatusStr();
                if (value.equals("page.dashboard.labels.table.filter.status.options.all")) {
                    value = "All";
                }
                addStringCookie(value, Constants.COOKIE_ADVERTISER_STATUS);

                // triggers the charts and header refresh
                refreshAgencyChartsValues();
            }
        }

        selected = null;
    }

    public String convertStatus(String status) {
        if (StringUtils.isEmpty(status)) {
            return "ACTIVE";
        }
        return AdvertiserStatus.valueOf(status).getAdvertiserStatusStr();
    }

    // TODO CHANGE FROM HERE!!!!!!
    private void sort(String columnName, boolean ascending) {
        if (Constants.AC_DASHBOARD_DATATABLE_ADVERTISERNAMEID.equals(columnName)) {
            updateDashboardBeanDto(AgencyConsoleSortBy.ADVERTISER_NAME, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.AC_DASHBOARD_DATATABLE_IMPRESSIONSID.equals(columnName)) {
            updateDashboardBeanDto(AgencyConsoleSortBy.IMPRESSIONS, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.AC_DASHBOARD_DATATABLE_SPENDID.equals(columnName)) {
            updateDashboardBeanDto(AgencyConsoleSortBy.SPEND, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.AC_DASHBOARD_DATATABLE_SPENDYESTERDAYID.equals(columnName)) {
            updateDashboardBeanDto(AgencyConsoleSortBy.SPEND_YESTERDAY, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.AC_DASHBOARD_DATATABLE_BALANCEID.equals(columnName)) {
            updateDashboardBeanDto(AgencyConsoleSortBy.BALANCE, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        }
        // ((DataTable) event.getSource()).getSortOrder();
    }

    private void updateDashboardBeanDto(AgencyConsoleSortBy sort, OrderBy orderby) {
        AgencyConsoleDashboardMBean dash = getAgencyDashboardMBean();
        dash.getDashboardDto().setSortBy(sort);
        dash.getDashboardDto().setOrderBy(orderby);
        if (dash.getDashboardDto().getStatusFilter() == null) {
            dash.getDashboardDto().setStatusFilter(AdvertiserStatus.ALL);
        }
        setDashboardDto(dash.getDashboardDto());
        cacheDashboardDto = null;
    }

    public void doChangeStatus(ActionEvent event) {
        String id = event.getComponent().getId();
        if (!"newCampaignButtonId".equals(id)) {
            doChangeState((String) event.getComponent().getAttributes().get("stat"));
            // reset selected checks
            // selected.clear();
        }
    }

    private void doChangeState(String status) {
        if (!ArrayUtils.isEmpty(selected)) {
            List<Long> list = new ArrayList<Long>(0);
            for (AgencyConsoleStatisticsDto st : Arrays.asList(selected)) {
                list.add(st.getAdvertiserId());
            }
            if (status.equals("inactive")) {
                cService.changeAdvertiserStatus(list, AdvertiserStatus.INACTIVE);
            } else if (status.equals("activate")) {
                cService.changeAdvertiserStatus(list, AdvertiserStatus.ACTIVE);
            }
        }

        selected = null;
    }

    public AgencyConsoleDashboardDto getDashboardDto() {
        if (dashboardDto == null) {
            dashboardDto = new AgencyConsoleDashboardDto();
        }
        return dashboardDto;
    }

    public void setDashboardDto(AgencyConsoleDashboardDto dashboardDto) {
        this.dashboardDto = dashboardDto;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isFiltered() {
        return isFiltered;
    }

    public void setFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
    }

    public List<AdvertiserStatus> getStatusList() {
        if (statusList == null) {
            statusList = new ArrayList<AdvertiserStatus>();
            statusList.add(AdvertiserStatus.ALL);
            statusList.add(AdvertiserStatus.ACTIVE);
            statusList.add(AdvertiserStatus.INACTIVE);
        }
        return statusList;
    }

    public void setStatusList(List<AdvertiserStatus> statusList) {
        this.statusList = statusList;
    }

    public String getAdvertiserStatusFilter() {
        return advertiserStatusFilter;
    }

    public void setAdvertiserStatusFilter(String advertiserStatusFilter) {
        this.advertiserStatusFilter = advertiserStatusFilter;
    }

    public AgencyConsoleDashboardDto getCacheDashboardDto() {
        return cacheDashboardDto;
    }

    public void setCacheDashboardDto(AgencyConsoleDashboardDto cacheDashboardDto) {
        this.cacheDashboardDto = cacheDashboardDto;
    }

    public List<AgencyConsoleStatisticsDto> getStatisticsList() {
        return statisticsList;
    }

    public void setStatisticsList(List<AgencyConsoleStatisticsDto> statisticsList) {
        this.statisticsList = statisticsList;
    }

    public AgencyConsoleStatisticsDto[] getSelected() {
        return selected;
    }

    public void setSelected(AgencyConsoleStatisticsDto[] selected) {
        this.selected = selected;
    }

}

package com.adfonic.tools.beans.dashboard.reporting;

import static com.adfonic.tools.util.BudgetUtils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CellEditEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.BiddingStrategyName;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.campaign.scheduling.CampaignTimePeriodDto;
import com.adfonic.dto.company.AccountFixedMarginDto;
import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.DashboardParameters.SortBy;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;
import com.adfonic.presentation.account.AccountService;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.dashboard.DashboardService;
import com.adfonic.tools.beans.dashboard.DashBoardMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;
import com.adfonic.tools.exception.BudgetValidatorException;

@Component
@Scope("view")
public class ReportingMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingMBean.class);

    @Autowired
    private DashboardService dService;
    @Autowired
    private CampaignService cService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CompanyService comService;

    private String campaignStatusFilter;
    private DashboardDto dashboardDto;
    private DashboardDto cacheDashboardDto;
    // statisticsList just to cache the response in case search bean is the
    // same.
    private List<StatisticsDto> statisticsList = new ArrayList<StatisticsDto>(0);

    private List<CampaignStatus> statusList = null;

    private LazyDataModel<StatisticsDto> lazyModel;

    private StatisticsDto[] selected;

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
        // Empty
    }

    public LazyDataModel<StatisticsDto> getLazyModel() {
        if (lazyModel == null) {
            lazyModel = new LazyDataModel<StatisticsDto>() {
                private static final long serialVersionUID = -1021263345029971044L;
                private List<StatisticsDto> data;

                @Override
                public List<StatisticsDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
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
                    data = getReportingList();
                    this.setRowCount(dashboardDto.getNumTotalRecords().intValue());
                    selected = null;

                    return data;
                }

                @Override
                public String getRowKey(StatisticsDto t) {
                    return Double.toString(t.getCampaignId());
                }

                @Override
                public StatisticsDto getRowData(String rowKey) {
                    if (!CollectionUtils.isEmpty(data)) {
                        for (StatisticsDto dto : data) {
                            if (Double.toString(dto.getCampaignId()).equals(rowKey)) {
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

    public List<StatisticsDto> getReportingList() {
        if (!dashboardDto.equals(cacheDashboardDto)) {
            // need to know if I come from a filtered action or a normal one.
            // if filtered action, need to grab the campaign ids at the
            // statisticsList
            DashboardDto dto = dService.getReportingTable(dashboardDto);
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
                    List<Long> campaignsIds = Utils.getCampaignListId(statisticsList);
                    getDashboardMBean().getDashboardDto().setCampaignsIdFiltered(campaignsIds);
                } else {
                    getDashboardMBean().getDashboardDto().setCampaignsIdFiltered(null);
                }
                dashboardDto = getDashboardMBean().getDashboardDto();
                dashboardDto.setNumTotalRecords(total);
                // UPDATE dahsbordDto as this bean can change parameters used to
                // search.
                getHeaderFiguresMBean().setDashboardDto(dashboardDto);
                // force a search. Not always will do the search. it depends if
                // the pri:ajax combo has reference to figures
                getHeaderFiguresMBean().setCacheDashboardDto(null);
                cacheDashboardDto = dashboardDto;
            }
        }
        return statisticsList;
    }

    public List<StatisticsDto> getStatistics(int numRows) {
        List<StatisticsDto> result = new ArrayList<StatisticsDto>();
        List<StatisticsDto> reportingList = getReportingList();

        if (numRows > reportingList.size()) {
            numRows = reportingList.size();
        }

        for (int i = 0; i < numRows; i++) {
            result.add(reportingList.get(i));
        }

        return result;
    }

    public void filter(/* AjaxBehaviorEvent event */) {
        this.dashboardDto.setRecentlyFiltered(true);
    }

    public void processFilter(ValueChangeEvent e) {
        // after this actions is completed, well need to update the
        // headerFigures dashboardDto object because a refresh is done
        // afterwards
        if (Constants.FILTER_BIDTYPE.equals(e.getComponent().getAttributes().get("filterBy"))) {
            if (e.getNewValue() == null) {
                if (e.getOldValue() != null) {
                    getDashboardMBean().getDashboardDto().setBidTypeFilter((BidType) e.getOldValue());
                    setDashboardDto(getDashboardMBean().getDashboardDto());

                    // cacheDashboardDto = null;
                }
            } else {
                isFiltered = ((BidType) e.getNewValue()) == BidType.ALL ? false : true;
                getDashboardMBean().getDashboardDto().setBidTypeFilter((BidType) e.getNewValue());
                getDashboardMBean().getDashboardDto().setBidTypeFiltered(((BidType) e.getNewValue()) == BidType.ALL ? false : true);
                setDashboardDto(getDashboardMBean().getDashboardDto());
                cacheDashboardDto = null;
                if (getDashboardMBean().getDashboardDto().isBidTypeFiltered()) {
                    addStringCookie(((BidType) e.getNewValue()).getId(), Constants.COOKIE_CAMPAIGN_BIDTYPE);
                } else {
                    addStringCookie(null, Constants.COOKIE_CAMPAIGN_BIDTYPE);
                }

                // triggers the charts and header refresh
                refreshHeaderAndChartsValues();
            }
        }
        if (Constants.FILTER_STATUS.equals(e.getComponent().getAttributes().get("filterBy"))) {
            if (e.getNewValue() == null) {

                if (e.getOldValue() != null) {
                    getDashboardMBean().getDashboardDto().setCampaignStatusFilter((CampaignStatus) e.getOldValue());
                    setDashboardDto(getDashboardMBean().getDashboardDto());
                    // cacheDashboardDto = null;
                }

            } else {
                isFiltered = ((CampaignStatus) e.getNewValue()) == CampaignStatus.ALL ? false : true;
                getDashboardMBean().getDashboardDto().setCampaignStatusFilter((CampaignStatus) e.getNewValue());
                getDashboardMBean().getDashboardDto().setStatusFiltered(
                        ((CampaignStatus) e.getNewValue()) == CampaignStatus.ALL ? false : true);
                setDashboardDto(getDashboardMBean().getDashboardDto());
                cacheDashboardDto = null;
                String value = ((CampaignStatus) e.getNewValue()).getCampaignStatusStr();
                if ("page.dashboard.labels.table.filter.status.options.all".equals(value)) {
                    value = "All";
                }
                addStringCookie(value, Constants.COOKIE_CAMPAIGN_STATUS);

                // triggers the charts and header refresh
                refreshHeaderAndChartsValues();
            }
        }
        selected = null;
    }

    private void sort(String columnName, boolean ascending) {
        if (Constants.DASHBOARD_DATATABLE_CAMPAIGNNAMEID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.CAMPAIGN_NAME, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);
        } else if (Constants.DASHBOARD_DATATABLE_BIDTTYPEID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.BID_TYPE, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_BIDPRICEID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.BID_PRICE, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_LIFETIMEBUDGETID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.TOTAL_BUDGET, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_LIFETIMESPENDID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.TOTAL_SPEND, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_DAILYCAPID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.DAILY_BUDGET, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_SPENDYESTERDAYID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.SPEND_YESTERDAY, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_CTRID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.CTR, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_CVRID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.CVR, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_SPENDID.equals(columnName)) {

            updateDashboardBeanDto(SortBy.SPEND, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_ECPAID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.ECPM, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

        } else if (Constants.DASHBOARD_DATATABLE_ECPMID.equals(columnName)) {
            updateDashboardBeanDto(SortBy.ECPA, ascending ? OrderBy.ASCENDING : OrderBy.DESCENDING);

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
    private void updateDashboardBeanDto(SortBy sort, OrderBy orderby) {
        DashBoardMBean dash = getDashboardMBean();
        dash.getDashboardDto().setSortBy(sort);
        dash.getDashboardDto().setOrderBy(orderby);
        // it happens that when sorting the table, if the
        if (dash.getDashboardDto().getBidTypeFilter() == null) {
            dash.getDashboardDto().setBidTypeFilter(BidType.ALL);
        }
        if (dash.getDashboardDto().getCampaignStatusFilter() == null) {
            dash.getDashboardDto().setCampaignStatusFilter(CampaignStatus.ALL);
        }
        setDashboardDto(dash.getDashboardDto());
        cacheDashboardDto = null;
    }

    public void doChangeStatus(ActionEvent event) {
        String id = event.getComponent().getId();
        if (!"newCampaignButtonId".equals(id)) {
            doChangeState((String) event.getComponent().getAttributes().get("stat"));
            // reset selected checks
            selected = null;
        }
    }

    public boolean isSelectedCampaignsCompleted() {
        boolean allCompleted = true;
        if (!ArrayUtils.isEmpty(this.selected)) {
            for (StatisticsDto st : Arrays.asList(selected)) {
                if (!st.getStatus().equals(CampaignStatus.COMPLETED.name())) {
                    allCompleted = false;
                }
            }
        } else {
            allCompleted = false;
        }
        return allCompleted;
    }

    private void doChangeState(String status) {
        if (!ArrayUtils.isEmpty(selected)) {
            List<Long> list = new ArrayList<Long>(0);
            for (StatisticsDto st : Arrays.asList(selected)) {
                list.add(st.getCampaignId());
            }
            if ("pause".equals(status)) {
                cService.changeCampaignStatus(list, CampaignStatus.PAUSED);
            } else if ("activate".equals(status)) {
                cService.changeCampaignStatus(list, CampaignStatus.ACTIVE);
            } else if ("reactivate".equals(status)) {
                // TODO notify adserver
                // add new open ended time period
                Calendar cal = Calendar.getInstance(getCompanyTimeZone(comService));
                CampaignTimePeriodDto ctp = new CampaignTimePeriodDto();
                ctp.setStartDate(cal.getTime());
                ctp.setEndDate(null);
                for (Long id : list) {
                    try {
                        cService.addTimePeriod(ctp, id);
                    } catch (IllegalArgumentException iae) {
                        // couldn't add time period, just update campaign due to
                        // overlaping
                        LOGGER.debug("addTimePeriod threw: " + iae.getMessage() + " when adding to campaign id: " + id);
                    }
                }
                cService.changeCampaignStatus(list, CampaignStatus.ACTIVE);
            } else if ("stop".equals(status)) {
                cService.changeCampaignStatus(list, CampaignStatus.STOPPED);
            }
            // Clear selections after changing the status
            selected = null;
        }
    }

    public String convertStatus(String status) {
        return CampaignStatus.valueOf(status).getCampaignStatusStr();
    }

    public String getCampaignStatusFilter() {
        return campaignStatusFilter;
    }

    public void setCampaignStatusFilter(String campaignStatusFilter) {
        this.campaignStatusFilter = campaignStatusFilter;
    }

    public StatisticsDto[] getSelected() {
        return selected;
    }

    public void setSelected(StatisticsDto[] selected) {
        this.selected = Arrays.copyOf(selected, selected.length);
    }

    public DashboardDto getDashboardDto() {
        if (dashboardDto == null) {
            dashboardDto = new DashboardDto();
        }
        return dashboardDto;
    }

    public void setDashboardDto(DashboardDto dashboardDto) {
        this.dashboardDto = dashboardDto;
    }

    public List<StatisticsDto> getStatisticsList() {
        return statisticsList;
    }

    public void setStatisticsList(List<StatisticsDto> statisticsList) {
        this.statisticsList = statisticsList;
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

    public void setLazyModel(LazyDataModel<StatisticsDto> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public DashboardDto getCacheDashboardDto() {
        return cacheDashboardDto;
    }

    public void setCacheDashboardDto(DashboardDto cacheDashboardDto) {
        this.cacheDashboardDto = cacheDashboardDto;
    }

    public List<CampaignStatus> getStatusList() {
        if (statusList == null) {
            statusList = new ArrayList<CampaignStatus>();
            statusList.add(CampaignStatus.ALL);
            statusList.add(CampaignStatus.ACTIVE);
            statusList.add(CampaignStatus.PAUSED);
            statusList.add(CampaignStatus.PENDING);
            statusList.add(CampaignStatus.PENDING_PAUSED);
            statusList.add(CampaignStatus.NEW);
            statusList.add(CampaignStatus.NEW_REVIEW);
            statusList.add(CampaignStatus.COMPLETED);
            statusList.add(CampaignStatus.STOPPED);
        }
        return statusList;
    }

    public void setStatusList(List<CampaignStatus> statusList) {
        this.statusList = statusList;
    }
    
    // Event handler for all kind of cell editors
    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        
        // Process only if the input has changed
        if(newValue != null && !newValue.equals(oldValue)) {
            
            // Identify the changed row
            int editedRow = event.getRowIndex();
            lazyModel.setRowIndex(editedRow);
            StatisticsDto changedStatistics = lazyModel.getRowData();
            
            // Search for the selected campaign for more info
            CampaignDto searchDto = new CampaignDto();
            searchDto.setId(changedStatistics.getCampaignId());
            CampaignDto campaignDto = cService.getCampaignById(searchDto);
            
            // Account fixed margin [MAD-3348]
            AccountFixedMarginDto accountFixedMarginDto = comService.getAccountFixedMargin(getUser().getCompany().getId());
            
            // Prepare margin, adserving fee
            writeFixedMargin(campaignDto, readFixedMargin(campaignDto, accountFixedMarginDto), accountFixedMarginDto);
            writeAdServingCpmFee(campaignDto, readAdServingCpmFee(campaignDto));
            
            String cellEditorId = event.getColumn().getCellEditor().getId();
            switch (cellEditorId) {
            case "bidPriceEditor":
                // Prepare bid price
                String newBidPrice = ObjectUtils.toString(changedStatistics.getBidPrice());
                writeCurrentBid(campaignDto, newBidPrice, newBidPrice, newBidPrice);
                cService.saveBid(campaignDto);
                break;
            case "lifetimeBudgetEditor":
                // Prepare overall budget
                writeOverallBudget(campaignDto, campaignDto.getBudType(), ObjectUtils.toString(changedStatistics.getTotalBudget()));
                cService.saveBid(campaignDto);
                break;
            case "dailyCapEditor":
                // Prepare daily budget
                writeDailyBudget(campaignDto, campaignDto.getBudType(), ObjectUtils.toString(changedStatistics.getDailyCap()));
                cService.saveBid(campaignDto);
                break;
            default:
                break;
            }
        }
    }
    
    /** Specific validator for editing prices in cells
     * 
     * @param newPrice one of the edited value from dashboard cell
     * @throws BudgetValidatorException
     */
    public void priceValidator(FacesContext context, UIComponent component, Object newPrice) {
        
        // Get the actual row object based on data table "var" attribute reference
        StatisticsDto changedStatistics = context.getApplication().evaluateExpressionGet(context, "#{stc}", StatisticsDto.class);
        
        // Search for the actual campaign
        CampaignDto searchDto = new CampaignDto();
        searchDto.setId(changedStatistics.getCampaignId());
        CampaignDto campaignDto = cService.getCampaignById(searchDto);
        
        // Initialize actual values for validation
        String bidType = changedStatistics.getBidType().getBidType();
        String amountCpc = readAmountCpc(campaignDto);
        String amountCpm = readAmountCpm(campaignDto);
        String amountCpx = readAmountCpx(campaignDto);
        String dailyBudget = readDailyBudget(campaignDto);
        String overallBudget = readOverallBudget(campaignDto);
        boolean averageMaximumBidEnabled = campaignDto.getBiddingStrategies().contains(BiddingStrategyName.AVERAGE_MAXIMUM_BID);
        
        // Identify which field has changed based on its client id
        String clientId = component.getClientId();
        String cellInputId = clientId.substring(clientId.lastIndexOf(':') + 1, clientId.length());
        
        // Set the proper new value before validation
        String newValue = String.valueOf(newPrice);
        switch (cellInputId) {
        case "bidPrice":
            if (isCpc(bidType)) {
                amountCpc = newValue;
            } else if (isCpm(bidType)) {
                amountCpm = newValue;
            } else {
                amountCpx = newValue;
            }
            break;
        case "lifeTimeBudget":
            overallBudget = newValue;
            break;
        case "dailyCap":
            dailyBudget = newValue;
            break;
        default:
            break;
        }
        
        // Throws validation error in case of any improper data
        validatePrices(campaignDto, bidType, amountCpc, amountCpm, amountCpx, dailyBudget, overallBudget, getAccountDailyBudget(accountService),
        		averageMaximumBidEnabled, campaignDto.getMaxBidThreshold());
    }

}


package com.adfonic.presentation.dashboard.statistics.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.campaign.enums.CampaignStatus;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.DashboardParameters.Report;
import com.adfonic.dto.dashboard.DashboardParameters.SortBy;
import com.adfonic.dto.dashboard.statistic.AdvertiserHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.dashboard.statistics.AdvertiserDashboardDao;
import com.adfonic.presentation.sql.mappers.RecordCountResultSetExtractor;
import com.adfonic.presentation.util.Utils;

/**
 * DAO class for the Advertiser Dashboard.
 * 
 * @author antonysohal
 */

public class AdvertiserDashboardDaoImpl extends JdbcDaoSupport implements AdvertiserDashboardDao {

    private static Logger LOGGER = LoggerFactory.getLogger(AdvertiserDashboardDaoImpl.class);
    
    @Autowired
    private CampaignService campaignService;
    
    
    @Override
    public Long getNumberOfRecordsForDashboardReportingTableForAdvertiser(Long advertiserId,
            CampaignStatus campaignStatus, BidType bidType, int dateRange,boolean showDeletedCampaigns) {
        LOGGER.debug("params: advertiserId={}, campaignStatus={}, bidType={}, dateRange={}", new Object[] {
                advertiserId, campaignStatus, bidType, dateRange });

        return getNumberOfRecordsForDashboardReportingTable(advertiserId, null, campaignStatus, bidType, dateRange,showDeletedCampaigns);
    }

    @Override
    public Long getNumberOfRecordsForDashboardReportingTableForCampaigns(Long advertiserId, List<Long> campaignIds,
            CampaignStatus campaignStatus, BidType bidType, int dateRange,boolean showDeletedCampaigns) {
        LOGGER.debug("params: advertiserId={}, campaignIds={}, campaignStatus={}, bidType={}, dateRange={}",
                new Object[] { advertiserId, campaignIds, campaignStatus, bidType, dateRange });
        return getNumberOfRecordsForDashboardReportingTable(advertiserId, Utils.getDelimitedIds(campaignIds), campaignStatus,
                bidType, dateRange,showDeletedCampaigns);
    }
            
    private Long getNumberOfRecordsForDashboardReportingTable(Long advertiserId, String campaignIds,
            CampaignStatus campaignStatus, BidType bidType, int dateRange,boolean showDeletedCampaigns) {
        DashboardReportTableNumberOfRecordsStoredProcedure procedure = new DashboardReportTableNumberOfRecordsStoredProcedure(
                getDataSource(), "proc_return_adv_dshbrd_detail_record_count");

        int deletedCampaigns = 0;
        if(showDeletedCampaigns){
            deletedCampaigns=1;
        }
        Map<String, Object> data = procedure.execute(advertiserId, campaignIds, ((Object)campaignStatus.getStatus()), ((Object)bidType.getId()), dateRange,deletedCampaigns);

        Long result  = (Long) data.get("result");
        LOGGER.debug("Returning result - {}", result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adfonic.presentation.dashboard.impl.AdvertiserDashboardDao#
     * getDashboardReportingTableForAdvertiser(java.lang.Integer,
     * java.util.Date, java.util.Date,
     * com.adfonic.presentation.dashboard.DashboardParameters.SortBy)
     */
    @Override
    public List<StatisticsDto> getDashboardReportingTableForAdvertiser(Long advertiserId, CampaignStatus campaignStatus,
            BidType bidType, int dateRange, SortBy sortBy, OrderBy orderBy, Long start, Long numberOfRecords,boolean showDeletedCampaigns) {
        LOGGER.debug("params: advertiserId={}, campaignStatus={}, bidType={}, dateRange={}, sortBy={}, orderBy={}",
                new Object[] { advertiserId, campaignStatus, bidType, dateRange, sortBy, orderBy });
        
        return getDashboardReportingTable(advertiserId, null, campaignStatus, bidType, dateRange, sortBy, orderBy,
                start, numberOfRecords,showDeletedCampaigns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adfonic.presentation.dashboard.impl.AdvertiserDashboardDao#
     * getDashboardReportingTableForCampaigns(java.lang.Integer, java.util.List,
     * java.util.Date, java.util.Date,
     * com.adfonic.presentation.dashboard.DashboardParameters.SortBy)
     */
    @Override
    public List<StatisticsDto> getDashboardReportingTableForCampaigns(Long advertiserId, List<Long> campaignIds,
    		CampaignStatus campaignStatus, BidType bidType, int dateRange, SortBy sortBy, OrderBy orderBy, Long start,
            Long numberOfRecords,boolean showDeletedCampaigns) {
        LOGGER.debug(
                "params: advertiserId={}, campaignIds={}, campaignStatus={}, bidType={}, dateRange={}, sortBy={}, orderBy={}",
                new Object[] { advertiserId, campaignIds, campaignStatus, bidType, dateRange, sortBy, orderBy });
        return getDashboardReportingTable(advertiserId, Utils.getDelimitedIds(campaignIds), campaignStatus, bidType, dateRange,
                sortBy, orderBy, start, numberOfRecords,showDeletedCampaigns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adfonic.presentation.dashboard.impl.AdvertiserDashboardDao#
     * getChartDataForAdvertiser(java.lang.Integer, java.util.Date,
     * java.util.Date,
     * com.adfonic.presentation.dashboard.DashboardParameters.Report,
     * com.adfonic.presentation.dashboard.DashboardParameters.Interval)
     */
    @Override
    public Map<Object, Number> getChartDataForAdvertiser(Long advertiserId, int dateRange, Report report, CampaignStatus campaignStatus,
            BidType bidType,boolean showDeletedCampaigns) {
        LOGGER.debug("params: advertiserId={}, campaignStatus={}, bidType={}, dateRange={}, report={}", new Object[] { advertiserId,  campaignStatus, bidType,
                dateRange, report}); 
        return getChartData(advertiserId, null, campaignStatus, bidType, dateRange, report,showDeletedCampaigns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adfonic.presentation.dashboard.impl.AdvertiserDashboardDao#
     * getChartDataForCampaign(java.lang.Integer, java.util.List,
     * java.util.Date, java.util.Date,
     * com.adfonic.presentation.dashboard.DashboardParameters.Report,
     * com.adfonic.presentation.dashboard.DashboardParameters.Interval)
     */
    @Override
    public Map<Object, Number> getChartDataForCampaign(Long advertiserId, List<Long> campaignIds, CampaignStatus campaignStatus,
            BidType bidType, int dateRange, Report report,boolean showDeletedCampaigns) {
        LOGGER.debug("params: advertiserId={}, campaignId={}, campaignStatus={}, bidType={}, dateRange={}, report={}", new Object[] {
                advertiserId, campaignIds, dateRange, report});
        return getChartData(advertiserId, Utils.getDelimitedIds(campaignIds), campaignStatus, bidType, dateRange, report,showDeletedCampaigns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adfonic.presentation.dashboard.impl.AdvertiserDashboardDao#
     * getHeadlineFiguresForAdvertiser(java.lang.Integer, java.util.Date,
     * java.util.Date)
     */
    @Override
    public AdvertiserHeadlineStatsDto getHeadlineFiguresForAdvertiser(Long advertiserId, CampaignStatus campaignStatus, BidType bidType, int dateRange,boolean showDeletedCampaigns) {
        LOGGER.debug("params: advertiserId={}, campaignStatus={}, bidType={}, dateRange={}", new Object[] { advertiserId, campaignStatus, bidType, dateRange });
        return getHeadlineFigures(advertiserId, null, campaignStatus, bidType, dateRange,showDeletedCampaigns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.adfonic.presentation.dashboard.impl.AdvertiserDashboardDao#
     * getHeadlineFiguresForCampaigns(java.lang.Integer, java.lang.String,
     * java.util.Date, java.util.Date)
     */
    @Override
    public AdvertiserHeadlineStatsDto getHeadlineFiguresForCampaigns(Long advertiserId, List<Long> campaignIds, CampaignStatus campaignStatus,
            BidType bidType, int dateRange,boolean showDeletedCampaigns) {
        LOGGER.debug("params: advertiserId={}, campaignId={}, campaignStatus={}, bidType={}, dateRange={}, report={}, interval={}", new Object[] {
                advertiserId, campaignIds, campaignStatus, bidType, dateRange });
        return getHeadlineFigures(advertiserId, Utils.getDelimitedIds(campaignIds), campaignStatus, bidType, dateRange,showDeletedCampaigns);
    }

    @SuppressWarnings("unchecked")
    private List<StatisticsDto> getDashboardReportingTable(Long advertiserId, String campaignIds,
    		CampaignStatus campaignStatus, BidType bidType, int dateRange, SortBy sortBy, OrderBy orderBy, Long start,
            Long numberOfRecords,boolean showDeletedCampaigns) {
        DashboardReportTableStoredProcedure procedure = new DashboardReportTableStoredProcedure(getDataSource(),
                "proc_return_adv_dshbrd_detail_figures");

        //fix over the object..if we leave the call as campaignStatus.getStatus() --> when null its behaviour is like a string, so searcher for a status=null as string
        //hence, no results with status=null
        String sort;
        if(sortBy.equals(SortBy.SPEND)){
            sort = null;
        }
        else{
            sort = sortBy.getDbValue();
        }
        int deletedCampaigns = 0;
        if(showDeletedCampaigns){
            deletedCampaigns=1;
        }
        Map<String, Object> data = procedure.execute(advertiserId, campaignIds, ((Object)campaignStatus.getStatus()), ((Object)bidType.getId()), dateRange, 
                sort, orderBy.getDbValue(), numberOfRecords, start,deletedCampaigns);

        List<StatisticsDto> result = (List<StatisticsDto>) data.get("result");
        
        // Checking if campaign has any creative rejected
        if (result!=null){
            result.forEach(statisticsDto -> {
                boolean isAnyCreativeRejected = campaignService.hasAllCreativeRejected(statisticsDto.getCampaignId());
                statisticsDto.setAllCreativeRejected(isAnyCreativeRejected);
            });
        }
        
        LOGGER.debug("Returning result - {}", result);
        return result;

    }

    private AdvertiserHeadlineStatsDto getHeadlineFigures(Long advertiserId, String campaignIds, CampaignStatus campaignStatus, BidType bidType, int dateRange,boolean showDeletedCampaigns) {
        HeadlineDataStoredProcedure procedure = new HeadlineDataStoredProcedure(getDataSource(),
                "proc_return_adv_dshbrd_headline_figures");

        int deletedCampaigns = 0;
        if(showDeletedCampaigns){
            deletedCampaigns=1;
        }
         Map<String, Object> data = procedure.execute(advertiserId, campaignIds, ((Object)campaignStatus.getStatus()), ((Object)bidType.getId()), dateRange, deletedCampaigns);
        AdvertiserHeadlineStatsDto result = (AdvertiserHeadlineStatsDto) data.get("result");
        LOGGER.debug("Returning result - {}", result);
        return result;
    }

    private Map<Object, Number> getChartData(Long advertiserId, String campaignId, CampaignStatus campaignStatus, BidType bidType, int dateRange, Report report,boolean showDeletedCampaigns) {
        ChartDataStoredProcedure procedure = new ChartDataStoredProcedure(getDataSource(),
                "proc_return_adv_dshbrd_chart_figures");

        int deletedCampaigns = 0;
        if(showDeletedCampaigns){
            deletedCampaigns=1;
        }
        Map<String, Object> data = procedure.execute(advertiserId, campaignId, report.getDbValue(), ((Object)campaignStatus.getStatus()), ((Object)bidType.getId()), dateRange,deletedCampaigns);

        @SuppressWarnings("unchecked")
        Map<Object, Number> result = (Map<Object, Number>) data.get("result");
        LOGGER.debug("Returning result - {}", result);
        return result;
    }

    class DashboardReportTableStoredProcedure extends StoredProcedure {

        public DashboardReportTableStoredProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
            declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_cam_status", Types.VARCHAR));
            declareParameter(new SqlParameter("in_cam_type", Types.VARCHAR));
            declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
            declareParameter(new SqlParameter("in_sort_column", Types.NUMERIC));
            declareParameter(new SqlParameter("in_sort_direction", Types.VARCHAR));
            declareParameter(new SqlParameter("in_records_per_page", Types.NUMERIC));
            declareParameter(new SqlParameter("in_start_page", Types.NUMERIC));
            declareParameter(new SqlParameter("in_show_deleted_campaigns", Types.NUMERIC));
            declareParameter(new SqlReturnResultSet("result", new StatisticsDtoRowMapper()));
            compile();
        }
    }

    class DashboardReportTableNumberOfRecordsStoredProcedure extends StoredProcedure {

        public DashboardReportTableNumberOfRecordsStoredProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
            declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_cam_status", Types.VARCHAR));
            declareParameter(new SqlParameter("in_cam_type", Types.VARCHAR));           
            declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
            declareParameter(new SqlParameter("in_show_deleted_campaigns", Types.NUMERIC));
            declareParameter(new SqlReturnResultSet("result", new RecordCountResultSetExtractor()));
            compile();
        }
    }

    class HeadlineDataStoredProcedure extends StoredProcedure {

        public HeadlineDataStoredProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
            declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_cam_status", Types.VARCHAR));
            declareParameter(new SqlParameter("in_cam_type", Types.VARCHAR));
            declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
            declareParameter(new SqlParameter("in_show_deleted_campaigns", Types.NUMERIC));
            declareParameter(new SqlReturnResultSet("result", new HeadlineDataResultSetExtractor()));
            compile();
        }
    }

    class ChartDataStoredProcedure extends StoredProcedure {

        public ChartDataStoredProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_adv_id", Types.NUMERIC));
            declareParameter(new SqlParameter("in_cam_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_report_type", Types.NUMERIC));
            declareParameter(new SqlParameter("in_cam_status", Types.VARCHAR));
            declareParameter(new SqlParameter("in_cam_type", Types.VARCHAR));
            declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
            declareParameter(new SqlParameter("in_show_deleted_campaigns", Types.NUMERIC));
            declareParameter(new SqlReturnResultSet("result", new ChartDataResultSetExtractor()));
            compile();
        }
    }

    class HeadlineDataResultSetExtractor implements ResultSetExtractor<AdvertiserHeadlineStatsDto> {

        public AdvertiserHeadlineStatsDto extractData(ResultSet rs) throws SQLException, DataAccessException {
            AdvertiserHeadlineStatsDto result = new AdvertiserHeadlineStatsDto();
            if (rs.next()) { // there should be only one row
                result.setClicks(rs.getLong("clicks"));
                result.setConversions(rs.getLong("conversions"));
                result.setCtr(rs.getDouble("ctr"));
                result.setImpressions(rs.getLong("impressions"));
                result.setSpend(rs.getDouble("spend"));
            }
            return result;
        }
    }
    
    class StatisticsDtoRowMapper implements RowMapper<StatisticsDto> {
        @Override
        public StatisticsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            StatisticsDto tableRow = new StatisticsDto();
            tableRow.setBidPrice(rs.getDouble("bid_setting"));
            tableRow.setBudgetSpent(rs.getDouble("spend"));
            tableRow.setCampaignName(rs.getString("NAME"));
            tableRow.setCampaignId(rs.getLong("ID"));
            tableRow.setCpa(rs.getDouble("cpa"));
            tableRow.setCpm(rs.getDouble("cpm"));
            tableRow.setSpend(rs.getDouble("spend"));
            tableRow.setStatus(rs.getString("STATUS"));
            if (rs.getString("BID_TYPE") != null) { // this can be null
            	tableRow.setBidType(BidType.valueOf(rs.getString("BID_TYPE")));
            }	
            tableRow.setCtr(rs.getDouble("ctr"));
            tableRow.setCvr(rs.getDouble("cvr"));
            String totalBudget = rs.getString("OVERALL_BUDGET_CAP");
            if(totalBudget!=null){
                tableRow.setTotalBudget(Double.valueOf(totalBudget));
            }
            tableRow.setSpendYesterday(rs.getDouble("campaign_spend_yesterday"));
            tableRow.setTotalSpend(rs.getDouble("campaign_total_spend"));
            String dailyCap = rs.getString("DAILY_BUDGET_CAP");
            if(dailyCap!=null){
                tableRow.setDailyCap(Double.valueOf(dailyCap));
            }
            tableRow.setBudgetUnit(rs.getString("budget_type"));
            String spendToday = rs.getString("campaign_spend_today");
            if(spendToday!=null){
                tableRow.setBudgetDeliveredToday(Double.valueOf(spendToday));
            }
            tableRow.setEvenDistributionOverallBudget(rs.getBoolean("EVEN_DISTRIBUTION_OVERALL_BUDGET"));
            tableRow.setEvenDistributionDailyBudget(rs.getBoolean("EVEN_DISTRIBUTION_DAILY_BUDGET"));
            tableRow.setPriceOverridden(rs.getBoolean("PRICE_OVERRIDDEN"));
            return tableRow;
        }
    }

    class ChartDataResultSetExtractor implements ResultSetExtractor<Map<Object, Number>> {

        private SimpleDateFormat dateFormatter = null;

        public Map<Object, Number> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Object, Number> results = new LinkedHashMap<Object, Number>();
            while (rs.next()) {
                String timeId = rs.getString("ADVERTISER_TIME_ID");
                
                if (timeId == null) {
                	LOGGER.debug("Processing ResultSet and the timeId is NULL, so continuing traversing the ResultSet.");
                	continue;
                }
                
                Double data = rs.getDouble(2); // as we do not know the column
                                               // name
                results.put(getTimeIdAsTimeStamp(timeId), data);
            }
            return results;
        }

        private Timestamp getTimeIdAsTimeStamp(String timeId) {
            if (dateFormatter == null) {
                dateFormatter = new SimpleDateFormat("yyyyMMddHH");
            }
            
            if (timeId == null) {
            	LOGGER.warn("timeId was null. Returning null");
            	return null;
            }

            try {
                return new Timestamp(dateFormatter.parse(timeId).getTime());
            }
            catch (ParseException e) {
                LOGGER.warn("Unable to parse={}. Returning null", timeId);
                return null;
            }
        }
    }
}

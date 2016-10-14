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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.dto.advertiser.enums.AdvertiserStatus;
import com.adfonic.dto.dashboard.DashboardParameters.AgencyConsoleSortBy;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.DashboardParameters.SortBy;
import com.adfonic.dto.dashboard.statistic.AgencyConsoleStatisticsDto;
import com.adfonic.presentation.dashboard.statistics.AgencyConsoleDashboardDao;
import com.adfonic.presentation.sql.mappers.RecordCountResultSetExtractor;
import com.adfonic.presentation.util.Utils;

/**
 * DAO class for the Advertiser Dashboard.
 * 
 * @author antonysohal
 */

public class AgencyConsoleDashboardDaoImpl extends JdbcDaoSupport implements AgencyConsoleDashboardDao {

    private static Logger LOGGER = LoggerFactory.getLogger(AdvertiserDashboardDaoImpl.class);
    
    @Override
    public Long getNumberOfRecordsForDashboardReportingTable(List<Long> advertisersIds, AdvertiserStatus advertiserStatus, int dateRange) {
        LOGGER.debug("params: advertisersIds={}, advertiserStatus={},  dateRange={}", new Object[] {
                advertisersIds, advertiserStatus, dateRange });

        DashboardReportTableNumberOfRecordsStoredProcedure procedure = new DashboardReportTableNumberOfRecordsStoredProcedure(
                getDataSource(), "proc_return_agncy_cnsl_detail_record_count");

        Map<String, Object> data = procedure.execute(Utils.getDelimitedIds(advertisersIds), ((Object)advertiserStatus.getStatus()), dateRange);

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
    public List<AgencyConsoleStatisticsDto> getDashboardReportingTable(List<Long> advertisersIds, AdvertiserStatus advertiserStatus, int dateRange, 
            AgencyConsoleSortBy sortBy, OrderBy orderBy, Long start, Long numberOfRecords){
        LOGGER.debug("params: advertisersIds={}, advertiserStatus={}, dateRange={}, sortBy={}, orderBy={}, start={}, numberOfRecords={}",
                new Object[] { advertisersIds, advertiserStatus, dateRange, sortBy, orderBy, start, numberOfRecords});
        
        DashboardReportTableStoredProcedure procedure = new DashboardReportTableStoredProcedure(getDataSource(),
                "proc_return_agncy_cnsl_detail_figures");

        //fix over the object..if we leave the call as campaignStatus.getStatus() --> when null its behaviour is like a string, so searcher for a status=null as string
        //hence, no results with status=null
        String sort;
        if(sortBy.equals(SortBy.SPEND)){
            sort = null;
        }
        else{
            sort = sortBy.getDbValue();
        }
        Map<String, Object> data = procedure.execute(Utils.getDelimitedIds(advertisersIds), ((Object)advertiserStatus.getStatus()), dateRange, sort, 
                orderBy.getDbValue(), numberOfRecords, start);

        @SuppressWarnings("unchecked")
        List<AgencyConsoleStatisticsDto> result = (List<AgencyConsoleStatisticsDto>) data.get("result");
        LOGGER.debug("Returning result - {}", result);
        return result;
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
    public Map<Object, Number> getChartData(List<Long> advertisersIds, AdvertiserStatus advertiserStatus, int dateRange) {
        LOGGER.debug("params: advertisersIds={}, advertiserStatus={}, dateRange={}", new Object[] { advertisersIds,  advertiserStatus, dateRange}); 
        
        ChartDataStoredProcedure procedure = new ChartDataStoredProcedure(getDataSource(),
                "proc_return_agncy_cnsl_chart_figures");

        Map<String, Object> data = procedure.execute(Utils.getDelimitedIds(advertisersIds), ((Object)advertiserStatus.getStatus()), dateRange);

        @SuppressWarnings("unchecked")
        Map<Object, Number> result = (Map<Object, Number>) data.get("result");
        LOGGER.debug("Returning result - {}", result);
        return result;
    }

   

    class DashboardReportTableStoredProcedure extends StoredProcedure {

        public DashboardReportTableStoredProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_adv_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_adv_status", Types.VARCHAR));
            declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
            declareParameter(new SqlParameter("in_sort_column", Types.NUMERIC));
            declareParameter(new SqlParameter("in_sort_direction", Types.VARCHAR));
            declareParameter(new SqlParameter("in_records_per_page", Types.NUMERIC));
            declareParameter(new SqlParameter("in_start_page", Types.NUMERIC));
            declareParameter(new SqlReturnResultSet("result", new StatisticsDtoRowMapper()));
            compile();
        }
    }

    class DashboardReportTableNumberOfRecordsStoredProcedure extends StoredProcedure {

        public DashboardReportTableNumberOfRecordsStoredProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_adv_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_adv_status", Types.VARCHAR));
            declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
            declareParameter(new SqlReturnResultSet("result", new RecordCountResultSetExtractor()));
            compile();
        }
    }

    class ChartDataStoredProcedure extends StoredProcedure {

        public ChartDataStoredProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_adv_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_adv_status", Types.VARCHAR));
            declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
            declareParameter(new SqlReturnResultSet("result", new ChartDataResultSetExtractor()));
            compile();
        }
    }
    
    class StatisticsDtoRowMapper implements RowMapper<AgencyConsoleStatisticsDto> {
        @Override
        public AgencyConsoleStatisticsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            AgencyConsoleStatisticsDto tableRow = new AgencyConsoleStatisticsDto();
            tableRow.setAdvertiserName(rs.getString("NAME"));
            tableRow.setAdvertiserId(rs.getLong("ID"));
            tableRow.setStatus(rs.getString("STATUS"));
            tableRow.setImpressions(rs.getLong("impressions"));
            tableRow.setClicks(rs.getLong("clicks"));
            tableRow.setSpend(rs.getDouble("spend"));
            tableRow.setSpendYesterday(rs.getDouble("advertiser_spend_yesterday"));
            tableRow.setBalance(rs.getDouble("balance"));
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

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

import com.adfonic.dto.dashboard.DashboardParameters.Interval;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherReport;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherSortBy;
import com.adfonic.dto.dashboard.statistic.PublisherHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.PublisherStatisticsDto;
import com.adfonic.dto.publication.enums.Approval;
import com.adfonic.dto.publication.enums.Backfill;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.presentation.dashboard.statistics.PublisherDashboardDao;
import com.adfonic.presentation.sql.mappers.RecordCountResultSetExtractor;
import com.adfonic.presentation.util.Utils;

/**
 * DAO class for the Advertiser Dashbaord.
 * 
 * @author antonysohal
 */

public class PublisherDashboardDaoImpl extends JdbcDaoSupport implements PublisherDashboardDao {

	private static Logger LOGGER = LoggerFactory.getLogger(PublisherDashboardDaoImpl.class);

	@Override
	public Long getNumberOfRecordsForDashboardReportingTableForPublisher(Long publisherId,
			PublicationStatus publicationStatus, PublicationtypeDto platform, Approval approval, Backfill backFill, int dateRange, PublisherSortBy sortBy) {

		LOGGER.debug("params: publisherId={}, publicationStatus={}, platform={}, approval{}, backfill, dateRange={}",
				new Object[] { publisherId, publicationStatus, platform, approval, backFill, dateRange });

		return getNumberOfRecordsForDashboardReportingTable(publisherId, null, publicationStatus, platform, approval,
				backFill, dateRange, sortBy);
	}

	@Override
	public Long getNumberOfRecordsForDashboardReportingTableForPublications(Long publisherId,
			List<Long> publicationIds, PublicationStatus publicationStatus, PublicationtypeDto platform, Approval approval,
			Backfill backFill, int dateRange, PublisherSortBy sortBy) {

		LOGGER.debug(
				"params: publisherId={}, publicationIds={}, publicationStatus={}, platform={}, approval{}, backfill, dateRange{}",
				new Object[] { publisherId, publicationIds, publicationStatus, platform, approval, backFill, dateRange });

		return getNumberOfRecordsForDashboardReportingTable(publisherId, Utils.getDelimitedIds(publicationIds),
				publicationStatus, platform, approval, backFill, dateRange, sortBy);
	}

	private Long getNumberOfRecordsForDashboardReportingTable(Long publisherId, String publicationIds,
			PublicationStatus publicationStatus, PublicationtypeDto platformDto, Approval approval, Backfill backFill,
			int dateRange, PublisherSortBy sortBy) {

		DashboardReportTableNumberOfRecordsStoredProcedure procedure = new DashboardReportTableNumberOfRecordsStoredProcedure(
				getDataSource(), "proc_return_pub_dshbrd_detail_record_count");

		Long platformId = null;
		if (platformDto.getId() != -1){
			platformId = platformDto.getId();
		}

		Map<String, Object> data = procedure.execute(publisherId, publicationIds, publicationStatus.getStatus(),
				platformId, approval.getId(), backFill.getId(), dateRange, sortBy.getDbValue());

        Long result  = (Long) data.get("result");
        LOGGER.debug("Returning result - {}", result);
        return result;
	}

	public List<PublisherStatisticsDto> getDashboardReportingTableForPublisher(Long publisherId,
			PublicationStatus publicationStatus, PublicationtypeDto platformDto, Approval approval, Backfill backFill,
			int dateRange, PublisherSortBy sortBy, OrderBy orderBy, Long start, Long numberOfRecords) {
		LOGGER.debug(
				"params: publisherId={}, publicationStatus={}, platformDto={}, approval={}, backFill={}, dateRange={}, sortBy={}, orderBy={}, start={}, numberOfRecords={}",
				new Object[] { publisherId, publicationStatus, platformDto, approval, backFill, dateRange, sortBy,
						orderBy, start, numberOfRecords });

		return getDashboardReportingTable(publisherId, null, publicationStatus, platformDto, approval, backFill, dateRange, sortBy, orderBy, start, numberOfRecords);
	}

	public List<PublisherStatisticsDto> getDashboardReportingTableForPublications(Long publisherId,
			List<Long> publicationIds, PublicationStatus publicationStatus, PublicationtypeDto platformDto, Approval approval,
			Backfill backFill, int dateRange, PublisherSortBy sortBy, OrderBy orderBy, Long start,
			Long numberOfRecords) {
		LOGGER.debug(
				"params: publisherId={}, publicationIds={}, publicationStatus={}, platformDto={}, approval={}, backFill={}, dateRange={}, sortBy={}, orderBy={}, start={}, numberOfRecords={}",
				new Object[] { publisherId, publicationIds, publicationStatus, platformDto, approval, backFill, dateRange, sortBy, orderBy, start, numberOfRecords });

		return getDashboardReportingTable(publisherId, Utils.getDelimitedIds(publicationIds), publicationStatus,
				platformDto, approval, backFill, dateRange, sortBy, orderBy, start, numberOfRecords);
	}

	public Map<Object, Number> getChartDataForPublisher(Long publisherId, PublicationStatus publicationStatus, PublicationtypeDto platformDto, 
	        Approval approval, Backfill backFill,int dateRange, PublisherReport report,Interval interval) {
		LOGGER.debug("params: publisherId={}, publicationStatus={}, platformDto={}, approval={}, backFill={}, dateRange={}, report={}, interval={}", new Object[] { publisherId,
		        dateRange, report, interval });
		return getChartData(publisherId, null, publicationStatus, platformDto, approval, backFill, dateRange, report, interval);
	}

	public Map<Object, Number> getChartDataForPublication(Long publisherId, List<Long> publicationIds, PublicationStatus publicationStatus, 
	        PublicationtypeDto platformDto, Approval approval,Backfill backFill, int dateRange, PublisherReport report, Interval interval) {
		return getChartData(publisherId, Utils.getDelimitedIds(publicationIds), publicationStatus, platformDto, approval, backFill, dateRange, report, interval);
	}

	public PublisherHeadlineStatsDto getHeadlineFiguresForPublisher(Long publisherId, PublicationStatus publicationStatus, PublicationtypeDto platformDto, Approval approval,
            Backfill backFill, int dateRange) {
		LOGGER.debug("params: publisherId={}, publicationStatus={}, platformDto={}, approval={}, backFill={}, dateRange={}", new Object[] { publisherId, dateRange });
		return getHeadlineFigures(publisherId, null, publicationStatus, platformDto, approval, backFill, dateRange);
	}

	public PublisherHeadlineStatsDto getHeadlineFiguresForPublications(Long publisherId, List<Long> publicationIds,
	        PublicationStatus publicationStatus, PublicationtypeDto platformDto, Approval approval, Backfill backFill,int dateRange) {
		LOGGER.debug("params: publisherId={}, publisherIds={}, publicationStatus={}, platformDto={}, approval={}, backFill={}, dateRange={}", new Object[] { publisherId,
				publicationIds, publicationStatus, platformDto, approval, backFill, dateRange });
		return getHeadlineFigures(publisherId, Utils.getDelimitedIds(publicationIds), publicationStatus, platformDto, approval, backFill, dateRange);
	}

	private PublisherHeadlineStatsDto getHeadlineFigures(Long publisherId, String publicationIds, PublicationStatus publicationStatus, 
	        PublicationtypeDto platformDto, Approval approval, Backfill backFill, int dateRange) {
		HeadlineDataStoredProcedure procedure = new HeadlineDataStoredProcedure(getDataSource(),
				"proc_return_pub_dshbrd_headline_figures");

		Long platformId = null;
        if (platformDto.getId() != -1) {
            platformId = platformDto.getId();
        }

		Map<String, Object> data = procedure.execute(publisherId, publicationIds, ((Object) publicationStatus.getStatus()), platformId, approval.getId(), backFill.getId(),
		        dateRange);
		PublisherHeadlineStatsDto result = (PublisherHeadlineStatsDto) data.get("result");

		LOGGER.debug("Returning result - {}", result);
		return result;
	}

	private Map<Object, Number> getChartData(Long publisherId, String publicationIds, PublicationStatus publicationStatus, PublicationtypeDto platformDto, 
	        Approval approval, Backfill backFill, int dateRange, PublisherReport report, Interval interval) {
		ChartDataStoredProcedure procedure = new ChartDataStoredProcedure(getDataSource(),
				"proc_return_pub_dshbrd_chart_figures");

		Long platformId = null;
        if (platformDto.getId() != -1) {
            platformId = platformDto.getId();
        }

		Map<String, Object> data = procedure.execute(publisherId, publicationIds, report.getDbValue(), ((Object) publicationStatus.getStatus()), platformId, approval.getId(), 
		        backFill.getId(), dateRange, interval.getDbValue());

		@SuppressWarnings("unchecked")
		Map<Object, Number> result = (Map<Object, Number>) data.get("result");

		LOGGER.debug("Returning result - {}", result);
		return result;
	}

	private List<PublisherStatisticsDto> getDashboardReportingTable(Long publisherId, String publicationIds,
			PublicationStatus publicationStatus, PublicationtypeDto platform, Approval approval, Backfill backFill, int dateRange, PublisherSortBy sortBy, OrderBy orderBy, Long start, Long numberOfRecords) {

		DashboardReportTableStoredProcedure procedure = new DashboardReportTableStoredProcedure(getDataSource(),
				"proc_return_pub_dshbrd_detail_figures");

		Long platformId = null;
		if (platform.getId() != -1) {
			platformId = platform.getId();
		}

		Map<String, Object> data = procedure.execute(publisherId, publicationIds,
				((Object) publicationStatus.getStatus()), platformId, approval.getId(), backFill.getId(), dateRange, sortBy.getDbValue(), orderBy.getDbValue(), numberOfRecords, start);

		@SuppressWarnings("unchecked")
		List<PublisherStatisticsDto> result = (List<PublisherStatisticsDto>) data.get("result");
		LOGGER.debug("Returning result - {}", result);
		return result;
	}

	class HeadlineDataStoredProcedure extends StoredProcedure {

		public HeadlineDataStoredProcedure(DataSource dataSource, String procedureCall) {
			super(dataSource, procedureCall);
			declareParameter(new SqlParameter("in_pub_id", Types.NUMERIC));
			declareParameter(new SqlParameter("in_pubn_ids", Types.VARCHAR));
			declareParameter(new SqlParameter("in_pubn_status", Types.VARCHAR));
            declareParameter(new SqlParameter("in_pubn_type_id", Types.NUMERIC));
            declareParameter(new SqlParameter("in_pubn_appr", Types.NUMERIC));
            declareParameter(new SqlParameter("in_pubn_bfill", Types.NUMERIC));
            declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
			declareParameter(new SqlReturnResultSet("result", new HeadlineDataResultSetExtractor()));
			compile();
		}
	}

	class HeadlineDataResultSetExtractor implements ResultSetExtractor<PublisherHeadlineStatsDto> {

		public PublisherHeadlineStatsDto extractData(ResultSet rs) throws SQLException, DataAccessException {
			PublisherHeadlineStatsDto result = new PublisherHeadlineStatsDto();
			if (rs.next()) { // there should be only one row
				result.setRequests(rs.getLong("requests"));
				result.setImpressions(rs.getLong("impressions"));
				result.setFillRate(rs.getDouble("fill_rate"));
				result.setEcpm(rs.getDouble("ecpm"));
				result.setRevenue(rs.getLong("revenue"));
			}
			return result;
		}

	}

	class StatisticsDtoRowMapper implements RowMapper<PublisherStatisticsDto> {
		@Override
		public PublisherStatisticsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
			PublisherStatisticsDto tableRow = new PublisherStatisticsDto();
			tableRow.setPublicationId(rs.getLong("ID"));
			tableRow.setStatus(rs.getString("STATUS"));
			tableRow.setPublicationName(rs.getString("NAME"));
			PublicationtypeDto dto = new PublicationtypeDto();
			dto.setName(rs.getString("publication_type"));
			tableRow.setPlatform(dto);
			tableRow.setApproval(Approval.value(rs.getString("AUTO_APPROVAL")));
			tableRow.setBackfill(Backfill.value(rs.getString("BACKFILL_ENABLED")));
			tableRow.setRequests(rs.getLong("requests"));
			tableRow.setImpressions(rs.getLong("impressions"));
			tableRow.setFillRate(rs.getDouble("fill_rate"));
			tableRow.setRevenue(rs.getLong("revenue"));
			tableRow.setEcpm(rs.getDouble("ecpm"));
			tableRow.setClicks(rs.getLong("clicks"));
			tableRow.setCtr(rs.getDouble("ctr"));
			return tableRow;
		}
	}

	class DashboardReportTableStoredProcedure extends StoredProcedure {

		public DashboardReportTableStoredProcedure(DataSource dataSource, String procedureCall) {
			super(dataSource, procedureCall);
			declareParameter(new SqlParameter("in_pub_id", Types.NUMERIC));
			declareParameter(new SqlParameter("in_pubn_ids", Types.VARCHAR));
			declareParameter(new SqlParameter("in_pubn_status", Types.VARCHAR));
			declareParameter(new SqlParameter("in_pubn_type_id", Types.NUMERIC));
			declareParameter(new SqlParameter("in_pubn_appr", Types.NUMERIC));
			declareParameter(new SqlParameter("in_pubn_bfill", Types.NUMERIC));
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
			declareParameter(new SqlParameter("in_pub_id", Types.NUMERIC));
			declareParameter(new SqlParameter("in_pubn_ids", Types.VARCHAR));
			declareParameter(new SqlParameter("in_pubn_status", Types.VARCHAR));
			declareParameter(new SqlParameter("in_pubn_type_id", Types.NUMERIC));
			declareParameter(new SqlParameter("in_pubn_appr", Types.NUMERIC));
			declareParameter(new SqlParameter("in_pubn_bfill", Types.NUMERIC));
			declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
			declareParameter(new SqlParameter("in_sort_column", Types.NUMERIC));
			declareParameter(new SqlReturnResultSet("result", new RecordCountResultSetExtractor()));
			compile();
		}
	}

	class ChartDataStoredProcedure extends StoredProcedure {

		public ChartDataStoredProcedure(DataSource dataSource, String procedureCall) {
			super(dataSource, procedureCall);
			declareParameter(new SqlParameter("in_pub_id", Types.NUMERIC));
			declareParameter(new SqlParameter("in_pubn_ids", Types.VARCHAR));
			declareParameter(new SqlParameter("in_report_type", Types.NUMERIC));
			declareParameter(new SqlParameter("in_pubn_status", Types.VARCHAR));
            declareParameter(new SqlParameter("in_pubn_type_id", Types.NUMERIC));
            declareParameter(new SqlParameter("in_pubn_appr", Types.NUMERIC));
            declareParameter(new SqlParameter("in_pubn_bfill", Types.NUMERIC));
            declareParameter(new SqlParameter("in_date_range", Types.NUMERIC));
			declareParameter(new SqlReturnResultSet("result", new ChartDataResultSetExtractor()));
			compile();
		}
	}

	class ChartDataResultSetExtractor implements ResultSetExtractor<Map<Object, Number>> {

		private SimpleDateFormat dateFormatter = null;

		public Map<Object, Number> extractData(ResultSet rs) throws SQLException, DataAccessException {
			Map<Object, Number> results = new LinkedHashMap<Object, Number>();
			while (rs.next()) {
				String timeId = rs.getString("PUBLISHER_TIME_ID");
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
				LOGGER.debug("timeId is null. Returning null");
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

package com.adfonic.presentation.publicationlist.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.dto.campaign.publicationlist.PublicationForListDto;
import com.adfonic.presentation.publicationlist.dao.PublicationSearchDao;
import com.adfonic.presentation.sql.mappers.AbstractLongResultSetExtractor;
import com.adfonic.presentation.util.Utils;

/**
 * DAO class for the Advertiser Dashboard.
 * 
 * @author antonysohal
 */

public class PublicationSearchDaoImpl extends JdbcDaoSupport implements PublicationSearchDao {

    private static Logger LOGGER = LoggerFactory.getLogger(PublicationSearchDaoImpl.class);
    
    @Override
    public Long getNumberOfRecordsForPublications(String searchString, int publicationType, List<Long> publicationIds){
        LOGGER.debug("params: searchString={}, publicationType={}, publicationIds={}",
                new Object[] { searchString, publicationType, publicationIds });
        
        PublicationSearchNumberOfRecordsStoredProcedure procedure = new PublicationSearchNumberOfRecordsStoredProcedure(
                getDataSource(), "proc_return_inv_target_list_record_count");

        Map<String, Object> data = procedure.execute(searchString, publicationType, Utils.getDelimitedIds(publicationIds));

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
    @SuppressWarnings("unchecked")
    public List<PublicationForListDto> getPublications(String searchString, int publicationType, Long numRecords, Long start, List<Long> publicationIds){
        LOGGER.debug("params: searchString={}, publicationType={}, numRecords={}, start={}, publicationIds={}",
                new Object[] { searchString, publicationType, numRecords, start, publicationIds });
        
        PublicationSearchStoredProcedure procedure = new PublicationSearchStoredProcedure(
                getDataSource(), "proc_return_inv_target_list");

        Map<String, Object> data = procedure.execute(searchString, publicationType, numRecords, start, Utils.getDelimitedIds(publicationIds));

        List<PublicationForListDto> result  = (List<PublicationForListDto>) data.get("result");
        LOGGER.debug("Returning result - {}", result);
        return result;
    }


    class PublicationSearchStoredProcedure extends StoredProcedure {

        public PublicationSearchStoredProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_pubn_search_str", Types.VARCHAR));
            declareParameter(new SqlParameter("in_pubn_type", Types.NUMERIC));
            declareParameter(new SqlParameter("in_records_per_page", Types.NUMERIC));
            declareParameter(new SqlParameter("in_start_page", Types.NUMERIC));
            declareParameter(new SqlParameter("in_exclude_list_pubn_ids", Types.VARCHAR));
            declareParameter(new SqlReturnResultSet("result", new PublicationSearchDtoRowMapper()));
            compile();
        }
    }

    class PublicationSearchNumberOfRecordsStoredProcedure extends StoredProcedure {

        public PublicationSearchNumberOfRecordsStoredProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_pubn_search_str", Types.VARCHAR));
            declareParameter(new SqlParameter("in_pubn_type", Types.NUMERIC));
            declareParameter(new SqlParameter("in_exclude_list_pubn_ids", Types.VARCHAR));
            declareParameter(new SqlReturnResultSet("result", new RecordCountResultSetExtractor()));
            compile();
        }
    }
    
    class RecordCountResultSetExtractor extends AbstractLongResultSetExtractor {
        @Override
        protected String getFieldResultSetColumnName() {
            return "inv_target_list_record_count";
        }
    }
    
    class PublicationSearchDtoRowMapper implements RowMapper<PublicationForListDto> {
        @Override
        public PublicationForListDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            PublicationForListDto tableRow = new PublicationForListDto();
            tableRow.setPublicationId(rs.getLong("publication_id"));
            tableRow.setName(rs.getString("publication_name"));
            tableRow.setFriendlyName(rs.getString("publication_friendly_name"));
            tableRow.setExternalId(rs.getString("publication_external_id"));
            tableRow.setDisplayName(rs.getString("display_name"));
            return tableRow;
        }
    }

    
}

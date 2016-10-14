package com.adfonic.weve.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.weve.dto.DeviceIdentifierTypeDto;
import com.adfonic.weve.dto.WeveOperatorDto;

public class BeaconServiceDaoImpl extends JdbcDaoSupport implements BeaconServiceDao {

    private static final String PROC_RESULT_PARAM = "result";
    private static final Logger LOG = LogManager.getLogger(BeaconServiceDaoImpl.class.getName());
    
    @Override
    @SuppressWarnings("unchecked")
    public List<WeveOperatorDto> getIpRangesAndHeaderNameForOperator() {
        OperatorIpRangesStoredProcedure proc = new OperatorIpRangesStoredProcedure(getDataSource());
        LOG.debug("Procedure Call [{}]", proc.getCallString());
        Map<String, Object> data = proc.execute();
        return (List<WeveOperatorDto>) data.get(PROC_RESULT_PARAM);
    }
    
    @Override
    public Long findWeveId(Integer operatorId, String endUserId) {
        // in_service_user_id, in_display_uid, returns display_service_esk as a resultSet
        WeveIdStoredProcedure proc = new WeveIdStoredProcedure(getDataSource(), "weve.proc_check_weve_id_exists");
        LOG.debug("Updated Procedure Call [{}] with [{}, {}]", proc.getCallString(), operatorId, endUserId);
        Map<String, Object> data = proc.execute(operatorId, endUserId);
        return (Long) ((List<?>) data.get(PROC_RESULT_PARAM)).get(0);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<DeviceIdentifierTypeDto> getDeviceIdsAndRegexValidationString() {
        DeviceIdentifierTypeStoredProcedure proc = new DeviceIdentifierTypeStoredProcedure(getDataSource());
        LOG.debug("Procedure Call [{}]", proc.getCallString());
        Map<String, Object> data = proc.execute();
        return (List<DeviceIdentifierTypeDto>) data.get(PROC_RESULT_PARAM);
    }
    
    @Override
    public Integer saveDeviceIds(Long weveId, String deviceIds, String adSpaceExternalId, String creativeExternalId) {
        // in_weve_id, in_device_ids e.g. device_id~device_type_id|device_id~device_type_id|device_id~device_type_id
        // MAD-828 Adding adSpace and creative external ids to correlate ads served with beacon requests
        RecordDeviceIdsAgainstWeveUserStoredProcedure proc = new RecordDeviceIdsAgainstWeveUserStoredProcedure(getDataSource(), "weve.proc_store_device_ids");
        LOG.debug("Procedure Call [proc_store_device_ids with weveId: {}, and deviceIds: {} and adSpaceId: {} and creativeId: {}]", 
                weveId, deviceIds, adSpaceExternalId, creativeExternalId);
        Map<String, Object> data = proc.execute(weveId, deviceIds, adSpaceExternalId, creativeExternalId);
        return (Integer) ((List<?>) data.get(PROC_RESULT_PARAM)).get(0);
    }
    
    @Override
    public Integer saveDeviceIdsForUnknownUser(String encodedEndUserId, Integer operatorId, String deviceIds, 
                                            String adSpaceExternalId, String creativeExternalId) {
        // in_service_user_id, in_display_uid, in_device_ids e.g. device_id~device_type_id|device_id~device_type_id
        RecordDeviceIdsAgainstEncodedEndUserStoredProcedure proc = 
                new RecordDeviceIdsAgainstEncodedEndUserStoredProcedure(getDataSource(), "weve.proc_store_device_ids_display_uid");
        LOG.debug("Procedure Call [proc_store_device_ids_display_uid with endUserId: {}, operatorId: {}, deviceIds: {}, adSpaceId: {}, creativeId: {}]", 
                encodedEndUserId, operatorId, deviceIds, adSpaceExternalId, creativeExternalId);
        Map<String, Object> data = proc.execute(encodedEndUserId, operatorId, deviceIds, adSpaceExternalId, creativeExternalId);
        return (Integer) ((List<?>) data.get(PROC_RESULT_PARAM)).get(0);
    }    
    
    static class OperatorIpRangesStoredProcedure extends SimpleJdbcCall {
        public OperatorIpRangesStoredProcedure(DataSource dataSource) {
            super(dataSource);
            withCatalogName("weve");
            withProcedureName("proc_return_operator_ip_ranges");
            setAccessCallParameterMetaData(false);
            declareParameters(new SqlReturnResultSet(PROC_RESULT_PARAM, new RowMapper<WeveOperatorDto>(){
                
                @Override
                public WeveOperatorDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new WeveOperatorDto(rs.getInt("service_user"), 
                                               rs.getLong("ip_address_start"), 
                                               rs.getLong("ip_address_end"), 
                                               rs.getString("header_name"),
                                               rs.getLong("decryption_method"),
                                               rs.getBoolean("bs_fine_logging_on"),
                                               rs.getBoolean("oo_fine_logging_on"));
                }
                
            }));
            compile();
        }
    }
    
    static class WeveIdStoredProcedure extends StoredProcedure {
        public WeveIdStoredProcedure(DataSource dataSource, String procedureName) {
            super(dataSource, procedureName);
            declareParameter(new SqlParameter("in_service_user_id", Types.INTEGER));
            declareParameter(new SqlParameter("in_display_uid", Types.VARCHAR));
            declareParameter(new SqlReturnResultSet(PROC_RESULT_PARAM, new RowMapper<Long>() {

                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("display_service_esk");
                }
                
            }));
            compile();
        }
    }
    
    static class DeviceIdentifierTypeStoredProcedure extends SimpleJdbcCall {
        public DeviceIdentifierTypeStoredProcedure(DataSource dataSource) {
            super(dataSource);
            withCatalogName("weve");
            withProcedureName("proc_return_device_identifer_types");
            setAccessCallParameterMetaData(false);
            declareParameters(new SqlReturnResultSet(PROC_RESULT_PARAM, new RowMapper<DeviceIdentifierTypeDto>(){

                @Override
                public DeviceIdentifierTypeDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new DeviceIdentifierTypeDto(rs.getInt("ID"),
                                                       rs.getString("SYSTEM_NAME"), 
                                                       rs.getString("VALIDATION_REGEX"),
                                                       rs.getBoolean("SECURE"));
                }
                
            }));
            compile();
        }
    }
    
    static class RecordDeviceIdsAgainstWeveUserStoredProcedure extends StoredProcedure {
        public RecordDeviceIdsAgainstWeveUserStoredProcedure(DataSource dataSource, String procedureName) {
            super(dataSource, procedureName);
            declareParameter(new SqlParameter("in_weve_id", Types.BIGINT));
            declareParameter(new SqlParameter("in_device_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_external_adspace_id", Types.VARCHAR));
            declareParameter(new SqlParameter("in_external_creative_id", Types.VARCHAR));
            declareParameter(new SqlReturnResultSet(PROC_RESULT_PARAM, new RowMapper<Integer>() {

                @Override
                public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getInt("record_inserted");
                }
                
            }));
            compile();
        }        
    }
    
    static class RecordDeviceIdsAgainstEncodedEndUserStoredProcedure extends StoredProcedure {
        public RecordDeviceIdsAgainstEncodedEndUserStoredProcedure(DataSource dataSource, String procedureName) {
            super(dataSource, procedureName);
            declareParameter(new SqlParameter("in_display_uid", Types.VARCHAR));
            declareParameter(new SqlParameter("in_service_user_id", Types.INTEGER));
            declareParameter(new SqlParameter("in_device_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_external_adspace_id", Types.VARCHAR));
            declareParameter(new SqlParameter("in_external_creative_id", Types.VARCHAR));
            declareParameter(new SqlReturnResultSet(PROC_RESULT_PARAM, new RowMapper<Integer>() {

                @Override
                public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getInt("record_inserted");
                }
                
            }));
            compile();
        }
    }
}

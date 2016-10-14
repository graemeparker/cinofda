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
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;

public class OptOutServiceDaoImpl extends JdbcDaoSupport implements OptOutServiceDao {
    
    private static final Logger LOG = LogManager.getLogger(OptOutServiceDaoImpl.class.getName());
    
    private static final String PROC_RESULT_PARAM = "result";
    
    @Override
    @SuppressWarnings("rawtypes")
    public Integer saveOptOut(String deviceIds, int optout) {
        OptOutChangeProcedure proc = new OptOutChangeProcedure(getDataSource(), "weve.proc_opt_change_device_ids");
        LOG.debug("Procedure Call [proc_opt_change_device_ids : {}]", deviceIds);
        Map<String, Object> data = proc.execute(deviceIds,optout); //0 = optout, 1 = optin
        return (Integer) ((List) data.get(PROC_RESULT_PARAM)).get(0);
    }

    /**
    * Opt out using the weve id (esk)
    */
    @Override
    @SuppressWarnings("rawtypes")
    public Integer saveOptOutEsk(String weveIds, int optout) {
        OptOutEskChangeProcedure proc = new OptOutEskChangeProcedure(getDataSource(), "weve.proc_opt_change_display_service_esks");
        LOG.debug("Procedure Call [proc_opt_change_display_service_esks : {}]", weveIds);
        Map<String, Object> data = proc.execute(weveIds,optout); //0 = optout, 1 = optin
        return (Integer) ((List) data.get(PROC_RESULT_PARAM)).get(0);
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Long findWeveId(String deviceId, int deviceIdType) {
        // in_service_user_id, in_display_uid, out_display_service_esk
        WeveIdStoredProcedure proc = new WeveIdStoredProcedure(getDataSource(), "weve.proc_return_display_service_esk_from_device_id");
        LOG.debug("Procedure Call [{}] with [{}, {}]", proc.getCallString(), deviceId, deviceIdType);
        Map<String, Object> data = proc.execute(deviceId, deviceIdType);
        return (Long) ((List) data.get(PROC_RESULT_PARAM)).get(0);
    }
    
    static class OptOutChangeProcedure extends StoredProcedure {
        public OptOutChangeProcedure(DataSource dataSource, String procedureName) {
            super(dataSource, procedureName);
            declareParameter(new SqlParameter("in_device_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_option", Types.INTEGER));
            declareParameter(new SqlReturnResultSet(PROC_RESULT_PARAM, new RowMapper<Integer>() {
                public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getInt("record_inserted");
                }
            }));
            compile();
        }
    }

    /**
     *  OptOut stored proc using weve id (esk)
     */
    static class OptOutEskChangeProcedure extends StoredProcedure {
        public OptOutEskChangeProcedure(DataSource dataSource, String procedureName) {
            super(dataSource, procedureName);
            declareParameter(new SqlParameter("in_display_service_esks", Types.VARCHAR));
            declareParameter(new SqlParameter("in_option", Types.INTEGER));
            declareParameter(new SqlReturnResultSet(PROC_RESULT_PARAM, new RowMapper<Integer>() {
                public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getInt("record_inserted");
                }
            }));
            compile();
        }
    }

    static class WeveIdStoredProcedure extends StoredProcedure {
        public WeveIdStoredProcedure(DataSource dataSource, String procedureName) {
            super(dataSource, procedureName);
            declareParameter(new SqlParameter("in_device_id", Types.VARCHAR));
            declareParameter(new SqlParameter("in_device_type_id", Types.INTEGER));
            declareParameter(new SqlReturnResultSet(PROC_RESULT_PARAM, new RowMapper<Long>() {
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("display_service_esk");
                }
            }));
            compile();
        }
    }
}

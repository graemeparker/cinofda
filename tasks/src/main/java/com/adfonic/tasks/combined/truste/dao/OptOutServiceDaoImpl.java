package com.adfonic.tasks.combined.truste.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.stereotype.Component;

import com.adfonic.util.ConfUtils;

@Component
public class OptOutServiceDaoImpl extends JdbcDaoSupport implements OptOutServiceDao {

    private final transient Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier(ConfUtils.OPTOUT_DS)
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        super.setDataSource(dataSource);
    }

    @Override
    public Integer saveUserPreferences(String deviceIds, boolean optinFlag) {

        try {
            OptOutChangeProcedure proc = new OptOutChangeProcedure(getDataSource(), "proc_opt_change_device_ids");

            int opt = optinFlag ? 2 : 1;
            logger.info("Procedure Call [proc_opt_change_device_ids : {} flag:{} ]", deviceIds, optinFlag, 1);
            Map<String, Object> data = proc.execute(deviceIds, opt, 1); // 1 = opt out 2 = opt in
            Object result = data.get("result");
            List resultList = (List) result;

            logger.debug("Procedure Call result {}", resultList);
            return (Integer) resultList.get(0);
        } catch (Exception e) {
            logger.error("failed", deviceIds, e);
        }
        return -1;
    }

    class OptOutChangeProcedure extends StoredProcedure {
        public OptOutChangeProcedure(DataSource dataSource, String procedureName) {
            super(dataSource, procedureName);
            declareParameter(new SqlParameter("in_device_ids", Types.VARCHAR));
            declareParameter(new SqlParameter("in_option", Types.INTEGER));
            declareParameter(new SqlParameter("in_opt_out_type_id", Types.INTEGER));
            declareParameter(new SqlReturnResultSet("result", new RowMapper<Integer>() {
                @Override
                public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getInt("record_inserted");
                }
            }));
            compile();
        }
    }

}

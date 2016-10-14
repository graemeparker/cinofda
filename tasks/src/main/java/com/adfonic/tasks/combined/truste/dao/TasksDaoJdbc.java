package com.adfonic.tasks.combined.truste.dao;

import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.util.ConfUtils;

@Component
public class TasksDaoJdbc implements TasksDao {

    private final transient Logger logger = LoggerFactory.getLogger(getClass().getName());

    static final String LAST_RUN_TIME_PROPERTY_NAME = "truste-sync-tasks.last-run-time";

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource toolsDataSource;

    @Autowired
    @Qualifier(ConfUtils.TOOLS_JDBC_TEMPLATE)
    JdbcTemplate jdbcTemplate;

    private final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

    @Override
    public DateTime loadLastRunTime() throws IOException {
        logger.info("loading loadLastRunTime");

        try {
            List<String> values = jdbcTemplate.queryForList("SELECT PROPERTIES.PROPERTY_VALUE from PROPERTIES WHERE PROPERTIES.PROPERTY_NAME=?",
                    new Object[] { LAST_RUN_TIME_PROPERTY_NAME }, String.class);
            if (values.isEmpty()) {
                logger.warn("no {} in PROPERTIES, returning null", LAST_RUN_TIME_PROPERTY_NAME);
                return null;
            }

            if (values.size() > 1) {
                logger.warn("expected 0 or 1 element, got: {} elements", values.size());
            }

            String lastRunTime = values.get(0);
            if (StringUtils.isBlank(lastRunTime)) {
                logger.warn("the property {} exists but is blank", LAST_RUN_TIME_PROPERTY_NAME);
                return null;
            }

            DateTime lastRunDateTime = fmt.parseDateTime(lastRunTime);
            return lastRunDateTime;

        } catch (DataAccessException e) {
            logger.error("querying PROPERTIES failed", e);
        }

        return null;
    }

    @Override
    public void storeLastRunTime(DateTime lastRunTime) throws IOException {

        String runTimeFormatted = fmt.print(lastRunTime);
        int updated = jdbcTemplate.update("update PROPERTIES set PROPERTY_VALUE= ? WHERE PROPERTIES.PROPERTY_NAME=? ", runTimeFormatted, LAST_RUN_TIME_PROPERTY_NAME);

        if (updated == 0) {
            logger.warn("property 'truste-sync-tasks.last-run-time' does not exists, inserting");

            int inserted = jdbcTemplate.update("insert into  PROPERTIES (PROPERTY_NAME, PROPERTY_VALUE, PRIORITY_SCOPE) values(?, ?, ?)", LAST_RUN_TIME_PROPERTY_NAME,
                    runTimeFormatted, 4);
            logger.info("property 'truste-sync-tasks.last-run-time' inserted {}", inserted);
            return;
        }

        if (updated != 1) {
            logger.error("expected exactly one record updated, but updated {} records", updated);
        }
    }

}

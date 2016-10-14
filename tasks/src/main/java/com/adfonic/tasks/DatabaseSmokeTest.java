package com.adfonic.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public final class DatabaseSmokeTest implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSmokeTest.class);

    @Autowired
    private Map<String, DataSource> dataSourcesByName;

    @Override
    public void run() {
        LOG.info("Smoke testing data sources");
        for (Map.Entry<String, DataSource> entry : dataSourcesByName.entrySet()) {
            String name = entry.getKey();
            DataSource dataSource = entry.getValue();
            LOG.info("Smoke testing {}", name);
            Connection conn = null;
            PreparedStatement pst = null;
            ResultSet rs = null;
            try {
                conn = dataSource.getConnection();
                pst = conn.prepareStatement("SELECT 1");
                rs = pst.executeQuery();
                rs.next();
                LOG.info("{} is OK", name);
            } catch (java.sql.SQLException e) {
                throw new IllegalStateException("Failure detected on " + name, e);
            } finally {
                DbUtils.closeQuietly(conn, pst, rs);
            }
        }
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            SpringTaskBase.runBean(DatabaseSmokeTest.class, "adfonic-toolsdb-context.xml", "adfonic-admreportingdb-context.xml");
        } catch (Throwable e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}

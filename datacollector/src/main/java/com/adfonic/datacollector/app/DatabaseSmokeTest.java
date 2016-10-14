package com.adfonic.datacollector.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;

public final class DatabaseSmokeTest extends SpringAppBase implements Runnable {

    private static final transient Logger LOG = Logger.getLogger(DatabaseSmokeTest.class.getName());

    @Autowired
    private Map<String, DataSource> dataSourcesByName;

    public DatabaseSmokeTest() {
        super("adfonic-datacollector-db-context.xml");
    }

    @Override
    public void run() {
        LOG.info("Smoke testing data sources");
        for (Map.Entry<String, DataSource> entry : dataSourcesByName.entrySet()) {
            String name = entry.getKey();
            DataSource dataSource = entry.getValue();
            LOG.info("Smoke testing " + name);
            Connection conn = null;
            PreparedStatement pst = null;
            ResultSet rs = null;
            try {
                conn = dataSource.getConnection();
                pst = conn.prepareStatement("SELECT 1");
                rs = pst.executeQuery();
                rs.next();
                LOG.info(name + " is OK");
            } catch (java.sql.SQLException e) {
                LOG.log(Level.SEVERE, "Failure detected on " + name, e);
            } finally {
                DbUtils.closeQuietly(conn, pst, rs);
            }
        }
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            new DatabaseSmokeTest().run();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception caught", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}

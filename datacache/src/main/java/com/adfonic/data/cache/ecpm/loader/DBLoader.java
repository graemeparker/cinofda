package com.adfonic.data.cache.ecpm.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

import com.adfonic.domain.cache.ext.util.AdfonicStopWatch;

public abstract class DBLoader {

	private static final transient Logger LOG = Logger.getLogger(DBLoader.class.getName());
	
    protected void loadEntitiesFromDb(DataSource dataSource, AdfonicStopWatch adfonicStopWatch, String taskName, String sql, ReadFromRecordSet readBlock) throws java.sql.SQLException {

        String watchTaskName = "Loading " + taskName;
        adfonicStopWatch.start(watchTaskName);
        LOG.fine(watchTaskName);

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(sql);
        }

        Connection conn = null;
        Statement pst = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = dataSource.getConnection();

            pst = conn.createStatement();
            rs = pst.executeQuery(sql);

            while (rs.next()) {
                if (readBlock.read(rs)) {
                    count++;
                }
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Loaded/cached " + count + " " + taskName + " cache values");
            }
        } finally {
            adfonicStopWatch.stop(watchTaskName);
            DbUtils.closeQuietly(conn, pst, rs);
        }

    }
    
    protected void loadEntitiesFromDb(DataSource dataSource, AdfonicStopWatch adfonicStopWatch, String taskName, PreparedStatement pst, ReadFromRecordSet readBlock) throws java.sql.SQLException {

        String watchTaskName = "Loading " + taskName;
        adfonicStopWatch.start(watchTaskName);
        LOG.fine(watchTaskName);


        Connection conn = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = dataSource.getConnection();
            rs = pst.executeQuery();

            while (rs.next()) {
                if (readBlock.read(rs)) {
                    count++;
                }
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Loaded/cached " + count + " " + taskName + " cache values");
            }
        } finally {
            adfonicStopWatch.stop(watchTaskName);
            DbUtils.closeQuietly(conn, pst, rs);
        }

    }
	
}

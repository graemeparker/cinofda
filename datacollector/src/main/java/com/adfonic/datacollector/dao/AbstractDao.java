package com.adfonic.datacollector.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

public abstract class AbstractDao {

    private final DataSource dataSource;

    protected AbstractDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected final DataSource getDataSource() {
        return dataSource;
    }

    protected long getLastInsertId(Connection conn) throws java.sql.SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT LAST_INSERT_ID()");
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new IllegalStateException("LAST_INSERT_ID() returned nothing");
            }
            return rs.getLong(1);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
        }
    }
}

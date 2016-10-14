package com.adfonic.adserver.stoppages;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

import com.adfonic.adserver.Stoppage;

public class DatabaseStoppageServiceImpl implements StoppagesService {

	private final static Logger LOG = Logger
			.getLogger(DatabaseStoppageServiceImpl.class.getSimpleName());
	
    
    private final DataSource dataSource;

	public DatabaseStoppageServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
	public Map<Long, Stoppage> getAdvertiserStoppages() throws IOException {
		Map<Long, Stoppage> stoppages = new HashMap<Long, Stoppage>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = dataSource.getConnection();
			ps = conn
					.prepareStatement("SELECT ADVERTISER_ID, TIMESTAMP, REACTIVATE_DATE"
							+ " FROM ADVERTISER_STOPPAGE"
							+ " WHERE REACTIVATE_DATE IS NULL OR REACTIVATE_DATE > CURRENT_TIMESTAMP");
			rs = executeQuery(stoppages, ps);
		} catch (java.sql.SQLException e) {
			LOG.log(Level.SEVERE, "Failed to load stoppages", e);
		} finally {
			DbUtils.closeQuietly(conn, ps, rs);
		}

		return stoppages;
	}

	@Override
	public Map<Long, Stoppage> getCampaignStoppages() throws IOException {
		Map<Long, Stoppage> stoppages = new HashMap<Long, Stoppage>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement("SELECT CAMPAIGN_ID, TIMESTAMP, REACTIVATE_DATE"
		              + " FROM CAMPAIGN_STOPPAGE"
		              + " WHERE REACTIVATE_DATE IS NULL OR REACTIVATE_DATE > CURRENT_TIMESTAMP");
			rs = executeQuery(stoppages, ps);
		} catch (java.sql.SQLException e) {
			LOG.log(Level.SEVERE, "Failed to load stoppages", e);
		} finally {
			DbUtils.closeQuietly(conn, ps, rs);
		}

		return stoppages;
	}

	private ResultSet executeQuery(Map<Long, Stoppage> stoppages,
			PreparedStatement ps) throws SQLException {
		ResultSet rs;
		rs = ps.executeQuery();
		while (rs.next()) {
			long advertiserId = rs.getLong(1);
			long timestamp = rs.getTimestamp(2).getTime();
			Long reactivation = rs.getTimestamp(3) != null ? rs
					.getTimestamp(3).getTime() : null;

			stoppages.put(advertiserId, new Stoppage(timestamp,
					reactivation));
		}
		
		rs.close();
		ps.close();
		return rs;
	}
}

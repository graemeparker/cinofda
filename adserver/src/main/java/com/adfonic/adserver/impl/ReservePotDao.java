package com.adfonic.adserver.impl;

import java.util.List;
import java.util.logging.Logger;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.QueryExecutionException;

public class ReservePotDao {

    private static final transient Logger LOG = Logger.getLogger(ReservePotDao.class.getName());

    // 1 week
    private static final int ttl = 7*24*3600 ;

    protected final Cluster cluster;
    protected final Session session;
    protected PreparedStatement psRead;
    protected PreparedStatement psUpdate;
    protected PreparedStatement psInSert;
    private int maxRetries = 30;

    public ReservePotDao(Cluster cluster, Session session) {
        this.cluster = cluster;
        this.session = session;
        initialize();
    }

    private void initialize() {

        ConsistencyLevel read = ConsistencyLevel.QUORUM;
        ConsistencyLevel write = ConsistencyLevel.QUORUM;

        if (psRead == null) {
            String s = "SELECT reserved FROM reserve_pot WHERE id = ?;";
            psRead = session.prepare(s);
            psRead.setConsistencyLevel(read);
        }

        if (psUpdate == null) {
            String s = "UPDATE reserve_pot USING TTL ? set reserved = ? WHERE id = ? IF reserved = ? ;";
            psUpdate = session.prepare(s);
            psUpdate.setConsistencyLevel(write);
        }

        if (psInSert == null) {
            String s = "INSERT into reserve_pot (id,reserved) VALUES(?,?) IF NOT EXISTS USING TTL ?;";
            psInSert = session.prepare(s);
            psInSert.setConsistencyLevel(write);
        }
    }

    private Long getLongOrNull(Row row, String col) {
        long longVal = row.getLong(col);
        if (row.isNull(col)) {
            return null;
        }

        return longVal;
    }

    public Long readReserved(long campaignId) {

        ResultSet rs = session.execute(psRead.bind(campaignId));
        List<Row> rows = rs.all();
        if (!rows.isEmpty()) {
            Row row = rows.get(0);
            Long reserved = getLongOrNull(row, "reserved");
            return reserved;
        }
        return null;
    }

    public boolean insert(long campaignId, long value) {

        ResultSet rs = session.execute(psInSert.bind(campaignId, value, ttl));
        boolean wasApplied = rs.wasApplied();
        return wasApplied;
    }

    public boolean increaseReserved(long campaignId, long delta) {
        if (delta < 0) {
            LOG.warning("campaignId " + campaignId + " delta " + delta + "cant be negative");
            return false;
        }
        if (delta == 0) {
            return true;
        }

        int retry = maxRetries;
        try {
            boolean wasApplied = false;
            do {
                retry--;
                Long oldVal = readReserved(campaignId);
                if (oldVal == null) {
                    boolean succ = insert(campaignId, delta);
                    if (succ) {
                        return true;
                    } else {
                        oldVal = readReserved(campaignId);
                    }
                }
                long newVal = oldVal == null ? delta : oldVal + delta;
                LOG.info("increaseReserved campaignId " + campaignId + " delta " + delta + " oldVal " + oldVal + " newVal " + newVal);

                ResultSet rs = session.execute(psUpdate.bind(ttl, newVal, campaignId, oldVal));
                wasApplied = rs.wasApplied();
                if (wasApplied)
                    return true;

            } while (retry > 0);
        } catch (QueryExecutionException qe) {
            // errorsOccured.incrementAndGet();
            // counterManager.incrementCounter("bm.updateBudget.error." + qe.getMessage());
            LOG.warning("condUpdateReserved failed campaignId " + campaignId + " delta " + delta + " retry " + retry + " " + qe.getMessage());
        }
        return false;
    }

    public long take(long campaignId, final long wanted) {
        if (wanted <= 0) {
            LOG.warning("campaignId " + campaignId + " wanted " + wanted + "must be positive");
            return 0;
        }

        long toTake = wanted;
        int retry = maxRetries;
        try {
            boolean wasApplied = false;
            do {
                retry--;
                Long oldVal = readReserved(campaignId);
                if (oldVal == null) {
                    return 0;
                }
                toTake = java.lang.Math.min(toTake, oldVal);
                if (toTake <= 0) {
                    // nothing to take
                    return 0;
                }
                long newVal = oldVal - toTake;
                LOG.info("take campaignId " + campaignId + " toTake " + toTake + " oldVal " + oldVal + " newVal " + newVal);

                ResultSet rs = session.execute(psUpdate.bind(newVal, campaignId, oldVal));
                wasApplied = rs.wasApplied();
                if (wasApplied)
                    return toTake;

            } while (retry > 0);
        } catch (QueryExecutionException qe) {
            // errorsOccured.incrementAndGet();
            // counterManager.incrementCounter("bm.updateBudget.error." + qe.getMessage());
            LOG.warning("take failed campaignId " + campaignId + " wanted " + wanted + " retry " + retry + " " + qe.getMessage());
        }
        return 0;
    }
}

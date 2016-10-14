package com.adfonic.tasks.xaudit.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.adfonic.domain.Creative;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.domain.PublisherAuditedCreative.Status;
import com.adfonic.jms.StatusChangeMessage;
import com.adfonic.tasks.xaudit.adx.AdXAuditService;

/**
 * @author mvanek
 * 
 * Scheduled task to maintain statuses in PUBLISHER_AUDITED_CREATIVE
 *
 */
public class CreativeAuditStatusTask {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String[] statuses = { PublisherAuditedCreative.Status.CREATION_INITIATED.toString(), PublisherAuditedCreative.Status.LOCAL_INVALID.toString(),
            PublisherAuditedCreative.Status.SUBMIT_FAILED.toString(), PublisherAuditedCreative.Status.PENDING.toString() };

    private final String select = "SELECT * FROM PUBLISHER_AUDITED_CREATIVE WHERE PUBLISHER_ID=? AND (LATEST_FETCH_TIME IS NULL OR LATEST_FETCH_TIME BETWEEN ? AND ?) AND STATUS IN ('"
            + String.join("' ,'", statuses) + "')";

    private final PacRowMapper mapper = new PacRowMapper();

    private final AdXAuditService adxService;

    private final JdbcTemplate jdbc;

    public CreativeAuditStatusTask(AdXAuditService adxService, JdbcTemplate jdbc) {
        this.adxService = adxService;
        this.jdbc = jdbc;
    }

    /**
     * DB update -> DB trigger -> JMS Topic -> here!
     * 
     * This is also called for creatives that are not extenally audited and have no record in PUBLISHER_AUDITED_CREATIVE table
     */
    public void onStatusChange(StatusChangeMessage msg) {
        if ("Creative".equals(msg.getEntityType())) {
            Creative.Status newStatus = Creative.Status.valueOf(msg.getNewStatus());
            if (newStatus == Creative.Status.PENDING || newStatus == Creative.Status.PENDING_PAUSED) {
                String sql = "UPDATE PUBLISHER_AUDITED_CREATIVE SET STATUS='" + PublisherAuditedCreative.Status.LOCAL_INVALID
                        + "', LATEST_FETCH_TIME=?, LAST_AUDIT_REMARKS=? WHERE CREATIVE_ID=?";
                Date now = new Date();
                String message = "Creative was changed";
                // Scheduled check will take care of resubmitting it 
                int count = jdbc.update(sql, now, message, msg.getEntityId());
                if (count != 0) {
                    log.debug("External audit record invalidated for creative: " + msg.getEntityId());
                } else {
                    log.debug("External audit record not found for creative: " + msg.getEntityId());
                }
            }
        }
    }

    /**
     * Every X minutes...
     */
    public void onScheduled() {
        // ignore too old and too fresh entries...
        Date now = new Date();
        Date sevenDaysAgo = date(now, -7, Calendar.DAY_OF_YEAR);
        Date fewMinutesAgo = date(now, -15, Calendar.MINUTE);
        List<Pac> list = jdbc.query(select, mapper, adxService.getPublisherId(), sevenDaysAgo, fewMinutesAgo);
        int errors = 0;
        for (Pac pac : list) {
            try {
                adxService.onScheduledCheck(pac.creativeId);
            } catch (Exception x) {
                log.error("Failed to check audit status for " + pac, x);
                if (++errors > 10) {
                    log.warn("Leaving beacuse of too many errors during audit status check");
                }
            }
        }
        log.info("Processed " + list.size() + " creative audit records");
    }

    private Date date(Date from, int amount, int unit) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);
        calendar.add(unit, amount);
        return calendar.getTime();

    }

    static class PacRowMapper implements RowMapper<Pac> {

        @Override
        public Pac mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Pac(rs.getLong("CREATIVE_ID"), rs.getLong("PUBLISHER_ID"), PublisherAuditedCreative.Status.valueOf(rs.getString("STATUS")), rs.getDate("LATEST_FETCH_TIME"));
        }
    }

    static class Pac {
        final long creativeId;
        final long publisherId;
        final PublisherAuditedCreative.Status status;
        final Date lastFetchTs;

        public Pac(long creativeId, long publisherId, Status status, Date lastFetchTs) {
            this.creativeId = creativeId;
            this.publisherId = publisherId;
            this.lastFetchTs = lastFetchTs;
            this.status = status;
        }

        public long getCreativeId() {
            return creativeId;
        }

        public long getPublisherId() {
            return publisherId;
        }

        public PublisherAuditedCreative.Status getStatus() {
            return status;
        }

        public Date getLastFetchTs() {
            return lastFetchTs;
        }

        @Override
        public String toString() {
            return "Pac {creativeId=" + creativeId + ", publisherId=" + publisherId + ", status=" + status + ", lastFetchTs=" + lastFetchTs + "}";
        }

    }

}

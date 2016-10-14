package com.adfonic.tasks.combined;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.adfonic.util.ConfUtils;

/**
 * Deletes NotificationFlag entries that are past their expiration date.
 * These are harmless but tend to clog up the database.
 */
@Component
public class ExpiredNotificationDeleter {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource dataSource;

    //@Scheduled(fixedRate=3600100)
    public void deleteExpiredNotifications() {
        LOG.debug("Starting expired notification deleter");

        Connection conn = null;
        PreparedStatement pst = null;
        try {
            conn = dataSource.getConnection();
            pst = conn.prepareStatement("DELETE FROM NOTIFICATION_FLAG WHERE EXPIRATION_DATE <= CURRENT_TIMESTAMP");
            pst.executeUpdate();
        } catch (java.sql.SQLException e) {
            LOG.error("Failed to delete expired notification flags {}", e);
        } finally {
            DbUtils.closeQuietly(conn, pst, null);
        }

        LOG.debug("Finished expired notification deleter");
    }
}

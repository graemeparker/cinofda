package com.adfonic.tasks.combined;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.StatusChangeMessage;
import com.adfonic.util.ConfUtils;

/**
 * Inserts into STATUS_CHANGE table are doen using DB triggers on CREATIVE, CAMPAIGN, AD_SPACE, PUBLICATION
 * This job periadicaly reads, sends @see StatusChangeMessage into JMS Topic and deletes records
 */
@Component
public class StatusChangeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StatusChangeProcessor.class.getName());

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource dataSource;
    @Autowired
    private JmsUtils jmsUtils;
    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_TEMPLATE)
    private JmsTemplate centralJmsTemplate;
    @Value("${StatusChangeProcessor.maxAgeMs}")
    private long maxAgeMs;

    //@Scheduled(fixedRate=10000)
    public void runPeriodically() {
        LOG.debug("Starting STATUS_CHANGE processing cycle");

        // Calculate a creationTime threshold based on max allowable age
        long creationTimeThreshold = System.currentTimeMillis() - maxAgeMs;

        // Keep track of the max ID we encounter, which we'll use in the DELETE
        Long maxIdProcessed = null;

        // Build a LinkedHashMap whose key is (entity type + entity id), and whose
        // value is (new status).  We use LinkedHashMap in order to respect FIFO
        // order...i.e. earlier status changes will get conveyed to adserver first.
        // And what we're doing by using a Map like this is allowing more recent
        // changes for the same entity (class + id) to supersede older changes.
        // In other words, if there were four status changes for the same entity,
        // you don't want to flood adserver with all four changes...just collapse
        // them into a single change, the most recent one.  The Map, along with
        // ordering by CREATION_TIME, accomplishes that.
        Map<String, String> changeMap = new LinkedHashMap<String, String>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pst = conn.prepareStatement("SELECT ID, CREATION_TIME, ENTITY_TYPE, ENTITY_ID, OLD_STATUS, NEW_STATUS FROM STATUS_CHANGE ORDER BY CREATION_TIME");
            rs = pst.executeQuery();
            while (rs.next()) {
                long id = rs.getLong(1);
                Date creationTime = rs.getTimestamp(2);
                String entityType = rs.getString(3);
                long entityId = rs.getLong(4);
                //String oldStatus = rs.getString(5);
                String newStatus = rs.getString(6);

                if (maxIdProcessed == null || id > maxIdProcessed) {
                    maxIdProcessed = id;
                }

                // As each row is read, ensure that its CREATION_TIME is not prior to some
                // threshold based on a "max age" configurable property.  The purpose of
                // this is...if combined task is down for some period of time and then
                // comes back online, we don't want "old queued changes" to get sent to
                // adserver, since they've probably already been conveyed via a domain
                // cache update.  So beyond some age threshold, just discard the changes.
                if (creationTime.getTime() < creationTimeThreshold) {
                    LOG.debug("Discarding old row id={}, creationTime={}", id, creationTime);
                    continue;
                }

                // Track the status change in our most recent/FIFO change map
                changeMap.put(entityType + ":" + entityId, newStatus);
            }
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pst);

            // For each entry in the Map, publish a message to the new "adfonic.status.change" JMS topic.
            for (Map.Entry<String, String> entry : changeMap.entrySet()) {
                String[] keyToks = entry.getKey().split(":");
                final String entityType = keyToks[0];
                final long entityId = Long.parseLong(keyToks[1]);
                final String newStatus = entry.getValue();
                LOG.debug("Publishing status change message for {} id={}, newStatus={}", entityType, entityId, newStatus);

                jmsUtils.sendObject(centralJmsTemplate, JmsResource.STATUS_CHANGE_TOPIC, new StatusChangeMessage(entityType, entityId, newStatus));
            }

            // Delete all rows from STATUS_CHANGE that we processed
            if (maxIdProcessed != null) {
                String deleteSql = "DELETE FROM STATUS_CHANGE WHERE ID <= " + maxIdProcessed;
                LOG.debug(deleteSql);
                pst = conn.prepareStatement(deleteSql);
                pst.executeUpdate();
            }
        } catch (java.sql.SQLException e) {
            LOG.error("Failed to process status changes {}", e);
            return;
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        LOG.debug("Finished cycle with " + changeMap.size() + " changes");
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            com.adfonic.tasks.SpringTaskBase.runBean(StatusChangeProcessor.class, "adfonic-toolsdb-context.xml", "adfonic-tasks-context.xml");
            // Fcuk - Spring environment is not populated correctly
            // com.adfonic.tasks.SpringTaskBase.runBean(StatusChangeProcessor.class, SingleTasksSpringConfig.class, ToolsDbSpringConfig.class);
        } catch (Exception e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}

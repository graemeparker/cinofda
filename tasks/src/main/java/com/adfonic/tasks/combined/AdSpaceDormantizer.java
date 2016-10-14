package com.adfonic.tasks.combined;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Publication;
import com.adfonic.util.ConfUtils;

@Component
public class AdSpaceDormantizer {

    private static final Logger LOG = LoggerFactory.getLogger(AdSpaceDormantizer.class.getName());

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource toolsDataSource;
    @Autowired
    @Qualifier(ConfUtils.ADM_REPORTING_DS)
    private DataSource admReportingDataSource;
    @Value("${AdSpaceDormantizer.enabled}")
    private boolean enabled;
    @Value("${AdSpaceDormantizer.thresholdDays}")
    private int thresholdDays;
    @Value("${AdSpaceDormantizer.whitelist.AdSpace.ids}")
    private String whitelistAdSpaceIds;
    @Value("${AdSpaceDormantizer.whitelist.Company.ids}")
    private String whitelistCompanyIds;

    // Run every 3 hours
    //@Scheduled(fixedRate=10800000)
    public void runPeriodically() {
        if (!enabled) {
            LOG.debug("Disabled, skipping");
            return;
        }

        LOG.debug("Looking for newly DORMANT AdSpaces");

        String sqlSelect = "SELECT AD_SPACE.ID" + " FROM AD_SPACE" + " INNER JOIN PUBLICATION ON PUBLICATION.ID=AD_SPACE.PUBLICATION_ID"
                + " INNER JOIN PUBLISHER ON PUBLISHER.ID=PUBLICATION.PUBLISHER_ID" + " WHERE AD_SPACE.STATUS NOT IN ('" + AdSpace.Status.DELETED + "','" + AdSpace.Status.DORMANT
                + "')" + " AND PUBLICATION.STATUS IN ('"
                + Publication.Status.ACTIVE
                + "')"
                // Only AdSpaces older than the time threshold should be considered
                + " AND AD_SPACE.CREATION_TIME <= (CURRENT_TIMESTAMP - INTERVAL "
                + thresholdDays
                + " DAY)"
                // AdSpaces that were reactivated after being marked dormant get another
                // full time threshold before being considered dormant again
                + " AND (AD_SPACE.REACTIVATION_TIME IS NULL OR AD_SPACE.REACTIVATION_TIME <= (CURRENT_TIMESTAMP - INTERVAL " + thresholdDays + " DAY))"
                + " AND AD_SPACE.ID NOT IN (" + "SELECT DISTINCT AD_SPACE_ID" + " FROM agg_l_pub_ADM" + " WHERE GMT_TIME_ID >= DATE_FORMAT(CURRENT_TIMESTAMP - INTERVAL "
                + thresholdDays + " DAY, '%Y%m%d%h')" + ")";
        if (StringUtils.isNotBlank(whitelistAdSpaceIds)) {
            // BE-80 - apply whitelist rules for AdSpace
            sqlSelect += " AND AD_SPACE.ID NOT IN (" + whitelistAdSpaceIds + ")";
        }
        if (StringUtils.isNotBlank(whitelistCompanyIds)) {
            // BE-80 - apply whitelist rules for Company
            sqlSelect += " AND PUBLISHER.COMPANY_ID NOT IN (" + whitelistCompanyIds + ")";
        }
        LOG.debug(sqlSelect);

        Set<Long> adSpaceIds = new TreeSet<Long>();
        Connection admReportingConn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            admReportingConn = admReportingDataSource.getConnection();
            pst = admReportingConn.prepareStatement(sqlSelect);
            rs = pst.executeQuery();
            while (rs.next()) {
                adSpaceIds.add(rs.getLong(1));
            }
        } catch (java.sql.SQLException e) {
            LOG.error("SQL select failed: {} {}", sqlSelect, e);
            return;
        } finally {
            DbUtils.closeQuietly(admReportingConn, pst, rs);
        }

        if (adSpaceIds.isEmpty()) {
            LOG.debug("No dormant-eligible AdSpaces discovered");
            return;
        } else {
            LOG.info("Found {} dormant-eligible AdSpaces", adSpaceIds.size());
        }

        Connection toolsConn = null;
        try {
            toolsConn = toolsDataSource.getConnection();
            for (Long adSpaceId : adSpaceIds) {
                LOG.debug("Updating AdSpace id={} status to DORMANT", adSpaceId);
                String sqlUpdate = "UPDATE AD_SPACE SET STATUS='" + AdSpace.Status.DORMANT + "' WHERE ID=" + adSpaceId;
                LOG.debug(sqlUpdate);
                try {
                    pst = toolsConn.prepareStatement(sqlUpdate);
                    pst.execute();
                } catch (Exception e) {
                    LOG.error("SQL update failed: {} {}", sqlUpdate, e);
                    continue;
                }
            }
        } catch (java.sql.SQLException e) {
            LOG.error("Status update failed {}", e);
            return;
        } finally {
            DbUtils.closeQuietly(toolsConn, pst, null);
        }

        LOG.debug("Finished cycle");
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            com.adfonic.tasks.SpringTaskBase.runBean(AdSpaceDormantizer.class, "adfonic-toolsdb-context.xml", "adfonic-admreportingdb-context.xml", "adfonic-tasks-context.xml");
        } catch (Exception e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}

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
 * Clears completed/stopped campaigns from publication collections
 * @author dcheckoway
 */
@Component
public class CleanupCompletedCampaigns {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource dataSource;

    //@Scheduled(fixedRate=3600000)
    public void runPeriodically() {
        LOG.debug("Cleaning up completed campaigns");

        Connection conn = null;
        PreparedStatement pst = null;
        try {
            conn = dataSource.getConnection();
            pst = conn
                    .prepareStatement("delete PUBLICATION_APPROVED_CREATIVE from PUBLICATION_APPROVED_CREATIVE inner join CREATIVE on CREATIVE_ID = CREATIVE.ID inner join CAMPAIGN on CAMPAIGN_ID = CAMPAIGN.ID where (CAMPAIGN.STATUS = 'COMPLETED' or CAMPAIGN.STATUS = 'STOPPED' or CREATIVE.STATUS = 'STOPPED')");
            int rows = pst.executeUpdate();
            if (rows > 0) {
                LOG.info("Deleted {} rows from PUBLICATION_APPROVED_CREATIVE", rows);
            }

            pst.close();

            pst = conn
                    .prepareStatement("delete PUBLICATION_DENIED_CREATIVE from PUBLICATION_DENIED_CREATIVE inner join CREATIVE on CREATIVE_ID = CREATIVE.ID inner join CAMPAIGN on CAMPAIGN_ID = CAMPAIGN.ID where (CAMPAIGN.STATUS = 'COMPLETED' or CAMPAIGN.STATUS = 'STOPPED' or CREATIVE.STATUS = 'STOPPED')");
            rows = pst.executeUpdate();
            if (rows > 0) {
                LOG.info("Deleted {} rows from PUBLICATION_DENIED_CREATIVE", rows);
            }
        } catch (java.sql.SQLException e) {
            LOG.error("Failure detected {}", e);
        } finally {
            DbUtils.closeQuietly(conn, pst, null);
        }

        LOG.debug("Finished cleaning up completed campaigns");
    }
}

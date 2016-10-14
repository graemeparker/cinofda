package com.adfonic.tasks.combined.consumers;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AdSpace;
import com.adfonic.util.ConfUtils;

@Component
public class DormantAdSpaceReactivator {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource dataSource;

    public void reactivateDormantAdSpace(String adSpaceExternalId) {
        LOG.debug("Reactivating DORMANT AdSpace: {}", adSpaceExternalId);

        boolean reactivated;

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement("UPDATE AD_SPACE SET STATUS=?, REACTIVATION_TIME=CURRENT_TIMESTAMP WHERE EXTERNAL_ID=? AND STATUS=?");
            ps.setString(1, AdSpace.Status.VERIFIED.name());
            ps.setString(2, adSpaceExternalId);
            ps.setString(3, AdSpace.Status.DORMANT.name());
            reactivated = ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOG.error("Failed to reactivate DORMANT AdSpace: {} {}", adSpaceExternalId, e);
            return;
        } finally {
            DbUtils.closeQuietly(conn, ps, null);
        }

        if (reactivated) {
            LOG.debug("Reactivated DORMANT AdSpace: {}", adSpaceExternalId);
        } else {
            LOG.debug("AdSpace must already have been reactivated: {}", adSpaceExternalId);
        }
    }
}

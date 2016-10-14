package com.adfonic.tasks.combined.consumers;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AdSpace;
import com.adfonic.jms.AdSpaceVerifiedMessage;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.util.ConfUtils;

@Component
public class AdSpaceVerifier {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    private DataSource dataSource;
    @Autowired
    private JmsUtils jmsUtils;
    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_TEMPLATE)
    private JmsTemplate centralJmsTemplate;

    public void onAdSpaceVerified(AdSpaceVerifiedMessage msg) {
        LOG.debug("Handling {}", msg);

        boolean markedVerified;

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement("UPDATE AD_SPACE SET STATUS=? WHERE ID=? AND STATUS=?");
            ps.setString(1, AdSpace.Status.VERIFIED.name());
            ps.setLong(2, msg.getAdSpaceId());
            ps.setString(3, AdSpace.Status.UNVERIFIED.name());
            markedVerified = ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOG.warn("Failed to mark AdSpace id={} verified {}", msg.getAdSpaceId(), e);
            return;
        } finally {
            DbUtils.closeQuietly(conn, ps, null);
        }

        if (markedVerified) {
            LOG.info("Marked AdSpace id={} verified", msg.getAdSpaceId());

            // Bugzilla 1468 - don't send the Mondrian cache flush message unless we actually updated
            // the AdSpace's status.  This check de-dups the noise being heard by tools and admin.
            // Notify anybody using Mondrian that they should flush their cache
            // The message itself isn't important, just send any old object
            // as the message body (we use boolean)
            jmsUtils.sendObject(centralJmsTemplate, JmsResource.MONDRIAN_CACHE_FLUSH_TOPIC, Boolean.TRUE);
        }
    }
}

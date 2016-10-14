package com.adfonic.adserver.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdSpaceUtils;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;

@Component
public class AdSpaceUtilsImpl implements AdSpaceUtils {

    private static final transient Logger LOG = Logger.getLogger(AdSpaceUtilsImpl.class.getName());

    // How long we keep records of having queued a reactivation, after which
    // we'll queue another request.
    private static final long REACTIVATION_RECORD_TTL = 3600000;

    private final ConcurrentHashMap<String, Long> reactivationRecord = new ConcurrentHashMap<String, Long>();
    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_TEMPLATE)
    private JmsTemplate centralJmsTemplate;

    @Autowired
    private JmsUtils jmsUtils;

    /** {@inheritDoc} */
    @Override
    public void reactivateDormantAdSpace(String adSpaceExternalId) {
        long expireTime = System.currentTimeMillis() + REACTIVATION_RECORD_TTL;
        Long previousExpireTime = reactivationRecord.putIfAbsent(adSpaceExternalId, expireTime);
        if (previousExpireTime != null) {
            // See if the previously queued request has already "expired"
            if (previousExpireTime > System.currentTimeMillis()) {
                // Nope it's still good...no need to re-queue
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Reactivation of DORMANT AdSpace " + adSpaceExternalId + " already queued");
                }
                return;
            } else {
                // It has expired...let's issue a new request...and track the new expire time
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Reactivation of DORMANT AdSpace " + adSpaceExternalId + " previously queued but expired");
                }
                reactivationRecord.put(adSpaceExternalId, expireTime);
            }
        }

        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Queueing reactivation of DORMANT AdSpace " + adSpaceExternalId);
        }

        jmsUtils.sendObject(centralJmsTemplate, JmsResource.ADSPACE_REACTIVATE, adSpaceExternalId);
    }
}
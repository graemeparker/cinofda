package com.adfonic.tasks.whatsup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.domain.AdserverShard;
import com.adfonic.domain.AdserverStatus;
import com.adfonic.tasks.SpringTaskBase;
import com.byyd.middleware.integrations.service.IntegrationsManager;

public class WhatsUpService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsUpService.class.getName());

    @Autowired
    private WhatsUpManager whatsUpManager;

    @Autowired
    private IntegrationsManager integrationsManager;

    @Value("${WhatsUp.statusUpdateTimeout}")
    private long statusUpdateTimeout;

    @Override
    public void run() {
        List<AdserverStatus> adserverStatuses = integrationsManager.getAllStatuses();
        List<AdserverShard> adserverShards = integrationsManager.getAllShards();
        whatsUpManager.ping(adserverStatuses, adserverShards);
        try {
            Thread.sleep(statusUpdateTimeout);
            integrationsManager.updateAllStatuses(whatsUpManager.getPingUpdate());
        } catch (InterruptedException e) {
            LOG.error("Service interrupted {}", e);
        }
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            SpringTaskBase.runBean(WhatsUpService.class, "adfonic-toolsdb-context.xml", "adfonic-tasks-context.xml", "adfonic-whatsup-context.xml");
        } catch (Exception e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}

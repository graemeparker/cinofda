package com.adfonic.tasks.whatsup;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AdserverShard;
import com.adfonic.domain.AdserverStatus;

@Component
public class WhatsUpManager {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Value("${WhatsUp.connectionTimeout}")
    private int connectionTimeout;

    @Value("${WhatsUp.readTimeout}")
    private int readTimeout;

    @Value("${WhatsUp.ping.url}")
    private String pingUrl;

    private List<WhatsUpConnection> connections = new ArrayList<>();
    private List<AdserverStatus> adserverStatuses = new ArrayList<>();

    public void ping(List<AdserverStatus> adserverStatuses, List<AdserverShard> adserverShards) {
        for (AdserverStatus adserverStatus : adserverStatuses) {
            LOG.debug("Pinging AdServer : {}", adserverStatus.getName());
            WhatsUpConnection connection = new WhatsUpConnection(adserverStatus, String.format(pingUrl, adserverStatus.getName()), connectionTimeout, readTimeout, adserverShards);
            connection.run();
            connections.add(connection);
        }
    }

    public List<AdserverStatus> getPingUpdate() {
        for (WhatsUpConnection connection : connections) {
            adserverStatuses.add(connection.getAdserverStatus());
        }
        return adserverStatuses;
    }
}

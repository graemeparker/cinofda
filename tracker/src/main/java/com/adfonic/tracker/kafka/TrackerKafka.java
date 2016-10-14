package com.adfonic.tracker.kafka;

import net.byyd.archive.model.v1.AdEvent;

public interface TrackerKafka {
    /**
     * Send an AdEvent to the embedded broker's queue
     */
    void logAdEvent(AdEvent adEvent);
}

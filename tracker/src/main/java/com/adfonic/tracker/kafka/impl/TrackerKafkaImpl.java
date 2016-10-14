package com.adfonic.tracker.kafka.impl;

import kafka.common.KafkaException;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import net.byyd.archive.model.v1.AdEvent;
import net.byyd.archive.model.v1.ArchiveV1JsonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.tracker.kafka.TrackerKafka;

public class TrackerKafkaImpl implements TrackerKafka {
    private static final transient Logger LOG = LoggerFactory.getLogger(TrackerKafkaImpl.class.getName());

    @Autowired
    private AdEventFactory adEventFactory;

    private Producer<String, String> kafkaLogger;
    private boolean kafkaLoggerEnabled;
    private String kafkaTopicPrefix;
    private String kafkaTopicPostfix;
    private String environment;

    private ArchiveV1JsonWriter writer = new ArchiveV1JsonWriter();
    private ProducerConfig config;

    public TrackerKafkaImpl(ProducerConfig config, String kafkaTopicPrefix, String kafkaTopicPosfix, String environment, boolean kafkaLoggerEnabled) {
        this.kafkaTopicPrefix = kafkaTopicPrefix;
        this.kafkaTopicPostfix = kafkaTopicPosfix;
        this.environment = environment;
        this.kafkaLoggerEnabled = kafkaLoggerEnabled;
        recreateKafkaLogger(config);
    }

    /** {@inheritDoc} */
    @Override
    public void logAdEvent(AdEvent adEvent) {
        if (kafkaLoggerEnabled) {
            LOG.debug("Sending an AdEvent to kafka topic, adAction={}", adEvent.getAdAction());
            StringBuilder sb = new StringBuilder();
            writer.write(adEvent, sb);
            String json = sb.toString();
            try {
                String topic = kafkaTopicPrefix + "." + adEvent.getAdAction().getShortName() + "_" + environment + "_" + kafkaTopicPostfix;
                kafkaLogger.send(new KeyedMessage<String, String>(topic, json));
            } catch (KafkaException ke) {
                LOG.error("Unable to send message to kafka: " + json);
                recreateKafkaLogger(config);
            }
        }
    }

    private void recreateKafkaLogger(ProducerConfig config) {
        if (kafkaLogger != null) {
            try {
                kafkaLogger.close();
            } catch (Throwable t) {
                LOG.warn("Unable to close kafka logger: " + t.getMessage());
            }
        }

        kafkaLogger = new Producer<String, String>(config);
    }
}

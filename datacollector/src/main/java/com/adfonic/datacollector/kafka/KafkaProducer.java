package com.adfonic.datacollector.kafka;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.kafka.support.KafkaHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    private static final transient Logger LOG = Logger.getLogger(KafkaConsumer.class.getName());
    
    @Autowired
    private MessageChannel inputToKafka;

    public void sendMessage(String message, String topic) {
    
        try {
            inputToKafka.send(MessageBuilder.withPayload(message)
                        .setHeader(KafkaHeaders.TOPIC, topic).build());
        } catch (Exception e) {
            LOG.severe("Failed sending " + message + " due to  " + e.getMessage());
        }
    }
}